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
    private final AddressingMode mode = new AddressingMode(this);

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

        // 1
        opcode = read(reg.pc());
        reg.pcInc1();
        switch (opcode) {

            // Stack instructions
            case 0x00 -> BRK();
            case 0x40 -> RTI();
            case 0x60 -> RTS();
            case 0x08, 0x48 -> PUSH();
            case 0x28, 0x68 -> PULL();
            case 0x20 -> mode.absolute_jsr(alu::jsr);
            case 0x0A -> mode.accumulator(alu::asl);
            case 0x2A -> mode.accumulator(alu::rol);
            case 0x4A -> mode.accumulator(alu::lsr);
            case 0x6A -> mode.accumulator(alu::ror);
            case 0x18 -> mode.implied(alu::clc);
            case 0x38 -> mode.implied(alu::sec);
            case 0x58 -> mode.implied(alu::cli);
            case 0x78 -> mode.implied(alu::sei);
            case 0x88 -> mode.implied(alu::dey);
            case 0x8A -> mode.implied(alu::txa);
            case 0x98 -> mode.implied(alu::tya);
            case 0x9A -> mode.implied(alu::txs);
            case 0xA8 -> mode.implied(alu::tay);
            case 0xAA -> mode.implied(alu::tax);
            case 0xB8 -> mode.implied(alu::clv);
            case 0xBA -> mode.implied(alu::tsx);
            case 0xC8 -> mode.implied(alu::iny);
            case 0xCA -> mode.implied(alu::dex);
            case 0xD8 -> mode.implied(alu::cld);
            case 0xE8 -> mode.implied(alu::inx);
            case 0xF8 -> mode.implied(alu::sed);
            case 0x1A, 0x3A, 0x5A, 0x7A, 0xDA, 0xEA, 0xFA -> mode.implied(alu::nop);
            case 0x09 -> mode.immediate(alu::ora);
            case 0x0B, 0x2B -> mode.immediate(alu::anc);
            case 0x29 -> mode.immediate(alu::and);
            case 0x49 -> mode.immediate(alu::eor);
            case 0x4B -> mode.immediate(alu::alr);
            case 0x69 -> mode.immediate(alu::adc);
            case 0x6B -> mode.immediate(alu::arr);
            case 0x8B -> mode.immediate(alu::xaa);
            case 0xA0 -> mode.immediate(alu::ldy);
            case 0xA2 -> mode.immediate(alu::ldx);
            case 0xA9 -> mode.immediate(alu::lda);
            case 0xAB -> mode.immediate(alu::lax);
            case 0xC0 -> mode.immediate(alu::cpy);
            case 0xC9 -> mode.immediate(alu::cmp);
            case 0xCB -> mode.immediate(alu::axs);
            case 0xE0 -> mode.immediate(alu::cpx);
            case 0xE9, 0xEB -> mode.immediate(alu::sbc);
            case 0x80, 0x82, 0x89, 0xC2, 0xE2 -> mode.immediate(alu::nop);
            case 0x4C -> mode.absolute_jump(alu::jmp);
            case 0x0C -> mode.absolute_read(alu::nop);
            case 0x0D -> mode.absolute_read(alu::ora);
            case 0x2C -> mode.absolute_read(alu::bit);
            case 0x2D -> mode.absolute_read(alu::and);
            case 0x4D -> mode.absolute_read(alu::eor);
            case 0x6D -> mode.absolute_read(alu::adc);
            case 0xAC -> mode.absolute_read(alu::ldy);
            case 0xAD -> mode.absolute_read(alu::lda);
            case 0xAE -> mode.absolute_read(alu::ldx);
            case 0xAF -> mode.absolute_read(alu::lax);
            case 0xCC -> mode.absolute_read(alu::cpy);
            case 0xCD -> mode.absolute_read(alu::cmp);
            case 0xEC -> mode.absolute_read(alu::cpx);
            case 0xED -> mode.absolute_read(alu::sbc);
            case 0x0E -> mode.absolute_modify(alu::asl);
            case 0x0F -> mode.absolute_modify(alu::slo);
            case 0x2E -> mode.absolute_modify(alu::rol);
            case 0x2F -> mode.absolute_modify(alu::rla);
            case 0x4F -> mode.absolute_modify(alu::sre);
            case 0x4E -> mode.absolute_modify(alu::lsr);
            case 0x6E -> mode.absolute_modify(alu::ror);
            case 0x6F -> mode.absolute_modify(alu::rra);
            case 0xCE -> mode.absolute_modify(alu::dec);
            case 0xCF -> mode.absolute_modify(alu::dcp);
            case 0xEE -> mode.absolute_modify(alu::inc);
            case 0xEF -> mode.absolute_modify(alu::isc);
            case 0x8C -> mode.absolute_write(alu::sty);
            case 0x8D -> mode.absolute_write(alu::sta);
            case 0x8E -> mode.absolute_write(alu::stx);
            case 0x8F -> mode.absolute_write(alu::sax);
            case 0x04, 0x44, 0x64 -> mode.zero_page_read(alu::nop);
            case 0x05 -> mode.zero_page_read(alu::ora);
            case 0x24 -> mode.zero_page_read(alu::bit);
            case 0x25 -> mode.zero_page_read(alu::and);
            case 0x45 -> mode.zero_page_read(alu::eor);
            case 0x65 -> mode.zero_page_read(alu::adc);
            case 0xA4 -> mode.zero_page_read(alu::ldy);
            case 0xA5 -> mode.zero_page_read(alu::lda);
            case 0xA6 -> mode.zero_page_read(alu::ldx);
            case 0xA7 -> mode.zero_page_read(alu::lax);
            case 0xC4 -> mode.zero_page_read(alu::cpy);
            case 0xC5 -> mode.zero_page_read(alu::cmp);
            case 0xE4 -> mode.zero_page_read(alu::cpx);
            case 0xE5 -> mode.zero_page_read(alu::sbc);
            case 0x06 -> mode.zero_page_modify(alu::asl);
            case 0x07 -> mode.zero_page_modify(alu::slo);
            case 0x26 -> mode.zero_page_modify(alu::rol);
            case 0x27 -> mode.zero_page_modify(alu::rla);
            case 0x46 -> mode.zero_page_modify(alu::lsr);
            case 0x47 -> mode.zero_page_modify(alu::sre);
            case 0x66 -> mode.zero_page_modify(alu::ror);
            case 0x67 -> mode.zero_page_modify(alu::rra);
            case 0xC6 -> mode.zero_page_modify(alu::dec);
            case 0xC7 -> mode.zero_page_modify(alu::dcp);
            case 0xE6 -> mode.zero_page_modify(alu::inc);
            case 0xE7 -> mode.zero_page_modify(alu::isc);
            case 0x84 -> mode.zero_page_write(alu::sty);
            case 0x85 -> mode.zero_page_write(alu::sta);
            case 0x86 -> mode.zero_page_write(alu::stx);
            case 0x87 -> mode.zero_page_write(alu::sax);
            case 0x14, 0x15, 0x34, 0x35, 0x54, 0x55, 0x74, 0x75, 0xB4, 0xB5, 0xB6, 0xB7, 0xD4, 0xD5, 0xF4, 0xF5 ->
                    ZERO_PAGE_INDEXED_READ();
            case 0x16, 0x17, 0x36, 0x37, 0x56, 0x57, 0x76, 0x77, 0xD6, 0xD7, 0xF6, 0xF7 ->
                    ZERO_PAGE_INDEXED_READ_MODIFY_WRITE();
            case 0x94, 0x95, 0x96, 0x97 -> ZERO_PAGE_INDEXED_WRITE();
            case 0x19 -> mode.absolute_indexed_y_read(alu::ora);
            case 0x39 -> mode.absolute_indexed_y_read(alu::and);
            case 0x59 -> mode.absolute_indexed_y_read(alu::eor);
            case 0x79 -> mode.absolute_indexed_y_read(alu::adc);
            case 0xB9 -> mode.absolute_indexed_y_read(alu::lda);
            case 0xBB -> mode.absolute_indexed_y_read(alu::las);
            case 0xBE -> mode.absolute_indexed_y_read(alu::ldx);
            case 0xBF -> mode.absolute_indexed_y_read(alu::lax);
            case 0xD9 -> mode.absolute_indexed_y_read(alu::cmp);
            case 0xF9 -> mode.absolute_indexed_y_read(alu::sbc);
            case 0x1D -> mode.absolute_indexed_x_read(alu::ora);
            case 0x3D -> mode.absolute_indexed_x_read(alu::and);
            case 0x5D -> mode.absolute_indexed_x_read(alu::eor);
            case 0x7D -> mode.absolute_indexed_x_read(alu::adc);
            case 0xBC -> mode.absolute_indexed_x_read(alu::ldy);
            case 0xBD -> mode.absolute_indexed_x_read(alu::lda);
            case 0xDD -> mode.absolute_indexed_x_read(alu::cmp);
            case 0xFD -> mode.absolute_indexed_x_read(alu::sbc);
            case 0x1C, 0x3C, 0x5C, 0x7C, 0xDC, 0xFC -> mode.absolute_indexed_x_read(alu::nop);
            case 0x1B -> mode.absolute_indexed_y_modify(alu::slo);
            case 0x3B -> mode.absolute_indexed_y_modify(alu::rla);
            case 0x5B -> mode.absolute_indexed_y_modify(alu::sre);
            case 0x7B -> mode.absolute_indexed_y_modify(alu::rra);
            case 0xDB -> mode.absolute_indexed_y_modify(alu::dcp);
            case 0xFB -> mode.absolute_indexed_y_modify(alu::isc);
            case 0x1E -> mode.absolute_indexed_x_modify(alu::asl);
            case 0x1F -> mode.absolute_indexed_x_modify(alu::slo);
            case 0x3E -> mode.absolute_indexed_x_modify(alu::rol);
            case 0x3F -> mode.absolute_indexed_x_modify(alu::rla);
            case 0x5E -> mode.absolute_indexed_x_modify(alu::lsr);
            case 0x5F -> mode.absolute_indexed_x_modify(alu::sre);
            case 0x7E -> mode.absolute_indexed_x_modify(alu::ror);
            case 0x7F -> mode.absolute_indexed_x_modify(alu::rra);
            case 0xDE -> mode.absolute_indexed_x_modify(alu::dec);
            case 0xDF -> mode.absolute_indexed_x_modify(alu::dcp);
            case 0xFE -> mode.absolute_indexed_x_modify(alu::inc);
            case 0xFF -> mode.absolute_indexed_x_modify(alu::isc);
            case 0x99, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F -> ABSOLUTE_INDEXED_WRITE();
            case 0x10, 0x30, 0x50, 0x70, 0x90, 0xB0, 0xD0, 0xF0 -> RELATIVE_BRANCH();
            case 0x01, 0x21, 0x41, 0x61, 0xA1, 0xA3, 0xC1, 0xE1 -> INDEXED_INDIRECT_READ();
            case 0x03, 0x23, 0x43, 0x63, 0xC3, 0xE3 -> INDEXED_INDIRECT_READ_MODIFY_WRITE();
            case 0x81, 0x83 -> INDEXED_INDIRECT_WRITE();
            case 0x11, 0x31, 0x51, 0x71, 0xB1, 0xB3, 0xD1, 0xF1 -> INDIRECT_INDEXED_READ();
            case 0x13, 0x33, 0x53, 0x73, 0xD3, 0xF3 -> INDIRECT_INDEXED_READ_MODIFY_WRITE();
            case 0x91, 0x93 -> INDIRECT_INDEXED_WRITE();
            case 0x6C -> mode.absolute_indirect_jump(alu::jmp);
            case 0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72, 0x92, 0xB2, 0xD2, 0xF2 -> KIL();
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

    public int read(final int address) {

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
        boolean _r = reg.r();
        boolean _b = reg.b();
        reg.p(readStack());
        reg.r(_r);
        reg.b(_b);
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

    private void KIL() {
        reg.pcDec1();
        if (running) {
            running = false;
            App.cpuKilled(mapper.readCpuMemory(reg.pc()), reg.pc());
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
            case 0xB6, 0xB7 -> address += reg.y();
            default -> address += reg.x();
        }
        address &= 0x00FF;
        // 4        
        final int value = read(address);
        switch (opcode) {
            case 0x15 -> alu.ora(value);
            case 0x35 -> alu.and(value);
            case 0x55 -> alu.eor(value);
            case 0x75 -> alu.adc(value);
            case 0xB4 -> alu.ldy(value);
            case 0xB5 -> alu.lda(value);
            case 0xB6 -> alu.ldx(value);
            case 0xB7 -> alu.lax(value);
            case 0xD5 -> alu.cmp(value);
            case 0xF5 -> alu.sbc(value);
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
            case 0x16 -> value = alu.asl(value);
            case 0x17 -> value = alu.slo(value);
            case 0x36 -> value = alu.rol(value);
            case 0x37 -> value = alu.rla(value);
            case 0x56 -> value = alu.lsr(value);
            case 0x57 -> value = alu.sre(value);
            case 0x76 -> value = alu.ror(value);
            case 0x77 -> value = alu.rra(value);
            case 0xD6 -> value = alu.dec(value);
            case 0xD7 -> value = alu.dcp(value);
            case 0xF6 -> value = alu.inc(value);
            case 0xF7 -> value = alu.isc(value);
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
            case 0x94 -> alu.sty(toU8(address + reg.x()));
            case 0x95 -> alu.sta(toU8(address + reg.x()));
            case 0x96 -> alu.stx(toU8(address + reg.y()));
            case 0x97 -> alu.sax(toU8(address + reg.y()));
        }
    }

    // -- Absolute indexed addressing instructions -------------------------------

    private void ABSOLUTE_INDEXED_WRITE() {
        // 2
        int value = read(reg.pc());
        reg.pcInc1();
        // 3
        final int offset;
        final int high = read(reg.pc());
        value |= high << 8;
        switch (opcode) {
            case 0x9C, 0x9D -> offset = reg.x();
            default -> offset = reg.y();
        }
        int address = toU16(value + offset);
        value = (value & 0xFF00) | (address & 0x00FF);
        reg.pcInc1();
        // 4
        read(value);
        // 5        
        switch (opcode) {
            case 0x99, 0x9D -> alu.sta(address);
            case 0x9B -> alu.tas(address, high);
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
            case 0x10 -> branchTaken = !reg.n();
            case 0x30 -> branchTaken = reg.n();
            case 0x50 -> branchTaken = !reg.v();
            case 0x70 -> branchTaken = reg.v();
            case 0x90 -> branchTaken = !reg.c();
            case 0xB0 -> branchTaken = reg.c();
            case 0xD0 -> branchTaken = !reg.z();
            case 0xF0 -> branchTaken = reg.z();
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
            case 0x01 -> alu.ora(value);
            case 0x21 -> alu.and(value);
            case 0x41 -> alu.eor(value);
            case 0x61 -> alu.adc(value);
            case 0xA1 -> alu.lda(value);
            case 0xA3 -> alu.lax(value);
            case 0xC1 -> alu.cmp(value);
            case 0xE1 -> alu.sbc(value);
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
            case 0x03 -> value = alu.slo(value);
            case 0x23 -> value = alu.rla(value);
            case 0x43 -> value = alu.sre(value);
            case 0x63 -> value = alu.rra(value);
            case 0xC3 -> value = alu.dcp(value);
            case 0xE3 -> value = alu.isc(value);
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
            case 0x81 -> alu.sta(address);
            case 0x83 -> alu.sax(address);
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
            case 0x11 -> alu.ora(value);
            case 0x31 -> alu.and(value);
            case 0x51 -> alu.eor(value);
            case 0x71 -> alu.adc(value);
            case 0xB1 -> alu.lda(value);
            case 0xB3 -> alu.lax(value);
            case 0xD1 -> alu.cmp(value);
            case 0xF1 -> alu.sbc(value);
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
            case 0x13 -> value = alu.slo(value);
            case 0x33 -> value = alu.rla(value);
            case 0x53 -> value = alu.sre(value);
            case 0x73 -> value = alu.rra(value);
            case 0xD3 -> value = alu.dcp(value);
            case 0xF3 -> value = alu.isc(value);
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
            case 0x91 -> alu.sta(address);
            case 0x93 -> alu.ahx(address);
        }
    }

    public Register getRegister() {
        return reg;
    }
}