package nintaco.cpu;

import static nintaco.util.ByteUtil.toU8;

// https://www.nesdev.org/wiki/Stack
public class Stack {

    private final CPU cpu;
    private final Register reg;

    public Stack(CPU cpu) {
        this.cpu = cpu;
        this.reg = cpu.register();
    }

    public int peek() {
        return cpu.read_u8(VEC_STACK_START | reg.s());
    }

    public void poke(int value) {
        cpu.write_u8(VEC_STACK_START | reg.s(), value);
    }

    public int pull_u8() {
        reg.sInc1();
        return peek();
    }

    public void push_u8(int value) {
        poke(value);
        reg.sDec1();
    }

    public int pull_u16() {
        int l = pull_u8();
        int h = pull_u8();
        return l | h << 8;
    }

    public void push_u16(int value) {
        push_u8(value >> 8);
        push_u8(toU8(value));
    }

    public static final int VEC_STACK_START = 0x0100;
}
