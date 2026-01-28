package nintaco.cpu;


import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static nintaco.util.ByteUtil.*;

public class AddressMode {

    private final CPU cpu;
    private final Register reg;
    private final Interrupt interrupt;
    private final State state;

    public AddressMode(CPU cpu) {
        this.cpu = cpu;
        this.reg = cpu.register();
        this.interrupt = cpu.interrupt();
        this.state = cpu.state();
    }

    public void absolute_read(Consumer<Integer> op) {
        op.accept(cpu.read_u8(cpu.read_u16_pc()));
    }

    public void absolute_modify(Function<Integer, Integer> op) {
        int address = cpu.read_u16_pc();
        int value = cpu.read_u8(address);
        cpu.write_u8(address, value);
        value = op.apply(value);
        cpu.write_u8(address, value);
    }

    public void absolute_write(Consumer<Integer> op) {
        op.accept(cpu.read_u16_pc());
    }

    private void absolute_read(Consumer<Integer> op, int offset) {
        int address_old = cpu.read_u16_pc();
        int address_new = toU16(address_old + offset);
        int value = cpu.read_u8(address_new);
        if (checkPageCrossed(address_old, address_new)) {
            cpu.read_u8(address_new);
        }
        op.accept(value);
    }

    public void absolute_x_read(Consumer<Integer> op) {
        absolute_read(op, reg.x());
    }

    public void absolute_y_read(Consumer<Integer> op) {
        absolute_read(op, reg.y());
    }

    private void absolute_modify(Function<Integer, Integer> op, int offset) {
        int address_old = cpu.read_u16_pc();
        int address_new = toU16(address_old + offset);
        cpu.read_u8((address_old & 0xFF00) | (address_new & 0x00FF));
        address_old = cpu.read_u8(address_new);
        cpu.write_u8(address_new, address_old);
        address_old = op.apply(address_old);
        cpu.write_u8(address_new, address_old);
    }

    public void absolute_x_modify(Function<Integer, Integer> op) {
        absolute_modify(op, reg.x());
    }

    public void absolute_y_modify(Function<Integer, Integer> op) {
        absolute_modify(op, reg.y());
    }

    private void absolute_write(Consumer<Integer> op, int offset) {
        int address_old = cpu.read_u16_pc();
        int address_new = toU16(address_old + offset);
        cpu.read_u8((address_old & 0xFF00) | (address_new & 0x00FF));
        op.accept(address_new);
    }

    public void absolute_x_write(Consumer<Integer> op) {
        absolute_write(op, reg.x());
    }

    public void absolute_y_write(Consumer<Integer> op) {
        absolute_write(op, reg.y());
    }

    public void zero_page_read(Consumer<Integer> op) {
        op.accept(cpu.read_u8(cpu.read_u8_pc()));
    }

    public void zero_page_modify(Function<Integer, Integer> op) {
        int address = cpu.read_u8_pc();
        int value = cpu.read_u8(address);
        cpu.write_u8(address, value);
        value = op.apply(value);
        cpu.write_u8(address, value);
    }

    public void zero_page_write(Consumer<Integer> op) {
        op.accept(cpu.read_u8_pc());
    }

    private void zero_page_read(Consumer<Integer> op, int offset) {
        int address = cpu.read_u8_pc();
        cpu.read_u8(address);
        op.accept(cpu.read_u8(toU8(address + offset)));
    }

    public void zero_page_x_read(Consumer<Integer> op) {
        zero_page_read(op, reg.x());
    }

    public void zero_page_y_read(Consumer<Integer> op) {
        zero_page_read(op, reg.y());
    }

    public void zero_page_x_modify(Function<Integer, Integer> op) {
        int address = cpu.read_u8_pc();
        cpu.read_u8(address);
        address = toU8(address + reg.x());
        int value = cpu.read_u8(address);
        cpu.write_u8(address, value);
        value = op.apply(value);
        cpu.write_u8(address, value);
    }

    private void zero_page_write(Consumer<Integer> op, int offset) {
        int address = cpu.read_u8_pc();
        cpu.read_u8(address);
        op.accept(toU8(address + offset));
    }

    public void zero_page_x_write(Consumer<Integer> op) {
        zero_page_write(op, reg.x());
    }

    public void zero_page_y_write(Consumer<Integer> op) {
        zero_page_write(op, reg.y());
    }

