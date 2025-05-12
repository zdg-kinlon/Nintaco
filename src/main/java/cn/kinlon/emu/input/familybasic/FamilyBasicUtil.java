package cn.kinlon.emu.input.familybasic;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.MessageException;
import cn.kinlon.emu.mappers.Mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static cn.kinlon.emu.utils.MathUtil.clamp;
import static cn.kinlon.emu.utils.StringUtil.isBlank;

public final class FamilyBasicUtil {

    private static final String[] TOKENS = new String[256];

    static {
        TOKENS[0x80] = "GOTO";
        TOKENS[0x81] = "GOSUB";
        TOKENS[0x82] = "RUN";
        TOKENS[0x83] = "RETURN";
        TOKENS[0x84] = "RESTORE";
        TOKENS[0x85] = "THEN";
        TOKENS[0x86] = "LIST";
        TOKENS[0x87] = "SYSTEM";
        TOKENS[0x88] = "TO";
        TOKENS[0x89] = "STEP";
        TOKENS[0x8A] = "SPRITE";
        TOKENS[0x8B] = "PRINT";
        TOKENS[0x8C] = "FOR";
        TOKENS[0x8D] = "NEXT";
        TOKENS[0x8E] = "PAUSE";
        TOKENS[0x8F] = "INPUT";
        TOKENS[0x90] = "LINPUT";
        TOKENS[0x91] = "DATA";
        TOKENS[0x92] = "IF";
        TOKENS[0x93] = "READ";
        TOKENS[0x94] = "DIM";
        TOKENS[0x95] = "REM";
        TOKENS[0x96] = "STOP";
        TOKENS[0x97] = "CONT";
        TOKENS[0x98] = "CLS";
        TOKENS[0x99] = "CLEAR";
        TOKENS[0x9A] = "ON";
        TOKENS[0x9B] = "OFF";
        TOKENS[0x9C] = "CUT";
        TOKENS[0x9D] = "NEW";
        TOKENS[0x9E] = "POKE";
        TOKENS[0x9F] = "CGSET";
        TOKENS[0xA0] = "VIEW";
        TOKENS[0xA1] = "MOVE";
        TOKENS[0xA2] = "END";
        TOKENS[0xA3] = "PLAY";
        TOKENS[0xA4] = "BEEP";
        TOKENS[0xA5] = "LOAD";
        TOKENS[0xA6] = "SAVE";
        TOKENS[0xA7] = "POSITION";
        TOKENS[0xA8] = "KEY";
        TOKENS[0xA9] = "COLOR";
        TOKENS[0xAA] = "DEF";
        TOKENS[0xAB] = "CGEN";
        TOKENS[0xAC] = "SWAP";
        TOKENS[0xAD] = "CALL";
        TOKENS[0xAE] = "LOCATE";
        TOKENS[0xAF] = "PALET";
        TOKENS[0xB0] = "ERA";
        TOKENS[0xB1] = "TR";
        TOKENS[0xB2] = "FIND";
        TOKENS[0xB3] = "GAME";
        TOKENS[0xB4] = "BGTOOL";
        TOKENS[0xB5] = "AUTO";
        TOKENS[0xB6] = "DELETE";
        TOKENS[0xB7] = "RENUM";
        TOKENS[0xB8] = "FILTER";
        TOKENS[0xB9] = "CLICK";
        TOKENS[0xBA] = "SCREEN";
        TOKENS[0xBB] = "BACKUP";
        TOKENS[0xBC] = "ERROR";
        TOKENS[0xBD] = "RESUME";
        TOKENS[0xBE] = "BGPUT";
        TOKENS[0xBF] = "BGGET";
        TOKENS[0xC0] = "CAN";

        TOKENS[0xCA] = "ABS";
        TOKENS[0xCB] = "ASC";
        TOKENS[0xCC] = "STR$";
        TOKENS[0xCD] = "FRE";
        TOKENS[0xCE] = "LEN";
        TOKENS[0xCF] = "PEEK";
        TOKENS[0xD0] = "RND";
        TOKENS[0xD1] = "SGN";
        TOKENS[0xD2] = "SPC";
        TOKENS[0xD3] = "TAB";
        TOKENS[0xD4] = "MID$";
        TOKENS[0xD5] = "STICK";
        TOKENS[0xD6] = "STRIG";
        TOKENS[0xD7] = "XPOS";
        TOKENS[0xD8] = "YPOS";
        TOKENS[0xD9] = "VAL";
        TOKENS[0xDA] = "POS";
        TOKENS[0xDB] = "CSRLIN";
        TOKENS[0xDC] = "CHR$";
        TOKENS[0xDD] = "HEX$";
        TOKENS[0xDE] = "INKEY$";
        TOKENS[0xDF] = "RIGHT$";
        TOKENS[0xE0] = "LEFT$";
        TOKENS[0xE1] = "SCR$";
        TOKENS[0xE2] = "INSTR";
        TOKENS[0xE3] = "CRASH";
        TOKENS[0xE4] = "ERR";
        TOKENS[0xE5] = "ERL";
        TOKENS[0xE6] = "VCT";

        TOKENS[0xEF] = "XOR";
        TOKENS[0xF0] = "OR";
        TOKENS[0xF1] = "AND";
        TOKENS[0xF2] = "NOT";
        TOKENS[0xF3] = "<>";
        TOKENS[0xF4] = ">=";
        TOKENS[0xF5] = "<=";
        TOKENS[0xF6] = "=";
        TOKENS[0xF7] = ">";
        TOKENS[0xF8] = "<";
        TOKENS[0xF9] = "+";
        TOKENS[0xFA] = "-";
        TOKENS[0xFB] = "MOD";
        TOKENS[0xFC] = "/";
        TOKENS[0xFD] = "*";
    }

