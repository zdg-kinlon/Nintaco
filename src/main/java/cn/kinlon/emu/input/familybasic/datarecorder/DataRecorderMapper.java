package cn.kinlon.emu.input.familybasic.datarecorder;

import cn.kinlon.emu.cpu.CPU;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;

import java.io.*;

import static cn.kinlon.emu.input.familybasic.datarecorder.DataRecorderMode.*;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

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

}