package app;

import com.sun.istack.internal.NotNull;

/**
 * Created by san on 7/25/14.
 */
public class Sample1 extends VM {

    static final String map1 =
            "#######################\n" +
            "#..........#..........#\n" +
            "#.###.####.#.####.###.#\n" +
            "#o###.####.#.####.###o#\n" +
            "#.....................#\n" +
            "#.###.#.#######.#.###.#\n" +
            "#.....#....#....#.....#\n" +
            "#####.#### # ####.#####\n" +
            "#   #.#    =    #.#   #\n" +
            "#####.# ### ### #.#####\n" +
            "#    .  # === #  .    #\n" +
            "#####.# ####### #.#####\n" +
            "#   #.#    %    #.#   #\n" +
            "#####.# ####### #.#####\n" +
            "#..........#..........#\n" +
            "#.###.####.#.####.###.#\n" +
            "#o..#......\\......#..o#\n" +
            "###.#.#.#######.#.#.###\n" +
            "#.....#....#....#.....#\n" +
            "#.########.#.########.#\n" +
            "#.....................#\n" +
            "#######################\n";

    public Sample1() {
    }

    @Compiled
    static class SearchItem {

    }

    @Compiled
    static class Point {
        int x;
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Compiled
    public Object run(Cons map, Tuple<Integer,Integer> location, Integer direction, Integer lives, Integer score) {
        int x = location.a;
        int y = location.b;

        CT left = getMapItem(map, y, x-1);
        CT right = getMapItem(map, y, x+1);
        CT top = getMapItem(map, y-1, x);
        CT bottom = getMapItem(map, y+1, x);

        return null;
    }


    public static Cons convertMap(String map) {
        String[] rows = map.split("\n");
        Cons result = null;
        for (int i1 = rows.length - 1; i1 >= 0; i1--) {
            String row = rows[i1];
            Cons lst = null;
            for (int i = row.length() - 1; i >= 0; i--) {
                lst = cons(convertMapCharacter(row.charAt(i)), lst);
            }
            result = cons(lst, result);
        }
        return result;
    }

    @Compiled
    public CT getMapItem(Cons map, int y, int x) {
        return (CT) list_item((Cons)list_item(map, y), x);
    }


    public enum CT {
        SPACE, WALL, PILL, POWER, FRUIT, LAMBDA, GHOST;
    }

    private static CT convertMapCharacter(char c) {
        switch(c) {
            case ' ': return CT.SPACE;
            case '#': return CT.WALL;
            case '.': return CT.PILL;
            case 'o': return CT.POWER;
            case '%': return CT.FRUIT;
            case '\\': return CT.LAMBDA;
            case '=': return CT.GHOST;
            default:
                throw new IllegalArgumentException("Oh");
        }
    }

    public static void main(String[] args) {
        String theMap = map1;

        int x = -1;
        int y = -1;
        String[] rows = theMap.split("\n");
        for (int yy = rows.length - 1; yy >= 0; yy--) {
            String row = rows[yy];
            for (int ii = row.length() - 1; ii >= 0; ii--) {
                if (row.charAt(ii) == '\\') {
                    x = ii;
                    y = yy;
                }
            }
        }


        new Sample1().run(convertMap(theMap), new Tuple<Integer,Integer>(x, y), 1, 3, 0);
    }

}
