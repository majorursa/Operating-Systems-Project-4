/* $Id: Kernel.java,v 1.17 2004/03/31 17:05:43 solomon Exp solomon $ */

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/** A simple kernel simulation.
 *
 * <p>
 * There is only one public interface to this class: interrupt().
 * System calls, disk notification, and power on messages all arrive
 * by way of this function.
 * <p>
 * See the list of <samp>SYSCALL_XXX</samp> constants to learn what
 * system calls are currently supported.
 *
 * @see Disk
 * @see Library
 */
public class Kernel {

    //////////////// Values for the first parameter ("kind") to interrupt()

    /** An interrupt kind indicating that a user program caused the interrupt.
     * <ul>
     * <li><b>Parameter i1</b> -- a valid system call number.
     * </ul>
     * Other parameters depend on the call number.
     */
    public static final int INTERRUPT_USER = 0;

    /** An interrupt kind indicating that a disk caused the interrupt.
     * All other parameters will be null or zero.
     */
    public static final int INTERRUPT_DISK = 1;

    /** An interrupt kind indicating that the system just started.
    * The Kernel should set up any internal state and
    * begin executing the first program.
    * <ul>
    * <li><b>Parameter i1</b> --  the number of blocks to use in the
    * disk cache.
    * <li><b>Parameter o1</b> -- an instance of Disk to use as the disk.
    * <li><b>Parameter o2</b> -- a String containing the name of the shell.
    * </ul>
    */
    public static final int INTERRUPT_POWER_ON = 2;

    //////////////// Values for the second parameter ("i1") for USER interrupts

    /** System call to output text on the console.
     * <ul>
     * <li><b>Parameter o1</b>  -- A string to display
     * <li><b>Returns</b> -- Zero.
     * </ul>
     */
    public static final int SYSCALL_OUTPUT = 0;

    /** System call to read text from the console.
     * This function returns when the user presses [Enter].
     * <ul>
     * <li><b>Parameter o1</b> -- A StringBuffer to fill with input text.
     * <li><b>Returns</b> -- Zero, ERROR_BAD_ARGUMENT, ERROR_END_OF_FILE,
     * or ERROR_IO.
     * </ul>
     */
    public static final int SYSCALL_INPUT = 1;

    /** System call to execute a new program.
     * The new program will run in parallel to the current program.
     * <ul>
     * <li><b>Parameter o1</b> - The name of a Java class to execute.
     * <li><b>Parameter o2</b> - An array for String arguments.
     * <li><b>Returns</b> - A non-negative process id or ERROR_BAD_ARGUMENT,
     * ERROR_NO_CLASS, ERROR_NO_MAIN, or ERROR_BAD_COMMAND.
     * </ul>
     */
    public static final int SYSCALL_EXEC = 2;

    /** System call to wait for a process to terminate.
     * This call will not return until the indicated process has
     * run to completion.
     * <ul>
     * <li><b>Parameter i2</b> - the process id to wait for.
     * <li><b>Returns</b> -- Zero or ERROR_NO_SUCH_PROCESS.
     * </ul>
     */
    public static final int SYSCALL_JOIN = 3;

    /** System call to get time of day
     * <ul>
     * <li><b>Returns</b> -- Zero or ERROR_NO_SUCH_PROCESS.
     * </ul>
     */
    public static final int SYSCALL_GET_TIME = 4;

    /** System call to get disk block count
     */
    public static final int SYSCALL_GET_BLOCK_COUNT = 5;

    /** System call to get block size in bytes
     */
    public static final int SYSCALL_GET_BLOCK_SIZE = 6;

    /** System call to get block size in bytes
     */
    public static final int SYSCALL_READ_DISK_BLOCK = 7;

    /** System call to get block size in bytes
     */
    public static final int SYSCALL_WRITE_DISK_BLOCK = 8;

    //////////////// Error codes returned by interrupt()

    /** An error code indicating that one of the system call parameters made no
     * sense.
     */
    public static final int ERROR_BAD_ARGUMENT = -1;

    /** An error code indicating that the class name passed to SYSCALL_EXEC
     * could not be found.
     */
    public static final int ERROR_NO_CLASS = -2;

    /** An error code indicating that the class name passed to SYSCALL_EXEC
     * named a class with no appropriate main() method.
     */
    public static final int ERROR_NO_MAIN = -3;

