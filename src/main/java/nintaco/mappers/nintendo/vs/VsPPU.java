package nintaco.mappers.nintendo.vs;

public interface VsPPU {

    int RP2C03B = 0x0;
    int RP2C03G = 0x1;
    int RP2C04_0001 = 0x2;
    int RP2C04_0002 = 0x3;
    int RP2C04_0003 = 0x4;
    int RP2C04_0004 = 0x5;
    int RC2C03B = 0x6;
    int RC2C03C = 0x7;
    int RC2C05_01 = 0x8;
    int RC2C05_02 = 0x9;
    int RC2C05_03 = 0xA;
    int RC2C05_04 = 0xB;
    int RC2C05_05 = 0xC;

    String[] NAMES = {
            "RP2C03B",     // 0
            "RP2C03G",     // 1
            "RP2C04-0001", // 2
            "RP2C04-0002", // 3
            "RP2C04-0003", // 4
            "RP2C04-0004", // 5
            "RC2C03B",     // 6
            "RC2C03C",     // 7
            "RC2C05-01",   // 8
            "RC2C05-02",   // 9
            "RC2C05-03",   // A
            "RC2C05-04",   // B
            "RC2C05-05",   // C
    };

    static String toString(final int vsPPU) {
        return (vsPPU < 0 || vsPPU >= NAMES.length) ? "Other" : NAMES[vsPPU];
    }
}
