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
    public Request(int blockNumber, byte data[]) {
        this.blockNumber = blockNumber;
        
    }

}