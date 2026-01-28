package nintaco.disassembler;

import nintaco.App;
import nintaco.cpu.CPU;
import nintaco.PPU;
import nintaco.mappers.Mapper;

import java.util.*;

import static nintaco.disassembler.AddressType.*;
import static nintaco.disassembler.BranchesType.AbsoluteBranches;
import static nintaco.disassembler.BranchesType.HexBranches;
import static nintaco.disassembler.OperandsType.*;
import static nintaco.util.StringUtil.*;

public final class Disassembler {

    private static final InstructionAddressComparator INSTRUCTION_COMPARATOR
            = new InstructionAddressComparator();
    private static final int[] LENGTHS
            = {1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 0};
    private static final String[] MNEMONICS = {
            "AAC", "ADC", "AND", "ARR", "ASL", "ASR", "ATX", "AXS", "BCC", "BCS", "BEQ",
            "BIT", "BMI", "BNE", "BPL", "BRK", "BVC", "BVS", "CLC", "CLD", "CLI", "CLV",
            "CMP", "CPX", "CPY", "DCP", "DEC", "DEX", "DEY", "DOP", "EOR", "INC", "INX",
            "INY", "ISB", "JMP", "JSR", "KIL", "LAR", "LAX", "LDA", "LDX", "LDY", "LSR",
            "NOP", "ORA", "PHA", "PHP", "PLA", "PLP", "RLA", "ROL", "ROR", "RRA", "RTI",
            "RTS", "SAX", "SBC", "SEC", "SED", "SEI", "SHA", "SHX", "SHY", "SLO", "SRE",
            "STA", "STX", "STY", "TAX", "TAY", "TSX", "TXA", "TXS", "TYA", "UNDEFINED",
            "XAA", "XAS",
    };
    private static final String[] BRANCH_MNEMONICS = {
            "BPL", "BMI", "BVC", "BVS", "BCC", "BCS", "BNE", "BEQ",
    };
    private static final boolean[] BRANCHES = createMnemonicSet(BRANCH_MNEMONICS);
    private static final String[] JUMP_MNEMONICS = {"JMP", "JSR"};
    private static final boolean[] JUMPS = createMnemonicSet(JUMP_MNEMONICS);
    private static final int RTS = findMnemonic("RTS");
    private static final int[][] ALL_PATTERNS = {
            {15, 0}, {45, 6}, {37, 0}, {64, 6}, {44, 3}, {45, 3},
            {4, 3}, {64, 3}, {47, 0}, {45, 2}, {4, 1}, {0, 2},
            {44, 8}, {45, 8}, {4, 8}, {64, 8}, {14, 3}, {45, 7},
            {37, 0}, {64, 7}, {44, 4}, {45, 4}, {4, 4}, {64, 4},
            {18, 0}, {45, 11}, {44, 0}, {64, 11}, {44, 10}, {45, 10},
            {4, 10}, {64, 10}, {36, 8}, {2, 6}, {37, 0}, {50, 6},
            {11, 3}, {2, 3}, {51, 3}, {50, 3}, {49, 0}, {2, 2},
            {51, 1}, {0, 2}, {11, 8}, {2, 8}, {51, 8}, {50, 8},
            {12, 3}, {2, 7}, {37, 0}, {50, 7}, {44, 4}, {2, 4},
            {51, 4}, {50, 4}, {58, 0}, {2, 11}, {44, 0}, {50, 11},
            {44, 10}, {2, 10}, {51, 10}, {50, 10}, {54, 0}, {30, 6},
            {37, 0}, {65, 6}, {44, 3}, {30, 3}, {43, 3}, {65, 3},
            {46, 0}, {30, 2}, {43, 1}, {5, 2}, {35, 8}, {30, 8},
            {43, 8}, {65, 8}, {16, 3}, {30, 7}, {37, 0}, {65, 7},
            {44, 4}, {30, 4}, {43, 4}, {65, 4}, {20, 0}, {30, 11},
            {44, 0}, {65, 11}, {44, 10}, {30, 10}, {43, 10}, {65, 10},
            {55, 0}, {1, 6}, {37, 0}, {53, 6}, {44, 3}, {1, 3},
            {52, 3}, {53, 3}, {48, 0}, {1, 2}, {52, 1}, {3, 2},
            {35, 9}, {1, 8}, {52, 8}, {53, 8}, {17, 3}, {1, 7},
            {37, 0}, {53, 7}, {44, 4}, {1, 4}, {52, 4}, {53, 4},
            {60, 0}, {1, 11}, {44, 0}, {53, 11}, {44, 10}, {1, 10},
            {52, 10}, {53, 10}, {44, 2}, {66, 6}, {29, 2}, {56, 6},
            {68, 3}, {66, 3}, {67, 3}, {56, 3}, {28, 0}, {29, 2},
            {72, 0}, {76, 2}, {68, 8}, {66, 8}, {67, 8}, {56, 8},
            {8, 3}, {66, 7}, {37, 0}, {61, 7}, {68, 4}, {66, 4},
            {67, 5}, {56, 5}, {74, 0}, {66, 11}, {73, 0}, {77, 11},
            {63, 10}, {66, 10}, {62, 11}, {61, 11}, {42, 2}, {40, 6},
            {41, 2}, {39, 6}, {42, 3}, {40, 3}, {41, 3}, {39, 3},
            {70, 0}, {40, 2}, {69, 0}, {6, 2}, {42, 8}, {40, 8},
            {41, 8}, {39, 8}, {9, 3}, {40, 7}, {37, 0}, {39, 7},
            {42, 4}, {40, 4}, {41, 5}, {39, 5}, {21, 0}, {40, 11},
            {71, 0}, {38, 11}, {42, 10}, {40, 10}, {41, 11}, {39, 11},
            {24, 2}, {22, 6}, {29, 2}, {25, 6}, {24, 3}, {22, 3},
            {26, 3}, {25, 3}, {33, 0}, {22, 2}, {27, 0}, {7, 2},
            {24, 8}, {22, 8}, {26, 8}, {25, 8}, {13, 3}, {22, 7},
            {37, 0}, {25, 7}, {44, 4}, {22, 4}, {26, 4}, {25, 4},
            {19, 0}, {22, 11}, {44, 0}, {25, 11}, {44, 10}, {22, 10},
            {26, 10}, {25, 10}, {23, 2}, {57, 6}, {29, 2}, {34, 6},
            {23, 3}, {57, 3}, {31, 3}, {34, 3}, {32, 0}, {57, 2},
            {44, 0}, {57, 2}, {23, 8}, {57, 8}, {31, 8}, {34, 8},
            {10, 3}, {57, 7}, {37, 0}, {34, 7}, {44, 4}, {57, 4},
            {31, 4}, {34, 4}, {59, 0}, {57, 11}, {44, 0}, {34, 11},
            {44, 10}, {57, 10}, {31, 10}, {34, 10},
    };
    private static final int[][] OFFICIAL_PATTERNS = {
            {15, 0}, {45, 6}, {75, 12}, {75, 12}, {75, 12}, {45, 3},
            {4, 3}, {75, 12}, {47, 0}, {45, 2}, {4, 1}, {75, 12},
            {75, 12}, {45, 8}, {4, 8}, {75, 12}, {14, 3}, {45, 7},
            {75, 12}, {75, 12}, {75, 12}, {45, 4}, {4, 4}, {75, 12},
            {18, 0}, {45, 11}, {75, 12}, {75, 12}, {75, 12}, {45, 10},
            {4, 10}, {75, 12}, {36, 8}, {2, 6}, {75, 12}, {75, 12},
            {11, 3}, {2, 3}, {51, 3}, {75, 12}, {49, 0}, {2, 2},
            {51, 1}, {75, 12}, {11, 8}, {2, 8}, {51, 8}, {75, 12},
            {12, 3}, {2, 7}, {75, 12}, {75, 12}, {75, 12}, {2, 4},
            {51, 4}, {75, 12}, {58, 0}, {2, 11}, {75, 12}, {75, 12},
            {75, 12}, {2, 10}, {51, 10}, {75, 12}, {54, 0}, {30, 6},
            {75, 12}, {75, 12}, {75, 12}, {30, 3}, {43, 3}, {75, 12},
            {46, 0}, {30, 2}, {43, 1}, {75, 12}, {35, 8}, {30, 8},
            {43, 8}, {75, 12}, {16, 3}, {30, 7}, {75, 12}, {75, 12},
            {75, 12}, {30, 4}, {43, 4}, {75, 12}, {20, 0}, {30, 11},
            {75, 12}, {75, 12}, {75, 12}, {30, 10}, {43, 10}, {75, 12},
            {55, 0}, {1, 6}, {75, 12}, {75, 12}, {75, 12}, {1, 3},
            {52, 3}, {75, 12}, {48, 0}, {1, 2}, {52, 1}, {75, 12},
            {35, 9}, {1, 8}, {52, 8}, {75, 12}, {17, 3}, {1, 7},
            {75, 12}, {75, 12}, {75, 12}, {1, 4}, {52, 4}, {75, 12},
            {60, 0}, {1, 11}, {75, 12}, {75, 12}, {75, 12}, {1, 10},
            {52, 10}, {75, 12}, {75, 12}, {66, 6}, {75, 12}, {75, 12},
            {68, 3}, {66, 3}, {67, 3}, {75, 12}, {28, 0}, {75, 12},
            {72, 0}, {75, 12}, {68, 8}, {66, 8}, {67, 8}, {75, 12},
            {8, 3}, {66, 7}, {75, 12}, {75, 12}, {68, 4}, {66, 4},
            {67, 5}, {75, 12}, {74, 0}, {66, 11}, {73, 0}, {75, 12},
            {75, 12}, {66, 10}, {75, 12}, {75, 12}, {42, 2}, {40, 6},
            {41, 2}, {75, 12}, {42, 3}, {40, 3}, {41, 3}, {75, 12},
            {70, 0}, {40, 2}, {69, 0}, {75, 12}, {42, 8}, {40, 8},
            {41, 8}, {75, 12}, {9, 3}, {40, 7}, {75, 12}, {75, 12},
            {42, 4}, {40, 4}, {41, 5}, {75, 12}, {21, 0}, {40, 11},
            {71, 0}, {75, 12}, {42, 10}, {40, 10}, {41, 11}, {75, 12},
            {24, 2}, {22, 6}, {75, 12}, {75, 12}, {24, 3}, {22, 3},
            {26, 3}, {75, 12}, {33, 0}, {22, 2}, {27, 0}, {75, 12},
            {24, 8}, {22, 8}, {26, 8}, {75, 12}, {13, 3}, {22, 7},
            {75, 12}, {75, 12}, {75, 12}, {22, 4}, {26, 4}, {75, 12},
            {19, 0}, {22, 11}, {75, 12}, {75, 12}, {75, 12}, {22, 10},
            {26, 10}, {75, 12}, {23, 2}, {57, 6}, {75, 12}, {75, 12},
            {23, 3}, {57, 3}, {31, 3}, {75, 12}, {32, 0}, {57, 2},
            {44, 0}, {75, 12}, {23, 8}, {57, 8}, {31, 8}, {75, 12},
            {10, 3}, {57, 7}, {75, 12}, {75, 12}, {75, 12}, {57, 4},
            {31, 4}, {75, 12}, {59, 0}, {57, 11}, {75, 12}, {75, 12},
            {75, 12}, {57, 10}, {31, 10}, {75, 12},
    };
    public static char[][] FLAGS = {
            {'n', 'N'}, {'v', 'V'}, {'u', 'U'}, {'b', 'B'},
            {'d', 'D'}, {'i', 'I'}, {'z', 'Z'}, {'c', 'C'},
    };
    private static Map<Integer, AddressLabel> labels;
    private static boolean showPC = true;
    private static boolean showLabels = true;
    private static int addressType;
    private static boolean machineCode = true;
    private static boolean inspections = true;
    private static int branchesType;
    private Disassembler() {
    }

