package nintaco.assembler;

import nintaco.MessageException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nintaco.assembler.LabelType.*;
import static nintaco.util.StringUtil.isBlank;

public class Assembler {

    private static final Object[][] INSTRUCTIONS = {
            {"AAC", 2, 2, 0x2B, false}, {"ADC", 1, 2, 0x65, true},
            {"ADC", 1, 3, 0x6D, true}, {"ADC", 2, 2, 0x69, true},
            {"ADC", 4, 2, 0x75, true}, {"ADC", 4, 3, 0x7D, true},
            {"ADC", 5, 3, 0x79, true}, {"ADC", 6, 2, 0x61, true},
            {"ADC", 7, 2, 0x71, true}, {"AND", 1, 2, 0x25, true},
            {"AND", 1, 3, 0x2D, true}, {"AND", 2, 2, 0x29, true},
            {"AND", 4, 2, 0x35, true}, {"AND", 4, 3, 0x3D, true},
            {"AND", 5, 3, 0x39, true}, {"AND", 6, 2, 0x21, true},
            {"AND", 7, 2, 0x31, true}, {"ARR", 2, 2, 0x6B, false},
            {"ASL", 1, 1, 0x0A, true}, {"ASL", 1, 2, 0x06, true},
            {"ASL", 1, 3, 0x0E, true}, {"ASL", 4, 2, 0x16, true},
            {"ASL", 4, 3, 0x1E, true}, {"ASR", 2, 2, 0x4B, false},
            {"ATX", 2, 2, 0xAB, false}, {"AXS", 2, 2, 0xCB, false},
            {"BCC", 1, 2, 0x90, true}, {"BCS", 1, 2, 0xB0, true},
            {"BEQ", 1, 2, 0xF0, true}, {"BIT", 1, 2, 0x24, true},
            {"BIT", 1, 3, 0x2C, true}, {"BMI", 1, 2, 0x30, true},
            {"BNE", 1, 2, 0xD0, true}, {"BPL", 1, 2, 0x10, true},
            {"BRK", 0, 0, 0x00, true}, {"BVC", 1, 2, 0x50, true},
            {"BVS", 1, 2, 0x70, true}, {"CLC", 0, 0, 0x18, true},
            {"CLD", 0, 0, 0xD8, true}, {"CLI", 0, 0, 0x58, true},
            {"CLV", 0, 0, 0xB8, true}, {"CMP", 1, 2, 0xC5, true},
            {"CMP", 1, 3, 0xCD, true}, {"CMP", 2, 2, 0xC9, true},
            {"CMP", 4, 2, 0xD5, true}, {"CMP", 4, 3, 0xDD, true},
            {"CMP", 5, 3, 0xD9, true}, {"CMP", 6, 2, 0xC1, true},
            {"CMP", 7, 2, 0xD1, true}, {"CPX", 1, 2, 0xE4, true},
            {"CPX", 1, 3, 0xEC, true}, {"CPX", 2, 2, 0xE0, true},
            {"CPY", 1, 2, 0xC4, true}, {"CPY", 1, 3, 0xCC, true},
            {"CPY", 2, 2, 0xC0, true}, {"DCP", 1, 2, 0xC7, false},
            {"DCP", 1, 3, 0xCF, false}, {"DCP", 4, 2, 0xD7, false},
            {"DCP", 4, 3, 0xDF, false}, {"DCP", 5, 3, 0xDB, false},
            {"DCP", 6, 2, 0xC3, false}, {"DCP", 7, 2, 0xD3, false},
            {"DEC", 1, 2, 0xC6, true}, {"DEC", 1, 3, 0xCE, true},
            {"DEC", 4, 2, 0xD6, true}, {"DEC", 4, 3, 0xDE, true},
            {"DEX", 0, 0, 0xCA, true}, {"DEY", 0, 0, 0x88, true},
            {"DOP", 2, 2, 0xE2, false}, {"EOR", 1, 2, 0x45, true},
            {"EOR", 1, 3, 0x4D, true}, {"EOR", 2, 2, 0x49, true},
            {"EOR", 4, 2, 0x55, true}, {"EOR", 4, 3, 0x5D, true},
            {"EOR", 5, 3, 0x59, true}, {"EOR", 6, 2, 0x41, true},
            {"EOR", 7, 2, 0x51, true}, {"INC", 1, 2, 0xE6, true},
            {"INC", 1, 3, 0xEE, true}, {"INC", 4, 2, 0xF6, true},
            {"INC", 4, 3, 0xFE, true}, {"INX", 0, 0, 0xE8, true},
            {"INY", 0, 0, 0xC8, true}, {"ISB", 1, 2, 0xE7, false},
            {"ISB", 1, 3, 0xEF, false}, {"ISB", 4, 2, 0xF7, false},
            {"ISB", 4, 3, 0xFF, false}, {"ISB", 5, 3, 0xFB, false},
            {"ISB", 6, 2, 0xE3, false}, {"ISB", 7, 2, 0xF3, false},
            {"JMP", 1, 3, 0x4C, true}, {"JMP", 3, 3, 0x6C, true},
            {"JSR", 1, 3, 0x20, true}, {"KIL", 0, 0, 0xF2, false},
            {"LAR", 5, 3, 0xBB, false}, {"LAX", 1, 2, 0xA7, false},
            {"LAX", 1, 3, 0xAF, false}, {"LAX", 5, 2, 0xB7, false},
            {"LAX", 5, 3, 0xBF, false}, {"LAX", 6, 2, 0xA3, false},
            {"LAX", 7, 2, 0xB3, false}, {"LDA", 1, 2, 0xA5, true},
            {"LDA", 1, 3, 0xAD, true}, {"LDA", 2, 2, 0xA9, true},
            {"LDA", 4, 2, 0xB5, true}, {"LDA", 4, 3, 0xBD, true},
            {"LDA", 5, 3, 0xB9, true}, {"LDA", 6, 2, 0xA1, true},
            {"LDA", 7, 2, 0xB1, true}, {"LDX", 1, 2, 0xA6, true},
            {"LDX", 1, 3, 0xAE, true}, {"LDX", 2, 2, 0xA2, true},
            {"LDX", 5, 2, 0xB6, true}, {"LDX", 5, 3, 0xBE, true},
            {"LDY", 1, 2, 0xA4, true}, {"LDY", 1, 3, 0xAC, true},
            {"LDY", 2, 2, 0xA0, true}, {"LDY", 4, 2, 0xB4, true},
            {"LDY", 4, 3, 0xBC, true}, {"LSR", 1, 1, 0x4A, true},
            {"LSR", 1, 2, 0x46, true}, {"LSR", 1, 3, 0x4E, true},
            {"LSR", 4, 2, 0x56, true}, {"LSR", 4, 3, 0x5E, true},
            {"NOP", 0, 0, 0xEA, true}, {"NOP", 1, 2, 0x64, false},
            {"NOP", 1, 3, 0x0C, false}, {"NOP", 2, 2, 0x80, false},
            {"NOP", 4, 2, 0xF4, false}, {"NOP", 4, 3, 0xFC, false},
            {"ORA", 1, 2, 0x05, true}, {"ORA", 1, 3, 0x0D, true},
            {"ORA", 2, 2, 0x09, true}, {"ORA", 4, 2, 0x15, true},
            {"ORA", 4, 3, 0x1D, true}, {"ORA", 5, 3, 0x19, true},
            {"ORA", 6, 2, 0x01, true}, {"ORA", 7, 2, 0x11, true},
            {"PHA", 0, 0, 0x48, true}, {"PHP", 0, 0, 0x08, true},
            {"PLA", 0, 0, 0x68, true}, {"PLP", 0, 0, 0x28, true},
            {"RLA", 1, 2, 0x27, false}, {"RLA", 1, 3, 0x2F, false},
            {"RLA", 4, 2, 0x37, false}, {"RLA", 4, 3, 0x3F, false},
            {"RLA", 5, 3, 0x3B, false}, {"RLA", 6, 2, 0x23, false},
            {"RLA", 7, 2, 0x33, false}, {"ROL", 1, 1, 0x2A, true},
            {"ROL", 1, 2, 0x26, true}, {"ROL", 1, 3, 0x2E, true},
            {"ROL", 4, 2, 0x36, true}, {"ROL", 4, 3, 0x3E, true},
            {"ROR", 1, 1, 0x6A, true}, {"ROR", 1, 2, 0x66, true},
            {"ROR", 1, 3, 0x6E, true}, {"ROR", 4, 2, 0x76, true},
            {"ROR", 4, 3, 0x7E, true}, {"RRA", 1, 2, 0x67, false},
            {"RRA", 1, 3, 0x6F, false}, {"RRA", 4, 2, 0x77, false},
            {"RRA", 4, 3, 0x7F, false}, {"RRA", 5, 3, 0x7B, false},
            {"RRA", 6, 2, 0x63, false}, {"RRA", 7, 2, 0x73, false},
            {"RTI", 0, 0, 0x40, true}, {"RTS", 0, 0, 0x60, true},
            {"SAX", 1, 2, 0x87, false}, {"SAX", 1, 3, 0x8F, false},
            {"SAX", 5, 2, 0x97, false}, {"SAX", 6, 2, 0x83, false},
            {"SBC", 1, 2, 0xE5, true}, {"SBC", 1, 3, 0xED, true},
            {"SBC", 2, 2, 0xE9, true}, {"SBC", 4, 2, 0xF5, true},
            {"SBC", 4, 3, 0xFD, true}, {"SBC", 5, 3, 0xF9, true},
            {"SBC", 6, 2, 0xE1, true}, {"SBC", 7, 2, 0xF1, true},
            {"SEC", 0, 0, 0x38, true}, {"SED", 0, 0, 0xF8, true},
            {"SEI", 0, 0, 0x78, true}, {"SHA", 5, 3, 0x9F, false},
            {"SHA", 7, 2, 0x93, false}, {"SHX", 5, 3, 0x9E, false},
            {"SHY", 4, 3, 0x9C, false}, {"SLO", 1, 2, 0x07, false},
            {"SLO", 1, 3, 0x0F, false}, {"SLO", 4, 2, 0x17, false},
            {"SLO", 4, 3, 0x1F, false}, {"SLO", 5, 3, 0x1B, false},
            {"SLO", 6, 2, 0x03, false}, {"SLO", 7, 2, 0x13, false},
            {"SRE", 1, 2, 0x47, false}, {"SRE", 1, 3, 0x4F, false},
            {"SRE", 4, 2, 0x57, false}, {"SRE", 4, 3, 0x5F, false},
            {"SRE", 5, 3, 0x5B, false}, {"SRE", 6, 2, 0x43, false},
            {"SRE", 7, 2, 0x53, false}, {"STA", 1, 2, 0x85, true},
            {"STA", 1, 3, 0x8D, true}, {"STA", 4, 2, 0x95, true},
            {"STA", 4, 3, 0x9D, true}, {"STA", 5, 3, 0x99, true},
            {"STA", 6, 2, 0x81, true}, {"STA", 7, 2, 0x91, true},
            {"STX", 1, 2, 0x86, true}, {"STX", 1, 3, 0x8E, true},
            {"STX", 5, 2, 0x96, true}, {"STY", 1, 2, 0x84, true},
            {"STY", 1, 3, 0x8C, true}, {"STY", 4, 2, 0x94, true},
            {"TAX", 0, 0, 0xAA, true}, {"TAY", 0, 0, 0xA8, true},
            {"TSX", 0, 0, 0xBA, true}, {"TXA", 0, 0, 0x8A, true},
            {"TXS", 0, 0, 0x9A, true}, {"TYA", 0, 0, 0x98, true},
            {"XAA", 2, 2, 0x8B, false}, {"XAS", 5, 3, 0x9B, false},
    };

