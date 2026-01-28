package nintaco.mappers.bandai;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class FCG extends Mapper {

    protected static final int X24C0X_STANDBY = 0;
    protected static final int X24C0X_ADDRESS = 1;
    protected static final int X24C0X_WORD = 2;
    protected static final int X24C0X_READ = 3;
    protected static final int X24C0X_WRITE = 4;
    private static final long serialVersionUID = 0;
    protected int[] x24c0x_data = new int[256];
    protected int x24c0x_state = X24C0X_STANDBY;
    protected int x24c0x_addr;
    protected int x24c0x_word;
    protected int x24c0x_latch;
    protected int x24c0x_bitcount;
    protected int x24c0x_sda;
    protected int x24c0x_scl;
    protected int x24c0x_out;
    protected int x24c0x_oe;
    protected int x24c02;

    protected boolean irqCounting;
    protected int irqCounter;
    protected int irqLatch;

    public FCG(final CartFile cartFile) {
        super(cartFile, 4, 8, 0x6000, 0x8000);
        setPrgBank(3, -1);
    }

    @Override
    public int readMemory(int address) {
        if (!nonVolatilePrgRamPresent && (address & 0xE000) == 0x6000) {
            return x24c0x_read();
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(int address, int value) {
        switch (address & 0x000F) {
            case 0x08:
                setPrgBank(2, value & 0x0F);
                break;
            case 0x09:
                setNametableMirroring(value & 3);
                break;
            case 0x0A:
                writeIrqControl(value);
                break;
            case 0x0B:
                writeIrqCounterLow(value);
                break;
            case 0x0C:
                writeIrqCounterHigh(value);
                break;
            case 0x0D:
                x24c0x_write(value);
                break;
        }
    }

    protected void writeIrqControl(int value) {
        irqCounting = getBitBool(value, 0);
        irqCounter = irqLatch;
        cpu.interrupt().setMapperIrq(false);
    }

    protected void writeIrqCounterHigh(int value) {
        irqLatch = (irqLatch & 0x00FF) | (value << 8);
    }

    protected void writeIrqCounterLow(int value) {
        irqLatch = (irqLatch & 0xFF00) | value;
    }

    @Override
    public void update() {
        if (irqCounting) {
            irqCounter = (irqCounter - 1) & 0xFFFF;
            if (irqCounter == 0) {
                cpu.interrupt().setMapperIrq(true);
            }
        }
    }

    protected void x24c0x_write(int data) {
        int sda = (data >> 6) & 1;
        int scl = (data >> 5) & 1;
        x24c0x_oe = (data >> 7);

        if (x24c0x_scl != 0 && scl != 0) {
            if (x24c0x_sda != 0 && sda == 0) {        // START
                x24c0x_state = X24C0X_ADDRESS;
                x24c0x_bitcount = 0;
                x24c0x_addr = 0;
            } else if (x24c0x_sda == 0 && sda != 0) { //STOP
                x24c0x_state = X24C0X_STANDBY;
            }
        } else if (x24c0x_scl == 0 && scl != 0) {        // RISING EDGE
            switch (x24c0x_state) {
                case X24C0X_ADDRESS:
                    if (x24c0x_bitcount < 7) {
                        x24c0x_addr <<= 1;
                        x24c0x_addr |= sda;
                    } else {
                        if (x24c02 == 0) { // X24C01 mode
                            x24c0x_word = x24c0x_addr;
                        }
                        if (sda != 0) {                // READ COMMAND
                            x24c0x_state = X24C0X_READ;
                        } else {                // WRITE COMMAND
                            if (x24c02 != 0) {            // X24C02 mode
                                x24c0x_state = X24C0X_WORD;
                            } else {
                                x24c0x_state = X24C0X_WRITE;
                            }
                        }
                    }
                    x24c0x_bitcount++;
                    break;
                case X24C0X_WORD:
                    if (x24c0x_bitcount == 8) {    // ACK
                        x24c0x_word = 0;
                        x24c0x_out = 0;
                    } else {                    // WORD ADDRESS INPUT
                        x24c0x_word <<= 1;
                        x24c0x_word |= sda;
                        if (x24c0x_bitcount == 16) {    // END OF ADDRESS INPUT
                            x24c0x_bitcount = 7;
                            x24c0x_state = X24C0X_WRITE;
                        }
                    }
                    x24c0x_bitcount++;
                    break;
                case X24C0X_READ:
                    if (x24c0x_bitcount == 8) {    // ACK
                        x24c0x_out = 0;
                        x24c0x_latch = x24c0x_data[x24c0x_word];
                        x24c0x_bitcount = 0;
                    } else {                    // REAL OUTPUT
                        x24c0x_out = x24c0x_latch >> 7;
                        x24c0x_latch <<= 1;
                        x24c0x_bitcount++;
                        if (x24c0x_bitcount == 8) {
                            x24c0x_word++;
                            x24c0x_word &= 0xff;
                        }
                    }
                    break;
                case X24C0X_WRITE:
                    if (x24c0x_bitcount == 8) {    // ACK
                        x24c0x_out = 0;
                        x24c0x_latch = 0;
                        x24c0x_bitcount = 0;
                    } else {                    // REAL INPUT
                        x24c0x_latch <<= 1;
                        x24c0x_latch |= sda;
                        x24c0x_bitcount++;
                        if (x24c0x_bitcount == 8) {
                            x24c0x_data[x24c0x_word] = x24c0x_latch;
                            x24c0x_word++;
                            x24c0x_word &= 0xff;
                        }
                    }
                    break;
            }
        }

        x24c0x_sda = sda;
        x24c0x_scl = scl;
    }

    protected int x24c0x_read() {
        return x24c0x_out << 4;
    }
}