    private static boolean[] createMnemonicSet(final String[] mnemonics) {
        final boolean[] mnemonicMap = new boolean[MNEMONICS.length];
        for (int i = mnemonics.length - 1; i >= 0; i--) {
            mnemonicMap[findMnemonic(mnemonics[i])] = true;
        }
        return mnemonicMap;
    }

    private static int findMnemonic(final String mnemonic) {
        for (int i = MNEMONICS.length - 1; i >= 0; i--) {
            if (mnemonic.equals(MNEMONICS[i])) {
                return i;
            }
        }
        return -1;
    }

    public static void setLabels(final Map<Integer, AddressLabel> labels) {
        Disassembler.labels = labels;
    }

    public static void setShowPC(final boolean showPC) {
        Disassembler.showPC = showPC;
    }

    public static void setShowLabels(final boolean showLabels) {
        Disassembler.showLabels = showLabels;
    }

    public static void setAddressType(final int addressType) {
        Disassembler.addressType = addressType;
    }

    public static void setMachineCode(final boolean machineCode) {
        Disassembler.machineCode = machineCode;
    }

    public static void setInspections(final boolean inspections) {
        Disassembler.inspections = inspections;
    }

    public static void setBranchesType(final int branchesType) {
        Disassembler.branchesType = branchesType;
    }

