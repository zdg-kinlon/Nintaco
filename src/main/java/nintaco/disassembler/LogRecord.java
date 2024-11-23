package nintaco.disassembler;

public class LogRecord {

    public int frameCounter;
    public int instructionsCounter;
    public long cpuCycleCounter;
    public int scanlineCycle;
    public int scanline;

    public int length;
    public int opcode;
    public int b1;
    public int b2;

    public int mnemonicIndex;
    public String mnemonic;
    public int instructionType;

    public int bank;
    public int PC;
    public int A;
    public int X;
    public int Y;
    public int P;
    public int S;

    public int v;
    public int t;
    public int x;
    public boolean w;

    public int value0;
    public int value1;
    public int value2;

    public int bank0;
    public int bank1;

    public int[] values;
}
