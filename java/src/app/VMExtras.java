package app;

/**
 * Created by san on 7/26/14.
 */
public class VMExtras extends VM {

    public static final int GET_READER = 100;
    public static final int GET_WRITER = 101;

    @Compiled
    public static Function2<Integer, Integer, Function1<Integer, Integer>> array_256() {
        return array_256_impl();
//        return array_256_impl(Integer.class);
    }

    @Compiled
    public static Function2<Integer, Point, Function1<ParsedEdge, ParsedEdge>> edgesArrayForMap(int w, int h) {
        // I'm very generic, mwa-ha-ha!
        Function2<Integer, Integer, Function1<Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>, Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>>> wrapper = array_256_impl();
        fillArrayForMap(wrapper, w);
        return (final Integer op, final Point p) -> {
            Function1<Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>, Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>> outerAccessor = wrapper.apply(GET_READER, p.x);
            Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>> innerAccessor = outerAccessor.apply(null);
            return innerAccessor.apply(op, p.y);
        };
    }

    @Compiled
    private static void fillArrayForMap(Function2<Integer, Integer, Function1<Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>, Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>>> wrapper, int w) {
        if (w > 0) {
            Function1<Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>, Function2<Integer, Integer, Function1<ParsedEdge, ParsedEdge>>> accessor = wrapper.apply(GET_WRITER, w - 1);
            accessor.apply(array_256_impl());
            fillArrayForMap(wrapper, w - 1);
        }
    }

    @Compiled
    @Native(nlocals = 0)
    public static <Stored> Function2<Integer, Integer, Function1<Stored, Stored>> array_256_impl() {
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
    public static <Stored> Function2<Integer, Integer, Function1<Stored, Stored>> array_256_impl(Class<Stored> storedType) {
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
        Sample1.Point a;
        Sample1.Point b;
        ListCons<Sample1.Point> edge;
        ListCons<Tuple<Function1<Integer, Integer>, Sample1.Point>> edgeAccess;
        int count;
        int edgeNumber;
        int opposingEdgeNumber;
        Function2<Integer, Integer, Function1<Integer, Integer>> danger;

        ParsedEdge(Sample1.Point a, Sample1.Point b, ListCons<Sample1.Point> edge, ListCons<Tuple<Function1<Integer, Integer>, Sample1.Point>> edgeAccess, int count, int edgeNumber, int opposingEdgeNumber, Function2<Integer, Integer, Function1<Integer, Integer>> danger) {
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
            return "[Edge: form=" + a + " to=" + b + " count=" + count + " id=" + edgeNumber + "]";
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
