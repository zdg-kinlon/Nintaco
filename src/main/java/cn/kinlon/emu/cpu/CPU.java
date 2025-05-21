package cn.kinlon.emu.cpu;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.ppu.PPU;
import cn.kinlon.emu.ServicedType;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.DeltaModulationChannel;
import cn.kinlon.emu.mappers.Mapper;

import java.io.Serializable;

import static cn.kinlon.emu.ppu.PPU.REG_OAM_DATA;
import static cn.kinlon.emu.utils.BitUtil.*;
import static cn.kinlon.emu.utils.ByteUtil.*;
import static cn.kinlon.emu.utils.ByteUtil.toU8;

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

    private static final int IRQ_MAPPER = 0;
    private static final int IRQ_APU = 1;
    private static final int IRQ_DMC = 2;

    private final Register reg = new Register();
    private final ALU alu = new ALU(this);

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
            // BRK
            case 0x00:
                BRK();
                break;
// RTI
            case 0x40:
                RTI();
                break;
// RTS
            case 0x60:
                RTS();
                break;
            // PHP
            // PHA
            case 0x08:
            case 0x48:
                PUSH();
                break;
            // PLP
            // PLA
            case 0x28:
            case 0x68:
                PULL();
                break;
// JSR $A5B6
            case 0x20:
                JSR();
                break;


            // Accumulator or implied addressing instructions
            // ASL A
            // *NOP
            // CLC
            // ROL A
            // SEC
            // *NOP
            // LSR A
            // CLI
            // *NOP
            // ROR A
            // SEI
            // *NOP
            // DEY
            // TXA
            // TYA
            // TXS
            // TAY
            // TAX
            // CLV
            // TSX
            // INY
            // DEX
            // CLD
            // *NOP
            // INX
            // NOP
            // SED
            // *NOP
            case 0x0A:
            case 0x1A:
            case 0x18:
            case 0x2A:
            case 0x38:
            case 0x3A:
            case 0x4A:
            case 0x58:
            case 0x5A:
            case 0x6A:
            case 0x78:
            case 0x7A:
            case 0x88:
            case 0x8A:
            case 0x98:
            case 0x9A:
            case 0xA8:
            case 0xAA:
            case 0xB8:
            case 0xBA:
            case 0xC8:
            case 0xCA:
            case 0xD8:
            case 0xDA:
            case 0xE8:
            case 0xEA:
            case 0xF8:
            case 0xFA:
                ACCUMULATOR_OR_IMPLIED();
                break;


            // Immediate addressing instructions
            // ORA #$A5
            // *AAC #$A5
            // *AAC #$A5
            // AND #$A5
            // EOR #$A5
            // *ASR #$A5
            // ADC #$A5
            // *ARR #$A5
            // *NOP #$89
            // *DOP #$89
            // *DOP #$89
            // *XAA #$A5
            // LDY #$A5
            // LDX #$A5
            // LDA #$A5
            // *ATX #$A5
            // CPY #$A5
            // *DOP #$89
            // CMP #$A5
            // *AXS #$A5
            // CPX #$A5
            // *DOP #$89
            // SBC #$A5
            // *SBC #$40
            case 0x09:
            case 0x0B:
            case 0x2B:
            case 0x29:
            case 0x49:
            case 0x4B:
            case 0x69:
            case 0x6B:
            case 0x80:
            case 0x82:
            case 0x89:
            case 0x8B:
            case 0xA0:
            case 0xA2:
            case 0xA9:
            case 0xAB:
            case 0xC0:
            case 0xC2:
            case 0xC9:
            case 0xCB:
            case 0xE0:
            case 0xE2:
            case 0xE9:
            case 0xEB:
                IMMEDIATE();
                break;


            // Absolute addressing instructions
            // JMP $A5B6
            case 0x4C:
                ABSOLUTE_JUMP();
                break;
            // *NOP $A9A9
            // ORA $A5B6
            // BIT $A5B6
            // AND $A5B6
            // EOR $A5B6
            // ADC $A5B6
            // LDY $A5B6
            // LDA $A5B6
            // LDX $A5B6
            // *LAX $0577
            // CPY $A5B6
            // CMP $A5B6
            // CPX $A5B6
            // SBC $A5B6
            case 0x0C:
            case 0x0D:
            case 0x2C:
            case 0x2D:
            case 0x4D:
            case 0x6D:
            case 0xAC:
            case 0xAD:
            case 0xAE:
            case 0xAF:
            case 0xCC:
            case 0xCD:
            case 0xEC:
            case 0xED:
                ABSOLUTE_READ();
                break;
            // ASL $A5B6
            // *SLO $0647
            // ROL $A5B6
            // *RLA $0647
            // *SRE $0647
            // LSR $A5B6
            // ROR $A5B6
            // *RRA $0647
            // DEC $A5B6
            // *DCP $0647
            // INC $A5B6
            // *ISB $0647
            case 0x0E:
            case 0x0F:
            case 0x2E:
            case 0x2F:
            case 0x4F:
            case 0x4E:
            case 0x6E:
            case 0x6F:
            case 0xCE:
            case 0xCF:
            case 0xEE:
            case 0xEF:
                ABSOLUTE_READ_MODIFY_WRITE();
                break;
            // STY $A5B6
            // STA $A5B6
            // STX $A5B6
            // *SAX $0549
            case 0x8C:
            case 0x8D:
            case 0x8E:
            case 0x8F:
                ABSOLUTE_WRITE();
                break;


            // Zero page addressing instructions
            // *NOP $A9
            // ORA $A5
            // BIT $A5
            // AND $A5
            // *NOP $A9
            // EOR $A5
            // *NOP $A9
            // ADC $A5
            // LDY $A5
            // LDA $A5
            // LDX $A5
            // *LAX $67
            // CPY $A5
            // CMP $A5
            // CPX $A5
            // SBC $A5
            case 0x04:
            case 0x05:
            case 0x24:
            case 0x25:
            case 0x44:
            case 0x45:
            case 0x64:
            case 0x65:
            case 0xA4:
            case 0xA5:
            case 0xA6:
            case 0xA7:
            case 0xC4:
            case 0xC5:
            case 0xE4:
            case 0xE5:
                ZERO_PAGE_READ();
                break;
            // ASL $A5
            // *SLO $47
            // ROL $A5
            // *RLA $47
            // LSR $A5
            // *SRE $47
            // ROR $A5
            // *RRA $47
            // DEC $A5
            // *DCP $47
            // INC $A5
            // *ISB $47
            case 0x06:
            case 0x07:
            case 0x26:
            case 0x27:
            case 0x46:
            case 0x47:
            case 0x66:
            case 0x67:
            case 0xC6:
            case 0xC7:
            case 0xE6:
            case 0xE7:
                ZERO_PAGE_READ_MODIFY_WRITE();
                break;
            // STY $A5
            // STA $A5
            // STX $A5
            // *SAX $49
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
                ZERO_PAGE_WRITE();
                break;


            // Zero page indexed addressing instructions
            // *NOP $A9,X
            // ORA $A5,X
            // *NOP $A9,X
            // AND $A5,X
            // *NOP $A9,X
            // EOR $A5,X
            // *NOP $A9,X
            // ADC $A5,X
            // LDY $A5,X
            // LDA $A5,X
            // LDX $A5,Y
            // *LAX $10,Y
            // *NOP $A9,X
            // CMP $A5,X
            // *NOP $A9,X
            // SBC $A5,X
            case 0x14:
            case 0x15:
            case 0x34:
            case 0x35:
            case 0x54:
            case 0x55:
            case 0x74:
            case 0x75:
            case 0xB4:
            case 0xB5:
            case 0xB6:
            case 0xB7:
            case 0xD4:
            case 0xD5:
            case 0xF4:
            case 0xF5:
                ZERO_PAGE_INDEXED_READ();
                break;
            // ASL $A5,X
            // *SLO $48,X
            // ROL $A5,X
            // *RLA $48,X
            // LSR $A5,X
            // *SRE $48,X
            // ROR $A5,X
            // *RRA $48,X
            // DEC $A5,X
            // *DCP $48,X
            // INC $A5,X
            // *ISB $48,X
            case 0x16:
            case 0x17:
            case 0x36:
            case 0x37:
            case 0x56:
            case 0x57:
            case 0x76:
            case 0x77:
            case 0xD6:
            case 0xD7:
            case 0xF6:
            case 0xF7:
                ZERO_PAGE_INDEXED_READ_MODIFY_WRITE();
                break;
            // STY $A5,X
            // STA $A5,X
            // STX $A5,Y
            // *SAX $4A,Y
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
                ZERO_PAGE_INDEXED_WRITE();
                break;


            // Absolute indexed addressing instructions
            // ORA $A5B6,Y
            // *NOP $A9A9,X
            // ORA $A5B6,X
            // *NOP $A9A9,X
            // AND $A5B6,X
            // AND $A5B6,Y
            // *NOP $A9A9,X
            // EOR $A5B6,X
            // EOR $A5B6,Y
            // *NOP $A9A9,X
            // ADC $A5B6,X
            // ADC $A5B6,Y      
            // *LAR $A5B6,Y
            // LDY $A5B6,X
            // LDA $A5B6,Y
            // LDA $A5B6,X
            // LDX $A5B6,Y
            // *LAX $0557,Y
            // CMP $A5B6,Y
            // *NOP $A9A9,X
            // CMP $A5B6,X
            // SBC $A5B6,Y
            // *NOP $A9A9,X
            // SBC $A5B6,X
            case 0x19:
            case 0x1C:
            case 0x1D:
            case 0x3C:
            case 0x3D:
            case 0x39:
            case 0x5C:
            case 0x5D:
            case 0x59:
            case 0x7C:
            case 0x7D:
            case 0x79:
            case 0xBB:
            case 0xBC:
            case 0xB9:
            case 0xBD:
            case 0xBE:
            case 0xBF:
            case 0xD9:
            case 0xDC:
            case 0xDD:
            case 0xF9:
            case 0xFC:
            case 0xFD:
                ABSOLUTE_INDEXED_READ();
                break;
            // *SLO $0548,Y
            // ASL $A5B6,X
            // *SLO $0548,X
            // *RLA $0548,Y  
            // ROL $A5B6,X
            // *RLA $0548,X
            // LSR $A5B6,X
            // *SRE $0548,Y
            // *SRE $0548,X
            // *RRA $0548,Y
            // ROR $A5B6,X
            // *RRA $0548,X      
            // *DCP $0548,Y
            // DEC $A5B6,X
            // *DCP $0548,X
            // INC $A5B6,X
            // *ISB $0548,Y  
            // *ISB $0548,X
            case 0x1B:
            case 0x1E:
            case 0x1F:
            case 0x3B:
            case 0x3E:
            case 0x3F:
            case 0x5E:
            case 0x5B:
            case 0x5F:
            case 0x7B:
            case 0x7E:
            case 0x7F:
            case 0xDB:
            case 0xDE:
            case 0xDF:
            case 0xFE:
            case 0xFB:
            case 0xFF:
                ABSOLUTE_INDEXED_READ_MODIFY_WRITE();
                break;
            // STA $A5B6,Y
            // *XAS $A5B6,Y  
            // *SHY $A5B6,X
            // STA $A5B6,X
            // *SHX $A5B6,Y
            // *SHA $A5B6,Y
            case 0x99:
            case 0x9B:
            case 0x9C:
            case 0x9D:
            case 0x9E:
            case 0x9F:
                ABSOLUTE_INDEXED_WRITE();
                break;


            // Relative addressing instructions
            // BPL $A5
            // BMI $A5
            // BVC $A5
            // BVS $A5
            // BCC $A5
            // BCS $A5
            // BNE $A5
            // BEQ $A5
            case 0x10:
            case 0x30:
            case 0x50:
            case 0x70:
            case 0x90:
            case 0xB0:
            case 0xD0:
            case 0xF0:
                RELATIVE_BRANCH();
                break;


            // Indexed indirect addressing instructions
            // ORA ($A5,X)
            // AND ($A5,X)
            // EOR ($A5,X)
            // ADC ($A5,X)
            // LDA ($A5,X)
            // *LAX ($40,X)
            // CMP ($A5,X)
            // SBC ($A5,X)
            case 0x01:
            case 0x21:
            case 0x41:
            case 0x61:
            case 0xA1:
            case 0xA3:
            case 0xC1:
            case 0xE1:
                INDEXED_INDIRECT_READ();
                break;
            // *SLO ($45,X)
            // *RLA ($45,X)
            // *SRE ($45,X)
            // *RRA ($45,X)
            // *DCP ($45,X)
            // *ISB ($45,X)
            case 0x03:
            case 0x23:
            case 0x43:
            case 0x63:
            case 0xC3:
            case 0xE3:
                INDEXED_INDIRECT_READ_MODIFY_WRITE();
                break;
            // STA ($A5,X)
            // *SAX ($49,X)
            case 0x81:
            case 0x83:
                INDEXED_INDIRECT_WRITE();
                break;


            // Indirect indexed addressing instructions
            // ORA ($A5),Y
            // AND ($A5),Y
            // EOR ($A5),Y
            // ADC ($A5),Y
            // LDA ($A5),Y
            // *LAX ($43),Y  
            // CMP ($A5),Y
            // SBC ($A5),Y
            case 0x11:
            case 0x31:
            case 0x51:
            case 0x71:
            case 0xB1:
            case 0xB3:
            case 0xD1:
            case 0xF1:
                INDIRECT_INDEXED_READ();
                break;
            // *SLO ($45),Y
            // *RLA ($45),Y
            // *SRE ($45),Y
            // *RRA ($45),Y
            // *DCP ($45),Y
            // *ISB ($45),Y
            case 0x13:
            case 0x33:
            case 0x53:
            case 0x73:
            case 0xD3:
            case 0xF3:
                INDIRECT_INDEXED_READ_MODIFY_WRITE();
                break;
            // STA ($A5),Y
            // *SHA ($A5),Y
            case 0x91:
            case 0x93:
                INDIRECT_INDEXED_WRITE();
                break;


            // Absolute indirect addressing instructions
            // JMP ($A5B6)
            case 0x6C:
                ABSOLUTE_INDIRECT_JUMP();
                break;
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            // *KIL
            case 0x02:
            case 0x12:
            case 0x22:
            case 0x32:
            case 0x42:
            case 0x52:
            case 0x62:
            case 0x72:
            case 0x92:
            case 0xB2:
            case 0xD2:
            case 0xF2:
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

    public int pull() {
        reg.spInc1();
        return read(VEC_STACK_START | reg.sp());
    }

    public void push(int value) {
        write(VEC_STACK_START | reg.sp(), value);
        reg.spDec1();
    }

    public int readStack() {
        return read(VEC_STACK_START | reg.sp());
    }

    public void writeStack(int value) {
        write(VEC_STACK_START | reg.sp(), value);
    }

    public void write(final int address, int value) {
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
        reg.pc((reg.pc() & 0xFF00) | read(VEC_RESET));
        reg.i(true);
        // 7
        reg.pc((read(VEC_RESET + 1) << 8) | (reg.pc() & 0x00FF));
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
        reg.r(true);
        writeStack(reg.p());
        reg.spDec1();
        // 6
        reg.i(true);
        reg.pc((reg.pc() & 0xFF00) | read(VEC_NMI));
        // 7
        reg.pc((read(VEC_NMI + 1) << 8) | (reg.pc() & 0x00FF));
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
        reg.r(true);
        writeStack(reg.p());
        reg.spDec1();
        int address;
        if (nmiRequested) {
            nmiRequested = false;
            address = VEC_NMI;
            serviced = ServicedType.NMI;
        } else {
            address = VEC_IRQ;
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
        reg.r(true);
        reg.b(true);
        writeStack(reg.p());
        reg.b(false);
        reg.spDec1();
        int address;
        if (nmiRequested) {
            nmiRequested = false;
            address = VEC_NMI;
            serviced = ServicedType.NMI;
        } else {
            address = VEC_IRQ;
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
        boolean r = reg.r();
        boolean b = reg.b();
        reg.p(readStack());
        reg.r(r);
        reg.b(b);
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
                alu.php();
                break;
            case 0x48: // PHA
                alu.pha();
                break;
        }
    }

    private void PULL() {
        // 2
        read(reg.pc());
        // 3
        readStack();
        // 4
        switch (opcode) {
            case 0x28: // PLP
                alu.plp();
                break;
            case 0x68: // PLA
                alu.pla();
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
            case 0x0A -> // ASL A
                    reg.a(alu.asl(reg.a()));
            case 0x18 -> // CLC
                    alu.clc();
            case 0x2A -> // ROL A
                    reg.a(alu.rol(reg.a()));
            case 0x38 -> // SEC
                    alu.sec();
            case 0x4A -> // LSR A
                    reg.a(alu.lsr(reg.a()));
            case 0x58 -> // CLI
                    alu.cli();
            case 0x6A -> // ROR A
                    reg.a(alu.ror(reg.a()));
            case 0x78 -> // SEI
                    alu.sei();
            case 0x88 -> // DEY
                    alu.dey();
            case 0x8A -> // TXA
                    alu.txa();
            case 0x98 -> // TYA
                    alu.tya();
            case 0x9A -> // TXS
                    alu.txs();
            case 0xA8 -> // TAY
                    alu.tay();
            case 0xAA -> // TAX
                    alu.tax();
            case 0xB8 -> // CLV
                    alu.clv();
            case 0xBA -> // TSX
                    alu.tsx();
            case 0xC8 -> // INY
                    alu.iny();
            case 0xCA -> // DEX
                    alu.dex();
            case 0xD8 -> // CLD
                    alu.cld();
            case 0xE8 -> // INX
                    alu.inx();
            case 0xF8 -> // SED
                    alu.sed();
            // *NOP
            // *NOP
            // *NOP
            // *NOP
            // *NOP
            // NOP
            case 0x1A, 0x3A, 0x5A, 0x7A, 0xDA, 0xEA, 0xFA -> {
            }
        }
    }

    // -- Immediate addressing instructions --------------------------------------

    private void IMMEDIATE() {
        // 2
        final int value = read(reg.pc());
        reg.pcInc1();
        switch (opcode) {
            case 0x09 -> // ORA #$A5
                    alu.ora(value);
            // *AAC #$A5
            case 0x0B, 0x2B -> // *AAC #$A5
                    alu.anc(value);
            case 0x29 -> // AND #$A5
                    alu.and(value);
            case 0x49 -> // EOR #$A5
                    alu.eor(value);
            case 0x4B -> // *ASR #$A5
                    alu.alr(value);
            case 0x69 -> // ADC #$A5
                    alu.adc(value);
            case 0x6B -> // *ARR #$A5
                    alu.arr(value);
            case 0x8B -> // *XAA #$A5
                    alu.xaa(value);
            case 0xA0 -> // LDY #$A5
                    alu.ldy(value);
            case 0xA2 -> // LDX #$A5
                    alu.ldx(value);
            case 0xA9 -> // LDA #$A5
                    alu.lda(value);
            case 0xAB -> // *ATX #$A5
                    alu.lax(value);
            case 0xC0 -> // CPY #$A5
                    alu.cpy(value);
            case 0xC9 -> // CMP #$A5
                    alu.cmp(value);
            case 0xCB -> // *AXS #$A5
                    alu.axs(value);
            case 0xE0 -> // CPX #$A5
                    alu.cpx(value);
            // SBC #$A5
            case 0xE9, 0xEB -> // *SBC #$40
                    alu.sbc(value);
            // *NOP #$89
            // *DOP #$89
            // *DOP #$89
            // *DOP #$89
            case 0x80, 0x82, 0x89, 0xC2, 0xE2 -> {
            }
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
            case 0x0C -> {
            }
            case 0x0D -> // ORA $A5B6
                    alu.ora(value);
            case 0x2C -> // BIT $A5B6
                    alu.bit(value);
            case 0x2D -> // AND $A5B6
                    alu.and(value);
            case 0x4D -> // EOR $A5B6
                    alu.eor(value);
            case 0x6D -> // ADC $A5B6
                    alu.adc(value);
            case 0xAC -> // LDY $A5B6
                    alu.ldy(value);
            case 0xAD -> // LDA $A5B6
                    alu.lda(value);
            case 0xAE -> // LDX $A5B6
                    alu.ldx(value);
            case 0xAF -> // *LAX $0577
                    alu.lax(value);
            case 0xCC -> // CPY $A5B6
                    alu.cpy(value);
            case 0xCD -> // CMP $A5B6
                    alu.cmp(value);
            case 0xEC -> // CPX $A5B6
                    alu.cpx(value);
            case 0xED -> // SBC $A5B6
                    alu.sbc(value);
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
            case 0x0E -> // ASL $A5B6
                    value = alu.asl(value);
            case 0x0F -> // *SLO $0647
                    value = alu.slo(value);
            case 0x2E -> // ROL $A5B6
                    value = alu.rol(value);
            case 0x2F -> // *RLA $0647
                    value = alu.rla(value);
            case 0x4F -> // *SRE $0647
                    value = alu.sre(value);
            case 0x4E -> // LSR $A5B6
                    value = alu.lsr(value);
            case 0x6E -> // ROR $A5B6
                    value = alu.ror(value);
            case 0x6F -> // *RRA $0647
                    value = alu.rra(value);
            case 0xCE -> // DEC $A5B6
                    value = alu.dec(value);
            case 0xCF -> // *DCP $0647
                    value = alu.dcp(value);
            case 0xEE -> // INC $A5B6
                    value = alu.inc(value);
            case 0xEF -> // *ISB $0647
                    value = alu.isc(value);
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
            case 0x8C -> // STY $A5B6
                    alu.sty(address);
            case 0x8D -> // STA $A5B6
                    alu.sta(address);
            case 0x8E -> // STX $A5B6
                    alu.stx(address);
            case 0x8F -> // *SAX $0549
                    alu.sax(address);
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
            case 0x04, 0x44, 0x64 -> {
            }
            case 0x05 -> // ORA $A5
                    alu.ora(value);
            case 0x24 -> // BIT $A5
                    alu.bit(value);
            case 0x25 -> // AND $A5
                    alu.and(value);
            case 0x45 -> // EOR $A5
                    alu.eor(value);
            case 0x65 -> // ADC $A5
                    alu.adc(value);
            case 0xA4 -> // LDY $A5
                    alu.ldy(value);
            case 0xA5 -> // LDA $A5
                    alu.lda(value);
            case 0xA6 -> // LDX $A5
                    alu.ldx(value);
            case 0xA7 -> // *LAX $67
                    alu.lax(value);
            case 0xC4 -> // CPY $A5
                    alu.cpy(value);
            case 0xC5 -> // CMP $A5
                    alu.cmp(value);
            case 0xE4 -> // CPX $A5
                    alu.cpx(value);
            case 0xE5 -> // SBC $A5
                    alu.sbc(value);
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
            case 0x06 -> // ASL $A5
                    value = alu.asl(value);
            case 0x07 -> // *SLO $47
                    value = alu.slo(value);
            case 0x26 -> // ROL $A5
                    value = alu.rol(value);
            case 0x27 -> // *RLA $47
                    value = alu.rla(value);
            case 0x46 -> // LSR $A5
                    value = alu.lsr(value);
            case 0x47 -> // *SRE $47
                    value = alu.sre(value);
            case 0x66 -> // ROR $A5
                    value = alu.ror(value);
            case 0x67 -> // *RRA $47
                    value = alu.rra(value);
            case 0xC6 -> // DEC $A5
                    value = alu.dec(value);
            case 0xC7 -> // *DCP $47
                    value = alu.dcp(value);
            case 0xE6 -> // INC $A5
                    value = alu.inc(value);
            case 0xE7 -> // *ISB $47
                    value = alu.isc(value);
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
            case 0x84 -> // STY $A5
                    alu.sty(address);
            case 0x85 -> // STA $A5
                    alu.sta(address);
            case 0x86 -> // STX $A5
                    alu.stx(address);
            case 0x87 -> // *SAX $49
                    alu.sax(address);
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
            case 0x15 -> // ORA $A5,X
                    alu.ora(value);
            case 0x35 -> // AND $A5,X
                    alu.and(value);
            case 0x55 -> // EOR $A5,X
                    alu.eor(value);
            case 0x75 -> // ADC $A5,X
                    alu.adc(value);
            case 0xB4 -> // LDY $A5,X
                    alu.ldy(value);
            case 0xB5 -> // LDA $A5,X
                    alu.lda(value);
            case 0xB6 -> // LDX $A5,Y
                    alu.ldx(value);
            case 0xB7 -> // *LAX $10,Y
                    alu.lax(value);
            case 0xD5 -> // CMP $A5,X
                    alu.cmp(value);
            case 0xF5 -> // SBC $A5,X
                    alu.sbc(value);
            // *NOP $A9,X
            // *NOP $A9,X
            // *NOP $A9,X
            // *NOP $A9,X
            // *NOP $A9,X
            case 0x14, 0x34, 0x54, 0x74, 0xD4, 0xF4 -> {
            }
        }
    }

    private void ZERO_PAGE_INDEXED_READ_MODIFY_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        read(address);
        address = toU8(address + reg.x());
        // 4
        int value = read(address);
        // 5
        write(address, value);
        switch (opcode) {
            case 0x16 -> // ASL $A5,X
                    value = alu.asl(value);
            case 0x17 -> // *SLO $48,X
                    value = alu.slo(value);
            case 0x36 -> // ROL $A5,X
                    value = alu.rol(value);
            case 0x37 -> // *RLA $48,X
                    value = alu.rla(value);
            case 0x56 -> // LSR $A5,X
                    value = alu.lsr(value);
            case 0x57 -> // *SRE $48,X
                    value = alu.sre(value);
            case 0x76 -> // ROR $A5,X
                    value = alu.ror(value);
            case 0x77 -> // *RRA $48,X
                    value = alu.rra(value);
            case 0xD6 -> // DEC $A5,X
                    value = alu.dec(value);
            case 0xD7 -> // *DCP $48,X
                    value = alu.dcp(value);
            case 0xF6 -> // INC $A5,X
                    value = alu.inc(value);
            case 0xF7 -> // *ISB $48,X
                    value = alu.isc(value);
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
        // 4        
        switch (opcode) {
            case 0x94 -> // STY $A5,X
                    alu.sty(toU8(address + reg.x()));
            case 0x95 -> // STA $A5,X
                    alu.sta(toU8(address + reg.x()));
            case 0x96 -> // STX $A5,Y
                    alu.stx(toU8(address + reg.y()));
            case 0x97 -> // *SAX $4A,Y
                    alu.sax(toU8(address + reg.y()));
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
        switch (opcode) { // ORA $A5B6,Y
            // AND $A5B6,Y
            // EOR $A5B6,Y
            // ADC $A5B6,Y      
            // LDA $A5B6,Y
            // *LAR $A5B6,Y
            // LDX $A5B6,Y
            // *LAX $0557,Y
            // CMP $A5B6,Y
            case 0x19, 0x39, 0x59, 0x79, 0xB9, 0xBB, 0xBE, 0xBF, 0xD9, 0xF9 -> // SBC $A5B6,Y
                    offset = reg.y();
            default -> offset = reg.x();
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
        switch (opcode) { // ORA $A5B6,Y
            case 0x19, 0x1D -> // ORA $A5B6,X
                    alu.ora(value);
            // AND $A5B6,X
            case 0x3D, 0x39 -> // AND $A5B6,Y
                    alu.and(value);
            // EOR $A5B6,X
            case 0x5D, 0x59 -> // EOR $A5B6,Y
                    alu.eor(value);
            // ADC $A5B6,X
            case 0x7D, 0x79 -> // ADC $A5B6,Y
                    alu.adc(value);
            case 0xBB -> // *LAR $A5B6,Y
                    alu.las(value);
            case 0xBC -> // LDY $A5B6,X
                    alu.ldy(value);
            // LDA $A5B6,Y
            case 0xB9, 0xBD -> // LDA $A5B6,X
                    alu.lda(value);
            case 0xBE -> // LDX $A5B6,Y
                    alu.ldx(value);
            case 0xBF -> // *LAX $0557,Y
                    alu.lax(value);
            // CMP $A5B6,Y
            case 0xD9, 0xDD -> // CMP $A5B6,X
                    alu.cmp(value);
            // SBC $A5B6,Y
            case 0xF9, 0xFD -> // SBC $A5B6,X
                    alu.sbc(value);
            // *NOP $A9A9,X
            // *NOP $A9A9,X
            // *NOP $A9A9,X
            // *NOP $A9A9,X
            // *NOP $A9A9,X
            case 0x1C, 0x3C, 0x5C, 0x7C, 0xDC, 0xFC -> {
            }
        }
    }

    private void ABSOLUTE_INDEXED_READ_MODIFY_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        final int offset;
        switch (opcode) { // *SLO $0548,Y
            // *RLA $0548,Y
            // *SRE $0548,Y
            // *RRA $0548,Y
            // *DCP $0548,Y
            case 0x1B, 0x3B, 0x5B, 0x7B, 0xDB, 0xFB -> // *ISB $0548,Y 
                    offset = reg.y();
            default -> offset = reg.x();
        }
        value |= read(reg.pc()) << 8;
        int address = toU16(value + offset);
        reg.pcInc1();
        // 4
        read((value & 0xFF00) | (address & 0x00FF));
        // 5
        value = read(address);
        // 6
        write(address, value);
        switch (opcode) { // *SLO $0548,Y
            case 0x1B, 0x1F -> // *SLO $0548,X
                    value = alu.slo(value);
            case 0x1E -> // ASL $A5B6,X
                    value = alu.asl(value);
            // *RLA $0548,Y
            case 0x3B, 0x3F -> // *RLA $0548,X
                    value = alu.rla(value);
            case 0x3E -> // ROL $A5B6,X
                    value = alu.rol(value);
            // *SRE $0548,Y
            case 0x5B, 0x5F -> // *SRE $0548,X
                    value = alu.sre(value);
            case 0x5E -> // LSR $A5B6,X
                    value = alu.lsr(value);
            // *RRA $0548,Y
            case 0x7B, 0x7F -> // *RRA $0548,X
                    value = alu.rra(value);
            case 0x7E -> // ROR $A5B6,X
                    value = alu.ror(value);
            // *DCP $0548,Y
            case 0xDB, 0xDF -> // *DCP $0548,X
                    value = alu.dcp(value);
            case 0xDE -> // DEC $A5B6,X
                    value = alu.dec(value);
            // *ISB $0548,Y
            case 0xFB, 0xFF -> // *ISB $0548,X
                    value = alu.isc(value);
            case 0xFE -> // INC $A5B6,X
                    value = alu.inc(value);
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
        switch (opcode) { // *SHY $A5B6,X
            case 0x9C, 0x9D -> // STA $A5B6,X
                    offset = reg.x();
            default -> offset = reg.y();
        }
        int address = toU16(value + offset);
        value = (value & 0xFF00) | (address & 0x00FF);
        reg.pcInc1();
        // 4
        read(value);
        // 5        
        switch (opcode) { // STA $A5B6,Y
            case 0x99, 0x9D -> // STA $A5B6,X
                    alu.sta(address);
            case 0x9B -> // *XAS $A5B6,Y
                    alu.tas(address, high);
            case 0x9C -> {
                if ((value >> 8) != (address >> 8)) {
                    value &= reg.y() << 8;
                }
                alu.shy(value);
            }
            case 0x9E -> {
                if ((value >> 8) != (address >> 8)) {
                    value &= reg.x() << 8;
                }
                alu.shx(value);
            }
            case 0x9F -> {
                if ((value >> 8) != (address >> 8)) {
                    value &= (reg.x() & reg.a()) << 8;
                }
                alu.ahx(address);
            }
        }
    }

    // -- Relative addressing instructions ---------------------------------------

    private void RELATIVE_BRANCH() {
        // 2
        final int addressOffset = read(reg.pc());
        reg.pcInc1();
        boolean branchTaken = false;
        switch (opcode) {
            case 0x10 -> // BPL $A5
                    branchTaken = !reg.n();
            case 0x30 -> // BMI $A5
                    branchTaken = reg.n();
            case 0x50 -> // BVC $A5
                    branchTaken = !reg.v();
            case 0x70 -> // BVS $A5
                    branchTaken = reg.v();
            case 0x90 -> // BCC $A5
                    branchTaken = !reg.c();
            case 0xB0 -> // BCS $A5
                    branchTaken = reg.c();
            case 0xD0 -> // BNE $A5
                    branchTaken = !reg.z();
            case 0xF0 -> // BEQ $A5
                    branchTaken = reg.z();
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
            final int jumpAddress = toU16(reg.pc() + (byte) addressOffset);
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
        address1 = toU8(address1 + reg.x());
        // 4
        int address2 = read(address1);
        address1 = toU8(address1 + 1);
        // 5
        address2 |= read(address1) << 8;
        // 6        
        final int value = read(address2);
        switch (opcode) {
            case 0x01 -> // ORA ($A5,X)
                    alu.ora(value);
            case 0x21 -> // AND ($A5,X)
                    alu.and(value);
            case 0x41 -> // EOR ($A5,X)
                    alu.eor(value);
            case 0x61 -> // ADC ($A5,X)
                    alu.adc(value);
            case 0xA1 -> // LDA ($A5,X)
                    alu.lda(value);
            case 0xA3 -> // *LAX ($40,X)
                    alu.lax(value);
            case 0xC1 -> // CMP ($A5,X)
                    alu.cmp(value);
            case 0xE1 -> // SBC ($A5,X)
                    alu.sbc(value);
        }
    }

    private void INDEXED_INDIRECT_READ_MODIFY_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        read(value);
        value = toU8(value + reg.x());
        // 4
        int address = read(value);
        value = toU8(value + 1);
        // 5
        address |= read(value) << 8;
        // 6
        value = read(address);
        // 7
        write(address, value);
        switch (opcode) {
            case 0x03 -> // *SLO ($45,X)
                    value = alu.slo(value);
            case 0x23 -> // *RLA ($45,X)
                    value = alu.rla(value);
            case 0x43 -> // *SRE ($45,X)
                    value = alu.sre(value);
            case 0x63 -> // *RRA ($45,X)
                    value = alu.rra(value);
            case 0xC3 -> // *DCP ($45,X)
                    value = alu.dcp(value);
            case 0xE3 -> // *ISB ($45,X)
                    value = alu.isc(value);
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
        value = toU8(value + reg.x());
        // 4
        int address = read(value);
        value = toU8(value + 1);
        // 5
        address |= read(value) << 8;
        // 6        
        switch (opcode) {
            case 0x81: // STA ($A5,X)
                alu.sta(address);
                break;
            case 0x83: // *SAX ($49,X)
                alu.sax(address);
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
        address1 = toU8(++address1);
        // 4
        address2 |= read(address1) << 8;
        address1 = toU16(address2 + reg.y());
        address2 = (address2 & 0xFF00) | (address1 & 0x00FF);
        // 5
        int value = read(address2);
        if (address2 != address1) {
            // 6
            value = read(address1);
        }
        switch (opcode) {
            case 0x11: // ORA ($A5),Y
                alu.ora(value);
                break;
            case 0x31: // AND ($A5),Y
                alu.and(value);
                break;
            case 0x51: // EOR ($A5),Y
                alu.eor(value);
                break;
            case 0x71: // ADC ($A5),Y
                alu.adc(value);
                break;
            case 0xB1: // LDA ($A5),Y
                alu.lda(value);
                break;
            case 0xB3: // *LAX ($43),Y
                alu.lax(value);
                break;
            case 0xD1: // CMP ($A5),Y
                alu.cmp(value);
                break;
            case 0xF1: // SBC ($A5),Y
                alu.sbc(value);
                break;
        }
    }

    private void INDIRECT_INDEXED_READ_MODIFY_WRITE() {
        // 2
        int address = read(reg.pc());
        reg.pcInc1();
        // 3
        int value = read(address);
        address = toU8(++address);
        // 4
        value |= read(address) << 8;
        address = toU16(value + reg.y());
        value = (value & 0xFF00) | (address & 0x00FF);
        // 5
        read(value);
        // 6
        value = read(address);
        // 7
        write(address, value);
        switch (opcode) {
            case 0x13 -> // *SLO ($45),Y
                    value = alu.slo(value);
            case 0x33 -> // *RLA ($45),Y
                    value = alu.rla(value);
            case 0x53 -> // *SRE ($45),Y
                    value = alu.sre(value);
            case 0x73 -> // *RRA ($45),Y
                    value = alu.rra(value);
            case 0xD3 -> // *DCP ($45),Y
                    value = alu.dcp(value);
            case 0xF3 -> // *ISB ($45),Y
                    value = alu.isc(value);
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
        address = toU8(++address);
        // 4
        value |= read(address) << 8;
        address = toU16(value + reg.y());
        value = (value & 0xFF00) | (address & 0x00FF);
        // 5
        read(value);
        // 6        
        switch (opcode) {
            case 0x91: // STA ($A5),Y
                alu.sta(address);
                break;
            case 0x93: // *SHA ($A5),Y
                alu.ahx(address);
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