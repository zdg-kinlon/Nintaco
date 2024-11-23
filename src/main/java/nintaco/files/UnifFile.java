package nintaco.files;

import nintaco.cartdb.Cart;
import nintaco.cartdb.CartDB;
import nintaco.mappers.NametableMirroring;
import nintaco.mappers.nintendo.vs.VsGame;
import nintaco.mappers.nintendo.vs.VsHardware;
import nintaco.tv.TVSystem;
import nintaco.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.CRC32;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nintaco.files.FileType.UNIF;
import static nintaco.mappers.NametableMirroring.*;
import static nintaco.tv.TVSystem.NTSC;
import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.BitUtil.ceilBase2;
import static nintaco.util.CollectionsUtil.toIntArray;
import static nintaco.util.MathUtil.clamp;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.StringUtil.*;

public class UnifFile implements CartFile {

    public static final String HEADER_ID = "UNIF";
    private static final long serialVersionUID = 0;
    private final byte[] fileContents;
    private byte[][] prgROMs = new byte[16][];
    private byte[][] chrROMs = new byte[16][];
    private int[] prgCRCs = new int[16];
    private int[] chrCRCs = new int[16];
    private int minimumVersion;
    private String mapper;
    private String board;
    private String name;
    private String writer;
    private String readMe;
    private String dumpAuthor;
    private int dumpDay;
    private int dumpMonth;
    private int dumpYear;
    private String dumpSoftware;
    private TVSystem tvSystem = NTSC;
    private boolean anyTVSystem;
    private int controller;
    private boolean nonVolatilePrgRamPresent;
    private boolean chrRamPresent;
    private int mirroring;
    private final String fileName;
    private final String entryFileName;
    private final String archiveFileName;
    private String description;
    private int[] prgROM;
    private int[] chrROM;
    private int prgRomCRC;
    private int chrRomCRC;
    private int fileCRC;
    private int prgRomLength;
    private int chrRomLength;
    private Cart cart;
    public UnifFile(final DataInputStream in, final long fileSize,
                    final String entryFileName, final String archiveFileName)
            throws Throwable {
        this(in, fileSize, entryFileName, archiveFileName, true);
    }

    public UnifFile(final DataInputStream in, final long fileSize,
                    final String entryFileName, final String archiveFileName,
                    final boolean modifyHeader) throws Throwable {

        this.entryFileName = entryFileName;
        this.archiveFileName = archiveFileName;
        this.fileName = FileUtil.getFileName(entryFileName)
                .toLowerCase(Locale.ENGLISH);

        fileContents = new byte[(int) fileSize];
        in.readFully(fileContents);

        try (final DataInputStream dis = new DataInputStream(
                new ByteArrayInputStream(fileContents))) {
            readHeader(dis);
            readChunks(dis);
        }
        flattenROMs();
        computeCRCs();
        checkCartDB(modifyHeader);
        createDescription();
        cleanUp();
    }

    @Override
    public int getFileType() {
        return UNIF;
    }

    @Override
    public int[] getFileContents() {
        return toIntArray(fileContents);
    }

    private void readHeader(final DataInputStream in) throws Throwable {
        if (!HEADER_ID.equals(readString(in, HEADER_ID.length()))) {
            throw new IOException("Not Universal NES Image Format");
        }
        minimumVersion = readInt32LE(in);
        in.readFully(new byte[24]);
    }

    private void readChunks(final DataInputStream in) throws Throwable {

        while (true) {
            final byte[] type = new byte[4];
            try {
                in.readFully(type);
            } catch (final EOFException e) {
                break;
            }
            final int length = readInt32LE(in);
            switch (new String(type, ISO_8859_1).toUpperCase(Locale.ENGLISH)) {
                case "MAPR":
                    readMapper(in, length);
                    break;
                case "NAME":
                    readName(in, length);
                    break;
                case "WRTR":
                    readWriter(in, length);
                    break;
                case "READ":
                    readReadMe(in, length);
                    break;
                case "DINF":
                    readDumpingInformation(in);
                    break;
                case "TVCI":
                    readTVCompatibilityInformation(in);
                    break;
                case "CTRL":
                    readController(in);
                    break;
                case "BATR":
                    readBatteryBackedRAM(in);
                    break;
                case "VROR":
                    readVideoRomIsRam(in);
                    break;
                case "MIRR":
                    readMirroring(in);
                    break;
                default: {
                    final int index = clamp(type[3] - '0', 0x00, 0x0F);
                    switch (new String(type, 0, 3, ISO_8859_1)
                            .toUpperCase(Locale.ENGLISH)) {
                        case "PRG":
                            readPrgROM(in, length, index);
                            break;
                        case "CHR":
                            readChrROM(in, length, index);
                            break;
                        case "PCK":
                            readPrgCRC(in, length, index);
                            break;
                        case "CCK":
                            readChrCRC(in, length, index);
                            break;
                    }
                    break;
                }
            }
        }
    }

