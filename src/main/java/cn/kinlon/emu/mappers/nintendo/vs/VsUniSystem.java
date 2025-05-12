package cn.kinlon.emu.mappers.nintendo.vs;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.cpu.CPU.REG_OUTPUT_PORT;
import static cn.kinlon.emu.mappers.NametableMirroring.FOUR_SCREEN;
import static cn.kinlon.emu.utils.BitUtil.getBit;

public class VsUniSystem extends Mapper {

    private static final long serialVersionUID = 0;

    public VsUniSystem(final CartFile cartFile) {
        super(cartFile, 8, 1);
        setNametableMirroring(FOUR_SCREEN);
        setPrgBank(4, 0);
        setPrgBank(5, 1);
        setPrgBank(6, 2);
        setPrgBank(7, 3);
    }

    @Override
    public void writeCpuMemory(final int address, final int value) {
        if (address == REG_OUTPUT_PORT) {
            writeBankSelect(value);
        }
        super.writeCpuMemory(address, value);
    }

    private void writeBankSelect(final int value) {
        final int bank = getBit(value, 2);
        setChrBank(bank);
        if (prgRomLength > 0x8000) {
            setPrgBank(4, bank << 2);
        }
    }
}
