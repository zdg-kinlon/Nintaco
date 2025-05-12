package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static java.util.Arrays.fill;

public class _3D_Block extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int PHASE_NONE = 0;
    private static final int PHASE_RAISE = 1;
    private static final int PHASE_HOLD = 2;
    private static final int PHASE_RELEASE = 3;

    private int PICCounter;
    private int LastPICAddress;
    private int LastFunction;
    private int irqsToRaise;
    private int irqRaiseCount;
    private int irqHoldCount;
    private int irqReleaseCountOdd;
    private int irqReleaseCountEven;
    private int irqCounter;
    private int irqPhase;
    private int maxCounter;

    // "3D Block"-specific variables
    private int _3dLatchBits;
    private int _3dLatch;
    private int _3dCounter;

    // "Block Force"-specific variables
    private final int[] bfRAM = new int[32];
    private int bfAddr;
    private int bfLatch;

    // "3D Block"'s game state
    private int _3dX;
    private int _3dY;
    private int _3dZ;
    private int _3dMinZ;
    private int _3dHitGround;
    private int _3dNewZ;
    private int _3dNewY;
    private int _3dNewX;
    private int _3dMaxZ;
    private int _3dMinY;
    private int _3dMaxY;
    private int _3dMinX;
    private int _3dMaxX;
    private int _3dCommand;
    private int byte2DD;
    private int byte2DE;
    private int byte2E1;
    private int byte2E2;
    private int byte2E5;
    private int byte2E6;

    public _3D_Block(final CartFile cartFile) {
        super(cartFile, 1, 1);
    }

    @Override
    public void init() {
        fill(bfRAM, 0);
        bfRAM[0x1A] = 0x02;
        bfRAM[0x1C] = 0x05;
        bfRAM[0x1D] = 0x06;
        PICCounter = LastPICAddress = _3dLatch = irqsToRaise = irqRaiseCount
                = irqHoldCount = irqReleaseCountOdd = irqReleaseCountEven = irqCounter
                = irqPhase = maxCounter = 0;
    }

    @Override
    public void resetting() {
        init();
    }

    private void _3dUp() {
        if (_3dY > _3dMinY) {
            --_3dY;
        }
    }

    private void _3dDown() {
        if (_3dY < _3dMaxY) {
            ++_3dY;
        }
    }

    private void _3dLeft() {
        if (_3dX > _3dMinX) {
            --_3dX;
        }
    }

    private void _3dRight() {
        if (_3dX < _3dMaxX) {
            ++_3dX;
        }
    }

    private int _3dGetMin(final int val) {
        return ((val & 7) == 0) ? 2 : (((val & 1) != 0) ? 1 : 0);
    }

    private int _3dGetMax(final int val) {
        return ((val & 7) == 2) ? 5 : (((val & 7) == 3) ? 4 : 3);
    }

    private void _3dProcess() {

        final int byte1 = (_3dLatch >> 24) & 0xFF;
        final int byte2 = (_3dLatch >> 16) & 0xFF;
        final int byte3 = (_3dLatch >> 8) & 0xFF;
        final int byte4 = _3dLatch & 0xFF;

        byte2E1 = (byte1 >> 3) & 7;
        byte2DD = (byte1 >> 6) & 3;
        byte2DE = byte1 & 7;

        byte2E6 = (byte2 >> 3) & 7;
        byte2E2 = ((byte2 >> 6) & 3) | ((byte3 & 8) >> 1);
        byte2E5 = byte2 & 7;

        _3dZ = (byte3 >> 4) & 15;
        _3dX = byte3 & 7;

        _3dY = byte4 >> 5;
        _3dCommand = byte4 & 0x1F;

        _3dMinZ = ((byte2DD & 3) == 0) ? 2 : (((byte2DD & 1) != 0) ? 1 : 0);
        _3dMaxZ = ((byte2E2 & 3) == 0) ? 8 : (((byte2E2 & 1) != 0) ? 9 : 10);
        _3dMinX = _3dGetMin(byte2E1);
        _3dMinY = _3dGetMin(byte2DE);
        _3dMaxX = _3dGetMax(byte2E6);
        _3dMaxY = _3dGetMax(byte2E5);

        switch (_3dCommand & 0x1F) {
            case 0x08:
                ++_3dZ;
                break;
            case 0x10:
                _3dUp();
                break;
            case 0x11:
                _3dUp();
                _3dRight();
                break;
            case 0x12:
                _3dRight();
                break;
            case 0x13:
                _3dRight();
                _3dDown();
                break;
            case 0x14:
                _3dDown();
                break;
            case 0x15:
                _3dDown();
                _3dLeft();
                break;
            case 0x16:
                _3dLeft();
                break;
            case 0x17:
                _3dLeft();
                _3dUp();
                break;
        }

        if (_3dZ < _3dMinZ) {
            _3dHitGround = 0;
            _3dNewZ = _3dMinZ;
        } else if (_3dZ >= _3dMaxZ) {
            _3dHitGround = 1;
            _3dNewZ = _3dMaxZ;
        } else {
            _3dHitGround = 0;
            _3dNewZ = _3dZ;
        }

        if (_3dX < _3dMinX) {
            _3dNewX = _3dMinX;
        } else if (_3dX >= _3dMaxX) {
            _3dNewX = _3dMaxX;
        } else {
            _3dNewX = _3dX;
        }

        if (_3dY < _3dMinY) {
            _3dNewY = _3dMinY;
        } else if (_3dY >= _3dMaxY) {
            _3dNewY = _3dMaxY;
        } else {
            _3dNewY = _3dY;
        }

        _3dLatch = (_3dHitGround << 24) | (_3dNewY << 16) | (_3dNewX << 8)
                | _3dNewZ;
    }

    private void CheckAddress(final int Addr) {
        final int PICAddress = Addr >> 4;
        if (PICAddress == LastPICAddress) {
            ++PICCounter;
        } else {
            PICCounter = 0;
            LastFunction = 0xFF;
        }
        LastPICAddress = PICAddress;
        if (PICCounter == 100 && PICAddress >= 0xFE0 && PICAddress <= 0xFEF) {
            // Block Force
            final int PICFNum = PICAddress & 0xF;
            if (PICFNum != LastFunction) {
                switch (PICFNum) {
                    case 8:
                        bfAddr = 0;
                        break;
                    case 9:
                        ++bfAddr;
                        bfAddr &= 0x1F;
                        break;
                    case 10:
                        bfRAM[bfAddr] = 0;
                        break;
                    case 11:
                        ++bfRAM[bfAddr];
                        bfRAM[bfAddr] &= 0xFF;
                        break;
                    case 12: // status line
                        irqPhase = PHASE_RAISE;
                        irqRaiseCount = 25000;
                        irqHoldCount = 24;
                        irqsToRaise = irqReleaseCountOdd = irqReleaseCountEven = 1;
                        irqCounter = 0;
                        break;
                    case 13:
                        final int bfHighScore = bfRAM[0x1A] + bfRAM[0x1B] * 10
                                + bfRAM[0x1C] * 100 + bfRAM[0x1D] * 1000 + bfRAM[0x1E] * 10000
                                + bfRAM[0x1F] * 100000;
                        final int bfPlayerScore = bfRAM[0x14] + bfRAM[0x15] * 10
                                + bfRAM[0x16] * 100 + bfRAM[0x17] * 1000 + bfRAM[0x18] * 10000
                                + bfRAM[0x19] * 100000;
                        if (bfPlayerScore > bfHighScore) {
                            System.arraycopy(bfRAM, 0x14, bfRAM, 0x1A, 6);
                        }
                        break;
                    case 14:
                        bfLatch = bfRAM[bfAddr];
                        break;
                    case 15:
                        --bfLatch;
                        bfLatch &= 0xFF;
                        if (bfLatch == 0) {
                            irqPhase = PHASE_RAISE;
                            irqHoldCount = 24;
                            irqRaiseCount = irqsToRaise = irqReleaseCountOdd
                                    = irqReleaseCountEven = 1;
                            irqCounter = 0;
                        }
                        break;
                }
            }
            LastFunction = PICFNum;
        } else if (PICCounter == 100 && PICAddress >= 0xE18 && PICAddress <= 0xE1F) {
            // 3D Block
            final int PICFNum = PICAddress & 0x7;
            if (PICFNum != LastFunction) {
                switch (PICFNum) {
                    case 0:     // E180 screen split during _3d
                        irqPhase = PHASE_RAISE;
                        irqRaiseCount = 12400;
                        irqHoldCount = 24;
                        irqReleaseCountOdd = irqReleaseCountEven = 1;
                        irqsToRaise = 1;
                        irqCounter = 0;
                        break;
                    case 4:     // E1C0 screen split during level display
                        irqPhase = PHASE_RAISE;
                        irqRaiseCount = 1818 + 12 * 341 / 3;
                        irqHoldCount = 24;
                        irqReleaseCountOdd = irqReleaseCountEven = 1818 - 24;
                        irqsToRaise = 10;
                        irqCounter = 0;
                        break;
                    case 1:     // E190 reset counter
                        _3dLatch = 0;
                        _3dLatchBits = 0;
                        irqPhase = PHASE_RAISE;
                        irqRaiseCount = 1;
                        irqHoldCount = 24;
                        irqReleaseCountOdd = irqReleaseCountEven = 1;
                        irqsToRaise = 1;
                        irqCounter = 0;
                        break;
                    case 3:     // E1B0 1 bit
                        _3dLatch = (_3dLatch << 1) | 1;
                        ++_3dLatchBits;
                        if (_3dLatchBits == 32) {
                            _3dProcess();
                        }
                        irqPhase = PHASE_RAISE;
                        irqRaiseCount = 1;
                        irqHoldCount = 24;
                        irqReleaseCountOdd = irqReleaseCountEven = 1;
                        irqsToRaise = 1;
                        irqCounter = 0;
                        break;
                    case 5:     // E1D0 0 bit
                        _3dLatch = _3dLatch << 1;
                        _3dLatchBits++;
                        if (_3dLatchBits == 32) {
                            _3dProcess();
                        }
                        irqPhase = PHASE_RAISE;
                        irqRaiseCount = 1;
                        irqHoldCount = 24;
                        irqReleaseCountOdd = irqReleaseCountEven = 1;
                        irqsToRaise = 1;
                        irqCounter = 0;
                        break;
                    case 6:     // E1E0 number of IRQs returns value
                        irqsToRaise = _3dLatch & 0xFF;
                        _3dLatch >>= 8;
                        if (irqsToRaise != 0) {
                            irqPhase = PHASE_RAISE;
                            irqRaiseCount = 1;
                            irqHoldCount = 24;
                            irqReleaseCountOdd = irqReleaseCountEven = 24;
                        }
                        irqCounter = 0;
                        break;
                    case 7:     // reset
                        irqPhase = PHASE_NONE;
                        _3dCounter = 83;
                        irqCounter = 0;
                        break;
                    case 2:
                        // E1A0 Title screen. The IRQ handler increases $02D1 on every 
                        // invocation.  $02D1 must reach the value $F7, so the various 
                        // counters must be tweaked to reach that result at the end.
                        if (_3dCounter > 0) {
                            maxCounter = 250 * 341 / 3 - 100;
                            irqPhase = PHASE_RAISE;
                            irqRaiseCount = 28 * 256 + 48;
                            irqHoldCount = 24;
                            irqReleaseCountEven = 341 * _3dCounter / 6 - 24;
                            irqReleaseCountOdd = 16 * 341 / 3 - 24;
                            irqsToRaise = 64;
                            --_3dCounter;
                            irqCounter = 0;
                        }
                        break;
                }
            }
            LastFunction = PICFNum;
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x8000) {
            CheckAddress(address);
        }
        return super.readMemory(address);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        CheckAddress(address);
    }

    @Override
    public void update() {
        if (maxCounter != 0 && --maxCounter == 0) {
            cpu.setMapperIrq(false);
            irqPhase = PHASE_NONE;
            if ((irqsToRaise & 1) != 0) {
                irqPhase = PHASE_RAISE;
                irqsToRaise = 1;
                irqCounter = irqRaiseCount - 5;
            }
        }
        switch (irqPhase) {
            case PHASE_RAISE:
                if (++irqCounter == irqRaiseCount) {
                    cpu.setMapperIrq(true);
                    irqPhase = PHASE_HOLD;
                    irqCounter = 0;
                }
                break;
            case PHASE_HOLD:
                if (++irqCounter == irqHoldCount) {
                    cpu.setMapperIrq(false);
                    irqPhase = PHASE_RELEASE;
                    irqCounter = 0;
                }
                break;
            case PHASE_RELEASE:
                if (++irqCounter == ((irqsToRaise & 1) != 0 ? irqReleaseCountOdd
                        : irqReleaseCountEven)) {
                    if (--irqsToRaise != 0) {
                        irqPhase = PHASE_HOLD;
                        cpu.setMapperIrq(true);
                    } else {
                        irqPhase = PHASE_NONE;
                    }
                    irqCounter = 0;
                }
                break;
        }
    }
}