    private static final String[][] ALIASES = {
            {"AAC", "ANC"},
            {"SAX", "AAX", "AXS"},
            {"ASR", "ALR"},
            {"ATX", "LXA", "OAL"},
            {"AXA", "SHA", "AXA"},
            {"AXS", "SBX", "SAX"},
            {"DCP", "DCM"},
            {"ISC", "ISB", "INS"},
            {"KIL", "JAM", "HLT"},
            {"LAR", "LAE", "LAS"},
            {"NOP", "TOP", "SKW", "DOP", "SKB"},
            {"SLO", "ASO"},
            {"SRE", "LSE"},
            {"SXA", "SHX", "XAS"},
            {"SYA", "SHY", "SAY"},
            {"XAA", "ANE"},
            {"XAS", "SHS", "TAS"},
    };

    private static final String[] BRANCH_NAMES = {
            "BPL", "BMI", "BVC", "BVS", "BCC", "BCS", "BNE", "BEQ",
    };

    private static final String[] PATTERN_STRINGS = {
            "MNE",
            "MNE L",
            "MNE #L",
            "MNE ( L )",
            "MNE L , X",
            "MNE L , Y",
            "MNE ( L , X )",
            "MNE ( L ) , Y",
    };
    private static final Pattern[] PATTERNS = new Pattern[PATTERN_STRINGS.length];
    private static final Map<InstructionKey, Integer> officialOpcodes
            = new HashMap<>();
    private static final Map<InstructionKey, Integer> allOpcodes
            = new HashMap<>();
    private static final Set<String> branchNames = new HashSet<>();