    private FamilyBasicUtil() {
    }

    private static boolean isLetterOrNumber(final int value) {
        return (value >= 'A' && value <= 'Z')
                || (value >= '0' && value <= '9')
                || (value >= 'a' && value <= 'z');
    }

    private static void extractToken(final String line, final int lineNumber,
                                     final int start, final Token token) throws MessageException {

        final char c0 = charAt(line, start);
        final char c1 = charAt(line, start + 1);

        if (c0 == '&' && (c1 == 'H' || c1 == 'h')) {
            extractHex(line, lineNumber, start, token);
        } else if (c0 >= '0' && c0 <= '9') {
            if (token.type == TokenType.CHR && isLetterOrNumber(token.value)) {
                extractChr(line, lineNumber, start, token);
            } else {
                extractDec(line, lineNumber, start, token);
            }
        } else if (c0 == '"') {
            extractStr(line, lineNumber, start, token);
        } else if (!extractTkn(line, lineNumber, start, token)) {
            extractChr(line, lineNumber, start, token);
        }
    }

    private static void extractChr(String line, final int lineNumber,
                                   final int start, final Token token) throws MessageException {

        token.type = TokenType.CHR;
        token.next = start + 1;
        token.value = charAt(line, start);
    }

    private static boolean extractTkn(String line, final int lineNumber,
                                      final int start, final Token token) throws MessageException {

        line = line.toUpperCase(Locale.ENGLISH);
        for (int i = 0x80; i <= 0xFD; i++) {
            String t = TOKENS[i];
            if (t != null) {
                final char c = t.charAt(0);
                final boolean isWord = c >= 'A' && c <= 'Z';
                int j = t.length() - 1;
                while (true) {
                    if (line.startsWith(t, start)) {
                        token.type = TokenType.TKN;
                        token.str = TOKENS[i];
                        token.value = i;
                        token.next = start + t.length();
                        return true;
                    }
                    if (!isWord || j == 0) {
                        break;
                    }
                    t = TOKENS[i].substring(0, j) + ".";
                    j--;
                }
            }
        }

        return false;
    }

