/* $Id: Boot.java,v 1.12 2003/03/31 18:36:54 solomon Exp $ */

import java.lang.reflect.*;
import java.util.*;

/** A bootstrap program for the MiniKernel.
 * <p>
 * This program creates a Disk, and launches the kernel by calling
 * the POWER_ON interrupt.  When the Kernel returns from the interrupt,
 * we assume it wants to shut down.
 * <p>
 * The program expects four or more command-line arguments:
 * <ul>
 * <li> a numeric parameter to pass to the Kernel's POWER_ON interrupt.
 *      The kernel stores this number in its bufferSize field.
 * <li> the name of a class that implements the disk,
 * <li> the size of the disk, in blocks,
 * <li> the name of shell program, and
 *      any arguments to the shell program.
 * </ul>
 * <p>
 * An example invocation is
 * <pre>
 *    java Boot 10 Disk 100 Shell
 * </pre>
 *
 * @see Kernel
 * @see Disk
 */
public class Boot {
    /** Prints a message.
     * @param msg the message to print.
     */
    private static void pl(Object msg) {
        System.err.println(msg);
    }

    /** Prints a help message and exits. */
    private static void usage() {
        pl("usage: java Boot"
            + " <cacheSize> <diskName> <diskSize> <shell>"
            + " [ <shell parameters> ... ]");
        System.exit(-1);
    } // usage

    /** The main program.
     * @param args the command-line arguments
     */
    public static void main(String args[]) {
        if (args.length < 4) {
            usage();
        }

        int cacheSize = Integer.parseInt(args[0]);
        String diskName = args[1];
        int diskSize = Integer.parseInt(args[2]);
        String shellCommand = args[3];
        for (int i = 4; i < args.length; i++) {
            shellCommand += " " + args[i];
        }

        // Create a Disk drive and start it spinning
        Object disk = null;
        try {
            Class diskClass = Class.forName(diskName);
            Constructor ctor
                = diskClass.getConstructor(new Class[] { Integer.TYPE });
            disk = ctor.newInstance(new Object[] { new Integer(diskSize) });
            if (! (disk instanceof Disk)) {
                pl(diskName + " is not a subclass of Disk");
                usage();
            }
            if (!diskName.equals("FastDisk")) {
                new Thread((Disk) disk, "DISK").start();
            }
        } catch (ClassNotFoundException e) {
            pl(diskName + ": class not found");
            usage();
        } catch (NoSuchMethodException e) {
            pl(diskName + "(int): no such constructor");
            usage();
        } catch (InvocationTargetException e) {
            pl(diskName + ": " + e.getTargetException());
            usage();
        } catch (Exception e) {
            pl(diskName + ": " + e);
            usage();
        }
        pl("Boot: Starting kernel.");

        Kernel.interrupt(Kernel.INTERRUPT_POWER_ON,
                         cacheSize, 0, disk, shellCommand, null);

        System.out.println("Boot: Kernel has stopped.");
        System.exit(0);
    } // main
} // Boot
