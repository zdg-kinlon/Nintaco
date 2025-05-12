package cn.kinlon.emu.input;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Component.POV;
import cn.kinlon.emu.App;
import cn.kinlon.emu.AppMode;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.gui.Int;
import cn.kinlon.emu.gui.image.CursorType;
import cn.kinlon.emu.input.familybasic.keyboard.KeyboardPaster;
import cn.kinlon.emu.input.gamepad.GamepadMapper;
import cn.kinlon.emu.input.multitap.Famicom4PlayersAdapterMapper;
import cn.kinlon.emu.input.multitap.NESFourScoreMapper;
import cn.kinlon.emu.mappers.nintendo.vs.VsGame;
import cn.kinlon.emu.preferences.AppPrefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.kinlon.emu.files.FileUtil.getWorkingDirectory;
import static cn.kinlon.emu.input.ConsoleType.NES;
import static cn.kinlon.emu.input.ConsoleType.VsDualSystem;
import static cn.kinlon.emu.input.InputDevices.*;
import static cn.kinlon.emu.utils.CollectionsUtil.addElement;

public final class InputUtil {

    private static final int POV_UP_LEFT = (int) (8 * POV.UP_LEFT);
    private static final int POV_UP = (int) (8 * POV.UP);
    private static final int POV_UP_RIGHT = (int) (8 * POV.UP_RIGHT);
    private static final int POV_RIGHT = (int) (8 * POV.RIGHT);
    private static final int POV_DOWN_RIGHT = (int) (8 * POV.DOWN_RIGHT);
    private static final int POV_DOWN = (int) (8 * POV.DOWN);
    private static final int POV_DOWN_LEFT = (int) (8 * POV.DOWN_LEFT);
    private static final int POV_LEFT = (int) (8 * POV.LEFT);

    private static final int MAX_EVENTS = 0x2000;
    private static final Event event = new Event();
    private static final ControllerButton controllerButton
            = new ControllerButton();
    private static final Map<ControllerButton, Int> pressedButtons
            = new HashMap<>();
    private static final int[] pressedValues = new int[256];
    private static final Map<InputDeviceID, ControllerQueue> controllerQueuesMap
            = new HashMap<>();

    private static InputDeviceID defaultKeyboard;
    private static InputDeviceID defaultMouse;
    private static ControllerQueue[] controllerQueues;
    private static int presses;

    private static ControllerButton[][][] buttonCombos;

    private static volatile boolean inputDisabled;
    private static volatile boolean rewindTimeDisabled;
    private static volatile Ports ports = Ports.DEFAULTS;
    private static volatile DeviceDescriptor[] devices;
    private static volatile DeviceMapper[] deviceMappers;
    private static volatile int consoleType;
    private static volatile int[] portIndices;
    private static volatile int[][] portDeviceOverrides;
    private static volatile boolean multitapOverride;
    private static volatile int consoleOverride = -1;
    private static volatile int mouseCoordinates;
    private static volatile int buttons;
    private static volatile int vsProtectionMask;
    private static volatile boolean vsSystem;
    private static volatile boolean swapControllers;
    private static volatile boolean zapperGame;
    private static volatile float mouseDeltaX;
    private static volatile float mouseDeltaY;
    private static volatile int mouseDeltaWheel;
    private static volatile String barcode;
    private static volatile boolean rewindTimeDisabledByKeyboard;

    private static volatile KeyboardPaster familyBasicTypePaster;

    private static volatile OtherInput[] otherInputs;

    private InputUtil() {
    }

    public static void init() {
        System.setProperty("net.java.games.input.librarypath",
                getWorkingDirectory("lib", "native"));

        final String osName = System.getProperty("os.name");
        if (osName != null && osName.trim().toLowerCase().startsWith("win")) {
            System.setProperty("jinput.useDefaultPlugin", "false");
            System.setProperty("net.java.games.input.useDefaultPlugin", "false");
            System.setProperty("jinput.plugins", "");
            System.setProperty("net.java.games.input.plugins",
                    "net.java.games.input.DirectInputEnvironmentPlugin");
        }

        handleSettingsChange();
    }

