package app;

import com.sun.istack.internal.NotNull;

/**
 * Created by san on 7/25/14.
 */
public class Sample1 extends VM {

    static final String map1 = "#######################\n" +
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
    public Object run(Cons map, Cons location, Integer direction, Integer lives, Integer score) {
        Cons row0 = head(map);
        CT r0c0 = head(row0);
        int x = first(location);
        int y = second(location);


        if (r0c0 == CT.FRUIT) {

        } else {

        }
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
        new Sample1().run(convertMap(map1), cons(5, cons(5, null)), 1, 3, 0);
    }

}
