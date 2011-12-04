import java.util.*;

/**
 * Elevator is a scheduling class
 *   attempt 1: Use simple FIFO queue to schedule IO operations
 *   attempt 2: Use Elevator(scan) method to schedule IO requests
 *      emulate hard disk arm, by scheduling IO requests in one direction at a time
 *      ie schedule only increasing block numbers until there are no more increasing,
 *      then switch direction and schedule only in deacreasing order.
 * @author <a href="mailto:bartlantz@gmail.com">Bart Lantz</a>
 * @version 1.0
 */
public class Elevator {
    private static Disk disk;
    // PriorityQueue or ArrayList or LinkedList
    private Queue<Request> rQueue;
    Request current;
    public Elevator(Disk d) {
        disk = d;
        //rQueue = new LinkedList<Request>();
        rQueue = new LinkedList<Request>();
    }

    public int read(int blockNum, byte[] data) {
        Request r = new Request(blockNum, data, true);
        synchronized(this) {
            rQueue.add(r);
        }
        checkCurrent();
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        disk.beginRead(blockNum,data);
        return 0;
    }

    public int write(int blockNum, byte[] data) {
        Request r = new Request(blockNum, data, false);
        synchronized(this) {
            rQueue.add(r);
        }
        checkCurrent();
        while (!r.getFinished()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //disk.beginWrite(blockNum,data);
        return 0;
    }

    /**
     * Starts next request if no current running IO request
     *
     */
    private void checkCurrent() {
        // if no current request, we must start up queue
        if (current == null) {
            //nextRequest();
            endIO();
        }
    }
    
    /**
     * endIO is called when Disk finishes an IO Request
     *
     * @return an <code>int</code> value
     */
    public int endIO() {
        // this is called when the Disk finishes an IO Request
        // notify waiting threads so they can check if their IO request has finished.
        notifyAll();
        // fire off next IO request to Disk
        nextRequest();
        return 0;
    }
    public int nextRequest() {
        //current = (Request) rQueue.remove();
        current = rQueue.remove();
        int blockNum = current.getBlocks();
        byte[] data = current.getData();
        if (current != null) {
            if (current.getReadRequest() == true) {
                disk.beginRead(blockNum, data);
            } else {
                disk.beginWrite(blockNum, data);
            }
            // wait until next interrupt
            // try {
            //     this.wait();
            // } catch (InterruptedException ie) {
            //     ie.printStackTrace();
            // }

            // set this request's flag to finished
            current.setFinished();
        }
        return 0;
    }
    
}