    private static String createID(final Controller controller) {
        return String.format("%s %s %d", controller.getType(), controller.getName(),
                controller.getComponents().length);
    }

    private static String createDescription(final Controller controller,
                                            final int index) {
        final Controller.Type type = controller.getType();
        if (type == Controller.Type.STICK || type == Controller.Type.GAMEPAD) {
            return String.format("Joy %d", index);
        } else if (type == Controller.Type.MOUSE) {
            return "Mouse";
        } else {
            return "";
        }
    }

    private static boolean isValidController(final Controller controller) {
        final Controller.Type type = controller.getType();
        return type == Controller.Type.STICK
                || type == Controller.Type.GAMEPAD
                || type == Controller.Type.KEYBOARD
                || type == Controller.Type.MOUSE;
    }

    public static InputDeviceID getDefaultKeyboard() {
        return defaultKeyboard;
    }

    public static InputDeviceID getDefaultMouse() {
        return defaultMouse;
    }

    public static float getMouseDeltaX() {
        final float value = mouseDeltaX;
        mouseDeltaX = 0;
        return inputDisabled ? 0 : value;
    }

    public static float getMouseDeltaY() {
        final float value = mouseDeltaY;
        mouseDeltaY = 0;
        return inputDisabled ? 0 : value;
    }

    public static int getMouseDeltaWheel() {
        if (mouseDeltaWheel > 0) {
            mouseDeltaWheel--;
            return inputDisabled ? 0 : 1;
        } else if (mouseDeltaWheel < 0) {
            mouseDeltaWheel++;
            return inputDisabled ? 0 : -1;
        } else {
            return 0;
        }
    }

    public static String getBarcode() {
        final String value = barcode;
        barcode = null;
        return value; // inputDisabled does not apply
    }

    private static void addControllerButtons(
            final ControllerQueue controllerQueue, final String buttonName) {
        pressedButtons.put(new ControllerButton(controllerQueue, -1, buttonName),
                new Int());
        pressedButtons.put(new ControllerButton(controllerQueue, 1, buttonName),
                new Int());
    }

    private static void addControllerButtons(
            final ControllerQueue controllerQueue, final Controller controller) {
        for (final Component component : controller.getComponents()) {
            addControllerButtons(controllerQueue,
                    component.getIdentifier().getName());
        }
    }

    public static void initControllers() {

        defaultKeyboard = null;
        defaultMouse = null;
        pressedButtons.clear();
        controllerQueuesMap.clear();

        final Map<String, List<InputDeviceID>> deviceMap = new HashMap<>();
        final Controller[] cs = ControllerEnvironment.getDefaultEnvironment()
                .getControllers();
        final List<ControllerQueue> qs = new ArrayList<>();

        for (int i = cs.length - 1; i >= 0; i--) {
            final Controller controller = cs[i];
            if (isValidController(controller)) {
                final String id = createID(controller);
                List<InputDeviceID> inputDeviceIds = deviceMap.get(id);
                if (inputDeviceIds == null) {
                    inputDeviceIds = new ArrayList<>();
                    deviceMap.put(id, inputDeviceIds);
                }
                final int index = inputDeviceIds.size();
                final InputDeviceID inputDeviceID = new InputDeviceID(index, id,
                        createDescription(controller, index));
                inputDeviceIds.add(inputDeviceID);
                if (defaultKeyboard == null
                        && controller.getType() == Controller.Type.KEYBOARD) {
                    defaultKeyboard = inputDeviceID;
                }
                if (defaultMouse == null
                        && controller.getType() == Controller.Type.MOUSE) {
                    defaultMouse = inputDeviceID;
                }
                final ControllerQueue controllerQueue = new ControllerQueue(i,
                        controller, controller.getEventQueue(), inputDeviceID);
                controllerQueuesMap.put(inputDeviceID, controllerQueue);
                qs.add(controllerQueue);
                addControllerButtons(controllerQueue, controller);
            }
        }

        controllerQueues = new ControllerQueue[qs.size()];
        qs.toArray(controllerQueues);

        clearEventQueues();
    }