    private static void appendRegisters(final StringBuilder sb,
                                        final LogRecord record, final LogPrefs prefs) {

        if (prefs.frameCounter) {
            append(sb, "f%-7d", record.frameCounter);
        }
        if (prefs.cpuCounter) {
            append(sb, "c%-12d", record.cpuCycleCounter);
        }
        if (prefs.instructionCounter) {
            appendUnsignedInt(sb, "i%-12d", record.instructionsCounter);
        }
        if (prefs.A) {
            append(sb, "A:%02X ", record.A);
        }
        if (prefs.X) {
            append(sb, "X:%02X ", record.X);
        }
        if (prefs.Y) {
            append(sb, "Y:%02X ", record.Y);
        }
        if (prefs.logPType == LogPType.HH) {
            append(sb, "P:%02X ", record.P);
        } else if (prefs.logPType == LogPType.NVUBDIZC) {
            sb.append("P:");
            for (int i = 7; i >= 0; i--) {
                sb.append(FLAGS[7 - i][(record.P >> i) & 1]);
            }
            sb.append(' ');
        }
        if (prefs.logSType != LogSType.NoSP) {
            if (prefs.logSType == LogSType.S) {
                sb.append("S:");
            } else {
                sb.append("SP:");
            }
            append(sb, "%02X ", record.S);
        }
        if (prefs.v) {
            append(sb, "v:%04X ", record.v);
        }
        if (prefs.t) {
            append(sb, "t:%04X ", record.t);
        }
        if (prefs.x) {
            append(sb, "x:%d ", record.x);
        }
        if (prefs.w) {
            append(sb, "w:%d ", record.w ? 1 : 0);
        }
        if (prefs.dot) {
            append(sb, "CYC:%3d ", record.scanlineCycle);
        }
        if (prefs.scanline) {
            append(sb, "SL:%-3d ", record.scanline);
        }

        final int[] addresses = prefs.addresses;
        final int[] values = record.values;
        if (addresses != null && values != null
                && addresses.length == values.length) {
            for (int i = 0; i < addresses.length; i++) {
                append(sb, "%04X:%02X ", addresses[i], values[i]);
            }
        }
    }

    private static int appendInstruction(final StringBuilder sb,
                                         final LogRecord record, final LogPrefs prefs) {

        final Map<Integer, AddressLabel> labs = prefs.addressLabels ? labels : null;

        int size = 48;
        if (prefs.bank) {
            if (record.bank >= 0) {
                append(sb, "%02X", record.bank);
            } else {
                sb.append("  ");
            }
            if (prefs.logPCType == LogPCType.NoPC) {
                sb.append("  ");
                size += 4;
            } else {
                sb.append(":");
                size += 3;
            }
        }
        if (prefs.logPCType != LogPCType.NoPC) {
            if (prefs.logPCType == LogPCType.DollarPC) {
                sb.append('$');
                size++;
            }
            append(sb, "%04X  ", record.pc);
        }
        if (prefs.machineCode) {
            append(sb, "%02X", record.opcode);
            if (record.length > 1) {
                append(sb, " %02X", record.b1);
            } else {
                sb.append("   ");
            }
            if (record.length > 2) {
                append(sb, " %02X", record.b2);
            } else {
                sb.append("   ");
            }
            sb.append("  ");
        }
        if (prefs.instruction) {
            sb.append(record.mnemonic).append(' ');
            switch (record.instructionType) {
                case A:
                    sb.append('A');
                    break;
                case IMMEDIATE:
                    sb.append(String.format("#$%02X", record.b1));
                    break;
                case ZERO_PAGE:
                    if (BRANCHES[record.mnemonicIndex]) {
                        if (prefs.branchesType == AbsoluteBranches) {
                            appendAddress(sb, record.value1, record.bank1, true, false, labs);
                        } else if (!(showLabels && appendAddress(sb, record.value1,
                                record.bank1, true, true, labs))) {
                            if (prefs.branchesType == HexBranches) {
                                sb.append(String.format("$%02X", record.b1));
                            } else {
                                sb.append(String.format("%d", (byte) record.b1));
                            }
                        }
                    } else {
                        appendAddress(sb, record.b1, record.bank0, true, false, "%02X",
                                labs);
                        if (prefs.inspections) {
                            append(sb, " = %02X", record.value0);
                        }
                    }
                    break;
                case ZERO_PAGE_X:
                    append(sb, "$%02X,X", record.b1);
                    if (prefs.inspections) {
                        sb.append(" @ ");
                        appendAddress(sb, record.value0, record.bank0, false, false,
                                "%02X", labs);
                        append(sb, " = %02X", record.value1);
                    }
                    break;
                case ZERO_PAGE_Y:
                    append(sb, "$%02X,Y", record.b1);
                    if (prefs.inspections) {
                        sb.append(" @ ");
                        appendAddress(sb, record.value0, record.bank0, false, false,
                                "%02X", labs);
                        append(sb, " = %02X", record.value1);
                    }
                    break;
                case INDIRECT_X:
                    append(sb, "($%02X,X)", record.b1);
                    if (prefs.inspections) {
                        sb.append(" @ ");
                        appendAddress(sb, record.value1, record.bank1, false, false, labs);
                        append(sb, " = %02X", record.value2);
                    }
                    break;
                case INDIRECT_Y:
                    append(sb, "($%02X),Y", record.b1);
                    if (prefs.inspections) {
                        append(sb, " = %04X @ ", record.value0);
                        appendAddress(sb, record.value1, record.bank1, false, false, labs);
                        append(sb, " = %02X", record.value2);
                    }
                    break;
                case ABSOLUTE:
                    appendAddress(sb, record.value0, record.bank0, true, false, labs);
                    if (!JUMPS[record.mnemonicIndex] && prefs.inspections) {
                        append(sb, " = %02X", record.value1);
                    }
                    break;
                case INDIRECT:
                    sb.append('(');
                    appendAddress(sb, record.value0, record.bank0, true, false, labs);
                    sb.append(')');
                    if (prefs.inspections) {
                        append(sb, " = %04X", record.value1);
                    }
                    break;
                case ABSOLUTE_X:
                    appendAddress(sb, record.value0, record.bank0, true, false, labs);
                    sb.append(",X");
                    if (prefs.inspections) {
                        sb.append(" @ ");
                    }
                    appendAddress(sb, record.value1, record.bank1, false, false, labs);
                    append(sb, " = %02X", record.value2);
                    break;
                case ABSOLUTE_Y:
                    appendAddress(sb, record.value0, record.bank0, true, false, labs);
                    sb.append(",Y");
                    if (prefs.inspections) {
                        sb.append(" @ ");
                    }
                    appendAddress(sb, record.value1, record.bank1, false, false, labs);
                    append(sb, " = %02X", record.value2);
                    break;
            }
        }
        return size;
    }

