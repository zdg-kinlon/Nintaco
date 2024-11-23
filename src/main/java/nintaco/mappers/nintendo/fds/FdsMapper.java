package nintaco.mappers.nintendo.fds;

import nintaco.files.FdsFile;
import nintaco.mappers.Mapper;

import static nintaco.files.FdsFile.SIDE_SIZE;
import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class FdsMapper extends Mapper {

    private static final long serialVersionUID = 0;

    private final FdsAudio audio = new FdsAudio();

    private int side;
    private int head;
    private int writeSkip;
    private int irqTimer;
    private int irqTimerResetValue;
    private int irqDiskSeek;
    private int targetSide;
    private int insertDelay;
    private boolean irqTimerEnabled;
    private boolean diskIOEnabled;
    private boolean diskInserted = true;
    private boolean motorOn;
    private boolean readMode;
    private boolean irqByteTransferEnabled;
    private boolean readWriteReady;
    private boolean crcControl;
    private boolean transferReset;
    private boolean irqTimerOccurred;
    private boolean irqLoopTimer;
    private boolean irqByteTransferOccurred;
    private boolean activity;

    public FdsMapper(final FdsFile fdsFile) {
        super(fdsFile);
    }

    @Override
    public boolean isDiskActivity() {
        final boolean value = activity;
        activity = false;
        return value;
    }

    @Override
    public boolean isFdsMapper() {
        return true;
    }

    @Override
    public int getDiskSideCount() {
        return diskData.length;
    }

    @Override
    public int getDiskSide() {
        return diskInserted ? side : -1;
    }

    @Override
    public void setDiskSide(final int side) {
        if (side >= 0 && side < diskData.length) {
            targetSide = side;
            insertDelay = 0x200000;
            diskInserted = false;
        }
    }

    @Override
    public void ejectDisk() {
        diskInserted = false;
    }

    @Override
    public void writeMemory(int address, int value) {
        memory[address] = value;
        if (audio.writeRegister(address, value)) {
            return;
        }
        switch (address) {
            case 0x4020:
                writeIrqTimerLow(value);
                break;
            case 0x4021:
                writeIrqTimerHigh(value);
                break;
            case 0x4022:
                writeIrqTimerEnable(value);
                break;
            case 0x4023:
                writeMasterIOEnable(value);
                break;
            case 0x4024:
                writeDataRegister(value);
                break;
            case 0x4025:
                writeFdsControls(value);
                break;
            case 0x4026:
                writeExternalConnector(value);
                break;
        }
    }

    @Override
    public int readMemory(int address) {
        final int value = audio.readRegister(address);
        if (value >= 0) {
            return value;
        }
        switch (address) {
            case 0x4030:
                return readDiskStatusRegister0();
            case 0x4031:
                return readDataRegister();
            case 0x4032:
                return readDiskDriveStatusRegister();
            case 0x4033:
                return readExternalConnector();
            default:
                return memory[address];
        }
    }

    private void writeIrqTimerLow(final int value) {
        irqTimerOccurred = false;
        updateIrq();
        irqTimerResetValue = (irqTimerResetValue & 0xFF00) | value;
    }

    private void writeIrqTimerHigh(final int value) {
        irqTimerOccurred = false;
        updateIrq();
        irqTimerResetValue = (irqTimerResetValue & 0x00FF) | (value << 8);
    }

    private void writeIrqTimerEnable(final int value) {
        irqTimerOccurred = false;
        updateIrq();
        irqTimer = irqTimerResetValue;
        irqLoopTimer = getBitBool(value, 0);
        irqTimerEnabled = getBitBool(value, 1);
    }

    private void writeMasterIOEnable(final int value) {
        diskIOEnabled = getBitBool(value, 0);
    }

    private void writeDataRegister(final int value) {
        if (diskInserted && !readMode && diskIOEnabled && head >= 0
                && head < SIDE_SIZE) {
            if (writeSkip > 0) {
                writeSkip--;
            } else if (head >= 2) {
                diskData[side][head - 2] = value;
                activity = true;
            }
        }
    }

    private void writeFdsControls(final int value) {
        irqByteTransferOccurred = false;
        updateIrq();

        boolean priorReadWriteReady = readWriteReady;

        motorOn = getBitBool(value, 0);
        transferReset = getBitBool(value, 1);
        readMode = getBitBool(value, 2);
        setNametableMirroring(getBit(value, 3));
        crcControl = getBitBool(value, 4);
        readWriteReady = getBitBool(value, 6);
        irqByteTransferEnabled = getBitBool(value, 7);

        if (diskInserted) {
            if (!readWriteReady) {
                if (priorReadWriteReady && !crcControl) {
                    irqDiskSeek = 200;
                    head -= 2;
                }
                if (head < 0) {
                    head = 0;
                }
            } else {
                irqDiskSeek = 200;
            }
            if (!readMode) {
                writeSkip = 2;
            }
            if (transferReset) {
                head = 0;
                irqDiskSeek = 200;
            }
        }
    }

    private void writeExternalConnector(int value) {
    }

    private int readDiskStatusRegister0() {
        int value = 0;
        if (irqTimerOccurred) {
            value |= 1;
        }
        if (irqByteTransferOccurred) {
            value |= 2;
        }
        irqTimerOccurred = false;
        irqByteTransferOccurred = false;
        updateIrq();
        return value;
    }

    private int readDataRegister() {
        int value = 0;
        if (diskInserted) {
            value = diskData[side][head];
            activity = true;
            if (head < 64999) {
                head++;
            }
            irqDiskSeek = 150;
            irqByteTransferOccurred = false;
            updateIrq();
        }
        return value;
    }

    private int readDiskDriveStatusRegister() {
        int value = 0;
        if (!diskInserted) {
            value = 7;
        } else if (!motorOn || transferReset) {
            value = 2;
        }
        return value;
    }

    private int readExternalConnector() {
        return 0x80; // low battery
    }

    private void updateIrq() {
        cpu.setMapperIrq(irqByteTransferOccurred || irqTimerOccurred);
    }

    @Override
    public void update() {
        if (irqTimerEnabled && irqTimer > 0 && --irqTimer == 0) {
            irqTimerOccurred = true;
            if (irqLoopTimer) {
                irqTimer = irqTimerResetValue;
            } else {
                irqTimerEnabled = false;
            }
            updateIrq();
        }
        if (irqDiskSeek > 0 && --irqDiskSeek == 0 && irqByteTransferEnabled) {
            irqByteTransferOccurred = true;
            updateIrq();
        }
        if (insertDelay > 0 && --insertDelay == 0) {
            side = targetSide;
            diskInserted = true;
        }

        audio.update();
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }

    @Override
    public int getAudioMixerScale() {
        return audio.getAudioMixerScale();
    }
}