package nintaco.files;

import nintaco.MessageException;
import nintaco.cartdb.Cart;
import nintaco.cartdb.CartDB;
import nintaco.mappers.NametableMirroring;
import nintaco.mappers.nintendo.vs.VsGame;
import nintaco.mappers.nintendo.vs.VsHardware;
import nintaco.mappers.nintendo.vs.VsPPU;
import nintaco.tv.TVSystem;

import java.io.*;
import java.util.Locale;
import java.util.zip.CRC32;

import static nintaco.cartdb.CartDevices.*;
import static nintaco.files.FileType.NES;
import static nintaco.mappers.NametableMirroring.*;
import static nintaco.tv.TVSystem.NTSC;
import static nintaco.util.BitUtil.ceilBase2;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.StringUtil.*;

public class NesFile implements CartFile, Serializable, Cloneable {

    public static final String HEADER_ID = "NES\u001A";
    public static final int PRG_ROM_PAGE_SIZE = 16384;
    public static final int PRG_RAM_PAGE_SIZE = 8192;
    public static final int CHR_ROM_PAGE_SIZE = 8192;
    protected static final String[] PAL_IDENTIFIERS
            = {"(e)", "(europe)", "(pal)", "(f)", "(g)", "(i)"};
    protected static final String[] PC10_IDENTIFIERS
            = {"(pc10)", "(pc10 version)"};
    protected static final String[] MAPPER_NAMES = {
            "NROM",                   //   0
            "MMC1",                   //   1
            "UNROM",                  //   2
            "CNROM",                  //   3
            "MMC3",                   //   4
            "MMC5",                   //   5
            "FFE Rev. A",             //   6
            "ANROM",                  //   7
            "",                       //   8
            "MMC2",                   //   9
            "MMC4",                   //  10
            "Color Dreams",           //  11
            "REX DBZ 5",              //  12
            "CPROM",                  //  13
            "REX SL-1632",            //  14
            "100-in-1",               //  15
            "BANDAI 24C02",           //  16
            "FFE Rev. B",             //  17
            "JALECO SS880006",        //  18
            "Namcot 106",             //  19
            "",                       //  20
            "Konami VRC2/VRC4 A",     //  21
            "Konami VRC2/VRC4 B",     //  22
            "Konami VRC2/VRC4 C",     //  23
            "Konami VRC6 Rev. A",     //  24
            "Konami VRC2/VRC4 D",     //  25
            "Konami VRC6 Rev. B",     //  26
            "CC-21 MI HUN CHE",       //  27
            "",                       //  28
            "",                       //  29
            "",                       //  30
            "",                       //  31
            "IREM G-101",             //  32
            "TC0190FMC/TC0350FMR",    //  33
            "IREM I-IM/BNROM",        //  34
            "Wario Land 2",           //  35
            "TXC Policeman",          //  36
            "PAL-ZZ SMB/TETRIS/NWC",  //  37
            "Bit Corp.",              //  38
            "",                       //  39
            "SMB2j FDS",              //  40
            "CALTRON 6-in-1",         //  41
            "BIO MIRACLE FDS",        //  42
            "FDS SMB2j LF36",         //  43
            "MMC3 BMC PIRATE A",      //  44
            "MMC3 BMC PIRATE B",      //  45
            "RUMBLESTATION 15-in-1",  //  46
            "NES-QJ SSVB/NWC",        //  47
            "TAITO TCxxx",            //  48
            "MMC3 BMC PIRATE C",      //  49
            "SMB2j FDS Rev. A",       //  50
            "11-in-1 BALL SERIES",    //  51
            "MMC3 BMC PIRATE D",      //  52
            "SUPERVISION 16-in-1",    //  53
            "",                       //  54
            "",                       //  55
            "SMB3 Pirate",            //  56
            "SIMBPLE BMC PIRATE A",   //  57
            "SIMBPLE BMC PIRATE B",   //  58
            "",                       //  59
            "SIMBPLE BMC PIRATE C",   //  60
            "20-in-1 KAISER Rev. A",  //  61
            "700-in-1",               //  62
            "Hello Kitty 255 in 1",   //  63
            "TENGEN RAMBO1",          //  64
            "IREM-H3001",             //  65
            "MHROM",                  //  66
            "SUNSOFT-FZII",           //  67
            "Sunsoft Mapper #4",      //  68
            "SUNSOFT-5/FME-7",        //  69
            "BANDAI KAMEN DISCRETE",  //  70
            "CAMERICA BF9093",        //  71
            "JALECO JF-17",           //  72
            "KONAMI VRC3",            //  73
            "TW MMC3+VRAM Rev. A",    //  74
            "KONAMI VRC1",            //  75
            "NAMCOT 108 Rev. A",      //  76
            "IREM LROG017",           //  77
            "Irem 74HC161/32",        //  78
            "AVE/C&E/TXC BOARD",      //  79
            "TAITO X1-005 Rev. A",    //  80
            "",                       //  81
            "TAITO X1-017",           //  82
            "YOKO VRC Rev. B",        //  83
            "",                       //  84
            "KONAMI VRC7",            //  85
            "JALECO JF-13",           //  86
            "74*139/74 DISCRETE",     //  87
            "NAMCO 3433",             //  88
            "SUNSOFT-3",              //  89
            "HUMMER/JY BOARD",        //  90
            "EARLY HUMMER/JY BOARD",  //  91
            "JALECO JF-19",           //  92
            "SUNSOFT-3R",             //  93
            "HVC-UN1ROM",             //  94
            "NAMCOT 108 Rev. B",      //  95
            "BANDAI OEKAKIDS",        //  96
            "IREM TAM-S1",            //  97
            "",                       //  98
            "VS Uni/Dual- system",    //  99
            "",                       // 100
            "",                       // 101
            "",                       // 102
            "FDS DOKIDOKI FULL",      // 103
            "",                       // 104
            "NES-EVENT NWC1990",      // 105
            "SMB3 PIRATE A",          // 106
            "MAGIC CORP A",           // 107
            "FDS UNROM BOARD",        // 108
            "",                       // 109
            "",                       // 110
            "",                       // 111
            "ASDER/NTDEC BOARD",      // 112
            "HACKER/SACHEN BOARD",    // 113
            "MMC3 SG PROT. A",        // 114
            "MMC3 PIRATE A",          // 115
            "MMC1/MMC3/VRC PIRATE",   // 116
            "FUTURE MEDIA BOARD",     // 117
            "TSKROM",                 // 118
            "NES-TQROM",              // 119
            "FDS TOBIDASE",           // 120
            "MMC3 PIRATE PROT. A",    // 121
            "",                       // 122
            "MMC3 PIRATE H2288",      // 123
            "",                       // 124
            "FDS LH32",               // 125
            "",                       // 126
            "Double Dragon pirate",   // 127
            "",                       // 128
            "",                       // 129
            "",                       // 130
            "",                       // 131
            "TXC/MGENIUS 22111",      // 132
            "SA72008",                // 133
            "MMC3 BMC PIRATE",        // 134
            "",                       // 135
            "TCU02",                  // 136
            "S8259D",                 // 137
            "S8259B",                 // 138
            "S8259C",                 // 139
            "JALECO JF-11/14",        // 140
            "S8259A",                 // 141
            "UNLKS7032",              // 142
            "TCA01",                  // 143
            "AGCI 50282",             // 144
            "SA72007",                // 145
            "SA0161M",                // 146
            "TCU01",                  // 147
            "SA0037",                 // 148
            "SA0036",                 // 149
            "S74LS374N",              // 150
            "",                       // 151
            "BANDAI 74161/7432",      // 152
            "BANDAI SRAM",            // 153
            "",                       // 154
            "",                       // 155
            "",                       // 156
            "BANDAI BARCODE",         // 157
            "",                       // 158
            "BANDAI 24C01",           // 159
            "SA009",                  // 160
            "",                       // 161
            "",                       // 162
            "",                       // 163
            "",                       // 164
            "",                       // 165
            "SUBOR Rev. A",           // 166
            "SUBOR Rev. B",           // 167
            "",                       // 168
            "",                       // 169
            "",                       // 170
            "",                       // 171
            "",                       // 172
            "",                       // 173
            "",                       // 174
            "",                       // 175
            "BMCFK23C",               // 176
            "",                       // 177
            "",                       // 178
            "",                       // 179
            "",                       // 180
            "",                       // 181
            "Super Donkey Kong",      // 182
            "",                       // 183
            "Atlantis no Nazo",       // 184
            "CNROM (CHR disabled)",   // 185
            "",                       // 186
            "",                       // 187
            "",                       // 188
            "Thunder Warrior",        // 189
            "",                       // 190
            "",                       // 191
            "TW MMC3+VRAM Rev. B",    // 192
            "NTDEC TC-112",           // 193
            "TW MMC3+VRAM Rev. C",    // 194
            "TW MMC3+VRAM Rev. D",    // 195
            "",                       // 196
            "",                       // 197
            "TW MMC3+VRAM Rev. E",    // 198
            "",                       // 199
            "",                       // 200
            "",                       // 201
            "",                       // 202
            "",                       // 203
            "",                       // 204
            "",                       // 205
            "NAMCOT 108 Rev. C",      // 206
            "TAITO X1-005 Rev. B",    // 207
            "",                       // 208
            "",                       // 209
            "",                       // 210
            "",                       // 211
            "",                       // 212
            "",                       // 213
            "",                       // 214
            "",                       // 215
            "",                       // 216
            "",                       // 217
            "",                       // 218
            "UNLA9746",               // 219
            "Debug Mapper",           // 220
            "UNLN625092",             // 221
            "",                       // 222
            "",                       // 223
            "",                       // 224
            "72-in-1",                // 225
            "BMC 22+20-in-1",         // 226
            "1200-in-1",              // 227
            "Action 52",              // 228
            "31-in-1",                // 229
            "BMC Contra+22-in-1",     // 230
            "20-in-1",                // 231
            "BMC QUATTRO",            // 232
            "BMC 22+20-in-1 RST",     // 233
            "BMC MAXI",               // 234
            "Golden Game 150-in-1",   // 235
            "",                       // 236
            "",                       // 237
            "UNL6035052",             // 238
            "",                       // 239
            "",                       // 240
            "",                       // 241
            "",                       // 242
            "S74LS374NA",             // 243
            "DECATHLON",              // 244
            "",                       // 245
            "FONG SHEN BANG",         // 246
            "",                       // 247
            "",                       // 248
            "",                       // 249
            "Time Diver Avenger",     // 250
            "",                       // 251
            "SAN GUO ZHI PIRATE",     // 252
            "DRAGON BALL PIRATE",     // 253
            "Ai Senshi Nicol",        // 254
            "",                       // 255    
    };
    private static final long serialVersionUID = 0;
    protected transient int[] prgROM;
    protected transient int[] chrROM;
    protected transient int[] trainer;
    protected int[] header = new int[16];
    protected int chrRomPages;
    protected int prgRomPages;
    protected int prgRamPages;
    protected int prgRamSize;
    protected int mapperNumber;
    protected int originalMapperNumber;
    protected int submapperNumber;
    protected int originalSubmapperNumber;
    protected int mirroring;
    protected int originalMirroring;
    protected int nonVolatilePrgRamSize;
    protected int originalNonVolatilePrgRamSize;
    protected int nonVolatileChrRamSize;
    protected int chrRamSize;
    protected int originalConsole;
    protected int console;
    protected int extendedConsole;
    protected int miscellaneousROMs;
    protected int defaultExpansionDevice;
    protected int cpuPpuTiming;
    protected int originalCpuPpuTiming;
    protected int vsPPU;
    protected int vsHardware;
    protected boolean trainerPresent;
    protected boolean chrRamPresent;
    protected boolean nonVolatilePrgRamPresent;
    protected boolean originalNonVolatilePrgRamPresent;
    protected boolean nes20Format;
    protected String fileName;
    protected String entryFileName;
    protected String archiveFileName;
    protected String description;
    protected String mapperName;
    protected int prgRomCRC;
    protected int chrRomCRC;
    protected int fileCRC;
    protected int prgRomLength;
    protected int chrRomLength;
    protected Cart cart;
    protected VsGame vsGame;
    public NesFile(final DataInputStream in, final long fileSize,
                   final String entryFileName, final String archiveFileName)
            throws Throwable {
        this(in, fileSize, entryFileName, archiveFileName, true);
    }
    public NesFile(final DataInputStream in, final long fileSize,
                   final String entryFileName, final String archiveFileName,
                   final boolean modifyHeader) throws Throwable {

        this.archiveFileName = archiveFileName;
        if (isBlank(entryFileName)) {
            this.entryFileName = archiveFileName;
            this.fileName = FileUtil.getFileName(archiveFileName)
                    .toLowerCase(Locale.ENGLISH);
        } else {
            this.entryFileName = entryFileName;
            this.fileName = FileUtil.getFileName(entryFileName)
                    .toLowerCase(Locale.ENGLISH);
        }

        readBytes(in, header);

        if (!compareStrings(HEADER_ID, header)) {
            throw new MessageException("Invalid or unknown file format.");
        }

        prgRomPages = header[4];
        chrRomPages = header[5];

        mapperNumber = header[6] >> 4;
        if (getBitBool(header[6], 3)) {
            mirroring = FOUR_SCREEN;
        } else if (getBitBool(header[6], 0)) {
            mirroring = VERTICAL;
        } else {
            mirroring = HORIZONTAL;
        }
        originalMirroring = mirroring;
        trainerPresent = getBitBool(header[6], 2);
        nonVolatilePrgRamPresent = getBitBool(header[6], 1);
        nonVolatilePrgRamSize = nonVolatilePrgRamPresent ? 0x2000 : 0;

        nes20Format = (header[7] & 0x0C) == 0x08;

        if (!nes20Format) {
            if ((header[7] & 0x0C) != 0x00 || (header[7] & 0x03) == 0x03) {
                header[7] = header[8] = header[9] = 0x00;
            } else {
                for (int i = 10; i < 16; i++) {
                    if (header[i] != 0x00) {
                        header[7] = header[8] = header[9] = 0x00;
                        break;
                    }
                }
            }
        }

        mapperNumber |= header[7] & 0xF0;
        originalConsole = console = header[7] & 0x03;

        if (nes20Format) {

            submapperNumber = header[8] >> 4;
            mapperNumber |= (header[8] & 0x0F) << 8;

            chrRomPages |= (header[9] & 0xF0) << 4;
            prgRomPages |= (header[9] & 0x0F) << 8;

            nonVolatilePrgRamSize = decodeRamSize(header[10] >> 4);
            nonVolatilePrgRamPresent = nonVolatilePrgRamSize > 0;
            prgRamSize = decodeRamSize(header[10] & 0x0F);
            prgRamPages = prgRamSize / PRG_RAM_PAGE_SIZE;

            nonVolatileChrRamSize = decodeRamSize(header[11] >> 4);
            chrRamSize = decodeRamSize(header[11] & 0x0F);

            originalCpuPpuTiming = cpuPpuTiming = header[12] & 0x03;

            switch (console) {
                case Console.VS_SYSTEM:
                    vsPPU = header[13] & 0x0F;
                    vsHardware = header[13] >> 4;
                    break;
                case Console.EXTENDED:
                    extendedConsole = header[13] & 0x0F;
                    break;
            }

            miscellaneousROMs = header[14] & 0x03;

            defaultExpansionDevice = header[15] & 0x3F;

        } else {

            if (console == Console.EXTENDED) {
                console = Console.REGULAR;
            }
            if (modifyHeader) {
                for (final String pc10Identifier : PC10_IDENTIFIERS) {
                    if (fileName.contains(pc10Identifier)) {
                        console = Console.PLAYCHOICE_10;
                        break;
                    }
                }
            }

            prgRamPages = header[8];
            prgRamSize = PRG_RAM_PAGE_SIZE * prgRamPages;

            if (getBitBool(header[9], 0)) {
                originalCpuPpuTiming = cpuPpuTiming = CpuPpuTiming.PAL;
            } else {
                originalCpuPpuTiming = cpuPpuTiming = CpuPpuTiming.NTSC;
                if (modifyHeader) {
                    for (final String palIdentifier : PAL_IDENTIFIERS) {
                        if (fileName.contains(palIdentifier)) {
                            cpuPpuTiming = CpuPpuTiming.PAL;
                            break;
                        }
                    }
                }
            }
        }

        if (prgRomPages == 0) {
            prgRomPages = 0x100;
        }
        if ((prgRomPages >> 8) == 0xF) {
            final int multiplier = prgRomPages & 0x03;
            final int exponent = (prgRomPages >> 2) & 0x3F;
            prgRomLength = (1 << exponent) * ((multiplier << 1) + 1);
        } else {
            prgRomLength = PRG_ROM_PAGE_SIZE * prgRomPages;
        }

        if ((chrRomPages >> 8) == 0xF) {
            final int multiplier = chrRomPages & 0x03;
            final int exponent = (chrRomPages >> 2) & 0x3F;
            chrRomLength = (1 << exponent) * ((multiplier << 1) + 1);
        } else {
            chrRomLength = CHR_ROM_PAGE_SIZE * chrRomPages;
        }

        readROMs(in, fileSize);

        originalMapperNumber = mapperNumber;
        originalSubmapperNumber = submapperNumber;
        cart = CartDB.getCart(fileCRC);
        originalNonVolatilePrgRamPresent = nonVolatilePrgRamPresent;
        originalNonVolatilePrgRamSize = nonVolatilePrgRamSize;
        if (modifyHeader) {
            vsGame = VsGame.getVsGame(this);
            if (vsGame != null) {
                console = Console.VS_SYSTEM;
                vsHardware = vsGame.getHardware();
                vsPPU = vsGame.getPPU();
                cart = new Cart(fileCRC, vsGame.getMapper(), 0, NTSC,
                        vsGame.isZapperGame() ? Zapper : -1, vsGame.getMirroring(), false);
                if (vsGame.isNonVolatilePrgRamPresent()) {
                    nonVolatilePrgRamPresent = true;
                    nonVolatilePrgRamSize = 0x0800;
                }
            }
            if (cart != null && CartDB.isEnabled()) {
                mapperNumber = cart.getMapper();
                submapperNumber = cart.getSubmapper();
                if (cart.getMirroring() >= 0) {
                    mirroring = cart.getMirroring();
                }
                cpuPpuTiming = CpuPpuTiming.fromTVSystem(cart.getTVSystem());
            }
        }
        final boolean unknown = cart == null;
        if (cart == null) {
            cart = new Cart(getFileCRC(), getMapperNumber(), getSubmapperNumber(),
                    getTvSystem(), getCartDevice(), getMirroring(), false);
        }

        mapperName = mapperNumber < MAPPER_NAMES.length
                ? MAPPER_NAMES[mapperNumber] : "";
        final StringBuilder sb = new StringBuilder();
        if (!isBlank(archiveFileName)) {
            if (isBlank(entryFileName)) {
                appendLine(sb, "File name: %s", FileUtil.getFileName(archiveFileName));
            } else {
                appendLine(sb, "File name: %s <%s>",
                        FileUtil.getFileName(archiveFileName),
                        FileUtil.getFileName(entryFileName));
            }
            appendLine(sb, "Directory: %s",
                    FileUtil.getDirectoryPath(archiveFileName));
        } else if (!isBlank(entryFileName)) {
            appendLine(sb, "File name: %s", FileUtil.getFileName(entryFileName));
            appendLine(sb, "Directory: %s",
                    FileUtil.getDirectoryPath(entryFileName));
        }
        appendLine(sb, "File format: %s", nes20Format ? "NES 2.0" : "iNES");
        appendLine(sb, "File CRC: %08X%s", fileCRC,
                unknown ? " (unknown)" : "");
        if (originalMapperNumber != mapperNumber) {
            appendLine(sb, "Mapper #: %d (modified, was %d)", mapperNumber,
                    originalMapperNumber);
        } else {
            appendLine(sb, "Mapper #: %d", mapperNumber);
        }
        if (nes20Format || submapperNumber != 0) {
            if (originalSubmapperNumber != submapperNumber) {
                appendLine(sb, "Submapper #: %d (modified, was %d)", submapperNumber,
                        originalSubmapperNumber);
            } else {
                appendLine(sb, "Submapper #: %d", submapperNumber);
            }
        }
        appendLine(sb, "Mapper name: %s", mapperName);
        appendLine(sb, "Mirroring: %s", NametableMirroring.toString(mirroring));
        if ((prgRomPages >> 8) == 0xF) {
            append(sb, "PRG ROM size: %d bytes", prgRomLength);
        } else {
            append(sb, "PRG ROM size: %d bytes (%d pages x %d bytes)",
                    prgRomLength, prgRomPages, PRG_ROM_PAGE_SIZE);
        }
        if (prgRomLength != prgROM.length) {
            append(sb, ", %d bytes (adjusted)", prgROM.length);
        }
        appendLine(sb);
        appendLine(sb, "PRG ROM CRC: %08X", prgRomCRC);
        if ((chrRomPages >> 8) == 0xF) {
            append(sb, "CHR ROM size: %d bytes", chrRomLength);
        } else {
            append(sb, "CHR ROM size: %d bytes (%d pages x %d bytes)",
                    chrRomLength, chrRomPages, CHR_ROM_PAGE_SIZE);
        }
        if (chrRomLength != chrROM.length) {
            append(sb, ", %d bytes (adjusted)", chrROM.length);
        }
        appendLine(sb);
        appendLine(sb, "CHR ROM CRC: %08X", chrRomCRC);
        appendLine(sb, "CHR RAM: %s", toYesNo(chrRamPresent));
        if (nes20Format) {
            appendLine(sb, "PRG RAM size: %d bytes", prgRamSize);
        } else {
            appendLine(sb, "PRG RAM size: %d bytes (%d pages x %d bytes)",
                    prgRamSize, prgRamPages, PRG_RAM_PAGE_SIZE);
        }
        if (originalNonVolatilePrgRamPresent != nonVolatilePrgRamPresent) {
            appendLine(sb, "Non-Volatile PRG RAM: %s (modified, was %s)",
                    toYesNo(nonVolatilePrgRamPresent),
                    toYesNo(originalNonVolatilePrgRamPresent));
        } else {
            appendLine(sb, "Non-Volatile PRG RAM: %s",
                    toYesNo(nonVolatilePrgRamPresent));
        }
        if (nes20Format) {
            if (originalNonVolatilePrgRamSize != nonVolatilePrgRamSize) {
                appendLine(sb,
                        "Non-Volatile PRG RAM size: %d bytes (modified, was %d bytes)",
                        nonVolatilePrgRamSize, originalNonVolatilePrgRamSize);
            } else {
                appendLine(sb, "Non-Volatile PRG RAM size: %d bytes",
                        nonVolatilePrgRamSize);
            }
            appendLine(sb, "Non-Volatile CHR RAM size: %d bytes",
                    nonVolatileChrRamSize);
            appendLine(sb, "CHR RAM size: %d bytes", chrRamSize);
        }
        if (cpuPpuTiming != originalCpuPpuTiming) {
            appendLine(sb, "TV System: %s (modified, was %s)",
                    CpuPpuTiming.toString(cpuPpuTiming),
                    CpuPpuTiming.toString(originalCpuPpuTiming));
        } else {
            appendLine(sb, "TV System: %s", CpuPpuTiming.toString(cpuPpuTiming));
        }
        if (console == Console.EXTENDED) {
            appendLine(sb, "Console type: %s",
                    ExtendedConsole.toString(extendedConsole));
        } else if (console != originalConsole) {
            appendLine(sb, "Console type: %s (modified, was %s)",
                    Console.toString(console), Console.toString(originalConsole));
        } else {
            appendLine(sb, "Console type: %s", Console.toString(console));
        }
        if (isVsSystem()) {
            appendLine(sb, "VS. PPU: %s", VsPPU.toString(vsPPU));
            appendLine(sb, "VS. Hardware: %s", VsHardware.toString(vsHardware));
        }
        if (nes20Format) {
            appendLine(sb, "Miscellaneous ROMs: %d", miscellaneousROMs);
            appendLine(sb, "Default expansion device: %s",
                    DefaultExpansionDevice.toString(defaultExpansionDevice));
        }
        appendLine(sb, "Trainer: %s", toYesNo(trainerPresent));
        description = sb.toString();
    }

