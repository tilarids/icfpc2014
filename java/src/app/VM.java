package app;

/**
 * Created by san on 7/25/14.
 */
public class VM {

    @Compiled
    class Queue<T> {
        ListCons<T> xs;
        ListCons<T> ys;

        Queue(ListCons<T> xs, ListCons<T> ys) {
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

        @Override
        public String toString() {
            return "Tuple{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }
    }


    public static <D> ListCons<D> lcons(D data, ListCons<D> addr) {
        return new ListCons<D>(data, addr);
    }

    public static <D> ListCons<D> cons(D data, ListCons<D> addr) {
        return new ListCons<D>(data, addr);
    }

    public static Cons cons(Object data, Object addr) {
        return new Cons(data, addr);
    }

    public static void debug(Object o) {
        System.out.println("DEBUG: " + o.toString());
    }

    public static void breakpoint() {
        System.out.println("DEBUG: BREAKPOINT");
    }

    @Compiled
    public static <T> T mydebug(T o) {
        debug(o);
        return o;
    }

    @Compiled
    public static <T> T mydebugv(Object tag, T o) {
        debug(tag);
        debug(o);
        return o;
    }

    public static <T> T head(ListCons<T> c) {
        if (c == null) {
            throw new RuntimeException("head: null");
        }
        return (T) c.data;
    }

    public static <T> T head(Cons c) {
        if (c == null) throw new RuntimeException("head: null");
        return (T) c.data;
    }


    public static <T> T first(Cons c) {
        if (c == null) throw new RuntimeException("first: null");
        return (T) c.data;
    }

    public static <T> T second(Cons c) {
        if (c == null) throw new RuntimeException("second: null");
        return (T) c.addr;
    }

    public static <D> ListCons<D> tail(ListCons<D> c) {
        if (c == null) throw new RuntimeException("tail: null");
        return (ListCons<D>) c.addr;
    }

    public static Cons tail(Cons c) {
        if (c == null) throw new RuntimeException("tail: null");
        return (Cons) c.addr;
    }

    /**
     * foldl :: (a -> b -> a) -> a -> [b] -> a
     * foldl f z []     = z
     * foldl f z (x:xs) = foldl f (f z x) xs
     */
    @Compiled
    public static <A, B> A foldl(Function2<A, B, A> f, A a, ListCons<B> l) {
        return l == null ? a : foldl(f, f.apply(a, head(l)), tail(l));
    }

    /**
     * foldr :: (a -> b -> b) -> b -> [a] -> b
     * foldr f z []     = z
     * foldr f z (x:xs) = f x (foldr f z xs)
     */
    @Compiled
    public static <A, B> B foldr(Function2<A, B, B> f, B a, ListCons<A> l) {
        return l == null ? a : f.apply(head(l), foldr(f, a, tail(l)));
    }

    /**
     * function composition
     * o :: (b -> c) -> (a -> b) -> a -> c
     */
    @Compiled
    static public <A, B, C> Function1<A, C> o(final Function1<B, C> f, final Function1<A, B> g) {
        return x -> f.apply(g.apply(x));
    }

    /**
     * Partial application.
     *
     * @param a The <code>A</code> to which to apply this function.
     * @return The function partially applied to the given argument.
     */
    static public <A, B, C> Function1<B, C> f(final Function2<A, B, C> f, final A a) {
        return b -> f.apply(a, b);
    }

    /**
     * Curries this wrapped function to a wrapped function of arity-1 that returns another wrapped function.
     */
    static public <A, B, C> Function1<A, Function1<B, C>> curry(final Function2<A, B, C> f) {
        return a -> b -> f.apply(a, b);
    }

    static public <A, B, C> Function2<A, B, C> uncurry(final Function1<A, Function1<B, C>> f) {
        return (a, b) -> f.apply(a).apply(b);
    }

    @Compiled
    static public <A, B, C> Function2<B, A, C> flip(Function2<A, B, C> f) {
        return (b, a) -> f.apply(a, b);
    }

    /*
    reverse = foldl (flip (:)) []
    */
//    @Compiled
//    public static <A> ListCons<A> reverse_(ListCons<A> l) {
//        return foldl((a, b) -> cons(b, a), null, l);
//    }

    /*
    map :: (a -> b) -> [a] -> [b]
    map f = foldr ((:) . f) []
    */
//    @Compiled
//    public static <A,B> ListCons<B> map_(Function1<A,B> f, ListCons<A> l) {
////        return foldr(uncurry(o(curry((a, b) -> cons(a, b)), f)), null, l);
//        return foldr((a, b) -> cons(f.apply(a), b), null, l);
//    }

    /**
     * basic map
     */
    @Compiled
    public static <T, T2> ListCons<T2> map(ListCons<T> c, Function1<T, T2> arg) {
        return (c == null) ? null :
                cons(arg.apply(head(c)), (ListCons<T2>) map(tail(c), arg));
    }

    /**
     * wrapper that can contain value, or does not contain
     */
    @Compiled
    static class Maybe<T> {
        /**
         * contains?
         */
        int set;
        /**
         * value
         */
        T data;

        Maybe(int set, T data) {
            this.set = set;
            this.data = data;
        }

        @Override
        public String toString() {
            if (set == 0) {
                return "Nothing";
            } else {
                return "[Just " + data + "]";
            }
        }
    }

    /**
     * map() with index (2nd arg for lambda)
     */
    @Compiled
    public static <D, D2> ListCons<D2> mapi(ListCons<D> c, int ix, Function2<D, Integer, D2> arg) {
        return (c == null) ? null :
                cons(arg.apply(head(c), ix), (ListCons<D2>) mapi(tail(c), ix + 1, arg));
    }

    /**
     * concatenates list of maybes into list of values where maybe contains value
     */
    @Compiled
    public static <D> ListCons<D> cat_maybes(ListCons<Maybe<D>> data) {
        ListCons<ListCons<D>> mtl = map(data, (d) -> maybeToList(d));
        ListCons<D> rv = concat(mtl);
        return rv;
    }

    /**
     * concatenates list of lists into one list
     */
    @Compiled
    public static <D> ListCons<D> concat(ListCons<ListCons<D>> data) {
        return reverse(concat_acc(data, null));
    }

    /**
     * concatenates list of lists into one list
     */
    @Compiled
    public static <D> D last(ListCons<D> data) {
        if (data == null) throw new RuntimeException("Last: null list");
        return tail(data) == null ? head(data) : last(tail(data));
    }

    /**
     * concatenates list of lists into one list
     */
    @Compiled
    public static <D> ListCons<D> concat_set(ListCons<ListCons<D>> data) {
        return concat_acc(data, null);
    }

    /**
     * helper for concat
     */
    @Compiled
    public static <D> ListCons<D> concat_acc(ListCons<ListCons<D>> data, ListCons<D> acc) {
        return data == null ? acc : concat_acc(tail(data), concat2(head(data), acc));
    }

    /**
     * concatenates 2 lists (haskell (++))
     */
    @Compiled
    public static <D> ListCons<D> concat2(ListCons<D> data, ListCons<D> data2) {
        return (data == null) ? data2 :
                (data2 == null ? data :
                        concat2_acc(reverse(data), data2));
    }

    /**
     * concatenates 2 lists, unordered
     */
    @Compiled
    public static <D> ListCons<D> concat2_set(ListCons<D> data, ListCons<D> data2) {
        return (data == null) ? data2 :
                (data2 == null ? data :
                        concat2_acc(data, data2));
    }

    /**
     * helper for concat2
     */
    @Compiled
    public static <D> ListCons<D> concat2_acc(ListCons<D> data, ListCons<D> acc) {
        return data == null ? acc : concat2_acc(tail(data), cons(head(data), acc));
    }

    /**
     * constructor for empty maybe
     */
    @Compiled
    public static <T> Maybe<T> NOTHING() {
        return new Maybe<>(0, null);
    }

    /**
     * constructor for full maybe
     */
    @Compiled
    public static <T> Maybe<T> JUST(T t) {
        Maybe<T> tMaybe = new Maybe<>(1, t);
        return tMaybe;
    }

    @Compiled
    public static <D> ListCons<D> maybeToList(Maybe<D> d) {
        return is_nothing(d) == 1 ? null : cons(from_maybe(d), null);
    }

    /**
     * helper for catMaybes
     */
    @Compiled
    public static <D> ListCons<D> catMaybes_acc(ListCons<Maybe<D>> data, ListCons<D> acc) {
        ListCons<D> rv = null;
        int nothing;
        D data1;
        ListCons<D> lastCons;
        if (data == null) {
            rv = acc;
        } else {
            nothing = is_nothing(head(data));
            data1 = from_maybe(head(data));
            lastCons = cons(data1, acc);
            rv = catMaybes_acc(tail(data), (nothing == 1) ?
                    acc
                    :
                    lastCons);
        }
        return rv;
    }

    /**
     * test is maybe is empty
     */
    @Compiled
    private static <D> int is_nothing(Maybe<D> head) {
        return 1 - head.set;
    }

    /**
     * extract value form maybe
     */
    @Compiled
    private static <D> D from_maybe(Maybe<D> head) {
        if (head.set == 0) throw new IllegalArgumentException("Maybe: nothing");
//        debug(710000);
//        debug(head);
        //        debug(rv);
        return head.data;
    }

    /**
     * reverse list
     */
    @Compiled
    public static <D> ListCons<D> reverse(ListCons<D> c) {
        return reverse_acc(c, null);
    }

    @Compiled
    public static <D> D maximum_by_acc(ListCons<D> d, Function1<D, Integer> projection, D acc) {
        return d == null ? acc :
                projection.apply(acc) > projection.apply(head(d)) ? maximum_by_acc(tail(d), projection, acc)
                        : maximum_by_acc(tail(d), projection, head(d));
    }

    @Compiled
    public static <D> D maximum_by(ListCons<D> d, Function1<D, Integer> projection) {
        if (d == null) throw new RuntimeException("maximum_by: null list");
        return maximum_by_acc(tail(d), projection, head(d));
    }

    /**
     * reverse list
     */
    @Compiled
    public static <D> ListCons<D> dropWhile(ListCons<D> c, Function1<D, Integer> test) {
        return (c == null) ? null :
                test.apply(head(c)) == 1 ? dropWhile(tail(c), test) : c;
    }

    /**
     * helper for reverse
     */
    @Compiled
    public static <D> ListCons<D> reverse_acc(ListCons<D> c, ListCons<D> acc) {
        return (c == null) ? acc :
                reverse_acc(tail(c), cons(head(c), acc));
    }

    /**
     * fold with constant
     */
    @Compiled
    public static <I, R> R fold0(ListCons<I> c, R init, Function2<R, I, R> arg) {
        return (c == null) ? init :
                fold0(tail(c), arg.apply(init, head(c)), arg);
    }

    @Compiled
    public <T> Queue<T> queue_new() {
        return new Queue<T>(null, null);
    }

    @Compiled
    public <T> Queue<T> queue_enqueue(Queue<T> q, T v) {
        return new Queue<T>(q.xs, cons(v, q.ys));
    }

    /**
     * check if queue is empty
     */
    @Compiled
    public <T> boolean queue_isempty(Queue<T> q) {
        return q.xs == null && q.ys == null;
    }

    /**
     * return n-th item in the list
     */
    @Compiled
    public static Object list_item(Cons list, int n) {
        if (n < 0) throw new RuntimeException("list_item(list, -1)");
        return (n == 0) ? head(list) : list_item(tail(list), n - 1);
    }

    /**
     * return n-th item in the list
     */
    @Compiled
    public static <T> T list_item(ListCons<T> list, int n) {
        if (n < 0) throw new RuntimeException("list_item(list, -1)");
        return (n == 0) ? head(list) : list_item(tail(list), n - 1);
    }

    /**
     * return n-th item in the list, with default if it is beyond the list
     */
    @Compiled
    public static Object list_item_def(Cons list, int index, Object deflt) {
        return (index < 0) ? deflt :
                index == 0 ? head(list) : list_item(tail(list), index - 1);
    }

    /**
     * remove from queue, return removed item and new queue
     */
    @Compiled
    public <T> Tuple<T, Queue<T>> queue_dequeue(Queue<T> q) {
        Tuple<T, Queue<T>> retval = null;
        if (q.xs == null) {
            if (q.ys != null) {
                retval = queue_dequeue(new Queue<T>(reverse(q.ys), null));
            } else {
                throw new IllegalArgumentException("error dequeue");
            }
        } else {
            retval = new Tuple<>(head(q.xs), new Queue<T>(tail(q.xs), q.ys));
        }
        return retval;
    }

    /**
     * length of list
     */
    @Compiled
    public static <T> int length(ListCons<T> list) {
        return elements_counter(list, 0);
    }

    /**
     * filter list by predicate
     */
    @Compiled
    public static <T> ListCons<T> filter(ListCons<T> list, Function1<T, Integer> pred) {
        return reverse(filter_acc(list, pred, null));
    }

    /**
     * has any satisfying item?
     */
    @Compiled
    public static <T> int any(ListCons<T> list, Function1<T, Integer> pred) {
        return list == null ? 0 :
                pred.apply(head(list)) == 1 ? 1 :
                        any(tail(list), pred);

    }

    /**
     * free of any satisfying items?
     */
    @Compiled
    public static <T> int noneof(ListCons<T> list, Function1<T, Integer> pred) {
        return list == null ? 1 :
                pred.apply(head(list)) == 1 ? 0 :
                        noneof(tail(list), pred);

    }

    /**
     * list is empty?
     */
    @Compiled
    public int empty(Cons d) {
        return d != null ? 0 : 1;
    }

    /**
     * list is not empty?
     */
    @Compiled
    public int notempty(Cons d) {
        return d != null ? 1 : 0;
    }

    /**
     * used by filter
     */
    @Compiled
    public static <T> ListCons<T> filter_acc(ListCons<T> list, Function1<T, Integer> pred, ListCons<T> acc) {
        return list == null ? acc : filter_acc(tail(list), pred, pred.apply(head(list)) == 1 ? cons(head(list), acc) : acc);
    }

    /*Map operations*/

    @Compiled
    public static int map_height(Cons map) {
        return elements_counter(map, 0);
    }

    @Compiled
    public static int map_width(Cons map) {
        return elements_counter(head(map), 0);
    }

    /**
     * used by length
     */
    @Compiled
    public static int elements_counter(Cons list, int counter) {
        return list == null ? counter :
                elements_counter(tail(list), counter + 1);
    }


    @Compiled
    public static <A, B, C> ListCons<C> zip_with(Function2<A, B, C> f, ListCons<A> x, ListCons<B> y) {
        return
                x == null ? null
                        : y == null ? null
                        : lcons(f.apply(head(x), head(y)), zip_with(f, tail(x), tail(y)));
    }

    @Compiled
    public static <A, B> ListCons<Tuple<A, B>> zip(final ListCons<A> x, final ListCons<B> y) {
        return
                x == null ? null
                        : y == null ? null
                        : lcons(new Tuple<>(head(x), head(y)), zip(tail(x), tail(y)));
    }

    // produce_n f a n = (first (f a)) : iterate f (second (f a)) (n - 1)
    // produce_n f a 0 = (first (f a))
    @Compiled
    public <T> ListCons<T> produce_n(Function1<T, Cons> f, T a, int n) {
        T elem = first(f.apply(a));
        return n == 0 ? cons(elem, null) : cons(elem, produce_n(f, second(f.apply(a)), n - 1));
    }


}
