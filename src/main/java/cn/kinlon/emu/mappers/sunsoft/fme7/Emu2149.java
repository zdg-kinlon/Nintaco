package cn.kinlon.emu.mappers.sunsoft.fme7;

// Port of emu2149.c v1.16 -- YM2149/AY-3-8910 emulator by Mitsutaka Okazaki
// zlib license

public final class Emu2149 {

    private static final int EMU2149_VOL_DEFAULT = 1;
    private static final int EMU2149_VOL_YM2149 = 0;
    private static final int EMU2149_VOL_AY_3_8910 = 1;

    private static int PSG_MASK_CH(int x) {
        return 1 << x;
    }

    private static final int[][] voltbl = {
            {0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03, 0x04,
                    0x05, 0x06, 0x07, 0x09, 0x0B, 0x0D, 0x0F, 0x12,
                    0x16, 0x1A, 0x1F, 0x25, 0x2D, 0x35, 0x3F, 0x4C,
                    0x5A, 0x6A, 0x7F, 0x97, 0xB4, 0xD6, 0xEB, 0xFF,},
            {0x00, 0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03,
                    0x05, 0x05, 0x07, 0x07, 0x0B, 0x0B, 0x0F, 0x0F,
                    0x16, 0x16, 0x1F, 0x1F, 0x2D, 0x2D, 0x3F, 0x3F,
                    0x5A, 0x5A, 0x7F, 0x7F, 0xB4, 0xB4, 0xFF, 0xFF,},
    };

    private static final int GETA_BITS = 24;

    private static void internal_refresh(final PSG psg) {
        if (psg.quality) {
            psg.base_incr = 1 << GETA_BITS;
            psg.realstep = (int) ((1L << 31L) / psg.rate);
            psg.psgstep = (int) ((1L << 31L) / (psg.clk / 16));
            psg.psgtime = 0;
        } else {
            psg.base_incr = (int) ((double) psg.clk * (1 << GETA_BITS)
                    / (16 * psg.rate));
        }
    }

    public static void PSG_set_quality(final PSG psg, final boolean q) {
        psg.quality = q;
        internal_refresh(psg);
    }

    public static PSG PSG_new(final int c, final int r) {
        PSG psg = new PSG();
        PSG_setVolumeMode(psg, EMU2149_VOL_DEFAULT);
        psg.clk = c;
        psg.rate = (r != 0) ? r : 44100;
        PSG_set_quality(psg, false);
        return psg;
    }

    public static void PSG_setVolumeMode(final PSG psg, final int type) {
        switch (type) {
            case 1:
                psg.voltbl = voltbl[EMU2149_VOL_YM2149];
                break;
            case 2:
                psg.voltbl = voltbl[EMU2149_VOL_AY_3_8910];
                break;
            default:
                psg.voltbl = voltbl[EMU2149_VOL_DEFAULT];
                break;
        }
    }

    public static void PSG_reset(final PSG psg) {

        psg.base_count = 0;

        for (int i = 0; i < 3; i++) {
            psg.cout[i] = 0;
            psg.count[i] = 0x1000;
            psg.freq[i] = 0;
            psg.edge[i] = false;
            psg.volume[i] = 0;
        }

        psg.mask = 0;

        for (int i = 0; i < 16; i++) {
            psg.reg[i] = 0;
        }
        psg.adr = 0;

        psg.noise_seed = 0xffff;
        psg.noise_count = 0x40;
        psg.noise_freq = 0;

        psg.env_ptr = 0;
        psg.env_freq = 0;
        psg.env_count = 0;
        psg.env_pause = true;

        psg.out = 0;
    }

    public static void PSG_writeIO(final PSG psg, final int adr,
                                   final int val) {
        if ((adr & 1) != 0) {
            PSG_writeReg(psg, psg.adr, val);
        } else {
            psg.adr = val & 0x1f;
        }
    }

