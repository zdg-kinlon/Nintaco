package nintaco.serializer;

import java.io.*;

public interface Transients {
    void readTransients(DataInput in) throws IOException;

    void writeTransients(DataOutput out) throws IOException;
}
