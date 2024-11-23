package nintaco.disassembler;

public interface OperandsType {
    int NONE = 0;
    int A = 1;
    int IMMEDIATE = 2;
    int ZERO_PAGE = 3;
    int ZERO_PAGE_X = 4;
    int ZERO_PAGE_Y = 5;
    int INDIRECT_X = 6;
    int INDIRECT_Y = 7;
    int ABSOLUTE = 8;
    int INDIRECT = 9;
    int ABSOLUTE_X = 10;
    int ABSOLUTE_Y = 11;
}
