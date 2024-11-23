package nintaco.cheats;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public final class ProActionRocky {

    private static final int[] SHIFTS = {
            3, 13, 14, 1, 6, 9, 5, 0, 12, 7, 2, 8, 10, 11, 4,     // address
            19, 21, 23, 22, 20, 17, 16, 18,                       // compare value
            29, 31, 24, 26, 25, 30, 27, 28,                    // data value
    };

    private static final int START = 0x7e5ee93a;
    private static final int XOR = 0x5c184b91;

    private ProActionRocky() {
    }

    // By default, the converter assumes that the code does not store 
    // address bit-15.  
    public static Cheat convert(String code) {
        return convert(code, false);
    }

    // If the code stores the inverse of address bit-15 as bit-0 of the eighth 
    // nibble, then set the second parameter to true to recover it.  
    public static Cheat convert(String code, boolean codeStoresAddressBit15) {

        if (code == null || code.length() != 8) {
            return null;
        }

        int encoded;
        try {
            encoded = ((int) Long.parseLong(code, 16));
        } catch (Throwable t) {
            return null;
        }

        final int b15 = codeStoresAddressBit15
                ? (((encoded & 1) ^ 1) << 15) : 0x8000; // stores address bit-15
        encoded >>= 1;

        int reg = START;
        int decoded = 0;
        for (int i = SHIFTS.length - 1; i >= 0; i--) {
            if (getBitBool(reg ^ encoded, 30)) {
                decoded |= 1 << SHIFTS[i];
                reg ^= XOR;
            }
            encoded <<= 1;
            reg <<= 1;
        }

        return new Cheat(b15 | (decoded & 0x7fff), (decoded >> 24) & 0xff,
                (decoded >> 16) & 0xff);
    }

    // The inverse of address bit-15 is stored as bit-0 of the eighth nibble.
    public static String convert(Cheat cheat) {

        if (cheat == null || !cheat.hasCompareValue()
                || cheat.getAddress() < 0x8000) {
            return null;
        }

        final int decoded = (cheat.getDataValue() << 24)
                | (cheat.getCompareValue() << 16) | cheat.getAddress() & 0x7fff;

        int reg = START;
        int encoded = 1 ^ getBit(cheat.getAddress(), 15);
        for (int i = SHIFTS.length - 1; i >= 0; i--) {
            final int bit = decoded >> SHIFTS[i];
            if ((((reg >> 30) ^ bit) & 1) != 0) {
                encoded |= 2 << i;
            }
            if ((bit & 1) != 0) {
                reg ^= XOR;
            }
            reg <<= 1;
        }

        return String.format("%08X", encoded);
    }
}
