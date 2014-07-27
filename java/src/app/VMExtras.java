package app;

/**
 * Created by san on 7/26/14.
 */
public class VMExtras extends VM {

    public static final int GET_READER = 100;
    public static final int GET_WRITER = 101;

    @Compiled
    @Native(nlocals = 0)
    public Function2<Integer, Integer, Function1<Integer,Integer>> array_256() {
        return create_array_accessor(256);
    }

    public Function2<Integer, Integer, Function1<Integer,Integer>> create_array_accessor(int n) {
        final int[] d = new int[n];
        Function2<Integer,Integer,Function1<Integer,Integer>> f = (final Integer ix, Integer op) ->  {
            Function1<Integer, Integer> getter = null;
            Function1<Integer, Integer> setter = null;
            switch(op) {
                case 100:         // make getter
                    getter = (Integer __)->d[ix];
                    return getter;
                case 101:         // make setter
                    setter = (Integer v)-> { int q = d[ix]; d[ix]=v; return q;};
                    return setter;

            }
            return null;
        };
        return f;
    }



}
