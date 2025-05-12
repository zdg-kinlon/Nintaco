package cn.kinlon.emu.mappers.ntdec;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper041 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean innerChrBankSelectEnabled;

    public Mapper041(final CartFile cartFile) {
        super(cartFile, 2, 1);
        setPrgBank(1, 0);
    }

    @Override
    public void writeMemory(int address, int value) {
        if ((address & 0xF800) == 0x6000) {
            writeOuterBank(address);
        } else if (address >= 0x8000) {
            writerInnerChrBank(value);
        } else {
            memory[address] = value;
        }
    }

    private void writeOuterBank(int address) {
        innerChrBankSelectEnabled = getBitBool(address, 2);
        setPrgBank(address & 7);
        chrBanks[0] = (chrBanks[0] & 0x06000) | ((address & 0x0018) << 12);
        setNametableMirroring(getBit(address, 5));
    }

    private void writerInnerChrBank(int value) {
        if (innerChrBankSelectEnabled) {
            chrBanks[0] = (chrBanks[0] & 0x18000) | ((value & 3) << 13);
        }
    }
}