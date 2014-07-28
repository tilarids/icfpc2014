package app;

/**
 * Created by san on 7/26/14.
 */
public class VMExtras extends VM {

    public static final int GET_READER = 100;
    public static final int GET_WRITER = 101;

    @Compiled
    @Native(nlocals = 0)
    public Function2<Integer, Integer, Function1<Integer, Integer>> array_1() {
        return create_array_accessor(1);
    }


    @Compiled
    public static <Key, Value> Function1<Key, Value> toReadOnlyAccessor(final Function2<Integer, Key, Function1<Value, Value>> readWriteAccessor) {
        return (Key k) -> readWriteAccessor.apply(VMExtras.GET_READER, k).apply(null);
    }

    @Compiled
    public static Function2<Integer, Point, Function1<ListCons<ParsedEdge>, ListCons<ParsedEdge>>> emptyEdgesArrayForMap(int h) {
        // I'm very generic, mwa-ha-ha!
        Function2<Integer, Integer, Function1<Function2<Integer, Integer, Function1<ListCons<ParsedEdge>, ListCons<ParsedEdge>>>, Function2<Integer, Integer, Function1<ListCons<ParsedEdge>, ListCons<ParsedEdge>>>>> wrapper = array_256();
        int ignore = fillArrayForMap(wrapper, h);
        return (final Integer op, final Point p) -> wrapper.apply(VMExtras.GET_READER, p.y).apply(null).apply(op, p.x);
    }

    @Compiled
    private static int fillArrayForMap(Function2<Integer, Integer, Function1<Function2<Integer, Integer, Function1<ListCons<ParsedEdge>, ListCons<ParsedEdge>>>, Function2<Integer, Integer, Function1<ListCons<ParsedEdge>, ListCons<ParsedEdge>>>>> wrapper, int h) {
        int res = 0;
        Function2<Integer, Integer, Function1<ListCons<ParsedEdge>, ListCons<ParsedEdge>>> __;
        if (h > 0) {
            __ = wrapper.apply(VMExtras.GET_WRITER, h - 1).apply(array_256());
            res = fillArrayForMap(wrapper, h - 1) + 1;
        }
        return res;
    }

    @Compiled
    @Native(nlocals = 0)
    public static <Stored> Function2<Integer, Integer, Function1<Stored, Stored>> array_256() {
        return create_array_accessor(256);
    }

    public static <Stored> Function2<Integer, Integer, Function1<Stored, Stored>> create_array_accessor(int n) {
        final Object[] d = new Object[n];
        Function2<Integer, Integer, Function1<Stored, Stored>> f = (final Integer op, final Integer ix) -> {
            Function1<Stored, Stored> getter = null;
            Function1<Stored, Stored> setter = null;
            switch (op) {
                case GET_READER:         // make getter
                    getter = (Stored __) -> (Stored) d[ix];
                    return getter;
                case GET_WRITER:         // make setter
                    setter = (Stored v) -> {
                        Stored q = (Stored) d[ix];
                        d[ix] = v;
                        return q;
                    };
                    return setter;

            }
            return null;
        };
        return f;
    }

    /*
    @Compiled
    @Native(nlocals = 0)
    public static <Stored> Function2<Integer, Integer, Function1<Stored, Stored>> array_256(Class<Stored> storedType) {
        return create_array_accessor(storedType, 256);
    }

    public static <Stored> Function2<Integer, Integer, Function1<Stored, Stored>> create_array_accessor(Class<Stored> storedType, int n) {
        final Stored[] d = (Stored[]) Array.newInstance(storedType, n);
        Function2<Integer, Integer, Function1<Stored, Stored>> f = (final Integer op, final Integer ix) -> {
            Function1<Stored, Stored> getter = null;
            Function1<Stored, Stored> setter = null;
            switch (op) {
                case GET_READER:         // make getter
                    getter = (Stored __) -> d[ix];
                    return getter;
                case GET_WRITER:         // make setter
                    setter = (Stored v) -> {
                        Stored q = d[ix];
                        d[ix] = v;
                        return q;
                    };
                    return setter;

            }
            return null;
        };
        return f;
    }
//*/

    @Compiled
    @Native(nlocals = 0)
    public static Cons sample_map() {
        return Sample1.convertMap(Sample1.map1, null);
    }

    @Compiled
    static class ParsedEdge {
        Point a;
        Point b;
        ListCons<Point> edge;
        ListCons<Tuple<Function1<Integer, Integer>, Point>> edgeAccess;
        int count;
        int edgeNumber;
        int opposingEdgeNumber;
        Function2<Integer, Integer, Function1<Integer, Integer>> danger;

        ParsedEdge(Point a, Point b, ListCons<Point> edge, ListCons<Tuple<Function1<Integer, Integer>, Point>> edgeAccess, int count, int edgeNumber, int opposingEdgeNumber, Function2<Integer, Integer, Function1<Integer, Integer>> danger) {
            this.a = a;
            this.b = b;
            this.edge = edge;
            this.edgeAccess = edgeAccess;
            this.count = count;
            this.edgeNumber = edgeNumber;
            this.opposingEdgeNumber = opposingEdgeNumber;
            this.danger = danger;
        }

        @Override
        public String toString() {
            return "[Edge: form=" + a + " to=" + b + " count=" + count + " id=" + edgeNumber + " danger="+danger.apply(VMExtras.GET_READER, 0).apply(0)+"]";
        }
    }

    @Compiled
    public static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point(" + x + "," + y + ")";
        }
    }
}