    private static final String DATA_STRING = "\\s+#?([\\$|%|-]?\\w+)";
    private static final Pattern BYTE_PATTERN = Pattern.compile("\\.BYTE"
            + DATA_STRING);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\.WORD"
            + DATA_STRING);

    static {

        Collections.addAll(branchNames, BRANCH_NAMES);

        final Map<String, String[]> aliases = new HashMap<>();
        for (final String[] as : ALIASES) {
            for (final String alias : as) {
                aliases.put(alias, as);
            }
        }

        for (int i = PATTERN_STRINGS.length - 1; i >= 0; i--) {
            PATTERNS[i] = Pattern.compile(PATTERN_STRINGS[i]
                    .replaceAll("\\(", "\\\\(")
                    .replaceAll("\\)", "\\\\)")
                    .replaceAll("\\s+", "\\\\s*")
                    .replaceAll("MNE", "(\\\\p{Alpha}{3})")
                    .replaceAll("L", "([\\\\\\$|%|-]?\\\\w+)"));
        }

        final String[] list = new String[1];
        for (final Object[] instruction : INSTRUCTIONS) {
            final String mnemonic = (String) instruction[0];
            String[] as = aliases.get(mnemonic);
            if (as == null) {
                as = list;
                as[0] = mnemonic;
            }
            for (final String a : as) {
                InstructionKey key = new InstructionKey(a, (int) instruction[1],
                        (int) instruction[2]);
                while (true) {
                    final int opCode = (int) instruction[3];
                    allOpcodes.put(key, opCode);
                    if ((boolean) instruction[4]) {
                        officialOpcodes.put(key, opCode);
                    }
                    if (key.getLabelType() == A) {
                        key = new InstructionKey(key.getMnemonic(), key.getPatternIndex(),
                                NONE);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public static int[] assemble(final String code) throws MessageException {

        final List<Integer> hex = new ArrayList<>();
        final CodeLabel codeLabel = new CodeLabel();
        final InstructionKey key = new InstructionKey();

        if (code != null) {
            final String[] lines = code.split("\n|\r");
            final Map<String, Integer> labels = new HashMap<>();
            for (final String line : lines) {
                processLine(hex, line, codeLabel, key, labels, true);
            }
            hex.clear();
            for (final String line : lines) {
                processLine(hex, line, codeLabel, key, labels, false);
            }
        }

        final int[] values = new int[hex.size()];
        for (int i = 0; i < hex.size(); i++) {
            values[i] = hex.get(i);
        }
        return values;
    }

    private static CodeLabel parseLabel(final String label) {
        final CodeLabel codeLabel = new CodeLabel();
        parseLabel(label, codeLabel);
        return codeLabel;
    }

    private static void parseBase(final String label, final CodeLabel codeLabel,
                                  final int base, final boolean isWord) {
        try {
            int value = Integer.parseInt(label, base);
            if (base == 10 && value < 0) {
                if (isWord) {
                    value &= 0xFFFF;
                } else if (value >= Byte.MIN_VALUE) {
                    value &= 0xFF;
                } else if (value >= Short.MIN_VALUE) {
                    value &= 0xFFFF;
                }
            }
            codeLabel.setValue(value);
            if (value >= 0) {
                if (value <= 0xFF && !isWord) {
                    codeLabel.setType(BYTE);
                    if (base == 2) {
                        if (label.length() > 8) {
                            codeLabel.setType(WORD);
                        }
                    } else if (base == 16) {
                        if (label.length() > 2) {
                            codeLabel.setType(WORD);
                        }
                    }
                } else if (value <= 0xFFFF) {
                    codeLabel.setType(WORD);
                }
            }
        } catch (final Throwable t) {
        }
    }

    private static void parseLabel(final String label,
                                   final CodeLabel codeLabel) {
        parseLabel(label, codeLabel, false);
    }

    private static void parseLabel(final String label,
                                   final CodeLabel codeLabel, final boolean isWord) {

        codeLabel.setType(NONE);
        if (isBlank(label)) {
            return;
        }
        if (label.equals("A")) {
            codeLabel.setType(A);
        } else if (label.startsWith("$")) {
            parseBase(label.substring(1), codeLabel, 16, isWord);
        } else if (label.startsWith("%")) {
            parseBase(label.substring(1), codeLabel, 2, isWord);
        } else if (label.startsWith("0")) {
            parseBase(label.equals("0") ? label : label.substring(1),
                    codeLabel, 8, isWord);
        } else if (label.charAt(0) == '-' || Character.isDigit(label.charAt(0))) {
            parseBase(label, codeLabel, 10, isWord);
        } else {
            codeLabel.setType(NAME);
            codeLabel.setName(label);
        }
    }

    private static void processLine(final List<Integer> hex, String line,
                                    final CodeLabel codeLabel, final InstructionKey key,
                                    final Map<String, Integer> labels, final boolean findingLabels)
            throws MessageException {

        codeLabel.setType(NONE);
        codeLabel.setName(null);

        if (line == null) {
            return;
        }
        line = line.trim().toUpperCase(Locale.ENGLISH);
        if (line.isEmpty()) {
            return;
        }

        final int semicolonIndex = line.indexOf(';');
        if (semicolonIndex >= 0) {
            line = line.substring(0, semicolonIndex).trim();
            if (line.isEmpty()) {
                return;
            }
        }

        final int colonIndex = line.indexOf(":");
        if (colonIndex >= 0) {
            if (findingLabels) {
                final String label = line.substring(0, colonIndex);
                if (labels.containsKey(label)) {
                    throwParseException(line, "Duplicate label: " + label);
                }
                labels.put(label, hex.size());
            }
            line = line.substring(colonIndex + 1).trim();
            if (line.isEmpty()) {
                return;
            }
        }

        Matcher matcher = null;
        matcher = BYTE_PATTERN.matcher(line);
        if (matcher.find()) {
            parseLabel(matcher.group(1), codeLabel);
            if (codeLabel.getType() != BYTE) {
                throwParseException(line, "Invalid byte.");
            }
            hex.add(codeLabel.getValue() & 0xFF);
            return;
        }
        matcher = WORD_PATTERN.matcher(line);
        if (matcher.find()) {
            parseLabel(matcher.group(1), codeLabel, true);
            if (codeLabel.getType() != WORD) {
                throwParseException(line, "Invalid word.");
            }
            hex.add(codeLabel.getValue() & 0xFF);
            hex.add((codeLabel.getValue() >> 8) & 0xFF);
            return;
        }

        int patternIndex = 0;
        outer:
        {
            for (int i = PATTERNS.length - 1; i >= 0; i--) {
                matcher = PATTERNS[i].matcher(line);
                if (matcher.find()) {
                    patternIndex = i;
                    break outer;
                }
            }
            throwParseException(line, "Unrecognized syntax.");
        }

        final String mnemonic = matcher.group(1);
        final boolean isBranchInstruction = branchNames.contains(mnemonic);

        int labelType = 0;
        if (patternIndex > 0) {
            parseLabel(matcher.group(2), codeLabel);
            labelType = codeLabel.getType();
        }
        key.setMnemonic(mnemonic);
        key.setPatternIndex(patternIndex);
        key.setLabelType(labelType);

        boolean isLabeledBranch = false;
        if (codeLabel.getType() == NAME) {
            if (isBranchInstruction && patternIndex == 1) {
                key.setLabelType(BYTE);
                isLabeledBranch = true;
            } else {
                throwParseException(line, "Label support is limited to conditional "
                        + "branch instructions.");
            }
        }

        Integer opCode = officialOpcodes.get(key);
        if (opCode == null) {
            opCode = allOpcodes.get(key);
        }
        if (opCode == null && labelType == BYTE) {
            labelType = WORD;
            key.setLabelType(labelType);
            opCode = officialOpcodes.get(key);
            if (opCode == null) {
                opCode = allOpcodes.get(key);
            }
        }

        if (opCode == null) {
            throwParseException(line, "Unknown mnemonic or missing operand.");
        }


        hex.add(opCode);
        if (isLabeledBranch) {
            Integer offset = labels.get(codeLabel.getName());
            if (offset == null && findingLabels) {
                offset = 0;
            }
            if (offset == null) {
                throwParseException(line, "Unknown label: <code>"
                        + codeLabel.getName() + "</code>");
            } else {
                final int value = offset - hex.size() - 1;
                if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                    throwParseException(line, "Label is out of range.");
                } else {
                    hex.add(value & 0xFF);
                }
            }
        } else {
            switch (labelType) {
                case BYTE:
                    hex.add(codeLabel.getValue() & 0xFF);
                    break;
                case WORD:
                    hex.add(codeLabel.getValue() & 0xFF);
                    hex.add((codeLabel.getValue() >> 8) & 0xFF);
                    break;
            }
        }
    }

    private static void throwParseException(final String line,
                                            final String reason) throws MessageException {
        throw new MessageException("<html>Failed to parse: <code>%s</code>"
                + "<br/>%s</html>", line, reason);
    }
}
