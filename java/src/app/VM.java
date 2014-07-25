package app;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

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

    public @Compiled static Cons map(Cons c, Function1 arg) {
        return cons(arg.apply(head(c)), map(tail(c), arg));
    }

    public @Compiled static Object fold0(Cons c, Object init, Function2 arg) {
        return arg.apply(init, fold(c, arg));
    }

    public @Compiled static Object fold(Cons c, Function2 arg) {
        if (tail(c) == null) return head(c);
        return fold0(tail(c), head(c), arg);
    }


}
