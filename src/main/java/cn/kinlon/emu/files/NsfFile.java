package cn.kinlon.emu.files;

import cn.kinlon.emu.MessageException;
import cn.kinlon.emu.tv.TVSystem;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;

import static java.lang.Math.min;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;
import static cn.kinlon.emu.utils.MathUtil.roundUp;
import static cn.kinlon.emu.utils.StreamUtil.*;
import static cn.kinlon.emu.utils.StringUtils.*;

public class NsfFile implements IFile, Serializable {

    public static final String NSF_HEADER_ID = "NESM\u001A";
    public static final String NSFE_HEADER_ID = "NSFE";
    public static final String DEFAULT_TEXT = "<?>";
    private static final long serialVersionUID = 0;
    private static final int MAX_DATA_SIZE = 0x100000;

    private final String fileName;
    private final String entryFileName;
    private final String archiveFileName;
    private final int[] initBanks = new int[8];
    private int version;
    private int ntscPlaySpeed;
    private int palPlaySpeed;
    private boolean infoChunkRead;
    private int loadAddress;
    private int initAddress;
    private int playAddress;
    private TVSystem tvSystem;
    private int chipCount;
    private boolean usesVRC6Audio;
    private boolean usesVRC7Audio;
    private boolean usesFdsAudio;
    private boolean usesMMC5Audio;
    private boolean usesNamco163Audio;
    private boolean usesSunsoft5BAudio;
    private int totalSongs;
    private int startingSong;
    private byte[] data;
    private boolean bankSwitched;
    private int[] playlist;
    private long[] trackMillis;
    private long[] fadeMillis;
    private String[] trackLabels;
    private String[] albumInfo;
    private String text;
    private String description;
    private int dataSize;

    private transient int[] prgROM;

    public NsfFile(final DataInputStream in, final long fileSize,
                   final String entryFileName, final String archiveFileName)
            throws Throwable {

        this.entryFileName = entryFileName;
        this.archiveFileName = archiveFileName;
        this.fileName = FileUtil.getFileName(entryFileName)
                .toLowerCase(Locale.ENGLISH);

        for (int i = initBanks.length - 1; i >= 0; i--) {
            initBanks[i] = i;
        }

        final int[] headerID = new int[5];
        readByteArray(in, headerID, 0, 4, true);
        if (compareStrings(NSFE_HEADER_ID, headerID)) {
            readNSFe(in);
        } else {
            headerID[4] = in.readUnsignedByte();
            if (compareStrings(NSF_HEADER_ID, headerID)) {
                readNSF(fileSize, in);
            } else {
                throw new MessageException("Not NSF or NSFe file format.");
            }
        }
    }

    private void readNSF(final long fileSize, final DataInputStream in)
            throws Throwable {

        version = in.readUnsignedByte();
        totalSongs = in.readUnsignedByte();
        startingSong = Math.max(0, in.readUnsignedByte() - 1);

        readAddresses(in);

        albumInfo = new String[4];
        for (int i = 0; i < 3; i++) {
            albumInfo[i] = readNullTerminatedString(in, 32);
        }

        ntscPlaySpeed = readInt16LE(in);

        readByteArray(in, initBanks);
        outer:
        {
            for (int i = initBanks.length - 1; i >= 0; i--) {
                if (initBanks[i] != 0) {
                    bankSwitched = true;
                    break outer;
                }
            }
            bankSwitched = false;
        }

        palPlaySpeed = readInt16LE(in);

        readTvSystem(in);
        readChips(in);

        for (int i = 0; i < 4; i++) {
            in.readUnsignedByte();
        }

        dataSize = (int) fileSize - 0x80;
        prgROM = new int[roundUp((loadAddress & 0x0FFF) + dataSize, 0x1000)];
        readByteArray(in, prgROM, loadAddress & 0x0FFF, dataSize, true);

        makeAdjustments();
        createDescription();
    }

