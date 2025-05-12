package cn.kinlon.emu.cpu;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.PPU;
import cn.kinlon.emu.ServicedType;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.DeltaModulationChannel;
import cn.kinlon.emu.mappers.Mapper;

import java.io.Serializable;

import static cn.kinlon.emu.PPU.REG_OAM_DATA;
import static cn.kinlon.emu.utils.BitUtil.*;

public class CPU implements Serializable {

    private static final long serialVersionUID = 0;

    public static final int REG_OAM_DMA = 0x4014;
    public static final int REG_OUTPUT_PORT = 0x4016;
    public static final int REG_INPUT_PORT_1 = 0x4016;
    public static final int REG_INPUT_PORT_2 = 0x4017;
    public static final int VEC_STACK_START = 0x0100;

    public static final int VEC_NMI = 0xfffa;
    public static final int VEC_RESET = 0xfffc;
    public static final int VEC_IRQ = 0xfffe;
    public static final int VEC_BRK = VEC_IRQ;

    private static final int IRQ_MAPPER = 0;
    private static final int IRQ_APU = 1;
    private static final int IRQ_DMC = 2;

    private final Register reg = new Register();

    private long cycleCounter;
    private int opcode;
    private boolean running = true;

    private boolean resetRequested;
    private boolean nmiRequested;
    private int irqRequested;
    private boolean triggerNMI;
    private boolean triggerIRQ;
    private ServicedType serviced;
    private volatile int instructionsCounter;

    private Mapper mapper;
    private PPU ppu;
    private APU apu;
    private DeltaModulationChannel dmc;

    private int dmcCycle;
    private int dmcAddress;

    // -- public methods ---------------------------------------------------------

