package nintaco.mappers.homebrew;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;
import nintaco.preferences.GamePrefs;

import java.io.DataInput;
import java.io.IOException;

import static java.util.Arrays.fill;
import static nintaco.mappers.NametableMirroring.FOUR_SCREEN;
import static nintaco.mappers.NametableMirroring.ONE_SCREEN_A;
import static nintaco.util.BitUtil.getBitBool;

public class COOLGIRL extends Mapper {

    private static final int save_flash_size = 0x400000;
    private static final int save_flash_mask = save_flash_size - 1;
    private final int[] flash_buffer_a = new int[10];
    private final int[] flash_buffer_v = new int[10];
    private final boolean[] flashBanks = new boolean[8];
    private final int[] TKSMIR = new int[8];
    private boolean REG_WRAM_enabled;
    private int REG_WRAM_page;
    private boolean REG_can_write_CHR_RAM;
    private boolean REG_map_ROM_on_6000;
    private int REG_flags;
    private int REG_mapper;
    private boolean REG_can_write_PRG;
    private int REG_mirroring;
    private boolean REG_four_screen;
    private boolean REG_lockout;
    private int REG_PRG_base;
    private int REG_PRG_mask;
    private int REG_PRG_mode;
    private int REG_PRG_bank_6000;
    private int REG_PRG_bank_A;
    private int REG_PRG_bank_B;
    private int REG_PRG_bank_C;
    private int REG_PRG_bank_D;
    private int REG_CHR_mask;
    private int REG_CHR_mode;
    private int REG_CHR_bank_A;
    private int REG_CHR_bank_B;
    private int REG_CHR_bank_C;
    private int REG_CHR_bank_D;
    private int REG_CHR_bank_E;
    private int REG_CHR_bank_F;
    private int REG_CHR_bank_G;
    private int REG_CHR_bank_H;
    private boolean REG_scanline_IRQ_enabled;
    private int REG_scanline_IRQ_counter;
    private int REG_scanline_IRQ_latch;
    private boolean REG_scanline_IRQ_reload;
    private boolean REG_scanline2_IRQ_enabled;
    private int REG_scanline2_IRQ_line;
    private boolean REG_scanline2_IRQ_pending;
    private int REG_CPU_IRQ_value;
    private int REG_CPU_IRQ_control;
    private int REG_CPU_IRQ_latch;
    private int REG_VRC4_IRQ_prescaler;
    private int REG_VRC4_IRQ_prescaler_counter;
    private int REG_R0;
    private int REG_R1;
    private int REG_R2;
    private int REG_R3;
    private int REG_R4;
    private int REG_R5;
    private int flash_state;
    private boolean ppu_latch0;
    private boolean ppu_latch1;
    private int ppu_mapper163_latch;
    private int REG_PRG_bank_6000_mapped;
    private int REG_PRG_bank_A_mapped;
    private int REG_PRG_bank_B_mapped;
    private int REG_PRG_bank_C_mapped;
    private int REG_PRG_bank_D_mapped;

    private int LastPPUScanline;
    private boolean LastPPUIsRendering;

    private transient int[] SAVE_FLASH;

    public COOLGIRL(final CartFile cartFile) {
        super(cartFile, 8, 8);
        xram = new int[0x8000];
        loadFlash();
    }

    @Override
    public void init() {
        REG_WRAM_enabled = false;
        REG_WRAM_page = 0;
        REG_can_write_CHR_RAM = false;
        REG_map_ROM_on_6000 = false;
        REG_flags = 0;
        REG_mapper = 0;
        REG_can_write_PRG = false;
        REG_mirroring = 0;
        REG_four_screen = false;
        REG_lockout = false;
        REG_PRG_base = 0;
        REG_PRG_mask = 0xF8; // 11111000, 128KB
        REG_PRG_mode = 0;
        REG_PRG_bank_6000 = 0;
        REG_PRG_bank_A = 0;
        REG_PRG_bank_B = 1;
        REG_PRG_bank_C = 0xFE;
        REG_PRG_bank_D = 0xFF;
        REG_CHR_mask = 0;
        REG_CHR_mode = 0;
        REG_CHR_bank_A = 0;
        REG_CHR_bank_B = 1;
        REG_CHR_bank_C = 2;
        REG_CHR_bank_D = 3;
        REG_CHR_bank_E = 4;
        REG_CHR_bank_F = 5;
        REG_CHR_bank_G = 6;
        REG_CHR_bank_H = 7;
        REG_scanline_IRQ_enabled = false;
        REG_scanline_IRQ_counter = 0;
        REG_scanline_IRQ_latch = 0;
        REG_scanline_IRQ_reload = false;
        REG_scanline2_IRQ_enabled = false;
        REG_scanline2_IRQ_line = 0;
        REG_CPU_IRQ_value = 0;
        REG_CPU_IRQ_control = 0;
        REG_CPU_IRQ_latch = 0;
        REG_VRC4_IRQ_prescaler = 0;
        REG_VRC4_IRQ_prescaler_counter = 0;
        REG_R0 = 0;
        REG_R1 = 0;
        REG_R2 = 0;
        REG_R3 = 0;
        REG_R4 = 0;
        REG_R5 = 0;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updatePrgBanks() {

        REG_PRG_bank_6000_mapped = (REG_PRG_base << 1) | (REG_PRG_bank_6000
                & (((~REG_PRG_mask & 0x7F) << 1) | 1));
        REG_PRG_bank_A_mapped = (REG_PRG_base << 1) | (REG_PRG_bank_A
                & (((~REG_PRG_mask & 0x7F) << 1) | 1));
        REG_PRG_bank_B_mapped = (REG_PRG_base << 1) | (REG_PRG_bank_B
                & (((~REG_PRG_mask & 0x7F) << 1) | 1));
        REG_PRG_bank_C_mapped = (REG_PRG_base << 1) | (REG_PRG_bank_C
                & (((~REG_PRG_mask & 0x7F) << 1) | 1));
        REG_PRG_bank_D_mapped = (REG_PRG_base << 1) | (REG_PRG_bank_D
                & (((~REG_PRG_mask & 0x7F) << 1) | 1));

        final boolean REG_A_CHIP = REG_PRG_bank_A_mapped >= 0x1FE00;
        final boolean REG_B_CHIP = REG_PRG_bank_B_mapped >= 0x1FE00;
        final boolean REG_C_CHIP = REG_PRG_bank_C_mapped >= 0x1FE00;
        final boolean REG_D_CHIP = REG_PRG_bank_D_mapped >= 0x1FE00;

        switch (REG_PRG_mode & 7) {
            default:
            case 0:
                set2PrgBanks(4, REG_PRG_bank_A_mapped & ~1, REG_A_CHIP);
                set2PrgBanks(6, REG_PRG_bank_C_mapped & ~1, REG_C_CHIP);
                break;
            case 1:
                set2PrgBanks(4, REG_PRG_bank_C_mapped & ~1, REG_C_CHIP);
                set2PrgBanks(6, REG_PRG_bank_A_mapped & ~1, REG_A_CHIP);
                break;
            case 4:
                setPrgBank(4, REG_PRG_bank_A_mapped, REG_A_CHIP);
                setPrgBank(5, REG_PRG_bank_B_mapped, REG_B_CHIP);
                setPrgBank(6, REG_PRG_bank_C_mapped, REG_C_CHIP);
                setPrgBank(7, REG_PRG_bank_D_mapped, REG_D_CHIP);
                break;
            case 5:
                setPrgBank(4, REG_PRG_bank_C_mapped, REG_C_CHIP);
                setPrgBank(5, REG_PRG_bank_B_mapped, REG_B_CHIP);
                setPrgBank(6, REG_PRG_bank_A_mapped, REG_A_CHIP);
                setPrgBank(7, REG_PRG_bank_D_mapped, REG_D_CHIP);
                break;
            case 6:
                set4PrgBanks(4, REG_PRG_bank_B_mapped & ~3, REG_A_CHIP);
                break;
            case 7:
                set4PrgBanks(4, REG_PRG_bank_A_mapped & ~3, REG_A_CHIP);
                break;
        }

        if (REG_map_ROM_on_6000) {
            setPrgBank(3, REG_PRG_bank_6000_mapped); // Map ROM on $6000-$7FFF
        } else if (REG_WRAM_enabled) {
            setPrgBank(3, REG_WRAM_page); // Select WRAM page
        } else {
            setPrgBank(3, -5);
        }
    }

    private void setPrgBank(final int bank, final int value,
                            final boolean flashBank) {
        setPrgBank(bank, value);
        flashBanks[bank] = flashBank;
    }

    private void set2PrgBanks(final int firstBank, final int firstValue,
                              final boolean flashBank) {
        setPrgBank(firstBank, firstValue, flashBank);
        setPrgBank(firstBank + 1, firstValue + 1, flashBank);
    }

    private void set4PrgBanks(final int firstBank, final int firstValue,
                              final boolean flashBank) {
        setPrgBank(firstBank, firstValue, flashBank);
        setPrgBank(firstBank + 1, firstValue + 1, flashBank);
        setPrgBank(firstBank + 2, firstValue + 2, flashBank);
        setPrgBank(firstBank + 3, firstValue + 3, flashBank);
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank,
                value & ((((~REG_CHR_mask & 0x1F) + 1) << 3) - 1));
    }