    public static boolean isInputDisabled() {
        return inputDisabled;
    }

    public static void setInputDisabled(final boolean inputDisabled) {
        InputUtil.inputDisabled = inputDisabled;
    }

    public static void setRewindTimeDisabled(final boolean rewindTimeDisabled) {
        InputUtil.rewindTimeDisabled = rewindTimeDisabled;
    }

    public static void setVsGame(final VsGame vsGame) {
        if (vsGame == null) {
            InputUtil.vsSystem = false;
            InputUtil.swapControllers = false;
            InputUtil.zapperGame = false;
            InputUtil.vsProtectionMask = 0;
        } else {
            InputUtil.vsSystem = true;
            InputUtil.swapControllers = vsGame.isSwapControllers();
            InputUtil.zapperGame = vsGame.isZapperGame();
            InputUtil.vsProtectionMask = vsGame.isProtected() ? 0x08080808 : 0;
        }
    }

    public static int getMouseCoordinates() {
        return inputDisabled ? 0xFFFF : mouseCoordinates;
    }

    public static void setMouseCoordinates(final int mouseCoordinates) {
        InputUtil.mouseCoordinates = mouseCoordinates & 0xFFFF;
    }

    public static void clearEventQueues() {
        mouseDeltaX = mouseDeltaY = mouseDeltaWheel = 0;
        if (controllerQueues == null) {
            return;
        }
        for (int i = controllerQueues.length - 1; i >= 0; i--) {
            final ControllerQueue controllerQueue = controllerQueues[i];
            if (controllerQueue == null) {
                continue;
            }
            final Controller controller = controllerQueue.controller;
            if (controller.poll()) {
                final EventQueue queue = controllerQueue.getEventQueue();
                while (queue.getNextEvent(event)) ;
            } else {
                controllerQueues[i] = null;
            }
        }
    }

    private static Ports adjustNetplayServerPorts(final Ports ports) {
        final int[][] portDevices = ports.getPortDevices();

        outer:
        if (portDevices.length == 2 || portDevices.length == 4) {
            for (int i = portDevices.length - 1; i >= 0; i--) {
                if (!isGamepad(portDevices[i][1])) {
                    break outer;
                }
            }
            return ports;
        }

        final List<Integer> gamepads = new ArrayList<>();
        gamepads.add(Gamepad1);
        gamepads.add(Gamepad2);
        gamepads.add(Gamepad3);
        gamepads.add(Gamepad4);

        final Map<Integer, Integer> map = new HashMap<>();
        for (final int[] entry : portDevices) {
            final int portIndex = entry[0];
            final int inputDevice = entry[1];
            gamepads.remove(Integer.valueOf(inputDevice));
            if (portIndex != Ports.ExpansionPort) {
                map.put(portIndex, inputDevice);
            }
        }

        final int[][] devices = new int[ports.isMultitap() ? 4 : 2][2];
        for (int i = 0; i < devices.length; i++) {
            devices[i][0] = i;
            final Integer inputDevice = map.get(i);
            devices[i][1] = (inputDevice == null || !isGamepad(inputDevice))
                    ? gamepads.remove(0) : inputDevice;
        }

        return new Ports(devices, ports.isMultitap(), ports.getConsoleType());
    }

    public synchronized static void handleSettingsChange() {
        handleSettingsChange(null);
    }

