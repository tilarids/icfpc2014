package app;

/**
 * Created by san on 7/25/14.
 */
public class VM {

    public static Cons cons(Object data, Object addr) {
        return new Cons(data, addr);
    }

    public static <T> T head(Cons c) {
        return (T)c.data;
    }

    public static <T> T first(Cons c) {
        return (T)c.data;
    }

    public static<T> T second(Cons c) {
        return (T)c.addr;
    }

    public static Cons tail(Cons c) {
        return (Cons)c.addr;
    }


}
