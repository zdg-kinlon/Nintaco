package nintaco.movie;

import java.io.*;

import nintaco.input.*;

import static nintaco.movie.Movie.*;

public class MovieBlock implements Serializable {

    private static final long serialVersionUID = 0L;

    public byte[] saveState;
    public final int[] buttons = new int[BLOCK_SIZE];
    public OtherInput[][] otherInputs;
}