    private void readNSFe(final DataInputStream in) throws Throwable {
        outer:
        while (true) {
            final int chunkLength = readInt32LE(in);
            final String chunkID = readString(in, 4);
            switch (chunkID) {
                case "INFO":
                    readInfoChunk(in, chunkLength);
                    break;
                case "DATA":
                    readDataChunk(in, chunkLength);
                    break;
                case "NEND":
                    break outer;
                case "BANK":
                    readBankChunk(in, chunkLength);
                    break;
                case "plst":
                    readPlaylistChunk(in, chunkLength);
                    break;
                case "time":
                    readTimeChunk(in, chunkLength);
                    break;
                case "fade":
                    readFadeChunk(in, chunkLength);
                    break;
                case "tlbl":
                    readTrackLabelChunk(in, chunkLength);
                    break;
                case "auth":
                    readAuthorChunk(in, chunkLength);
                    break;
                case "text":
                    readTextChunk(in, chunkLength);
                    break;
                default:
                    throw new IOException("Unknown chunk type: " + chunkID);
            }
        }

        init();
        createDescription();
    }

    private void readAddresses(final DataInputStream in) throws Throwable {
        loadAddress = readInt16LE(in);
        initAddress = readInt16LE(in);
        playAddress = readInt16LE(in);
    }

    private void readTvSystem(final DataInputStream in) throws Throwable {
        final int tv = in.readUnsignedByte();
        if (getBitBool(tv, 0)) {
            tvSystem = TVSystem.PAL;
        } else {
            tvSystem = TVSystem.NTSC;
        }
    }

    private void readChips(final DataInputStream in) throws Throwable {
        final int chips = in.readUnsignedByte();
        chipCount = Integer.bitCount(chips & 0x3F);
        usesVRC6Audio = getBitBool(chips, 0);
        usesVRC7Audio = getBitBool(chips, 1);
        usesFdsAudio = getBitBool(chips, 2);
        usesMMC5Audio = getBitBool(chips, 3);
        usesNamco163Audio = getBitBool(chips, 4);
        usesSunsoft5BAudio = getBitBool(chips, 5);
    }

    private void readInfoChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {

        readAddresses(in);
        readTvSystem(in);
        readChips(in);

        totalSongs = in.readUnsignedByte();

        if (chunkLength > 9) {
            startingSong = in.readUnsignedByte();
        }
        discardRemainingChunk(in, chunkLength, 10);
        infoChunkRead = true;
    }

    private void discardRemainingChunk(final DataInputStream in,
                                       int chunkLength, final int expectedMaxLength) throws Throwable {
        if (chunkLength > expectedMaxLength) {
            chunkLength -= expectedMaxLength;
            do {
                in.readUnsignedByte();
            } while (--chunkLength > 0);
        }
    }

