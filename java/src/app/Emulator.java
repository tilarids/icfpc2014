package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkopiychenko on 7/25/14.
 */
public class Emulator {
    private static final int LAMBDA_MAN_TICKS_PER_MOVE = 127;
    private static final int GHOST_TICKS_PER_MOVE = 137;

    public static List<LambdaMap> emulate(LambdaMap initialMap, ManAI LambdaManAI, List<GhostAI> gBots, int ticksLimit){
        List<LambdaMap> result = new ArrayList<>();
        result.add(initialMap);

        LambdaMap currentMap = initialMap;

        //Simplified logic

        for(int i = 1; i < ticksLimit; i++){
            if(i % LAMBDA_MAN_TICKS_PER_MOVE == 0){
                int step = LambdaManAI.run(currentMap.toCons());
                currentMap = manStep(currentMap, step);
                result.add(currentMap);
            }
            if(i % GHOST_TICKS_PER_MOVE == 0){
                int step = gBots.get(0).run(currentMap.toCons());
                currentMap = ghostStep(currentMap, step);
                result.add(currentMap);
            }
        }

        return result;
    }

    private static LambdaMap ghostStep(LambdaMap map, int step) {
        return map;
    }


    private static LambdaMap manStep(LambdaMap map, int step) {
        return map;
    }
}
