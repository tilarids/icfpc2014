package ghc;

/**
 * Author: brox
 * Since: 2014-07-25
 */

public class GHC {

    byte[] data = new byte[256];
    byte[] code = new byte[256];
    byte pc;

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
        pc++;
        return true;
    }

}
