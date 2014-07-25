package app;

/**
 * Created by dkopiychenko on 7/25/14.
 */
public enum CT {
    SPACE, WALL, PILL, POWER, FRUIT, LAMBDA, GHOST;

    public static CT convertMapCharacter(char c) {
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
}
