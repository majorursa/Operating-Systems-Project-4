/**
 * Represents an IO request.
 *
 * @author <a href="mailto:bart@seamus-laptop">Bart Lantz</a>
 * @version 1.0
 */
public class Request {
    private boolean readRequest;
    private int blockNumber;
    private byte[] data;
    private boolean finished;
    public Request(int blockNumber, byte data[], boolean readRequest) {
        this.blockNumber = blockNumber;
        this.data        = data;
        this.readRequest = readRequest;
        this.finished = false;
    }
    public int getBlocks() {
        return blockNumber;
    }
    
    public byte[] getData() {
        return data;
    }
    
    /** Returns if request is a read request
     *
     * @return a <code>boolean</code> value
     */
    public boolean getReadRequest() {
        return readRequest;
    }
    
    /** Sets finished flag so process can return
     */
    public void setFinished() {
        finished = true;
    }
    
    /** Returns whether or not IO call is finished.
     * @return finished flag
     */
    public boolean getFinished() {
        return finished;
    }

}
