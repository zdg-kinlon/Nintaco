// TODO THIS CLASS IS NOT USED. CONSIDER REMOVING IT.

package nintaco.movie;

import java.io.*;
import java.util.*;

import static nintaco.movie.Movie.*;

public final class MovieStates {

    private MovieStates() {
    }

    public static void save(final String fileName, final Movie movie,
                            final boolean compact) throws Throwable {

        try (final DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)))) {

            final List<MovieBlock> blocks = movie.getMovieBlocks();

            out.writeBoolean(movie.isVsDualSystem());
            out.writeInt(movie.getFrameIndex());
            out.writeInt(movie.getFrameCount());
            out.writeInt(blocks.size());
            out.writeBoolean(compact);
            for (int i = 0; i < blocks.size(); i++) {
                final MovieBlock block = blocks.get(i);
                final int[] buttons = block.buttons;
                if (i == 0 || !compact) {
                    final byte[] saveState = block.saveState;
                    out.writeInt(saveState.length);
                    out.write(saveState);
                }
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    out.writeInt(buttons[j]);
                }
            }
        }
    }

    public static Movie load(final String fileName) throws Throwable {
        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(fileName)))) {

            final boolean vsDualSystem = in.readBoolean();
            final Movie movie = new Movie(vsDualSystem);
            final List<MovieBlock> blocks = movie.getMovieBlocks();
            movie.frameIndex = in.readInt();
            movie.frameCount = in.readInt();
            final int blocksSize = in.readInt();
            final boolean compact = in.readBoolean();
            for (int i = 0; i < blocksSize; i++) {
                final MovieBlock block = new MovieBlock();
                final int[] buttons = block.buttons;
                blocks.add(block);
                if (i == 0 || !compact) {
                    block.saveState = new byte[in.readInt()];
                    in.readFully(block.saveState);
                }
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    buttons[j] = in.readInt();
                }
            }
            return movie;
        }
    }
}