    public void executeInstruction() {

        serviced = ServicedType.None;

        if (resetRequested) {
            resetRequested = false;
            running = true;
            apu.reset();
            ppu.reset();
            RESET();
        }

        opcode = read(reg.pc());
        reg.pcInc1();
        switch (opcode) {

            // Stack instructions
            case 0x00: // BRK
                BRK();
                break;
            case 0x40: // RTI
                RTI();
                break;
            case 0x60: // RTS
                RTS();
                break;
            case 0x08: // PHP
            case 0x48: // PHA
                PUSH();
                break;
            case 0x28: // PLP
            case 0x68: // PLA
                PULL();
                break;
            case 0x20: // JSR $A5B6
                JSR();
                break;

            // Accumulator or implied addressing instructions
            case 0x0A: // ASL A
            case 0x1A: // *NOP
            case 0x18: // CLC
            case 0x2A: // ROL A
            case 0x38: // SEC
            case 0x3A: // *NOP
            case 0x4A: // LSR A
            case 0x58: // CLI
            case 0x5A: // *NOP
            case 0x6A: // ROR A
            case 0x78: // SEI
            case 0x7A: // *NOP
            case 0x88: // DEY
            case 0x8A: // TXA
            case 0x98: // TYA
            case 0x9A: // TXS
            case 0xA8: // TAY
            case 0xAA: // TAX
            case 0xB8: // CLV
            case 0xBA: // TSX
            case 0xC8: // INY
            case 0xCA: // DEX
            case 0xD8: // CLD
            case 0xDA: // *NOP
            case 0xE8: // INX
            case 0xEA: // NOP
            case 0xF8: // SED
            case 0xFA: // *NOP
                ACCUMULATOR_OR_IMPLIED();
                break;

            // Immediate addressing instructions
            case 0x09: // ORA #$A5
            case 0x0B: // *AAC #$A5
            case 0x2B: // *AAC #$A5
            case 0x29: // AND #$A5
            case 0x49: // EOR #$A5
            case 0x4B: // *ASR #$A5
            case 0x69: // ADC #$A5
            case 0x6B: // *ARR #$A5
            case 0x80: // *NOP #$89
            case 0x82: // *DOP #$89
            case 0x89: // *DOP #$89
            case 0x8B: // *XAA #$A5
            case 0xA0: // LDY #$A5
            case 0xA2: // LDX #$A5
            case 0xA9: // LDA #$A5
            case 0xAB: // *ATX #$A5
            case 0xC0: // CPY #$A5
            case 0xC2: // *DOP #$89
            case 0xC9: // CMP #$A5
            case 0xCB: // *AXS #$A5
            case 0xE0: // CPX #$A5
            case 0xE2: // *DOP #$89
            case 0xE9: // SBC #$A5
            case 0xEB: // *SBC #$40
                IMMEDIATE();
                break;

            // Absolute addressing instructions
            case 0x4C: // JMP $A5B6
                ABSOLUTE_JUMP();
                break;
            case 0x0C: // *NOP $A9A9
            case 0x0D: // ORA $A5B6
            case 0x2C: // BIT $A5B6
            case 0x2D: // AND $A5B6
            case 0x4D: // EOR $A5B6
            case 0x6D: // ADC $A5B6
            case 0xAC: // LDY $A5B6
            case 0xAD: // LDA $A5B6
            case 0xAE: // LDX $A5B6
            case 0xAF: // *LAX $0577
            case 0xCC: // CPY $A5B6
            case 0xCD: // CMP $A5B6
            case 0xEC: // CPX $A5B6
            case 0xED: // SBC $A5B6
                ABSOLUTE_READ();
                break;
            case 0x0E: // ASL $A5B6
            case 0x0F: // *SLO $0647
            case 0x2E: // ROL $A5B6
            case 0x2F: // *RLA $0647
            case 0x4F: // *SRE $0647
            case 0x4E: // LSR $A5B6
            case 0x6E: // ROR $A5B6
            case 0x6F: // *RRA $0647
            case 0xCE: // DEC $A5B6
            case 0xCF: // *DCP $0647
            case 0xEE: // INC $A5B6
            case 0xEF: // *ISB $0647
                ABSOLUTE_READ_MODIFY_WRITE();
                break;
            case 0x8C: // STY $A5B6
            case 0x8D: // STA $A5B6
            case 0x8E: // STX $A5B6
            case 0x8F: // *SAX $0549
                ABSOLUTE_WRITE();
                break;

            // Zero page addressing instructions
            case 0x04: // *NOP $A9
            case 0x05: // ORA $A5
            case 0x24: // BIT $A5
            case 0x25: // AND $A5
            case 0x44: // *NOP $A9
            case 0x45: // EOR $A5
            case 0x64: // *NOP $A9
            case 0x65: // ADC $A5
            case 0xA4: // LDY $A5
            case 0xA5: // LDA $A5
            case 0xA6: // LDX $A5
            case 0xA7: // *LAX $67
            case 0xC4: // CPY $A5
            case 0xC5: // CMP $A5
            case 0xE4: // CPX $A5
            case 0xE5: // SBC $A5
                ZERO_PAGE_READ();
                break;
            case 0x06: // ASL $A5
            case 0x07: // *SLO $47
            case 0x26: // ROL $A5
            case 0x27: // *RLA $47
            case 0x46: // LSR $A5
            case 0x47: // *SRE $47
            case 0x66: // ROR $A5
            case 0x67: // *RRA $47
            case 0xC6: // DEC $A5
            case 0xC7: // *DCP $47
            case 0xE6: // INC $A5
            case 0xE7: // *ISB $47
                ZERO_PAGE_READ_MODIFY_WRITE();
                break;
            case 0x84: // STY $A5
            case 0x85: // STA $A5
            case 0x86: // STX $A5
            case 0x87: // *SAX $49
                ZERO_PAGE_WRITE();
                break;

            // Zero page indexed addressing instructions
            case 0x14: // *NOP $A9,X
            case 0x15: // ORA $A5,X
            case 0x34: // *NOP $A9,X
            case 0x35: // AND $A5,X
            case 0x54: // *NOP $A9,X
            case 0x55: // EOR $A5,X
            case 0x74: // *NOP $A9,X
            case 0x75: // ADC $A5,X
            case 0xB4: // LDY $A5,X
            case 0xB5: // LDA $A5,X
            case 0xB6: // LDX $A5,Y
            case 0xB7: // *LAX $10,Y
            case 0xD4: // *NOP $A9,X
            case 0xD5: // CMP $A5,X
            case 0xF4: // *NOP $A9,X
            case 0xF5: // SBC $A5,X
                ZERO_PAGE_INDEXED_READ();
                break;
            case 0x16: // ASL $A5,X
            case 0x17: // *SLO $48,X
            case 0x36: // ROL $A5,X
            case 0x37: // *RLA $48,X
            case 0x56: // LSR $A5,X
            case 0x57: // *SRE $48,X
            case 0x76: // ROR $A5,X
            case 0x77: // *RRA $48,X
            case 0xD6: // DEC $A5,X
            case 0xD7: // *DCP $48,X
            case 0xF6: // INC $A5,X
            case 0xF7: // *ISB $48,X
                ZERO_PAGE_INDEXED_READ_MODIFY_WRITE();
                break;
            case 0x94: // STY $A5,X
            case 0x95: // STA $A5,X
            case 0x96: // STX $A5,Y
            case 0x97: // *SAX $4A,Y
                ZERO_PAGE_INDEXED_WRITE();
                break;

            // Absolute indexed addressing instructions
            case 0x19: // ORA $A5B6,Y
            case 0x1C: // *NOP $A9A9,X
            case 0x1D: // ORA $A5B6,X
            case 0x3C: // *NOP $A9A9,X
            case 0x3D: // AND $A5B6,X
            case 0x39: // AND $A5B6,Y
            case 0x5C: // *NOP $A9A9,X
            case 0x5D: // EOR $A5B6,X
            case 0x59: // EOR $A5B6,Y
            case 0x7C: // *NOP $A9A9,X
            case 0x7D: // ADC $A5B6,X
            case 0x79: // ADC $A5B6,Y      
            case 0xBB: // *LAR $A5B6,Y
            case 0xBC: // LDY $A5B6,X
            case 0xB9: // LDA $A5B6,Y
            case 0xBD: // LDA $A5B6,X
            case 0xBE: // LDX $A5B6,Y
            case 0xBF: // *LAX $0557,Y
            case 0xD9: // CMP $A5B6,Y
            case 0xDC: // *NOP $A9A9,X
            case 0xDD: // CMP $A5B6,X
            case 0xF9: // SBC $A5B6,Y
            case 0xFC: // *NOP $A9A9,X
            case 0xFD: // SBC $A5B6,X
                ABSOLUTE_INDEXED_READ();
                break;
            case 0x1B: // *SLO $0548,Y
            case 0x1E: // ASL $A5B6,X
            case 0x1F: // *SLO $0548,X
            case 0x3B: // *RLA $0548,Y  
            case 0x3E: // ROL $A5B6,X
            case 0x3F: // *RLA $0548,X
            case 0x5E: // LSR $A5B6,X
            case 0x5B: // *SRE $0548,Y
            case 0x5F: // *SRE $0548,X
            case 0x7B: // *RRA $0548,Y
            case 0x7E: // ROR $A5B6,X
            case 0x7F: // *RRA $0548,X      
            case 0xDB: // *DCP $0548,Y
            case 0xDE: // DEC $A5B6,X
            case 0xDF: // *DCP $0548,X
            case 0xFE: // INC $A5B6,X
            case 0xFB: // *ISB $0548,Y  
            case 0xFF: // *ISB $0548,X
                ABSOLUTE_INDEXED_READ_MODIFY_WRITE();
                break;
            case 0x99: // STA $A5B6,Y
            case 0x9B: // *XAS $A5B6,Y  
            case 0x9C: // *SHY $A5B6,X
            case 0x9D: // STA $A5B6,X
            case 0x9E: // *SHX $A5B6,Y
            case 0x9F: // *SHA $A5B6,Y
                ABSOLUTE_INDEXED_WRITE();
                break;

            // Relative addressing instructions
            case 0x10: // BPL $A5
            case 0x30: // BMI $A5
            case 0x50: // BVC $A5
            case 0x70: // BVS $A5
            case 0x90: // BCC $A5
            case 0xB0: // BCS $A5
            case 0xD0: // BNE $A5
            case 0xF0: // BEQ $A5
                RELATIVE_BRANCH();
                break;

            // Indexed indirect addressing instructions
            case 0x01: // ORA ($A5,X)
            case 0x21: // AND ($A5,X)
            case 0x41: // EOR ($A5,X)
            case 0x61: // ADC ($A5,X)
            case 0xA1: // LDA ($A5,X)
            case 0xA3: // *LAX ($40,X)
            case 0xC1: // CMP ($A5,X)
            case 0xE1: // SBC ($A5,X)
                INDEXED_INDIRECT_READ();
                break;
            case 0x03: // *SLO ($45,X)
            case 0x23: // *RLA ($45,X)
            case 0x43: // *SRE ($45,X)
            case 0x63: // *RRA ($45,X)
            case 0xC3: // *DCP ($45,X)
            case 0xE3: // *ISB ($45,X)
                INDEXED_INDIRECT_READ_MODIFY_WRITE();
                break;
            case 0x81: // STA ($A5,X)
            case 0x83: // *SAX ($49,X)
                INDEXED_INDIRECT_WRITE();
                break;

            // Indirect indexed addressing instructions
            case 0x11: // ORA ($A5),Y
            case 0x31: // AND ($A5),Y
            case 0x51: // EOR ($A5),Y
            case 0x71: // ADC ($A5),Y
            case 0xB1: // LDA ($A5),Y
            case 0xB3: // *LAX ($43),Y  
            case 0xD1: // CMP ($A5),Y
            case 0xF1: // SBC ($A5),Y
                INDIRECT_INDEXED_READ();
                break;
            case 0x13: // *SLO ($45),Y
            case 0x33: // *RLA ($45),Y
            case 0x53: // *SRE ($45),Y
            case 0x73: // *RRA ($45),Y
            case 0xD3: // *DCP ($45),Y
            case 0xF3: // *ISB ($45),Y
                INDIRECT_INDEXED_READ_MODIFY_WRITE();
                break;
            case 0x91: // STA ($A5),Y
            case 0x93: // *SHA ($A5),Y
                INDIRECT_INDEXED_WRITE();
                break;

            // Absolute indirect addressing instructions
            case 0x6C: // JMP ($A5B6)
                ABSOLUTE_INDIRECT_JUMP();
                break;

            case 0x02: // *KIL
            case 0x12: // *KIL
            case 0x22: // *KIL
            case 0x32: // *KIL
            case 0x42: // *KIL
            case 0x52: // *KIL
            case 0x62: // *KIL
            case 0x72: // *KIL
            case 0x92: // *KIL
            case 0xB2: // *KIL
            case 0xD2: // *KIL
            case 0xF2: // *KIL
                KIL();
                break;
        }

        if (running) {
            if (triggerNMI) {
                NMI();
                nmiRequested = false;
            } else if (triggerIRQ) {
                IRQ();
            }
        }

        instructionsCounter++;
    }