    private void readDataChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        dataSize = min(chunkLength, MAX_DATA_SIZE);
        data = new byte[dataSize];
        in.readFully(data);
        discardRemainingChunk(in, chunkLength, MAX_DATA_SIZE);
    }

    private void readBankChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        bankSwitched = true;
        final int size = Math.min(initBanks.length, chunkLength);
        for (int i = 0; i < size; i++) {
            initBanks[i] = in.readUnsignedByte();
        }
        discardRemainingChunk(in, chunkLength, initBanks.length);
    }

    private void readPlaylistChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        playlist = new int[chunkLength];
        readByteArray(in, playlist);
    }

    private void readTimeChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        if ((chunkLength & 0x03) != 0) {
            throw new IOException(String.format("The time chunk length, %d, is not "
                    + "divisible by 4.", chunkLength));
        }
        trackMillis = new long[chunkLength >> 2];
        for (int i = 0; i < trackMillis.length; i++) {
            trackMillis[i] = readInt32LE(in);
        }
    }

    private void readFadeChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        if ((chunkLength & 0x03) != 0) {
            throw new IOException(String.format("The fade chunk length, %d, is not "
                    + "divisible by 4.", chunkLength));
        }
        fadeMillis = new long[chunkLength >> 2];
        for (int i = 0; i < fadeMillis.length; i++) {
            fadeMillis[i] = readInt32LE(in);
        }
    }

    private void readTrackLabelChunk(final DataInputStream in,
                                     final int chunkLength) throws Throwable {
        trackLabels = readNullTerminatedStrings(in, chunkLength);
    }

    private void readAuthorChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        albumInfo = readNullTerminatedStrings(in, chunkLength, 4);
    }

    private void readTextChunk(final DataInputStream in, final int chunkLength)
            throws Throwable {
        text = readNullTerminatedString(in, chunkLength);
    }

    private String setDefaultText(final String str) {
        return isBlank(str) ? DEFAULT_TEXT : str;
    }

    private long[] adjustSize(long[] values) {
        if (values == null) {
            values = new long[totalSongs];
            Arrays.fill(values, -1L);
            return values;
        } else if (values.length == totalSongs) {
            return values;
        } else {
            final long[] vs = new long[totalSongs];
            System.arraycopy(values, 0, vs, 0, Math.min(totalSongs, values.length));
            for (int i = values.length; i < totalSongs; i++) {
                vs[i] = -1;
            }
            return vs;
        }
    }

    private String[] adjustSize(final String[] values) {
        return adjustSize(values, totalSongs);
    }

    private String[] adjustSize(String[] values, final int expectedSize) {
        if (values == null) {
            values = new String[expectedSize];
            Arrays.fill(values, DEFAULT_TEXT);
            return values;
        } else if (values.length != expectedSize) {
            String[] ss = new String[expectedSize];
            System.arraycopy(values, 0, ss, 0, Math.min(expectedSize, values.length));
            for (int i = values.length; i < expectedSize; i++) {
                ss[i] = DEFAULT_TEXT;
            }
            values = ss;
        }
        for (int i = values.length - 1; i >= 0; i--) {
            values[i] = removeNewlines(setDefaultText(values[i]));
        }
        return values;
    }

    private void makeAdjustments() throws Throwable {
        if (totalSongs <= 0) {
            throw new IOException("No songs to play.");
        }
        trackMillis = adjustSize(trackMillis);
        fadeMillis = adjustSize(fadeMillis);
        trackLabels = adjustSize(trackLabels);
        albumInfo = adjustSize(albumInfo, 4);
        text = replaceNewlines(setDefaultText(text));
        if (playlist == null) {
            playlist = new int[0];
        }
    }

    private void init() throws Throwable {

        if (!infoChunkRead) {
            throw new IOException("The file header is missing the INFO chunk.");
        }
        if (data == null) {
            throw new IOException("The file header is missing the DATA chunk.");
        }

        prgROM = new int[roundUp((loadAddress & 0x0FFF) + dataSize, 0x1000)];
        final int offset = loadAddress & 0x0FFF;
        for (int i = dataSize - 1; i >= 0; i--) {
            prgROM[offset + i] = data[i] & 0xFF;
        }

        makeAdjustments();
    }

    public String getFileName() {
        return fileName;
    }

    public long[] getTrackMillis() {
        return trackMillis;
    }

    public long[] getFadeMillis() {
        return fadeMillis;
    }

    public String[] getTrackLabels() {
        return trackLabels;
    }

    public String[] getAlbumInfo() {
        return albumInfo;
    }

    public int getStartingSong() {
        return startingSong;
    }

    public int getLoadAddress() {
        return loadAddress;
    }

    public int getInitAddress() {
        return initAddress;
    }

    public int getPlayAddress() {
        return playAddress;
    }

    public boolean isBankSwitched() {
        return bankSwitched;
    }

    public int[] getInitBanks() {
        return initBanks;
    }

    public TVSystem getTvSystem() {
        return tvSystem;
    }

    public int getChipCount() {
        return chipCount;
    }

    public boolean usesVRC6Audio() {
        return usesVRC6Audio;
    }

    public boolean usesVRC7Audio() {
        return usesVRC7Audio;
    }

    public boolean usesFdsAudio() {
        return usesFdsAudio;
    }

    public boolean usesMMC5Audio() {
        return usesMMC5Audio;
    }

    public boolean usesNamco163Audio() {
        return usesNamco163Audio;
    }

    public boolean usesSunsoft5BAudio() {
        return usesSunsoft5BAudio;
    }

    public int[] getPrgROM() {
        return prgROM;
    }

    public boolean isExtendedNSF() {
        return infoChunkRead;
    }

    public boolean isUsesVRC6Audio() {
        return usesVRC6Audio;
    }

    public boolean isUsesVRC7Audio() {
        return usesVRC7Audio;
    }

    public boolean isUsesFdsAudio() {
        return usesFdsAudio;
    }

    public boolean isUsesMMC5Audio() {
        return usesMMC5Audio;
    }

    public boolean isUsesNamco163Audio() {
        return usesNamco163Audio;
    }

    public boolean isUsesSunsoft5BAudio() {
        return usesSunsoft5BAudio;
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        prgROM = readByteArray(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeByteArray(out, prgROM);
    }

    private String formatMillis(final long millis) {
        if (millis < 0) {
            return "NA";
        } else {
            return millis + " millis";
        }
    }

    private void createDescription() {
        final StringBuilder sb = new StringBuilder();
        appendLine(sb, "File name: %s", fileName);
        if (!isExtendedNSF()) {
            appendLine(sb, "Version: %d", version);
        }
        appendLine(sb, "Data size: %d bytes", dataSize);
        appendLine(sb, "Total songs: %d", totalSongs);
        appendLine(sb, "Starting song: %d", startingSong);
        sb.append(String.format("Bank switched: %b", bankSwitched));
        if (bankSwitched) {
            sb.append(" (");
            for (int i = 0; i < initBanks.length; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(String.format("%02x", initBanks[i]));
            }
            appendLine(sb, ")");
        } else {
            appendLine(sb);
        }
        appendLine(sb, "Load address: %04X", loadAddress);
        appendLine(sb, "Init address: %04X", initAddress);
        appendLine(sb, "Play address: %04X", playAddress);
        sb.append("Play list: ");
        if (playlist.length == 0) {
            appendLine(sb, "none");
        } else {
            for (int i = 0; i < playlist.length; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(playlist[i]);
            }
            appendLine(sb);
        }
        appendLine(sb, "Album info:");
        appendLine(sb, "  Game title: %s", albumInfo[0]);
        appendLine(sb, "  Artist: %s", albumInfo[1]);
        appendLine(sb, "  Copyright: %s", albumInfo[2]);
        appendLine(sb, "  Ripper: %s", albumInfo[3]);
        appendLine(sb, "Tracks:");
        for (int i = 0; i < totalSongs; i++) {
            appendLine(sb, "  %d. %s (length: %s, fade: %s)", i, trackLabels[i],
                    formatMillis(trackMillis[i]), formatMillis(fadeMillis[i]));
        }
        appendLine(sb, "TV System: %s", tvSystem);
        if (!isExtendedNSF()) {
            appendLine(sb, "NTSC play speed: %d micros", ntscPlaySpeed);
            appendLine(sb, "PAL play speed: %d micros", palPlaySpeed);
        }
        appendLine(sb, "Text: %s", text);
        appendLine(sb, "Chip count: %d", chipCount);
        if (usesVRC6Audio) {
            appendLine(sb, "  VRC6 Audio");
        }
        if (usesVRC7Audio) {
            appendLine(sb, "  VRC7 Audio");
        }
        if (usesFdsAudio) {
            appendLine(sb, "  FDS Audio");
        }
        if (usesMMC5Audio) {
            appendLine(sb, "  MMC5 Audio");
        }
        if (usesNamco163Audio) {
            appendLine(sb, "  Namco 163 Audio");
        }
        if (usesSunsoft5BAudio) {
            appendLine(sb, "  Sunsoft 5B Audio");
        }
        description = sb.toString();
    }

    @Override
    public String toString() {
        return description;
    }
}