    private static void extractData(final String line, final int lineNumber,
                                    final int start, final Token token) throws MessageException {

        token.type = TokenType.DAT;
        token.next = line.length();

        final StringBuffer sb = new StringBuffer();
        boolean insideString = false;
        for (int i = start; i < line.length(); i++) {
            final char c = charAt(line, i);
            if (!insideString && c == ':') {
                token.next = i;
                break;
            } else if (c == '"') {
                insideString = !insideString;
            }
            sb.append(c);
        }

        token.str = sb.toString();
    }

    private static void extractRemark(final String line, final int lineNumber,
                                      final int start, final Token token) throws MessageException {

        token.type = TokenType.REM;
        token.next = line.length();
        token.str = line.substring(start);
    }

    private static void extractStr(final String line, final int lineNumber,
                                   final int start, final Token token) throws MessageException {

        final StringBuffer sb = new StringBuffer();
        boolean endQuote = false;
        for (int i = start + 1; i < line.length(); i++) {
            final char c = charAt(line, i);
            if (c == '"') {
                endQuote = true;
                break;
            } else {
                sb.append(c);
            }
        }
        token.type = TokenType.STR;
        token.str = sb.toString();
        token.value = endQuote ? 1 : 0;
        token.next = start + sb.length() + 2;
    }