    private void createDescription() {
        final StringBuilder sb = new StringBuilder();
        if (archiveFileName != null) {
            appendLine(sb, "File name: %s <%s>",
                    FileUtil.getFileName(archiveFileName),
                    FileUtil.getFileName(entryFileName));
            appendLine(sb, "Directory: %s",
                    FileUtil.getDirectoryPath(archiveFileName));
        } else {
            appendLine(sb, "File name: %s", FileUtil.getFileName(entryFileName));
            appendLine(sb, "Directory: %s",
                    FileUtil.getDirectoryPath(entryFileName));
        }
        appendLine(sb, "File format: UNIF");
        appendLine(sb, "File CRC: %08X%s", fileCRC,
                cart == null ? " (unknown)" : "");
        appendLine(sb, "Name: %s", makeEmpty(name));
        appendLine(sb, "Mapper: %s", makeEmpty(mapper));
        appendLine(sb, "Board: %s", makeEmpty(board));
        appendLine(sb, "Mirroring: %s", NametableMirroring.toString(mirroring));
        for (int i = 0; i < prgROMs.length; i++) {
            if (prgROMs[i] != null) {
                appendLine(sb, "PRG ROM %01X: length = %d, CRC = %08X",
                        i, prgROMs[i].length, prgCRCs[i]);
            }
        }
        for (int i = 0; i < chrROMs.length; i++) {
            if (chrROMs[i] != null) {
                appendLine(sb, "CHR ROM %01X: length = %d, CRC = %08X",
                        i, chrROMs[i].length, chrCRCs[i]);
            }
        }
        StringUtil.append(sb, "PRG ROM size: %d bytes", prgRomLength);
        if (prgRomLength != prgROM.length) {
            StringUtil.append(sb, ", %d bytes (adjusted)", prgROM.length);
        }
        appendLine(sb);
        appendLine(sb, "PRG ROM CRC: %08X", prgRomCRC);
        StringUtil.append(sb, "CHR ROM size: %d bytes", chrRomLength);
        if (chrRomLength != chrROM.length) {
            StringUtil.append(sb, ", %d bytes (adjusted)", chrROM.length);
        }
        appendLine(sb);
        appendLine(sb, "CHR ROM CRC: %08X", chrRomCRC);
        appendLine(sb, "CHR RAM: %s", toYesNo(chrRamPresent));
        appendLine(sb, "Non-Volatile PRG RAM: %s",
                toYesNo(nonVolatilePrgRamPresent));
        appendLine(sb, "TV System: %s", tvSystem);
        appendLine(sb, "Any TV System: %s", toYesNo(anyTVSystem));
        sb.append("Controllers: ");
        appendController(sb);
        appendLine(sb);
        appendLine(sb, "Minimum version: %d", minimumVersion);
        appendLine(sb, "Dump software: %s", makeEmpty(dumpSoftware));
        appendLine(sb, "Dump writer: %s", makeEmpty(writer));
        appendLine(sb, "Dump author: %s", makeEmpty(dumpAuthor));
        if (dumpYear != 0) {
            appendLine(sb, "Dump date: %04d-%02d-%02d", dumpYear, dumpMonth, dumpDay);
        } else {
            appendLine(sb, "Dump date:");
        }
        appendLine(sb, "Read me: %s", makeEmpty(readMe));

        description = sb.toString();
    }

    private void readMapper(final DataInputStream in, final int length)
            throws Throwable {
        final byte[] data = new byte[length];
        in.readFully(data);
        mapper = createString(data);
        if (mapper != null) {
            mapper = mapper.trim();
            if (mapper.length() > 4) {
                switch (mapper.substring(0, 4).toUpperCase(Locale.ENGLISH)) {
                    case "NES-":
                    case "UNL-":
                    case "HVC-":
                    case "BTL-":
                    case "BMC-":
                        board = mapper.substring(4);
                        break;
                    default:
                        board = mapper;
                        break;
                }
            }
        }
    }

    private void readPrgROM(final DataInputStream in, final int length,
                            final int index) throws Throwable {
        prgROMs[index] = new byte[length];
        in.readFully(prgROMs[index]);
    }

    private void readChrROM(final DataInputStream in, final int length,
                            final int index) throws Throwable {
        chrROMs[index] = new byte[length];
        in.readFully(chrROMs[index]);
    }

