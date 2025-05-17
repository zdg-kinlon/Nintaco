package cn.kinlon.emu.ppu;

public class PPU2C05 extends PPU {

    private final int ppuStatusID;

    public PPU2C05(final int ppuStatusID) {
        this.ppuStatusID = ppuStatusID;
    }

    @Override
    public void writeRegister(final int register, final int value) {
        switch (register) {
            case REG_PPU_CTRL:
                super.writeRegister(REG_PPU_MASK, value);
                break;
            case REG_PPU_MASK:
                super.writeRegister(REG_PPU_CTRL, value);
                break;
            default:
                super.writeRegister(register, value);
                break;
        }
    }

    private int applyStatusID(final int register, final int value) {
        return register == REG_PPU_STATUS ? (value & 0xC0) | ppuStatusID : value;
    }

    @Override
    public int peekRegister(final int register) {
        return applyStatusID(register, super.peekRegister(register));
    }

    @Override
    public int readRegister(final int register) {
        return applyStatusID(register, super.readRegister(register));
    }
}
