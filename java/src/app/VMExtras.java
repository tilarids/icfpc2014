package app;

/**
 * Created by san on 7/26/14.
 */
public class VMExtras extends VM {

    public Function2<Integer, Integer, Object> create_array_accessor(int n) {
        final int[] d = new int[n];
        Function2<Integer,Integer,Object> f = (final Integer ix, Integer op) ->  {
            Function0<Integer> getter = null;
            Function1<Integer, Void> setter = null;
            switch(op) {
                case 0:         // make getter
                    getter = ()->d[ix];
                    return getter;
                case 1:         // make setter
                    setter = (Integer v)-> { d[ix]=v; return null;};
                    return setter;

            }
            return null;
        };
        return f;
    }



}
