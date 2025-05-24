package cn.kinlon.emu.cpu;

import java.util.function.Consumer;
import java.util.function.Function;

import static cn.kinlon.emu.utils.ByteUtil.*;

public class AddressingMode {

    private final CPU cpu;
    private final Register reg;

    public AddressingMode(CPU cpu) {
        this.cpu = cpu;
        this.reg = cpu.getRegister();
    }

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
        address |= (cpu.read(reg.pc()) << 8);
        // cycle = 4
        cpu.readStack();
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

    public void absolute_write(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address |= (cpu.read(reg.pc()) << 8);
        reg.pcInc1();
        // cycle = 4
        op.accept(address);
    }

    public void immediate(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        op.accept(address);
    }

    public void accumulator(Function<Integer, Integer> op) {
        // cycle = 2
        cpu.read(reg.pc());
        reg.a(op.apply(reg.a()));
    }

    public void implied(Runnable op) {
        // cycle = 2
        cpu.read(reg.pc());
        op.run();
    }

    public void zero_page_read(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        int value = cpu.read(address);
        op.accept(value);
    }

    public void zero_page_modify(Function<Integer, Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        int value = cpu.read(address);
        // cycle = 4
        cpu.write(address, value);
        value = op.apply(value);
        // cycle = 5
        cpu.write(address, value);
    }

    public void zero_page_write(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        op.accept(address);
    }

    private void absolute_indexed_read(Consumer<Integer> op, int offset) {
        // cycle = 2
        int address_old = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address_old |= cpu.read(reg.pc()) << 8;
        reg.pcInc1();
        int address_new = toU16(address_old + offset);
        address_old = (address_old & U16_HIGH_MASK) | (address_new & U16_LOW_MASK);
        // cycle = 4
        int value = cpu.read(address_old);
        if (address_old != address_new) {
            // cycle = 5 : page crossed
            value = cpu.read(address_new);
        }
        op.accept(value);
    }

    public void absolute_indexed_x_read(Consumer<Integer> op) {
        absolute_indexed_read(op, reg.x());
    }

    public void absolute_indexed_y_read(Consumer<Integer> op) {
        absolute_indexed_read(op, reg.y());
    }

    private void absolute_indexed_modify(Function<Integer, Integer> op, int offset) {
        // cycle = 2
        int address_old = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        address_old |= cpu.read(reg.pc()) << 8;
        reg.pcInc1();
        int address_new = toU16(address_old + offset);
        // cycle = 4
        cpu.read((address_old & 0xFF00) | (address_new & 0x00FF));
        address_new = (address_new & U16_HIGH_MASK) | (address_new & U16_LOW_MASK);
        // cycle = 5
        address_old = cpu.read(address_new);
        // cycle = 6
        cpu.write(address_new, address_old);
        address_old = op.apply(address_old);
        // cycle = 7
        cpu.write(address_new, address_old);
    }

    public void absolute_indexed_x_modify(Function<Integer, Integer> op) {
        absolute_indexed_modify(op, reg.x());
    }

    public void absolute_indexed_y_modify(Function<Integer, Integer> op) {
        absolute_indexed_modify(op, reg.y());
    }

    private void absolute_indexed_write(Function<Integer, Integer> op, int offset) {
        // cycle = 2
        int value = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        int high = cpu.read(reg.pc());
        reg.pcInc1();
        value |= high << 8;
        int address = toU16(value + offset);
        value = (value & 0xFF00) | (address & 0x00FF);
        // cycle = 4
        cpu.read(value);
        op.apply(value);
    }

    public void absolute_indexed_x_write(Function<Integer, Integer> op) {
        absolute_indexed_write(op, reg.x());
    }

    public void absolute_indexed_y_write(Function<Integer, Integer> op) {
        absolute_indexed_write(op, reg.y());
    }

    public void indexed_indirect_write(Consumer<Integer> op) {
        // cycle = 2
        int address_old = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        cpu.read(address_old);
        address_old = toU8(address_old + reg.x());
        // cycle = 4
        int address_new = cpu.read(address_old);
        address_old = toU8(address_old + 1);
        // cycle = 5
        address_new |= cpu.read(address_old) << 8;
        // cycle = 6
        op.accept(address_new);
    }

