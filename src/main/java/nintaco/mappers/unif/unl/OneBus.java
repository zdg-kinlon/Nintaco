package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.files.ExtendedConsole.*;
import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.*;

public abstract class OneBus extends Mapper {

    protected static final long serialVersionUID = 0;

    protected static final int[] VB0STable = {0, 1, 2, 0, 3, 4, 5, 0};
    protected static final int[] Bank2RVIndex
            = {0x04, 0x04, 0x05, 0x05, 0x00, 0x01, 0x02, 0x03};
    protected static final int[] Bank2AND
            = {0xFE, 0xFE, 0xFE, 0xFE, 0xFF, 0xFF, 0xFF, 0xFF};
    protected static final int[] Bank2OR
            = {0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00};

    // Table to use when writing to the "compatible" (8000/8001) registers
    protected static final int[] MMC3_VT02
            = {0x2016, 0x2017, 0x2012, 0x2013, 0x2014, 0x2015, 0x4107, 0x4108};

    // Extended bits in PPUAddr:
    //   10-11: Read type
    //   12-14: EVA
    protected static final int READ_NORMAL = 0;
    protected static final int READ_BG = 1;
    protected static final int READ_SP = 2;

    protected final int[] RV = new int[7];
    protected final int[] PQ = new int[8];
    protected final int[] ALU = new int[8];

    protected final int consoleType;

    protected boolean PIX16EN;
    protected boolean BK16EN;
    protected boolean SP16EN;
    protected boolean SPEXTEN;
    protected boolean BKEXTEN;
    protected boolean SPOPEN;
    protected boolean V16BEN;
    protected boolean COLCOMP1;

    protected boolean IVRCH;
    protected boolean EVA12S;
    protected boolean PQ2EN;
    protected boolean EVRAMEN;
    protected boolean FWEN;
    protected boolean TSYNEN;
    protected boolean IRQEnabled;
    protected boolean IRQReload;
    protected int BKPAGE;
    protected int VRWB;
    protected int COMR6;
    protected int COMR7;
    protected int PS;
    protected int VA;
    protected int CHRBankMask;
    protected int BGSprConfig;
    protected int IRQType;
    protected int IRQLatch;
    protected int IRQCounter;
    protected int IRQLatency;
    protected int IRQDelay;
    protected int Mirroring;
    protected int MMC3Cmd;
    protected int CHRBase;
    protected int PRGBase;
    protected int v415c;

    public OneBus(final CartFile cartFile) {
        super(cartFile, 8, 8);
        consoleType = (cartFile.getConsole() == Console.EXTENDED)
                ? cartFile.getExtendedConsole() : cartFile.getConsole();
    }

