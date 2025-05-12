package cn.kinlon.emu.mappers.sunsoft.fme7;

import java.io.*;

public class PSG implements Serializable {

    private static final long serialVersionUID = 0;

    /* Volume Table */
    public int[] voltbl;

    public final int[] reg = new int[0x20];
    public int out;
    public final int[] cout = new int[3];

    public int clk;
    public int rate;
    public int base_incr;
    public boolean quality;

    public final int[] count = new int[3];
    public final int[] volume = new int[3];
    public final int[] freq = new int[3];
    public final boolean[] edge = new boolean[3];
    public final boolean[] tmask = new boolean[3];
    public final boolean[] nmask = new boolean[3];
    public int mask;

    public int base_count;

    public int env_ptr;
    public boolean env_face;

    public boolean env_continue;
    public boolean env_attack;
    public boolean env_alternate;
    public boolean env_hold;
    public boolean env_pause;

    public int env_freq;
    public int env_count;

    public int noise_seed;
    public int noise_count;
    public int noise_freq;

    /* rate converter */
    public int realstep;
    public int psgtime;
    public int psgstep;

    /* I/O Ctrl */
    public int adr;
}