    public void init() {
        reset();
    }

    public void reset() {
        resetRequested = true;
    }

    public void setNMI(final boolean value) {
        nmiRequested = value;
    }

    public void setMapperIrq(final boolean value) {
        irqRequested = setBit(irqRequested, IRQ_MAPPER, value);
    }

    public boolean getMapperIrq() {
        return getBitBool(irqRequested, IRQ_MAPPER);
    }

    public void setApuIrq(final boolean value) {
        irqRequested = setBit(irqRequested, IRQ_APU, value);
    }

    public boolean getApuIrq() {
        return getBitBool(irqRequested, IRQ_APU);
    }

    public void setDmcIrq(final boolean value) {
        irqRequested = setBit(irqRequested, IRQ_DMC, value);
    }

    public boolean getDmcIrq() {
        return getBitBool(irqRequested, IRQ_DMC);
    }

    public void dmcRead(final int address) {
        dmcAddress = address;
        dmcCycle = 4;
    }

    public int getInstructionsCounter() {
        return instructionsCounter;
    }

    public void oamTransfer(int value) {

        value <<= 8;

        if ((cycleCounter & 1) == 0) {
            read(reg.pc());
        }
        read(reg.pc());
        for (int i = 0; i < 256; i++) {
            write(REG_OAM_DATA, read(value | i));
        }
    }

    public void setMachine(final Machine machine) {
        this.mapper = machine.getMapper();
        this.ppu = machine.getPPU();
        this.apu = machine.getAPU();
        this.dmc = apu.getDMC();
    }

    public void setMapper(final Mapper mapper) {
        this.mapper = mapper;
    }

    public void setPPU(final PPU ppu) {
        this.ppu = ppu;
    }

    public void setAPU(final APU apu) {
        this.apu = apu;
        this.dmc = apu.getDMC();
    }

    public long getCycleCounter() {
        return cycleCounter;
    }

    public ServicedType getServiced() {
        return serviced;
    }

    // -- private methods --------------------------------------------------------

    private void handleCpuCycle() {
        triggerNMI = nmiRequested;
        triggerIRQ = irqRequested != 0 && !reg.i();
        mapper.update();
        cycleCounter++;
        apu.update((cycleCounter & 1) == 1);
        ppu.update();
    }


    private int readStack() {
        return read(VEC_STACK_START | reg.sp());
    }

    private void writeStack(int value) {
        write(VEC_STACK_START | reg.sp(), value);
    }

    private void write(final int address, int value) {
        if (dmcCycle > 0) {
            dmcCycle--;
        }
        handleCpuCycle();

        mapper.writeCpuMemory(address, value);
    }

    private int read(final int address) {

        if (dmcCycle > 0) {
            int cycle = dmcCycle - 1;
            dmcCycle = 0;
            if (address == REG_INPUT_PORT_1 || address == REG_INPUT_PORT_2) {
                if (cycle-- > 0) {
                    read(address);
                }
                while (cycle-- > 0) {
                    handleCpuCycle();
                }
            } else {
                while (cycle-- > 0) {
                    read(address);
                }
            }
            dmc.fillSampleBuffer(read(dmcAddress));
        }

        handleCpuCycle();

        return mapper.readCpuMemory(address);
    }

    // -- Instructions -----------------------------------------------------------

    private void AAC(final int value) {
        reg.a(reg.a() & value);

        reg.c(getBitBool(reg.a(), 7));
        reg.n(reg.c());
        reg.z(reg.a() == 0);
    }

    private void ADC(int value) {
        int t = reg.a() + value + (reg.c() ? 1 : 0);

        reg.v(getBitBool((reg.a() ^ t) & (value ^ t), 7));
        reg.a(t);

        reg.c(getBitBool(t, 8));
        reg.n(getBitBool(t, 7));
        reg.z(reg.a() == 0);
    }

