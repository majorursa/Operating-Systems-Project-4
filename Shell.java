/* $Id: Shell.java,v 1.11 2004/04/13 16:14:15 solomon Exp solomon $ */

import java.util.*;

/** A simple command-line shell for the MiniKernel.
 *
 * <p>
 * This program displays a prompt and waits for the user to enter
 * <em>command lines</em>.
 * A command line consists of one or more <em>commands</em>, separated by
 * ampersands (&amp;).
 * A command is the name of a Java class that implements
 * <samp>public static void main(String args[])</samp>, followed by
 * zero or more arguments to the command.
 * All the commands on the line are run in parallel.
 * The shell waits for them to finish before issuing another prompt.
 * <p>
 * The Shell terminates if it sees end-of-file (Control-D if input is coming
 * from the keyboard).
 * <p>
 * If this shell is invoked with any arguments, they are joined together with
 * spaces and run as a single command line.  For example,
 * <pre>
 *      java Shell Test foo "&amp; Test bar"
 * </pre>
 * is equivalent to
 * <pre>
 *      java Shell
 *      Shell&gt; Test foo &amp; Test bar
 *      Shell&gt; exit
 * </pre>
 * <p>
 * The Shell also has the following "built-in" commands.  Any arguments
 * are ignored.
 * <dl>
 * <dt><b>exit</b><dd>The Shell terminates immediately.
 * <dt><b>help</b><dd>The Shell prints a short help message.
 * <dt><b>?</b><dd>Equivalent to <b>help</b>.
 * </dl>
 * @see Kernel
 */
public class Shell {
    /** The main program.
     * @param args command-line arguments.  If empty, prompt the user for
     * commands.
     */
    public static void main(String args[]) {
        StringBuffer sb = new StringBuffer();
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(args[i]);
            }
            runCommandLine(sb.toString());
            return;
        }

        for (;;) {
            Library.output("Shell> ");
            int rc = Library.input(sb);

            if (rc == Kernel.ERROR_END_OF_FILE) {
                return;
            }
            if (rc < 0) {
                Library.output("Fatal error trying to read from console\n");
                System.exit(1);
            }

            if (runCommandLine(sb.toString())) {
                break;
            }
        }
    } // main(String[])

    /** Help message, one line per element. */
    private static String[] help = {
        "usage:  Shell [ command [ & command] ... ]",
        "If no commands are specified, the Shell prompts for command lines.",
        "It terminates on end-of-file.",
        "The following commands are built in:",
        "    exit    terminate immediately",
        "    help    print this message",
        "    ?       same as help"
        };

    /** Parses and runs one command line.
     * @param line the command line to run.
     * @return true if the command line included an exit command.
     */
    private static boolean runCommandLine(String line) {
        // Split into commands separated by &
        StringTokenizer st = new StringTokenizer(line, "&");
        int commandCount = st.countTokens();
        int[] pids = new int[commandCount];
        int processes = 0;
        boolean done = false;

        while (st.hasMoreTokens()) {
            String command = st.nextToken().trim();

            // Check for special cases
            if (command.equals("exit")) {
                // Immediately exit.  Don't wait for other commands if any
                // to complete.
                done = true;
                continue;
            }
            if (command.equals("help") || command.equals("?")) {
                for (int i = 0; i < help.length; i++) {
                    Library.output(help[i] + "\n");
                }
                continue;
            }

            // Split each command by spaces
            StringTokenizer cst = new StringTokenizer(command);
            if (!cst.hasMoreTokens()) {
                // empty command
                continue;
            }

            String program = cst.nextToken();
            String[] progArgs = new String[cst.countTokens()];
            for (int i = 0; cst.hasMoreTokens(); ) {
                progArgs[i++] = cst.nextToken();
            }

            int pid = Library.exec(program,progArgs);
            if (pid < 0) {
                Library.output("Shell: Error executing " + program
                    + ":  " + Library.errorMessage[-pid] + "\n");
            } else {
                pids[processes++] = pid;
            }
        }

        // Wait for all the processes to complete
        for (int i = 0; i < processes; i++) {
            if (Library.join(pids[i]) != 0) {
                Library.output("Error waiting for process " + pids[i]
                    + " to complete\n");
            }
        }
        return done;
    } // runCommandLine(StringBuffer)
} // class Shell