    private static void handleSettingsChange(Ports ports) {

//    App.clearRewindTime();

        initControllers();

        final Inputs inputs = AppPrefs.getInstance().getInputs();
        if (ports == null) {
            ports = inputs.getPorts();
        }
        
        if (App.getAppMode() == AppMode.NetplayServer) {
            ports = adjustNetplayServerPorts(ports);
        }

        // portDeviceOverrides initialize the DeviceDescriptors and conditionally
        // the DeviceMapper
        final int[][] portDevices = portDeviceOverrides != null
                ? portDeviceOverrides : ports.getPortDevices();
        final ControllerButton[][][] combos
                = new ControllerButton[portDevices.length][][];
        final List<DeviceConfig> deviceConfigs = inputs.getDeviceConfigs();
        final DeviceDescriptor[] deviceDescriptors
                = new DeviceDescriptor[portDevices.length];
        final int[] portNumbers = new int[portDevices.length];
        DeviceMapper[] mappers = new DeviceMapper[portDevices.length];
        final boolean usesZapper = Ports.hasDevice(portDevices, Zapper)
                || Ports.hasDevice(portDevices, BandaiHyperShot);
        final boolean usesMouse = Ports.hasDevice(portDevices, HoriTrack)
                || Ports.hasDevice(portDevices, SnesMouse)
                || Ports.hasDevice(portDevices, Subor)
                || Ports.hasDevice(portDevices, UForce);
        {
            int i = 0;
            for (final int[] entry : portDevices) {
                int portIndex = entry[0];
                final int inputDevice = entry[1];
                if (inputDevice >= 0) {
                    if (usesZapper && App.isVsUniSystem()) {
                        portIndex ^= 1;
                    }
                    final List<ButtonMapping> mapping = deviceConfigs.get(inputDevice)
                            .getButtonMappings();
                    if (mapping != null) {
                        combos[i] = new ControllerButton[mapping.size()][];
                        for (int j = mapping.size() - 1; j >= 0; j--) {
                            combos[i][j] = createButtonsCombo(mapping.get(j));
                        }
                    } else {
                        combos[i] = new ControllerButton[0][0];
                    }
                } else {
                    combos[i] = new ControllerButton[0][0];
                }
                portNumbers[i] = portIndex;
                mappers[i] = DeviceMapper.createDeviceMapper(portIndex, inputDevice);
                deviceDescriptors[i] = DeviceDescriptor.getDescriptor(inputDevice);
                i++;
            }
        }

        if (consoleOverride >= 0) {
            // Netplay always sets up 2 or 4 Gamepad DeviceMappers and the 
            // DeviceDescriptors are determined by the Netplay Client settings.
            mappers = createDeviceMappers(multitapOverride, consoleOverride);
        } else if (ports.isMultitap()) {
            mappers = createDeviceMappers(true, ports.getConsoleType());
        }

        familyBasicTypePaster = null;
        consoleType = ports.getConsoleType();
        portIndices = portNumbers;
        devices = deviceDescriptors;
        buttonCombos = combos;
        deviceMappers = mappers;
        InputUtil.ports = new Ports(portDevices, ports.isMultitap(), consoleType);

        if (usesZapper) {
            setImagePaneMouseCursor(inputs.isShowZapperCrosshairs()
                    ? CursorType.Crosshairs : CursorType.Default);
        } else if (usesMouse) {
            setImagePaneMouseCursor(inputs.isHideMouseCursor()
                    ? CursorType.Blank : CursorType.Default);
        } else {
            setImagePaneMouseCursor(CursorType.Default);
        }
        final boolean keyboard = Ports.hasDevice(portDevices, Keyboard)
                || Ports.hasDevice(portDevices, TransformerKeyboard)
                || Ports.hasDevice(portDevices, DoremikkoKeyboard)
                || Ports.hasDevice(portDevices, Subor)
                || Ports.hasDevice(portDevices, DongdaPEC586Keyboard);
        App.getImageFrame().setKeyEventsEnabled(!keyboard);
        rewindTimeDisabledByKeyboard = keyboard
                && inputs.isDisableKeyboardRewindTime();

        for (int i = DevicesCount - 1; i >= 0; i--) {
            DeviceDescriptor.getDescriptor(i).handleSettingsChange(inputs);
        }
    }