    /** An error code indicating some unspecified problem running the class
     * passed SYSCALL_EXEC.
     */
    public static final int ERROR_BAD_COMMAND = -4;

    /** An error code indicating that one parameter was too big or too small */
    public static final int ERROR_OUT_OF_RANGE = -5;

    /** An error code indicating that end of file was reached. */
    public static final int ERROR_END_OF_FILE = -6;

    /** An error code indicating that somthing went wrong during an I/O
     * operation.
     */
    public static final int ERROR_IO = -7;

    /** An error code indicating that a child program caused an exception and
     * crashed.
     */
    public static final int ERROR_IN_CHILD = -8;

    /** An error code indicating an attempt to join with a non-existant process
     */
    public static final int ERROR_NO_SUCH_PROCESS = -9;

    //////////////// Transient state of the kernel

    /** The disk to be used */
    private static Disk disk;

    /** The size of the disk cache */
    private static int cacheSize;

    /** Elevator monitor to keep track of requests */
    private static Elevator elev;

    //////////////// Methods

    /** This is the only entry into the kernel.
    * <p>A user may call this function to perform a system call.
    * In that case, set <tt>kind</tt> to <tt>INTERRUPT_USER</tt>
    * and set <tt>i1</tt> to the system call number.  Other
    * parameters should be set as the system call requires.
    * <p>
    * A disk may call this function to indicate the current operation
    * has completed.  In that case, <tt>kind</tt> will be
    * <tt>INTERRUPT_DISK</tt> and all parameters will be zero or null.
    * <br>
    * <b>Important:</b> If the Disk calls <tt>interrupt()</tt>, the
    * Kernel should take care of business and return from the interrupt
    * as soon as possible.  All Disk I/O is halted while the interrupt is
    * being processed.
    * <p>
    * The boot code may call this function to indicate that the computer
    * has been turned on and it is time to start the first program
    * and use the disk.  In that case, <tt>kind</tt> will be
    * <tt>INTERRUPT_POWER_ON</tt>, o1 will point to the Disk to be
    * used, o2 will be a String containing the name of the shell to use,
    * i1 will indicate the size of the buffer cache,
    * and all other parameters will be zero or null.
    * <p>
    * Since different system calls require different parameters, this
    * method has a variety of arguments of various types.  Any one
    * system call will use at most a few of them.  The others should be
    * zero or null.
    *
    * @param kind the kind of system call, one of the
    *   <samp>INTERRUPT_XXX</samp> codes.
    * @param i1 the first integer parameter.  If <samp>kind ==
    *   INTERRUPT_USER</samp>, <samp>i1</samp> should be one of the
    *   <samp>SYSTEM_XXX</samp> codes to indicate which system call is being
    *   invoked.
    * @param i2 another integer parameter.
    * @param o1 a parameter of some object type.
    * @param o2 another parameter of some object type.
    * @param a a byte-array parameter (generally used for binary input/output).
    * 
    * @return a negative number indicating an error code, or other
    * values depending on the system call.
    */
    public static int interrupt(int kind, int i1, int i2,
            Object o1, Object o2, byte a[])
    {
        try {
            switch (kind) {
            case INTERRUPT_USER:
                switch (i1) {
                case SYSCALL_OUTPUT:
                    return doOutput((String)o1);

                case SYSCALL_INPUT:
                    return doInput((StringBuffer)o1);

                case SYSCALL_EXEC:
                    return doExec((String)o1,(String[])o2);

                case SYSCALL_JOIN:
                    return doJoin(i2);

                case SYSCALL_GET_TIME:
                    return doGetTime((long[])o1);

                case SYSCALL_GET_BLOCK_SIZE:
                    return doGetDiskBlockSize();
                    
                case SYSCALL_GET_BLOCK_COUNT:
                    return doGetDiskBlockCount();

                case SYSCALL_READ_DISK_BLOCK:
                    return doReadDiskBlock(i2,(byte[])o1);

                case SYSCALL_WRITE_DISK_BLOCK:
                    return doWriteDiskBlock(i1,(byte[])o1);
                 
                default:
                    return ERROR_BAD_ARGUMENT;
                }

            case INTERRUPT_DISK:
                break;

            case INTERRUPT_POWER_ON:
                doPowerOn(i1, o1, o2);
                doShutdown();
                break;

            default:
                return ERROR_BAD_ARGUMENT;
            } // switch (kind)
        } catch (Exception e) {
            // Most likely, we arrived here due to a bad cast. 
            e.printStackTrace();
            return ERROR_BAD_ARGUMENT;
        }
        return 0;
    } // interrupt

