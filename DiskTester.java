/**
 * Tests implementation
 *
 * @author <a href="mailto:bart@seamus-laptop">Bart Lantz</a>
 * @version 1.0
 */
class DiskTester {
    public static void main(String[] args) {
        int blockSize = Library.getDiskBlockSize();
        boolean test = false;
        byte[] buffer = new byte[blockSize];
        byte[] out = new byte[blockSize];
        int curBlock = 0;

        Library.output("Block Size: " + blockSize + "\n");
        for (int i = 0; i < 100; i++) {
            curBlock = i;
            setPattern(curBlock, buffer);
            Library.writeDiskBlock(curBlock, buffer);
            //Library.output("write: "+ curBlock + "\n");
            Library.readDiskBlock(curBlock, out);
            //Library.output("read: "+ curBlock + "\n");
            test = checkPattern(curBlock, out);
            Library.output("Test " + curBlock + ": " + test + "\n");
        } 
    }

    private static void setPattern(int blockNumber, byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) (blockNumber+i);
        }
    }
    private static boolean checkPattern(int blockNumber, byte[] out) {
        for (int i = 0; i < out.length; i++) {
            if (out[i] != (byte) (blockNumber+i-1)) {
                //System.out.format("%02X ", (byte)(blockNumber + i));
                //System.out.format("%02X \n", out[i]);
                return false;
            }
        }
        return true;
    }
}