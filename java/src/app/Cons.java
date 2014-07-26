package app;

/**
 * Created by san on 7/25/14.
 */
public class Cons {

    Object data;
    Object addr;

    public Cons(Object data, Object addr) {
        this.data = data;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "(CONS "+data+" "+addr+")";
    }
}