    private void updateChrBanks() {
        switch (REG_CHR_mode & 7) {
            default:
            case 0:
                set8ChrBanks(0, REG_CHR_bank_A & ~3);
                break;
            case 1:
                set4ChrBanks(0, ppu_mapper163_latch & ~2);
                set4ChrBanks(4, ppu_mapper163_latch & ~2);
                break;
            case 2:
                set2ChrBanks(0, REG_CHR_bank_A & ~1);
                TKSMIR[0] = TKSMIR[1] = REG_CHR_bank_A;
                set2ChrBanks(2, REG_CHR_bank_C & ~1);
                TKSMIR[2] = TKSMIR[3] = REG_CHR_bank_C;
                setChrBank(4, REG_CHR_bank_E);
                TKSMIR[4] = REG_CHR_bank_E;
                setChrBank(5, REG_CHR_bank_F);
                TKSMIR[5] = REG_CHR_bank_F;
                setChrBank(6, REG_CHR_bank_G);
                TKSMIR[6] = REG_CHR_bank_G;
                setChrBank(7, REG_CHR_bank_H);
                TKSMIR[7] = REG_CHR_bank_H;
                break;
            case 3:
                setChrBank(0, REG_CHR_bank_E);
                TKSMIR[0] = REG_CHR_bank_E;
                setChrBank(1, REG_CHR_bank_F);
                TKSMIR[1] = REG_CHR_bank_F;
                setChrBank(2, REG_CHR_bank_G);
                TKSMIR[2] = REG_CHR_bank_G;
                setChrBank(3, REG_CHR_bank_H);
                TKSMIR[3] = REG_CHR_bank_H;
                set2ChrBanks(4, REG_CHR_bank_A & ~1);
                TKSMIR[4] = TKSMIR[5] = REG_CHR_bank_A;
                set2ChrBanks(6, REG_CHR_bank_C & ~1);
                TKSMIR[6] = TKSMIR[7] = REG_CHR_bank_C;
                break;
            case 4:
                set4ChrBanks(0, REG_CHR_bank_A & ~2);
                set4ChrBanks(4, REG_CHR_bank_E & ~2);
                break;
            case 5:
                set4ChrBanks(0, (ppu_latch0 ? REG_CHR_bank_B : REG_CHR_bank_A) & ~2);
                set4ChrBanks(4, (ppu_latch1 ? REG_CHR_bank_F : REG_CHR_bank_E) & ~2);
                break;
            case 6:
                set2ChrBanks(0, REG_CHR_bank_A & ~1);
                set2ChrBanks(2, REG_CHR_bank_C & ~1);
                set2ChrBanks(4, REG_CHR_bank_E & ~1);
                set2ChrBanks(6, REG_CHR_bank_G & ~1);
                break;
            case 7:
                setChrBank(0, REG_CHR_bank_A);
                setChrBank(1, REG_CHR_bank_B);
                setChrBank(2, REG_CHR_bank_C);
                setChrBank(3, REG_CHR_bank_D);
                setChrBank(4, REG_CHR_bank_E);
                setChrBank(5, REG_CHR_bank_F);
                setChrBank(6, REG_CHR_bank_G);
                setChrBank(7, REG_CHR_bank_H);
                break;
        }
    }

    private void updateMirroring() {
        if (REG_four_screen) {
            setNametableMirroring(FOUR_SCREEN);
        } else if (!((REG_mapper == 20) && getBitBool(REG_flags, 0))) { // 189 check
            setNametableMirroring(REG_mirroring);
        }
    }

    private void updateState() {
        updatePrgBanks();
        updateChrBanks();
        updateMirroring();
    }

    @Override
    public int readMemory(final int address) {
        switch (address & 0xF000) {
            case 0x5000:
                return read5(address);
            case 0x6000:
            case 0x7000:
                return read67(address);
            case 0x8000:
            case 0x9000:
            case 0xA000:
            case 0xB000:
            case 0xC000:
            case 0xD000:
            case 0xE000:
            case 0xF000:
                return read8_F(address);
            default:
                return memory[address];
        }
    }

    private int read5(final int address) {
        switch (REG_mapper) {
            case 0:
                return 0;
            case 6: // 163
                if ((address & 0x0700) == 0x0100) {
                    return REG_R2 | REG_R0 | REG_R1 | ~REG_R3;
                } else if ((address & 0x700) == 0x0500) {
                    return (REG_R5 & 1) != 0 ? REG_R2 : REG_R1;
                }
                break;
            case 15: // MMC5
                if (address == 0x5204) {
                    final int result = (REG_scanline2_IRQ_pending ? 0x80 : 0x00)
                            | ((!LastPPUIsRendering || LastPPUScanline + 1 >= 241)
                            ? 0x00 : 0x40);
                    cpu.interrupt().setMapperIrq(false);
                    REG_scanline2_IRQ_pending = false;
                    return result;
                }
                break;
        }

        return 0xFF;
    }

