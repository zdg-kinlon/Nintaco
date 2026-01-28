package nintaco.movie;

import java.io.*;
import java.util.*;

import nintaco.*;
import nintaco.gui.historyeditor.*;
import nintaco.gui.rob.*;
import nintaco.input.*;
import nintaco.mappers.nintendo.vs.*;
import nintaco.netplay.protocol.*;

import static nintaco.util.BitUtil.*;
import static nintaco.util.StreamUtil.*;

public class Movie implements Serializable {

    private static final long serialVersionUID = 0L;

    public static final int BLOCK_SIZE = 64;
    public static final int BLOCK_MASK = BLOCK_SIZE - 1;
    public static final int BLOCK_SHIFT = log2(BLOCK_SIZE);

    public static final int DOUBLE_SIZE = BLOCK_SIZE << 1;
    public static final int DOUBLE_MASK = DOUBLE_SIZE - 1;
    public static final int DOUBLE_SHIFT = log2(DOUBLE_SIZE);

    public final List<MovieBlock> movieBlocks = new ArrayList<>();

    public final boolean vsDualSystem;

    public int frameIndex = -1;
    public int frameCount = 0;

    public transient MovieFrame[] movieFrames;

    private transient volatile HistoryEditorFrame historyEditorFrame;

    public Movie(final boolean vsDualSystem) {
        this.vsDualSystem = vsDualSystem;
        initMovieFrames();
    }

    private void initMovieFrames() {
        movieFrames = new MovieFrame[DOUBLE_SIZE];
        for (int i = movieFrames.length - 1; i >= 0; i--) {
            movieFrames[i] = new MovieFrame(vsDualSystem);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        initMovieFrames();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public void clear() {
        clear(null);
    }

    public void clear(final byte[] saveState) {
        clearCachedFrames();
        frameIndex = -1;
        frameCount = 0;
        movieBlocks.clear();
        if (saveState != null) {
            final MovieBlock block = new MovieBlock();
            block.saveState = saveState;
            movieBlocks.add(block);
        }
    }

    public void clearCachedFrames() {
        for (int i = movieFrames.length - 1; i >= 0; i--) {
            movieFrames[i].frameIndex = -1;
        }
    }

    public void truncate() {
        frameCount = frameIndex + 1;
        final int blockIndex = frameIndex >> BLOCK_SHIFT;
        if (blockIndex < movieBlocks.size()) {
            final MovieBlock movieBlock = movieBlocks.get(blockIndex);
            for (int i = (frameIndex & BLOCK_MASK) + 1; i < BLOCK_SIZE; i++) {
                movieBlock.buttons[i] = 0;
            }
            if (movieBlocks.size() > blockIndex + 1) {
                movieBlocks.subList(blockIndex + 1, movieBlocks.size()).clear();
            }
        }
    }

    public void setHistoryEditorFrame(HistoryEditorFrame historyEditorFrame) {
        this.historyEditorFrame = historyEditorFrame;
    }

    public MovieFrame getCurrentMovieFrame() {
        return movieFrames[frameIndex & DOUBLE_MASK];
    }

    public void updateMovieFrame(final int[] screen, final Machine machine) {

        final MovieFrame movieFrame = getCurrentMovieFrame();
        movieFrame.frameIndex = frameIndex;
        System.arraycopy(screen, 0, movieFrame.screen, 0, screen.length);

        if (machine != null) {
            final PPU ppu = machine.getPPU();
            final RobController rob = ppu.getRob();
            if (rob == null) {
                movieFrame.robState.game = RobGame.NONE;
            } else {
                movieFrame.robState.init(rob.getState());
            }

            if (machine.isVsDualSystem()) {
                final PPU subPPU = ((MainCPU) machine.cpu()).getSubPPU();
                System.arraycopy(subPPU.getScreen(), 0, movieFrame.screen2, 0,
                        screen.length);
            }
        }

        final HistoryEditorFrame frame = historyEditorFrame;
        if (frame != null) {
            frame.movieUpdated(frameIndex);
        }
    }

    public void updateMovieBlock(final Machine machine,
                                 final ControllerInput controllerInput, final boolean isNotClient) {

        frameIndex++;
        getCurrentMovieFrame().audioLength = 0;

        if (isNotClient) {
            MovieBlock movieBlock = null;
            final int blockIndex = frameIndex >> BLOCK_SHIFT;
            while (blockIndex >= movieBlocks.size()) {
                movieBlocks.add(new MovieBlock());
            }
            movieBlock = movieBlocks.get(blockIndex);

            final int index = frameIndex & BLOCK_MASK;
            if (index == 0) {
                try {
                    movieBlock.saveState = toByteArrayOutputStream(machine).toByteArray();
                } catch (final Throwable t) {
                    //t.printStackTrace();
                }
            }
            movieBlock.buttons[index] = controllerInput.input;
            if (controllerInput.otherInputs != null) {
                if (movieBlock.otherInputs == null) {
                    movieBlock.otherInputs = new OtherInput[BLOCK_SIZE][];
                }
                movieBlock.otherInputs[index] = controllerInput.otherInputs;
            }
            frameCount = (((movieBlocks.size() - 1) << BLOCK_SHIFT)
                    | (frameIndex & BLOCK_MASK)) + 1;
        }
    }

    public boolean isVsDualSystem() {
        return vsDualSystem;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public List<MovieBlock> getMovieBlocks() {
        return movieBlocks;
    }

    public MovieFrame[] getMovieFrames() {
        return movieFrames;
    }
}
