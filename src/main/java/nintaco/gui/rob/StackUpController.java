package nintaco.gui.rob;

import nintaco.mappers.Mapper;

import java.util.Arrays;

import static java.lang.Math.*;
import static nintaco.gui.rob.RobCommand.*;
import static nintaco.gui.rob.RobGame.STACK_UP;

public class StackUpController extends RobController {

    private static final long serialVersionUID = 0;

    private static final int MODE_ADDRESS = 0x0038;
    private static final int SCROLL_ADDRESS = 0x000B;
    private static final int PROGRAM_RUNNING_ADDRESS = 0x0065;

    private static final int VERTICAL_STEPS = 12;
    private static final int HORIZONTAL_STEPS = 16;
    private static final int GRASP_STEPS = 8;
    private static final int ADVANCE_PHASE_STEPS = 16;
    private static final float ACCELERATION = 0.1f;

    private static final float VERTICAL_DELTA = 1f / VERTICAL_STEPS;
    private static final float HORIZONTAL_DELTA = 1f / HORIZONTAL_STEPS;
    private static final float GRASP_DELTA = 1f / GRASP_STEPS;

    private final boolean[] heldBlocks = new boolean[5];
    private final int[][][] puzzles = new int[4][6][5];

    private float fallingVelocity;
    private float stackHeight;

    public StackUpController() {
        state.game = STACK_UP;
        state.reset();
    }

    @Override
    public int pollButtons(final int buttons, final Mapper mapper) {
        if ((buttons & 0x08080808) != 0) {
            Arrays.fill(heldBlocks, false);
            fallingVelocity = 0;
            stackHeight = 0;
            state.reset();
            scanMemory(mapper);
        } else if (executingCommand == AdvancePhase) {
            final int mode = mapper.readMemory(MODE_ADDRESS);
            switch (mode) {
                case 1:
                case 2:
                    return buttons | 0x08;
                case 3:
                    if (counter > (ADVANCE_PHASE_STEPS >> 1)) {
                        return buttons | 0x01;
                    } else {
                        final int scroll = mapper.readMemory(SCROLL_ADDRESS);
                        if (scroll != 0x80) {
                            counter++;
                        } else {
                            return buttons | 0x08;
                        }
                    }
                    break;
            }
        }
        return buttons;
    }

    @Override
    public void scanMemory(final Mapper mapper) {

        final int mode = mapper.readMemory(MODE_ADDRESS);
        if (mode == 0) {
            return;
        }

        boolean matches = true;
        for (int i = 5; i >= 0; i--) {
            for (int j = 4; j >= 0; j--) {
                final int offset = 5 * i + j;
                final int startValue = mapper.readMemory(0x0520 | offset);
                if (startValue > 5) {
                    return;
                }
                final int goalValue = mapper.readMemory(0x0500 | offset);
                if (goalValue > 5) {
                    return;
                }
                if (puzzles[0][i][j] != startValue || puzzles[1][i][j] != goalValue) {
                    matches = false;
                }
                puzzles[2][i][j] = startValue;
                puzzles[3][i][j] = goalValue;
            }
        }

        if (matches) {
            if (requestedCommand == null && executingCommand == null) {
                final float[][] blocks = state.pieces;
                if (mode == 2 && mapper.readMemory(PROGRAM_RUNNING_ADDRESS) != 0X01) {
                    return;
                }
                switch (mode) {
                    case 1:
                    case 2:
                        outer:
                        {
                            for (int i = 4; i >= 0; i--) {
                                int y = (int) blocks[i][1];
                                if (y == 0 || puzzles[1][y][2 + (int) blocks[i][0]] != i + 1) {
                                    break outer;
                                }
                            }
                            request(AdvancePhase);
                        }
                        break;
                    case 3:
                        outer:
                        {
                            for (int i = 4; i >= 0; i--) {
                                int y = (int) blocks[i][1];
                                if (y == 0 || puzzles[1][y][2 + (int) blocks[i][0]] == 0) {
                                    break outer;
                                }
                            }
                            request(AdvancePhase);
                        }
                        break;
                }
            }
        } else {
            int[][] temp = puzzles[2];
            puzzles[2] = puzzles[0];
            puzzles[0] = temp;
            temp = puzzles[3];
            puzzles[3] = puzzles[1];
            puzzles[1] = temp;

            for (int i = 5; i >= 0; i--) {
                for (int j = 4; j >= 0; j--) {
                    int index = puzzles[0][i][j];
                    if (index > 0) {
                        index--;
                        state.pieces[index][0] = j - 2;
                        state.pieces[index][1] = i;
                    }
                }
            }

            executingCommand = requestedCommand = null;
            counter = 0;
            if (state.handsOpen == 0f) {
                request(OpenHands);
            } else {
                request(Repaint);
            }
        }
    }

    @Override
    protected void initMoveUp() {
        if (state.handsY == 0f) {
            executingCommand = null;
        } else {
            counter = VERTICAL_STEPS;
        }
    }

    @Override
    protected void initMoveDown() {
        if (state.handsY == 5f) {
            executingCommand = null;
            return;
        }
        final float height = getStackHeight(state.handsX);
        if (state.handsOpen == 0f && state.handsY == height) {
            executingCommand = null;
            return;
        }
        counter = VERTICAL_STEPS;
    }

