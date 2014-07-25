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
        return (T)c.data;
    }

    public static <T> T first(Cons c) {
        return (T)c.data;
    }

    public static<T> T second(Cons c) {
        return (T)c.addr;
    }

    public static Cons tail(Cons c) {
        if (c == null) throw new RuntimeException("tail of NULL");
        return (Cons)c.addr;
    }

    /*
    foldl :: (a -> b -> a) -> a -> [b] -> a
    foldl f z []     = z
    foldl f z (x:xs) = foldl f (f z x) xs
    */
    @Compiled
    public static Object foldl(Function2 f, Object a, Cons l) {
        if (l == null) return a;
        return foldl(f, f.apply(a, head(l)), tail(l));
    }

    /*
    foldr :: (a -> b -> b) -> b -> [a] -> b
    foldr f z []     = z
    foldr f z (x:xs) = f x (foldr f z xs)    
    */
    @Compiled
    public static Object foldr(Function2 f, Object a, Cons l) {
        if (l == null) return a;
        return f.apply(head(l), foldr(f, a, tail(l)));
    }

    /*
    map f = foldr ((:) . f) []
    */
    @Compiled
    public static Cons map(Function1 f, Cons l) {
        return foldr(compose(fCons, f), null, l)
    }

    @Compiled
    public static Function1 compose(Function1 f, Function1 g) {
	return new Function1(x) {
	    Object apply(Object a, Object b) { return f.apply(g.apply(x)); }
	}
    }

    /*
    lifted cons
    a -> [a] -> [a]
    */
    @Compiled
    public static Function2 fCons = new Function2() {
	Object apply(Object a, Object b) { return cons(a, b); }
    }
    
    /*
    reverse = foldl (flip (:)) []
    */
    @Compiled
    public static Cons reverse(Cons l) {
	// return foldl(flip(fCons), null, l);
	return foldl(flip(fCons), null, l);
    }

    /*
    (a -> b -> c) -> b -> a -> c
    */
    @Compiled
    public static Function2 flip(Function2 f) {
	return new Function2() {
	    Object apply(Object a, Object b) { return f.apply(b, a); }
	}
    }

    @Compiled
    public static Object fold0(Cons c, Object init, Function2 arg) {
        return arg.apply(init, fold(c, arg));
    }
  
    @Compiled
    public static Object fold(Cons c, Function2 arg) {
        if (tail(c) == null) return head(c);
        return fold0(tail(c), head(c), arg);
    }

    @Compiled
    public Queue queue_new() {
        return new Queue(null, null);
    }

    @Compiled
    public Queue queue_enqueue(Queue q, Object v) {
        return new Queue(q.xs, cons(v, q.ys));
    }

    @Compiled
    public boolean queue_isempty(Queue q) {
        return q.xs == null && q.ys == null;
    }

    @Compiled
    public Object list_item(Cons list, int index) {
        if (index < 0) throw new RuntimeException("list_item(list, -1)");
        if (index == 0) return head(list); else return list_item(tail(list), index-1);
    }

    @Compiled
    public Tuple<Object, Queue> queue_dequeue(Queue q) {
        if (q.xs == null) {
            if (q.ys != null) {
                return queue_dequeue(new Queue(reverse(q.ys), null));
            } else {
                throw new IllegalArgumentException("error dequeue");
            }
        }
        return new Tuple<>(head(q.xs), new Queue(tail(q.xs), q.ys));
    }

}
