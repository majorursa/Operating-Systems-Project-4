class DiskTester {
    public static void main(String[] args) {
        int blockSize = Library.getDiskBlockSize();
        byte[] buffer = new byte[blockSize];
        Library.writeDiskBlock(1, buffer);
        Library.readDiskBlock(1, buffer);
    }
}