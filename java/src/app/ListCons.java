package app;

/**
 * Created by san on 7/25/14.
 */
public class ListCons<D extends Object> extends Cons {

    public ListCons(D data, ListCons<D> addr) {
        super(data, addr);
    }

    @Override
    public String toString() {
        return "(LCONS "+data+" "+addr+")";
    }
}