    public static void appendLogRecord(final StringBuilder sb,
                                       final LogRecord record, final LogPrefs prefs) {
        if (prefs.tabBySP) {
            appendRegisters(sb, record, prefs);
            for (int i = record.S; i < 0xFF; i++) {
                sb.append(' ');
            }
            appendInstruction(sb, record, prefs);
        } else {
            final int size = appendInstruction(sb, record, prefs);
            while (sb.length() < size) {
                sb.append(' ');
            }
            appendRegisters(sb, record, prefs);
        }
    }

    public static void captureRegisters(final LogPrefs prefs,
                                        final LogRecord record, final CPU cpu, final PPU ppu,
                                        final Mapper mapper) {

        final int[] addresses = prefs.addresses;
        if (addresses != null) {
            final int[] values = record.values;
            for (int i = addresses.length - 1; i >= 0; i--) {
                values[i] = mapper.peekCpuMemory(addresses[i]);
            }
        }

        record.A = cpu.register().a();
        record.X = cpu.register().x();
        record.Y = cpu.register().y();
        record.P = cpu.register().p();
        record.S = cpu.register().s();

        record.v = ppu.getV();
        record.t = ppu.getT();
        record.x = ppu.getX();
        record.w = ppu.isW();
    }

    public static void captureInstruction(final LogRecord record,
                                          final CPU cpu, final PPU ppu, final Mapper mapper) {

        record.frameCounter = ppu.getFrameCounter();
        record.instructionsCounter = cpu.state().instructionsCounter();
        record.cpuCycleCounter = cpu.state().cycleCounter();

        record.bank = mapper.getPrgBank(cpu.register().pc());
        record.pc = cpu.register().pc();

        record.scanlineCycle = ppu.getScanlineCycle();
        record.scanline = ppu.getScanline();
        record.opcode = mapper.peekCpuMemory(record.pc);

        final int[] pattern = ALL_PATTERNS[record.opcode];
        record.length = LENGTHS[pattern[1]];
        if (record.length >= 2) {
            record.b1 = mapper.peekCpuMemory(record.pc + 1);
        }
        if (record.length == 3) {
            record.b2 = mapper.peekCpuMemory(record.pc + 2);
        }

        record.mnemonicIndex = pattern[0];
        record.mnemonic = MNEMONICS[record.mnemonicIndex];
        record.instructionType = pattern[1];

        switch (record.instructionType) {
            case ZERO_PAGE:
                record.value0 = mapper.peekCpuMemory(record.b1);
                record.bank0 = mapper.getPrgBank(record.b1);
                record.value1 = (2 + record.pc + (byte) record.b1) & 0xFFFF;
                record.bank1 = mapper.getPrgBank(record.value1);
                break;
            case ZERO_PAGE_X:
                record.value0 = (record.b1 + cpu.register().x()) & 0x00FF;
                record.value1 = mapper.peekCpuMemory(record.value0);
                record.bank0 = mapper.getPrgBank(record.value0);
                break;
            case ZERO_PAGE_Y:
                record.value0 = (record.b1 + cpu.register().y()) & 0x00FF;
                record.value1 = mapper.peekCpuMemory(record.value0);
                record.bank0 = mapper.getPrgBank(record.value0);
                break;
            case INDIRECT_X:
                record.value0 = (record.b1 + cpu.register().x()) & 0x00FF;
                record.value1 = (mapper.peekCpuMemory((record.value0 + 1) & 0xFF) << 8)
                        | mapper.peekCpuMemory(record.value0 & 0xFF);
                record.value2 = mapper.peekCpuMemory(record.value1);
                record.bank1 = mapper.getPrgBank(record.value1);
                break;
            case INDIRECT_Y:
                record.value0 = ((mapper.peekCpuMemory((record.b1 + 1) & 0xFF) << 8)
                        | mapper.peekCpuMemory(record.b1)) & 0xFFFF;
                record.value1 = (record.value0 + cpu.register().y()) & 0xFFFF;
                record.value2 = mapper.peekCpuMemory(record.value1);
                record.bank1 = mapper.getPrgBank(record.value1);
                break;
            case ABSOLUTE:
                record.value0 = (record.b2 << 8) | record.b1;
                record.value1 = mapper.peekCpuMemory(record.value0);
                record.bank0 = mapper.getPrgBank(record.value0);
                break;
            case INDIRECT:
                record.value0 = (record.b2 << 8) | record.b1;
                record.bank0 = mapper.getPrgBank(record.value0);
                record.value1 = (mapper.peekCpuMemory(record.value0 + 1) << 8)
                        | mapper.peekCpuMemory(record.value0);
                break;
            case ABSOLUTE_X:
                record.value0 = (record.b2 << 8) | record.b1;
                record.bank0 = mapper.getPrgBank(record.value0);
                record.value1 = (record.value0 + cpu.register().x()) & 0xFFFF;
                record.value2 = mapper.peekCpuMemory(record.value1);
                record.bank1 = mapper.getPrgBank(record.value1);
                break;
            case ABSOLUTE_Y:
                record.value0 = (record.b2 << 8) | record.b1;
                record.bank0 = mapper.getPrgBank(record.value0);
                record.value1 = (record.value0 + cpu.register().y()) & 0xFFFF;
                record.value2 = mapper.peekCpuMemory(record.value1);
                record.bank1 = mapper.getPrgBank(record.value1);
                break;
        }
    }

