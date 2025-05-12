package cn.kinlon.emu.cpu;

public enum StatusFlag {
    // C
    CARRY_FLAG(0),
    // Z
    ZERO_FLAG(1),
    // I
    INTERRUPT_DISABLE(2),
    // D
    DECIMAL_MODE(3),
    // B
    BREAK_COMMAND(4),
    // R
    RESERVED(5),
    // V
    OVERFLOW_FLAG(6),
    // N
    NEGATIVE_FLAG(7);

    private final int mask;

    StatusFlag(int i) {
        this.mask = 1 << i;
    }

    public int getMash() {
        return mask;
    }
}
