package app;

/**
 * Created by san on 7/25/14.
 */
public class Cons<D extends Object, A extends Object> {

    D data;
    A addr;

    public Cons(D data, A addr) {
        this.data = data;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "(CONS "+data+" "+addr+")";
    }
}