    private void write5(final int address, final int value) {

        if (!REG_lockout) {
            // 342: COOLGIRL
            switch (address & 7) {
                case 0:
                    REG_PRG_base = (REG_PRG_base & 0xFF) | (value << 8);
                    break;
                case 1:
                    REG_PRG_base = (REG_PRG_base & 0xFF00) | value;
                    break;
                case 2:
                    REG_PRG_mask = value & 0xFF;
                    break;
                case 3:
                    REG_PRG_mode = (value & 0xE0) >> 5;
                    REG_CHR_bank_A = (REG_CHR_bank_A & 7) | (value << 3);
                    break;
                case 4:
                    REG_CHR_mode = (value & 0xE0) >> 5;
                    REG_CHR_mask = value & 0x1F;
                    break;
                case 5:
                    REG_PRG_bank_A = (REG_PRG_bank_A & 0xC1)
                            | ((value & 0x7C) >> 1);
                    REG_WRAM_page = value & 3;
                    break;
                case 6:
                    REG_flags = (value & 0xE0) >> 5;
                    REG_mapper = value & 0x1F;
                    break;
                case 7:
                    REG_lockout = getBitBool(value, 7);
                    REG_four_screen = getBitBool(value, 5);
                    REG_mirroring = (value & 0x18) >> 3;
                    REG_can_write_PRG = getBitBool(value, 2);
                    REG_can_write_CHR_RAM = getBitBool(value, 1);
                    REG_WRAM_enabled = getBitBool(value, 0);
                    break;
            }
            if (REG_mapper == 17) {
                // for MMC2
                REG_PRG_bank_B = 0xFD;
            } else if (REG_mapper == 14) {
                // for mapper #65f
                REG_PRG_bank_B = 1;
            }
        }

        switch (REG_mapper) {
            case 6: // 163
                if (address == 0x5101) {
                    if (REG_R4 != 0 && value == 0) {
                        REG_R5 ^= 1;
                    }
                    REG_R4 = value;
                } else if (address == 0x5100 && value == 6) {
                    REG_PRG_mode = REG_PRG_mode & 0xFE;
                    REG_PRG_bank_B = 12;
                } else {
                    switch ((address >> 8) & 3) {
                        case 2:
                            REG_PRG_mode |= 1;
                            REG_PRG_bank_A = (REG_PRG_bank_A & 0x3F) | ((value & 3) << 6);
                            REG_R0 = value;
                            break;
                        case 0:
                            REG_PRG_mode |= 1;
                            REG_PRG_bank_A = (REG_PRG_bank_A & 0xC3) | ((value & 0x0F) << 2);
                            REG_CHR_mode = (REG_CHR_mode & 0xFE) | (value >> 7);
                            REG_R1 = value;
                            break;
                        case 3:
                            REG_R2 = value;
                            break;
                        case 1:
                            REG_R3 = value;
                            break;
                    }
                }
                break;
            case 15: // 005: Nintendo MMC3
                switch (address) {
                    case 0x5105:
                        if (value == 0xFF) {
                            REG_four_screen = true;
                        } else {
                            REG_four_screen = false;
                            switch (((value >> 2) & 1) | ((value >> 3) & 2)) {
                                case 0:
                                    REG_mirroring = 2;
                                    break;
                                case 1:
                                    REG_mirroring = 0;
                                    break;
                                case 2:
                                    REG_mirroring = 1;
                                    break;
                                case 3:
                                    REG_mirroring = 3;
                                    break;
                            }
                        }
                        break;
                    case 0x5115:
                        REG_PRG_bank_A = value & ~1;
                        REG_PRG_bank_B = value | 1;
                        break;
                    case 0x5116:
                        REG_PRG_bank_C = value;
                        break;
                    case 0x5117:
                        REG_PRG_bank_D = value;
                        break;
                    case 0x5120:
                        REG_CHR_bank_A = value;
                        break;
                    case 0x5121:
                        REG_CHR_bank_B = value;
                        break;
                    case 0x5122:
                        REG_CHR_bank_C = value;
                        break;
                    case 0x5123:
                        REG_CHR_bank_D = value;
                        break;
                    case 0x5128:
                        REG_CHR_bank_E = value;
                        break;
                    case 0x5129:
                        REG_CHR_bank_F = value;
                        break;
                    case 0x512A:
                        REG_CHR_bank_G = value;
                        break;
                    case 0x512B:
                        REG_CHR_bank_H = value;
                        break;
                    case 0x5203:
                        cpu.interrupt().setMapperIrq(false);
                        REG_scanline2_IRQ_pending = false;
                        REG_scanline2_IRQ_line = value;
                        break;
                    case 0x5204:
                        cpu.interrupt().setMapperIrq(false);
                        REG_scanline2_IRQ_pending = false;
                        REG_scanline2_IRQ_enabled = getBitBool(value, 7);
                        break;
                }
                break;
            case 20: // 189: TXC 01-22018-400
                if ((REG_flags & 2) != 0) {
                    REG_PRG_bank_A = (REG_PRG_bank_A & 0xC3) | ((value & 0x0F) << 2)
                            | ((value & 0xF0) >> 2);
                }
                break;
        }

        updateState();
    }

    private int read67(final int address) {
        if (REG_map_ROM_on_6000) {
            return prgROM[(prgBanks[3] | (address & prgAddressMask))
                    & prgRomSizeMask];
        } else if (REG_WRAM_enabled) {
            return xram[(prgBanks[3] | (address & 0x1FFF)) & 0x7FFF];
        } else {
            return 0xFF;
        }
    }

    private int read8_F(final int address) {
        final int bank = address >> prgShift;
        if (flashBanks[bank]) {
            return SAVE_FLASH[(prgBanks[bank] | (address & prgAddressMask))
                    & save_flash_mask];
        } else {
            return prgROM[(prgBanks[bank] | (address & prgAddressMask))
                    & prgRomSizeMask];
        }
    }

    private void write67(final int address, final int value) {

        if (REG_mapper == 12) {
            // 87: Konami/Jaleco CHR-ROM switch
            REG_CHR_bank_A = (REG_CHR_bank_A & 0xE7) | ((value & 1) << 4)
                    | ((value & 2) << 2);
        } else if (REG_mapper == 20 && (REG_flags & 2) != 0) {
            // 189: TXC 01-22018-400
            REG_PRG_bank_A = (REG_PRG_bank_A & 0xC3) | ((value & 0x0F) << 2)
                    | ((value & 0xF0) >> 2);
        }

        if (!REG_map_ROM_on_6000 && REG_WRAM_enabled) {
            xram[(prgBanks[3] | (address & 0x1FFF)) & 0x7FFF] = value;
        }
    }

