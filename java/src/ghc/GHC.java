package ghc;

/**
 * Author: brox
 * Since: 2014-07-25
 */

public class GHC {

    byte[] data = new byte[256];
    byte[] code = new byte[256];
    byte _a = 0;
    byte _b = 0;
    byte _c = 0;
    byte _d = 0;
    byte _e = 0;
    byte _f = 0;
    byte _g = 0;
    byte _h = 0;
    byte pc;

    public static final byte MOV = 0;
    public static final byte INC = 1;
    public static final byte DEC = 2;
    public static final byte ADD = 3;
    public static final byte SUB = 4;
    public static final byte MUL = 5;
    public static final byte DIV = 6;
    public static final byte AND = 7;
    public static final byte OR = 8;
    public static final byte XOR = 9;
    public static final byte JLT = 10;
    public static final byte JEQ = 11;
    public static final byte JGT = 12;
    public static final byte INT = 13;
    public static final byte HLT = 14;

    public GHC() {
        for (int i = 0; i < 256; i++) {
            data[i] = 0;
        }
    }

    public void gameCycle() {
        pc = 0;
        for (int execCycleCounter = 0; execCycleCounter < 1024; execCycleCounter++) {
            if (!execCycle()) {
                break;
            }
        }
    }

    public boolean execCycle() {
        byte opco = code[pc];
        byte dst;
        byte src;
        switch (opco) {
            case MOV:
                break;
            case INC:
                break;
            case DEC:
                break;
            case ADD:
                break;
            case SUB:
                break;
            case MUL:
                break;
            case DIV:
                break;
            case AND:
                break;
            case OR:
                break;
            case XOR:
                break;
            case JLT:
                break;
            case JEQ:
                break;
            case JGT:
                break;
            case INT:
                break;
            case HLT:
                return false;
            default:
                throw new IllegalArgumentException("GHC::bad opcode");
        }
        pc++;
        return true;
    }

}
