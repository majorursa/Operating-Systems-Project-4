/* $Id: Count.java,v 1.4 2002/11/23 13:49:42 solomon Exp $ */

/** A very tiny example MiniKernel program.
 * Count down from 10 to 1.
 * @author Douglas Thain 
 * @see Kernel
 */
public class Count {
    /** The main program.
     * @param args ignored.
     */
    public static void main(String args[]) {
        for (int i = 10; i > 0; i--) {
            Library.output("Counting: " + i + "\n");
        }
        Library.output("*** Blast off ***!\n");
    }
}
