package nintaco.files;

import java.io.*;

import static nintaco.mappers.NametableMirroring.FOUR_SCREEN;
import static nintaco.mappers.NametableMirroring.VERTICAL;
import static nintaco.util.BitUtil.log2;
import static nintaco.util.BitUtil.setBit;
import static nintaco.util.StreamUtil.*;

// During construction, only the header is decoded. The rest of the file is
// read into the prgROM array. The only mutable part is the header.
public class MutableNesFile extends NesFile implements Serializable, Cloneable {

    private static final long serialVersionUID = 0;

    public MutableNesFile(final DataInputStream in, final long fileSize)
            throws Throwable {
        this(in, fileSize, "", "");
    }

    public MutableNesFile(final DataInputStream in, final long fileSize,
                          final String entryFileName, final String archiveFileName)
            throws Throwable {
        super(in, fileSize, entryFileName, archiveFileName, false);
    }

    @Override
    protected void readROMs(final DataInputStream in,
                            final long fileSize) throws Throwable {

        trainer = new int[0];
        chrROM = new int[0];

        // Read the entire file body without decoding it.
        prgROM = new int[(int) (fileSize - header.length)];
        readBytes(in, prgROM);
    }

    public void write(final String fileName) throws Throwable {
        write(new File(fileName));
    }

    public void write(final File file) throws Throwable {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(file)))) {
            write(out);
        }
    }

    public void write(final DataOutputStream out) throws Throwable {

        // 0, 1, 2, 3
        writeString(out, HEADER_ID);

        // 4
        out.write(prgRomPages);

        // 5
        out.write(chrRomPages);

        int _6 = (mapperNumber & 0x0F) << 4;
        _6 = setBit(_6, 1, nonVolatilePrgRamPresent);
        _6 = setBit(_6, 2, trainerPresent);
        if (mirroring == VERTICAL) {
            _6 = setBit(_6, 0);
        } else if (mirroring == FOUR_SCREEN) {
            _6 = setBit(_6, 3);
        }
        out.write(_6);

        int _7 = (mapperNumber & 0xF0) | (console & 0x03);
        _7 = setBit(_7, 3, nes20Format);
        out.write(_7);

        if (nes20Format) {

            // 8
            out.write(((submapperNumber & 0x0F) << 4) | ((mapperNumber >> 8) & 0x0F));

            // 9
            out.write(((chrRomPages >> 4) & 0xF0) | ((prgRomPages >> 8) & 0x0F));

            // A
            out.write(((encodeRamSize(nonVolatilePrgRamSize) & 0x0F) << 4)
                    | (encodeRamSize(prgRamSize) & 0x0F));

            // B
            out.write(((encodeRamSize(nonVolatileChrRamSize) & 0x0F) << 4)
                    | (encodeRamSize(chrRamSize) & 0x0F));

            // C      
            out.write(cpuPpuTiming & 0x03);

            int D;
            switch (console) {
                case Console.VS_SYSTEM:
                    D = ((vsHardware & 0x0F) << 4) | (vsPPU & 0x0F);
                    break;
                default:
                    D = extendedConsole & 0x0F;
                    break;
            }
            out.write(D);

            // E
            out.write(miscellaneousROMs & 0x03);

            // F
            out.write(defaultExpansionDevice & 0x3F);
        } else {

            // 8
            out.write(prgRamSize / PRG_RAM_PAGE_SIZE);

            // 9
            out.write(cpuPpuTiming == CpuPpuTiming.PAL ? 1 : 0);

            // A, B, C, D, E, F
            out.write(new byte[6]);
        }

        // Write the entire file body.
        writeBytes(out, prgROM);
    }

    private int encodeRamSize(final int ramSize) {

        if (ramSize == 0) {
            return 0;
        }

        return log2(ramSize) - 6;
    }

    public void setChrRomPages(final int chrRomPages) {
        this.chrRomPages = chrRomPages;
    }

    public void setPrgRomPages(final int prgRomPages) {
        this.prgRomPages = prgRomPages;
    }

    public void setPrgRamSize(final int prgRamSize) {
        this.prgRamSize = prgRamSize;
    }

    public void setSubmapperNumber(final int submapperNumber) {
        this.submapperNumber = submapperNumber;
    }

    public void setMirroring(final int mirroring) {
        this.mirroring = mirroring;
    }

    public void setNonVolatilePrgRamSize(final int NonVolatilePrgRamSize) {
        this.nonVolatilePrgRamSize = NonVolatilePrgRamSize;
    }

    public void setNonVolatileChrRamSize(final int NonVolatileChrRamSize) {
        this.nonVolatileChrRamSize = NonVolatileChrRamSize;
    }

    public void setChrRamSize(final int chrRamSize) {
        this.chrRamSize = chrRamSize;
    }

    public void setPrgRamPages(final int prgRamPages) {
        this.prgRamPages = prgRamPages;
    }

    public void setTrainerPresent(final boolean trainerPresent) {
        this.trainerPresent = trainerPresent;
    }

    public void setCpuPpuTiming(final int cpuPpuTiming) {
        this.cpuPpuTiming = cpuPpuTiming;
    }

    public void setVsPPU(final int vsPPU) {
        this.vsPPU = vsPPU;
    }

    public void setVsHardware(final int vsHardware) {
        this.vsHardware = vsHardware;
    }

    public void setNonVolatilePrgRamPresent(final boolean sramPresent) {
        this.nonVolatilePrgRamPresent = sramPresent;
    }

    public void setConsole(final int console) {
        this.console = console;
    }

    public void setExtendedConsole(final int extendedConsole) {
        this.extendedConsole = extendedConsole;
    }

    public void setMiscellaneousROMs(final int miscellaneousROMs) {
        this.miscellaneousROMs = miscellaneousROMs;
    }

    public void setDefaultExpansionDevice(final int defaultExpansionDevice) {
        this.defaultExpansionDevice = defaultExpansionDevice;
    }

    public void setNes20Format(final boolean nintaco20Format) {
        this.nes20Format = nintaco20Format;
    }

    public void setMapperNumber(final int mapperNumber) {
        this.mapperNumber = mapperNumber;
    }

    public MutableNesFile copy() {
        try {
            return (MutableNesFile) clone();
        } catch (Throwable t) {
            return null;
        }
    }
}