    public void indirect_jmp(Consumer<Integer> op) {
        op.accept(cpu.read_u16_bug(cpu.read_u16_pc()));
    }

    public void indirect_x_read(Consumer<Integer> op) {
        int address = cpu.read_u8_pc();
        cpu.read_u8(address);
        op.accept(cpu.read_u8(cpu.read_u16_bug(toU8(address + reg.x()))));
    }

    public void indirect_x_modify(Function<Integer, Integer> op) {
        int value = cpu.read_u8_pc();
        cpu.read_u8(value);
        int address = cpu.read_u16_bug(toU8(value + reg.x()));
        value = cpu.read_u8(address);
        cpu.write_u8(address, value);
        value = op.apply(value);
        cpu.write_u8(address, value);
    }

    public void indirect_x_write(Consumer<Integer> op) {
        int address = cpu.read_u8_pc();
        cpu.read_u8(address);
        op.accept(cpu.read_u16_bug(toU8(address + reg.x())));
    }

    public void indirect_y_read(Consumer<Integer> op) {
        int address_old = cpu.read_u8_pc();
        int address_new = cpu.read_u16_bug(address_old);
        address_old = toU16(address_new + reg.y());
        address_new = (address_new & 0xFF00) | (address_old & 0x00FF);
        int value = cpu.read_u8(address_new);
        if (checkPageCrossed(address_old, address_new)) {
            value = cpu.read_u8(address_old);
        }
        op.accept(value);
    }

    public void indirect_y_modify(Function<Integer, Integer> op) {
        int address = cpu.read_u8_pc();
        int value = cpu.read_u16_bug(address);
        address = toU16(value + reg.y());
        value = (value & 0xFF00) | (address & 0x00FF);
        cpu.read_u8(value);
        value = cpu.read_u8(address);
        cpu.write_u8(address, value);
        value = op.apply(value);
        cpu.write_u8(address, value);
    }

    public void indirect_y_write(Consumer<Integer> op) {
        int address = cpu.read_u8_pc();
        int value = cpu.read_u16_bug(address);
        address = toU16(value + reg.y());
        value = (value & 0xFF00) | (address & 0x00FF);
        cpu.read_u8(value);
        op.accept(address);
    }

    public void immediate(Consumer<Integer> op) {
        op.accept(cpu.read_u8_pc());
    }

    public void implied_accumulator(Function<Integer, Integer> op) {
        cpu.read_u8(reg.pc());
        reg.a(op.apply(reg.a()));
    }

    public void implied(Runnable op) {
        cpu.read_u8(reg.pc());
        op.run();
    }

    public void relative(Supplier<Boolean> op) {
        int offset = toI8(cpu.read_u8_pc());
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
            cpu.read_u8(reg.pc());
            if (clearNMI) {
                interrupt.triggerNMI(false);
            }
            if (clearIRQ) {
                interrupt.triggerIRQ(false);
            }
            int address = toU16(reg.pc() + offset);
            reg.pc((reg.pc() & 0xFF00) | (address & 0x00FF));
            if (checkPageCrossed(reg.pc(), address)) {
                cpu.read_u8(reg.pc());
                reg.pc(address);
            }
        }
    }

    // https://forums.nesdev.org/viewtopic.php?p=297765
    private void _tas_ahx_shx_shy(int indexReg, int valueReg) {
        int bassAddr = cpu.read_u16_pc();
        int address = toU16(bassAddr + indexReg);
        boolean pageCrossed = checkPageCrossed(bassAddr, address);
        boolean hadDMA = state.hasDmcCycle();
        cpu.read_u8(toU16(address - (pageCrossed ? 0x0100 : 0)));
        if (pageCrossed) address &= (valueReg << 8) | toU8(address);
        int value = hadDMA ? valueReg : toU8(valueReg & ((bassAddr >> 8) + 1));
        cpu.write_u8(address, value);
    }

    public void absolute_x_shy(Supplier<Integer> op) {
        _tas_ahx_shx_shy(reg.x(), op.get());
    }

    public void absolute_y_shx(Supplier<Integer> op) {
        _tas_ahx_shx_shy(reg.y(), op.get());
    }

    public boolean checkPageCrossed(int address_old, int address_new) {
        return (address_old & 0xFF00) != (address_new & 0xFF00);
    }
}