    private void writeFlash(final int address, final int value) {

        if (flash_state < 10) {
            flash_buffer_a[flash_state] = address & 0x0FFF;
            flash_buffer_v[flash_state] = value;
            ++flash_state;
            flash_state &= 0xFF;

            // sector erase
            if ((flash_state == 6)
                    && (flash_buffer_a[0] == 0x0AAA) && (flash_buffer_v[0] == 0xAA)
                    && (flash_buffer_a[1] == 0x0555) && (flash_buffer_v[1] == 0x55)
                    && (flash_buffer_a[2] == 0x0AAA) && (flash_buffer_v[2] == 0x80)
                    && (flash_buffer_a[3] == 0x0AAA) && (flash_buffer_v[3] == 0xAA)
                    && (flash_buffer_a[4] == 0x0555) && (flash_buffer_v[4] == 0x55)
                    && (flash_buffer_v[5] == 0x30)) {
                final int sector_address = REG_PRG_bank_A_mapped << 13;
                for (int i = sector_address; i < sector_address + 0x20000; ++i) {
                    SAVE_FLASH[i & save_flash_mask] = 0xFF;
                }
                flash_state = 0;
            }

            // writing byte
            if ((flash_state == 4)
                    && (flash_buffer_a[0] == 0x0AAA) && (flash_buffer_v[0] == 0xAA)
                    && (flash_buffer_a[1] == 0x0555) && (flash_buffer_v[1] == 0x55)
                    && (flash_buffer_a[2] == 0x0AAA) && (flash_buffer_v[2] == 0xA0)) {
                final int sector_address = REG_PRG_bank_A_mapped << 13;
                final int flash_addr = sector_address + (address & 0x7FFF);
                SAVE_FLASH[flash_addr & save_flash_mask] = value;
                flash_state = 0;
            }
        }
        if (value == 0xF0) {
            flash_state = 0;
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {

        if (REG_can_write_PRG) {
            writeFlash(address, value);
        }

        switch (REG_mapper) {
            case 1: // 002: Nintendo UNROM/UOROM
                if ((REG_flags & 1) == 0 || (address & 0xF000) != 0x9000) {
                    REG_PRG_bank_A = (REG_PRG_bank_A & 0xC1) | ((value & 0x1F) << 1);
                } else {
                    REG_mirroring = 2 + ((value >> 4) & 1);
                }
                break;
            case 2: // 003: Nintendo CNROM
                REG_CHR_bank_A = (REG_CHR_bank_A & 7) | (value << 3);
                break;
            case 3: // 078: Irem Holy Diver
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xF1) | ((value & 7) << 1);
                REG_CHR_bank_A = (REG_CHR_bank_A & 0x87) | ((value & 0xF0) >> 1);
                REG_mirroring = ((value >> 3) & 1) ^ 1;
                break;
            case 4: // 097: Irem TAM-S1
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xE1) | ((value & 0x0F) << 1);
                REG_mirroring = (value >> 6) ^ ((value >> 6) & 2);
                break;
            case 5: // 093: Sunsoft-2 
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xF1) | ((value & 0x70) >> 3);
                REG_can_write_CHR_RAM = getBitBool(value, 0);
                break;
            case 7: // 018: Jaleco
                switch (((address >> 10) & 0x1C) | (address & 3)) {
                    case 0:
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0xF0) | (value & 0x0F);
                        break;
                    case 1:
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 2:
                        REG_PRG_bank_B = (REG_PRG_bank_B & 0xF0) | (value & 0x0F);
                        break;
                    case 3:
                        REG_PRG_bank_B = (REG_PRG_bank_B & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 4:
                        REG_PRG_bank_C = (REG_PRG_bank_C & 0xF0) | (value & 0x0F);
                        break;
                    case 5:
                        REG_PRG_bank_C = (REG_PRG_bank_C & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        REG_CHR_bank_A = (REG_CHR_bank_A & 0xF0) | (value & 0x0F);
                        break;
                    case 9:
                        REG_CHR_bank_A = (REG_CHR_bank_A & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 10:
                        REG_CHR_bank_B = (REG_CHR_bank_B & 0xF0) | (value & 0x0F);
                        break;
                    case 11:
                        REG_CHR_bank_B = (REG_CHR_bank_B & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 12:
                        REG_CHR_bank_C = (REG_CHR_bank_C & 0xF0) | (value & 0x0F);
                        break;
                    case 13:
                        REG_CHR_bank_C = (REG_CHR_bank_C & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 14:
                        REG_CHR_bank_D = (REG_CHR_bank_D & 0xF0) | (value & 0x0F);
                        break;
                    case 15:
                        REG_CHR_bank_D = (REG_CHR_bank_D & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 16:
                        REG_CHR_bank_E = (REG_CHR_bank_E & 0xF0) | (value & 0x0F);
                        break;
                    case 17:
                        REG_CHR_bank_E = (REG_CHR_bank_E & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 18:
                        REG_CHR_bank_F = (REG_CHR_bank_F & 0xF0) | (value & 0x0F);
                        break;
                    case 19:
                        REG_CHR_bank_F = (REG_CHR_bank_F & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 20:
                        REG_CHR_bank_G = (REG_CHR_bank_G & 0xF0) | (value & 0x0F);
                        break;
                    case 21:
                        REG_CHR_bank_G = (REG_CHR_bank_G & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 22:
                        REG_CHR_bank_H = (REG_CHR_bank_H & 0xF0) | (value & 0x0F);
                        break;
                    case 23:
                        REG_CHR_bank_H = (REG_CHR_bank_H & 0x0F) | ((value & 0x0F) << 4);
                        break;
                    case 24:
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xFFF0) | (value & 0x0F);
                        break;
                    case 25:
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xFF0F)
                                | ((value & 0x0F) << 4);
                        break;
                    case 26:
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xF0FF)
                                | ((value & 0x0F) << 8);
                        break;
                    case 27:
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0x0FFF)
                                | ((value & 0x0F) << 12);
                        break;
                    case 28:
                        cpu.interrupt().setMapperIrq(false);
                        REG_CPU_IRQ_value = REG_CPU_IRQ_latch;
                        break;
                    case 29:
                        cpu.interrupt().setMapperIrq(false);
                        REG_CPU_IRQ_control = value & 0x0F;
                        break;
                    case 30:
                        REG_mirroring = value ^ (((value >> 1) & 1) ^ 1);
                        break;
                    case 31: // sound
                        break;
                }
                break;
            case 8: // 007: Nintendo AMROM/ANROM/AOROM; 034/241: Nintendo BNROM
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xC3) | ((value & 0xF) << 2);
                if ((REG_flags & 1) == 0) {
                    REG_mirroring = 2 + ((value >> 4) & 1);
                }
                break;
            case 9: // 228: Cheetahmen II
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xC3) | ((address & 0x780) >> 5);
                REG_CHR_bank_A = (REG_CHR_bank_A & 7) | ((address & 7) << 5)
                        | ((value & 3) << 3);
                REG_mirroring = (address >> 13) & 1;
                break;
            case 10: // 011: ColorDreams
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xF3) | ((value & 3) << 2);
                REG_CHR_bank_A = (REG_CHR_bank_A & 0x87) | ((value & 0xF0) >> 1);
                break;
            case 11: // 066: Nintendo GNROM/MHROM
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xF3) | ((value & 0x30) >> 2);
                REG_CHR_bank_A = (REG_CHR_bank_A & 0xE7) | ((value & 3) << 3);
                break;
            case 13: // 090: JY
                switch ((address >> 12) & 7) {
                    case 0: // $800x
                        switch (address & 3) {
                            case 0: // $8000
                                REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                                break;
                            case 1: // $8001
                                REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                                break;
                            case 2: // $8002
                                REG_PRG_bank_C = (REG_PRG_bank_C & 0xC0) | (value & 0x3F);
                                break;
                            case 3: // $8003
                                REG_PRG_bank_D = (REG_PRG_bank_D & 0xC0) | (value & 0x3F);
                                break;
                        }
                        break;
                    case 1: // $900x
                        switch (address & 7) {
                            case 0: // $9000
                                REG_CHR_bank_A = value;
                                break;
                            case 1: // $9001
                                REG_CHR_bank_B = value;
                                break;
                            case 2: // $9002
                                REG_CHR_bank_C = value;
                                break;
                            case 3: // $9003
                                REG_CHR_bank_D = value;
                                break;
                            case 4: // $9004
                                REG_CHR_bank_E = value;
                                break;
                            case 5: // $9005
                                REG_CHR_bank_F = value;
                                break;
                            case 6: // $9006
                                REG_CHR_bank_G = value;
                                break;
                            case 7: // $9007
                                REG_CHR_bank_H = value;
                                break;
                        }
                        break;
                    case 4: // $C00x
                        switch (address & 7) {
                            case 0:
                                if ((value & 1) != 0) {
                                    REG_scanline_IRQ_enabled = true;
                                } else {
                                    REG_scanline_IRQ_enabled = false;
                                    cpu.interrupt().setMapperIrq(false);
                                }
                                break;
                            case 2:
                                REG_scanline_IRQ_enabled = false;
                                cpu.interrupt().setMapperIrq(false);
                                break;
                            case 3:
                                REG_scanline_IRQ_enabled = true;
                                break;
                            case 5:
                                REG_scanline_IRQ_latch = value ^ REG_R0;
                                REG_scanline_IRQ_reload = true;
                                break;
                            case 6:
                                REG_R0 = value;
                                break;
                        }
                    case 5: // $D00x
                        if ((address & 3) == 1) {
                            // $D001
                            REG_mirroring = value & 3;
                        }
                        break;
                }
                break;
            case 14: // 065: Irem H3001
                switch (((address >> 9) & 0x38) | (address & 7)) {
                    case 0:
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                        break;
                    case 9:
                        REG_mirroring = (value >> 7) & 1;
                        break;
                    case 11:
                        REG_CPU_IRQ_control = (REG_CPU_IRQ_control & 0xFE)
                                | ((value >> 7) & 1);
                        cpu.interrupt().setMapperIrq(false);
                        break;
                    case 12:
                        REG_CPU_IRQ_value = (REG_R0 << 8) | REG_R1;
                        cpu.interrupt().setMapperIrq(false);
                        break;
                    case 13:
                        REG_R0 = value;
                        break;
                    case 14:
                        REG_R1 = value;
                        break;
                    case 16:
                        REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                        break;
                    case 24:
                        REG_CHR_bank_A = value;
                        break;
                    case 25:
                        REG_CHR_bank_B = value;
                        break;
                    case 26:
                        REG_CHR_bank_C = value;
                        break;
                    case 27:
                        REG_CHR_bank_D = value;
                        break;
                    case 28:
                        REG_CHR_bank_E = value;
                        break;
                    case 29:
                        REG_CHR_bank_F = value;
                        break;
                    case 30:
                        REG_CHR_bank_G = value;
                        break;
                    case 31:
                        REG_CHR_bank_H = value;
                        break;
                    case 32:
                        REG_PRG_bank_C = (REG_PRG_bank_C & 0xC0) | (value & 0x3F);
                        break;
                }
                break;
            case 16: // 001: Nintendo SxROM
                // r0 - load register, flag0 - 16KB of WRAM (SOROM)
                if ((value & 0x80) != 0) {
                    // reset
                    REG_R0 = (REG_R0 & 0xC0) | 0x20;
                    REG_PRG_mode = 0;
                    REG_PRG_bank_C = (REG_PRG_bank_C & 0xE0) | 0x1E;
                } else {
                    REG_R0 = (REG_R0 & 0xC0) | ((value & 1) << 5)
                            | ((REG_R0 & 0x3E) >> 1);
                    if ((REG_R0 & 1) != 0) {
                        switch ((address >> 13) & 3) {
                            case 0: // $8000-$9FFF
                                switch (REG_R0 & 0x18) {
                                    case 0x18:
                                        REG_PRG_mode = 0;
                                        REG_PRG_bank_C = (REG_PRG_bank_C & 0xE0) | 0x1E;
                                        break;
                                    case 0x10:
                                        REG_PRG_mode = 1;
                                        REG_PRG_bank_C = (REG_PRG_bank_C & 0xE0);
                                        break;
                                    default:
                                        REG_PRG_mode = 7;
                                        break;
                                }
                                if (((REG_R0 >> 5) & 1) != 0) {
                                    REG_CHR_mode = 4;
                                } else {
                                    REG_CHR_mode = 0;
                                }
                                REG_mirroring = ((REG_R0 >> 1) & 3) ^ 2;
                                break;
                            case 1: // $A000-$BFFF
                                REG_CHR_bank_A = (REG_CHR_bank_A & 0x83)
                                        | ((REG_R0 & 0x3E) << 1);
                                REG_PRG_bank_A = (REG_PRG_bank_A & 0xDF) | (REG_R0 & 0x20);
                                REG_PRG_bank_C = (REG_PRG_bank_C & 0xDF) | (REG_R0 & 0x20);
                                break;
                            case 2: // $C000-$DFFF
                                REG_CHR_bank_E = (REG_CHR_bank_E & 0x83)
                                        | ((REG_R0 & 0x3E) << 1);
                                break;
                            case 3: // $E000-$FFFF
                                REG_PRG_bank_A = (REG_PRG_bank_A & 0xE1) | (REG_R0 & 0x1E);
                                REG_WRAM_enabled = !getBitBool(REG_R0, 5);
                                break;
                        }
                        REG_R0 = (REG_R0 & 0xC0) | 0x20;
                        if ((REG_flags & 1) != 0) { // (flags[0]) - 16KB of WRAM
                            if ((REG_CHR_mode & 4) != 0) {
                                REG_WRAM_page = 2 | (REG_CHR_bank_A >> 6) ^ 1;
                            } else {
                                REG_WRAM_page = 2 | (REG_CHR_bank_A >> 5) ^ 1;
                            }
                        }
                    }
                }
                break;
            case 17: // 009/010: Nintendo PNROM/FJROM/FKROM. flag0 - 0=MMC2, 1=MMC4
                switch ((address >> 12) & 7) {
                    case 2: // $A000-$AFFF
                        if ((REG_flags & 1) == 0) {
                            // MMC2
                            REG_PRG_bank_A = (REG_PRG_bank_A & 0xF0) | (value & 0x0F);
                        } else {
                            // MMC4
                            REG_PRG_bank_A = (REG_PRG_bank_A & 0xE1)
                                    | ((value & 0x0F) << 1);
                        }
                        break;
                    case 3: // $B000-$BFFF
                        REG_CHR_bank_A = (REG_CHR_bank_A & 0x83) | ((value & 0x1F) << 2);
                        break;
                    case 4: // $C000-$CFFF
                        REG_CHR_bank_B = (REG_CHR_bank_B & 0x83) | ((value & 0x1F) << 2);
                        break;
                    case 5: // $D000-$DFFF
                        REG_CHR_bank_E = (REG_CHR_bank_E & 0x83) | ((value & 0x1F) << 2);
                        break;
                    case 6: // $E000-$EFFF
                        REG_CHR_bank_F = (REG_CHR_bank_F & 0x83) | ((value & 0x1F) << 2);
                        break;
                    case 7: // $F000-$FFFF
                        REG_mirroring = value & 1;
                        break;
                }
                break;
            case 18: // 152
                REG_CHR_bank_A = (REG_CHR_bank_A & 0x87) | ((value & 0x0F) << 3);
                REG_PRG_bank_A = (REG_PRG_bank_A & 0xF1) | ((value & 0x70) >> 3);
                REG_mirroring = 2 | (value >> 7);
                break;
            case 19: // 073: VRC3
                switch ((address >> 12) & 7) {
                    case 0: // $8000-$8FFF
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xFFF0)
                                | (value & 0x0F);
                        break;
                    case 1: // $9000-$9FFF
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xFF0F)
                                | ((value & 0x0F) << 4);
                        break;
                    case 2: // $A000-$AFFF
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xF0FF)
                                | ((value & 0x0F) << 8);
                        break;
                    case 3: // $B000-$BFFF
                        REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0x0FFF)
                                | ((value & 0x0F) << 12);
                        break;
                    case 4: // $C000-$CFFF
                        cpu.interrupt().setMapperIrq(false);
                        REG_CPU_IRQ_control = (REG_CPU_IRQ_control & 0xF8) | (value & 7);
                        if ((REG_CPU_IRQ_control & 2) != 0) {
                            REG_CPU_IRQ_value = REG_CPU_IRQ_latch;
                        }
                        break;
                    case 5: // $D000-$DFFF
                        cpu.interrupt().setMapperIrq(false);
                        REG_CPU_IRQ_control = (REG_CPU_IRQ_control & 0xFD)
                                | (REG_CPU_IRQ_control & 1) << 1;
                        break;
                    case 7: // $F000-$FFFF
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0xF1) | ((value & 7) << 1);
                        break;
                }
                break;
            case 20: // 004/118/189: TxROM/TxSROM/TXC 01-22018-400
                // r0[2:0] - internal register
                // flag0 - TxSROM, flag1 - mapper #189
                switch (((address >> 12) & 6) | (address & 1)) {
                    case 0: // $8000-$9FFE, even
                        REG_R0 = (REG_R0 & 0xF8) | (value & 7);
                        if ((REG_flags & 2) == 0) {
                            REG_PRG_mode = getBitBool(value, 6) ? 5 : 4;
                        }
                        REG_CHR_mode = getBitBool(value, 7) ? 3 : 2;
                        break;
                    case 1: // $8001-$9FFF, odd
                        switch (REG_R0 & 7) {
                            case 0:
                                REG_CHR_bank_A = value;
                                break;
                            case 1:
                                REG_CHR_bank_C = value;
                                break;
                            case 2:
                                REG_CHR_bank_E = value;
                                break;
                            case 3:
                                REG_CHR_bank_F = value;
                                break;
                            case 4:
                                REG_CHR_bank_G = value;
                                break;
                            case 5:
                                REG_CHR_bank_H = value;
                                break;
                            case 6:
                                if ((REG_flags & 2) == 0) {
                                    REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                                }
                                break;
                            case 7:
                                if ((REG_flags & 2) == 0) {
                                    REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                                }
                                break;
                        }
                        break;
                    case 2: // $A000-$BFFE, even (mirroring)
                        REG_mirroring = value & 1;
                        break;
                    case 4: // $C000-$DFFE, even (IRQ latch)
                        REG_scanline_IRQ_latch = value;
                        break;
                    case 5: // $C001-$DFFF, odd
                        REG_scanline_IRQ_reload = true;
                        break;
                    case 6: // $E000-$FFFE, even
                        REG_scanline_IRQ_enabled = false;
                        cpu.interrupt().setMapperIrq(false);
                        break;
                    case 7: // $E001-$FFFF, odd
                        REG_scanline_IRQ_enabled = true;
                        break;
                }
                break;
            case 21: // 112: NTDEC rewired N108 clone (r0[2:0] - internal register)
                switch ((address >> 13) & 3) {
                    case 0:
                        REG_R0 = (REG_R0 & 0xF8) | (value & 7);
                        break;
                    case 1:
                        switch (REG_R0 & 7) {
                            case 0:
                                REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                                break;
                            case 1:
                                REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                                break;
                            case 2:
                                REG_CHR_bank_A = value;
                                break;
                            case 3:
                                REG_CHR_bank_C = value;
                                break;
                            case 4:
                                REG_CHR_bank_E = value;
                                break;
                            case 5:
                                REG_CHR_bank_F = value;
                                break;
                            case 6:
                                REG_CHR_bank_G = value;
                                break;
                            case 7:
                                REG_CHR_bank_H = value;
                                break;
                        }
                        break;
                    case 3:
                        REG_mirroring = value & 1;
                        break;
                }
                break;
            case 22: // 033/048: Taito (flag0=0 - #33, flag0=1 - #48)
                switch (((address >> 11) & 0xC) | (address & 3)) {
                    case 0:
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                        if ((REG_flags & 1) == 0) {
                            REG_mirroring = (value >> 6) & 1;
                        }
                        break;
                    case 1:
                        REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                        break;
                    case 2:
                        REG_CHR_bank_A = value << 1;
                        break;
                    case 3:
                        REG_CHR_bank_C = value << 1;
                        break;
                    case 4:
                        REG_CHR_bank_E = value;
                        break;
                    case 5:
                        REG_CHR_bank_F = value;
                        break;
                    case 6:
                        REG_CHR_bank_G = value;
                        break;
                    case 7:
                        REG_CHR_bank_H = value;
                        break;
                    case 12:
                        if ((REG_flags & 1) != 0) {
                            REG_mirroring = (value >> 6) & 1;
                        }
                    case 8:
                        REG_scanline_IRQ_latch = value;
                        break;
                    case 9:
                        REG_scanline_IRQ_reload = true;
                        break;
                    case 10:
                        REG_scanline_IRQ_enabled = true;
                        break;
                    case 11:
                        REG_scanline_IRQ_enabled = false;
                        cpu.interrupt().setMapperIrq(false);
                        break;
                }
                break;
            case 24: { // 021/022/023/025: Konami VRC2/4
                //	flag0 - switches A0 and A1 lines
                //   0 = A0,A1 like VRC2b (mapper #23)
                //   1 = A1,A0 like VRC2a(#22), VRC2c(#25)
                // flag1 - divides CHR bank select by two (mapper #22, VRC2a)
                final int vrc_2b_hi = ((address >> 1) & 1) | ((address >> 3) & 1)
                        | ((address >> 5) & 1) | ((address >> 7) & 1);
                final int vrc_2b_low = (address & 1) | ((address >> 2) & 1)
                        | ((address >> 4) & 1) | ((address >> 6) & 1);
                switch (((address >> 10) & 0x1C)
                        | (((REG_flags & 1) != 0 ? vrc_2b_low : vrc_2b_hi) << 1)
                        | ((REG_flags & 1) != 0 ? vrc_2b_hi : vrc_2b_low)) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0xE0) | (value & 0x1F);
                        break;
                    case 4:
                    case 5:
                        if (value != 0xFF) {
                            // $FF check: prevent Wai Wai World (VRC2) from using VRC4's 
                            // one-screen mirroring
                            REG_mirroring = value & 3;
                        }
                        break;
                    case 6:
                    case 7:
                        REG_PRG_mode = (REG_PRG_mode & 0xFE) | ((value >> 1) & 1);
                        break;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                        REG_PRG_bank_B = (REG_PRG_bank_B & 0xE0) | (value & 0x1F);
                        break;
                }
                if ((REG_flags & 2) == 0) {
                    switch (((address >> 10) & 0x1C)
                            | (((REG_flags & 1) != 0 ? vrc_2b_low : vrc_2b_hi) << 1)
                            | ((REG_flags & 1) != 0 ? vrc_2b_hi : vrc_2b_low)) {
                        case 12:
                            REG_CHR_bank_A = (REG_CHR_bank_A & 0xF0) | (value & 0x0F);
                            break;
                        case 13:
                            REG_CHR_bank_A = (REG_CHR_bank_A & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 14:
                            REG_CHR_bank_B = (REG_CHR_bank_B & 0xF0) | (value & 0x0F);
                            break;
                        case 15:
                            REG_CHR_bank_B = (REG_CHR_bank_B & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 16:
                            REG_CHR_bank_C = (REG_CHR_bank_C & 0xF0) | (value & 0x0F);
                            break;
                        case 17:
                            REG_CHR_bank_C = (REG_CHR_bank_C & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 18:
                            REG_CHR_bank_D = (REG_CHR_bank_D & 0xF0) | (value & 0x0F);
                            break;
                        case 19:
                            REG_CHR_bank_D = (REG_CHR_bank_D & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 20:
                            REG_CHR_bank_E = (REG_CHR_bank_E & 0xF0) | (value & 0x0F);
                            break;
                        case 21:
                            REG_CHR_bank_E = (REG_CHR_bank_E & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 22:
                            REG_CHR_bank_F = (REG_CHR_bank_F & 0xF0) | (value & 0x0F);
                            break;
                        case 23:
                            REG_CHR_bank_F = (REG_CHR_bank_F & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 24:
                            REG_CHR_bank_G = (REG_CHR_bank_G & 0xF0) | (value & 0x0F);
                            break;
                        case 25:
                            REG_CHR_bank_G = (REG_CHR_bank_G & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 26:
                            REG_CHR_bank_H = (REG_CHR_bank_H & 0xF0) | (value & 0x0F);
                            break;
                        case 27:
                            REG_CHR_bank_H = (REG_CHR_bank_H & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                    }
                } else {
                    switch (((address >> 10) & 0x1C)
                            | (((REG_flags & 1) != 0 ? vrc_2b_low : vrc_2b_hi) << 1)
                            | ((REG_flags & 1) != 0 ? vrc_2b_hi : vrc_2b_low)) {
                        case 12:
                            REG_CHR_bank_A = (REG_CHR_bank_A & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 13:
                            REG_CHR_bank_A = (REG_CHR_bank_A & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 14:
                            REG_CHR_bank_B = (REG_CHR_bank_B & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 15:
                            REG_CHR_bank_B = (REG_CHR_bank_B & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 16:
                            REG_CHR_bank_C = (REG_CHR_bank_C & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 17:
                            REG_CHR_bank_C = (REG_CHR_bank_C & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 18:
                            REG_CHR_bank_D = (REG_CHR_bank_D & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 19:
                            REG_CHR_bank_D = (REG_CHR_bank_D & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 20:
                            REG_CHR_bank_E = (REG_CHR_bank_E & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 21:
                            REG_CHR_bank_E = (REG_CHR_bank_E & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 22:
                            REG_CHR_bank_F = (REG_CHR_bank_F & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 23:
                            REG_CHR_bank_F = (REG_CHR_bank_F & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 24:
                            REG_CHR_bank_G = (REG_CHR_bank_G & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 25:
                            REG_CHR_bank_G = (REG_CHR_bank_G & 0x07) | ((value & 0x0F) << 3);
                            break;
                        case 26:
                            REG_CHR_bank_H = (REG_CHR_bank_H & 0x78) | ((value & 0x0E) >> 1);
                            break;
                        case 27:
                            REG_CHR_bank_H = (REG_CHR_bank_H & 0x07) | ((value & 0x0F) << 3);
                            break;
                    }
                }
                if (((address >> 12) & 7) == 7) {
                    switch ((((REG_flags & 1) != 0 ? vrc_2b_low : vrc_2b_hi) << 1)
                            | ((REG_flags & 1) != 0 ? vrc_2b_hi : vrc_2b_low)) {
                        case 0:
                            REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0xF0) | (value & 0x0F);
                            break;
                        case 1:
                            REG_CPU_IRQ_latch = (REG_CPU_IRQ_latch & 0x0F)
                                    | ((value & 0x0F) << 4);
                            break;
                        case 2:
                            cpu.interrupt().setMapperIrq(false);
                            REG_CPU_IRQ_control = (REG_CPU_IRQ_control & 0xF8)
                                    | (value & 7);
                            if ((REG_CPU_IRQ_control & 2) != 0) {
                                REG_VRC4_IRQ_prescaler_counter = 0;
                                REG_VRC4_IRQ_prescaler = 0;
                                REG_CPU_IRQ_value = REG_CPU_IRQ_latch;
                            }
                            break;
                        case 3:
                            cpu.interrupt().setMapperIrq(false);
                            REG_CPU_IRQ_control = (REG_CPU_IRQ_control & 0xFD)
                                    | (REG_CPU_IRQ_control & 1) << 1;
                            break;
                    }
                }
                break;
            }
            case 25: // 069: Sunsoft FME-7 (r0 - command register)
                if (((address >> 13) & 3) == 0) {
                    REG_R0 = (REG_R0 & 0xF0) | (value & 0x0F);
                } else if (((address >> 13) & 3) == 1) {
                    switch (REG_R0 & 0x0F) {
                        case 0:
                            REG_CHR_bank_A = value;
                            break;
                        case 1:
                            REG_CHR_bank_B = value;
                            break;
                        case 2:
                            REG_CHR_bank_C = value;
                            break;
                        case 3:
                            REG_CHR_bank_D = value;
                            break;
                        case 4:
                            REG_CHR_bank_E = value;
                            break;
                        case 5:
                            REG_CHR_bank_F = value;
                            break;
                        case 6:
                            REG_CHR_bank_G = value;
                            break;
                        case 7:
                            REG_CHR_bank_H = value;
                            break;
                        case 8:
                            REG_WRAM_enabled = getBitBool(value, 7);
                            REG_map_ROM_on_6000 = !getBitBool(value, 6);
                            REG_PRG_bank_6000 = value & 0x3F;
                            break;
                        case 9:
                            REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                            break;
                        case 10:
                            REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                            break;
                        case 11:
                            REG_PRG_bank_C = (REG_PRG_bank_C & 0xC0) | (value & 0x3F);
                            break;
                        case 12:
                            REG_mirroring = value & 3;
                            break;
                        case 13:
                            REG_CPU_IRQ_control = ((value >> 6) & 1) | (value & 1);
                            cpu.interrupt().setMapperIrq(false);
                            break;
                        case 14:
                            REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0xFF00) | value;
                            break;
                        case 15:
                            REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0x00FF) | (value << 8);
                            break;
                    }
                }
                break;
            case 26: // 032: Irem G-101
                switch ((address >> 12) & 3) {
                    case 0:
                        REG_PRG_bank_A = (REG_PRG_bank_A & 0xC0) | (value & 0x3F);
                        break;
                    case 1:
                        REG_PRG_mode = (REG_PRG_mode & 6) | ((value >> 1) & 1);
                        REG_mirroring = value & 1;
                        break;
                    case 2:
                        REG_PRG_bank_B = (REG_PRG_bank_B & 0xC0) | (value & 0x3F);
                        break;
                    case 3:
                        switch (address & 7) {
                            case 0:
                                REG_CHR_bank_A = value;
                                break;
                            case 1:
                                REG_CHR_bank_B = value;
                                break;
                            case 2:
                                REG_CHR_bank_C = value;
                                break;
                            case 3:
                                REG_CHR_bank_D = value;
                                break;
                            case 4:
                                REG_CHR_bank_E = value;
                                break;
                            case 5:
                                REG_CHR_bank_F = value;
                                break;
                            case 6:
                                REG_CHR_bank_G = value;
                                break;
                            case 7:
                                REG_CHR_bank_H = value;
                                break;
                        }
                        break;
                }
                break;
            case 31: // temp/test
                REG_PRG_bank_6000 = ((value >> 1) & 0xF) + 4;
                REG_map_ROM_on_6000 = true;
                break;
        }

        updateState();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xF000) {
            case 0x0000:
            case 0x1000:
            case 0x2000:
            case 0x3000:
            case 0x4000:
                break;
            case 0x5000:
                write5(address, value);
                break;
            case 0x6000:
            case 0x7000:
                write67(address, value);
                break;
            default:
                writeRegister(address, value);
                break;
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            if (REG_can_write_CHR_RAM) {
                xChrRam[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                        & chrRamSizeMask] = value;
            }
        } else {
            vram[address] = value;
        }
    }

    @Override
    public void update() {
        switch (REG_mapper) {
            case 7: // Mapper #18
                if ((REG_CPU_IRQ_control & 1) != 0) {
                    if ((REG_CPU_IRQ_control & 8) != 0) {
                        if ((REG_CPU_IRQ_value & 0x000F) == 0) {
                            cpu.interrupt().setMapperIrq(true);
                        }
                        REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0xFFF0)
                                | ((REG_CPU_IRQ_value - 1) & 0x000F);
                    } else if ((REG_CPU_IRQ_control & 4) != 0) {
                        if ((REG_CPU_IRQ_value & 0x00FF) == 0) {
                            cpu.interrupt().setMapperIrq(true);
                        }
                        REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0xFF00)
                                | ((REG_CPU_IRQ_value - 1) & 0x00FF);
                    } else if ((REG_CPU_IRQ_control & 2) != 0) {
                        if ((REG_CPU_IRQ_value & 0x0FFF) == 0) {
                            cpu.interrupt().setMapperIrq(true);
                        }
                        REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0xF000)
                                | ((REG_CPU_IRQ_value - 1) & 0x0FFF);
                    } else {
                        if (REG_CPU_IRQ_value == 0) {
                            cpu.interrupt().setMapperIrq(true);
                        }
                        --REG_CPU_IRQ_value;
                        REG_CPU_IRQ_value &= 0xFFFF;
                    }
                }
                break;
            case 14: // Mapper #65 - Irem's H3001
                if ((REG_CPU_IRQ_control & 1) != 0 && REG_CPU_IRQ_value > 0) {
                    --REG_CPU_IRQ_value;
                    REG_CPU_IRQ_value &= 0xFFFF;
                    if (REG_CPU_IRQ_value == 0) {
                        cpu.interrupt().setMapperIrq(true);
                    }
                }
                break;
            case 19: // Mapper #73 - VRC3
                if ((REG_CPU_IRQ_control & 2) != 0) {
                    if ((REG_CPU_IRQ_control & 4) != 0) {  // 8-bit mode
                        REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0xFF00)
                                | ((REG_CPU_IRQ_value + 1) & 0x00FF);
                        if ((REG_CPU_IRQ_value & 0xFF) == 0) {
                            cpu.interrupt().setMapperIrq(true);
                            REG_CPU_IRQ_value = (REG_CPU_IRQ_value & 0xFF00)
                                    | (REG_CPU_IRQ_latch & 0x00FF);
                        }
                    } else {
                        ++REG_CPU_IRQ_value;
                        REG_CPU_IRQ_value &= 0xFFFF;
                        if (REG_CPU_IRQ_value == 0) {
                            cpu.interrupt().setMapperIrq(true);
                            REG_CPU_IRQ_value = REG_CPU_IRQ_latch;
                        }
                    }
                }
                break;
            case 24: // Mapper #24 - VRC4
                if ((REG_CPU_IRQ_control & 2) != 0) {
                    if ((REG_CPU_IRQ_control & 4) != 0) {
                        ++REG_CPU_IRQ_value;
                        REG_CPU_IRQ_value &= 0xFFFF;
                        if ((REG_CPU_IRQ_value & 0x00FF) == 0) {
                            cpu.interrupt().setMapperIrq(true);
                            REG_CPU_IRQ_value = REG_CPU_IRQ_latch;
                        }
                    } else {
                        ++REG_VRC4_IRQ_prescaler;
                        REG_VRC4_IRQ_prescaler &= 0xFF;
                        if (((REG_VRC4_IRQ_prescaler_counter & 2) == 0
                                && REG_VRC4_IRQ_prescaler == 114)
                                || ((REG_VRC4_IRQ_prescaler_counter & 2) != 0
                                && REG_VRC4_IRQ_prescaler == 113)) {
                            ++REG_CPU_IRQ_value;
                            REG_CPU_IRQ_value &= 0xFFFF;
                            REG_VRC4_IRQ_prescaler = 0;
                            REG_VRC4_IRQ_prescaler_counter++;
                            if (REG_VRC4_IRQ_prescaler_counter == 3) {
                                REG_VRC4_IRQ_prescaler_counter = 0;
                            }
                            if ((REG_CPU_IRQ_value & 0x00FF) == 0) {
                                cpu.interrupt().setMapperIrq(true);
                                REG_CPU_IRQ_value = REG_CPU_IRQ_latch;
                            }
                        }
                    }
                }
                break;
            case 25: // Mapper #69 - Sunsoft FME-7
                if (REG_CPU_IRQ_value == 0 && (REG_CPU_IRQ_control & 1) != 0) {
                    cpu.interrupt().setMapperIrq(true);
                }
                --REG_CPU_IRQ_value;
                REG_CPU_IRQ_value &= 0xFFFF;
                break;
        }
    }

    @Override
    public void handlePpuCycle(final int scanline,
                               final int scanlineCycle, final int address, final boolean rendering) {

        LastPPUScanline = scanline;
        LastPPUIsRendering = rendering;

        if (rendering && scanlineCycle == 260) { // Scanline counter
            if (REG_scanline_IRQ_reload || REG_scanline_IRQ_counter == 0) {
                REG_scanline_IRQ_counter = REG_scanline_IRQ_latch;
                REG_scanline_IRQ_reload = false;
            } else {
                --REG_scanline_IRQ_counter;
                REG_scanline_IRQ_counter &= 0xFF;
            }
            if (REG_scanline_IRQ_counter == 0 && REG_scanline_IRQ_enabled) {
                cpu.interrupt().setMapperIrq(true);
            }

            // for MMC5
            if (REG_scanline2_IRQ_line == scanline + 1 && REG_scanline2_IRQ_enabled) {
                cpu.interrupt().setMapperIrq(true);
                REG_scanline2_IRQ_pending = true;
            }

            // for mapper #163
            if (REG_mapper == 6) {
                if (scanline == 239) {
                    ppu_mapper163_latch = 0;
                    updateChrBanks();
                } else if (scanline == 127) {
                    ppu_mapper163_latch = 1;
                    updateChrBanks();
                }
            }
        }

        if (REG_mapper == 20 && (REG_flags & 1) != 0) {
            // TKSROM/TLSROM
            setNametableMirroring(ONE_SCREEN_A
                    + ((TKSMIR[(address & 0x1FFF) >> 10] >> 7) & 1));
        } else if (REG_mapper == 17) {
            // Mapper #9 and #10 - MMC2 and MMC4
            switch (address >> 4) {
                case 0x00FD:
                    ppu_latch0 = false;
                    updateChrBanks();
                    break;
                case 0x00FE:
                    ppu_latch0 = true;
                    updateChrBanks();
                    break;
                case 0x01FD:
                    ppu_latch1 = false;
                    updateChrBanks();
                    break;
                case 0x01FE:
                    ppu_latch1 = true;
                    updateChrBanks();
                    break;
            }
        }
    }

    private void loadFlash() {
        if (SAVE_FLASH == null || SAVE_FLASH.length != save_flash_size) {
            SAVE_FLASH = new int[save_flash_size];
        }
        synchronized (GamePrefs.class) {
            final int[] flash = GamePrefs.getInstance().getStorageUnitRam();
            if (flash != null && flash.length == save_flash_size) {
                System.arraycopy(flash, 0, SAVE_FLASH, 0, save_flash_size);
            } else {
                fill(SAVE_FLASH, 0);
            }
        }
    }

    @Override
    public void readTransients(final DataInput in) throws IOException {
        super.readTransients(in);
        loadFlash();
    }

    @Override
    protected void saveNonVolatilePrgRam() {
        synchronized (GamePrefs.class) {
            final GamePrefs prefs = GamePrefs.getInstance();
            int[] flash = prefs.getStorageUnitRam();
            if (flash == null || flash.length != save_flash_size) {
                flash = new int[save_flash_size];
            }
            System.arraycopy(SAVE_FLASH, 0, flash, 0, save_flash_size);
            prefs.setStorageUnitRam(flash);
        }
        GamePrefs.save();
        super.saveNonVolatilePrgRam();
    }
}