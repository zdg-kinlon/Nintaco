package cn.kinlon.emu.cpu;

import static cn.kinlon.emu.utils.BitUtil.*;
import static cn.kinlon.emu.utils.ByteUtil.toU8;

public class ALU {

    private final CPU cpu;
    private final Register reg;

    public ALU(CPU cpu) {
        this.cpu = cpu;
        this.reg = cpu.getRegister();
    }

    // -- Official 6502 Instructions ------------------------------------------------
    // https://www.nesdev.org/wiki/Instruction_reference

    /// No Operation
    public void nop(int value) {
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

    /// Transfer A to X
    public void tax() {
        reg.x(reg.a());
        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    /// Transfer X to A
    public void txa() {
        reg.a(reg.x());
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Transfer A to Y
    public void tay() {
        reg.y(reg.a());
        reg.n(getBitBool(reg.y(), 7));
        reg.z(reg.y() == 0);
    }

    /// Transfer Y to A
    public void tya() {
        reg.a(reg.y());
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Transfer X to Stack Pointer
    public void txs() {
        reg.sp(reg.x());
    }

    /// Transfer Stack Pointer to X
    public void tsx() {
        reg.x(reg.sp());
        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    /// Push A
    public void pha() {
        cpu.push(reg.a());
    }

    /// Pull A
    public void pla() {
        reg.a(cpu.pull());
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Push Processor Status
    public void php() {
        boolean _b = reg.b();
        reg.r(true);
        reg.b(true);
        cpu.push(reg.p());
        reg.b(_b);
    }

    /// Pull Processor Status
    public void plp() {
        boolean _r = reg.r();
        boolean _b = reg.b();
        reg.p(cpu.pull());
        reg.r(_r);
        reg.b(_b);
    }

    /// Compare A
    public void cmp(int value) {
        reg.c(reg.a() >= value);
        value = reg.a() - value;
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
    }

    /// Compare X
    public void cpx(int value) {
        reg.c(reg.x() >= value);
        value = reg.x() - value;
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
    }

    /// Compare Y
    public void cpy(int value) {
        reg.c(reg.y() >= value);
        value = reg.y() - value;
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
    }

    /// Arithmetic Shift Left
    public int asl(int value) {
        reg.c(getBitBool(value, 7));
        value <<= 1;
        value = toU8(value);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    /// Logical Shift Right
    public int lsr(int value) {
        reg.c(getBitBool(value, 0));
        value >>>= 1;
        value = toU8(value);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    /// Rotate Left
    public int rol(int value) {
        boolean _c = getBitBool(value, 7);
        value = toU8(value << 1) | (toBit(reg.c()));
        reg.c(_c);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    /// Rotate Right
    public int ror(int value) {
        boolean _c = getBitBool(value, 0);
        value = toU8(value >>> 1) | (toBit(reg.c()) << 7);
        reg.c(_c);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    /// Bitwise AND
    public void and(int value) {
        reg.a(reg.a() & value);
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Bitwise OR
    public void ora(int value) {
        reg.a(reg.a() | value);
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Bitwise Exclusive OR
    public void eor(int value) {
        reg.a(reg.a() ^ value);
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Bit Test
    public void bit(int value) {
        reg.v(getBitBool(value, 6));
        reg.n(getBitBool(value, 7));
        reg.z((reg.a() & value) == 0);
    }

    /// Add with Carry
    public void adc(int value) {
        int _i = reg.a() + value + toBit(reg.c());
        reg.v(getBitBool((reg.a() ^ _i) & (value ^ _i), 7));
        reg.c(getBitBool(_i, 8));
        reg.a(_i);
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Subtract with Carry
    public void sbc(int value) {
        adc(toU8(~value));
    }

    /// Increment Memory
    public int inc(int value) {
        value = toU8(++value);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
    }

    /// Decrement Memory
    public int dec(int value) {
        value = toU8(--value);
        reg.n(getBitBool(value, 7));
        reg.z(value == 0);
        return value;
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
        reg.a(value);
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }

    /// Store A
    public void sta(int value) {
        cpu.write(value, reg.a());
    }

    /// Load X
    public void ldx(int value) {
        reg.x(value);
        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    /// Store X
    public void stx(int value) {
        cpu.write(value, reg.x());
    }

    /// Load Y
    public void ldy(int value) {
        reg.y(value);
        reg.n(getBitBool(reg.y(), 7));
        reg.z(reg.y() == 0);
    }

    /// Store Y
    public void sty(int value) {
        cpu.write(value, reg.y());
    }

    /// Jump
    public void jmp(int value) {
        reg.pc(value);
    }

    /// Jump to Subroutine
    public void jsr(int value) {
        cpu.push(reg.pc() >> 8);
        cpu.push(reg.pc() & 0x00FF);
        reg.pc(value);
    }

    // -- Unofficial 6502 Instructions ------------------------------------------------
    // https://www.nesdev.org/wiki/CPU_unofficial_opcodes

    public void lax(int value) {
        lda(value);
        tax();
    }

    public void las(int value) {
        reg.a(value & reg.sp());
        reg.sp(reg.a());
        tax();
    }

    public int xas(int value) {
        reg.sp(reg.x() & reg.a());
        return reg.sp() & value;
    }

    public void sax(int value) {
        cpu.write(value, reg.a() & reg.x());
    }

    public void shx(int value) {
        cpu.write(value, reg.x() & ((value >> 8) + 1));
    }

    public void shy(int value) {
        cpu.write(value, reg.y() & ((value >> 8) + 1));
    }

    public void ahx(int value) {
        cpu.write(value, reg.a() & reg.x() & ((value >> 8) + 1));
    }

    // bug
    public void tas(int value, int page) {
        cpu.write(value, xas(toU8(++page)));
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
        value = (reg.a() & reg.x()) + 256 - value;
        reg.x(value);
        reg.c(getBitBool(value, 8));
        reg.n(getBitBool(reg.x(), 7));
        reg.z(reg.x() == 0);
    }

    public void xaa(int value) {
        reg.a(reg.a() & reg.x() & value);
        reg.c(getBitBool(reg.a(), 7));
        reg.n(getBitBool(reg.a(), 7));
        reg.z(reg.a() == 0);
    }
}