    private static int calc(final PSG psg) {

        int mix = 0;

        psg.base_count += psg.base_incr;
        final int incr = (psg.base_count >> GETA_BITS);
        psg.base_count &= (1 << GETA_BITS) - 1;

        /* Envelope */
        psg.env_count += incr;
        while (psg.env_count >= 0x10000 && psg.env_freq != 0) {
            if (!psg.env_pause) {
                if (psg.env_face) {
                    psg.env_ptr = (psg.env_ptr + 1) & 0x3f;
                } else {
                    psg.env_ptr = (psg.env_ptr + 0x3f) & 0x3f;
                }
            }

            if ((psg.env_ptr & 0x20) != 0) { /* if carry or borrow */
                if (psg.env_continue) {
                    if (psg.env_alternate ^ psg.env_hold) {
                        psg.env_face ^= true;
                    }
                    if (psg.env_hold) {
                        psg.env_pause = true;
                    }
                    psg.env_ptr = psg.env_face ? 0 : 0x1f;
                } else {
                    psg.env_pause = true;
                    psg.env_ptr = 0;
                }
            }

            psg.env_count -= psg.env_freq;
        }

        /* Noise */
        psg.noise_count += incr;
        if ((psg.noise_count & 0x40) != 0) {
            if ((psg.noise_seed & 1) != 0) {
                psg.noise_seed ^= 0x24000;
            }
            psg.noise_seed >>= 1;
            psg.noise_count -= psg.noise_freq;
        }

        final boolean noise = (psg.noise_seed & 1) != 0;

        /* Tone */
        for (int i = 0; i < 3; i++) {
            psg.count[i] += incr;
            if ((psg.count[i] & 0x1000) != 0) {
                if (psg.freq[i] > 1) {
                    psg.edge[i] = !psg.edge[i];
                    psg.count[i] -= psg.freq[i];
                } else {
                    psg.edge[i] = true;
                }
            }

            psg.cout[i] = 0; // maintaining cout for stereo mix

            if ((psg.mask & PSG_MASK_CH(i)) != 0) {
                continue;
            }

            if ((psg.tmask[i] || psg.edge[i]) && (psg.nmask[i] || noise)) {
                if ((psg.volume[i] & 32) == 0) {
                    psg.cout[i] = psg.voltbl[psg.volume[i] & 31];
                } else {
                    psg.cout[i] = psg.voltbl[psg.env_ptr];
                }

                mix += psg.cout[i];
            }
        }

        return mix;
    }

    public static int PSG_calc(final PSG psg) {
        if (!psg.quality) {
            return calc(psg) << 4;
        }

        /* Simple rate converter */
        while (psg.realstep > psg.psgtime) {
            psg.psgtime += psg.psgstep;
            psg.out += calc(psg);
            psg.out >>= 1;
        }

        psg.psgtime = psg.psgtime - psg.realstep;

        return psg.out << 4;
    }

    public static void PSG_writeReg(final PSG psg, final int reg,
                                    final int val) {

        if (reg > 15) {
            return;
        }

        psg.reg[reg] = val & 0xff;

        switch (reg) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: {
                final int c = reg >> 1;
                psg.freq[c] = ((psg.reg[c * 2 + 1] & 15) << 8) + psg.reg[c * 2];
                break;
            }

            case 6:
                psg.noise_freq = (val == 0) ? 1 : ((val & 31) << 1);
                break;

            case 7:
                psg.tmask[0] = (val & 1) != 0;
                psg.tmask[1] = (val & 2) != 0;
                psg.tmask[2] = (val & 4) != 0;
                psg.nmask[0] = (val & 8) != 0;
                psg.nmask[1] = (val & 16) != 0;
                psg.nmask[2] = (val & 32) != 0;
                break;

            case 8:
            case 9:
            case 10:
                psg.volume[reg - 8] = val << 1;
                break;

            case 11:
            case 12:
                psg.env_freq = (psg.reg[12] << 8) + psg.reg[11];
                break;

            case 13:
                psg.env_continue = ((val >> 3) & 1) != 0;
                psg.env_attack = ((val >> 2) & 1) != 0;
                psg.env_alternate = ((val >> 1) & 1) != 0;
                psg.env_hold = (val & 1) != 0;
                psg.env_face = psg.env_attack;
                psg.env_pause = false;
                psg.env_count = 0x10000 - psg.env_freq;
                psg.env_ptr = psg.env_face ? 0 : 0x1f;
                break;
        }

        return;
    }

    private Emu2149() {
    }
}
