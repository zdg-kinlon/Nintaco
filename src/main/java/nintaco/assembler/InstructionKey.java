package nintaco.assembler;

public class InstructionKey implements Comparable<InstructionKey> {

    private String mnemonic;
    private int patternIndex;
    private int labelType;
    private int hash;

    public InstructionKey() {
    }

    public InstructionKey(final String mnemonic, final int patternIndex,
                          final int labelType) {
        this.mnemonic = mnemonic;
        this.patternIndex = patternIndex;
        this.labelType = labelType;
        updateHash();
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
        updateHash();
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public void setPatternIndex(int patternIndex) {
        this.patternIndex = patternIndex;
        updateHash();
    }

    public int getLabelType() {
        return labelType;
    }

    public void setLabelType(int labelType) {
        this.labelType = labelType;
        updateHash();
    }

    private void updateHash() {
        hash = mnemonic.hashCode() ^ (patternIndex << 8) ^ labelType;
    }

    @Override
    public boolean equals(Object obj) {
        final InstructionKey key = (InstructionKey) obj;
        return patternIndex == key.patternIndex && labelType == key.labelType
                && mnemonic.equals(key.mnemonic);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(final InstructionKey key) {
        int v = mnemonic.compareTo(key.mnemonic);
        if (v == 0) {
            v = Integer.compare(patternIndex, key.patternIndex);
            if (v == 0) {
                return Integer.compare(labelType, key.labelType);
            } else {
                return v;
            }
        } else {
            return v;
        }
    }
}
