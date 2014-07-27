package app;

import static app.VM.*;

/**
 * Created by lenovo on 27.07.2014.
 */ // Int -> T map
@Compiled
public class SortedMap<T> {
    SortedMapNode<T> node;
    int dummy;

    SortedMap(SortedMapNode<T> node, int dummy) {
        this.node = node;
        this.dummy = dummy;
    }


    @Compiled
    public static class SortedMapNode<T> {
        int count;
        int key;
        T val;
        int lev;
        SortedMapNode<T> lo;
        SortedMapNode<T> hi;

        SortedMapNode(int count, int key, T val, int lev, SortedMapNode<T> lo, SortedMapNode<T> hi) {
            this.count = count;
            this.key = key;
            this.val = val;
            this.lev = lev;
            this.lo = lo;
            this.hi = hi;
        }
    }


    @Compiled
    public static <T> SortedMapNode<T> sorted_node_new(int key, T val, int lev, SortedMapNode<T> lo, SortedMapNode<T> hi) {
        return new SortedMapNode<T>(1 + (lo != null ? lo.count : 0) + (hi != null ? hi.count : 0),
                key,
                val,
                lev,
                lo,
                hi);
    }


    @Compiled
    public static <T> SortedMapNode<T> sorted_node_with_lev(SortedMapNode<T> node, int lev) {
        return sorted_node_new(node.key, node.val, lev, node.lo, node.hi);
    }

    @Compiled
    public static <T> SortedMapNode<T> sorted_node_with_lo_hi(SortedMapNode<T> node, SortedMapNode<T> lo, SortedMapNode<T> hi) {
        return sorted_node_new(node.key, node.val, node.lev, lo, hi);
    }

    @Compiled
    public static <T> SortedMapNode<T> sorted_node_with_lo(SortedMapNode<T> node, SortedMapNode<T> lo) {
        return sorted_node_with_lo_hi(node, lo, node.hi);
    }

    @Compiled
    public static <T> SortedMapNode<T> sorted_node_with_hi(SortedMapNode<T> node, SortedMapNode<T> hi) {
        return sorted_node_with_lo_hi(node, node.lo, hi);
    }

    // go_lo = key < node.key


    @Compiled
    public static <T> int sorted_node_has(SortedMapNode<T> node, int key) {
        return node == null ? 0 : (key == node.key ? 1 : sorted_node_has(key < node.key ? node.lo : node.hi, key));
    }

    @Compiled
    public static <T> T sorted_node_get(SortedMapNode<T> node, int key, T def) {
        return node == null ? def : (key == node.key ? node.val : sorted_node_get(key < node.key ? node.lo : node.hi, key, def));
    }

    @Compiled
    public static <T> SortedMapNode<T> sorted_node_put(SortedMapNode<T> node, int key, T val) {
        return node ==
                null ? sorted_node_new(key, val, 0, null, null)
                : (key == node.key ? (val == node.val ? node : sorted_node_new(key, val, node.lev, node.lo, node.hi))
                : sorted_node_split(key < node.key ? sorted_node_skew(node, sorted_node_put(node.lo, key, val))
                : sorted_node_skew(sorted_node_with_hi(node, sorted_node_put(node.hi, key, val)), null)));
    }


    @Compiled
    public static <T> SortedMapNode<T> sorted_node_skew(SortedMapNode<T> node, SortedMapNode<T> lo) {
        lo = lo != null ? lo : node.lo;
        SortedMapNode<T> true_node = sorted_node_with_lo(node, lo);
        return lo == null ? true_node : (node.lev > lo.lev ? true_node : sorted_node_with_hi(lo, sorted_node_with_lo(node, lo.hi)));
    }

    @Compiled
    public static <T> SortedMapNode<T> sorted_node_split(SortedMapNode<T> node) {
        SortedMapNode<T> hi = node.hi;
        return hi == null ? node : (hi.hi == null ? node : (node.lev > hi.hi.lev ? node : sorted_node_new(hi.key, hi.val, hi.lev + 1, sorted_node_with_hi(node, hi.lo), hi.hi)));
    }

    public static <T, X> X sorted_node_walk(SortedMapNode<T> node, X acc, Function2<X, Tuple<Integer, T>, X> fun) {
        return
                node == null ? acc
                        : node.hi != null ? sorted_node_walk(node.hi, fun.apply(sorted_node_walk(node.lo, acc, fun), new Tuple<>(node.key, node.val)), fun)
                        : fun.apply(sorted_node_walk(node.lo, acc, fun), new Tuple<>(node.key, node.val));
    }

    @Compiled
    public static <T> int sorted_map_count(SortedMap<T> m) {
        return m.node != null ? m.node.count : 0;
    }

    @Compiled
    public static <T> int sorted_map_contains(SortedMap<T> m, int key) {
        return m.node != null ? sorted_node_has(m.node, key) : 0;
    }

    @Compiled
    public static <T> T sorted_map_get(SortedMap<T> m, int key, T def) {
        return m.node != null ? sorted_node_get(m.node, key, def) : def;
    }

    @Compiled
    public static <T> SortedMap<T> sorted_map_assoc(SortedMap<T> m, int key, T val) {
        return new SortedMap<T>(sorted_node_put(m.node, key, val), 0);
    }

    @Compiled
    public static <T> SortedMap<T> sorted_map_assoc_all(SortedMap<T> m, ListCons<Tuple<Integer, T>> l) {
        return fold0(l,
                m,
                (SortedMap<T> acc, Tuple<Integer, T> elem) -> sorted_map_assoc(acc, elem.a, elem.b));
    }

    public static <T, X> X sorted_map_walk(SortedMap<T> map, X acc, Function2<X, Tuple<Integer, T>, X> fun) {
        return map.node == null ? acc : sorted_node_walk(map.node, acc, fun);
    }


}