    public static String toString(final CPU cpu, final PPU ppu,
                                  final Mapper mapper) {

        final int PC = cpu.register().pc();
        final int opcode = mapper.peekCpuMemory(PC);
        final int[] pattern = ALL_PATTERNS[opcode];
        final int length = LENGTHS[pattern[1]];
        int b1 = 0;
        int b2 = 0;

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%04X  %02X ", PC & 0xFFFF, opcode));
        if (length >= 2) {
            b1 = mapper.peekCpuMemory(PC + 1);
            sb.append(String.format("%02X ", b1));
        } else {
            sb.append("   ");
        }
        if (length == 3) {
            b2 = mapper.peekCpuMemory(PC + 2);
            sb.append(String.format("%02X  ", b2));
        } else {
            sb.append("    ");
        }

        sb.append(MNEMONICS[pattern[0]]).append(' ');
        switch (pattern[1]) {
            case A:
                sb.append("A                           ");
                break;
            case IMMEDIATE:
                sb.append(String.format("#$%02X                        ", b1));
                break;
            case ZERO_PAGE:
                if (BRANCHES[pattern[0]]) {
                    sb.append(String.format("$%04X                       ",
                            (2 + PC + (byte) b1) & 0xFFFF));
                } else {
                    sb.append(String.format("$%02X = %02X                    ", b1,
                            mapper.peekCpuMemory(b1)));
                }
                break;
            case ZERO_PAGE_X: {
                int addr = (b1 + cpu.register().x()) & 0x00FF;
                sb.append(String.format("$%02X,X @ %02X = %02X             ", b1,
                        addr, mapper.peekCpuMemory(addr)));
                break;
            }
            case ZERO_PAGE_Y: {
                int addr = (b1 + cpu.register().y()) & 0x00FF;
                sb.append(String.format("$%02X,Y @ %02X = %02X             ", b1,
                        addr, mapper.peekCpuMemory(addr)));
                break;
            }
            case INDIRECT_X: {
                int addr = b1 + cpu.register().x();
                int a0 = mapper.peekCpuMemory(addr & 0xFF);
                int a1 = mapper.peekCpuMemory((addr + 1) & 0xFF);
                addr = (a1 << 8) | a0;
                sb.append(String.format("($%02X,X) @ %04X = %02X         ", b1,
                        addr, mapper.peekCpuMemory(addr)));
                break;
            }
            case INDIRECT_Y: {
                int a0 = mapper.peekCpuMemory(b1);
                int a1 = mapper.peekCpuMemory((b1 + 1) & 0xFF);
                int addr = (((a1 << 8) | a0) + cpu.register().y()) & 0xFFFF;
                sb.append(String.format("($%02X),Y = %04X @ %04X = %02X  ", b1,
                        ((a1 << 8) | a0) & 0xFFFF, addr, mapper.peekCpuMemory(addr)));
                break;
            }
            case ABSOLUTE: {
                int addr = (b2 << 8) | b1;
                if (JUMPS[pattern[0]]) {
                    sb.append(String.format("$%04X                       ", addr));
                } else {
                    sb.append(String.format("$%04X = %02X                  ", addr,
                            mapper.peekCpuMemory(addr)));
                }
                break;
            }
            case INDIRECT: {
                int addr = (b2 << 8) | b1;
                int a0 = mapper.peekCpuMemory(addr);
                int a1 = mapper.peekCpuMemory(addr + 1);
                sb.append(String.format("($%04X) = %04X              ", addr,
                        (a1 << 8) | a0));
                break;
            }
            case ABSOLUTE_X: {
                int word = (b2 << 8) | b1;
                int addr = (word + cpu.register().x()) & 0xFFFF;
                sb.append(String.format("$%04X,X @ %04X = %02X         ", word, addr,
                        mapper.peekCpuMemory(addr)));
                break;
            }
            case ABSOLUTE_Y: {
                int word = (b2 << 8) | b1;
                int addr = (word + cpu.register().y()) & 0xFFFF;
                sb.append(String.format("$%04X,Y @ %04X = %02X         ", word, addr,
                        mapper.peekCpuMemory(addr)));
                break;
            }
            default:
                sb.append("                            ");
                break;
        }

        sb.append(String.format(
                "A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC:%3d SL:%d",
                cpu.register().a(), cpu.register().x(), cpu.register().y(), cpu.register().p(), cpu.register().s(),
                ppu.getScanlineCycle(), ppu.getScanline()));

        return sb.toString();
    }

    private static int countAlignments(final Mapper mapper, int address,
                                       final int maxCount, final int[][] patterns) {
        int count = 0;
        while (count < maxCount) {
            final int length = getOfficialInstructionLength(mapper, address,
                    patterns);
            if (length == 0) {
                break;
            } else {
                address += length;
                if (address > 0xFFFF) {
                    break;
                }
                count++;
            }
        }
        return count;
    }

    private static int countInstructions(final Mapper mapper, int startAddress,
                                         final int endAddress, final int[][] patterns) {
        int count = 0;
        while (startAddress < endAddress) {
            final int length = getOfficialInstructionLength(mapper, startAddress,
                    patterns);
            if (length == 0) {
                break;
            } else {
                startAddress += length;
                count++;
            }
        }
        return startAddress == endAddress ? count : -1;
    }

    private static int getPositiveInstructionLength(final Mapper mapper,
                                                    final int address, final int[][] patterns) {
        final int length = LENGTHS[patterns[mapper.peekCpuMemory(address)][1]];
        return length == 0 ? 1 : length;
    }

    private static int getOfficialInstructionLength(final Mapper mapper,
                                                    final int address, final int[][] patterns) {
        return LENGTHS[patterns[mapper.peekCpuMemory(address)][1]];
    }

    private static int getAllInstructionLength(final Mapper mapper,
                                               final int address) {
        return LENGTHS[ALL_PATTERNS[mapper.peekCpuMemory(address)][1]];
    }

    private static Instruction createUnknownInstruction(final CPU cpu,
                                                        final int address) {

        final StringBuilder sb = new StringBuilder();
        final Instruction instruction = new Instruction();
        final Mapper mapper = cpu.mapper();
        final int bank = cpu.mapper().getPrgBank(address);
        final int descriptionLines = appendHeader(cpu, mapper, address, bank, sb,
                instruction);
        final String code = String.format("%02X", mapper.peekCpuMemory(address));

        if (machineCode) {
            sb.append(code).append("        ");
        } else if (addressType != None) {
            sb.append("  ");
        }
        sb.append(MNEMONICS[MNEMONICS.length - 1]);

        instruction.setMachineCode(code);
        instruction.setAddress(address);
        instruction.setLength(1);
        instruction.setDescription(sb.toString());
        instruction.setDescriptionLines(descriptionLines);

        return instruction;
    }

    private static int appendHeader(final CPU cpu, final Mapper mapper,
                                    final int address, final int bank, final StringBuilder sb,
                                    final Instruction instruction) {

        final Map<Integer, AddressLabel> labs = showLabels ? labels : null;

        int descriptionLines = 1;
        if (labs != null) {
            AddressLabel addressLabel = labs.get(AddressLabel
                    .createKey(bank, address));
            if (addressLabel == null) {
                addressLabel = labs.get(AddressLabel.createKey(-1, address));
            }
            if (addressLabel != null && addressLabel.isCode()) {
                final String label = addressLabel.getLabel();
                if (!isBlank(label)) {
                    appendAddress(sb, address, instruction, mapper, true, false);
                    sb.append(':').append('\n');
                    descriptionLines++;
                }
                final String comment = addressLabel.getComment();
                if (!isBlank(comment)) {
                    final String[] lines = comment.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        sb.append("; ").append(lines[i]).append('\n');
                        descriptionLines++;
                    }
                }
            }
        }

