package app;

/**
 * Created by dkopiychenko on 7/25/14.
 */
public class CT {

    public static final int SPACE = 1;
    public static final int WALL = 0;
    public static final int PILL = 2;
    public static final int POWER = 3;
    public static final int FRUIT = 4;
    public static final int LAMBDA = 5;
    public static final int GHOST = 6;

    public static int convertMapCharacter(char c) {
        switch (c) {
            case ' ':
                return CT.SPACE;
            case '#':
                return CT.WALL;
            case '.':
                return CT.PILL;
            case 'o':
                return CT.POWER;
            case '%':
                return CT.FRUIT;
            case '\\':
                return CT.LAMBDA;
            case '=':
                return CT.GHOST;
            default:
                throw new IllegalArgumentException("Oh");
        }
    }

    private CT() {
    }
}


