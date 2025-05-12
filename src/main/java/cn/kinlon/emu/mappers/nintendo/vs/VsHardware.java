package cn.kinlon.emu.mappers.nintendo.vs;

public interface VsHardware {

    int VS_UNISYSTEM_NORMAL = 0;
    int VS_UNISYSTEM_RBI_BASEBALL = 1;
    int VS_UNISYSTEM_TKO_BOXING = 2;
    int VS_UNISYSTEM_SUPER_XEVIOUS = 3;
    int VS_UNISYSTEM_ICE_CLIMBER_JAPAN = 4;
    int VS_DUALSYSTEM_NORMAL = 5;
    int VS_DUALSYSTEM_RAID_ON_BUNGELING_BAY = 6;

    String[] NAMES = {
            "VS. UniSystem (normal)",                  // 0
            "VS. UniSystem (RBI Baseball)",            // 1
            "VS. UniSystem (TKO Boxing)",              // 2
            "VS. UniSystem (Super Xevious)",           // 3
            "VS. UniSystem (Ice Climber Japan)",       // 4
            "VS. DualSystem (normal)",                 // 5
            "VS. DualSystem (Raid on Bungeling Bay)",  // 6
    };

    static String toString(final int vsHardware) {
        return (vsHardware < 0 || vsHardware >= NAMES.length) ? "Other"
                : NAMES[vsHardware];
    }
}
