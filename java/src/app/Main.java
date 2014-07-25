package app;

import java.util.ArrayList;
import java.util.List;

public class Main {

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


    public static void main(String[] args) {

        LambdaMap map = new LambdaMap(map1);

        int height = map.height();
        int width = map.width();
        int ticksLimit = 127*16*height*width;

        List<GhostAI> gBots = new ArrayList<>();
        gBots.add(new GhostAI());

        List<LambdaMap> l = Emulator.emulate(map, new ManAI(), gBots, 10);

    }
}
