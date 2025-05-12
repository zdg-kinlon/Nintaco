package cn.kinlon.emu.input.turbofile;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.preferences.GamePrefs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.TurboFile;
import static cn.kinlon.emu.utils.StreamUtil.readSparseByteArray;
import static cn.kinlon.emu.utils.StreamUtil.writeSparseByteArray;

public class TurboFileMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int WRITE_BIT = 0x01;
    private static final int NO_RESET = 0x02;
    private static final int WRITE_ENABLE = 0x04;
    private static final int READ_BIT = 0x04;

    private transient int[] sram = new int[0x2000];

    private int address = 0x0000;
    private int bit = 0x01;
    private int output;
    private boolean writeEnabled;

    public TurboFileMapper() {
        final int[] turboFileRam = GamePrefs.getInstance().getStorageUnitRam();
        if (turboFileRam != null && turboFileRam.length == 0x2000) {
            sram = turboFileRam;
        }
    }

    @Override
    public int getInputDevice() {
        return TurboFile;
    }

    @Override
    public void update(final int buttons) {
    }

    @Override
    public void writePort(final int value) {
        if ((value & NO_RESET) == 0) {
            address = 0x0000;
            bit = 0x01;
        }
        final boolean advance = writeEnabled;
        writeEnabled = (value & WRITE_ENABLE) != 0;
        if (writeEnabled) {
            sram[address] &= ~bit;
            if ((value & WRITE_BIT) != 0) {
                sram[address] |= bit;
            }
        } else if (advance) {
            if (bit != 0x80) {
                bit <<= 1;
            } else {
                bit = 0x01;
                address = (address + 1) & 0x1FFF;
            }
        }
        output = (sram[address] & bit) != 0 ? READ_BIT : 0x00;
    }

    @Override
    public int readPort(final int portIndex) {
        return portIndex == 1 ? output : 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }

    @Override
    public void close(final boolean saveNonVolatileData) {
        if (saveNonVolatileData) {
            GamePrefs.getInstance().setStorageUnitRam(sram);
            GamePrefs.save();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        sram = readSparseByteArray(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeSparseByteArray(out, sram);
    }
}