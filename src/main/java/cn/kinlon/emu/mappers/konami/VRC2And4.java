package cn.kinlon.emu.mappers.konami;

import cn.kinlon.emu.files.CartFile;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class VRC2And4 extends VrcIrq {

    protected static final int VRC2a = 0;
    protected static final int VRC2b = 1;
    protected static final int VRC2c = 2;
    protected static final int VRC4a = 3;
    protected static final int VRC4b = 4;
    protected static final int VRC4c = 5;
    protected static final int VRC4d = 6;
    protected static final int VRC4e = 7;
    protected static final int VRC4f = 8;
    private static final long serialVersionUID = 0;
    protected final int[] chrLow = new int[8];
    protected final int[] chrHigh = new int[8];

    protected boolean useHeuristics;
    protected boolean prgSwapMode;
    protected int variant;
    protected int prgSelect0;
    protected int prgSelect1;
    protected int prgHigh;
    protected int ganbareGoemonGaiden;

    public VRC2And4(final CartFile cartFile) {
        super(cartFile);
        detectVariant(cartFile);
    }

    @Override
    public void init() {
        super.init();
        updateBanks();
    }

    protected void detectVariant(final CartFile cartFile) {
        switch (cartFile.getMapperNumber()) {

            case 22:
                variant = VRC2a;
                break;

            case 23:
                prgHigh = 0x20;
                variant = (cartFile.getSubmapperNumber() == 2) ? VRC4e : VRC2b;
                break;

            case 25:
                prgHigh = 0x20;
                switch (cartFile.getSubmapperNumber()) {
                    case 2:
                        variant = VRC4d;
                        break;
                    case 3:
                        variant = VRC2c;
                        break;
                    default:
                        variant = VRC4b;
                        break;
                }
                break;

            case 27:
                variant = VRC4f;
                break;

            default:
                variant = (cartFile.getSubmapperNumber() == 2) ? VRC4c : VRC4a;
                break;
        }

        useHeuristics = cartFile.getSubmapperNumber() == 0
                && cartFile.getMapperNumber() != 22 && cartFile.getMapperNumber() != 27;
    }

    protected int translateAddress(final int address) {
        int A0 = 0;
        int A1 = 0;

        if (useHeuristics) {
            switch (variant) {
                case VRC2c:
                case VRC4b:
                case VRC4d:
                    A0 = ((address >> 1) | (address >> 3)) & 1;
                    A1 = (address | (address >> 2)) & 1;
                    break;
                case VRC4a:
                case VRC4c:
                    A0 = ((address >> 1) | (address >> 6)) & 1;
                    A1 = ((address >> 2) | (address >> 7)) & 1;
                    break;
                case VRC2b:
                case VRC4e:
                    A0 = (address | (address >> 2)) & 1;
                    A1 = ((address >> 1) | (address >> 3)) & 1;
                    break;
            }
        } else {
            switch (variant) {
                case VRC2a:
                    A0 = (address >> 1) & 1;
                    A1 = (address & 1);
                    break;
                case VRC4f:
                    A0 = address & 1;
                    A1 = (address >> 1) & 1;
                    break;
                case VRC2c:
                case VRC4b:
                    A0 = (address >> 1) & 1;
                    A1 = (address & 1);
                    break;
                case VRC4d:
                    A0 = (address >> 3) & 1;
                    A1 = (address >> 2) & 1;
                    break;
                case VRC4a:
                    A0 = (address >> 1) & 1;
                    A1 = (address >> 2) & 1;
                    break;
                case VRC4c:
                    A0 = (address >> 6) & 1;
                    A1 = (address >> 7) & 1;
                    break;
                case VRC2b:
                    A0 = address & 1;
                    A1 = (address >> 1) & 1;
                    break;
                case VRC4e:
                    A0 = (address >> 2) & 1;
                    A1 = (address >> 3) & 1;
                    break;
            }
        }
        return (address & 0xFF00) | (A1 << 1) | A0;
    }

    protected void updatePrgBanks() {
        if (prgSwapMode) {
            setPrgBank(4, prgHigh | (-2 & 0x1F));
            setPrgBank(6, prgHigh | prgSelect0);
        } else {
            setPrgBank(4, prgHigh | prgSelect0);
            setPrgBank(6, prgHigh | (-2 & 0x1F));
        }
        setPrgBank(5, prgHigh | prgSelect1);
        setPrgBank(7, prgHigh | (-1 & 0x1F));
    }

    protected void updateChrBanks() {
        if (chrRamPresent) {
            for (int i = 7; i >= 0; i--) {
                setChrBank(i, i);
            }
        } else {
            if (ganbareGoemonGaiden > 0) {
                ganbareGoemonGaiden--;
                setChrBank(0, 0xFC);
                setChrBank(1, 0xFD);
                setChrBank(2, 0xFF);
            } else {
                for (int i = 7; i >= 0; i--) {
                    int bank = chrLow[i] | (chrHigh[i] << 4);
                    if (variant == VRC2a) {
                        bank >>= 1;
                    }
                    setChrBank(i, bank);
                }
            }
        }
    }

    protected void updateBanks() {
        updatePrgBanks();
        updateChrBanks();
    }

    protected void writePrgSwapMode(final int value) {
        prgSwapMode = getBitBool(value, 1);
        updateBanks();
    }

    protected void writePrgSelect0(final int value) {
        prgSelect0 = value & 0x1F;
        updateBanks();
    }

    protected void writePrgSelect1(final int value) {
        prgSelect1 = value & 0x1F;
        updateBanks();
    }

    protected void writeChrSelectLow(final int bank, final int value) {
        chrLow[bank] = value & 0x0F;
        updateBanks();
    }

    protected void writeChrSelectHigh(final int bank, final int value) {
        chrHigh[bank] = value & 0x1F;
        updateBanks();
    }

    protected void writeMirroringControl(final int value) {
        int mask = 0x03;
        if (!useHeuristics && (variant == VRC2a || variant == VRC2b)) {
            mask = 0x01;
        }
        setNametableMirroring(value & mask);
    }

    @Override
    protected void writeRegister(int address, final int value) {

        if (variant == VRC2a && address == 0xC007) {
            ganbareGoemonGaiden = 8;
        }

        address = translateAddress(address) & 0xF00F;

        if (address >= 0x8000 && address <= 0x8006) {
            writePrgSelect0(value);
        } else if ((variant <= VRC2c && address >= 0x9000 && address <= 0x9003)
                || (variant >= VRC4a && address >= 0x9000 && address <= 0x9001)) {
            writeMirroringControl(value);
        } else if (variant >= VRC4a && address >= 0x9002 && address <= 0x9003) {
            writePrgSwapMode(value);
        } else if (address >= 0xA000 && address <= 0xA006) {
            writePrgSelect1(value);
        } else if (address >= 0xB000 && address <= 0xE006) {
            if (chrRamPresent) {
                prgHigh = (value & 8) << 2;
                updateBanks();
            } else {
                final int bank = ((((address >> 12) & 0x07) - 3) << 1)
                        + ((address >> 1) & 0x01);
                if (getBitBool(address, 0)) {
                    writeChrSelectHigh(bank, value);
                } else {
                    writeChrSelectLow(bank, value);
                }
            }
        } else if (address == 0xF000) {
            writeIrqLatchLow(value);
        } else if (address == 0xF001) {
            writeIrqLatchHigh(value);
        } else if (address == 0xF002) {
            writeIrqControl(value);
        } else if (address == 0xF003) {
            writeIrqAcknowledge();
        }
    }
}