    @Override
    protected void initMoveLeft() {
        if (state.handsX == -2f) {
            executingCommand = null;
            return;
        }
        boolean holding = false;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                holding = true;
                break;
            }
        }
        final float height = getStackHeight(state.handsX);
        if ((state.handsOpen == 1f && state.handsY > height)
                || (holding && state.handsY >= height)) {
            executingCommand = null;
            return;
        }
        final float targetHeight = getStackHeight(state.handsX - 1);
        if (state.handsY > targetHeight
                || (holding && state.handsY >= targetHeight)) {
            executingCommand = null;
            return;
        }
        counter = HORIZONTAL_STEPS;
    }

    @Override
    protected void initMoveRight() {
        if (state.handsX == 2f) {
            executingCommand = null;
            return;
        }
        boolean holding = false;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                holding = true;
                break;
            }
        }
        final float height = getStackHeight(state.handsX);
        if ((state.handsOpen == 1f && state.handsY > height)
                || (holding && state.handsY >= height)) {
            executingCommand = null;
            return;
        }
        final float targetHeight = getStackHeight(state.handsX + 1);
        if (state.handsY > targetHeight
                || (holding && state.handsY >= targetHeight)) {
            executingCommand = null;
            return;
        }
        counter = HORIZONTAL_STEPS;
    }

    @Override
    protected void initOpenHands() {
        if (state.handsOpen == 1f) {
            executingCommand = null;
        } else {
            fallingVelocity = 0f;
            stackHeight = getStackHeight(state.handsX);
            counter = GRASP_STEPS;
        }
    }

    @Override
    protected void initCloseHands() {
        if (state.handsOpen == 0f) {
            executingCommand = null;
        } else {
            counter = GRASP_STEPS;
        }
    }

    @Override
    protected void initAdvancePhase() {
        counter = ADVANCE_PHASE_STEPS;
    }

    @Override
    protected void executeMoveUp() {
        state.handsY -= VERTICAL_DELTA;
        float[][] blocks = state.pieces;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                blocks[i][1] -= VERTICAL_DELTA;
            }
        }
        if (--counter == 0) {
            state.handsY = round(state.handsY);
            for (int i = 4; i >= 0; i--) {
                if (heldBlocks[i]) {
                    blocks[i][1] = round(blocks[i][1]);
                }
            }
            executingCommand = null;
        }
    }

    @Override
    protected void executeMoveDown() {
        state.handsY += VERTICAL_DELTA;
        float[][] blocks = state.pieces;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                blocks[i][1] += VERTICAL_DELTA;
            }
        }
        if (--counter == 0) {
            state.handsY = round(state.handsY);
            for (int i = 4; i >= 0; i--) {
                if (heldBlocks[i]) {
                    blocks[i][1] = round(blocks[i][1]);
                }
            }
            executingCommand = null;
        }
    }

    @Override
    protected void executeMoveLeft() {
        state.handsX -= HORIZONTAL_DELTA;
        float[][] blocks = state.pieces;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                blocks[i][0] -= HORIZONTAL_DELTA;
            }
        }
        if (--counter == 0) {
            state.handsX = round(state.handsX);
            for (int i = 4; i >= 0; i--) {
                if (heldBlocks[i]) {
                    blocks[i][0] = round(blocks[i][0]);
                }
            }
            executingCommand = null;
        }
    }

    @Override
    protected void executeMoveRight() {
        state.handsX += HORIZONTAL_DELTA;
        float[][] blocks = state.pieces;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                blocks[i][0] += HORIZONTAL_DELTA;
            }
        }
        if (--counter == 0) {
            state.handsX = round(state.handsX);
            for (int i = 4; i >= 0; i--) {
                if (heldBlocks[i]) {
                    blocks[i][0] = round(blocks[i][0]);
                }
            }
            executingCommand = null;
        }
    }

    @Override
    protected void executeOpenHands() {
        if (counter > 0) {
            state.handsOpen += GRASP_DELTA;
            counter--;
        }
        fallingVelocity += ACCELERATION;
        boolean fallCompleted = false;
        boolean fallingBlocks = false;
        final float[][] blocks = state.pieces;
        for (int i = 4; i >= 0; i--) {
            if (heldBlocks[i]) {
                fallingBlocks = true;
                blocks[i][1] += fallingVelocity;
                if (blocks[i][1] >= stackHeight) {
                    fallCompleted = true;
                }
            }
        }
        if (fallCompleted) {
            fallingBlocks = false;
            double delta = 0;
            for (int i = 4; i >= 0; i--) {
                if (heldBlocks[i] && blocks[i][1] >= stackHeight) {
                    delta = max(delta, blocks[i][1] - stackHeight);
                }
            }
            for (int i = 4; i >= 0; i--) {
                if (heldBlocks[i]) {
                    heldBlocks[i] = false;
                    blocks[i][1] = round(blocks[i][1] - delta);
                }
            }
        }
        if (counter == 0 && !fallingBlocks) {
            state.handsOpen = 1f;
            executingCommand = null;
        }
    }

    @Override
    protected void executeCloseHands() {
        state.handsOpen -= GRASP_DELTA;
        if (--counter == 0) {
            state.handsOpen = 0f;
            float[][] blocks = state.pieces;
            for (int i = 4; i >= 0; i--) {
                if (blocks[i][0] == state.handsX && blocks[i][1] <= state.handsY) {
                    heldBlocks[i] = true;
                }
            }
            executingCommand = null;
        }
    }

    @Override
    protected void executeAdvancePhase() {
        if (--counter == 0) {
            executingCommand = null;
            request(OpenHands);
        }
    }

    private float getStackHeight(final float x) {
        float[][] blocks = state.pieces;
        float height = 5f;
        for (int i = 4; i >= 0; i--) {
            if (!heldBlocks[i] && blocks[i][0] == x) {
                height = min(height, blocks[i][1] - 1);
            }
        }
        return height;
    }
}
