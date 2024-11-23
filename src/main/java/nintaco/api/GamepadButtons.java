package nintaco.api;

/**
 * Contains a set of constants used by the {@link API#readGamepad(int, int)
 * API.readGamepad()} and {@link API#writeGamepad(int, int, boolean)
 * API.writeGamepad()} methods, each referring to different button of the
 * standard controller.
 */
public interface GamepadButtons {

    /**
     * The A button.
     */
    int A = 0;

    /**
     * The B button.
     */
    int B = 1;

    /**
     * The Select button.
     */
    int Select = 2;

    /**
     * The Start button.
     */
    int Start = 3;

    /**
     * The Up button on the D-pad.
     */
    int Up = 4;

    /**
     * The Down button on the D-pad.
     */
    int Down = 5;

    /**
     * The Left button on the D-pad.
     */
    int Left = 6;

    /**
     * The Right button on the D-pad.
     */
    int Right = 7;
}