        if (showPC) {
            if (cpu.register().pc() == address) {
                sb.append('>');
            } else {
                sb.append(' ');
            }
        }

        switch (addressType) {
            case BankAndAddress:
                if (bank < 0) {
                    sb.append("  ");
                } else {
                    append(sb, "%02X", bank & 0xFF);
                }
                sb.append(':');
                appendAddress(sb, address, instruction, mapper, false, false);
                break;
            case FileOffset:
                sb.append(' ');
                final int fileIndex = App.getFileIndex(address, true);
                if (fileIndex >= 0) {
                    append(sb, "%06X", fileIndex);
                } else {
                    sb.append(" :");
                    appendAddress(sb, address, instruction, mapper, false, false);
                }
                break;
            case None:
                break;
        }

        if (machineCode && addressType != None) {
            sb.append(':');
        }

        return descriptionLines;
    }

    private static Instruction disassemble(final CPU cpu, int address,
                                           final int[][] patterns) {

        address &= 0xFFFF;

        final Instruction instruction = new Instruction();
        final StringBuilder sb = new StringBuilder();
        final Mapper mapper = cpu.mapper();
        final int opcode = mapper.peekCpuMemory(address);
        final int bank = mapper.getPrgBank(address);
        final int[] pattern = patterns[opcode];
        final int length = LENGTHS[pattern[1]];
        final int descriptionLines = appendHeader(cpu, mapper, address, bank, sb,
                instruction);

        int b1 = 0;
        int b2 = 0;

        final String code;
        switch (length) {
            case 2:
                b1 = mapper.peekCpuMemory(address + 1);
                code = String.format("%02X %02X", opcode, b1);
                break;
            case 3:
                b1 = mapper.peekCpuMemory(address + 1);
                b2 = mapper.peekCpuMemory(address + 2);
                code = String.format("%02X %02X %02X", opcode, b1, b2);
                break;
            default:
                code = String.format("%02X", opcode);
                break;
        }

        if (machineCode) {
            sb.append(code);
            if (length <= 1) {
                sb.append("      ");
            } else if (length == 2) {
                sb.append("   ");
            }
            sb.append("  ");
        } else if (addressType != None) {
            sb.append("  ");
        }

        sb.append(MNEMONICS[pattern[0]]).append(' ');
        switch (pattern[1]) {
            case NONE:
                if (pattern[0] == RTS && inspections) {
                    sb.append("-----------------------------------------");
                }
                break;
            case A:
                sb.append('A');
                break;
            case IMMEDIATE:
                sb.append(String.format("#$%02X", b1));
                break;
            case ZERO_PAGE:
                if (BRANCHES[pattern[0]]) {
                    if (branchesType == AbsoluteBranches) {
                        appendAddress(sb, 2 + address + (byte) b1, instruction, mapper,
                                true, false);
                    } else if (!(showLabels && appendAddress(sb, 2 + address + (byte) b1,
                            instruction, mapper, true, true))) {
                        if (branchesType == HexBranches) {
                            sb.append(String.format("$%02X", b1));
                        } else {
                            sb.append(String.format("%d", (byte) b1));
                        }
                    }
                } else {
                    appendAddress(sb, b1, instruction, mapper, true, false);
                    if (inspections) {
                        sb.append(String.format(" = #$%02X", mapper.peekCpuMemory(b1)));
                    }
                }
                break;
            case ZERO_PAGE_X: {
                append(sb, "$%02X,X", b1);
                if (inspections) {
                    sb.append(" @ ");
                    final int addr = (b1 + cpu.register().x()) & 0x00FF;
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                }
                break;
            }
            case ZERO_PAGE_Y: {
                append(sb, "$%02X,Y", b1);
                if (inspections) {
                    sb.append(" @ ");
                    final int addr = (b1 + cpu.register().y()) & 0x00FF;
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                }
                break;
            }
            case INDIRECT_X: {
                append(sb, "($%02X,X)", b1);
                if (inspections) {
                    sb.append(" @ ");
                    int addr = b1 + cpu.register().x();
                    final int a0 = mapper.peekCpuMemory(addr & 0xFF);
                    final int a1 = mapper.peekCpuMemory((addr + 1) & 0xFF);
                    addr = (a1 << 8) | a0;
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                }
                break;
            }
            case INDIRECT_Y: {
                append(sb, "($%02X),Y", b1);
                if (inspections) {
                    sb.append(" @ ");
                    final int a0 = mapper.peekCpuMemory(b1);
                    final int a1 = mapper.peekCpuMemory((b1 + 1) & 0xFF);
                    final int addr = (((a1 << 8) | a0) + cpu.register().y()) & 0xFFFF;
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                }
                break;
            }
            case ABSOLUTE: {
                final int addr = (b2 << 8) | b1;
                if (JUMPS[pattern[0]]) {
                    appendAddress(sb, addr, instruction, mapper, true, false);
                } else {
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    if (inspections) {
                        append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                    }
                }
                break;
            }
            case INDIRECT: {
                final int addr = (b2 << 8) | b1;
                sb.append('(');
                appendAddress(sb, addr, instruction, mapper, true, false);
                sb.append(')');
                if (inspections) {
                    final int a0 = mapper.peekCpuMemory(addr);
                    final int a1 = mapper.peekCpuMemory(addr + 1);
                    append(sb, " = #$%04X", (a1 << 8) | a0);
                }
                break;
            }
            case ABSOLUTE_X: {
                final int word = (b2 << 8) | b1;
                appendAddress(sb, word, instruction, mapper, true, false);
                sb.append(",X");
                if (inspections) {
                    final int addr = (word + cpu.register().x()) & 0xFFFF;
                    sb.append(" @ ");
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                }
                break;
            }
            case ABSOLUTE_Y: {
                final int word = (b2 << 8) | b1;
                appendAddress(sb, word, instruction, mapper, true, false);
                sb.append(",Y");
                if (inspections) {
                    final int addr = (word + cpu.register().y()) & 0xFFFF;
                    sb.append(" @ ");
                    appendAddress(sb, addr, instruction, mapper, true, false);
                    append(sb, " = #$%02X", mapper.peekCpuMemory(addr));
                }
                break;
            }
        }

        instruction.setMachineCode(code);
        instruction.setAddress(address);
        instruction.setLength(length == 0 ? 1 : length);
        instruction.setDescription(sb.toString());
        instruction.setDescriptionLines(descriptionLines);

        return instruction;
    }

    private static boolean appendAddress(final StringBuilder sb, int address,
                                         final int bank, final boolean dollar,
                                         final boolean appendOnlyIfLabelFound, final String format,
                                         final Map<Integer, AddressLabel> labels) {
        return appendAddress(sb, address, null, bank, dollar,
                appendOnlyIfLabelFound, format, labels);
    }

    private static boolean appendAddress(final StringBuilder sb, int address,
                                         final int bank, final boolean dollar,
                                         final boolean appendOnlyIfLabelFound,
                                         final Map<Integer, AddressLabel> labels) {
        return appendAddress(sb, address, null, bank, dollar,
                appendOnlyIfLabelFound, "%04X", labels);
    }

    private static boolean appendAddress(final StringBuilder sb, int address,
                                         final Instruction instruction, final Mapper mapper,
                                         final boolean dollar, final boolean appendOnlyIfLabelFound) {
        return appendAddress(sb, address, instruction, mapper.getPrgBank(address),
                dollar, appendOnlyIfLabelFound, "%04X");
    }

    private static boolean appendAddress(final StringBuilder sb, int address,
                                         final Instruction instruction, final int bank,
                                         final boolean dollar, final boolean appendOnlyIfLabelFound,
                                         final String format) {
        return appendAddress(sb, address, instruction, bank, dollar,
                appendOnlyIfLabelFound, format, showLabels ? labels : null);
    }

    private static boolean appendAddress(final StringBuilder sb, int address,
                                         final Instruction instruction, final int bank,
                                         final boolean dollar, final boolean appendOnlyIfLabelFound,
                                         final String format, final Map<Integer, AddressLabel> labels) {

        address &= 0xFFFF;
        String label = null;
        if (dollar && labels != null) {
            AddressLabel addressLabel = labels.get(AddressLabel
                    .createKey(bank, address));
            if (addressLabel == null && bank >= 0) {
                addressLabel = labels.get(AddressLabel.createKey(-1, address));
            }
            if (addressLabel != null && addressLabel.isCode()) {
                final String lab = addressLabel.getLabel();
                if (!isBlank(lab)) {
                    label = lab;
                }
            }
        }
        if (label == null) {
            if (appendOnlyIfLabelFound) {
                return false;
            }
            if (dollar) {
                sb.append('$');
            }
            label = String.format(format, address);
        }
        if (instruction != null) {
            final AddressTextRange[] ranges = instruction.getRanges();
            int i = 0;
            for (; i < ranges.length; i++) {
                if (ranges[i] == null) {
                    break;
                }
            }
            ranges[i] = new AddressTextRange(bank, address, sb.length(),
                    sb.length() + label.length());
        }
        sb.append(label);
        return true;
    }

    private static void disassembleRange(final List<Instruction> result,
                                         final CPU cpu, int address, final int startIndex,
                                         final int count, final int[][] patterns) {
        final Mapper mapper = cpu.mapper();
        for (int i = 0; i < count && address <= 0xFFFF; i++) {
            if (i >= startIndex) {
                final Instruction instruction = disassemble(cpu, address, patterns);
                result.add(instruction);
                address += instruction.getLength();
            } else {
                address += getPositiveInstructionLength(mapper, address, patterns);
            }
        }
    }

    private static void findPriorInstructions(final List<Instruction> result,
                                              final CPU cpu, final int size, int startAddress, final int endAddress,
                                              final int[][] patterns) {

        if (size <= 0) {
            return;
        }
        if (startAddress < 0) {
            startAddress = 0;
        }
        if (startAddress >= endAddress || endAddress == 0) {
            return;
        }

        final Mapper mapper = cpu.mapper();
        int count = 0;
        while (true) {
            count = countInstructions(mapper, startAddress, endAddress, patterns);
            if (count > 0 || ++startAddress == endAddress) {
                break;
            }
        }

        if (startAddress == endAddress) {
            if (size > 1) {
                findPriorInstructions(result, cpu, size - 1,
                        endAddress - (size << 1) - 7, endAddress - 1, patterns);
            }
            result.add(createUnknownInstruction(cpu, endAddress - 1));
        } else {
            if (count < size) {
                findPriorInstructions(result, cpu, size - count,
                        startAddress - ((size - count) << 1) - 7, startAddress, patterns);
            }
            disassembleRange(result, cpu, startAddress, Math.max(0, count - size),
                    count, patterns);
        }
    }

    public static int getNearestAddress(final Mapper mapper, final int address,
                                        final boolean officialsOnly) {
        final int[][] patterns = officialsOnly ? OFFICIAL_PATTERNS : ALL_PATTERNS;
        int bestCount = Integer.MIN_VALUE;
        int bestAddress = 0;
        for (int i = -1; i <= 1; i++) {
            final int a = address + i;
            final int count = countAlignments(mapper, a, 64, patterns);
            if (count > bestCount) {
                bestCount = count;
                bestAddress = a;
            }
        }
        return bestAddress;
    }

    public static int getPriorAddress(final Mapper mapper, final int address,
                                      final boolean officialsOnly) {

        if (address == 0) {
            return 0;
        }

        int startAddress = address - 7;
        if (startAddress < 0) {
            startAddress = 0;
        }

        int count = 0;
        final int[][] patterns = officialsOnly ? OFFICIAL_PATTERNS : ALL_PATTERNS;
        while (true) {
            count = countInstructions(mapper, startAddress, address, patterns);
            if (count > 0 || ++startAddress == address) {
                break;
            }
        }

        if (startAddress == address) {
            return address - 1;
        }

        while (count > 1) {
            startAddress = getSuccessiveAddress(mapper, startAddress, officialsOnly);
            count--;
        }

        return startAddress;
    }

    public static int getSuccessiveAddress(final Mapper mapper,
                                           final int address, final boolean officialsOnly) {
        final int a = address + getPositiveInstructionLength(mapper, address,
                officialsOnly ? OFFICIAL_PATTERNS : ALL_PATTERNS);
        return a > 0xFFFF ? address : a;
    }

    public static List<Instruction> disassemble(final CPU cpu, final int address,
                                                final int prior, final int successive, final boolean officialsOnly) {
        final List<Instruction> result = new ArrayList<>();
        disassemble(result, cpu, address, prior, successive, officialsOnly);
        return result;
    }

    public static void disassemble(final List<Instruction> result, final CPU cpu,
                                   final int address, final int prior, final int successive,
                                   final boolean officialsOnly) {
        if (result == null) {
            return;
        }
        result.clear();
        final int[][] patterns = officialsOnly ? OFFICIAL_PATTERNS : ALL_PATTERNS;
        findPriorInstructions(result, cpu, prior, address - (prior << 1) - 7,
                address, patterns);
        disassembleRange(result, cpu, address, 0, successive, patterns);

        if (result.isEmpty()) {
            return;
        }

        final int pc = cpu.register().pc();
        if (address == pc) {
            return;
        }

        final int minAddress = result.getFirst().getAddress();
        if (pc < minAddress) {
            return;
        }
        final int maxAddress = result.getLast().getAddress();
        if (pc > maxAddress) {
            return;
        }

        final int pcIndex = Collections.binarySearch(result,
                new Instruction(pc, 0, null), INSTRUCTION_COMPARATOR);
        if (pcIndex >= 0) {
            return;
        }

        final int pcInsert = -(pcIndex + 1);
        final int size = result.size();
        int padding = 7;
        do {
            disassemble(result, cpu, pc, pcInsert + padding,
                    size - pcInsert + padding, officialsOnly);
            padding += 3;
        } while (result.size() < size);

        while (result.size() > size) {
            final int diff0 = Math.abs(minAddress - result.get(0).getAddress());
            final int diff1 = Math.abs(maxAddress
                    - result.get(result.size() - 1).getAddress());
            if (diff0 > diff1) {
                result.remove(0);
            } else {
                result.remove(result.size() - 1);
            }
        }
    }

    private static Instruction disassemble(final int[] bytes, final int offset,
                                           final int[][] patterns) {

        final Instruction instruction = new Instruction();
        final StringBuilder sb = new StringBuilder();
        final int opcode = bytes[offset];

        final int[] pattern = patterns[opcode];
        final int length = LENGTHS[pattern[1]];
        final Map<Integer, AddressLabel> labs = labels;

        int b1 = 0;
        int b2 = 0;
        int descriptionLines = 1;

        if (length >= 2) {
            b1 = bytes[(offset + 1) % bytes.length];
        }
        if (length == 3) {
            b2 = bytes[(offset + 2) % bytes.length];
        }

        if (pattern[0] == MNEMONICS.length - 1) {
            sb.append(".BYTE ");
            append(sb, "$%02X", opcode);
        } else {
            instruction.setMnemonic(MNEMONICS[pattern[0]]);
            sb.append(instruction.getMnemonic()).append(' ');
            switch (pattern[1]) {
                case NONE:
                    break;
                case A:
                    sb.append('A');
                    break;
                case IMMEDIATE:
                    sb.append(String.format("#$%02X", b1));
                    break;
                case ZERO_PAGE:
                    if (BRANCHES[pattern[0]]) {
                        sb.append(String.format("$%02X", b1));
                        instruction.setBank(1);
                        instruction.setStart(2 + offset + (byte) b1);
                    } else {
                        sb.append(String.format("$%02X", b1));
                    }
                    break;
                case ZERO_PAGE_X: {
                    append(sb, "$%02X,X", b1);
                    break;
                }
                case ZERO_PAGE_Y: {
                    append(sb, "$%02X,Y", b1);
                    break;
                }
                case INDIRECT_X: {
                    append(sb, "($%02X,X)", b1);
                    break;
                }
                case INDIRECT_Y: {
                    append(sb, "($%02X),Y", b1);
                    break;
                }
                case ABSOLUTE: {
                    final int addr = (b2 << 8) | b1;
                    if (JUMPS[pattern[0]]) {
                        if (labs != null) {
                            final AddressLabel addressLabel = labs.get(
                                    AddressLabel.createKey(-1, addr));
                            if (addressLabel != null && addressLabel.isCode()) {
                                final String label = addressLabel.getLabel();
                                if (!isBlank(label)) {
                                    sb.append(label);
                                    break;
                                }
                            }
                        }
                    }
                    append(sb, "$%04X", addr);
                    break;
                }
                case INDIRECT: {
                    append(sb, "($%04X)", (b2 << 8) | b1);
                    break;
                }
                case ABSOLUTE_X: {
                    append(sb, "$%04X,X", (b2 << 8) | b1);
                    break;
                }
                case ABSOLUTE_Y: {
                    append(sb, "$%04X,Y", (b2 << 8) | b1);
                    break;
                }
            }
        }

        instruction.setAddress(offset);
        instruction.setLength(length == 0 ? 1 : length);
        instruction.setDescription(sb.toString());
        instruction.setDescriptionLines(descriptionLines);

        return instruction;
    }

    public static List<Instruction> disassemble(final int[] bytes,
                                                final boolean officialsOnly) {
        final List<Instruction> instructions = new ArrayList<>();
        disassemble(instructions, bytes, officialsOnly);
        return instructions;
    }

    public static void disassemble(final List<Instruction> instructions,
                                   final int[] bytes, final boolean officialsOnly) {

        final int[][] patterns = officialsOnly ? OFFICIAL_PATTERNS : ALL_PATTERNS;
        int offset = 0;
        while (offset < bytes.length) {
            final Instruction instruction = disassemble(bytes, offset, patterns);
            instructions.add(instruction);
            offset += instruction.getLength();
        }
        int labelIndex = 0;
        Map<Integer, Instruction> map = new HashMap<>();
        Map<Integer, String> labs = new HashMap<>();
        for (final Instruction instruction : instructions) {
            map.put(instruction.getAddress(), instruction);
        }
        for (final Instruction instruction : instructions) {
            if (instruction.getBank() == 1) {
                Instruction inst = null;
                if (instruction.getStart() == offset) {
                    inst = instructions.get(instructions.size() - 1);
                } else {
                    inst = map.get(instruction.getStart());
                }
                if (inst != null) {
                    String label = labs.get(instruction.getStart());
                    if (label == null) {
                        label = String.format("label%d", labelIndex++);
                        labs.put(instruction.getStart(), label);
                    }
                    instruction.setDescription(String.format("%s %s",
                            instruction.getMnemonic(), label));
                }
            }
        }
        for (final Instruction instruction : instructions) {
            if (instruction.getBank() == 1) {
                Instruction inst = null;
                boolean before = true;
                if (instruction.getStart() == offset) {
                    inst = instructions.get(instructions.size() - 1);
                    before = false;
                } else {
                    inst = map.get(instruction.getStart());
                }
                if (inst != null) {
                    final String label = labs.get(instruction.getStart());
                    if (inst.getDescriptionLines() == 1) {
                        inst.setDescriptionLines(2);
                        if (before) {
                            inst.setDescription(String.format("%s:%n%s", label,
                                    inst.getDescription()));
                        } else {
                            inst.setDescription(String.format("%s%n%s:",
                                    inst.getDescription(), label));
                        }
                    }
                }
            }
        }
    }
}
