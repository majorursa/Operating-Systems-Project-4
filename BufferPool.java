public class BufferPool {
    private static byte[][] pool;
    private static Elevator elev;
    public BufferPool(int cacheSize, int blockSize, Elevator ev) {
        elev = ev;
        pool = new byte[cacheSize][];
        for (int i = 0; i < cacheSize; i++) {
            pool[i] = new byte[blockSize];
        }

    }
    public byte[] read(int blockNum, byte[] buffer) {
        // check if we have that in cache
        if (pool[blockNum] != null) {
            return pool[blockNum];
        } else {
            elev.read(blockNum,pool[blockNum]);
            return pool[blockNum];
        }
    }
    public int write(int blockNum, byte[] buffer) {
        return 0;
    }


    /** Flush the caches to disk
     *
     * @return an <code>int</code> value
     */
    public int flush() {
        return 0;
    }
}