    private static DeviceMapper[] createDeviceMappers(final boolean multitap,
                                                      final int console) {
        final DeviceMapper[] mappers;
        if (multitap) {
            if (console == NES) {
                mappers = new DeviceMapper[]{
                        new NESFourScoreMapper(0),
                        new NESFourScoreMapper(1),
                };
            } else {
                mappers = new DeviceMapper[]{
                        new Famicom4PlayersAdapterMapper(0),
                        new Famicom4PlayersAdapterMapper(1),
                };
            }
        } else if (console == VsDualSystem) {
            mappers = new DeviceMapper[]{
                    new GamepadMapper(0),
                    new GamepadMapper(1),
                    new GamepadMapper(2),
                    new GamepadMapper(3),
            };
        } else {
            mappers = new DeviceMapper[]{
                    new GamepadMapper(0),
                    new GamepadMapper(1),
            };
        }
        return mappers;
    }

    private static void setImagePaneMouseCursor(final CursorType cursorType) {
        App.getImageFrame().getImagePane().setCursorType(cursorType);
    }

    private static ControllerButton[] createButtonsCombo(
            final ButtonMapping buttonMapping) {

        final ButtonID[] buttonIds = buttonMapping.getButtonIds();
        final List<ControllerButton> controllerButtons = new ArrayList<>();
        for (int i = buttonIds.length - 1; i >= 0; i--) {
            final ButtonID buttonID = buttonIds[i];
            final ControllerQueue controllerQueue
                    = controllerQueuesMap.get(buttonID.getDevice());
            if (controllerQueue != null) {
                controllerButtons.add(new ControllerButton(controllerQueue,
                        buttonID.getValue(), buttonID.getName()));
            }
        }

        final ControllerButton[] combo
                = new ControllerButton[controllerButtons.size()];
        controllerButtons.toArray(combo);
        return combo;
    }

    private static void updatePressedButtons() {

        if (controllerQueues == null) {
            return;
        }

        for (int i = controllerQueues.length - 1; i >= 0; i--) {
            final ControllerQueue controllerQueue = controllerQueues[i];
            if (controllerQueue == null) {
                continue;
            }
            final Controller controller = controllerQueue.controller;
            if (controller.poll()) {
                final boolean mouse = controller.getType() == Controller.Type.MOUSE;
                final EventQueue queue = controllerQueue.eventQueue;
                for (int j = 0; j < MAX_EVENTS && queue.getNextEvent(event); j++) {
                    final Component.Identifier id = event.getComponent().getIdentifier();
                    if (mouse) {
                        if (id == Axis.X) {
                            mouseDeltaX += event.getValue();
                        } else if (id == Axis.Y) {
                            mouseDeltaY += event.getValue();
                        } else if (id == Axis.Z) {
                            final float value = event.getValue();
                            if (value > 0) {
                                mouseDeltaWheel++;
                            } else if (value < 0) {
                                mouseDeltaWheel--;
                            }
                        } else {
                            handleButton(controllerQueue, getButtonValue(), id.getName());
                        }
                    } else if (id == Axis.POV) {
                        switch ((int) (8 * (event.getValue()
                                - event.getComponent().getDeadZone()))) {
                            case POV_UP_LEFT:
                                handleButton(controllerQueue, -1, "y");
                                handleButton(controllerQueue, -1, "x");
                                break;
                            case POV_UP:
                                handleButton(controllerQueue, -1, "y");
                                handleButton(controllerQueue, 0, "x");
                                break;
                            case POV_UP_RIGHT:
                                handleButton(controllerQueue, -1, "y");
                                handleButton(controllerQueue, 1, "x");
                                break;
                            case POV_RIGHT:
                                handleButton(controllerQueue, 0, "y");
                                handleButton(controllerQueue, 1, "x");
                                break;
                            case POV_DOWN_RIGHT:
                                handleButton(controllerQueue, 1, "y");
                                handleButton(controllerQueue, 1, "x");
                                break;
                            case POV_DOWN:
                                handleButton(controllerQueue, 1, "y");
                                handleButton(controllerQueue, 0, "x");
                                break;
                            case POV_DOWN_LEFT:
                                handleButton(controllerQueue, 1, "y");
                                handleButton(controllerQueue, -1, "x");
                                break;
                            case POV_LEFT:
                                handleButton(controllerQueue, 0, "y");
                                handleButton(controllerQueue, -1, "x");
                                break;
                            default:
                                handleButton(controllerQueue, 0, "y");
                                handleButton(controllerQueue, 0, "x");
                                break;
                        }
                    } else {
                        handleButton(controllerQueue, getButtonValue(), id.getName());
                    }
                }
            } else {
                controllerQueues[i] = null;
            }
        }
    }