    private void readPrgCRC(final DataInputStream in, final int length,
                            final int index) throws Throwable {
        prgCRCs[index] = readInt32LE(in);
    }

    private void readChrCRC(final DataInputStream in, final int length,
                            final int index) throws Throwable {
        chrCRCs[index] = readInt32LE(in);
    }

    private void readName(final DataInputStream in, final int length)
            throws Throwable {
        final byte[] data = new byte[length];
        in.readFully(data);
        name = createString(data);
    }

    private void readWriter(final DataInputStream in, final int length)
            throws Throwable {
        final byte[] data = new byte[length];
        in.readFully(data);
        writer = createString(data);
    }

    private void readReadMe(final DataInputStream in, final int length)
            throws Throwable {
        final byte[] data = new byte[length];
        in.readFully(data);
        readMe = createString(data);
    }

    private void readDumpingInformation(final DataInputStream in)
            throws Throwable {
        final byte[] data = new byte[100];
        in.readFully(data);
        dumpAuthor = createString(data);
        dumpDay = in.readUnsignedByte();
        dumpMonth = in.readUnsignedByte();
        dumpYear = readInt16LE(in);
        in.readFully(data);
        dumpSoftware = createString(data);
    }

    private void readTVCompatibilityInformation(final DataInputStream in)
            throws Throwable {
        switch (in.readUnsignedByte()) {
            case 0:
                anyTVSystem = false;
                tvSystem = NTSC;
                break;
            case 1:
                anyTVSystem = false;
                tvSystem = PAL;
                break;
            default:
                anyTVSystem = true;
                tvSystem = NTSC;
                break;
        }
    }

    private void readController(final DataInputStream in) throws Throwable {
        controller = in.readUnsignedByte();
    }

    private void readBatteryBackedRAM(final DataInputStream in) throws Throwable {
        nonVolatilePrgRamPresent = in.readUnsignedByte() != 0;
    }

    private void readVideoRomIsRam(final DataInputStream in) throws Throwable {
        chrRamPresent = in.readUnsignedByte() != 0;
    }

    private void readMirroring(final DataInputStream in) throws Throwable {
        switch (in.readUnsignedByte()) {
            case 0:
                mirroring = HORIZONTAL;
                break;
            case 1:
                mirroring = VERTICAL;
                break;
            case 2:
                mirroring = ONE_SCREEN_A;
                break;
            case 3:
                mirroring = ONE_SCREEN_B;
                break;
            case 4:
                mirroring = FOUR_SCREEN;
                break;
            default:
                mirroring = MAPPER_CONTROLLED;
                break;
        }
    }

    private String createString(final byte[] data) {
        if (data == null) {
            return null;
        }
        int length = 0;
        while (length < data.length && data[length] != 0) {
            length++;
        }
        return new String(data, 0, length, UTF_8);
    }

    private StringBuilder append(final StringBuilder sb,
                                 final boolean appendComma) {
        if (appendComma) {
            sb.append(", ");
        }
        return sb;
    }

    private void appendController(final StringBuilder sb) {

        boolean appendComma = false;
        if ((controller & ControllerMasks.Gamepad) != 0) {
            append(sb, appendComma).append("Gamepad");
            appendComma = true;
        }
        if ((controller & ControllerMasks.Zapper) != 0) {
            append(sb, appendComma).append("Zapper");
            appendComma = true;
        }
        if ((controller & ControllerMasks.ROB) != 0) {
            append(sb, appendComma).append("R.O.B.");
            appendComma = true;
        }
        if ((controller & ControllerMasks.Arkanoid) != 0) {
            append(sb, appendComma).append("Arkanoid Vaus");
            appendComma = true;
        }
        if ((controller & ControllerMasks.PowerPad) != 0) {
            append(sb, appendComma).append("Power Pad");
            appendComma = true;
        }
        if ((controller & ControllerMasks.Multitap) != 0) {
            append(sb, appendComma).append("Multitap");
            appendComma = true;
        }
    }

    private void flattenROMs() {
        prgRomLength = computeRomLength(prgROMs);
        prgROM = flattenROM(prgROMs, prgRomLength);
        chrRomLength = computeRomLength(chrROMs);
        chrROM = flattenROM(chrROMs, chrRomLength);
        if (chrRomLength == 0) {
            chrRamPresent = true;
        }
    }

    private int computeRomLength(final byte[][] roms) {
        int romLength = 0;
        for (final byte[] rom : roms) {
            if (rom != null) {
                romLength += rom.length;
            }
        }
        return romLength;
    }

