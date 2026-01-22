package cn.kinlon.emu.cpu;

import java.util.function.Consumer;

import static cn.kinlon.emu.utils.BitUtil.*;
import static cn.kinlon.emu.utils.ByteUtil.toU8;

public class InstructionExecutor {

    private final CPU cpu;
    private final Register reg;
    private final Stack stack;

    public InstructionExecutor(CPU cpu) {
        this.cpu = cpu;
        this.reg = cpu.getRegister();
        this.stack = cpu.getStack();
    }

    // -- Official 6502 Instructions ------------------------------------------------
    // https://www.nesdev.org/wiki/Instruction_reference

    /// No Operation
    public void nop(int value) {
    }

    /// No Operation
    public void nop() {
    }

    /// Set Carry
    public void sec() {
        reg.c(true);
    }

    /// Clear Carry
    public void clc() {
        reg.c(false);
    }

    /// Set Interrupt Disable
    public void sei() {
        reg.i(true);
    }

    /// Clear Interrupt Disable
    public void cli() {
        reg.i(false);
    }

    /// Set Decimal
    public void sed() {
        reg.d(true);
    }

    /// Clear Decimal
    public void cld() {
        reg.d(false);
    }

    /// Clear Overflow
    public void clv() {
        reg.v(false);
    }

    /// Branch if Carry Clear
    public boolean bcc() {
        return !reg.c();
    }

    /// Branch if Carry Set
    public boolean bcs() {
        return reg.c();
    }

    /// Branch if Equal
    public boolean beq() {
        return reg.z();
    }

    /// Branch if Not Equal
    public boolean bne() {
        return !reg.z();
    }

    /// Branch if Plus
    public boolean bpl() {
        return reg.n();
    }

    /// Branch if Minus
    public boolean bmi() {
        return !reg.n();
    }

    /// Branch if Overflow Clear
    public boolean bvc() {
        return !reg.v();
    }

    /// Branch if Overflow Set
    public boolean bvs() {
        return reg.v();
    }

    private void set_nz(int value) {
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
    }

    private void set_nz_reg(int value, Consumer<Integer> reg) {
        reg.accept(value);
        set_nz(value);
    }

    /// Transfer A to X
    public void tax() {
        set_nz_reg(reg.a(), reg::x);
    }

    /// Transfer X to A
    public void txa() {
        set_nz_reg(reg.x(), reg::a);
    }

    /// Transfer A to Y
    public void tay() {
        set_nz_reg(reg.a(), reg::y);
    }

    /// Transfer Y to A
    public void tya() {
        set_nz_reg(reg.y(), reg::a);
    }

    /// Transfer X to Stack Pointer
    public void txs() {
        reg.s(reg.x());
    }

    /// Transfer Stack Pointer to X
    public void tsx() {
        set_nz_reg(reg.s(), reg::x);
    }

    /// Push A
    public void pha() {
        stack.push_u8(reg.a());
    }

    /// Pull A
    public void pla() {
        stack.peek();
        set_nz_reg(stack.pull_u8(), reg::a);
    }

    /// Push Processor Status
    public void php() {
        reg.b(true);
        reg.r(true);
        stack.push_u8(reg.p());
    }

    /// Pull Processor Status
    public void plp() {
        stack.peek();
        boolean _r = reg.r();
        boolean _b = reg.b();
        reg.p(stack.pull_u8());
        reg.r(_r);
        reg.b(_b);
    }

    private void compare(int value1, int value2) {
        reg.c(value1 >= value2);
        set_nz(value1 - value2);
    }

    /// Compare A
    public void cmp(int value) {
        compare(reg.a(), value);
    }

    /// Compare X
    public void cpx(int value) {
        compare(reg.x(), value);
    }

    /// Compare Y
    public void cpy(int value) {
        compare(reg.y(), value);
    }

    private int shift(int value, boolean c) {
        reg.c(c);
        set_nz(value = toU8(value));
        return value;
    }

    /// Arithmetic Shift Left
    public int asl(final int value) {
        return shift(value << 1, getBitBool(value, 7));
    }

    /// Logical Shift Right
    public int lsr(int value) {
        return shift(value >>> 1, getBitBool(value, 0));
    }

    /// Rotate Left
    public int rol(int value) {
        return shift((value << 1) | toBit(reg.c()), getBitBool(value, 7));
    }

    /// Rotate Right
    public int ror(int value) {
        return shift((value >>> 1) | toBit(reg.c()) << 7, getBitBool(value, 0));
    }

    private void bitwise(int value) {
        reg.a(value);
        set_nz(reg.a());
    }

    /// Bitwise AND
    public void and(int value) {
        bitwise(reg.a() & value);
    }

