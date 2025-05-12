package cn.kinlon.emu.mappers.konami.vrc7;

import java.io.Serializable;

public class OPLL_SLOT implements Serializable {

    private static final long serialVersionUID = 0;

    public OPLL_PATCH patch;

    public boolean type;     // false : modulator, true : carrier

    // OUTPUT
    public int feedback;
    public final int[] output = new int[2];   // Output value of slot

    // for Phase Generator (PG)
    public int[] sintbl;     // Wavetable
    public int phase;        // Phase
    public int dphase;       // Phase increment amount
    public int pgout;        // output

    // for Envelope Generator (EG)
    public int fnum;         // F-Number
    public int block;        // Block
    public int volume;       // Current volume
    public boolean sustine;  // Sustine true = ON, false = OFF
    public int tll;             // Total Level + Key scale level
    public int rks;          // Key scale offset (Rks)
    public int eg_mode;      // Current state
    public int eg_phase;     // Phase
    public int eg_dphase;    // Phase increment amount
    public int egout;        // output
}
