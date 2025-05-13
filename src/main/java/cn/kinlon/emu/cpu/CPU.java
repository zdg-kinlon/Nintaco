package cn.kinlon.emu.cpu;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.PPU;
import cn.kinlon.emu.ServicedType;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.DeltaModulationChannel;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.utils.ByteUtil;

import java.io.Serializable;

import static cn.kinlon.emu.PPU.REG_OAM_DATA;
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
            case 0x00 -> // BRK
                    BRK();
            case 0x40 -> // RTI
                    RTI();
            case 0x60 -> // RTS
                    RTS();
            // PHP
            case 0x08, 0x48 -> // PHA
                    PUSH();
            // PLP
            case 0x28, 0x68 -> // PLA
                    PULL();
            case 0x20 -> // JSR $A5B6
                    JSR();


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
            case 0x0A, 0x1A, 0x18, 0x2A, 0x38, 0x3A, 0x4A, 0x58, 0x5A, 0x6A, 0x78, 0x7A, 0x88, 0x8A, 0x98, 0x9A, 0xA8,
                 0xAA, 0xB8, 0xBA, 0xC8, 0xCA, 0xD8, 0xDA, 0xE8, 0xEA, 0xF8, 0xFA -> // *NOP
                    ACCUMULATOR_OR_IMPLIED();


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
            case 0x09, 0x0B, 0x2B, 0x29, 0x49, 0x4B, 0x69, 0x6B, 0x80, 0x82, 0x89, 0x8B, 0xA0, 0xA2, 0xA9, 0xAB, 0xC0,
                 0xC2, 0xC9, 0xCB, 0xE0, 0xE2, 0xE9, 0xEB -> // *SBC #$40
                    IMMEDIATE();


            // Absolute addressing instructions
            case 0x4C -> // JMP $A5B6
                    ABSOLUTE_JUMP();
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
            case 0x0C, 0x0D, 0x2C, 0x2D, 0x4D, 0x6D, 0xAC, 0xAD, 0xAE, 0xAF, 0xCC, 0xCD, 0xEC, 0xED -> // SBC $A5B6
                    ABSOLUTE_READ();
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
            case 0x0E, 0x0F, 0x2E, 0x2F, 0x4F, 0x4E, 0x6E, 0x6F, 0xCE, 0xCF, 0xEE, 0xEF -> // *ISB $0647
                    ABSOLUTE_READ_MODIFY_WRITE();
            // STY $A5B6
            // STA $A5B6
            // STX $A5B6
            case 0x8C, 0x8D, 0x8E, 0x8F -> // *SAX $0549
                    ABSOLUTE_WRITE();


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
            case 0x04, 0x05, 0x24, 0x25, 0x44, 0x45, 0x64, 0x65, 0xA4, 0xA5, 0xA6, 0xA7, 0xC4, 0xC5, 0xE4,
                 0xE5 -> // SBC $A5
                    ZERO_PAGE_READ();
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
            case 0x06, 0x07, 0x26, 0x27, 0x46, 0x47, 0x66, 0x67, 0xC6, 0xC7, 0xE6, 0xE7 -> // *ISB $47
                    ZERO_PAGE_READ_MODIFY_WRITE();
            // STY $A5
            // STA $A5
            // STX $A5
            case 0x84, 0x85, 0x86, 0x87 -> // *SAX $49
                    ZERO_PAGE_WRITE();


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
            case 0x14, 0x15, 0x34, 0x35, 0x54, 0x55, 0x74, 0x75, 0xB4, 0xB5, 0xB6, 0xB7, 0xD4, 0xD5, 0xF4,
                 0xF5 -> // SBC $A5,X
                    ZERO_PAGE_INDEXED_READ();
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
            case 0x16, 0x17, 0x36, 0x37, 0x56, 0x57, 0x76, 0x77, 0xD6, 0xD7, 0xF6, 0xF7 -> // *ISB $48,X
                    ZERO_PAGE_INDEXED_READ_MODIFY_WRITE();
            // STY $A5,X
            // STA $A5,X
            // STX $A5,Y
            case 0x94, 0x95, 0x96, 0x97 -> // *SAX $4A,Y
                    ZERO_PAGE_INDEXED_WRITE();


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
            case 0x19, 0x1C, 0x1D, 0x3C, 0x3D, 0x39, 0x5C, 0x5D, 0x59, 0x7C, 0x7D, 0x79, 0xBB, 0xBC, 0xB9, 0xBD, 0xBE,
                 0xBF, 0xD9, 0xDC, 0xDD, 0xF9, 0xFC, 0xFD -> // SBC $A5B6,X
                    ABSOLUTE_INDEXED_READ();
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
            case 0x1B, 0x1E, 0x1F, 0x3B, 0x3E, 0x3F, 0x5E, 0x5B, 0x5F, 0x7B, 0x7E, 0x7F, 0xDB, 0xDE, 0xDF, 0xFE, 0xFB,
                 0xFF -> // *ISB $0548,X
                    ABSOLUTE_INDEXED_READ_MODIFY_WRITE();
            // STA $A5B6,Y
            // *XAS $A5B6,Y  
            // *SHY $A5B6,X
            // STA $A5B6,X
            // *SHX $A5B6,Y
            case 0x99, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F -> // *SHA $A5B6,Y
                    ABSOLUTE_INDEXED_WRITE();


            // Relative addressing instructions
            // BPL $A5
            // BMI $A5
            // BVC $A5
            // BVS $A5
            // BCC $A5
            // BCS $A5
            // BNE $A5
            case 0x10, 0x30, 0x50, 0x70, 0x90, 0xB0, 0xD0, 0xF0 -> // BEQ $A5
                    RELATIVE_BRANCH();


            // Indexed indirect addressing instructions
            // ORA ($A5,X)
            // AND ($A5,X)
            // EOR ($A5,X)
            // ADC ($A5,X)
            // LDA ($A5,X)
            // *LAX ($40,X)
            // CMP ($A5,X)
            case 0x01, 0x21, 0x41, 0x61, 0xA1, 0xA3, 0xC1, 0xE1 -> // SBC ($A5,X)
                    INDEXED_INDIRECT_READ();
            // *SLO ($45,X)
            // *RLA ($45,X)
            // *SRE ($45,X)
            // *RRA ($45,X)
            // *DCP ($45,X)
            case 0x03, 0x23, 0x43, 0x63, 0xC3, 0xE3 -> // *ISB ($45,X)
                    INDEXED_INDIRECT_READ_MODIFY_WRITE();
            // STA ($A5,X)
            case 0x81, 0x83 -> // *SAX ($49,X)
                    INDEXED_INDIRECT_WRITE();


            // Indirect indexed addressing instructions
            // ORA ($A5),Y
            // AND ($A5),Y
            // EOR ($A5),Y
            // ADC ($A5),Y
            // LDA ($A5),Y
            // *LAX ($43),Y  
            // CMP ($A5),Y
            case 0x11, 0x31, 0x51, 0x71, 0xB1, 0xB3, 0xD1, 0xF1 -> // SBC ($A5),Y
                    INDIRECT_INDEXED_READ();
            // *SLO ($45),Y
            // *RLA ($45),Y
            // *SRE ($45),Y
            // *RRA ($45),Y
            // *DCP ($45),Y
            case 0x13, 0x33, 0x53, 0x73, 0xD3, 0xF3 -> // *ISB ($45),Y
                    INDIRECT_INDEXED_READ_MODIFY_WRITE();
            // STA ($A5),Y
            case 0x91, 0x93 -> // *SHA ($A5),Y
                    INDIRECT_INDEXED_WRITE();


            // Absolute indirect addressing instructions
            case 0x6C -> // JMP ($A5B6)
                    ABSOLUTE_INDIRECT_JUMP();
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
            case 0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72, 0x92, 0xB2, 0xD2, 0xF2 -> // *KIL
                    KIL();
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
        int t = reg.a() + value + toBit(reg.c());

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
        value = toU8(value - 1);
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
        value = toU8(value + 1);

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
                reg.r(true);
                reg.b(true);
                writeStack(reg.p());
                reg.b(false);
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
                boolean r = reg.r();
                boolean b = reg.b();
                reg.p(readStack());
                reg.r(r);
                reg.b(b);
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
            case 0x0A -> // ASL A
                    reg.a(ASL(reg.a()));
            case 0x18 -> // CLC
                    CLC();
            case 0x2A -> // ROL A
                    reg.a(ROL(reg.a()));
            case 0x38 -> // SEC
                    SEC();
            case 0x4A -> // LSR A
                    reg.a(LSR(reg.a()));
            case 0x58 -> // CLI
                    CLI();
            case 0x6A -> // ROR A
                    reg.a(ROR(reg.a()));
            case 0x78 -> // SEI
                    SEI();
            case 0x88 -> // DEY
                    DEY();
            case 0x8A -> // TXA
                    TXA();
            case 0x98 -> // TYA
                    TYA();
            case 0x9A -> // TXS
                    TXS();
            case 0xA8 -> // TAY
                    TAY();
            case 0xAA -> // TAX
                    TAX();
            case 0xB8 -> // CLV
                    CLV();
            case 0xBA -> // TSX
                    TSX();
            case 0xC8 -> // INY
                    INY();
            case 0xCA -> // DEX
                    DEX();
            case 0xD8 -> // CLD
                    CLD();
            case 0xE8 -> // INX
                    INX();
            case 0xF8 -> // SED
                    SED();
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
                    ORA(value);
            // *AAC #$A5
            case 0x0B, 0x2B -> // *AAC #$A5
                    AAC(value);
            case 0x29 -> // AND #$A5
                    AND(value);
            case 0x49 -> // EOR #$A5
                    EOR(value);
            case 0x4B -> // *ASR #$A5
                    ASR(value);
            case 0x69 -> // ADC #$A5
                    ADC(value);
            case 0x6B -> // *ARR #$A5
                    ARR(value);
            case 0x8B -> // *XAA #$A5
                    XAA(value);
            case 0xA0 -> // LDY #$A5
                    LDY(value);
            case 0xA2 -> // LDX #$A5
                    LDX(value);
            case 0xA9 -> // LDA #$A5
                    LDA(value);
            case 0xAB -> // *ATX #$A5
                    ATX(value);
            case 0xC0 -> // CPY #$A5
                    CPY(value);
            case 0xC9 -> // CMP #$A5
                    CMP(value);
            case 0xCB -> // *AXS #$A5
                    AXS(value);
            case 0xE0 -> // CPX #$A5
                    CPX(value);
            // SBC #$A5
            case 0xE9, 0xEB -> // *SBC #$40
                    SBC(value);
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
                    ORA(value);
            case 0x2C -> // BIT $A5B6
                    BIT(value);
            case 0x2D -> // AND $A5B6
                    AND(value);
            case 0x4D -> // EOR $A5B6
                    EOR(value);
            case 0x6D -> // ADC $A5B6
                    ADC(value);
            case 0xAC -> // LDY $A5B6
                    LDY(value);
            case 0xAD -> // LDA $A5B6
                    LDA(value);
            case 0xAE -> // LDX $A5B6
                    LDX(value);
            case 0xAF -> // *LAX $0577
                    LAX(value);
            case 0xCC -> // CPY $A5B6
                    CPY(value);
            case 0xCD -> // CMP $A5B6
                    CMP(value);
            case 0xEC -> // CPX $A5B6
                    CPX(value);
            case 0xED -> // SBC $A5B6
                    SBC(value);
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
                    value = ASL(value);
            case 0x0F -> // *SLO $0647
                    value = SLO(value);
            case 0x2E -> // ROL $A5B6
                    value = ROL(value);
            case 0x2F -> // *RLA $0647
                    value = RLA(value);
            case 0x4F -> // *SRE $0647
                    value = SRE(value);
            case 0x4E -> // LSR $A5B6
                    value = LSR(value);
            case 0x6E -> // ROR $A5B6
                    value = ROR(value);
            case 0x6F -> // *RRA $0647
                    value = RRA(value);
            case 0xCE -> // DEC $A5B6
                    value = DEC(value);
            case 0xCF -> // *DCP $0647
                    value = DCP(value);
            case 0xEE -> // INC $A5B6
                    value = INC(value);
            case 0xEF -> // *ISB $0647
                    value = ISB(value);
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
                    write(address, reg.y());
            case 0x8D -> // STA $A5B6
                    write(address, reg.a());
            case 0x8E -> // STX $A5B6
                    write(address, reg.x());
            case 0x8F -> // *SAX $0549
                    write(address, reg.a() & reg.x());
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
                    ORA(value);
            case 0x24 -> // BIT $A5
                    BIT(value);
            case 0x25 -> // AND $A5
                    AND(value);
            case 0x45 -> // EOR $A5
                    EOR(value);
            case 0x65 -> // ADC $A5
                    ADC(value);
            case 0xA4 -> // LDY $A5
                    LDY(value);
            case 0xA5 -> // LDA $A5
                    LDA(value);
            case 0xA6 -> // LDX $A5
                    LDX(value);
            case 0xA7 -> // *LAX $67
                    LAX(value);
            case 0xC4 -> // CPY $A5
                    CPY(value);
            case 0xC5 -> // CMP $A5
                    CMP(value);
            case 0xE4 -> // CPX $A5
                    CPX(value);
            case 0xE5 -> // SBC $A5
                    SBC(value);
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
                    value = ASL(value);
            case 0x07 -> // *SLO $47
                    value = SLO(value);
            case 0x26 -> // ROL $A5
                    value = ROL(value);
            case 0x27 -> // *RLA $47
                    value = RLA(value);
            case 0x46 -> // LSR $A5
                    value = LSR(value);
            case 0x47 -> // *SRE $47
                    value = SRE(value);
            case 0x66 -> // ROR $A5
                    value = ROR(value);
            case 0x67 -> // *RRA $47
                    value = RRA(value);
            case 0xC6 -> // DEC $A5
                    value = DEC(value);
            case 0xC7 -> // *DCP $47
                    value = DCP(value);
            case 0xE6 -> // INC $A5
                    value = INC(value);
            case 0xE7 -> // *ISB $47
                    value = ISB(value);
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
                    write(address, reg.y());
            case 0x85 -> // STA $A5
                    write(address, reg.a());
            case 0x86 -> // STX $A5
                    write(address, reg.x());
            case 0x87 -> // *SAX $49
                    write(address, reg.a() & reg.x());
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
                    ORA(value);
            case 0x35 -> // AND $A5,X
                    AND(value);
            case 0x55 -> // EOR $A5,X
                    EOR(value);
            case 0x75 -> // ADC $A5,X
                    ADC(value);
            case 0xB4 -> // LDY $A5,X
                    LDY(value);
            case 0xB5 -> // LDA $A5,X
                    LDA(value);
            case 0xB6 -> // LDX $A5,Y
                    LDX(value);
            case 0xB7 -> // *LAX $10,Y
                    LAX(value);
            case 0xD5 -> // CMP $A5,X
                    CMP(value);
            case 0xF5 -> // SBC $A5,X
                    SBC(value);
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
                    value = ASL(value);
            case 0x17 -> // *SLO $48,X
                    value = SLO(value);
            case 0x36 -> // ROL $A5,X
                    value = ROL(value);
            case 0x37 -> // *RLA $48,X
                    value = RLA(value);
            case 0x56 -> // LSR $A5,X
                    value = LSR(value);
            case 0x57 -> // *SRE $48,X
                    value = SRE(value);
            case 0x76 -> // ROR $A5,X
                    value = ROR(value);
            case 0x77 -> // *RRA $48,X
                    value = RRA(value);
            case 0xD6 -> // DEC $A5,X
                    value = DEC(value);
            case 0xD7 -> // *DCP $48,X
                    value = DCP(value);
            case 0xF6 -> // INC $A5,X
                    value = INC(value);
            case 0xF7 -> // *ISB $48,X
                    value = ISB(value);
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
        switch (opcode) { // STY $A5,X
            case 0x94, 0x95 -> // STA $A5,X
                    address = toU8(address + reg.x());
            // STX $A5,Y
            case 0x96, 0x97 -> // *SAX $4A,Y
                    address = toU8(address + reg.y());
        }
        // 4        
        switch (opcode) {
            case 0x94 -> // STY $A5,X
                    write(address, reg.y());
            case 0x95 -> // STA $A5,X
                    write(address, reg.a());
            case 0x96 -> // STX $A5,Y
                    write(address, reg.x());
            case 0x97 -> // *SAX $4A,Y
                    write(address, reg.a() & reg.x());
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
                    ORA(value);
            // AND $A5B6,X
            case 0x3D, 0x39 -> // AND $A5B6,Y
                    AND(value);
            // EOR $A5B6,X
            case 0x5D, 0x59 -> // EOR $A5B6,Y
                    EOR(value);
            // ADC $A5B6,X
            case 0x7D, 0x79 -> // ADC $A5B6,Y
                    ADC(value);
            case 0xBB -> // *LAR $A5B6,Y
                    LAR(value);
            case 0xBC -> // LDY $A5B6,X
                    LDY(value);
            // LDA $A5B6,Y
            case 0xB9, 0xBD -> // LDA $A5B6,X
                    LDA(value);
            case 0xBE -> // LDX $A5B6,Y
                    LDX(value);
            case 0xBF -> // *LAX $0557,Y
                    LAX(value);
            // CMP $A5B6,Y
            case 0xD9, 0xDD -> // CMP $A5B6,X
                    CMP(value);
            // SBC $A5B6,Y
            case 0xF9, 0xFD -> // SBC $A5B6,X
                    SBC(value);
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
                    value = SLO(value);
            case 0x1E -> // ASL $A5B6,X
                    value = ASL(value);
            // *RLA $0548,Y
            case 0x3B, 0x3F -> // *RLA $0548,X
                    value = RLA(value);
            case 0x3E -> // ROL $A5B6,X
                    value = ROL(value);
            // *SRE $0548,Y
            case 0x5B, 0x5F -> // *SRE $0548,X
                    value = SRE(value);
            case 0x5E -> // LSR $A5B6,X
                    value = LSR(value);
            // *RRA $0548,Y
            case 0x7B, 0x7F -> // *RRA $0548,X
                    value = RRA(value);
            case 0x7E -> // ROR $A5B6,X
                    value = ROR(value);
            // *DCP $0548,Y
            case 0xDB, 0xDF -> // *DCP $0548,X
                    value = DCP(value);
            case 0xDE -> // DEC $A5B6,X
                    value = DEC(value);
            // *ISB $0548,Y
            case 0xFB, 0xFF -> // *ISB $0548,X
                    value = ISB(value);
            case 0xFE -> // INC $A5B6,X
                    value = INC(value);
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
                    write(address, reg.a());
            case 0x9B -> // *XAS $A5B6,Y
                    write(address, XAS(high + 1));
            case 0x9C -> {
                if ((value >> 8) != (address >> 8)) {
                    value &= reg.y() << 8;
                }
                write(value, reg.y() & ((value >> 8) + 1));
            }
            case 0x9E -> {
                if ((value >> 8) != (address >> 8)) {
                    value &= reg.x() << 8;
                }
                write(value, reg.x() & ((value >> 8) + 1));
            }
            case 0x9F -> {
                if ((value >> 8) != (address >> 8)) {
                    value &= (reg.x() & reg.a()) << 8;
                }
                write(address, reg.x() & reg.a() & ((address >> 8) + 1));
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
                    ORA(value);
            case 0x21 -> // AND ($A5,X)
                    AND(value);
            case 0x41 -> // EOR ($A5,X)
                    EOR(value);
            case 0x61 -> // ADC ($A5,X)
                    ADC(value);
            case 0xA1 -> // LDA ($A5,X)
                    LDA(value);
            case 0xA3 -> // *LAX ($40,X)
                    LAX(value);
            case 0xC1 -> // CMP ($A5,X)
                    CMP(value);
            case 0xE1 -> // SBC ($A5,X)
                    SBC(value);
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
                    value = SLO(value);
            case 0x23 -> // *RLA ($45,X)
                    value = RLA(value);
            case 0x43 -> // *SRE ($45,X)
                    value = SRE(value);
            case 0x63 -> // *RRA ($45,X)
                    value = RRA(value);
            case 0xC3 -> // *DCP ($45,X)
                    value = DCP(value);
            case 0xE3 -> // *ISB ($45,X)
                    value = ISB(value);
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