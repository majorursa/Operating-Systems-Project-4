/**
 * Elevator is a scheduling class
 *   attempt 1: Use simple FIFO queue to schedule IO operations
 *   attempt 2: Use Elevator(scan) method to schedule IO requests
 *      emulate hard disk arm, by scheduling IO requests in one direction at a time
 *      ie schedule only increasing block numbers until there are no more increasing,
 *      then switch direction and schedule only in deacreasing order.
 * @author <a href="mailto:bart@seamus-laptop">Bart Lantz</a>
 * @version 1.0
 */
public class Elevator {
    private Disk disk;
    private 
    public Elevator(Disk d) {
        disk = d;
    }

    public int read() {
    }

    public int write() {
    }

    /**
     * endIO is called when Disk finishes an IO Request
     *
     * @return an <code>int</code> value
     */
    public int endIO() {
        // this is called when the Disk finishes an IO Request
        nextRequest();  
    }
    public int nextRequest() {
        
    }
}

