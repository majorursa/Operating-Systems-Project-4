import java.util.Date;
/**
 * Describe class <code>GetTime</code> here.
 *
 * @author <a href="mailto:bart@seamus-laptop">Bart Lantz</a>
 * @version 1.0
 */
public class GetTime {
    /**
     * Describe <code>main</code> method here.
     *
     * @param args a <code>String</code> value
     */
    public static void main(String args[]) {
        long now = Library.getTime();
        if (now < 0) {
            Library.output("Error: " + Library.errorMessage[(int) -now] + "\n");
        } else {
            Library.output("Current time is " + now + " = " + new Date(now) + "\n");
        }
    } // main
} // GetTime