package cn.kinlon.emu.cpu;

import java.util.function.Consumer;
import java.util.function.Function;

import static cn.kinlon.emu.utils.ByteUtil.U16_HIGH_MASK;
import static cn.kinlon.emu.utils.ByteUtil.U16_LOW_MASK;

public class AddressingMode {

    private final CPU cpu;
    private final Register reg;

    public AddressingMode(CPU cpu) {
        this.cpu = cpu;
        this.reg = cpu.getRegister();
    }

    // https://www.nesdev.org/wiki/CPU_addressing_modes


    public void absolute_jump(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address |= (cpu.read(reg.pc()) << 8);
        op.accept(address);
    }

    public void absolute_indirect_jump(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address |= cpu.read(reg.pc()) << 8;
        reg.pcInc1();
        // cycle = 4
        op.accept((reg.pc() & U16_HIGH_MASK) | cpu.read(address));
        address = (address & U16_HIGH_MASK) | ((address + 1) & U16_LOW_MASK);
        // cycle = 5
        op.accept((cpu.read(address) << 8) | (reg.pc() & U16_LOW_MASK));
    }

    public void absolute_jsr(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        cpu.readStack();
        // cycle = 4
        address |= (cpu.read(reg.pc()) << 8);
        // cycle = 5 6
        op.accept(address);
    }

    public void absolute_read(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address |= (cpu.read(reg.pc()) << 8);
        reg.pcInc1();
        // cycle = 4
        address = cpu.read(address);
        op.accept(address);
    }

    public void absolute_modify(Function<Integer, Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address |= (cpu.read(reg.pc()) << 8);
        reg.pcInc1();
        // cycle = 4
        int value = cpu.read(address);
        // cycle = 5
        cpu.write(address, value);
        value = op.apply(value);
        // cycle = 6
        cpu.write(address, value);
    }

    public void absolute_write(Consumer<Integer> opcode) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address |= (cpu.read(reg.pc()) << 8);
        reg.pcInc1();
        // cycle = 4
        opcode.accept(address);
    }
}
