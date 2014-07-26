package app;

/**
 * Created by dkopiychenko on 7/25/14.
 */
public enum CT {

    SPACE(1), WALL(0), PILL(2), POWER(3), FRUIT(4), LAMBDA(5), GHOST(6);

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

    int value;

    CT(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
