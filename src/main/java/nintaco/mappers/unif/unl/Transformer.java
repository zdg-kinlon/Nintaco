package nintaco.mappers.unif.unl;

import java.util.*;

import nintaco.files.*;
import nintaco.input.*;
import nintaco.input.familybasic.transformer.*;
import nintaco.mappers.*;

public class Transformer extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] queue = new int[256];

    private boolean[] pressed0 = new boolean[256];
    private boolean[] pressed1 = new boolean[256];
    private int head;
    private int tail;
    private int cpuCycleCounter;
    private TransformerMapper transformerMapper;

    public Transformer(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, -1);
        setChrBank(0);
    }

    @Override
    public void setDeviceMappers(final DeviceMapper[] deviceMappers) {
        super.setDeviceMappers(deviceMappers);
        transformerMapper = null;
        for (int i = deviceMappers.length - 1; i >= 0; i--) {
            if (deviceMappers[i].getInputDevice()
                    == InputDevices.TransformerKeyboard) {
                transformerMapper = (TransformerMapper) deviceMappers[i];
                break;
            }
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x5000 && address <= 0x5004) {
            int value = (head != tail) ? queue[tail] : 0;
            switch (address & 3) {
                case 0:
                    value = value & 0x0F;
                    break;
                case 1:
                    value = value >> 4;
                    break;
                case 2:
                    if (head != tail) {
                        tail = (tail + 1) & 0xFF;
                    }
                    break;
            }
            cpu.setMapperIrq(false);
            return value;
        } else {
            return super.readMemory(address);
        }
    }

    private void enqueue(final int value) {
        queue[head] = value;
        head = (head + 1) & 0xFF;
    }

    @Override
    public void update() {
        if (++cpuCycleCounter == 1000) {
            cpuCycleCounter = 0;

            final TransformerMapper mapper = transformerMapper;
            if (mapper != null) {
                Arrays.fill(pressed1, false);
                int scanCodes = mapper.getScanCodes();
                for (int i = 2; i >= 0; i--) {
                    if (scanCodes == 0) {
                        break;
                    }
                    pressed1[scanCodes & 0xFF] = true;
                    scanCodes >>>= 8;
                }
                for (int i = 255; i >= 0; i--) {
                    if (pressed1[i] && !pressed0[i]) {
                        enqueue(i);
                    } else if (!pressed1[i] && pressed0[i]) {
                        enqueue(0x80 ^ i);
                    }
                }
                final boolean[] temp = pressed0;
                pressed0 = pressed1;
                pressed1 = temp;
                if (head != tail) {
                    cpu.setMapperIrq(true);
                }
            }
        }
    }
}