    private void zero_page_indexed_read(Consumer<Integer> op, int offset) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        cpu.read(address);
        address = toU8(address + offset);
        // cycle = 4
        int value = cpu.read(address);
        op.accept(value);
    }

    public void zero_page_indexed_x_read(Consumer<Integer> op) {
        zero_page_indexed_read(op, reg.x());
    }

    public void zero_page_indexed_y_read(Consumer<Integer> op) {
        zero_page_indexed_read(op, reg.y());
    }

    public void zero_page_indexed_x_modify(Function<Integer, Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        cpu.read(address);
        address = toU8(address + reg.x());
        // cycle = 4
        int value = cpu.read(address);
        // cycle = 5
        cpu.write(address, value);
        value = op.apply(value);
        // cycle = 5
        cpu.write(address, value);
    }

    private void zero_page_indexed_write(Consumer<Integer> op, int offset) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        cpu.read(address);
        address = toU8(address + offset);
        // cycle = 4
        op.accept(address);
    }

    public void zero_page_indexed_x_write(Consumer<Integer> op) {
        zero_page_indexed_write(op, reg.x());
    }

    public void zero_page_indexed_y_write(Consumer<Integer> op) {
        zero_page_indexed_write(op, reg.y());
    }

    public void indexed_indirect_read(Consumer<Integer> op) {
        // 2
        int address_old = cpu.read(reg.pc());
        reg.pcInc1();
        // 3
        cpu.read(address_old);
        address_old = toU8(address_old + reg.x());
        // 4
        int address_new = cpu.read(address_old);
        address_old = toU8(address_old + 1);
        // 5
        address_new |= cpu.read(address_old) << 8;
        // 6
        int value = cpu.read(address_new);
        op.accept(value);
    }

    public void indexed_indirect_modify(Function<Integer, Integer> op) {
        // 2
        int value = cpu.read(reg.pc());
        reg.pcInc1();
        // 3
        cpu.read(value);
        value = toU8(value + reg.x());
        // 4
        int address = cpu.read(value);
        value = toU8(value + 1);
        // 5
        address |= cpu.read(value) << 8;
        // 6
        value = cpu.read(address);
        // 7
        cpu.write(address, value);
        value = op.apply(value);
        // 8
        cpu.write(address, value);
    }

    public void indirect_indexed_read(Consumer<Integer> op) {
        // cycle = 2
        int address_old = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        int address_new = cpu.read(address_old);
        address_old = toU8(++address_old);
        // cycle = 4
        address_new |= cpu.read(address_old) << 8;
        address_old = toU16(address_new + reg.y());
        address_new = (address_new & 0xFF00) | (address_old & 0x00FF);
        // cycle = 5
        int value = cpu.read(address_new);
        if (address_new != address_old) {
            // cycle = 6
            value = cpu.read(address_old);
        }
        op.accept(value);
    }

    public void indirect_indexed_modify(Function<Integer, Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        int value = cpu.read(address);
        address = toU8(++address);
        // cycle = 4
        value |= cpu.read(address) << 8;
        address = toU16(value + reg.y());
        value = (value & 0xFF00) | (address & 0x00FF);
        // cycle = 5
        cpu.read(value);
        // cycle = 6
        value = cpu.read(address);
        // cycle = 7
        cpu.write(address, value);
        value = op.apply(value);
        // cycle = 8
        cpu.write(address, value);
    }

    public void indirect_indexed_write(Consumer<Integer> op) {
        // cycle = 2
        int address = cpu.read(reg.pc());
        reg.pcInc1();
        // cycle = 3
        int value = cpu.read(address);
        address = toU8(++address);
        // cycle = 4
        value |= cpu.read(address) << 8;
        address = toU16(value + reg.y());
        value = (value & 0xFF00) | (address & 0x00FF);
        // cycle = 5
        cpu.read(value);
        // cycle = 6
        op.accept(address);
    }
}
