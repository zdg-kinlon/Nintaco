package nintaco.input.familybasic.datarecorder;

import nintaco.CPU;
import nintaco.Machine;
import nintaco.input.DeviceMapper;
import nintaco.input.InputDevices;

import java.io.*;

import static nintaco.input.familybasic.datarecorder.DataRecorderMode.*;
import static nintaco.util.BitUtil.getBitBool;

public class DataRecorderMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final BitList bitList = new BitList();

    private volatile CPU cpu; // TODO THIS COULD BE NULL, REVIEW

    private int recorderMode = Stop;
    private long counter;
    private boolean enabled;
    private int readIndex;
    private int samplingPeriod;

    @Override
    public void setMachine(final Machine machine) {
        if (machine == null) {
            cpu = null;
            recorderMode = Stop;
        } else {
            cpu = machine.getCPU();
        }
    }

    @Override
    public int getInputDevice() {
        return InputDevices.DataRecorder;
    }

    @Override
    public void update(final int buttons) {
    }

    public int getDataRecorderMode() {
        return recorderMode;
    }

    public void setDataRecorderMode(final int mode, final int samplingPeriod) {
        this.samplingPeriod = samplingPeriod;
        if (mode != recorderMode) {
            if (mode == Record || mode == Erase) {
                bitList.clear();
            }
            readIndex = 0;
            counter = cpu.getCycleCounter();
            recorderMode = mode == Erase ? Stop : mode;
        }
    }

    @Override
    public void writePort(final int value) {
        enabled = getBitBool(value, 2);
        if (enabled && recorderMode == Record) {
            final int bit = value & 1;
            final long cycleCounter = cpu.getCycleCounter();
            while (counter < cycleCounter) {
                counter += samplingPeriod;
                bitList.add(bit);
            }
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (enabled && portIndex == 0 && recorderMode == Play
                && readIndex < bitList.size()) {
            final long cycleCounter = cpu.getCycleCounter();
            while (counter < cycleCounter) {
                counter += samplingPeriod;
                if (++readIndex >= bitList.size()) {
                    return 0;
                }
            }
            return bitList.get(readIndex) << 1;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (enabled && portIndex == 0 && recorderMode == Play
                && !bitList.isEmpty()) {
            return bitList.get(readIndex) << 1;
        } else {
            return 0;
        }
    }

    public void saveTape(final File file) throws Throwable {
        try (final DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            bitList.save(out);
        }
    }

    public void loadTape(final File file) throws Throwable {
        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(file)))) {
            bitList.load(in);
        }
    }
}