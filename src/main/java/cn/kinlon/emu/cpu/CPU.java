package cn.kinlon.emu.cpu;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.ppu.PPU;
import cn.kinlon.emu.ServicedType;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.DeltaModulationChannel;
import cn.kinlon.emu.mappers.Mapper;

import java.io.Serializable;
import java.util.function.Supplier;

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

    public static final int VEC_NMI = 0xfffa;
    public static final int VEC_RESET = 0xfffc;
    public static final int VEC_IRQ = 0xfffe;

    private static final int IRQ_MAPPER = 0;
    private static final int IRQ_APU = 1;
    private static final int IRQ_DMC = 2;

    private final Register reg = new Register();
    private final Stack stack = new Stack(this);
    private final InstructionExecutor exe = new InstructionExecutor(this);
    private final AddressMode mode = new AddressMode(this);
    private final Interrupt interrupt = new Interrupt();

    private long cycleCounter;
    private boolean running = true;

    private volatile int instructionsCounter;

    private Mapper mapper;
    private PPU ppu;
    private APU apu;
    private DeltaModulationChannel dmc;

    private int dmcCycle;
    private int dmcAddress;

    // -- public methods ---------------------------------------------------------

    public void executeInstruction() {

        interrupt.serviced(ServicedType.None);

        if (interrupt.resetRequested()) {
            interrupt.resetRequested(false);
            running = true;
            apu.reset();
            ppu.reset();
            rst();
        }

        // 1
        int opcode = read_u8(reg.pc());
        reg.pcInc1();
        switch (opcode) {
            case 0x00 -> mode.implied(this::brk);
            case 0x40 -> mode.implied(exe::rti);
            case 0x60 -> mode.implied(exe::rts);
            case 0x08 -> mode.implied(exe::php);
            case 0x48 -> mode.implied(exe::pha);
            case 0x28 -> mode.implied(exe::plp);
            case 0x68 -> mode.implied(exe::pla);
            case 0x20 -> mode.immediate(exe::jsr);
            case 0x0A -> mode.implied_accumulator(exe::asl);
            case 0x2A -> mode.implied_accumulator(exe::rol);
            case 0x4A -> mode.implied_accumulator(exe::lsr);
            case 0x6A -> mode.implied_accumulator(exe::ror);
            case 0x18 -> mode.implied(exe::clc);
            case 0x38 -> mode.implied(exe::sec);
            case 0x58 -> mode.implied(exe::cli);
            case 0x78 -> mode.implied(exe::sei);
            case 0x88 -> mode.implied(exe::dey);
            case 0x8A -> mode.implied(exe::txa);
            case 0x98 -> mode.implied(exe::tya);
            case 0x9A -> mode.implied(exe::txs);
            case 0xA8 -> mode.implied(exe::tay);
            case 0xAA -> mode.implied(exe::tax);
            case 0xB8 -> mode.implied(exe::clv);
            case 0xBA -> mode.implied(exe::tsx);
            case 0xC8 -> mode.implied(exe::iny);
            case 0xCA -> mode.implied(exe::dex);
            case 0xD8 -> mode.implied(exe::cld);
            case 0xE8 -> mode.implied(exe::inx);
            case 0xF8 -> mode.implied(exe::sed);
            case 0x09 -> mode.immediate(exe::ora);
            case 0x0B, 0x2B -> mode.immediate(exe::anc);
            case 0x29 -> mode.immediate(exe::and);
            case 0x49 -> mode.immediate(exe::eor);
            case 0x4B -> mode.immediate(exe::alr);
            case 0x69 -> mode.immediate(exe::adc);
            case 0x6B -> mode.immediate(exe::arr);
            case 0x8B -> mode.immediate(exe::xaa);
            case 0xA0 -> mode.immediate(exe::ldy);
            case 0xA2 -> mode.immediate(exe::ldx);
            case 0xA9 -> mode.immediate(exe::lda);
            case 0xAB -> mode.immediate(exe::lax);
            case 0xC0 -> mode.immediate(exe::cpy);
            case 0xC9 -> mode.immediate(exe::cmp);
            case 0xCB -> mode.immediate(exe::axs);
            case 0xE0 -> mode.immediate(exe::cpx);
            case 0xE9, 0xEB -> mode.immediate(exe::sbc);
            case 0x0C -> mode.absolute_read(exe::nop);
            case 0x0D -> mode.absolute_read(exe::ora);
            case 0x2C -> mode.absolute_read(exe::bit);
            case 0x2D -> mode.absolute_read(exe::and);
            case 0x4D -> mode.absolute_read(exe::eor);
            case 0x6D -> mode.absolute_read(exe::adc);
            case 0xAC -> mode.absolute_read(exe::ldy);
            case 0xAD -> mode.absolute_read(exe::lda);
            case 0xAE -> mode.absolute_read(exe::ldx);
            case 0xAF -> mode.absolute_read(exe::lax);
            case 0xCC -> mode.absolute_read(exe::cpy);
            case 0xCD -> mode.absolute_read(exe::cmp);
            case 0xEC -> mode.absolute_read(exe::cpx);
            case 0xED -> mode.absolute_read(exe::sbc);
            case 0x0E -> mode.absolute_modify(exe::asl);
            case 0x0F -> mode.absolute_modify(exe::slo);
            case 0x2E -> mode.absolute_modify(exe::rol);
            case 0x2F -> mode.absolute_modify(exe::rla);
            case 0x4F -> mode.absolute_modify(exe::sre);
            case 0x4E -> mode.absolute_modify(exe::lsr);
            case 0x6E -> mode.absolute_modify(exe::ror);
            case 0x6F -> mode.absolute_modify(exe::rra);
            case 0xCE -> mode.absolute_modify(exe::dec);
            case 0xCF -> mode.absolute_modify(exe::dcp);
            case 0xEE -> mode.absolute_modify(exe::inc);
            case 0xEF -> mode.absolute_modify(exe::isc);
            case 0x8C -> mode.absolute_write(exe::sty);
            case 0x8D -> mode.absolute_write(exe::sta);
            case 0x8E -> mode.absolute_write(exe::stx);
            case 0x8F -> mode.absolute_write(exe::sax);
            case 0x05 -> mode.zero_page_read(exe::ora);
            case 0x24 -> mode.zero_page_read(exe::bit);
            case 0x25 -> mode.zero_page_read(exe::and);
            case 0x45 -> mode.zero_page_read(exe::eor);
            case 0x65 -> mode.zero_page_read(exe::adc);
            case 0xA4 -> mode.zero_page_read(exe::ldy);
            case 0xA5 -> mode.zero_page_read(exe::lda);
            case 0xA6 -> mode.zero_page_read(exe::ldx);
            case 0xA7 -> mode.zero_page_read(exe::lax);
            case 0xC4 -> mode.zero_page_read(exe::cpy);
            case 0xC5 -> mode.zero_page_read(exe::cmp);
            case 0xE4 -> mode.zero_page_read(exe::cpx);
            case 0xE5 -> mode.zero_page_read(exe::sbc);
            case 0x06 -> mode.zero_page_modify(exe::asl);
            case 0x07 -> mode.zero_page_modify(exe::slo);
            case 0x26 -> mode.zero_page_modify(exe::rol);
            case 0x27 -> mode.zero_page_modify(exe::rla);
            case 0x46 -> mode.zero_page_modify(exe::lsr);
            case 0x47 -> mode.zero_page_modify(exe::sre);
            case 0x66 -> mode.zero_page_modify(exe::ror);
            case 0x67 -> mode.zero_page_modify(exe::rra);
            case 0xC6 -> mode.zero_page_modify(exe::dec);
            case 0xC7 -> mode.zero_page_modify(exe::dcp);
            case 0xE6 -> mode.zero_page_modify(exe::inc);
            case 0xE7 -> mode.zero_page_modify(exe::isc);
            case 0x84 -> mode.zero_page_write(exe::sty);
            case 0x85 -> mode.zero_page_write(exe::sta);
            case 0x86 -> mode.zero_page_write(exe::stx);
            case 0x87 -> mode.zero_page_write(exe::sax);
            case 0xB6 -> mode.zero_page_y_read(exe::ldx);
            case 0xB7 -> mode.zero_page_y_read(exe::lax);
            case 0x15 -> mode.zero_page_x_read(exe::ora);
            case 0x35 -> mode.zero_page_x_read(exe::and);
            case 0x55 -> mode.zero_page_x_read(exe::eor);
            case 0x75 -> mode.zero_page_x_read(exe::adc);
            case 0xB4 -> mode.zero_page_x_read(exe::ldy);
            case 0xB5 -> mode.zero_page_x_read(exe::lda);
            case 0xD5 -> mode.zero_page_x_read(exe::cmp);
            case 0xF5 -> mode.zero_page_x_read(exe::sbc);
            case 0x16 -> mode.zero_page_x_modify(exe::asl);
            case 0x17 -> mode.zero_page_x_modify(exe::slo);
            case 0x36 -> mode.zero_page_x_modify(exe::rol);
            case 0x37 -> mode.zero_page_x_modify(exe::rla);
            case 0x56 -> mode.zero_page_x_modify(exe::lsr);
            case 0x57 -> mode.zero_page_x_modify(exe::sre);
            case 0x76 -> mode.zero_page_x_modify(exe::ror);
            case 0x77 -> mode.zero_page_x_modify(exe::rra);
            case 0xD6 -> mode.zero_page_x_modify(exe::dec);
            case 0xD7 -> mode.zero_page_x_modify(exe::dcp);
            case 0xF6 -> mode.zero_page_x_modify(exe::inc);
            case 0xF7 -> mode.zero_page_x_modify(exe::isc);
            case 0x94 -> mode.zero_page_x_write(exe::sty);
            case 0x95 -> mode.zero_page_x_write(exe::sta);
            case 0x96 -> mode.zero_page_y_write(exe::stx);
            case 0x97 -> mode.zero_page_y_write(exe::sax);
            case 0x19 -> mode.absolute_y_read(exe::ora);
            case 0x39 -> mode.absolute_y_read(exe::and);
            case 0x59 -> mode.absolute_y_read(exe::eor);
            case 0x79 -> mode.absolute_y_read(exe::adc);
            case 0xB9 -> mode.absolute_y_read(exe::lda);
            case 0xBB -> mode.absolute_y_read(exe::las);
            case 0xBE -> mode.absolute_y_read(exe::ldx);
            case 0xBF -> mode.absolute_y_read(exe::lax);
            case 0xD9 -> mode.absolute_y_read(exe::cmp);
            case 0xF9 -> mode.absolute_y_read(exe::sbc);
            case 0x1D -> mode.absolute_x_read(exe::ora);
            case 0x3D -> mode.absolute_x_read(exe::and);
            case 0x5D -> mode.absolute_x_read(exe::eor);
            case 0x7D -> mode.absolute_x_read(exe::adc);
            case 0xBC -> mode.absolute_x_read(exe::ldy);
            case 0xBD -> mode.absolute_x_read(exe::lda);
            case 0xDD -> mode.absolute_x_read(exe::cmp);
            case 0xFD -> mode.absolute_x_read(exe::sbc);
            case 0x1B -> mode.absolute_y_modify(exe::slo);
            case 0x3B -> mode.absolute_y_modify(exe::rla);
            case 0x5B -> mode.absolute_y_modify(exe::sre);
            case 0x7B -> mode.absolute_y_modify(exe::rra);
            case 0xDB -> mode.absolute_y_modify(exe::dcp);
            case 0xFB -> mode.absolute_y_modify(exe::isc);
            case 0x1E -> mode.absolute_x_modify(exe::asl);
            case 0x1F -> mode.absolute_x_modify(exe::slo);
            case 0x3E -> mode.absolute_x_modify(exe::rol);
            case 0x3F -> mode.absolute_x_modify(exe::rla);
            case 0x5E -> mode.absolute_x_modify(exe::lsr);
            case 0x5F -> mode.absolute_x_modify(exe::sre);
            case 0x7E -> mode.absolute_x_modify(exe::ror);
            case 0x7F -> mode.absolute_x_modify(exe::rra);
            case 0xDE -> mode.absolute_x_modify(exe::dec);
            case 0xDF -> mode.absolute_x_modify(exe::dcp);
            case 0xFE -> mode.absolute_x_modify(exe::inc);
            case 0xFF -> mode.absolute_x_modify(exe::isc);
            case 0x9D -> mode.absolute_x_write(exe::sta);
            case 0x99 -> mode.absolute_y_write(exe::sta);
            case 0x01 -> mode.indirect_x_read(exe::ora);
            case 0x21 -> mode.indirect_x_read(exe::and);
            case 0x41 -> mode.indirect_x_read(exe::eor);
            case 0x61 -> mode.indirect_x_read(exe::adc);
            case 0xA1 -> mode.indirect_x_read(exe::lda);
            case 0xA3 -> mode.indirect_x_read(exe::lax);
            case 0xC1 -> mode.indirect_x_read(exe::cmp);
            case 0xE1 -> mode.indirect_x_read(exe::sbc);
            case 0x03 -> mode.indirect_x_modify(exe::slo);
            case 0x23 -> mode.indirect_x_modify(exe::rla);
            case 0x43 -> mode.indirect_x_modify(exe::sre);
            case 0x63 -> mode.indirect_x_modify(exe::rra);
            case 0xC3 -> mode.indirect_x_modify(exe::dcp);
            case 0xE3 -> mode.indirect_x_modify(exe::isc);
            case 0x81 -> mode.indirect_x_write(exe::sta);
            case 0x83 -> mode.indirect_x_write(exe::sax);
            case 0x11 -> mode.indirect_y_read(exe::ora);
            case 0x31 -> mode.indirect_y_read(exe::and);
            case 0x51 -> mode.indirect_y_read(exe::eor);
            case 0x71 -> mode.indirect_y_read(exe::adc);
            case 0xB1 -> mode.indirect_y_read(exe::lda);
            case 0xB3 -> mode.indirect_y_read(exe::lax);
            case 0xD1 -> mode.indirect_y_read(exe::cmp);
            case 0xF1 -> mode.indirect_y_read(exe::sbc);
            case 0x13 -> mode.indirect_y_modify(exe::slo);
            case 0x33 -> mode.indirect_y_modify(exe::rla);
            case 0x53 -> mode.indirect_y_modify(exe::sre);
            case 0x73 -> mode.indirect_y_modify(exe::rra);
            case 0xD3 -> mode.indirect_y_modify(exe::dcp);
            case 0xF3 -> mode.indirect_y_modify(exe::isc);
            case 0x91 -> mode.indirect_y_write(exe::sta);
            case 0x93 -> mode.indirect_y_write(exe::ahx);
            case 0x4C -> mode.absolute_write(exe::jmp);
            case 0x6C -> mode.indirect_jmp(exe::jmp);
            case 0x10 -> relative(exe::bmi);
            case 0x30 -> relative(exe::bpl);
            case 0x50 -> relative(exe::bvc);
            case 0x70 -> relative(exe::bvs);
            case 0x90 -> relative(exe::bcc);
            case 0xB0 -> relative(exe::bcs);
            case 0xD0 -> relative(exe::bne);
            case 0xF0 -> relative(exe::beq);
            case 0x9B -> tas();
            case 0x9C -> shy();
            case 0x9E -> shx();
            case 0x9F -> ahx();
            case 0x80, 0x82, 0x89, 0xC2, 0xE2 -> mode.immediate(exe::nop);
            case 0x1A, 0x3A, 0x5A, 0x7A, 0xDA, 0xEA, 0xFA -> mode.implied(exe::nop);
            case 0x04, 0x44, 0x64 -> mode.zero_page_read(exe::nop);
            case 0x14, 0x34, 0x54, 0x74, 0xD4, 0xF4 -> mode.zero_page_x_read(exe::nop);
            case 0x1C, 0x3C, 0x5C, 0x7C, 0xDC, 0xFC -> mode.absolute_x_read(exe::nop);
            case 0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72, 0x92, 0xB2, 0xD2, 0xF2 -> KIL();
        }

        if (running) {
            if (interrupt.triggerNMI()) {
                NMI();
                interrupt.nmiRequested(false);
            } else if (interrupt.triggerIRQ()) {
                IRQ();
            }
        }

        ++instructionsCounter;
    }

    public void reset() {
        interrupt.resetRequested(true);
    }

    public void setNMI(final boolean value) {
        interrupt.nmiRequested(value);
    }

    public void setMapperIrq(final boolean value) {
        interrupt.irqRequested(setBit(interrupt.irqRequested(), IRQ_MAPPER, value));
    }

    public boolean getMapperIrq() {
        return getBitBool(interrupt.irqRequested(), IRQ_MAPPER);
    }

    public void setApuIrq(final boolean value) {
        interrupt.irqRequested(setBit(interrupt.irqRequested(), IRQ_APU, value));
    }

    public boolean getApuIrq() {
        return getBitBool(interrupt.irqRequested(), IRQ_APU);
    }

    public void setDmcIrq(final boolean value) {
        interrupt.irqRequested(setBit(interrupt.irqRequested(), IRQ_DMC, value));
    }

    public boolean getDmcIrq() {
        return getBitBool(interrupt.irqRequested(), IRQ_DMC);
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
            read_u8(reg.pc());
        }
        read_u8(reg.pc());
        for (int i = 0; i < 256; i++) {
            write_u8(REG_OAM_DATA, read_u8(value | i));
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
        return interrupt.serviced();
    }

    // -- private methods --------------------------------------------------------

    private void handleCpuCycle() {
        interrupt.triggerNMI(interrupt.nmiRequested());
        interrupt.triggerIRQ(interrupt.irqRequested() != 0 && !reg.i());
        mapper.update();
        cycleCounter++;
        apu.update((cycleCounter & 1) == 1);
        ppu.update();
    }

    public void write_u8(final int address, int value) {
        if (dmcCycle > 0) {
            dmcCycle--;
        }
        handleCpuCycle();

        mapper.writeCpuMemory(address, value);
    }

    public int read_u8(final int address) {

        if (dmcCycle > 0) {
            int cycle = dmcCycle - 1;
            dmcCycle = 0;
            if (address == REG_INPUT_PORT_1 || address == REG_INPUT_PORT_2) {
                if (cycle-- > 0) {
                    read_u8(address);
                }
                while (cycle-- > 0) {
                    handleCpuCycle();
                }
            } else {
                while (cycle-- > 0) {
                    read_u8(address);
                }
            }
            dmc.fillSampleBuffer(read_u8(dmcAddress));
        }

        handleCpuCycle();

        return mapper.readCpuMemory(address);
    }

    public int read_u8_pc() {
        int value = read_u8(reg.pc());
        reg.pcInc1();
        return value;
    }

    public int read_u16(final int address) {
        int l = read_u8(address);
        int h = read_u8(toU16(address + 1));
        return l | h << 8;
    }

    public int read_u16_bug(final int address) {
        int l = read_u8(address);
        int h = read_u8((address & 0xFF00) | toU8(address + 1));
        return l | h << 8;
    }

    public int read_u16_pc() {
        int l = read_u8_pc();
        int h = read_u8_pc();
        return l | h << 8;
    }

    // -- Stack instructions -----------------------------------------------------

    private void rst() {
        reg.s(0xFF);
        read_u8(reg.pc()); // 1
        read_u8(reg.pc()); // 2
        stack.peek(); // 3
        reg.sDec1();
        stack.peek(); // 4
        reg.sDec1();
        stack.peek(); // 5  
        reg.sDec1();
        reg.i(true);
        reg.pc(read_u16(VEC_RESET)); // 6 7
        interrupt.serviced(ServicedType.RST);
    }

    private void interrupt(int address) {
        read_u8(reg.pc()); // 1
        read_u8(reg.pc()); // 2
        stack.push_u16(reg.pc()); // 3 4
        reg.r(true);
        stack.push_u8(reg.p()); // 5
        reg.i(true);
        reg.pc(read_u16(address)); // 6 7
    }

    private void NMI() {
        interrupt.serviced(ServicedType.NMI);
        interrupt(VEC_NMI);
    }

    private void IRQ() {
        interrupt.serviced(ServicedType.IRQ);
        interrupt(VEC_IRQ);
    }

    private void brk() {
        reg.pcInc1();
        stack.push_u16(reg.pc()); // 3 4
        reg.r(true);
        reg.b(true);
        stack.push_u8(reg.p()); // 5
        reg.i(true);
        int address;
        if (interrupt.nmiRequested()) {
            interrupt.nmiRequested(false);
            address = VEC_NMI;
            interrupt.serviced(ServicedType.NMI);
        } else {
            address = VEC_IRQ;
            interrupt.serviced(ServicedType.BRK);
        }
        reg.pc(read_u16(address)); // 6 7
    }

    private void KIL() {
        reg.pcDec1();
        if (running) {
            running = false;
            App.cpuKilled(mapper.readCpuMemory(reg.pc()), reg.pc());
        }
    }

    // -- Absolute indexed addressing instructions -------------------------------

    public final static int MEM_PAGE_SIZEOF = 0x100;

    public boolean checkPageCrossed(int address_old, int address_new) {
        return toU8((address_old + address_new) >>> 8) != toU8(address_old >>> 8);
    }

    private void _AhxShxShy(int bassAddr, int indexReg, int valueReg) {
        // https://forums.nesdev.org/viewtopic.php?p=297765
        boolean pageCrossed = checkPageCrossed(bassAddr, indexReg);
        // cycle = 4
        // 对齐指令执行周期的读取操作
        read_u8(bassAddr + indexReg - (pageCrossed ? MEM_PAGE_SIZEOF : 0));
        // 被DMA中断，处理CPU与PPU的DMC交互操作，多一个指令执行周期
        boolean hadDMA = dmcCycle > 0;
        int address = bassAddr + indexReg;
        int addrHigh = address >> 8;
        int addrLow = toU8(address);
        if (pageCrossed) addrHigh &= valueReg;
        int value = hadDMA ? valueReg : (valueReg & ((bassAddr >> 8) + 1));
        // cycle = 5
        write_u8((addrHigh << 8 | addrLow), value);
    }

    public void shy() {
        _AhxShxShy(read_u16_pc(), reg.x(), reg.y());
    }

    public void shx() {
        _AhxShxShy(read_u16_pc(), reg.y(), reg.x());
    }

    public void ahx() {
        _AhxShxShy(read_u16_pc(), reg.y(), reg.x() & reg.a());
    }

    public void tas() {
        ahx();
        reg.s(reg.x() & reg.a());
    }

    // -- Relative addressing instructions ---------------------------------------

    public void relative(Supplier<Boolean> op) {
        int offset = toI8(read_u8_pc());
        boolean branchTaken = op.get();
        if (branchTaken) {
            boolean clearNMI = false;
            boolean clearIRQ = false;
            if (interrupt.nmiRequested() && !interrupt.triggerNMI()) {
                clearNMI = true;
            }
            if (interrupt.irqRequested() != 0 && !interrupt.triggerIRQ()) {
                clearIRQ = true;
            }
            read_u8(reg.pc());
            if (clearNMI) {
                interrupt.triggerNMI(false);
            }
            if (clearIRQ) {
                interrupt.triggerIRQ(false);
            }
            int address = toU16(reg.pc() + offset);
            reg.pc((reg.pc() & 0xFF00) | (address & 0x00FF));
            if (checkPageCrossed(reg.pc(), address)) {
                read_u8(reg.pc());
                reg.pc(address);
            }
        }
    }

    public Register getRegister() {
        return reg;
    }

    public Stack getStack() {
        return stack;
    }
}