package cn.kinlon.emu.cpu;

import cn.kinlon.emu.utils.BitUtil;

import static cn.kinlon.emu.cpu.StatusFlag.*;
import static cn.kinlon.emu.utils.BitUtil.*;
import static cn.kinlon.emu.utils.ByteUtil.toU16;
import static cn.kinlon.emu.utils.ByteUtil.toU8;

public class Register {
    private int a;
    private int x;
    private int y;
    private int pc;
    private int sp;
    private int p; // NVRBDIZC

    public void a(int v) {
        a = toU8(v);
    }

    public void x(int v) {
        x = toU8(v);
    }

    public void y(int v) {
        y = toU8(v);
    }

    public void pc(int v) {
        pc = toU16(v);
    }

    public void sp(int v) {
        sp = toU8(v);
    }

    public int a() {
        return a;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int pc() {
        return pc;
    }

    public int sp() {
        return sp;
    }

    public void pcDec1() {
        pc(pc - 1);
    }

    public void pcInc1() {
        pc(pc + 1);
    }

    public void spDec1() {
        sp(sp - 1);
    }

    public void spInc1() {
        sp(sp + 1);
    }

    public void p(int v) {
        p = toU8(v);
    }

    public int p() {
        return p;
    }

    private void p(StatusFlag flag, boolean bool) {
        p = setBit(p, flag.getBit(), bool);
    }

    private boolean p(StatusFlag flag) {
        return getBitBool(p, flag.getBit());
    }

    public boolean c() {
        return p(CARRY_FLAG);
    }

    public void c(boolean bool) {
        p(CARRY_FLAG, bool);
    }

    public boolean z() {
        return p(ZERO_FLAG);
    }

    public void z(boolean bool) {
        p(ZERO_FLAG, bool);
    }

    public boolean i() {
        return p(INTERRUPT_DISABLE);
    }

    public void i(boolean bool) {
        p(INTERRUPT_DISABLE, bool);
    }

    public boolean d() {
        return p(DECIMAL_MODE);
    }

    public void d(boolean bool) {
        p(DECIMAL_MODE, bool);
    }

    public boolean b() {
        return p(BREAK_COMMAND);
    }

    public void b(boolean bool) {
        p(BREAK_COMMAND, bool);
    }

    public boolean r() {
        return p(RESERVED);
    }

    public void r(boolean bool) {
        p(RESERVED, bool);
    }

    public boolean v() {
        return p(OVERFLOW_FLAG);
    }

    public void v(boolean bool) {
        p(OVERFLOW_FLAG, bool);
    }

    public boolean n() {
        return p(NEGATIVE_FLAG);
    }

    public void n(boolean bool) {
        p(NEGATIVE_FLAG, bool);
    }
}