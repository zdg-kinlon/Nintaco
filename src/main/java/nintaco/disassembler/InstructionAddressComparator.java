package nintaco.disassembler;

import java.util.Comparator;

public class InstructionAddressComparator implements Comparator<Instruction> {

    @Override
    public int compare(final Instruction i1, final Instruction i2) {
        return Integer.compare(i1.getAddress(), i2.getAddress());
    }
}
