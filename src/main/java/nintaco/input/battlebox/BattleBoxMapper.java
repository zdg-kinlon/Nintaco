package nintaco.input.battlebox;

import nintaco.input.DeviceMapper;
import nintaco.preferences.GamePrefs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static java.util.Arrays.fill;
import static nintaco.input.InputDevices.BattleBox;
import static nintaco.input.battlebox.BattleBoxMapper.Command.*;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.BitUtil.reverseBits;
import static nintaco.util.StreamUtil.readSparseByteArray;
import static nintaco.util.StreamUtil.writeSparseByteArray;

public class BattleBoxMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;
    private transient int[] sram = new int[512];
    private int dataIn;
    private int dataOut;
    private int input;
    private int bitsIn;
    private int output;
    private int addr0;
    private boolean clock;
    private boolean lastClock;
    private boolean eraseWriteEnabled;
    public BattleBoxMapper() {
        final int[] battleBoxRam = GamePrefs.getInstance().getStorageUnitRam();
        if (battleBoxRam != null && battleBoxRam.length == 512) {
            sram = battleBoxRam;
        }
    }

    @Override
    public int getInputDevice() {
        return BattleBox;
    }

    @Override
    public void update(final int buttons) {
    }

    @Override
    public void writePort(final int value) {

        clock = getBitBool(value, 0);

        if (clock) {
            if (!lastClock) {
                input = (input << 1) | dataIn;
                switch (++bitsIn) {
                    case 16:
                        bitsIn = 32;
                        switch ((input >> 4) & 0x0F) {
                            case Read: {
                                final int address = ((input >> 7) & 0x01FC) | addr0;
                                output = (reverseBits(sram[address | 2]) << 8)
                                        | reverseBits(sram[address]);
                                break;
                            }
                            case Program:
                                if (eraseWriteEnabled) {
                                    bitsIn = 16;
                                }
                                break;
                            case ChipErase:
                                if (eraseWriteEnabled) {
                                    fill(sram, (short) 0);
                                }
                                break;
                            case BusyMonitor:
                                output = -1;
                                break;
                            case EWEnable:
                                eraseWriteEnabled = true;
                                break;
                            case EWDisable:
                                eraseWriteEnabled = false;
                                break;
                        }
                        break;
                    case 32: {
                        final int address = ((input >> 23) & 0x01FC) | addr0;
                        sram[address] = (input >> 8) & 0xFF;
                        sram[address | 2] = input & 0xFF;
                        break;
                    }
                }
            }
        } else if (lastClock) {
            dataOut = output & 1;
            output >>= 1;
        }

        lastClock = clock;
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            dataIn ^= 1;
            if (clock) {
                output = bitsIn = 0;
                addr0 ^= 1;
            }
            return ((dataIn << 4) | (dataOut << 3)) ^ 0x18;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return ((dataIn << 4) | (dataOut << 3)) ^ 0x18;
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

    public interface Command {
        int Read = 0b1000;
        int Program = 0b0110;
        int ChipErase = 0b0011;
        int BusyMonitor = 0b1011;
        int EWEnable = 0b1001;
        int EWDisable = 0b1101;
    }
}