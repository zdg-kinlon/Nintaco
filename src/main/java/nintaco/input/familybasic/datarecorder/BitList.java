package nintaco.input.familybasic.datarecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;

import static java.lang.Math.min;
import static nintaco.util.MathUtil.roundUpDivision;

public class BitList implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int BLOCK_SIZE = 0x0800;

    private transient int[] data;

    private int capacity;
    private int size;

    private void ensureCapacity() {
        if (data == null) {
            data = new int[0];
        }
        if (capacity < size + 1) {
            final int[] d = new int[data.length + BLOCK_SIZE];
            System.arraycopy(data, 0, d, 0, data.length);
            data = d;
            capacity = data.length << 5;
        }
    }

    public void add(final boolean value) {
        add(value ? 1 : 0);
    }

    public void add(final int value) {
        ensureCapacity();
        data[size >> 5] |= (value & 1) << (size & 0x1F);
        size++;
    }

    public boolean getBool(final int index) {
        return get(index) == 1;
    }

    public int get(final int index) {
        return (data[index >> 5] >> (index & 0x1F)) & 1;
    }

    public void clear() {
        data = new int[0];
        capacity = size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void save(final DataOutputStream out) throws Throwable {
        ensureCapacity();
        final int length = min(data.length, roundUpDivision(size, 32));
        out.writeInt(capacity);
        out.writeInt(size);
        out.writeInt(length);
        for (int i = 0; i < length; i++) {
            out.writeInt(data[i]);
        }
    }

    public void load(final DataInputStream in) throws Throwable {
        try {
            capacity = in.readInt();
            size = in.readInt();
            final int length = in.readInt();
            data = new int[capacity >> 5];
            for (int i = 0; i < length; i++) {
                data[i] = in.readInt();
            }
        } catch (final Throwable t) {
            clear();
            throw t;
        }
    }

    // Leave below commented else the tape will be stored in history.

//  private void readObject(final ObjectInputStream in) 
//      throws IOException, ClassNotFoundException {    
//    in.defaultReadObject();
//    data = new int[capacity >> 5];
//    final int length = in.readInt();
//    for(int i = 0; i < length; i++) {
//      data[i] = in.readInt();
//    }
//  }  
//  
//  private void writeObject(final ObjectOutputStream out) throws IOException {
//    out.defaultWriteObject();
//    final int length = min(data.length, roundUpDivision(size, 32));
//    out.writeInt(length);
//    for(int i = 0; i < length; i++) {
//      out.writeInt(data[i]);
//    }
//  }
}