    private static void setPressed(final ControllerQueue controllerQueue,
                                   final int value, final String name, final int pressedValue) {
        final Int b = pressedButtons.get(controllerButton.setAll(controllerQueue,
                value, name));
        if (b != null) {
            b.setValue(pressedValue);
        }
    }

    private static void handleButton(final ControllerQueue controllerQueue,
                                     final int value, final String name) {

        if (value == 0) {
            setPressed(controllerQueue, -1, name, 0);
            setPressed(controllerQueue, 1, name, 0);
        } else {
            if (++presses == 0) {
                presses = 1;
            }
            setPressed(controllerQueue, value, name, presses);
        }
    }

    private static void updateButtons(final boolean play, final Machine machine) {

        int bits = 0;

        for (int i = buttonCombos.length - 1; i >= 0; i--) {
            final ControllerButton[][] combos = buttonCombos[i];
            outer:
            for (int j = combos.length - 1; j >= 0; j--) {
                pressedValues[j] = 0;
                if (!inputDisabled) {
                    final ControllerButton[] combo = combos[j];
                    int maxValue = Integer.MIN_VALUE;
                    if (combo != null && combo.length > 0) {
                        for (int k = combo.length - 1; k >= 0; k--) {
                            final Int b = pressedButtons.get(combo[k]);
                            if (b == null || b.value == 0) {
                                continue outer;
                            } else if (b.value > maxValue) {
                                maxValue = b.value;
                            }
                        }
                        pressedValues[j] = maxValue;
                    }
                }
            }

            final KeyboardPaster typePaster = familyBasicTypePaster;
            if (typePaster != null && devices[i] == DeviceDescriptor.Keyboard) {
                bits = typePaster.type(bits, consoleType, pressedValues);
                if (typePaster.isFinished()) {
                    familyBasicTypePaster = null;
                }
            } else {
                bits = devices[i].setButtonBits(bits, consoleType, portIndices[i],
                        pressedValues);
            }
        }

        if (vsSystem) {
            if (zapperGame) {
                bits = (0xFFFFF3FF & bits) | ((0x0400 & bits) << 1)
                        | ((0x0800 & bits) >> 1);
                buttons = swapControllers ? ((0xFFFF0C0C & bits) | ((0xF3 & bits) << 8)
                        | ((0xF300 & bits) >> 8)) : bits;
            } else {
                bits = (0xF3F3F3F3 & bits) | ((0x04040404 & bits) << 1)
                        | ((0x08080808 & bits) >> 1) | vsProtectionMask;
                buttons = swapControllers ? ((0x0C0C0C0C & bits)
                        | ((0x00F300F3 & bits) << 8)
                        | ((0xF300F300 & bits) >> 8)) : bits;
            }
        } else {
            buttons = bits;
        }

        if (rewindTimeDisabled || rewindTimeDisabledByKeyboard) {
            App.clearRewindTime();
            App.clearHighSpeed();
        } else {
            App.updateHighSpeed();
        }
    }

