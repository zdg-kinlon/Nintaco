package cn.kinlon.emu.mappers.nintendo.vs;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.gamepad.GamepadMapper;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.cpu.CPU.REG_INPUT_PORT_1;
import static cn.kinlon.emu.cpu.CPU.REG_OUTPUT_PORT;
import static cn.kinlon.emu.input.InputDevices.isGamepad;
import static cn.kinlon.emu.mappers.NametableMirroring.FOUR_SCREEN;
import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class SubVsDualSystem extends Mapper {

    private static final long serialVersionUID = 0;

    private MainVsDualSystem mainVsDualSystem;

    public SubVsDualSystem(final CartFile cartFile) {
        super(new SplitCartFile(cartFile, false), 8, 1);
        setNametableMirroring(FOUR_SCREEN);
        setPrgBank(4, 0);
        setPrgBank(5, 1);
        setPrgBank(6, 2);
        setPrgBank(7, 3);
    }

    @Override
    public boolean isVsDualSystem() {
        return true;
    }

    public void setMainVsDualSystem(
            final MainVsDualSystem mainVsDualSystem) {
        this.mainVsDualSystem = mainVsDualSystem;
    }

    @Override
    public int readCpuMemory(final int address) {
        if ((address & 0xE000) == 0x6000) {
            return mainVsDualSystem.readCpuMemory(address);
        } else if (address == REG_INPUT_PORT_1) {
            return 0x80 | super.readCpuMemory(address);
        } else {
            return super.readCpuMemory(address);
        }
    }

    @Override
    public void writeCpuMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            mainVsDualSystem.writeCpuMemory(address, value);
        } else {
            if (address == REG_OUTPUT_PORT) {
                writeBankSelect(value);
            }
            super.writeCpuMemory(address, value);
        }
    }

    public void setMapperIrq(final boolean value) {
        cpu.setMapperIrq(value);
    }

    private void writeBankSelect(final int value) {

        final int bank = getBit(value, 2);
        setChrBank(bank);
        if (prgRomLength > 0x8000) {
            setPrgBank(4, bank << 2);
        }

        mainVsDualSystem.setMapperIrq(!getBitBool(value, 1));
    }

    @Override
    public void updateButtons(final int buttons) {
        this.buttons = buttons;
        if (coinInserted > 0 && --coinInserted == 0) {
            coinMask = 0x00;
        }
        if (serviceButtonPressed > 0 && --serviceButtonPressed == 0) {
            serviceButtonMask = 0x00;
        }
    }

    @Override
    public void writeOutputPort(final int value) {
        final DeviceMapper[] mappers = deviceMappers;
        for (int i = mappers.length - 1; i >= 0; i--) {
            final DeviceMapper mapper = mappers[i];
            if (isGamepad(mapper.getInputDevice())
                    && ((GamepadMapper) mapper).getPortIndex() > 1) {
                mappers[i].writePort(value);
            }
        }
    }

    @Override
    public int readInputPort(int portIndex) {
        final DeviceMapper[] mappers = deviceMappers;
        int value;
        if (portIndex == 0) {
            value = coinMask | ((dipSwitchesValue & 0x03) << 3) | serviceButtonMask;
        } else {
            value = dipSwitchesValue & 0xFC;
        }
        portIndex += 2;
        for (int i = mappers.length - 1; i >= 0; i--) {
            value |= mappers[i].readPort(portIndex);
        }
        return value;
    }

    @Override
    public int peekInputPort(int portIndex) {
        final DeviceMapper[] mappers = deviceMappers;
        int value;
        if (portIndex == 0) {
            value = coinMask | ((dipSwitchesValue & 0x03) << 3) | serviceButtonMask;
        } else {
            value = dipSwitchesValue & 0xFC;
        }
        portIndex += 2;
        for (int i = mappers.length - 1; i >= 0; i--) {
            value |= mappers[i].peekPort(portIndex);
        }
        return value;
    }
}