    private static void extractDec(final String line, final int lineNumber,
                                   final int start, final Token token) throws MessageException {

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 5; i++) {
            final char c = charAt(line, start + i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            } else {
                break;
            }
        }
        boolean error = sb.length() == 0;
        if (!error) {
            token.type = TokenType.DEC;
            token.next = start + sb.length();
            try {
                token.value = Integer.parseInt(sb.toString());
            } catch (final Throwable t) {
                error = true;
            }
            if (token.value < 0 || token.value > 32768) {
                throw new MessageException("%d: Decimal value out of range.",
                        lineNumber);
            }
        }
        if (error) {
            throw new MessageException("%d: Invalid decimal value.", lineNumber);
        }
    }

    private static void extractHex(final String line, final int lineNumber,
                                   final int start, final Token token) throws MessageException {

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            final char c = charAt(line, start + 2 + i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')
                    || (c >= 'a' && c <= 'f')) {
                sb.append(c);
            } else {
                break;
            }
        }
        boolean error = sb.length() == 0;
        if (!error) {
            token.type = TokenType.HEX;
            token.next = start + 2 + sb.length();
            try {
                token.value = Integer.parseInt(sb.toString(), 16);
            } catch (final Throwable t) {
                error = true;
            }
            if (token.value < 0x0000 || token.value > 0xFFFF) {
                throw new MessageException("%d: Hexadecimal value out of range.",
                        lineNumber);
            }
        }
        if (error) {
            throw new MessageException("%d: Invalid hexadecimal value.", lineNumber);
        }
    }

    private static char charAt(final String line, final int index) {
        if (index < 0 || index >= line.length()) {
            return 0;
        } else {
            return line.charAt(index);
        }
    }

    public static char hexToChar(final int hexCode) {
        switch (hexCode) {
            case 0x00:
                return '\u0000';

            case 0x20:
                return ' ';
            case 0x21:
                return '!';
            case 0x22:
                return '"';
            case 0x23:
                return '#';
            case 0x24:
                return '$';
            case 0x25:
                return '%';
            case 0x26:
                return '&';
            case 0x27:
                return '\'';
            case 0x28:
                return '(';
            case 0x29:
                return ')';
            case 0x2a:
                return '*';
            case 0x2b:
                return '+';
            case 0x2c:
                return ',';
            case 0x2d:
                return '-';
            case 0x2e:
                return '.';
            case 0x2f:
                return '/';

            case 0x30:
                return '0';
            case 0x31:
                return '1';
            case 0x32:
                return '2';
            case 0x33:
                return '3';
            case 0x34:
                return '4';
            case 0x35:
                return '5';
            case 0x36:
                return '6';
            case 0x37:
                return '7';
            case 0x38:
                return '8';
            case 0x39:
                return '9';

            case 0x3a:
                return ':';
            case 0x3b:
                return ';';
            case 0x3c:
                return '<';
            case 0x3d:
                return '=';
            case 0x3e:
                return '>';
            case 0x3f:
                return '?';
            case 0x40:
                return '@';

            case 0x41:
                return 'A';
            case 0x42:
                return 'B';
            case 0x43:
                return 'C';
            case 0x44:
                return 'D';
            case 0x45:
                return 'E';
            case 0x46:
                return 'F';
            case 0x47:
                return 'G';
            case 0x48:
                return 'H';
            case 0x49:
                return 'I';
            case 0x4a:
                return 'J';
            case 0x4b:
                return 'K';
            case 0x4c:
                return 'L';
            case 0x4d:
                return 'M';
            case 0x4e:
                return 'N';
            case 0x4f:
                return 'O';
            case 0x50:
                return 'P';
            case 0x51:
                return 'Q';
            case 0x52:
                return 'R';
            case 0x53:
                return 'S';
            case 0x54:
                return 'T';
            case 0x55:
                return 'U';
            case 0x56:
                return 'V';
            case 0x57:
                return 'W';
            case 0x58:
                return 'X';
            case 0x59:
                return 'Y';
            case 0x5a:
                return 'Z';

            case 0x5b:
                return '\u300c';
            case 0x5c:
                return '\u00a5';
            case 0x5d:
                return '\u300d';

            case 0x5e:
                return '^';
            case 0x5f:
                return '_';

            case 0x60:
                return '\u30a2';
            case 0x61:
                return '\u30a4';
            case 0x62:
                return '\u30a6';
            case 0x63:
                return '\u30a8';
            case 0x64:
                return '\u30aa';
            case 0x65:
                return '\u30ab';
            case 0x66:
                return '\u30ad';
            case 0x67:
                return '\u30af';
            case 0x68:
                return '\u30b1';
            case 0x69:
                return '\u30b3';
            case 0x6a:
                return '\u30b5';
            case 0x6b:
                return '\u30b7';
            case 0x6c:
                return '\u30b9';
            case 0x6d:
                return '\u30bb';
            case 0x6e:
                return '\u30bd';
            case 0x6f:
                return '\u30bf';
            case 0x70:
                return '\u30c1';
            case 0x71:
                return '\u30c4';
            case 0x72:
                return '\u30c6';
            case 0x73:
                return '\u30c8';
            case 0x74:
                return '\u30ca';
            case 0x75:
                return '\u30cb';
            case 0x76:
                return '\u30cc';
            case 0x77:
                return '\u30cd';
            case 0x78:
                return '\u30ce';
            case 0x79:
                return '\u30cf';
            case 0x7a:
                return '\u30d2';
            case 0x7b:
                return '\u30d5';
            case 0x7c:
                return '\u30d8';
            case 0x7d:
                return '\u30db';
            case 0x7e:
                return '\u30de';
            case 0x7f:
                return '\u30df';
            case 0x80:
                return '\u30e0';
            case 0x81:
                return '\u30e1';
            case 0x82:
                return '\u30e2';
            case 0x83:
                return '\u30e4';
            case 0x84:
                return '\u30e6';
            case 0x85:
                return '\u30e8';
            case 0x86:
                return '\u30e9';
            case 0x87:
                return '\u30ea';
            case 0x88:
                return '\u30eb';
            case 0x89:
                return '\u30ec';
            case 0x8a:
                return '\u30ed';
            case 0x8b:
                return '\u30ef';
            case 0x8c:
                return '\u30f3';
            case 0x8d:
                return '\u30f2';
            case 0x8e:
                return '\u30a1';
            case 0x8f:
                return '\u30a3';
            case 0x90:
                return '\u30a5';
            case 0x91:
                return '\u30a7';
            case 0x92:
                return '\u30a9';
            case 0x93:
                return '\u30e3';
            case 0x94:
                return '\u30e5';
            case 0x95:
                return '\u30e7';
            case 0x96:
                return '\u30c3';
            case 0x97:
                return '\u30ac';
            case 0x98:
                return '\u30ae';
            case 0x99:
                return '\u30b0';
            case 0x9a:
                return '\u30b2';
            case 0x9b:
                return '\u30b4';
            case 0x9c:
                return '\u30b6';
            case 0x9d:
                return '\u30b8';
            case 0x9e:
                return '\u30ba';
            case 0x9f:
                return '\u30bc';
            case 0xa0:
                return '\u30be';
            case 0xa1:
                return '\u30c0';
            case 0xa2:
                return '\u30c2';
            case 0xa3:
                return '\u30c5';
            case 0xa4:
                return '\u30c7';
            case 0xa5:
                return '\u30c9';
            case 0xa6:
                return '\u30d0';
            case 0xa7:
                return '\u30d3';
            case 0xa8:
                return '\u30d6';
            case 0xa9:
                return '\u30d9';
            case 0xaa:
                return '\u30dc';
            case 0xab:
                return '\u30d1';
            case 0xac:
                return '\u30d4';
            case 0xad:
                return '\u30d7';
            case 0xae:
                return '\u30da';
            case 0xaf:
                return '\u30dd';
            case 0xb0:
                return '\u25a1';
            case 0xb1:
                return '\u3002';

            case 0xb2:
                return '[';
            case 0xb3:
                return ']';

            case 0xb4:
                return '\u00a9';
            case 0xb5:
                return '\u00d7';
            case 0xb6:
                return '\u00f7';

            default:
                return '?';
        }
    }

    public static int charToHex(final char ch) {
        switch (ch) {
            case '\u0000':
                return 0x00;

            case ' ':
                return 0x20;
            case '!':
                return 0x21;
            case '"':
                return 0x22;
            case '#':
                return 0x23;
            case '$':
                return 0x24;
            case '%':
                return 0x25;
            case '&':
                return 0x26;
            case '\'':
                return 0x27;
            case '(':
                return 0x28;
            case ')':
                return 0x29;
            case '*':
                return 0x2a;
            case '+':
                return 0x2b;
            case ',':
                return 0x2c;
            case '-':
                return 0x2d;
            case '.':
                return 0x2e;
            case '/':
                return 0x2f;

            case '0':
                return 0x30;
            case '1':
                return 0x31;
            case '2':
                return 0x32;
            case '3':
                return 0x33;
            case '4':
                return 0x34;
            case '5':
                return 0x35;
            case '6':
                return 0x36;
            case '7':
                return 0x37;
            case '8':
                return 0x38;
            case '9':
                return 0x39;

            case ':':
            case '\uFF1A':
                return 0x3a;
            case ';':
                return 0x3b;
            case '<':
                return 0x3c;
            case '=':
                return 0x3d;
            case '>':
                return 0x3e;
            case '?':
                return 0x3f;
            case '@':
                return 0x40;

            case 'A':
            case 'a':
                return 0x41;
            case 'B':
            case 'b':
                return 0x42;
            case 'C':
            case 'c':
                return 0x43;
            case 'D':
            case 'd':
                return 0x44;
            case 'E':
            case 'e':
                return 0x45;
            case 'F':
            case 'f':
                return 0x46;
            case 'G':
            case 'g':
                return 0x47;
            case 'H':
            case 'h':
                return 0x48;
            case 'I':
            case 'i':
                return 0x49;
            case 'J':
            case 'j':
                return 0x4a;
            case 'K':
            case 'k':
                return 0x4b;
            case 'L':
            case 'l':
                return 0x4c;
            case 'M':
            case 'm':
                return 0x4d;
            case 'N':
            case 'n':
                return 0x4e;
            case 'O':
            case 'o':
                return 0x4f;
            case 'P':
            case 'p':
                return 0x50;
            case 'Q':
            case 'q':
                return 0x51;
            case 'R':
            case 'r':
                return 0x52;
            case 'S':
            case 's':
                return 0x53;
            case 'T':
            case 't':
                return 0x54;
            case 'U':
            case 'u':
                return 0x55;
            case 'V':
            case 'v':
                return 0x56;
            case 'W':
            case 'w':
                return 0x57;
            case 'X':
            case 'x':
                return 0x58;
            case 'Y':
            case 'y':
                return 0x59;
            case 'Z':
            case 'z':
                return 0x5a;

            case '\u300c':
                return 0x5b;
            case '\u00a5':
            case '\uffe5':
                return 0x5c;
            case '\u300d':
                return 0x5d;

            case '^':
                return 0x5e;
            case '_':
                return 0x5f;

            case '\u30a2':
                return 0x60;
            case '\u30a4':
                return 0x61;
            case '\u30a6':
                return 0x62;
            case '\u30a8':
                return 0x63;
            case '\u30aa':
                return 0x64;
            case '\u30ab':
                return 0x65;
            case '\u30ad':
                return 0x66;
            case '\u30af':
                return 0x67;
            case '\u30b1':
                return 0x68;
            case '\u30b3':
                return 0x69;
            case '\u30b5':
                return 0x6a;
            case '\u30b7':
                return 0x6b;
            case '\u30b9':
                return 0x6c;
            case '\u30bb':
                return 0x6d;
            case '\u30bd':
                return 0x6e;
            case '\u30bf':
                return 0x6f;
            case '\u30c1':
                return 0x70;
            case '\u30c4':
                return 0x71;
            case '\u30c6':
                return 0x72;
            case '\u30c8':
                return 0x73;
            case '\u30ca':
                return 0x74;
            case '\u30cb':
                return 0x75;
            case '\u30cc':
                return 0x76;
            case '\u30cd':
                return 0x77;
            case '\u30ce':
                return 0x78;
            case '\u30cf':
                return 0x79;
            case '\u30d2':
                return 0x7a;
            case '\u30d5':
                return 0x7b;
            case '\u30d8':
                return 0x7c;
            case '\u30db':
                return 0x7d;
            case '\u30de':
                return 0x7e;
            case '\u30df':
                return 0x7f;
            case '\u30e0':
                return 0x80;
            case '\u30e1':
                return 0x81;
            case '\u30e2':
                return 0x82;
            case '\u30e4':
                return 0x83;
            case '\u30e6':
                return 0x84;
            case '\u30e8':
                return 0x85;
            case '\u30e9':
                return 0x86;
            case '\u30ea':
                return 0x87;
            case '\u30eb':
                return 0x88;
            case '\u30ec':
                return 0x89;
            case '\u30ed':
                return 0x8a;
            case '\u30ef':
                return 0x8b;
            case '\u30f3':
                return 0x8c;
            case '\u30f2':
                return 0x8d;
            case '\u30a1':
                return 0x8e;
            case '\u30a3':
                return 0x8f;
            case '\u30a5':
                return 0x90;
            case '\u30a7':
                return 0x91;
            case '\u30a9':
                return 0x92;
            case '\u30e3':
                return 0x93;
            case '\u30e5':
                return 0x94;
            case '\u30e7':
                return 0x95;
            case '\u30c3':
                return 0x96;
            case '\u30ac':
                return 0x97;
            case '\u30ae':
                return 0x98;
            case '\u30b0':
                return 0x99;
            case '\u30b2':
                return 0x9a;
            case '\u30b4':
                return 0x9b;
            case '\u30b6':
                return 0x9c;
            case '\u30b8':
                return 0x9d;
            case '\u30ba':
                return 0x9e;
            case '\u30bc':
                return 0x9f;
            case '\u30be':
                return 0xa0;
            case '\u30c0':
                return 0xa1;
            case '\u30c2':
                return 0xa2;
            case '\u30c5':
                return 0xa3;
            case '\u30c7':
                return 0xa4;
            case '\u30c9':
                return 0xa5;
            case '\u30d0':
                return 0xa6;
            case '\u30d3':
                return 0xa7;
            case '\u30d6':
                return 0xa8;
            case '\u30d9':
                return 0xa9;
            case '\u30dc':
                return 0xaa;
            case '\u30d1':
                return 0xab;
            case '\u30d4':
                return 0xac;
            case '\u30d7':
                return 0xad;
            case '\u30da':
                return 0xae;
            case '\u30dd':
                return 0xaf;
            case '\u25a1':
                return 0xb0;
            case '\u3002':
                return 0xb1;

            case '[':
                return 0xb2;
            case ']':
                return 0xb3;

            case '\u00a9':
                return 0xb4;
            case '\u00d7':
                return 0xb5;
            case '\u00f7':
                return 0xb6;

            default:
                return -1;
        }
    }

    public static String copyProgram() {

        final Machine machine = App.getMachine();
        if (machine == null) {
            return "";
        }
        final Mapper mapper = machine.getMapper();
        int address = clamp(mapper.peekWord(0x0005), 0x6000, 0x7FFF);
        final int endAddress = clamp(mapper.peekWord(0x0007) - 1, 0x6000, 0x7FFF);
        final boolean version3 = address < 0x7000;

        final StringBuilder sb = new StringBuilder();
        while (address < endAddress) {
            boolean insideString = false;
            final int lineLength = mapper.peekCpuMemory(address++) - 4;
            sb.append(String.format("%d ", mapper.peekWord(address)));
            address += 2;
            final int end = address + lineLength;
            while (address < end) {
                if (insideString) {
                    final char c = hexToChar(mapper.peekCpuMemory(address++));
                    sb.append(c);
                    if (c == '"') {
                        insideString = false;
                    }
                } else {
                    final int hex = mapper.peekCpuMemory(address++);
                    final String token = TOKENS[hex];
                    if (token != null) {
                        sb.append(token);
                    } else {
                        if (version3 && hex >= 0x01 && hex <= 0x0a) {
                            sb.append((char) (hex - 1 + '0'));
                        } else if (hex == 0x11) {
                            sb.append(String.format("&H%X", mapper.peekWord(address)));
                            address += 2;
                        } else if (hex == 0x12 || hex == 0x0B) {
                            sb.append(mapper.peekWord(address));
                            address += 2;
                        } else {
                            final char c = hexToChar(hex);
                            sb.append(c);
                            if (c == '"') {
                                insideString = true;
                            }
                        }
                    }
                }
            }
            address++;
            sb.append('\n');
        }

        return sb.toString();
    }

    public static void pasteProgram(final String program)
            throws MessageException {

        final Machine machine = App.getMachine();
        if (machine == null) {
            return;
        }
        final Mapper mapper = machine.getMapper();
        int address = mapper.peekWord(0x0005);
        final boolean version3 = address < 0x7000;

        final String[] lines = program.split("\n|\r");
        final List<BasicLine> basicLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i].trim();
            if (!isBlank(line)) {
                basicLines.add(processLine(version3, i + 1, line));
            }
        }
        Collections.sort(basicLines);

        final int endSpaceAddress = mapper.peekWord(0x0003) - 4;
        for (final BasicLine basicLine : basicLines) {
            for (final int value : basicLine.getData()) {
                mapper.writeMemory(address++, value);
                if (address >= endSpaceAddress) {
                    break;
                }
            }
        }
        mapper.writeMemory(address, 0x00);
        mapper.writeMemory(address + 1, 0x00);
        mapper.writeMemory(address + 2, 0x00);
        mapper.writeMemory(address + 3, 0x00);

        address++;
        mapper.writeWord(0x0007, address);
        mapper.writeWord(0x001D, address + 1);
        mapper.writeWord(0x001F, address + 2);
    }

    private static BasicLine processLine(final boolean version3,
                                         final int fileIndex, String line) throws MessageException {

        final List<Integer> data = new ArrayList<>();
        int lineNumber = 0;
        int index = 0;

        line = line + '\u0000';
        while (index < line.length()) {
            final char c = line.charAt(index);
            if (c >= '0' && c <= '9') {
                lineNumber = 10 * lineNumber + c - '0';
                index++;
            } else {
                break;
            }
        }
        if (index == 0) {
            throw new MessageException("%d: Line does not start with a line number.",
                    fileIndex);
        }
        if (lineNumber < 0x0000 || lineNumber > 0xFFFF) {
            throw new MessageException("%d: Invalid line number (%d).", fileIndex,
                    lineNumber);
        }
        if (index == line.length()) {
            throw new MessageException("%d: Line only contains a line number.",
                    fileIndex);
        }
        data.add(lineNumber & 0xFF);
        data.add((lineNumber >> 8) & 0xFF);

        if (line.charAt(index) == ' ') {
            index++;
        }

        final Token token = new Token();
        boolean valueIsLineNumber = false;
        while (index < line.length()) {
            extractToken(line, lineNumber, index, token);

//      System.out.format("type = %s, value = %s%n", token.type, token.value);

            switch (token.type) {
                case CHR:
                    if (token.value == '?') {
                        data.add(0x8B);
                    } else if (token.value == '\'') {
                        data.add(token.value);
                        extractRemark(line, lineNumber, token.next, token);
                        addString(data, token);
                    } else if (token.value >= 0) {
                        data.add(charToHex((char) token.value));
                    }
                    break;
                case DEC:
                case HEX: {
                    if (token.type != TokenType.HEX && !valueIsLineNumber
                            && token.value <= 9 && version3) {
                        data.add(token.value + 1);
                    } else {
                        data.add(valueIsLineNumber ? 0x0B : token.type == TokenType.HEX
                                ? 0x11 : 0x12);
                        data.add(token.value & 0xFF);
                        data.add((token.value >> 8) & 0xFF);
                    }
                    break;
                }
                case STR:
                    data.add(charToHex('"'));
                    addString(data, token);
                    if (token.value == 1) {
                        data.add(charToHex('"'));
                    }
                    break;
                case TKN:
                    data.add(token.value);
                    valueIsLineNumber = (token.value >= 0x80 && token.value <= 0x86);
                    switch (token.value) {
                        case 0x91:
                            extractData(line, lineNumber, token.next, token);
                            addString(data, token);
                            break;
                        case 0x95:
                            extractRemark(line, lineNumber, token.next, token);
                            addString(data, token);
                            break;
                    }
                    break;
            }
            index = token.next;
        }

        data.add(0, data.size() + 1);

        final int[] d = new int[data.size()];
        for (int i = d.length - 1; i >= 0; i--) {
            d[i] = data.get(i);
        }
        return new BasicLine(lineNumber, d);
    }

    private static void addString(final List<Integer> data, final Token token) {
        for (int i = 0; i < token.str.length(); i++) {
            final int value = charToHex(token.str.charAt(i));
            if (value >= 0) {
                data.add(value);
            }
        }
    }

    private enum TokenType {
        CHR, HEX, DEC, STR, TKN, DAT, REM
    }

    private static class Token {
        public int next;
        public TokenType type;
        public int value;
        public String str;
    }

//  public static void main(final String... args) {
//      
//    final String program = "10 VIEW:CGEN 2:PLAY\"T1C1\"";  
//      
//    final boolean version3 = true;
//    
//    final String[] lines = program.split("\n|\r");
//    final List<BasicLine> basicLines = new ArrayList<>();
//    for(int i = 0; i < lines.length; i++) {
//      final String line = lines[i].trim();      
//      if (!isBlank(line)) {
//        basicLines.add(processLine(version3, i + 1, line));
//      }
//    }
//    Collections.sort(basicLines);
//      
//    for(final BasicLine basicLine : basicLines) {
//      for(final int value : basicLine.getData()) {
//        System.out.format("%02X ", value);
//      }
//    }
//  }
}
