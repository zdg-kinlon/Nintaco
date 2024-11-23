package nintaco.gui.rob;

import nintaco.mappers.Mapper;

import static java.lang.Math.*;
import static nintaco.gui.rob.RobGame.GYROMITE;

public class GyromiteController extends RobController {

    private static final long serialVersionUID = 0;

    private static final int VERTICAL_STEPS = 12;
    private static final int HORIZONTAL_STEPS = 16;
    private static final int GRASP_STEPS = 8;
    private static final float ACCELERATION = 0.1f;

    private static final float VERTICAL_DELTA = 2f / VERTICAL_STEPS;
    private static final float HORIZONTAL_DELTA = 1f / HORIZONTAL_STEPS;
    private static final float GRASP_DELTA = 1f / GRASP_STEPS;

    private int heldGyro = -1;
    private float fallingVelocity;

    public GyromiteController() {
        state.game = GYROMITE;
        state.reset();
    }

    @Override
    public int pollButtons(int buttons, final Mapper mapper) {
        if (state.aButtonY == 4f) {
            buttons |= 0x0100;
        }
        if (state.bButtonY == 4f) {
            buttons |= 0x0200;
        }
        return buttons;
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
        if (state.handsY == 4f
                || (state.handsX == 2f && state.handsY == 2f)) {
            executingCommand = null;
            return;
        }
        if (state.handsOpen == 1f) {
            counter = VERTICAL_STEPS;
            return;
        }
        final boolean unheldGyro = containsUnheldGyro(state.handsX);
        if (!unheldGyro) {
            counter = VERTICAL_STEPS;
            return;
        }
        if (heldGyro >= 0 || state.handsY == 2f
                || (state.handsX == 2f && state.handsY == 0f)) {
            executingCommand = null;
            return;
        }
        counter = VERTICAL_STEPS;
    }

    @Override
    protected void initMoveLeft() {
        if (state.handsX == -2f || (containsUnheldGyro(state.handsX - 1)
                && ((heldGyro >= 0 && state.handsY >= 2f) || (state.handsY == 4f
                && state.handsX < 2f))) || ((state.handsY == 4f
                || (state.handsX == 2f && state.handsY == 2f))
                && (heldGyro >= 0 || (state.handsOpen == 1f
                && containsUnheldGyro(state.handsX))))) {
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
        final int sourceGyro = getUnheldGyro(state.handsX);
        if (sourceGyro >= 0 && state.handsOpen == 1f
                && state.handsY == state.pieces[sourceGyro][1]) {
            executingCommand = null;
            return;
        }
        final int targetGyro = getUnheldGyro(state.handsX + 1);
        if (targetGyro >= 0) {
            if (heldGyro >= 0) {
                if (state.pieces[heldGyro][1] >= state.pieces[targetGyro][1] - 2) {
                    executingCommand = null;
                    return;
                }
            } else if (state.handsY >= state.pieces[targetGyro][1]) {
                executingCommand = null;
                return;
            }
        } else if (heldGyro >= 0) {
            if (state.handsY == 4f || (state.handsX == 1f
                    && state.handsY != 0f)) {
                executingCommand = null;
                return;
            }
        } else if (state.handsX == 1f && state.handsY == 4f) {
            executingCommand = null;
            return;
        }
        counter = HORIZONTAL_STEPS;
    }

    @Override
    protected void initOpenHands() {
        if (state.handsOpen == 1f
                || (heldGyro >= 0 && containsUnheldGyro(state.handsX))) {
            executingCommand = null;
        } else {
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
    protected void executeMoveUp() {
        state.handsY -= VERTICAL_DELTA;
        if (--counter == 0) {
            state.handsY = round(state.handsY);
            executingCommand = null;
        }
        if (heldGyro >= 0) {
            state.pieces[heldGyro][1] = state.handsY;
            if (state.handsX == 0f) {
                state.aButtonY = max(3f, min(state.aButtonY, state.handsY));
            } else if (state.handsX == 1f) {
                state.bButtonY = max(3f, min(state.bButtonY, state.handsY));
            }
        }
    }

    @Override
    protected void executeMoveDown() {
        state.handsY += VERTICAL_DELTA;
        if (--counter == 0) {
            state.handsY = round(state.handsY);
            executingCommand = null;
        }
        if (heldGyro >= 0) {
            state.pieces[heldGyro][1] = state.handsY;
            if (state.handsX == 0f) {
                state.aButtonY = min(4f, max(state.aButtonY, state.handsY));
            } else if (state.handsX == 1f) {
                state.bButtonY = min(4f, max(state.bButtonY, state.handsY));
            }
        }
    }

    @Override
    protected void executeMoveLeft() {
        state.handsX -= HORIZONTAL_DELTA;
        if (--counter == 0) {
            state.handsX = round(state.handsX);
            executingCommand = null;
        }
        if (heldGyro >= 0) {
            state.pieces[heldGyro][0] = state.handsX;
        }
    }

    @Override
    protected void executeMoveRight() {
        state.handsX += HORIZONTAL_DELTA;
        if (--counter == 0) {
            state.handsX = round(state.handsX);
            executingCommand = null;
        }
        if (heldGyro >= 0) {
            state.pieces[heldGyro][0] = state.handsX;
        }
    }

    @Override
    protected void executeOpenHands() {
        if (heldGyro >= 0) {
            fallingVelocity += ACCELERATION;
            state.pieces[heldGyro][1] += fallingVelocity;
            final float ground = state.handsX < 2f ? 4f : 2f;
            if (state.pieces[heldGyro][1] >= ground) {
                state.pieces[heldGyro][1] = ground;
            }
            if (state.handsX == 0f
                    && state.pieces[heldGyro][1] > state.aButtonY) {
                state.aButtonY = state.pieces[heldGyro][1];
            }
            if (state.handsX == 1f
                    && state.pieces[heldGyro][1] > state.bButtonY) {
                state.bButtonY = state.pieces[heldGyro][1];
            }
            if (state.pieces[heldGyro][1] >= ground) {
                heldGyro = -1;
            }
        }
        if (counter > 0) {
            state.handsOpen += GRASP_DELTA;
            counter--;
        } else if (heldGyro < 0) {
            fallingVelocity = 0f;
            state.handsOpen = 1f;
            executingCommand = null;
        }
    }

    @Override
    protected void executeCloseHands() {
        state.handsOpen -= GRASP_DELTA;
        if (--counter == 0) {
            state.handsOpen = 0f;
            final int index = getUnheldGyro(state.handsX);
            if (index >= 0 && state.pieces[index][1] == state.handsY) {
                heldGyro = index;
            }
            executingCommand = null;
        }
    }

    public int getUnheldGyro(final float x) {
        for (int i = 2; i >= 0; i--) {
            if (i != heldGyro && state.pieces[i][0] == x) {
                return i;
            }
        }
        return -1;
    }

    private boolean containsUnheldGyro(final float x) {
        return getUnheldGyro(x) >= 0;
    }
}
