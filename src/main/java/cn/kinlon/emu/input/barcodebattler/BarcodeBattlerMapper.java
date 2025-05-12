package cn.kinlon.emu.input.barcodebattler;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import java.io.Serializable;

public class BarcodeBattlerMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private String data;
    private int dataIndex;
    private int bitIndex;

    @Override
    public int getInputDevice() {
        return InputDevices.BarcodeBattler;
    }

    @Override
    public void update(final int buttons) {
        if (data == null) {
            final String barcode = InputUtil.getBarcode();
            if (barcode != null) {
                if (barcode.length() == 8) {
                    data = "     " + barcode + "SUNSOFT";
                    dataIndex = -1;
                } else if (barcode.length() == 13) {
                    data = barcode + "SUNSOFT";
                    dataIndex = -1;
                }
            }
        }
    }

    @Override
    public void writePort(final int value) {
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1 && data != null) {
            if (dataIndex == -1) {
                dataIndex = 0;
                bitIndex = -1;
                return 0x04;
            } else if (bitIndex == -1) {
                bitIndex = 0;
                return 0x04;
            } else if (bitIndex == 8) {
                bitIndex = -1;
                if (++dataIndex == data.length()) {
                    data = null;
                }
                return 0x00;
            } else {
                final int value = ((~data.charAt(dataIndex) >> bitIndex) & 1) << 2;
                bitIndex++;
                return value;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (portIndex == 1 && data != null) {
            if (dataIndex == -1 || bitIndex == -1) {
                return 0x04;
            } else if (bitIndex == 8) {
                return 0x00;
            } else {
                final String d = data;
                final int index = dataIndex;
                if (d != null && index < data.length()) {
                    return ((~d.charAt(index) >> bitIndex) & 1) << 2;
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }
}