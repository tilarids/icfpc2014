package app;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Created by san on 7/25/14.
 */
public class VM {

    @Compiled
    class Queue {
        Cons xs;
        Cons ys;

        Queue(Cons xs, Cons ys) {
            this.xs = xs;
            this.ys = ys;
        }
    }

    @Compiled
    static class Tuple<A, B> {
        A a;
        B b;

        Tuple(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }

    public static Cons cons(Object data, Object addr) {
        return new Cons(data, addr);
    }

    public static <T> T head(Cons c) {
        if (c == null) throw new RuntimeException("head: null");
        return (T)c.data;
    }

    public static <T> T first(Cons c) {
        if (c == null) throw new RuntimeException("first: null");
        return (T)c.data;
    }

    public static<T> T second(Cons c) {
        if (c == null) throw new RuntimeException("second: null");
        return (T)c.addr;
    }

    public static Cons tail(Cons c) {
        if (c == null) throw new RuntimeException("tail: null");
        return (Cons)c.addr;
    }

    @Compiled
    public static Cons map(Cons c, Function1 arg) {
        return cons(arg.apply(head(c)), map(tail(c), arg));
    }

    @Compiled
    public static Cons reverse(Cons c) {
        return (c == null) ? null :
            (tail(c) == null) ?  c :
                cons(reverse(tail(c)), head(c));
    }

    @Compiled
    public static Object fold0(Cons c, Object init, Function2 arg) {
        return arg.apply(init, fold(c, arg));
    }

    @Compiled
    public static Object fold(Cons c, Function2 arg) {
        return tail(c) == null ? head(c) : fold0(tail(c), head(c), arg);
    }

    @Compiled
    public Queue queue_new() {
        return new Queue(null, null);
    }

    @Compiled
    public Queue queue_enqueue(Queue q, Object v) {
        int x = 2;
        int y = 3;
        int z = x + y;
        System.out.println("z="+z);
        return new Queue(q.xs, cons(v, q.ys));
    }

    @Compiled
    public boolean queue_isempty(Queue q) {
        return q.xs == null && q.ys == null;
    }

    @Compiled
    public Object list_item(Cons list, int index) {
        if (index < 0) throw new RuntimeException("list_item(list, -1)");
        return index == 0 ? head(list) : list_item(tail(list), index-1);
    }

    @Compiled
    public Tuple<Object, Queue> queue_dequeue(Queue q) {
        Tuple<Object, Queue> retval = null;
        if (q.xs == null) {
            if (q.ys != null) {
                retval = queue_dequeue(new Queue(reverse(q.ys), null));
            } else {
                throw new IllegalArgumentException("error dequeue");
            }
        }
        if (retval != null) {
            retval = new Tuple<>(head(q.xs), new Queue(tail(q.xs), q.ys));
        }
        return retval;
    }

    /*Map operations*/

    public static int map_height(Cons map){
        return elements_counter(map, 0);
    }

    public static int map_width(Cons map){
        return elements_counter(head(map), 0);
    }

    public static int elements_counter(Cons list, int counter){
        if(list == null) return counter;
        return elements_counter(tail(list), counter + 1);
    }


    /*End of map operations*/

}
