package nintaco.gui;

import java.util.ArrayList;
import java.util.List;

public class IntList {

    private static final int BLOCK_SHIFT = 10;
    private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
    private static final int BLOCK_MASK = BLOCK_SIZE - 1;

    private final List<int[]> blocks = new ArrayList<>();

    private int size;

    public void add(final int value) {
        final int blockIndex = size >> BLOCK_SHIFT;
        while (blocks.size() <= blockIndex) {
            blocks.add(new int[BLOCK_SIZE]);
        }
        blocks.get(blockIndex)[size & BLOCK_MASK] = value;
        size++;
    }

    public int get(final int index) {
        return blocks.get(index >> BLOCK_SHIFT)[index & BLOCK_MASK];
    }

    public void clear() {
        blocks.clear();
    }

    public int[] toArray() {

        final int[] values = new int[size];

        outer:
        for (int i = 0, k = 0; i < blocks.size() && k < size; i++) {
            final int[] block = blocks.get(i);
            for (int j = 0; j < block.length; j++) {
                if (k >= size) {
                    break outer;
                }
                values[k++] = block[j];
            }
        }

        return values;
    }

    public int size() {
        return size;
    }
}