    private void AND(int value) {
        reg.a(reg.a() & value);

        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private void ARR(int value) {
        reg.a(ROR(reg.a() & value));

        switch ((getBit(reg.a(), 6) << 1) | getBit(reg.a(), 5)) {
            case 0b00:
                reg.v(false);
                reg.c(false);
                break;
            case 0b01:
                reg.v(true);
                reg.c(false);
                break;
            case 0b10:
                reg.v(true);
                reg.c(true);
                break;
            case 0b11:
                reg.v(false);
                reg.c(true);
                break;
        }
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private int ASL(int value) {
        reg.c(getBitBool(value, 7));
        value = (value << 1) & 0b1111_1110;

        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    private void ASR(int value) {
        reg.a(LSR(reg.a() & value));
    }

    private void ATX(int value) {
        reg.a(value);
        TAX();
    }

    private void AXS(int value) {
        int t = (reg.a() & reg.x()) + 256 - value;
        reg.x(t);

        reg.c(getBitBool(t, 8));
        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    private void BIT(int value) {
        reg.v(getBitBool(value, 6));
        reg.n(getBitBool(value, 7));
        reg.z((reg.a() & value) == 0);
    }

    private void CLC() {
        reg.c(false);
    }

    private void CLD() {
        reg.d(false);
    }

    private void CLI() {
        reg.i(false);
    }

    private void CLV() {
        reg.v(false);
    }

    private void CMP(int value) {
        reg.c(reg.a() >= value);
        reg.n(getBitBool(reg.a() - value, 7));
        reg.z(reg.a() == value);
    }

    private void CPX(int value) {
        reg.c(reg.x() >= value);
        reg.n(getBitBool(reg.x() - value, 7));
        reg.z(reg.x() == value);
    }

    private void CPY(int value) {
        reg.c(reg.y() >= value);
        reg.n(getBitBool(reg.y() - value, 7));
        reg.z(reg.y() == value);
    }

    private int DCP(int value) {
        CMP(value = DEC(value));
        return value;
    }

    private int DEC(int value) {
        value = (value - 1) & 0b1111_1111;
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    private void DEX() {
        reg.x(reg.x() - 1);

        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    private void DEY() {
        reg.y(reg.y() - 1);

        reg.n(getBitBool(reg.y(), 7));
        reg.z(reg.y() == 0);
    }

    private void EOR(int value) {
        reg.a(reg.a() ^ value);

        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private int INC(int value) {
        value = (value + 1) & 0b1111_1111;

        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    private void INX() {
        reg.x(reg.x() + 1);

        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    private void INY() {
        reg.y(reg.y() + 1);

        reg.n(getBitBool(reg.y(), 7));
        reg.z(reg.y() == 0);
    }

    private int ISB(int value) {
        SBC(value = INC(value));
        return value;
    }

    private void LAR(final int value) {
        reg.a(value & reg.sp());
        reg.sp(reg.a());
        TAX();
    }

    private void LAX(int value) {
        LDA(value);
        TAX();
    }

    private void LDA(int value) {
        reg.a(value);

        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private void LDX(int value) {
        reg.x(value);

        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    private void LDY(int value) {
        reg.y(value);

        reg.n(getBitBool(reg.y(), 7));
        reg.z(reg.y() == 0);
    }

    private int LSR(int value) {
        reg.c(getBitBool(value, 0));
        value = (value >> 1) & 0b0111_1111;

        reg.n(false);
        reg.z(value == 0);
        return value;
    }

    private void ORA(int value) {
        reg.a(reg.a() | value);

        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private int RLA(int value) {
        AND(value = ROL(value));
        return value;
    }

    private int ROL(int value) {
        boolean t = getBitBool(value, 7);
        value = (value << 1) & 0b1111_1110;
        value |= reg.c() ? 1 : 0;

        reg.c(t);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    private int ROR(int value) {
        boolean t = getBitBool(value, 0);
        value = (value >> 1) & 0b0111_1111;
        value |= (reg.c() ? 1 : 0) << 7;

        reg.c(t);
        reg.z(value == 0);
        reg.n(getBitBool(value, 7));
        return value;
    }

    private int RRA(int value) {
        ADC(value = ROR(value));
        return value;
    }

    private void SBC(int value) {
        ADC(value ^ 0xFF);
    }

    private void SEC() {
        reg.c(true);
    }

    private void SED() {
        reg.d(true);
    }

    private void SEI() {
        reg.i(true);
    }

    private int SLO(int value) {
        ORA(value = ASL(value));
        return value;
    }

    private int SRE(int value) {
        EOR(value = LSR(value));
        return value;
    }

    private void TAX() {
        reg.x(reg.a());

        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    private void TAY() {
        reg.y(reg.a());

        reg.n(getBitBool(reg.y(), 7));
        reg.z(reg.y() == 0);
    }

    private void TSX() {
        reg.x(reg.sp());

        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    private void TXA() {
        reg.a(reg.x());

        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private void TXS() {
        reg.sp(reg.x());
    }

    private void TYA() {
        reg.a(reg.y());

        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    private void XAA(final int value) {
        reg.a(reg.a() & (reg.x() & value));

        reg.c(getBitBool(reg.a(), 7));
        reg.n(reg.c());
        reg.z(reg.a() == 0);
    }

    private int XAS(final int value) {
        reg.sp(reg.x() & reg.a());
        return reg.sp() & value;
    }

    // -- Stack instructions -----------------------------------------------------

    private void RESET() {

        // 1
        read(reg.pc());
        // 2
        read(reg.pc());
        // 3
        readStack();
        reg.spDec1();
        // 4
        readStack();
        reg.spDec1();
        // 5    
        readStack();
        reg.spDec1();
        // 6
        reg.pc((reg.pc() & 0xFF00) | read(0xFFFC));
        reg.i(true);
        // 7
        reg.pc((read(0xFFFD) << 8) | (reg.pc() & 0x00FF));
        serviced = ServicedType.RST;
    }

    private void NMI() {
        // 1
        read(reg.pc());
        // 2
        read(reg.pc());
        // 3
        writeStack(reg.pc() >> 8);
        reg.spDec1();
        // 4
        writeStack(reg.pc() & 0x00FF);
        reg.spDec1();
        // 5
        writeStack(reg.p() | 0b0010_0000);
        reg.spDec1();
        // 6
        reg.i(true);
        reg.pc((reg.pc() & 0xFF00) | read(0xFFFA));
        // 7
        reg.pc((read(0xFFFB) << 8) | (reg.pc() & 0x00FF));
        serviced = ServicedType.NMI;
    }

    private void IRQ() {
        // 1
        read(reg.pc());
        // 2
        read(reg.pc());
        // 3
        writeStack(reg.pc() >> 8);
        reg.spDec1();
        // 4
        writeStack(reg.pc() & 0x00FF);
        reg.spDec1();
        // 5
        writeStack(reg.p() | 0b0010_0000);
        reg.spDec1();
        int address;
        if (nmiRequested) {
            nmiRequested = false;
            address = 0xFFFA;
            serviced = ServicedType.NMI;
        } else {
            address = 0xFFFE;
            serviced = ServicedType.IRQ;
        }
        // 6
        reg.i(true);
        reg.pc((reg.pc() & 0xFF00) | read(address++));
        // 7
        reg.pc((read(address) << 8) | (reg.pc() & 0x00FF));
    }

    private void BRK() {
        // 2
        read(reg.pc());
        reg.pcInc1();
        // 3
        writeStack(reg.pc() >> 8);
        reg.spDec1();
        // 4
        writeStack(reg.pc() & 0x00FF);
        reg.spDec1();
        // 5
        writeStack(reg.p() | 0b0001_0000);
        reg.spDec1();
        int address;
        if (nmiRequested) {
            nmiRequested = false;
            address = 0xFFFA;
            serviced = ServicedType.NMI;
        } else {
            address = 0xFFFE;
            serviced = ServicedType.BRK;
        }
        // 6
        reg.i(true);
        reg.pc((reg.pc() & 0xFF00) | read(address++));
        // 7
        reg.pc((read(address) << 8) | (reg.pc() & 0x00FF));
    }

    private void RTI() {
        // 2
        read(reg.pc());
        // 3
        readStack();
        reg.spInc1();
        // 4
        reg.p(readStack() | 0b0010_0000);
        reg.spInc1();
        // 5
        reg.pc((reg.pc() & 0xFF00) | readStack());
        reg.spInc1();
        // 6
        reg.pc((reg.pc() & 0x00FF) | (readStack() << 8));
    }

    private void RTS() {
        // 2        
        read(reg.pc());
        // 3
        readStack();
        reg.spInc1();
        // 4
        reg.pc((reg.pc() & 0xFF00) | readStack());
        reg.spInc1();
        // 5
        reg.pc((reg.pc() & 0x00FF) | (readStack() << 8));
        // 6
        read(reg.pc());
        reg.pcInc1();
    }

    private void PUSH() {
        // 2
        read(reg.pc());
        // 3        
        switch (opcode) {
            case 0x08: // PHP
                writeStack(reg.p() | 0b0001_0000);
                break;
            case 0x48: // PHA
                writeStack(reg.a());
                break;
        }
        reg.spDec1();
    }

    private void PULL() {
        // 2
        read(reg.pc());
        // 3
        readStack();
        reg.spInc1();
        // 4
        switch (opcode) {
            case 0x28: // PLP
                reg.p(readStack() | 0b0010_0000);
                break;
            case 0x68: // PLA
                reg.a(readStack());
                reg.n(getBitBool(reg.a(), 7));
                reg.z(reg.a() == 0);
                break;
        }
    }

    private void JSR() {
        // 2
        final int address = read(reg.pc());
        reg.pcInc1();
        // 3
        readStack();
        // 4
        writeStack(reg.pc() >> 8);
        reg.spDec1();
        // 5
        writeStack(reg.pc() & 0x00FF);
        reg.spDec1();
        // 6
        reg.pc((read(reg.pc()) << 8) | address);
    }

    private void KIL() {
        reg.pcDec1();
        if (running) {
            running = false;
            App.cpuKilled(mapper.readCpuMemory(reg.pc()), reg.pc());
        }
    }

    // -- Accumulator or implied addressing instructions -------------------------
    private void ACCUMULATOR_OR_IMPLIED() {
        // 2
        read(reg.pc());
        switch (opcode) {
            case 0x0A: // ASL A
                reg.a(ASL(reg.a()));
                break;
            case 0x18: // CLC
                CLC();
                break;
            case 0x2A: // ROL A
                reg.a(ROL(reg.a()));
                break;
            case 0x38: // SEC
                SEC();
                break;
            case 0x4A: // LSR A
                reg.a(LSR(reg.a()));
                break;
            case 0x58: // CLI
                CLI();
                break;
            case 0x6A: // ROR A
                reg.a(ROR(reg.a()));
                break;
            case 0x78: // SEI
                SEI();
                break;
            case 0x88: // DEY
                DEY();
                break;
            case 0x8A: // TXA
                TXA();
                break;
            case 0x98: // TYA
                TYA();
                break;
            case 0x9A: // TXS
                TXS();
                break;
            case 0xA8: // TAY
                TAY();
                break;
            case 0xAA: // TAX
                TAX();
                break;
            case 0xB8: // CLV
                CLV();
                break;
            case 0xBA: // TSX
                TSX();
                break;
            case 0xC8: // INY
                INY();
                break;
            case 0xCA: // DEX
                DEX();
                break;
            case 0xD8: // CLD
                CLD();
                break;
            case 0xE8: // INX
                INX();
                break;
            case 0xF8: // SED
                SED();
                break;
            case 0x1A: // *NOP
            case 0x3A: // *NOP
            case 0x5A: // *NOP
            case 0x7A: // *NOP
            case 0xDA: // *NOP
            case 0xEA: // NOP
            case 0xFA: // *NOP
                break;
        }
    }

    // -- Immediate addressing instructions --------------------------------------

    private void IMMEDIATE() {
        // 2
        final int value = read(reg.pc());
        reg.pcInc1();
        switch (opcode) {
            case 0x09: // ORA #$A5
                ORA(value);
                break;
            case 0x0B: // *AAC #$A5
            case 0x2B: // *AAC #$A5
                AAC(value);
                break;
            case 0x29: // AND #$A5
                AND(value);
                break;
            case 0x49: // EOR #$A5
                EOR(value);
                break;
            case 0x4B: // *ASR #$A5
                ASR(value);
                break;
            case 0x69: // ADC #$A5
                ADC(value);
                break;
            case 0x6B: // *ARR #$A5
                ARR(value);
                break;
            case 0x8B: // *XAA #$A5
                XAA(value);
                break;
            case 0xA0: // LDY #$A5
                LDY(value);
                break;
            case 0xA2: // LDX #$A5
                LDX(value);
                break;
            case 0xA9: // LDA #$A5
                LDA(value);
                break;
            case 0xAB: // *ATX #$A5
                ATX(value);
                break;
            case 0xC0: // CPY #$A5
                CPY(value);
                break;
            case 0xC9: // CMP #$A5
                CMP(value);
                break;
            case 0xCB: // *AXS #$A5
                AXS(value);
                break;
            case 0xE0: // CPX #$A5
                CPX(value);
                break;
            case 0xE9: // SBC #$A5
            case 0xEB: // *SBC #$40
                SBC(value);
                break;
            case 0x80: // *NOP #$89
            case 0x82: // *DOP #$89
            case 0x89: // *DOP #$89
            case 0xC2: // *DOP #$89
            case 0xE2: // *DOP #$89
                break;
        }
    }

    // -- Absolute addressing instructions ---------------------------------------

    private void ABSOLUTE_JUMP() {
        // 2
        final int address = read(reg.pc());
        reg.pcInc1();
        // 3
        reg.pc((read(reg.pc()) << 8) | address);
    }

    private void ABSOLUTE_READ() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        address |= read(reg.pc()) << 8;
        reg.pcInc1();
        // 4       
        final int value = read(address);
        switch (opcode) {
            case 0x0C: // *NOP $A9A9
                break;
            case 0x0D: // ORA $A5B6
                ORA(value);
                break;
            case 0x2C: // BIT $A5B6
                BIT(value);
                break;
            case 0x2D: // AND $A5B6
                AND(value);
                break;
            case 0x4D: // EOR $A5B6
                EOR(value);
                break;
            case 0x6D: // ADC $A5B6
                ADC(value);
                break;
            case 0xAC: // LDY $A5B6
                LDY(value);
                break;
            case 0xAD: // LDA $A5B6
                LDA(value);
                break;
            case 0xAE: // LDX $A5B6
                LDX(value);
                break;
            case 0xAF: // *LAX $0577
                LAX(value);
                break;
            case 0xCC: // CPY $A5B6
                CPY(value);
                break;
            case 0xCD: // CMP $A5B6
                CMP(value);
                break;
            case 0xEC: // CPX $A5B6
                CPX(value);
                break;
            case 0xED: // SBC $A5B6
                SBC(value);
                break;
        }
    }

    private void ABSOLUTE_READ_MODIFY_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        address |= read(reg.pc()) << 8;
        reg.pcInc1();
        // 4
        int value = read(address);
        // 5
        write(address, value);
        switch (opcode) {
            case 0x0E: // ASL $A5B6
                value = ASL(value);
                break;
            case 0x0F: // *SLO $0647
                value = SLO(value);
                break;
            case 0x2E: // ROL $A5B6
                value = ROL(value);
                break;
            case 0x2F: // *RLA $0647
                value = RLA(value);
                break;
            case 0x4F: // *SRE $0647
                value = SRE(value);
                break;
            case 0x4E: // LSR $A5B6
                value = LSR(value);
                break;
            case 0x6E: // ROR $A5B6
                value = ROR(value);
                break;
            case 0x6F: // *RRA $0647
                value = RRA(value);
                break;
            case 0xCE: // DEC $A5B6
                value = DEC(value);
                break;
            case 0xCF: // *DCP $0647
                value = DCP(value);
                break;
            case 0xEE: // INC $A5B6
                value = INC(value);
                break;
            case 0xEF: // *ISB $0647
                value = ISB(value);
                break;
        }
        // 6        
        write(address, value);
    }

    private void ABSOLUTE_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        address |= read(reg.pc()) << 8;
        reg.pcInc1();
        // 4       
        switch (opcode) {
            case 0x8C: // STY $A5B6
                write(address, reg.y());
                break;
            case 0x8D: // STA $A5B6
                write(address, reg.a());
                break;
            case 0x8E: // STX $A5B6
                write(address, reg.x());
                break;
            case 0x8F: // *SAX $0549
                write(address, reg.a() & reg.x());
                break;
        }
    }

    // -- Zero page addressing instructions --------------------------------------

    private void ZERO_PAGE_READ() {
        // 2
        final int address = read(reg.pc());
        reg.pcInc1();
        // 3        
        final int value = read(address);
        switch (opcode) {
            case 0x04: // *NOP $A9
                break;
            case 0x05: // ORA $A5
                ORA(value);
                break;
            case 0x24: // BIT $A5
                BIT(value);
                break;
            case 0x25: // AND $A5
                AND(value);
                break;
            case 0x44: // *NOP $A9
                break;
            case 0x45: // EOR $A5
                EOR(value);
                break;
            case 0x64: // *NOP $A9
                break;
            case 0x65: // ADC $A5
                ADC(value);
                break;
            case 0xA4: // LDY $A5
                LDY(value);
                break;
            case 0xA5: // LDA $A5
                LDA(value);
                break;
            case 0xA6: // LDX $A5
                LDX(value);
                break;
            case 0xA7: // *LAX $67
                LAX(value);
                break;
            case 0xC4: // CPY $A5
                CPY(value);
                break;
            case 0xC5: // CMP $A5
                CMP(value);
                break;
            case 0xE4: // CPX $A5
                CPX(value);
                break;
            case 0xE5: // SBC $A5
                SBC(value);
                break;
        }
    }

    private void ZERO_PAGE_READ_MODIFY_WRITE() {
        // 2
        final int address = read(reg.pc());
        reg.pcInc1();
        // 3
        int value = read(address);
        // 4
        write(address, value);
        switch (opcode) {
            case 0x06: // ASL $A5
                value = ASL(value);
                break;
            case 0x07: // *SLO $47
                value = SLO(value);
                break;
            case 0x26: // ROL $A5
                value = ROL(value);
                break;
            case 0x27: // *RLA $47
                value = RLA(value);
                break;
            case 0x46: // LSR $A5
                value = LSR(value);
                break;
            case 0x47: // *SRE $47
                value = SRE(value);
                break;
            case 0x66: // ROR $A5
                value = ROR(value);
                break;
            case 0x67: // *RRA $47
                value = RRA(value);
                break;
            case 0xC6: // DEC $A5
                value = DEC(value);
                break;
            case 0xC7: // *DCP $47
                value = DCP(value);
                break;
            case 0xE6: // INC $A5
                value = INC(value);
                break;
            case 0xE7: // *ISB $47
                value = ISB(value);
                break;
        }
        // 5       
        write(address, value);
    }

    private void ZERO_PAGE_WRITE() {
        // 2
        final int address = read(reg.pc());
        reg.pcInc1();
        // 3        
        switch (opcode) {
            case 0x84: // STY $A5
                write(address, reg.y());
                break;
            case 0x85: // STA $A5
                write(address, reg.a());
                break;
            case 0x86: // STX $A5
                write(address, reg.x());
                break;
            case 0x87: // *SAX $49
                write(address, reg.a() & reg.x());
                break;
        }
    }

    // -- Zero page indexed addressing instructions ------------------------------

    private void ZERO_PAGE_INDEXED_READ() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        read(address);
        switch (opcode) {
            case 0xB6: // LDX $A5,Y
            case 0xB7: // *LAX $10,Y
                address += reg.y();
                break;
            default:
                address += reg.x();
                break;
        }
        address &= 0x00FF;
        // 4        
        final int value = read(address);
        switch (opcode) {
            case 0x15: // ORA $A5,X
                ORA(value);
                break;
            case 0x35: // AND $A5,X
                AND(value);
                break;
            case 0x55: // EOR $A5,X
                EOR(value);
                break;
            case 0x75: // ADC $A5,X
                ADC(value);
                break;
            case 0xB4: // LDY $A5,X
                LDY(value);
                break;
            case 0xB5: // LDA $A5,X
                LDA(value);
                break;
            case 0xB6: // LDX $A5,Y
                LDX(value);
                break;
            case 0xB7: // *LAX $10,Y
                LAX(value);
                break;
            case 0xD5: // CMP $A5,X
                CMP(value);
                break;
            case 0xF5: // SBC $A5,X
                SBC(value);
                break;
            case 0x14: // *NOP $A9,X
            case 0x34: // *NOP $A9,X
            case 0x54: // *NOP $A9,X
            case 0x74: // *NOP $A9,X
            case 0xD4: // *NOP $A9,X
            case 0xF4: // *NOP $A9,X
                break;
        }
    }

    private void ZERO_PAGE_INDEXED_READ_MODIFY_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        read(address);
        address += reg.x();
        address &= 0x00FF;
        // 4
        int value = read(address);
        // 5
        write(address, value);
        switch (opcode) {
            case 0x16: // ASL $A5,X
                value = ASL(value);
                break;
            case 0x17: // *SLO $48,X
                value = SLO(value);
                break;
            case 0x36: // ROL $A5,X
                value = ROL(value);
                break;
            case 0x37: // *RLA $48,X
                value = RLA(value);
                break;
            case 0x56: // LSR $A5,X
                value = LSR(value);
                break;
            case 0x57: // *SRE $48,X
                value = SRE(value);
                break;
            case 0x76: // ROR $A5,X
                value = ROR(value);
                break;
            case 0x77: // *RRA $48,X
                value = RRA(value);
                break;
            case 0xD6: // DEC $A5,X
                value = DEC(value);
                break;
            case 0xD7: // *DCP $48,X
                value = DCP(value);
                break;
            case 0xF6: // INC $A5,X
                value = INC(value);
                break;
            case 0xF7: // *ISB $48,X
                value = ISB(value);
                break;
        }
        // 6
        write(address, value);
    }

    private void ZERO_PAGE_INDEXED_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        read(address);
        switch (opcode) {
            case 0x94: // STY $A5,X
            case 0x95: // STA $A5,X
                address += reg.x();
                break;
            case 0x96: // STX $A5,Y
            case 0x97: // *SAX $4A,Y
                address += reg.y();
                break;
        }
        address &= 0x00FF;
        // 4        
        switch (opcode) {
            case 0x94: // STY $A5,X
                write(address, reg.y());
                break;
            case 0x95: // STA $A5,X
                write(address, reg.a());
                break;
            case 0x96: // STX $A5,Y
                write(address, reg.x());
                break;
            case 0x97: // *SAX $4A,Y
                write(address, reg.a() & reg.x());
                break;
        }
    }

    // -- Absolute indexed addressing instructions -------------------------------

    private void ABSOLUTE_INDEXED_READ() {
        // 2
        int address1 = read(reg.pc());
        reg.pcInc1();
        // 3    
        address1 |= read(reg.pc()) << 8;
        final int offset;
        switch (opcode) {
            case 0x19: // ORA $A5B6,Y
            case 0x39: // AND $A5B6,Y
            case 0x59: // EOR $A5B6,Y
            case 0x79: // ADC $A5B6,Y      
            case 0xB9: // LDA $A5B6,Y
            case 0xBB: // *LAR $A5B6,Y
            case 0xBE: // LDX $A5B6,Y
            case 0xBF: // *LAX $0557,Y
            case 0xD9: // CMP $A5B6,Y
            case 0xF9: // SBC $A5B6,Y
                offset = reg.y();
                break;
            default:
                offset = reg.x();
                break;
        }
        final int address2 = (address1 + offset) & 0xFFFF;
        address1 = (address1 & 0xFF00) | (address2 & 0x00FF);
        reg.pcInc1();
        // 4
        int value = read(address1);
        if (address1 != address2) {
            // 5
            value = read(address2);
        }
        switch (opcode) {
            case 0x19: // ORA $A5B6,Y
            case 0x1D: // ORA $A5B6,X
                ORA(value);
                break;
            case 0x3D: // AND $A5B6,X
            case 0x39: // AND $A5B6,Y
                AND(value);
                break;
            case 0x5D: // EOR $A5B6,X
            case 0x59: // EOR $A5B6,Y
                EOR(value);
                break;
            case 0x7D: // ADC $A5B6,X
            case 0x79: // ADC $A5B6,Y
                ADC(value);
                break;
            case 0xBB: // *LAR $A5B6,Y
                LAR(value);
                break;
            case 0xBC: // LDY $A5B6,X
                LDY(value);
                break;
            case 0xB9: // LDA $A5B6,Y
            case 0xBD: // LDA $A5B6,X
                LDA(value);
                break;
            case 0xBE: // LDX $A5B6,Y
                LDX(value);
                break;
            case 0xBF: // *LAX $0557,Y
                LAX(value);
                break;
            case 0xD9: // CMP $A5B6,Y
            case 0xDD: // CMP $A5B6,X
                CMP(value);
                break;
            case 0xF9: // SBC $A5B6,Y
            case 0xFD: // SBC $A5B6,X
                SBC(value);
                break;
            case 0x1C: // *NOP $A9A9,X
            case 0x3C: // *NOP $A9A9,X
            case 0x5C: // *NOP $A9A9,X
            case 0x7C: // *NOP $A9A9,X
            case 0xDC: // *NOP $A9A9,X
            case 0xFC: // *NOP $A9A9,X
                break;
        }
    }

    private void ABSOLUTE_INDEXED_READ_MODIFY_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        final int offset;
        switch (opcode) {
            case 0x1B: // *SLO $0548,Y
            case 0x3B: // *RLA $0548,Y
            case 0x5B: // *SRE $0548,Y
            case 0x7B: // *RRA $0548,Y
            case 0xDB: // *DCP $0548,Y
            case 0xFB: // *ISB $0548,Y 
                offset = reg.y();
                break;
            default:
                offset = reg.x();
                break;
        }
        value |= read(reg.pc()) << 8;
        int address = (value + offset) & 0xFFFF;
        reg.pcInc1();
        // 4
        read((value & 0xFF00) | (address & 0x00FF));
        // 5
        value = read(address);
        // 6
        write(address, value);
        switch (opcode) {
            case 0x1B: // *SLO $0548,Y
            case 0x1F: // *SLO $0548,X
                value = SLO(value);
                break;
            case 0x1E: // ASL $A5B6,X
                value = ASL(value);
                break;
            case 0x3B: // *RLA $0548,Y
            case 0x3F: // *RLA $0548,X
                value = RLA(value);
                break;
            case 0x3E: // ROL $A5B6,X
                value = ROL(value);
                break;
            case 0x5B: // *SRE $0548,Y
            case 0x5F: // *SRE $0548,X
                value = SRE(value);
                break;
            case 0x5E: // LSR $A5B6,X
                value = LSR(value);
                break;
            case 0x7B: // *RRA $0548,Y
            case 0x7F: // *RRA $0548,X
                value = RRA(value);
                break;
            case 0x7E: // ROR $A5B6,X
                value = ROR(value);
                break;
            case 0xDB: // *DCP $0548,Y
            case 0xDF: // *DCP $0548,X
                value = DCP(value);
                break;
            case 0xDE: // DEC $A5B6,X
                value = DEC(value);
                break;
            case 0xFB: // *ISB $0548,Y
            case 0xFF: // *ISB $0548,X
                value = ISB(value);
                break;
            case 0xFE: // INC $A5B6,X
                value = INC(value);
                break;
        }
        // 7        
        write(address, value);
    }

    private void ABSOLUTE_INDEXED_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        final int offset;
        final int high = read(reg.pc());
        value |= high << 8;
        switch (opcode) {
            case 0x9C: // *SHY $A5B6,X
            case 0x9D: // STA $A5B6,X
                offset = reg.x();
                break;
            default:
                offset = reg.y();
                break;
        }
        int address = (value + offset) & 0xFFFF;
        value = (value & 0xFF00) | (address & 0x00FF);
        reg.pcInc1();
        // 4
        read(value);
        // 5        
        switch (opcode) {
            case 0x99: // STA $A5B6,Y
            case 0x9D: // STA $A5B6,X
                write(address, reg.a());
                break;
            case 0x9B: // *XAS $A5B6,Y
                write(address, XAS(high + 1));
                break;
            case 0x9C: // *SHY $A5B6,X
                if ((value >> 8) != (address >> 8)) {
                    value &= reg.y() << 8;
                }
                write(value, reg.y() & ((value >> 8) + 1));
                break;
            case 0x9E: // *SHX $A5B6,Y
                if ((value >> 8) != (address >> 8)) {
                    value &= reg.x() << 8;
                }
                write(value, reg.x() & ((value >> 8) + 1));
                break;
            case 0x9F: // *SHA $A5B6,Y
                if ((value >> 8) != (address >> 8)) {
                    value &= (reg.x() & reg.a()) << 8;
                }
                write(address, reg.x() & reg.a() & ((address >> 8) + 1));
                break;
        }
    }

    // -- Relative addressing instructions ---------------------------------------

    private void RELATIVE_BRANCH() {
        // 2
        final int addressOffset = read(reg.pc());
        reg.pcInc1();
        boolean branchTaken = false;
        switch (opcode) {
            case 0x10: // BPL $A5
                branchTaken = !reg.n();
                break;
            case 0x30: // BMI $A5
                branchTaken = reg.n();
                break;
            case 0x50: // BVC $A5
                branchTaken = !reg.v();
                break;
            case 0x70: // BVS $A5
                branchTaken = reg.v() ;
                break;
            case 0x90: // BCC $A5
                branchTaken = !reg.c();
                break;
            case 0xB0: // BCS $A5
                branchTaken = reg.c();
                break;
            case 0xD0: // BNE $A5
                branchTaken = !reg.z();
                break;
            case 0xF0: // BEQ $A5
                branchTaken = reg.z();
                break;
        }
        if (branchTaken) {
            // 3
            boolean clearNMI = false;
            boolean clearIRQ = false;
            if (nmiRequested && !triggerNMI) {
                clearNMI = true;
            }
            if (irqRequested != 0 && !triggerIRQ) {
                clearIRQ = true;
            }
            read(reg.pc());
            if (clearNMI) {
                triggerNMI = false;
            }
            if (clearIRQ) {
                triggerIRQ = false;
            }
            final int jumpAddress = (reg.pc() + (byte) addressOffset) & 0xFFFF;
            reg.pc((reg.pc() & 0xFF00) | (jumpAddress & 0x00FF));
            if (reg.pc() != jumpAddress) {
                // 4
                read(reg.pc());
                reg.pc(jumpAddress);
            }
        }
    }

    // -- Indexed indirect addressing instructions -------------------------------

    private void INDEXED_INDIRECT_READ() {
        // 2
        int address1 = read(reg.pc());
        reg.pcInc1();
        // 3
        read(address1);
        address1 = (address1 + reg.x()) & 0x00FF;
        // 4
        int address2 = read(address1);
        address1 = (address1 + 1) & 0x00FF;
        // 5
        address2 |= read(address1) << 8;
        // 6        
        final int value = read(address2);
        switch (opcode) {
            case 0x01: // ORA ($A5,X)
                ORA(value);
                break;
            case 0x21: // AND ($A5,X)
                AND(value);
                break;
            case 0x41: // EOR ($A5,X)
                EOR(value);
                break;
            case 0x61: // ADC ($A5,X)
                ADC(value);
                break;
            case 0xA1: // LDA ($A5,X)
                LDA(value);
                break;
            case 0xA3: // *LAX ($40,X)
                LAX(value);
                break;
            case 0xC1: // CMP ($A5,X)
                CMP(value);
                break;
            case 0xE1: // SBC ($A5,X)
                SBC(value);
                break;
        }
    }

    private void INDEXED_INDIRECT_READ_MODIFY_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        read(value);
        value = (value + reg.x()) & 0x00FF;
        // 4
        int address = read(value);
        value = (value + 1) & 0x00FF;
        // 5
        address |= read(value) << 8;
        // 6
        value = read(address);
        // 7
        write(address, value);
        switch (opcode) {
            case 0x03: // *SLO ($45,X)
                value = SLO(value);
                break;
            case 0x23: // *RLA ($45,X)
                value = RLA(value);
                break;
            case 0x43: // *SRE ($45,X)
                value = SRE(value);
                break;
            case 0x63: // *RRA ($45,X)
                value = RRA(value);
                break;
            case 0xC3: // *DCP ($45,X)
                value = DCP(value);
                break;
            case 0xE3: // *ISB ($45,X)
                value = ISB(value);
                break;
        }
        // 8       
        write(address, value);
    }

    private void INDEXED_INDIRECT_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        read(value);
        value = (value + reg.x()) & 0x00FF;
        // 4
        int address = read(value);
        value = (value + 1) & 0x00FF;
        // 5
        address |= read(value) << 8;
        // 6        
        switch (opcode) {
            case 0x81: // STA ($A5,X)
                write(address, reg.a());
                break;
            case 0x83: // *SAX ($49,X)
                write(address, reg.a() & reg.x());
                break;
        }
    }

    // -- Indirect indexed addressing instructions -------------------------------

    private void INDIRECT_INDEXED_READ() {
        // 2
        int address1 = read(reg.pc());
        reg.pcInc1();
        // 3
        int address2 = read(address1);
        address1++;
        address1 &= 0x00FF;
        // 4
        address2 |= read(address1) << 8;
        address1 = (address2 + reg.y()) & 0xFFFF;
        address2 = (address2 & 0xFF00) | (address1 & 0x00FF);
        // 5
        int value = read(address2);
        if (address2 != address1) {
            // 6
            value = read(address1);
        }
        switch (opcode) {
            case 0x11: // ORA ($A5),Y
                ORA(value);
                break;
            case 0x31: // AND ($A5),Y
                AND(value);
                break;
            case 0x51: // EOR ($A5),Y
                EOR(value);
                break;
            case 0x71: // ADC ($A5),Y
                ADC(value);
                break;
            case 0xB1: // LDA ($A5),Y
                LDA(value);
                break;
            case 0xB3: // *LAX ($43),Y
                LAX(value);
                break;
            case 0xD1: // CMP ($A5),Y
                CMP(value);
                break;
            case 0xF1: // SBC ($A5),Y
                SBC(value);
                break;
        }
    }

    private void INDIRECT_INDEXED_READ_MODIFY_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        int value = read(address);
        address++;
        address &= 0x00FF;
        // 4
        value |= read(address) << 8;
        address = (value + reg.y()) & 0xFFFF;
        value = (value & 0xFF00) | (address & 0x00FF);
        // 5
        read(value);
        // 6
        value = read(address);
        // 7
        write(address, value);
        switch (opcode) {
            case 0x13: // *SLO ($45),Y
                value = SLO(value);
                break;
            case 0x33: // *RLA ($45),Y
                value = RLA(value);
                break;
            case 0x53: // *SRE ($45),Y
                value = SRE(value);
                break;
            case 0x73: // *RRA ($45),Y
                value = RRA(value);
                break;
            case 0xD3: // *DCP ($45),Y
                value = DCP(value);
                break;
            case 0xF3: // *ISB ($45),Y
                value = ISB(value);
                break;
        }
        // 8        
        write(address, value);
    }

    private void INDIRECT_INDEXED_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        int value = read(address);
        address++;
        address &= 0x00FF;
        // 4
        value |= read(address) << 8;
        address = (value + reg.y()) & 0xFFFF;
        value = (value & 0xFF00) | (address & 0x00FF);
        // 5
        read(value);
        // 6        
        switch (opcode) {
            case 0x91: // STA ($A5),Y
                write(address, reg.a());
                break;
            case 0x93: // *SHA ($A5),Y
                write(address, reg.x() & reg.a() & ((address >> 8) + 1));
                break;
        }
    }

    // -- Absolute indirect addressing instructions ------------------------------

    private void ABSOLUTE_INDIRECT_JUMP() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        address |= read(reg.pc()) << 8;
        reg.pcInc1();
        // 4
        reg.pc((reg.pc() & 0xFF00) | read(address));
        address = (address & 0xFF00) | ((address + 1) & 0x00FF);
        // 5       
        reg.pc((read(address) << 8) | (reg.pc() & 0x00FF));
    }
    public Register getRegister() {
        return reg;
    }
}