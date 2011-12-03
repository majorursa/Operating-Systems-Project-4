/**
 * Tests system calls getDiskBlockSize() and getDiskBlockCount()
 *
 * @author <a href="mailto:bart@seamus-laptop">Bart Lantz</a>
 * @version 1.0
 */
public class GetDiskSize {
    public static void main (String args[]) {
        int size = Library.getDiskBlockSize();
        if (size < 0) {
            Library.output("Error: " + Library.errorMessage[(int) -size] + "\n");
        } else {
            Library.output("Block Size is " + size + " bytes.\n");
        }

        int count = Library.getDiskBlockCount();
        if (count < 0) {
            Library.output("Error: " + Library.errorMessage[(int) -count] + "\n");
        } else {
            Library.output("Disk Size is " + count + " blocks.\n");
        }
    } // main
} // GetDiskSize
