package nintaco.input;

import net.java.games.input.Controller;
import net.java.games.input.EventQueue;

public class ControllerQueue {

    public final int index;
    public final Controller controller;
    public final EventQueue eventQueue;
    public final InputDeviceID inputDeviceID;

    public ControllerQueue(final int index, final Controller controller,
                           final EventQueue eventQueue, final InputDeviceID inputDeviceID) {
        this.index = index;
        this.controller = controller;
        this.eventQueue = eventQueue;
        this.inputDeviceID = inputDeviceID;
    }

    public int getIndex() {
        return index;
    }

    public Controller getController() {
        return controller;
    }

    public EventQueue getEventQueue() {
        return eventQueue;
    }

    public InputDeviceID getInputDeviceID() {
        return inputDeviceID;
    }
}
