package nintaco.gui.rob;

import nintaco.mappers.Mapper;

import java.io.Serializable;

import static nintaco.gui.rob.RobCommand.*;
import static nintaco.util.BitUtil.toBit;

public abstract class RobController implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int TEST_STEPS = 60;

    protected final RobState state = new RobState();

    protected RobCommand executingCommand;
    protected int counter;
    protected int shiftRegister;

    protected volatile RobCommand requestedCommand;

    public RobState getState() {
        return state;
    }

    public void scanMemory(final Mapper mapper) {
    }

    public int pollButtons(final int buttons, final Mapper mapper) {
        return buttons;
    }

    public void request(final RobCommand requestedCommand) {
        this.requestedCommand = requestedCommand;
    }

    public void signal(final boolean greenScreen) {
        shiftRegister = (shiftRegister << 1) | toBit(greenScreen);
        switch (shiftRegister & 0x1FFF) {
            case 0x02AE:
            case 0x02FB:
                request(MoveDown);
                break;
            case 0x02BA:
                request(MoveLeft);
                break;
            case 0x02BB:
            case 0x02FA:
                request(MoveUp);
                break;
            case 0x02BE:
                request(CloseHands);
                break;
            case 0x02EA:
                request(MoveRight);
                break;
            case 0x02EB:
                request(Test);
                break;
            case 0x02EE:
                request(OpenHands);
                break;
        }
    }

    public boolean update() {
        final RobCommand request = requestedCommand;
        requestedCommand = null;
        if (executingCommand == null && request != null) {
            executingCommand = request;
            initCommand();
        }
        if (executingCommand != null) {
            executeCommand();
            state.modifications++;
            return true;
        } else {
            return false;
        }
    }

    protected void initCommand() {
        switch (executingCommand) {
            case MoveUp:
                initMoveUp();
                break;
            case MoveDown:
                initMoveDown();
                break;
            case MoveLeft:
                initMoveLeft();
                break;
            case MoveRight:
                initMoveRight();
                break;
            case OpenHands:
                initOpenHands();
                break;
            case CloseHands:
                initCloseHands();
                break;
            case Test:
                initTest();
                break;
            case Repaint:
                initRepaint();
                break;
            case AdvancePhase:
                initAdvancePhase();
                break;
        }
    }

    protected void executeCommand() {
        switch (executingCommand) {
            case MoveUp:
                executeMoveUp();
                break;
            case MoveDown:
                executeMoveDown();
                break;
            case MoveLeft:
                executeMoveLeft();
                break;
            case MoveRight:
                executeMoveRight();
                break;
            case OpenHands:
                executeOpenHands();
                break;
            case CloseHands:
                executeCloseHands();
                break;
            case Test:
                executeTest();
                break;
            case Repaint:
                executeRepaint();
                break;
            case AdvancePhase:
                executeAdvancePhase();
                break;
        }
    }

    protected void initTest() {
        getState().testing = true;
        counter = TEST_STEPS;
    }

    protected void initRepaint() {
        counter = 2;
    }

    protected void initAdvancePhase() {
        executingCommand = null;
    }

    protected void executeTest() {
        if (--counter == 0) {
            getState().testing = false;
            executingCommand = null;
        }
    }

    protected void executeRepaint() {
        if (--counter == 0) {
            executingCommand = null;
        }
    }

    protected void executeAdvancePhase() {
    }

    protected abstract void initMoveUp();

    protected abstract void initMoveDown();

    protected abstract void initMoveLeft();

    protected abstract void initMoveRight();

    protected abstract void initOpenHands();

    protected abstract void initCloseHands();

    protected abstract void executeMoveUp();

    protected abstract void executeMoveDown();

    protected abstract void executeMoveLeft();

    protected abstract void executeMoveRight();

    protected abstract void executeOpenHands();

    protected abstract void executeCloseHands();
}