    protected void readROMs(final DataInputStream in, final long fileSize)
            throws Throwable {

        if (trainerPresent) {
            trainer = new int[512];
            readFully(in, trainer, 0, trainer.length, null);
        } else {
            trainer = new int[0];
        }

        prgROM = new int[ceilBase2(prgRomLength)];
        final CRC32 crc = new CRC32();
        prgRomCRC = readFully(in, prgROM, 0, prgRomLength, crc);

        // King Neptune's Adventure
        if (mapperNumber == 1 && prgRomLength == 0x8000
                && prgRomCRC == 0x3C6C8E6C) {
            prgRomPages <<= 1;
            prgRomLength <<= 1;
            final int[] rom = new int[0x10000];
            System.arraycopy(prgROM, 0, rom, 0, 0x8000);
            prgROM = rom;
            readFully(in, prgROM, 0x8000, 0x8000, crc);
            prgRomCRC = (int) crc.getValue();
        }

        if (chrRomLength == 0) {
            chrRamPresent = true;
            chrROM = new int[0];
            chrRomCRC = 0;
        } else {
            chrRamPresent = false;
            chrROM = new int[ceilBase2(chrRomLength)];
            chrRomCRC = readFully(in, chrROM, 0, chrRomLength, crc);
        }
        fileCRC = (int) crc.getValue();

        switch (fileCRC) {
            case 0x07A2F3B2: // VS Gumshoe
                System.arraycopy(prgROM, 0x8000, prgROM, 0xA000, 0x2000);
                break;
            case 0x51C76943: // VS Mahjong (Japan)
                System.arraycopy(prgROM, 0x6000, prgROM, 0xA000, 0x6000);
                System.arraycopy(prgROM, 0x0000, prgROM, 0x2000, 0x6000);
                break;
            case 0x70FFB591: // VS Raid on Bungeling Bay
                System.arraycopy(prgROM, 0x8000, prgROM, 0xE000, 0x2000);
                break;
            case 0xC492B4D1: // VS Tetris
                System.arraycopy(prgROM, 0x0000, prgROM, 0x2000, 0x6000);
                break;
        }
    }