    private int[] flattenROM(final byte[][] roms, final int romLength) {

        if (romLength == 0) {
            return new int[0];
        }

        final int[] flatROM = new int[ceilBase2(romLength)];
        int index = 0;
        for (final byte[] rom : roms) {
            if (rom != null) {
                for (int j = 0; j < rom.length; j++) {
                    flatROM[index++] = ((int) rom[j]) & 0xFF;
                }
            }
        }

        return flatROM;
    }

    private void computeCRCs() {
        final CRC32 crc = new CRC32();
        prgRomCRC = computeCRC(prgROM, crc);
        chrRomCRC = computeCRC(chrROM, crc);
        fileCRC = (int) crc.getValue();
    }

    private int computeCRC(final int[] data, final CRC32 crc) {
        final CRC32 dataCRC = new CRC32();
        for (int i = 0; i < data.length; i++) {
            dataCRC.update(data[i]);
            crc.update(data[i]);
        }
        return (int) dataCRC.getValue();
    }

    private void checkCartDB(final boolean modifyHeader) {
        cart = CartDB.getCart(fileCRC);
        if (cart != null && CartDB.isEnabled() && modifyHeader) {
            if (cart.getMirroring() >= 0) {
                mirroring = cart.getMirroring();
            }
            tvSystem = cart.getTVSystem();
        }
    }

    private void cleanUp() {
        prgROMs = null;
        chrROMs = null;
        prgCRCs = null;
        chrCRCs = null;
    }

    @Override
    public String getEntryFileName() {
        return entryFileName;
    }

    @Override
    public String getArchiveFileName() {
        return archiveFileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean isChrRamPresent() {
        return chrRamPresent;
    }

    @Override
    public boolean isNonVolatilePrgRamPresent() {
        return nonVolatilePrgRamPresent;
    }

    @Override
    public TVSystem getTvSystem() {
        return tvSystem;
    }

    @Override
    public int getMirroring() {
        return mirroring;
    }

    @Override
    public int[] getPrgROM() {
        return prgROM;
    }

    @Override
    public int[] getChrROM() {
        return chrROM;
    }

    @Override
    public int getFileCRC() {
        return fileCRC;
    }

    @Override
    public int getPrgRomLength() {
        return prgRomLength;
    }

    @Override
    public int getChrRomLength() {
        return chrRomLength;
    }

    public int getMinimumVersion() {
        return minimumVersion;
    }

    public String getMapper() {
        return mapper;
    }

    public String getBoard() {
        return board;
    }

    public String getName() {
        return name;
    }

    public String getWriter() {
        return writer;
    }

    public String getReadMe() {
        return readMe;
    }

    public String getDumpAuthor() {
        return dumpAuthor;
    }

    public int getDumpDay() {
        return dumpDay;
    }

    public int getDumpMonth() {
        return dumpMonth;
    }

    public int getDumpYear() {
        return dumpYear;
    }

    public String getDumpSoftware() {
        return dumpSoftware;
    }

    public boolean isAnyTVSystem() {
        return anyTVSystem;
    }

    public int getController() {
        return controller;
    }

    public String getDescription() {
        return description;
    }

    public int getPrgRomCRC() {
        return prgRomCRC;
    }

    public int getChrRomCRC() {
        return chrRomCRC;
    }

    @Override
    public int getVsHardware() {
        return VsHardware.VS_UNISYSTEM_NORMAL;
    }

    @Override
    public Cart getCart() {
        return cart;
    }

    @Override
    public VsGame getVsGame() {
        return null;
    }

    @Override
    public boolean isVsSystem() {
        return false;
    }

    @Override
    public boolean isVsUniSystem() {
        return false;
    }

    @Override
    public boolean isVsDualSystem() {
        return false;
    }

    @Override
    public int getMapperNumber() {
        return 0;
    }

    @Override
    public int getSubmapperNumber() {
        return 0;
    }

    @Override
    public boolean isTrainerPresent() {
        return false;
    }

    @Override
    public int getTrainerSize() {
        return 0;
    }

    @Override
    public int[] getTrainer() {
        return new int[0];
    }

    @Override
    public int getChrRamSize() {
        return 0;
    }

    @Override
    public int getConsole() {
        return Console.REGULAR;
    }

    @Override
    public int getExtendedConsole() {
        return ExtendedConsole.REGULAR;
    }

    @Override
    public String toString() {
        return description;
    }

    public interface ControllerMasks {
        int Gamepad = 0b0000_0001;
        int Zapper = 0b0000_0010;
        int ROB = 0b0000_0100;
        int Arkanoid = 0b0000_1000;
        int PowerPad = 0b0001_0000;
        int Multitap = 0b0010_0000;
    }
}