    /** Performs the actions associated with a POWER_ON interrupt.
     * @param i1 the first int parameter to the interrupt (the disk cache size)
     * @param o1 the first Object parameter to the interrupt (the Disk).
     * @param o2 the second Object parameter to the interrupt (the shell
     * command-line).
     */
    private static void doPowerOn(int i1, Object o1, Object o2) {
        cacheSize = i1;
        disk = (Disk)o1;
        String shellCommand = (String) o2;

        doOutput("Kernel: Disk is " + disk.DISK_SIZE + " blocks\n");
        doOutput("Kernel: Disk cache size is " + i1 + " blocks\n");
        doOutput("Kernel: Loading initial program.\n");
        
        elev =  new Elevator(disk); 
        StringTokenizer st = new StringTokenizer(shellCommand);
        int n = st.countTokens();
        if (n < 1) {
            doOutput("Kernel: No shell specified\n");
            System.exit(1);
        }
            
        String shellName = st.nextToken();
        String[] args = new String[n - 1];
        for (int i = 1; i < n; i++) {
            args[i - 1] = st.nextToken();
        }

        if (doExecAndWait(shellName, args) < 0) {
            doOutput("Kernel: Unable to start " + shellCommand + "!\n");
            System.exit(1);
        } else {
            doOutput("Kernel: " + shellCommand + " has terminated.\n");
        }

        Launcher.joinAll();
    } // doPowerOn

    /** Does any "shutdown" activities required after all activities started by
     * a POWER_ON interrupt have completed.
     */
    private static void doShutdown() {
        disk.flush();
    } // doShutdown()

    /** Displays a message on the console.
     * @param msg the message to display
     */
    private static int doOutput(String msg) {
        System.out.print(msg);
        return 0;
    } // doOutput

    private static BufferedReader br
        = new BufferedReader(new InputStreamReader(System.in));

    /** Reads a line from the console into a StringBuffer.
     * @param sb a place to put the line of input.
     */
    private static int doInput(StringBuffer sb) {
        try {
            String s = br.readLine();
            if (s==null) {
                return ERROR_END_OF_FILE;
            }
            sb.append(s);
            return 0;
        } catch (IOException t) {
            t.printStackTrace();
            return ERROR_IO;
        }
    } // doInput

    /** Loads a program and runs it.
     * Blocks the caller until the program has terminated.
     * @param command the program to run.
     * @param args command-line args to pass to the program.
     * @return the program's return code on success, ERROR_NO_CLASS,
     * ERROR_NO_MAIN, or ERROR_BAD_COMMAND if the command cannot be run, or
     * ERROR_IN_CHILD if the program throws an uncaught exception.
     */
    private static int doExecAndWait(String command, String args[]) {
        Launcher l;
        try {
            l = new Launcher(command, args);
        } catch (ClassNotFoundException e) {
            return ERROR_NO_CLASS;
        } catch (NoSuchMethodException e) {
            return ERROR_NO_MAIN;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_BAD_COMMAND;
        }
        try {
            l.run();
            l.delete();
            return l.returnCode;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_IN_CHILD;
        }
    } // doExecAndWait

    /** Loads a program and runs it in the background.
     * Does not wait for the program to terminate.
     * @param command the program to run.
     * @param args command-line args to pass to the program.
     * @return a process id on success or ERROR_NO_CLASS, ERROR_NO_MAIN, or
     * ERROR_BAD_COMMAND if the command cannot be run.
     */
    private static int doExec(String command, String args[]) {
        try {
            Launcher l = new Launcher(command, args);
            l.start();
            return l.pid.intValue();
        } catch (ClassNotFoundException e) {
            return ERROR_NO_CLASS;
        } catch (NoSuchMethodException e) {
            return ERROR_NO_MAIN;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_BAD_COMMAND;
        }
    } // doExec

    /** Waits for a program previous started by doExec to terminate.
     * @param pid the process id of the program.
     * @return the return code returned by the program.
     */
    private static int doJoin(int pid) {
        return Launcher.joinOne(pid);
    } // doJoin

    /** Gets time of day, places it into passed in and initialized array
     * @param initialized array with single long element
     * @return the return code returned by the program.
     */
    private static int doGetTime(long[] t) {
        t[0] = System.currentTimeMillis();
        return 0;
    } // doGetTime