    private int readFully(final DataInputStream in, final int[] data,
                          final int offset, final int length, final CRC32 crc) throws IOException {

        final CRC32 dataCRC = new CRC32();
        int bytesRead = 0;
        for (int i = 0; i < length; i++, bytesRead++) {
            int d = in.read();
            if (d < 0) {
                break;
            }
            data[offset + i] = d;
            dataCRC.update(d);
            if (crc != null) {
                crc.update(d);
            }
        }
        if (bytesRead > 0) {
            for (int i = offset + bytesRead; i < data.length; i++) {
                data[i] = data[i % bytesRead];
            }
        }
        return (int) dataCRC.getValue();
    }

    private int decodeRamSize(int value) {
        if (value == 0 || value == 15) {
            return 0;
        }
        return 0x40 << value;
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
    public boolean isTrainerPresent() {
        return trainerPresent;
    }

    @Override
    public int getTrainerSize() {
        return trainer.length;
    }

    @Override
    public int[] getTrainer() {
        return trainer;
    }

    @Override
    public int getMapperNumber() {
        return mapperNumber;
    }

    public int getOriginalMapperNumber() {
        return originalMapperNumber;
    }

    @Override
    public int getSubmapperNumber() {
        return submapperNumber;
    }

    public int getOriginalSubmapperNumber() {
        return originalSubmapperNumber;
    }

    @Override
    public int getMirroring() {
        return mirroring;
    }

    public int getOriginalMirroring() {
        return originalMirroring;
    }

    public int getNonVolatilePrgRamSize() {
        return nonVolatilePrgRamSize;
    }

    public int getNonVolatileChrRamSize() {
        return nonVolatileChrRamSize;
    }

    @Override
    public int getChrRamSize() {
        return chrRamSize;
    }

    public int getCpuPpuTiming() {
        return cpuPpuTiming;
    }

    public int getOriginalCpuPpuTiming() {
        return originalCpuPpuTiming;
    }

    public void setOriginalCpuPpuTiming(int originalCpuPpuTiming) {
        this.originalCpuPpuTiming = originalCpuPpuTiming;
    }

    @Override
    public TVSystem getTvSystem() {
        return CpuPpuTiming.toTVSystem(cpuPpuTiming);
    }

    public int getVsPPU() {
        return vsPPU;
    }

    @Override
    public int getVsHardware() {
        return vsHardware;
    }

    @Override
    public Cart getCart() {
        return cart;
    }

    @Override
    public VsGame getVsGame() {
        return vsGame;
    }

    @Override
    public boolean isChrRamPresent() {
        return chrRamPresent;
    }

    @Override
    public boolean isNonVolatilePrgRamPresent() {
        return nonVolatilePrgRamPresent;
    }

    public int getOriginalConsole() {
        return originalConsole;
    }

    @Override
    public int getConsole() {
        return console;
    }

    @Override
    public int getExtendedConsole() {
        return extendedConsole;
    }

    public boolean isPlaychoice10() {
        return console == Console.PLAYCHOICE_10 || (console == Console.EXTENDED
                && extendedConsole == ExtendedConsole.PLAYCHOICE_10);
    }

    @Override
    public boolean isVsSystem() {
        return (vsGame != null) || (nes20Format
                ? (console == Console.VS_SYSTEM || (console == Console.EXTENDED
                && extendedConsole == ExtendedConsole.VS_SYSTEM))
                : getBitBool(console, 0));
    }

    @Override
    public boolean isVsUniSystem() {
        return (vsGame != null) ? vsGame.isUniSystemGame() : (nes20Format
                && isVsSystem() && vsHardware < VsHardware.VS_DUALSYSTEM_NORMAL);
    }

    @Override
    public boolean isVsDualSystem() {
        return (vsGame != null) ? vsGame.isDualSystemGame() : (nes20Format
                && isVsSystem() && vsHardware >= VsHardware.VS_DUALSYSTEM_NORMAL);
    }

    public int getMiscellaneousROMs() {
        return miscellaneousROMs;
    }

    public int getDefaultExpansionDevice() {
        return defaultExpansionDevice;
    }

    public int getCartDevice() {
        return (cart == null)
                ? DefaultExpansionDevice.toCartDevice(defaultExpansionDevice)
                : cart.getDevice();
    }

    public boolean isNes20Format() {
        return nes20Format;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getEntryFileName() {
        return entryFileName;
    }

    @Override
    public String getArchiveFileName() {
        return archiveFileName;
    }

    public int getChrRomPages() {
        return chrRomPages;
    }

    public int getChrRomSize() {
        return CHR_ROM_PAGE_SIZE * chrRomPages;
    }

    public int[] getHeader() {
        return header;
    }

    public int getHeaderSize() {
        return header.length;
    }

    public int getPrgRomPages() {
        return prgRomPages;
    }

    public int getPrgRomSize() {
        return PRG_ROM_PAGE_SIZE * prgRomPages;
    }

    public int getPrgRamPages() {
        return prgRamPages;
    }

    public int getPrgRamSize() {
        return prgRamSize;
    }

    public String getMapperName() {
        return mapperName;
    }

    @Override
    public int getFileCRC() {
        return fileCRC;
    }

    public int getPrgRomCRC() {
        return prgRomCRC;
    }

    public int getChrRomCRC() {
        return chrRomCRC;
    }

    @Override
    public int getPrgRomLength() {
        return prgRomLength;
    }

    @Override
    public int getChrRomLength() {
        return chrRomLength;
    }

    @Override
    public int getFileType() {
        return NES;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int[] getFileContents() {
        final int[] data = new int[getHeaderSize() + getTrainerSize()
                + getPrgRomSize() + getChrRomSize()];
        System.arraycopy(header, 0, data, 0, getHeaderSize());
        System.arraycopy(trainer, 0, data, getHeaderSize(), getTrainerSize());
        System.arraycopy(prgROM, 0, data, getHeaderSize() + getTrainerSize(),
                getPrgRomSize());
        System.arraycopy(chrROM, 0, data, getHeaderSize() + getTrainerSize()
                + getPrgRomSize(), getChrRomSize());
        return data;
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        prgROM = readByteArray(in);
        chrROM = readByteArray(in);
        trainer = readByteArray(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeByteArray(out, prgROM);
        writeByteArray(out, chrROM);
        writeByteArray(out, trainer);
    }

    public interface CpuPpuTiming {

        int NTSC = 0;
        int PAL = 1;
        int MULTI_REGION = 2;
        int DENDY = 3;

        String[] NAMES = {
                "NTSC",          // 0
                "PAL",           // 1
                "Multi-region",  // 2
                "Dendy",         // 3
        };

        static String toString(final int cpuPpuTiming) {
            return (cpuPpuTiming < 0 || cpuPpuTiming >= NAMES.length) ? "Other"
                    : NAMES[cpuPpuTiming];
        }

        static TVSystem toTVSystem(final int cpuPpuTiming) {
            switch (cpuPpuTiming) {
                case PAL:
                    return TVSystem.PAL;
                case DENDY:
                    return TVSystem.Dendy;
                default:
                    return TVSystem.NTSC;
            }
        }

        static int fromTVSystem(final TVSystem tvSystem) {
            switch (tvSystem) {
                case PAL:
                    return PAL;
                case Dendy:
                    return DENDY;
                default:
                    return NTSC;
            }
        }
    }

    public interface DefaultExpansionDevice {

        int UNSPECIFIED = 0x00;
        int STANDARD_CONTROLLERS = 0x01;
        int NES_MULTITAP_WITH_4_STANDARD_CONTROLLERS = 0x02;
        int FAMICOM_MULTITAP_WITH_4_STANDARD_CONTROLLERS = 0x03;
        int VS_SYSTEM = 0x04;
        int VS_SYSTEM_WITH_REVERSED_INPUTS = 0x05;
        int VS_PINBALL_JAPAN = 0x06;
        int VS_ZAPPER = 0x07;
        int ZAPPER = 0x08;
        int TWO_ZAPPERS = 0x09;
        int BANDAI_HYPER_SHOT = 0x0A;
        int POWER_PAD_SIDE_A = 0x0B;
        int POWER_PAD_SIDE_B = 0x0C;
        int FAMILY_TRAINER_SIDE_A = 0x0D;
        int FAMILY_TRAINER_SIDE_B = 0x0E;
        int NES_ARKANOID_VAUS = 0x0F;
        int FAMICOM_ARKANOID_VAUS = 0x10;
        int TWO_VAUS_CONTROLLERS_WITH_DATA_RECORDER = 0x11;
        int KONAMI_HYPER_SHOT = 0x12;
        int COCONUTS_PACHINKO = 0x13;
        int EXCITING_BOXING_PUNCHING_BAG = 0x14;
        int JISSEN_MAHJONG = 0x15;
        int PARTY_TAP = 0x16;
        int OEKA_KIDS_TABLET = 0x17;
        int SUNSOFT_BARCODE_BATTLER = 0x18;
        int MIRACLE_PIANO_KEYBOARD = 0x19;
        int POKKUN_MOGURAA = 0x1A;
        int TOP_RIDER = 0x1B;
        int DOUBLE_FISTED = 0x1C;
        int FAMICOM_3D_SYSTEM = 0x1D;
        int DOREMIKKO_KEYBOARD = 0x1E;
        int ROB_GYRO_SET = 0x1F;
        int FAMICOM_DATA_RECORDER_NO_KEYBOARD = 0x20;
        int ASCII_TURBO_FILE = 0x21;
        int IGS_STORAGE_BATTLE_BOX = 0x22;
        int FAMILY_BASIC_KEYBOARD_AND_DATA_RECORDER = 0x23;
        int DONGDA_PEC_586_KEYBOARD = 0x24;
        int BIT_CORP_BIT_79_KEYBOARD = 0x25;
        int SUBOR_KEYBOARD = 0x26;
        int SUBOR_KEYBOARD_WITH_MOUSE_3X8_BIT_PROTOCOL = 0x27;
        int SUBOR_KEYBOARD_WITH_MOUSE_24_BIT_PROTOCOL = 0x28;
        int SNES_MOUSE = 0x29;
        int MULTICART = 0x2A;
        int TWO_SNES_CONTROLLERS = 0x2B;
        int RACERMATE_BICYCLE = 0x2C;
        int U_FORCE = 0x2D;
        int ROB_STACK_UP = 0x2E;

        String[] NAMES = {
                "Unspecified",                                  // 00
                "Standard controllers",                         // 01
                "NES multitap with 4 standard controllers",     // 02
                "Famicom multitap with 4 standard controllers", // 03
                "VS. System",                                   // 04
                "VS. System with reversed inputs",              // 05
                "VS. Pinball (Japan)",                          // 06
                "VS. Zapper",                                   // 07
                "Zapper",                                       // 08
                "2 Zappers",                                    // 09
                "Bandai Hyper Shot",                            // 0A
                "Power Pad (Side A)",                           // 0B
                "Power Pad (Side B)",                           // 0C
                "Family Trainer (Side A)",                      // 0D
                "Family Trainer (Side B)",                      // 0E
                "NES Arkanoid Vaus",                            // 0F
                "Famicom Arkanoid Vaus",                        // 10
                "2 Vaus controllers with Data Recorder",        // 11
                "Konami Hyper Shot",                            // 12
                "Coconuts Pachinko",                            // 13
                "Exciting Boxing Punching Bag",                 // 14
                "Jissen Mahjong",                               // 15
                "Party Tap",                                    // 16
                "Oeka Kids Tablet",                             // 17
                "Sunsoft Barcode Battler",                      // 18
                "Miracle Piano Keyboard",                       // 19
                "Pokkun Moguraa",                               // 1A
                "Top Rider",                                    // 1B
                "Double-Fisted",                                // 1C
                "Famicom 3D System",                            // 1D
                "Doremikko Keyboard",                           // 1E
                "R.O.B. Gyro Set",                              // 1F
                "Famicom Data Recorder (no keyboard)",          // 20
                "ASCII Turbo File",                             // 21
                "IGS Storage Battle Box",                       // 22
                "Family BASIC Keyboard and Data Recorder",      // 23
                "Dongda PEC-586 Keyboard",                      // 24
                "Bit Corp. Bit-79 Keyboard",                    // 25
                "Subor Keyboard",                               // 26
                "Subor Keyboard with mouse (3x8-bit protocol)", // 27
                "Subor Keyboard with mouse (24-bit protocol)",  // 28
                "SNES Mouse",                                   // 29
                "Multicart",                                    // 2A
                "2 SNES controllers",                           // 2B
                "RacerMate Bicycle",                            // 2C
                "U-Force",                                      // 2D
                "R.O.B. Stack-Up",                              // 2E
        };

        static String toString(final int defaultExpansionDevice) {
            return (defaultExpansionDevice < 0
                    || defaultExpansionDevice >= NAMES.length)
                    ? "Other" : NAMES[defaultExpansionDevice];
        }

        static int toCartDevice(final int defaultExpansionDevice) {
            switch (defaultExpansionDevice) {
                case VS_ZAPPER:
                case ZAPPER:
                case TWO_ZAPPERS:
                    return Zapper;
                case BANDAI_HYPER_SHOT:
                    return BandaiHyperShot;
                case POWER_PAD_SIDE_A:
                case POWER_PAD_SIDE_B:
                    return PowerPad;
                case FAMILY_TRAINER_SIDE_A:
                case FAMILY_TRAINER_SIDE_B:
                    return FamilyTrainer;
                case NES_ARKANOID_VAUS:
                case FAMICOM_ARKANOID_VAUS:
                case TWO_VAUS_CONTROLLERS_WITH_DATA_RECORDER:
                    return Arkanoid;
                case KONAMI_HYPER_SHOT:
                    return KonamiHyperShot;
                case COCONUTS_PACHINKO:
                    return Pachinko;
                case EXCITING_BOXING_PUNCHING_BAG:
                    return ExcitingBoxing;
                case JISSEN_MAHJONG:
                    return Mahjong;
                case PARTY_TAP:
                    return PartyTap;
                case OEKA_KIDS_TABLET:
                    return OekaKids;
                case SUNSOFT_BARCODE_BATTLER:
                    return BarcodeWorld;
                case MIRACLE_PIANO_KEYBOARD:
                    return MiraclePiano;
                case TOP_RIDER:
                    return TopRiderBike;
                case FAMICOM_3D_SYSTEM:
                    return _3DGlasses;
                case ROB_GYRO_SET:
                case ROB_STACK_UP:
                    return RobGyromite;
                case FAMICOM_DATA_RECORDER_NO_KEYBOARD:
                    return DataRecorder;
                case ASCII_TURBO_FILE:
                    return TurboFile;
                case IGS_STORAGE_BATTLE_BOX:
                    return BattleBox;
                case FAMILY_BASIC_KEYBOARD_AND_DATA_RECORDER:
                    return FamilyKeyboard;
                case DONGDA_PEC_586_KEYBOARD:
                    return DongdaPEC586;
                case BIT_CORP_BIT_79_KEYBOARD:
                case SUBOR_KEYBOARD:
                case SUBOR_KEYBOARD_WITH_MOUSE_3X8_BIT_PROTOCOL:
                case SUBOR_KEYBOARD_WITH_MOUSE_24_BIT_PROTOCOL:
                    return Subor;
                case RACERMATE_BICYCLE:
                    return RacerMate;
                case U_FORCE:
                    return UForce;

                case UNSPECIFIED:
                case STANDARD_CONTROLLERS:
                case NES_MULTITAP_WITH_4_STANDARD_CONTROLLERS:
                case FAMICOM_MULTITAP_WITH_4_STANDARD_CONTROLLERS:
                case VS_SYSTEM:
                case VS_SYSTEM_WITH_REVERSED_INPUTS:
                case VS_PINBALL_JAPAN:
                case POKKUN_MOGURAA:
                case DOUBLE_FISTED:
                case SNES_MOUSE:
                case MULTICART:
                case TWO_SNES_CONTROLLERS:
                case DOREMIKKO_KEYBOARD:
                default:
                    return 0;
            }
        }
    }
}