    @Override
    public void init() {
        CHRBankMask = 0xFF;
        PRGBase = 0;
        CHRBase = 0;
        PQ[0] = 0x3C;
        PQ[1] = 0x3D;
        PQ[2] = 0x00;
        PQ[3] = 0x00;
        PQ[4] = 0x00;
        PQ[5] = 0x00;
        PQ[6] = 0x00;
        PQ[7] = 0x00;
        RV[0] = 4;
        RV[1] = 5;
        RV[2] = 6;
        RV[3] = 7;
        RV[4] = 0;
        RV[5] = 2;
        RV[6] = 0;
        IRQType = IRQLatch = IRQCounter = IRQLatency = IRQDelay = VA
                = BKPAGE = VRWB = COMR6 = COMR7 = PS = 0;
        IRQReload = IRQEnabled = IVRCH = EVA12S = EVRAMEN = FWEN = PQ2EN = TSYNEN
                = false;
        Mirroring = MMC3Cmd = 0;

        write2(0x2010, 0x00); // reset video mode
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    protected void SetPRG_ROM8(final int bank, final int value) {
        prgBanks[bank >> 1] = PRGBase + (value << 13);
    }

    // TODO THIS DOES A 16-BIT READ?!  WTF?
    @Override
    public int readVRAM(final int address) {

        if (address >= 0x2000) {
            return super.readVRAM(address);
        }

        if (IVRCH) {
            // CIRAM as CHR-RAM
            return super.readVRAM(0x2000 | (address & 0x0FFF));
        }

        int EVA;
        int CHRAddr;
        int CHRData;

        final int PPUBank = (address >> 10) ^ COMR7;
        int CHRBank = (((RV[Bank2RVIndex[PPUBank]] & Bank2AND[PPUBank])
                | Bank2OR[PPUBank]) & CHRBankMask) | (RV[6] & ~CHRBankMask);

        // Apply address extension. The extended address is used
        //   1) when reading a background tile, and background address extension is 
        //      enabled, using the attribute bits as bank bits together with either 
        //      the mirroring or the page bit;
        //   2) when reading a sprite tile, and sprite address extension is enabled, 
        //      using the SPEVA bits in the OAM data;
        //   3) when reading CHR data from $2007, and either background or sprite 
        //      address extension is enabled, using the VRWB bits as bank bits.

        final int ReadType = (address >> 10) & 3;
        if ((ReadType == READ_BG && BKEXTEN) || (ReadType == READ_SP && SPEXTEN)
                || (ReadType == READ_NORMAL && (BKEXTEN || SPEXTEN))) {
            switch (ReadType) {
                case READ_BG:
                    EVA = ((address >> 12) & 3) | ((EVA12S ? (Mirroring & 1) : BKPAGE)
                            << 2);
                    break;
                case READ_SP:
                    EVA = (address >> 12) & 7;
                    break;
                default:
                    EVA = VRWB;
                    break;
            }
            CHRBank <<= 3;
            CHRBank |= VA & ~(7 << 8);
            CHRBank |= EVA;
        } else {
            CHRBank |= VA;
        }
        CHRAddr = CHRBank << 10;

        if ((ReadType == READ_BG && BK16EN) || (ReadType == READ_SP && SP16EN)) {
            if (V16BEN) {
                CHRAddr |= address & 0x3FF;
                CHRAddr <<= 1;
                if (CHRBase != 0) {
                    CHRAddr = CHRBase | (CHRAddr & (CHRBase - 1));
                }
                CHRAddr &= chrRomSizeMask;
                CHRData = chrROM[CHRAddr];
                CHRData |= chrROM[CHRAddr | 0x01] << 8;
            } else {
                CHRAddr |= address & 0x3F0;
                CHRAddr <<= 1;
                if (CHRBase != 0) {
                    CHRAddr = CHRBase | (CHRAddr & (CHRBase - 1));
                }
                CHRAddr |= address & 0x00F;
                CHRAddr &= chrRomSizeMask;
                CHRData = chrROM[CHRAddr];
                CHRData |= chrROM[CHRAddr | 0x10] << 8;
            }
        } else {
            // Normal 8-bit read
            CHRAddr |= address & 0x3FF;
            if (CHRBase != 0) {
                CHRAddr = CHRBase | (CHRAddr & (CHRBase - 1));
            }
            CHRData = chrROM[CHRAddr & chrRomSizeMask];
        }

        return CHRData;
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (IVRCH && address < 0x2000) {
            // CIRAM as CHR-RAM
            super.writeVRAM(0x2000 | (address & 0x0FFF), value);
        } else {
            super.writeVRAM(address, value);
        }
    }

    protected void updateState() {
        final int TPAMask = (PS == 7) ? 0xFF : (0x3F >> PS);
        final int PQ3Mask = ~TPAMask;

        final int outerBank = (PQ[3] & PQ3Mask) | (PQ[7] << 8);
        SetPRG_ROM8(0x8 ^ COMR6, (PQ[0] & TPAMask) | outerBank);
        SetPRG_ROM8(0xA, (PQ[1] & TPAMask) | outerBank);
        SetPRG_ROM8(0xC ^ COMR6, ((PQ2EN ? PQ[2] : 0xFE) & TPAMask) | outerBank);
        SetPRG_ROM8(0xE, (0xFF & TPAMask) | outerBank);

        setPrgBank(3, 0);

        if (EVRAMEN) {
            setNametableMirroring(FOUR_SCREEN);
        } else {
            setNametableMirroring(Mirroring & 3);
        }

        // TODO IS THIS NEEDED?
//    *EMU->multiPRGSize  =(TPAMask +1) <<13;
//    *EMU->multiPRGStart =ROM->PRGROMData + ((outerBank <<13) &PRGAddrMask);
    }

    protected void write2(final int address, int value) {
        final int MaskedAddr = address & 0xF3F;
        switch (MaskedAddr) {
            case 0x000:
                BGSprConfig = value & 0x38;
                super.writeCpuMemory(address, value);
                break;
            case 0x010:
                if (consoleType < VT03) {
                    value &= 0x78;
                }
                if (consoleType < VT09) {
                    value &= 0x9F;
                }
                PIX16EN = getBitBool(value, 7);
                BK16EN = getBitBool(value, 6);
                SP16EN = getBitBool(value, 5);
                SPEXTEN = getBitBool(value, 4);
                BKEXTEN = getBitBool(value, 3);
                SPOPEN = getBitBool(value, 2);
                V16BEN = getBitBool(value, 1);
                COLCOMP1 = getBitBool(value, 0);
                super.writeCpuMemory(address, value);
                updateState();
                break;
            case 0x011:
                EVA12S = (value & 3) != 0; // Documentation is contradictory on whether 
                // it's bit 0 or bit 1!
                EVRAMEN = getBitBool(value, 3);
                updateState();
                break;
            case 0x012:
            case 0x013:
            case 0x014:
            case 0x015:
            case 0x016:
            case 0x017:
                RV[MaskedAddr - 0x012] = value;
                break;
            case 0x018:
                VA = (VA & ~(7 << 8)) | (((value >> 4) & 7) << 8);
                BKPAGE = getBit(value, 3);
                VRWB = value & 7;
                updateState();
                break;
            case 0x01A:
                RV[6] = value & 0xF8;
                CHRBankMask = 0xFF >> VB0STable[value & 7];
                break;
            case 0x01C:
                break;    // ?? written alongside 2005
            case 0x01D:    // ??
                RV[4] = 0x2A;
                break;
            case 0x01E:    // ?? 
                RV[5] = 0x2C;
                break;
            case 0x01F:    // ?? 
                break;
            case 0x024:    // ??
                break;
            case 0x04C: // ??
                break;
            case 0x040:
            case 0x041:
            case 0x042:
            case 0x043:
            case 0x044:
            case 0x045:
            case 0x046:
            case 0x047:
            case 0x048:
            case 0x049: // LCD
                break;
            default:
                super.writeCpuMemory(address, value);
                break;
        }

        // TODO IS THIS NEEDED?
//    *EMU->multiCHRStart =ROM->CHRROMData +( ((((RV[6] &~CHRBankMask) <<(videoMode.BK16EN? 1: 0)) <<10) +CHRBase) &CHRAddrMask);
//    *EMU->multiCHRSize = 0;   
    }

    protected int read4(final int address) {
        switch (address) {
            case 0x4130:
            case 0x4131:
            case 0x4132:
            case 0x4133:
            case 0x4134:
            case 0x4135:
            case 0x4136:
            case 0x4137:
                return ALU[address & 7];
            case 0x4148:
            case 0x414A:
            case 0x414F:
                return 0xFF;
            case 0x415C:
                ++v415c;
                v415c &= 0xFF;
                return v415c ^ 0xFF;
            case 0x41B7:
                return 0x04;
            case 0x48A4:
            case 0x48A5:
                return 0x01;
            case 0x412B: // Random number generator used!
            default:
                return super.readMemory(address);
        }
    }

    protected void write4(final int address, final int value) {
        super.writeCpuMemory(address, value);
        switch (address) {
            case 0x4100:
                VA = (VA & ~(15 << 11)) | ((value & 15) << 11);
                PQ[7] = (PQ[7] & ~0x0F) | (value >> 4);
                updateState();
                break;
            case 0x4101:
                IRQLatch = value;
                break;
            case 0x4102:
                IRQReload = true;
                break;
            case 0x4103:
                IRQEnabled = false;
                cpu.interrupt().setMapperIrq(false);
                break;
            case 0x4104:
                IRQEnabled = true;
                break;
            case 0x4105:
                COMR7 = (value & 0x80) >> 5;
                COMR6 = (value & 0x40) >> 4;
                IVRCH = getBitBool(value, 5); // Use CIRAM as CHR-RAM
                MMC3Cmd = value & 7;
                updateState();
                break;
            case 0x4106:
                Mirroring = value; // MMC3 A000 bit 0
                if (consoleType < VT09) {
                    Mirroring &= 1; // No one-screen mirroring on VT02/VT03
                }
                // *EMU->multiMirroring =value &1; // TODO REQUIRED?
                updateState();
                break;
            case 0x4107:
            case 0x4108:
            case 0x4109:
            case 0x410A:
                PQ[address - 0x4107] = value;
                updateState();
                break;
            case 0x410B:
                PS = value & 7;
                FWEN = getBitBool(value, 3);
                PQ2EN = getBitBool(value, 6);
                TSYNEN = getBitBool(value, 7);
                if (consoleType == VT09) {
                    TSYNEN = true;
                }
                updateState();
                super.writeCpuMemory(address, value);
                // *EMU->multiCanSave =TRUE; // TODO REQUIRED?
                break;
            case 0x4130:
                ALU[0] = value;
                break;
            case 0x4131:
                ALU[1] = value;
                break;
            case 0x4132:
                ALU[2] = value;
                break;
            case 0x4133:
                ALU[3] = value;
                break;
            case 0x4134:
                ALU[4] = value;
                break;
            case 0x4135:
                ALU[5] = value;
            {
                final int result = ((ALU[5] << 8) | ALU[4]) * ((ALU[1] << 8)
                        | ALU[0]);
                ALU[0] = result & 0xFF;
                ALU[1] = (result >> 8) & 0xFF;
                ALU[2] = (result >> 16) & 0xFF;
                ALU[3] = (result >> 24) & 0xFF;
            }
            break;
            case 0x4136:
                ALU[6] = value;
                break;
            case 0x4137:
                ALU[7] = value;
            {
                final int quotient = ((ALU[3] << 24) | (ALU[2] << 16) | (ALU[1] << 8)
                        | ALU[0]) / ((ALU[7] << 8) | ALU[6]);
                final int remainder = ((ALU[3] << 24) | (ALU[2] << 16) | (ALU[1] << 8)
                        | ALU[0]) % ((ALU[7] << 8) | ALU[6]);
                ALU[0] = quotient & 0xFF;
                ALU[1] = (quotient >> 8) & 0xFF;
                ALU[2] = (quotient >> 16) & 0xFF;
                ALU[3] = (quotient >> 24) & 0xFF;
                ALU[4] = remainder & 0xFF;
                ALU[5] = (remainder >> 8) & 0xFF;
                ALU[6] = 0;
                ALU[7] = 0;
            }
            break;
        }
    }

    protected void writeMMC3(final int address, final int value) {
        if (!FWEN) {
            switch (address & 0xE001) {
                case 0x8000:
                    write4(0x4105, value & ~0x20); // Do not pass on the IVRCH bit
                    break;
                case 0x8001:
                    final int addr = MMC3_VT02[MMC3Cmd];
                    if ((addr >> 12) == 2) {
                        write2(addr, value);
                    } else {
                        write4(addr, value);
                    }
                    break;
                case 0xA000:
                    write4(0x4106, value & 1);
                    break; // Do not pass on the one-screen mirroring bit
                case 0xA001:
                    break;
                case 0xC000:
                    write4(0x4101, value);
                    break;
                case 0xC001:
                    write4(0x4102, value);
                    break;
                case 0xE000:
                    write4(0x4103, value);
                    break;
                case 0xE001:
                    write4(0x4104, value);
                    break;
            }
        }
    }

    @Override
    public int readMemory(final int address) {
        return ((address & 0xF000) == 0x4000) ? read4(address)
                : super.readMemory(address);
    }

    @Override
    public void writeCpuMemory(int address, int value) {
        switch (address & 0xF000) {
            case 0x2000:
                write2(address, value);
                break;
            case 0x4000:
                write4(address, value);
                break;
            case 0x8000:
            case 0x9000:
            case 0xA000:
            case 0xB000:
            case 0xC000:
            case 0xD000:
            case 0xE000:
            case 0xF000:
                writeMMC3(address, value);
                break;
            default:
                super.writeCpuMemory(address, value);
                break;
        }
    }

    @Override
    public void handlePpuCycle(final int scanline,
                               final int scanlineCycle, int address, final boolean rendering) {

        if ((BGSprConfig & 0x38) == 0x10 && address < 0x2000) {
            address ^= 0x1000;
        }

        if (consoleType == VT369 && BK16EN && scanline == -1) {
            return;
        }
        if (IRQLatency != 0) {
            --IRQLatency;
        }
        if (IRQDelay != 0 && --IRQDelay == 0) {
            cpu.interrupt().setMapperIrq(true);
        }
        if ((!TSYNEN && (address & 0x1000) != 0)
                || (TSYNEN && scanlineCycle == 256 && rendering)) {
            if (IRQLatency == 0) {
                if (IRQCounter == 0 || IRQReload) {
                    IRQCounter = IRQLatch;
                    IRQReload = false;
                } else {
                    --IRQCounter;
                }
                if (IRQCounter == 0 && IRQEnabled) {
                    if (consoleType == VT369 && BK16EN) {
                        IRQDelay = 32;
                    } else {
                        cpu.interrupt().setMapperIrq(true);
                    }
                }
            }
            IRQLatency = 8;
        }
    }

    protected void SetPRGBase(final int value) {
        PRGBase = value;
        updateState();
    }

    protected void SetCHRBase(final int value) {
        CHRBase = value;
        updateState();
    }
}