package nintaco.mappers.konami.vrc7;

import java.io.Serializable;

public class OPLL implements Serializable {

    private static final long serialVersionUID = 0;

    public int adr;
    public int out;

    public int realstep;
    public int oplltime;
    public int opllstep;
    public int prev;
    public int next;
    public int[] sprev = new int[2];
    public int[] snext = new int[2];
    public int[] pan = new int[16];

    // Register
    public int[] reg = new int[0x40];
    public boolean[] slot_on_flag = new boolean[18];

    // Pitch Modulator
    public int pm_phase;
    public int lfo_pm;

    // Amp Modulator
    public int am_phase;
    public int lfo_am;

    // Noise Generator
    public int noise_seed;

    // Channel Data
    public int[] patch_number = new int[9];
    public boolean[] key_status = new boolean[9];

    // Slot
    public OPLL_SLOT[] slot = new OPLL_SLOT[18];

    // Voice Data
    public OPLL_PATCH[] patch = new OPLL_PATCH[19 * 2];
    public int[] patch_update = new int[2]; // flag for check patch update

    public OPLL() {
        for (int i = slot.length - 1; i >= 0; i--) {
            slot[i] = new OPLL_SLOT();
        }
        for (int i = patch.length - 1; i >= 0; i--) {
            patch[i] = new OPLL_PATCH();
        }
    }
}
