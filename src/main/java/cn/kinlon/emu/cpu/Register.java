package cn.kinlon.emu.cpu;

import static cn.kinlon.emu.utils.BitUtil.toBitBool;
import static cn.kinlon.emu.utils.ByteUtil.toU16;
import static cn.kinlon.emu.utils.ByteUtil.toU8;

public class Register {
    private int a;
    private int x;
    private int y;
    private int s;
    private int pc;
    private boolean n;
    private boolean v;
    private boolean r;
    private boolean b;
    private boolean d;
    private boolean i;
    private boolean z;
    private boolean c;

    public void a(int i) {
        a = toU8(i);
    }

    public void x(int i) {
        x = toU8(i);
    }

    public void y(int i) {
        y = toU8(i);
    }

    public void s(int i) {
        s = toU8(i);
    }

    public void pc(int i) {
        pc = toU16(i);
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

    public int s() {
        return s;
    }

    public int pc() {
        return pc;
    }

    public void sInc1() {
        s(s + 1);
    }

    public void sDec1() {
        s(s - 1);
    }

    public void pcInc1() {
        pc(pc + 1);
    }

    public void pcDec1() {
        pc(pc - 1);
    }

    public void n(boolean bool) {
        n = bool;
    }

    public void v(boolean bool) {
        v = bool;
    }

    public void r(boolean bool) {
        r = bool;
    }

    public void b(boolean bool) {
        b = bool;
    }

    public void d(boolean bool) {
        d = bool;
    }

    public void i(boolean bool) {
        i = bool;
    }

    public void z(boolean bool) {
        z = bool;
    }

    public void c(boolean bool) {
        c = bool;
    }

    public boolean n() {
        return n;
    }

    public boolean v() {
        return v;
    }

    public boolean r() {
        return r;
    }

    public boolean b() {
        return b;
    }

    public boolean d() {
        return d;
    }

    public boolean i() {
        return i;
    }

    public boolean z() {
        return z;
    }

    public boolean c() {
        return c;
    }

    public void p(int p) {
        n = toBitBool(p & NEGATIVE_MASK);
        v = toBitBool(p & OVERFLOW_MASK);
        r = toBitBool(p & RESERVED_MASK);
        b = toBitBool(p & BREAK_MASK);
        d = toBitBool(p & DECIMAL_MASK);
        i = toBitBool(p & INTERRUPT_MASK);
        z = toBitBool(p & ZERO_MASK);
        c = toBitBool(p & CARRY_MASK);
    }

    public int p() {
        return (n ? NEGATIVE_MASK : 0)
                | (v ? OVERFLOW_MASK : 0)
                | (r ? RESERVED_MASK : 0)
                | (b ? BREAK_MASK : 0)
                | (d ? DECIMAL_MASK : 0)
                | (i ? INTERRUPT_MASK : 0)
                | (z ? ZERO_MASK : 0)
                | (c ? CARRY_MASK : 0);
    }

    public static int NEGATIVE_MASK = 1 << 7;
    public static int OVERFLOW_MASK = 1 << 6;
    public static int RESERVED_MASK = 1 << 5;
    public static int BREAK_MASK = 1 << 4;
    public static int DECIMAL_MASK = 1 << 3;
    public static int INTERRUPT_MASK = 1 << 2;
    public static int ZERO_MASK = 1 << 1;
    public static int CARRY_MASK = 1;
}