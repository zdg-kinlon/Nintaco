package cn.kinlon.emu.files;

public interface ExtendedConsole {

    int REGULAR = 0x0;
    int VS_SYSTEM = 0x1;
    int PLAYCHOICE_10 = 0x2;
    int VT03 = 0x7;
    int VT09 = 0x8;
    int VT369 = 0xA;

    String[] NAMES = {
            "Regular NES/Famicom/Dendy",          // 0
            "VS. System",                         // 1
            "PlayChoice-10",                      // 2
            "Famiclone with Decimal Mode",        // 3
            "V.R. Technology VT01 (monochrome)",  // 4
            "V.R. Technology VT01 (STN)",         // 5
            "V.R. Technology VT02",               // 6
            "V.R. Technology VT03",               // 7
            "V.R. Technology VT09",               // 8
            "V.R. Technology VT32",               // 9
            "V.R. Technology VT369",              // A
    };

    static String toString(final int extendedConsole) {
        return (extendedConsole < 0 || extendedConsole >= NAMES.length) ? "Other"
                : NAMES[extendedConsole];
    }
}