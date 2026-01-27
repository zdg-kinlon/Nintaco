package cn.kinlon.emu.cpu;

import cn.kinlon.emu.App;
import cn.kinlon.emu.ServicedType;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.ServicedType.*;

public class Interrupt {
    public static final int VEC_NMI = 0xfffa;
    public static final int VEC_RESET = 0xfffc;
    public static final int VEC_IRQ = 0xfffe;

    private final CPU cpu;
    private final Mapper mapper;
    private final Register reg;
    private final Stack stack;
    private final State state;

    public Interrupt(CPU cpu) {
        this.cpu = cpu;
        this.mapper = cpu.getMapper();
        this.reg = cpu.getRegister();
        this.stack = cpu.getStack();
        this.state = cpu.getState();
    }

    public static final int IRQ_MAPPER = 0;
    public static final int IRQ_APU = 1;
    public static final int IRQ_DMC = 2;

    private boolean resetRequested;
    private boolean nmiRequested;
    private int irqRequested;
    private boolean triggerNMI;
    private boolean triggerIRQ;
    private ServicedType serviced = NMI;

    public void rst() {
        //reg.s(0xFF);
        cpu.read_u8(reg.pc());
        cpu.read_u8(reg.pc());
        stack.peek();
        reg.sDec1();
        stack.peek();
        reg.sDec1();
        stack.peek();
        reg.sDec1();
        reg.i(true);
        reg.pc(cpu.read_u16(VEC_RESET));
        serviced = RST;
    }

    public void _irq_nmi(int address) {
        cpu.read_u8(reg.pc());
        cpu.read_u8(reg.pc());
        stack.push_u16(reg.pc());
        reg.r(true);
        stack.push_u8(reg.p());
        reg.i(true);
        reg.pc(cpu.read_u16(address));
    }

    public void nmi() {
        serviced = (NMI);
        _irq_nmi(VEC_NMI);
    }

    public void irq() {
        serviced = IRQ;
        _irq_nmi(VEC_IRQ);
    }

    public void brk() {
        reg.pcInc1();
        stack.push_u16(reg.pc());
        reg.r(true);
        reg.b(true);
        stack.push_u8(reg.p());
        reg.i(true);
        int address;
        if (nmiRequested()) {
            nmiRequested = false;
            address = VEC_NMI;
            serviced = NMI;
        } else {
            address = VEC_IRQ;
            serviced = BRK;
        }
        reg.pc(cpu.read_u16(address));
    }


    public void kil() {
        reg.pcDec1();
        if (state.running()) {
            state.running(false);
            App.cpuKilled(mapper.readCpuMemory(reg.pc()), reg.pc());
        }
    }

    public void resetRequested(boolean resetRequested) {
        this.resetRequested = resetRequested;
    }

    public void nmiRequested(boolean nmiRequested) {
        this.nmiRequested = nmiRequested;
    }

    public void irqRequested(int irqRequested) {
        this.irqRequested = irqRequested;
    }

    public void triggerNMI(boolean triggerNMI) {
        this.triggerNMI = triggerNMI;
    }

    public void triggerIRQ(boolean triggerIRQ) {
        this.triggerIRQ = triggerIRQ;
    }

    public void serviced(ServicedType serviced) {
        this.serviced = serviced;
    }

    public boolean resetRequested() {
        return resetRequested;
    }

    public boolean nmiRequested() {
        return nmiRequested;
    }

    public int irqRequested() {
        return irqRequested;
    }

    public boolean triggerNMI() {
        return triggerNMI;
    }

    public boolean triggerIRQ() {
        return triggerIRQ;
    }

    public ServicedType serviced() {
        return serviced;
    }
}