    /** Returns the Disk Block Size in bytes
     * @return int block size
     */
    private static int doGetDiskBlockSize() {
        return disk.BLOCK_SIZE;
    } // doGetDiskBlockSize
    
    /** Gets the Disk Block Count,
     *    this is the number of blocks in Disk
     * @return int block count
     */
    private static int doGetDiskBlockCount() {
        return disk.DISK_SIZE;
    } // doGetDiskBlockCount

    /** Reads block into byte[] data
     *   
     * @param blockNumber: the address of the block on Disk
     * @param data: an initialized byte array, where block will be stored
     * @return an <code>int</code> value
     */
    private static int doReadDiskBlock(int blockNumber,byte[] data) {
        return blockNumber;
    } // doReadDiskBlock

    /** Writes byte[] data into Block with address blockNumber on disk
     *   
     * @param blockNumber: the address of the block on Disk
     * @param data: an initialized byte array, that will be written to disk
     * @return an <code>int</code> value
     */
    private static int doWriteDiskBlock(int blockNumber,byte[] data) {
        return blockNumber;
    } // doWriteDiskBlock

    /** A Launcher instance represents one atomic command being run by the
     * Kernel.  It has associated with it a process id (pid), a Java method
     * to run, and a list of arguments to the method.
     * Do not modify any part of this class.
     */
    static private class Launcher extends Thread {
        /** Mapping of process ids (encoded as Integers) to Launcher
         * instances.
         */
        static Map pidMap = new HashMap();

        /** Source of unique ids for Launcher instances. */
        static private int nextpid = 1;

        /** The method being run by this command. */
        private Method method;

        /** The list of arguments to this command. */
        private Object arglist[];

        /** The process id of this command. */
        private Integer pid;

        /** Return code returned by this command (0 if the command has not yet
         * completed.
         */
        private int returnCode = 0;

        /** Creates a new Launcher for a program.
         * @param command the name of the program (new name of a class with
         * a main(String[]) method.
         * @param args command-line arguments to the program.
         */
        public Launcher(String command, String args[])
                throws ClassNotFoundException, NoSuchMethodException
        {
            /* If the user supplied no args, make a dummy. */
            if (args==null) {
                args = new String[0];
            }

            /* Create an array of the method types */
            Class params[] = new Class[] { args.getClass() };

            /* Find the program and look up its main method */
            Class programClass = Class.forName(command);
            method = programClass.getMethod("main",params); 

            /* Assemble an argument list for the method. */
            arglist = new Object[] { args };

            pid = new Integer(nextpid++);
            synchronized (pidMap) {
                pidMap.put(pid, this);
            }
        } // Launcher constructor

        /** Main loop of the Launcher */
        public void run() {
            /* Launch the method using the arglist */
            try {
                method.invoke(null,arglist);
            } catch (InvocationTargetException e) {
                /* Give the user a message */
                System.out.println("Kernel: User error:");
                e.getTargetException().printStackTrace();

                returnCode = ERROR_IN_CHILD;
            } catch (Exception e) {
                System.out.println("Kernel: " + e);
                returnCode = ERROR_IN_CHILD;
            }
        } // Launcher.run

        /** Waits for <em>all</em> existing Launchers to complete. */
        static public void joinAll() {
            for (Iterator e = pidMap.keySet().iterator(); e.hasNext(); ){
                Launcher l = (Launcher)e.next();
                try {
                    l.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    System.out.println("Kernel: join: " + ex);
                }
            }
        } // Launcher.joinAll

        /** Waits for a particular Launcher to complete.
         * @param pid the process id of the desired process.
         * @return the return code of the indicated process, or 
         *      ERROR_NO_SUCH_PROCESS if the pid is invalid.
         */
        static public int joinOne(int pid) {
            Object o;
            synchronized (pidMap) {
                o = pidMap.remove(new Integer(pid));
            }
            if (o == null) {
                return ERROR_NO_SUCH_PROCESS;
            }
            Launcher l = (Launcher)o;
            try {
                l.join();
            } catch (InterruptedException e) {
                System.out.println("Kernel: join: " + e);
            }
            return l.returnCode;
        } // Launcher.joinOne

        /** Removes this Launcher from the set of all active Launchers. */
        public void delete() {
            synchronized (pidMap) {
                pidMap.remove(pid);
            }
        }
    } // class Kernel.Launcher
} // class Kernel