    /// Bitwise OR
    public void ora(int value) {
        bitwise(reg.a() | value);
    }

    /// Bitwise Exclusive OR
    public void eor(int value) {
        bitwise(reg.a() ^ value);
    }

    /// Bit Test
    public void bit(int value) {
        reg.v(getBitBool(value, 6));
        reg.n(getBitBool(value, 7));
        reg.z(!toBitBool(value & reg.a()));
    }

    /// Add with Carry
    public void adc(int value) {
        int _i = reg.a() + value + toBit(reg.c());
        reg.v(getBitBool((reg.a() ^ _i) & (value ^ _i), 7));
        reg.c(getBitBool(_i, 8));
        set_nz_reg(toU8(_i), reg::a);
    }

    /// Subtract with Carry
    public void sbc(int value) {
        adc(toU8(~value));
    }

    public int increment(int value, int add) {
        set_nz(value = toU8(value + add));
        return value;
    }

    /// Increment Memory
    public int inc(int value) {
        return increment(value, 1);
    }

    /// Decrement Memory
    public int dec(int value) {
        return increment(value, -1);
    }

    /// Increment X
    public void inx() {
        reg.x(inc(reg.x()));
    }

    /// Decrement X
    public void dex() {
        reg.x(dec(reg.x()));
    }

    /// Increment Y
    public void iny() {
        reg.y(inc(reg.y()));
    }

    /// Decrement Y
    public void dey() {
        reg.y(dec(reg.y()));
    }

    /// Load A
    public void lda(int value) {
        set_nz_reg(value, reg::a);
    }

    /// Store A
    public void sta(int value) {
        cpu.write_u8(value, reg.a());
    }

    /// Load X
    public void ldx(int value) {
        set_nz_reg(value, reg::x);
    }

    /// Store X
    public void stx(int value) {
        cpu.write_u8(value, reg.x());
    }

    /// Load Y
    public void ldy(int value) {
        set_nz_reg(value, reg::y);
    }

    /// Store Y
    public void sty(int value) {
        cpu.write_u8(value, reg.y());
    }

    /// Jump
    public void jmp(int value) {
        reg.pc(value);
    }

    /// Return from Interrupt
    public void rti() {
        stack.peek();
        boolean _r = reg.r();
        boolean _b = reg.b();
        reg.p(stack.pull_u8());
        reg.r(_r);
        reg.b(_b);
        reg.pc((stack.pull_u16()));
    }

    /// Return from Subroutine
    public void rts() {
        stack.peek();
        reg.pc(stack.pull_u16());
        cpu.read_u8(reg.pc());
        reg.pcInc1();
    }

    /// Jump to Subroutine
    public void jsr(int value) {
        stack.peek();
        stack.push_u16(reg.pc());
        reg.pc(value | cpu.read_u8_pc() << 8);
    }

    // -- Unofficial 6502 Instructions ------------------------------------------------
    // https://www.nesdev.org/wiki/CPU_unofficial_opcodes
    // https://www.oxyron.de/html/opcodes02.html

    public void lax(int value) {
        lda(value);
        tax();
    }

    public void las(int value) {
        reg.a(value & reg.s());
        reg.s(reg.a());
        tax();
    }

    public void sax(int value) {
        cpu.write_u8(value, reg.a() & reg.x());
    }

    public void ahx(int value) {
        cpu.write_u8(value, reg.a() & reg.x() & ((value >> 8) + 1));
    }

    public int isc(int value) {
        sbc(value = inc(value));
        return value;
    }

    public int dcp(int value) {
        cmp(value = dec(value));
        return value;
    }

    public int rra(int value) {
        adc(value = ror(value));
        return value;
    }

    public int sre(int value) {
        eor(value = lsr(value));
        return value;
    }

    public int slo(int value) {
        ora(value = asl(value));
        return value;
    }

    public int rla(int value) {
        and(value = rol(value));
        return value;
    }

    public void anc(int value) {
        and(value);
        reg.c(getBitBool(reg.a(), 7));
    }

    public void alr(int value) {
        reg.a(lsr(reg.a() & value));
    }

    public void arr(int value) {
        reg.a(ror(reg.a() & value));
        reg.c(getBitBool(reg.a(), 6));
        reg.v(reg.c() ^ getBitBool(reg.a(), 5));
    }

    public void axs(int value) {
        int _ax = reg.a() & reg.x();
        reg.x(toU8(_ax - value));
        reg.c(_ax >= value);
        set_nz(reg.x());
    }

    public void xaa(int value) {
        reg.a(reg.a() & reg.x() & value);
        set_nz(reg.a());
        reg.c(reg.n());
    }
}