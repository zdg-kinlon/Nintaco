package cn.kinlon.emu.files;

import java.io.*;

import static cn.kinlon.emu.mappers.NametableMirroring.FOUR_SCREEN;
import static cn.kinlon.emu.mappers.NametableMirroring.VERTICAL;
import static cn.kinlon.emu.utils.BitUtil.log2;
import static cn.kinlon.emu.utils.BitUtil.setBit;
import static cn.kinlon.emu.utils.StreamUtil.*;

// During construction, only the header is decoded. The rest of the file is
// read into the prgROM array. The only mutable part is the header.
public class MutableNesFile extends NesFile implements Serializable, Cloneable {

    private static final long serialVersionUID = 0;

    public MutableNesFile(final DataInputStream in, final long fileSize,
                          final String entryFileName, final String archiveFileName)
            throws Throwable {
        super(in, fileSize, entryFileName, archiveFileName, false);
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

}