    public static void pollControllers(final Machine machine) {
        if (machine != null) {
            machine.getPPU().setZapper(null);
            final DeviceMapper[] mappers = deviceMappers;
            for (int i = mappers.length - 1; i >= 0; i--) {
                mappers[i].setMachine(machine);
            }
            machine.getMapper().setDeviceMappers(mappers);
            machine.getMapper().setPorts(ports);
        }
        updatePressedButtons();
        updateButtons(true, machine);
    }

    public static void pollControllers(final List<ButtonID> buttonIds) {

        if (controllerQueues == null) {
            return;
        }

        buttonIds.clear();
        for (int i = controllerQueues.length - 1; i >= 0; i--) {
            final ControllerQueue controllerQueue = controllerQueues[i];
            if (controllerQueue == null) {
                continue;
            }
            final Controller controller = controllerQueue.controller;
            if (controller.poll()) {
                final EventQueue queue = controllerQueue.eventQueue;
                long lastNanos = -1;
                ButtonID buttonID = null;
                while (queue.getNextEvent(event)) {
                    final Component component = event.getComponent();
                    if (!isMouseAxis(controller, component)) {
                        final int buttonValue;
                        final String buttonName;
                        if (component.getIdentifier() == Axis.POV) {
                            switch ((int) (8 * (event.getValue()
                                    - event.getComponent().getDeadZone()))) {
                                case POV_UP:
                                    buttonName = "y";
                                    buttonValue = -1;
                                    break;
                                case POV_RIGHT:
                                    buttonName = "x";
                                    buttonValue = 1;
                                    break;
                                case POV_DOWN:
                                    buttonName = "y";
                                    buttonValue = 1;
                                    break;
                                case POV_LEFT:
                                    buttonName = "x";
                                    buttonValue = -1;
                                    break;
                                default:
                                    buttonName = "";
                                    buttonValue = 0;
                                    break;
                            }
                        } else {
                            buttonName = component.getIdentifier().getName();
                            buttonValue = getButtonValue();
                        }
                        final ButtonID id = new ButtonID(controllerQueue.getInputDeviceID(),
                                buttonName, buttonValue);

                        if (buttonValue == 0) {
                            buttonIds.add(id);
                        } else if (lastNanos == event.getNanos()) {
                            buttonID = id;
                        } else {
                            if (buttonID != null) {
                                buttonIds.add(buttonID);
                            }
                            buttonID = id;
                        }
                        lastNanos = event.getNanos();
                    }
                }
                if (buttonID != null) {
                    buttonIds.add(buttonID);
                }
            } else {
                controllerQueues[i] = null;
            }
        }
    }

    public static void setPortDeviceOverrides(
            final int[][] portDeviceOverrides) {
        InputUtil.portDeviceOverrides = portDeviceOverrides;
        handleSettingsChange();
    }

    public static void clearPortDeviceOverrides() {
        setPortDeviceOverrides(null);
    }

    private static boolean isMouseAxis(final Controller controller,
                                       final Component component) {
        return controller.getType() == Controller.Type.MOUSE
                && component.getIdentifier() instanceof Axis;
    }

    public static int getButtons() {
        return buttons; // inputDisabled applied in updateButtons()
    }

    public static int getButtonValue() {
        float value = event.getValue() - event.getComponent().getDeadZone();
        if (value > 0.5f) {
            return 1;
        } else if (value < -0.5f) {
            return -1;
        } else {
            return 0;
        }
    }

    public static void addOtherInput(final OtherInput otherInput) {
        otherInputs = addElement(OtherInput.class, otherInputs, otherInput);
    }

    public static OtherInput[] getOtherInputs() {
        final OtherInput[] others = otherInputs;
        otherInputs = null;
        return others; // Not affected by inputDisabled
    }
}