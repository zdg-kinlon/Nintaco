package nintaco.mappers.pirate;

import nintaco.files.NesFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class Supervision extends Mapper {

    private final int[] regs = new int[2];

    public Supervision(final NesFile nesFile) {
        super(nesFile, 8, 1, 0x6000, 0x6000);
    }

    private void updateBanks() {
        final int r = (regs[0] << 3) & 0x78;
        setPrgBank(3, ((r << 1) | 0x0F) + 0x04);
        setPrgBanks(4, 2, ((regs[0] & 0x10) != 0 ? ((r | (regs[1] & 0x07)) + 0x02)
                : 0x00) << 1);
        setPrgBanks(6, 2, ((regs[0] & 0x10) != 0 ? ((r | (0xFF & 0x07)) + 0x02)
                : 0x01) << 1);

        setNametableMirroring(getBit(regs[0], 5));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        regs[getBit(address, 15)] = value;
        updateBanks();
    }
}