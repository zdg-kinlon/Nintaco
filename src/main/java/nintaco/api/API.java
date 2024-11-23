package nintaco.api;

/**
 * <p>The Program API interface. This API provides programmatic control of the
 * emulator at a very granular level. It supports direct access to CPU/PPU/APU
 * Memory and registers, to controller input and to save states. And, it offers
 * a multitude of listeners that enable programs to tap into emulation
 * events.</p>
 *
 * <h2>Running Programs</h2>
 *
 * <p>Programs written in Java can be loaded directly into the emulator for
 * execution. Programs written other languages can control the emulator through
 * the Remote API, which uses an internal socket connection for interprocess
 * communication.</p>
 *
 * <p>Programs developed to control the emulator are written as if they were
 * standalone applications; the entrypoint of the program is the {@code main()}
 * method. To load the program into the emulator, it must be compiled and
 * bundled into a JAR. From the Run Program dialog, enter the path to the JAR
 * file or press the Find JAR button to browse to it. The Load JAR button
 * scans the JAR for classes containing {@code main()} and it lists them in
 * a combo box. If the JAR manifest specifies the application's entrypoint,
 * the associated class will appear first in the list. If the JAR is modified
 * during development, press the Load JAR button again to import the changes. To
 * run the JAR, select the main class, enter any arguments to pass to
 * {@code main()}, and press the Run button. The standard output and error
 * streams are redirected to a text area within the Run Program dialog. To
 * terminate the program, press Stop.</p>
 *
 * <h2>Getting the API</h2>
 *
 * <p>As far as a program is concerned, the API is a singleton. Classes
 * interested in using the API can declare an object constant via the following
 * code to make the reference available to all methods.</p>
 *
 * <p>{@code private final API api = ApiSource.getAPI();}</p>
 *
 * <h2>Listeners</h2>
 *
 * <p>The API starts out as disabled and while disabled only the add/remove
 * listener methods work. After adding listeners, a program calls {@link #run()}
 * to activate the API; it signals that everything is setup and the program is
 * ready to receive events. Listeners are cached and they rarely need to be
 * removed. In the event that the API is temporarily disabled, listeners
 * do <u>not</u> need to be re-added. They are automatically removed on program
 * shutdown. And, most of the API methods that modify internal states do
 * <u>not</u> have the side effect of triggering listeners. For example, a
 * program that receives events when a region of CPU memory is updated can
 * modify the same region from the event listener without creating infinite
 * recursion.</p>
 *
 * <p>The easiest way for a program to do something once-per-frame is within
 * a {@link FrameListener}, which is called back immediately after a full frame
 * was rendered, but just before the frame is displayed to the user.
 * {@link ScanlineListener} works in a similar way, but it is invoked after a
 * specified scanline was rendered. {@link ScanlineCycleListener} takes that one
 * step further and responds to a specified dot. A program can manipulate
 * controller input from a {@link ControllersListener}, which is called back
 * immediately after the controllers were probed for data, but just
 * before the probed data is exposed to the machine. {@link AccessPointListener}
 * is triggered by a specified CPU Memory read or write, or instruction
 * execution point and {@link SpriteZeroListener} is triggered by sprite zero
 * hits. Finally, {@link ActivateListener}, {@link DeactivateListener},
 * {@link StatusListener} and {@link StopListener} respond to API enabled
 * events, API disabled events, status message events and Stop button events,
 * respectively.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>The Program API is single-threaded. After the {@code main()} thread
 * calls {@link API#run()}, the only thread that can safely invoke API methods
 * is the one that executes the listeners. While a listener is executing,
 * the emulator is effectively frozen; listeners need to return in a timely
 * manner to avoid slowing down emulation. Programs can spawn off additional
 * threads to perform parallel computations; however, the results of those
 * computations should be exposed to and used from the listeners to act on the
 * API.</p>
 *
 * <h2>The Remote API</h2>
 *
 * <p>The Remote API is exposed from the Start Program Server dialog, which
 * accepts a local hostname and port to listen on. The Remote API provides a
 * way for programs written in other languages to control the emulator. It uses
 * a socket connection for interprocess communication enabling external programs
 * to run on the same box as the emulator or a completely different machine.</p>
 *
 * <p>For external Java programs, add a line similar to the following to the
 * {@code main()} method and use the API in the same way as described above.</p>
 *
 * <p>{@code ApiSource.initRemoteAPI("localhost", 9999);}</p>
 *
 * <p>In the Remote API, {@code run()} never returns. Instead, it enters an
 * infinite loop that maintains the network connection. Add a
 * {@link StatusListener} to monitor the loop.</p>
 *
 * <p>Implementations of the Remote API for other languages is not discussed
 * here.</p>
 */
public interface API {

    /**
     * <p>Adds an {@link ActivateListener}. This listener is called back when the
     * API is enabled, providing an opportunity to perform initialization tasks.
     * It is invoked immediately after the {@link #run() run} method is called
     * if the emulator is actively running a file; otherwise, it will be invoked
     * when a file is opened. The Remote API is not enabled until a socket
     * connection is established.</p>
     *
     * <p>The API starts out as disabled and while disabled only the add/remove
     * listener methods work. Ideally, all listeners should be added prior to
     * calling {@link #run() run()}. Listeners are cached; they do not
     * need to be re-added in the event that the API is temporarily disabled.</p>
     *
     * @param listener The {@link ActivateListener} to notify.
     * @see #addDeactivateListener(nintaco.api.DeactivateListener)
     * @see #removeActivateListener(nintaco.api.ActivateListener)
     */
    void addActivateListener(ActivateListener listener);

    /**
     * Removes an {@link ActivateListener}.
     *
     * @param listener The {@link ActivateListener} to remove.
     * @see #addActivateListener(nintaco.api.ActivateListener)
     */
    void removeActivateListener(ActivateListener listener);

    /**
     * <p>Adds an {@link DeactivateListener}. This listener is called back when
     * the API transitions from enabled to disabled. It will be invoked when a
     * file that is actively running in the emulator is closed. The Remote API
     * is disabled if the socket connection is disrupted.</p>
     *
     * <p>The API starts out as disabled and while disabled only the add/remove
     * listener methods work. Ideally, all listeners should be added prior to
     * calling {@link #run() run()}. Listeners are cached; they do not
     * need to be re-added in the event that the API is temporarily disabled.</p>
     *
     * @param listener The {@link DeactivateListener} to notify.
     * @see #addActivateListener(nintaco.api.ActivateListener)
     * @see #removeDeactivateListener(nintaco.api.DeactivateListener)
     */
    void addDeactivateListener(DeactivateListener listener);

    /**
     * Removes a {@link DeactivateListener}.
     *
     * @param listener The {@link DeactivateListener} to remove.
     * @see #addDeactivateListener(nintaco.api.DeactivateListener)
     */
    void removeDeactivateListener(DeactivateListener listener);

    /**
     * <p>Adds a {@link StopListener}. This listener is only applicable to
     * programs running locally within the emulator. It is invoked when the Stop
     * button is pressed in the Run Program dialog, providing an opportunity to
     * perform cleanup operations such as terminating spawned threads, closing
     * open streams and freeing allocated resources. However, it is not necessary
     * to remove listeners; all added program listeners will be automatically
     * removed after the {@code StopListener} returns.</p>
     *
     * <p>Pressing the Stop button does not disable the API; it will not trigger
     * {@link DeactivateListener}s and the {@code StopListener} can make API
     * calls.</p>
     *
     * @param listener The {@link StopListener} to notify.
     * @see #removeStopListener(nintaco.api.StopListener)
     */
    void addStopListener(StopListener listener);

    /**
     * Removes a {@link StopListener}.
     *
     * @param listener The {@link StopListener} to remove.
     * @see #addStopListener(nintaco.api.StopListener)
     */
    void removeStopListener(StopListener listener);

    /**
     * <p>Adds an {@link AccessPointListener}, which is invoked when an access
     * point is encountered. An access point is a specified CPU Memory read or
     * write, or instruction execution point.</p>
     *
     * <p>The CPU Memory read and write methods do not trigger access points.
     * Consequentially, they can be called within the listener without causing
     * recursive loops.</p>
     *
     * @param listener        The {@link AccessPointListener} to notify.
     * @param accessPointType One of the {@link AccessPointType} constants,
     *                        indicating the type of access points that triggers the listener.
     * @param address         The CPU Memory address that triggers the listener.
     * @see AccessPointType
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int,
     * int)
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int,
     * int, int)
     */
    void addAccessPointListener(AccessPointListener listener, int accessPointType,
                                int address);

    /**
     * <p>Adds an {@link AccessPointListener}, which is invoked when an access
     * point is encountered. An access point is a specified CPU Memory read or
     * write, or instruction execution point.</p>
     *
     * <p>The CPU Memory read and write methods do not trigger access points.
     * Consequentially, they can be called within the listener without causing
     * recursive loops.</p>
     *
     * @param listener        The {@link AccessPointListener} to notify.
     * @param accessPointType One of the {@link AccessPointType} constants,
     *                        indicating the type of access points that triggers the listener.
     * @param minAddress      The lower bound in the range of CPU Memory addresses
     *                        that triggers the listener. If the range consists of a single memory
     *                        address, use this parameter for that address.
     * @param maxAddress      The upper bound in the range of CPU Memory addresses
     *                        that triggers the listener. If the range consists of a single memory
     *                        address, set the lower and upper bounds to the same value or set this
     *                        parameter to -1 to indicate that it should be ignored.
     * @see AccessPointType
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int)
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int,
     * int, int)
     */
    void addAccessPointListener(AccessPointListener listener, int accessPointType,
                                int minAddress, int maxAddress);

    /**
     * <p>Adds an {@link AccessPointListener}, which is invoked when an access
     * point is encountered. An access point is a specified CPU Memory read or
     * write, or instruction execution point.</p>
     *
     * <p>The CPU Memory read and write methods do not trigger access points.
     * Consequentially, they can be called within the listener without causing
     * recursive loops.</p>
     *
     * @param listener        The {@link AccessPointListener} to notify.
     * @param accessPointType One of the {@link AccessPointType} constants,
     *                        indicating the type of access points that triggers the listener.
     * @param minAddress      The lower bound in the range of CPU Memory addresses
     *                        that triggers the listener. If the range consists of a single memory
     *                        address, use this parameter for that address.
     * @param maxAddress      The upper bound in the range of CPU Memory addresses
     *                        that triggers the listener. If the range consists of a single memory
     *                        address, set the lower and upper bounds to the same value or set this
     *                        parameter to -1 to indicate that it should be ignored.
     * @param bank            For PRG ROM addresses, this parameter specifies the bank that
     *                        triggers the listener. The PRG ROM bank size and access index is
     *                        controlled by the mapper. Use -1 to indicate that all banks can trigger
     *                        the listener.
     * @see AccessPointType
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int)
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int,
     * int)
     */
    void addAccessPointListener(AccessPointListener listener, int accessPointType,
                                int minAddress, int maxAddress, int bank);

    /**
     * Removes an {@link AccessPointListener}.
     *
     * @param listener The {@link AccessPointListener} to remove.
     * @see #addAccessPointListener(nintaco.api.AccessPointListener, int, int,
     * int, int)
     */
    void removeAccessPointListener(AccessPointListener listener);

    /**
     * Adds a {@link ControllersListener}. The listener will be called back
     * immediately after the controllers were probed for data, just
     * before the probed data is exposed to the machine.
     *
     * @param listener The {@link ControllersListener} to notify.
     * @see #removeControllersListener(nintaco.api.ControllersListener)
     */
    void addControllersListener(ControllersListener listener);

    /**
     * Removes a {@link ControllersListener}.
     *
     * @param listener The {@link ControllersListener} to remove.
     * @see #addControllersListener(nintaco.api.ControllersListener)
     */
    void removeControllersListener(ControllersListener listener);

    /**
     * Adds a {@link FrameListener}. The listener will be called back immediately
     * after a full frame was rendered, just before the frame is displayed to the
     * user.
     *
     * @param listener The {@link FrameListener} to notify.
     * @see #removeFrameListener(nintaco.api.FrameListener)
     */
    void addFrameListener(FrameListener listener);

    /**
     * Removes a {@link FrameListener}.
     *
     * @param listener The {@link FrameListener} to remove.
     * @see #addFrameListener(nintaco.api.FrameListener)
     */
    void removeFrameListener(FrameListener listener);

    /**
     * Adds a {@link ScanlineListener} that will be invoked after rendering the
     * specified {@code scanline}. The listener is called back during scanline
     * cycle 321, after the PPU fetches the sprite tile data for the successive
     * scanline and before register <i>v</i> is incremented during the initial
     * background tile fetches.
     *
     * @param listener The {@link ScanlineListener} to notify.
     * @param scanline The associated scanline. Use -1 for the pre-render
     *                 scanline.
     * @see #removeScanlineListener(nintaco.api.ScanlineListener)
     */
    void addScanlineListener(ScanlineListener listener, int scanline);

    /**
     * Removes a {@link ScanlineListener}.
     *
     * @param listener The {@link ScanlineListener} to remove.
     * @see #addScanlineListener(nintaco.api.ScanlineListener, int)
     */
    void removeScanlineListener(ScanlineListener listener);

    /**
     * Adds a {@link ScanlineCycleListener} that will be invoked at the specified
     * {@code scanline} and {@code scanlineCycle} values.
     *
     * @param listener      The {@link ScanlineCycleListener} to notify.
     * @param scanline      The associated scanline. Use -1 for the pre-render
     *                      scanline.
     * @param scanlineCycle The associated scanline cycle (the dot number), a
     *                      value in the range [0, 340].
     * @see #removeScanlineCycleListener(nintaco.api.ScanlineCycleListener)
     */
    void addScanlineCycleListener(ScanlineCycleListener listener, int scanline,
                                  int scanlineCycle);

    /**
     * Removes a {@link ScanlineCycleListener}.
     *
     * @param listener The {@link ScanlineCycleListener} to remove.
     * @see #addScanlineCycleListener(nintaco.api.ScanlineCycleListener, int,
     * int)
     */
    void removeScanlineCycleListener(ScanlineCycleListener listener);

    /**
     * Adds a {@link SpriteZeroListener} that will be called back immediately
     * after the sprite zero flag is set.
     *
     * @param listener The {@link SpriteZeroListener} to notify.
     * @see #removeSpriteZeroListener(nintaco.api.SpriteZeroListener)
     */
    void addSpriteZeroListener(SpriteZeroListener listener);

    /**
     * Removes a {@link SpriteZeroListener}.
     *
     * @param listener The {@link SpriteZeroListener} to remove.
     * @see #addSpriteZeroListener(nintaco.api.SpriteZeroListener)
     */
    void removeSpriteZeroListener(SpriteZeroListener listener);

    /**
     * Adds a {@link StatusListener}. The listener is used to report status
     * change messages. For instance, in the Remote API, it will be called back
     * when the network connection is established or disrupted.
     *
     * @param listener The {@link StatusListener} to notify.
     * @see #removeStatusListener(nintaco.api.StatusListener)
     */
    void addStatusListener(StatusListener listener);

    /**
     * Removes a {@link StatusListener}.
     *
     * @param listener The {@link StatusListener} to remove.
     * @see #addStatusListener(nintaco.api.StatusListener)
     */
    void removeStatusListener(StatusListener listener);

    /**
     * <p>Activates the API. The API starts out as disabled and while disabled
     * only the add/remove listener methods work. Ideally, all listeners should be
     * added prior to calling this method; {@code run()} provides a way to signal
     * that everything is setup and the program is ready to receive events.
     * Shortly after invoking {@code run()}, the {@link ActivateListener}s
     * will be called back to signal that the API is enabled, if the emulator
     * is actively running a file.</p>
     *
     * <p>In the Remote API, {@code run()} never returns. Rather, it enters
     * an infinite loop that maintains the network connection. Add a
     * {@link StatusListener} to monitor the loop.</p>
     *
     * <p>{@code run()} should only be called once near the start of a program; it
     * does not need to be invoked again in the event that the API is temporarily
     * disabled. Similarly, listeners are cached and they never need to be
     * re-added.</p>
     *
     * @see ActivateListener
     * @see DeactivateListener
     * @see StopListener
     * @see StatusListener
     */
    void run();

    /**
     * Presses the power button twice, once to turn the machine off and a second
     * time to turn it back on again.
     *
     * @see #reset()
     */
    void powerCycle();

    /**
     * Presses the reset button.
     *
     * @see #powerCycle()
     */
    void reset();

    /**
     * Returns the paused mode.
     *
     * @return {@code true} if paused; {@code false} otherwise.
     * @see #setPaused(boolean)
     */
    boolean isPaused();

    /**
     * Pauses or resumes emulation.
     *
     * @param paused {@code true} to pause; {@code false} to resume.
     * @see #isPaused()
     */
    void setPaused(final boolean paused);

    /**
     * Returns the index of the current frame.
     *
     * @return The index of the current frame.
     */
    int getFrameCount();

    /**
     * Returns the contents of CPU register A, the 8-bit accumulator.
     *
     * @return The lower 8 bits contains the value.
     * @see #setA(int)
     */
    int getA();

    /**
     * Stores the specified value within CPU register A, the 8-bit accumulator.
     *
     * @param A Only the lower 8 bits are used.
     * @see #getA()
     */
    void setA(int A);

    /**
     * Returns the contents of CPU register S, the 8-bit stack pointer.
     *
     * @return The lower 8 bits contains the value.
     * @see #setS(int)
     */
    int getS();

    /**
     * Stores the specified value within CPU register S, the 8-bit stack pointer.
     *
     * @param S Only the lower 8 bits are used.
     * @see #getS()
     */
    void setS(int S);

    /**
     * Returns the contents of CPU register PC, the 16-bit program counter.
     *
     * @return The lower 16 bits contains the value.
     * @see #setPC(int)
     */
    int getPC();

    /**
     * Stores the specified value within CPU register PC, the 16-bit program
     * counter.
     *
     * @param PC Only the lower 16 bits are used.
     * @see #getPC()
     */
    void setPC(int PC);

    /**
     * Returns the contents of CPU register X, the 8-bit index register.
     *
     * @return The lower 8 bits contains the value.
     * @see #setX(int)
     */
    int getX();

    /**
     * Stores the specified value within CPU register X, the 8-bit index register.
     *
     * @param X Only the lower 8 bits are used.
     * @see #getX()
     */
    void setX(int X);

    /**
     * Returns the contents of CPU register Y, the 8-bit index register.
     *
     * @return The lower 8 bits contains the value.
     * @see #setY(int)
     */
    int getY();

    /**
     * Stores the specified value within CPU register Y, the 8-bit index register.
     *
     * @param Y Only the lower 8 bits are used.
     * @see #getY()
     */
    void setY(int Y);

    /**
     * Returns the CPU status flag bits. The B flag (bit 4) is always returned as
     * 0. Bit 5 is always returned as 1.
     *
     * @return {@code NV10DIZC}
     * @see #setP(int)
     */
    int getP();

    /**
     * Sets the CPU status flag bits. Bits 4 and 5 are ignored.
     *
     * @param P {@code NV..DIZC}
     * @see #getP()
     */
    void setP(int P);

    /**
     * Returns the CPU negative flag value, indicating if the most significant bit
     * of a prior result was set.
     *
     * @return The negative flag value.
     * @see #setN(boolean)
     */
    boolean isN();

    /**
     * Sets the CPU negative flag.
     *
     * @param N The negative flag value.
     * @see #isN()
     */
    void setN(boolean N);

    /**
     * Returns the CPU overflow flag value, indicating if an arithmetic overflow
     * occurred.
     *
     * @return The overflow flag value.
     * @see #setV(boolean)
     */
    boolean isV();

    /**
     * Sets the CPU overflow flag.
     *
     * @param V The overflow flag value.
     * @see #isV()
     */
    void setV(boolean V);

    /**
     * Returns the CPU decimal mode flag value, indicating whether arithmetic
     * operations are performed in binary ({@code false}) or binary coded decimal
     * ({@code true}).
     *
     * @return The decimal mode flag value.
     * @see #setD(boolean)
     */
    boolean isD();

    /**
     * Sets the CPU decimal mode flag, indicating whether arithmetic
     * operations are performed in binary ({@code false}) or binary coded decimal
     * ({@code true}).
     *
     * @param D The decimal mode flag value.
     * @see #isD()
     */
    void setD(boolean D);

    /**
     * Returns the CPU interrupt mask flag value, indicating whether maskable
     * interrupts are enabled ({@code false}) or disabled ({@code true}).
     *
     * @return The interrupt mask flag value.
     * @see #setI(boolean)
     */
    boolean isI();

    /**
     * Sets the CPU interrupt mask flag value, indicating whether maskable
     * interrupts are enabled ({@code false}) or disabled ({@code true}).
     *
     * @param I The interrupt mask flag value.
     * @see #isI()
     */
    void setI(boolean I);

    /**
     * Returns the CPU zero flag value, indicating if a prior result was 0.
     *
     * @return The zero flag value.
     * @see #setZ(boolean)
     */
    boolean isZ();

    /**
     * Sets the CPU zero flag value.
     *
     * @param Z The zero flag value.
     * @see #isZ()
     */
    void setZ(boolean Z);

    /**
     * Returns the CPU carry flag value that stores the carry/borrow/shift/rotate
     * out/in bit for/from an operation.
     *
     * @return The carry flag value.
     * @see #setC(boolean)
     */
    boolean isC();

    /**
     * Sets the CPU carry flag value.
     *
     * @param C The carry flag value.
     * @see #isC()
     */
    void setC(boolean C);

    /**
     * Returns the contents of PPU register <i>v</i>, the current VRAM address
     * (15 bits).
     *
     * @return The lower 15 bits contains the value.
     * @see #setPPUv(int)
     */
    int getPPUv();

    /**
     * Stores the specified value within PPU register <i>v</i>, the current VRAM
     * address (15 bits).
     *
     * @param v Only the lower 15 bits are used.
     * @see #getPPUv()
     */
    void setPPUv(int v);

    /**
     * Returns the contents of PPU register <i>t</i>, the temporary VRAM address
     * (the 15-bit address of the top-left on-screen tile).
     *
     * @return The lower 15 bits contains the value.
     * @see #setPPUt(int)
     */
    int getPPUt();

    /**
     * Stores the specified value within PPU register <i>t</i>, the temporary VRAM
     * address (the 15-bit address of the top-left on-screen tile).
     *
     * @param t Only the lower 15 bits are used.
     * @see #getPPUt()
     */
    void setPPUt(int t);

    /**
     * Returns the contents of PPU register <i>x</i>, the fine X scroll (3 bits).
     *
     * @return The lower 3 bits contains the value.
     * @see #setPPUx(int)
     */
    int getPPUx();

    /**
     * Stores the specified value within PPU register <i>x</i>, the fine X scroll
     * (3 bits).
     *
     * @param x Only the lower 3 bits are used.
     * @see #getPPUx()
     */
    void setPPUx(int x);

    /**
     * Returns the PPU first/second write toggle flag.
     *
     * @return The write flag value.
     * @see #setPPUw(boolean)
     */
    boolean isPPUw();

    /**
     * Sets the PPU first/second write toggle flag.
     *
     * @param w The write flag value.
     * @see #isPPUw()
     */
    void setPPUw(boolean w);

    /**
     * <p>Returns the horizontal coordinate of the left side of the rectangular
     * region of the nametables surface that is visible to the user. Within each
     * scanline, this method will only function properly after dot 257, i.e.,
     * after the PPU copies all bits related to horizontal position from register
     * <i>t</i> to register <i>v</i>. The easiest way to ensure that is by
     * calling this method from a {@link ScanlineListener} or a
     * {@link ScanlineCycleListener}. The returned value is extracted from
     * registers <i>v</i> and <i>x</i> via the following code:</p>
     *
     * <p>{@code ((v >> 2) & 0x100) | ((v << 3) & 0xF8) | (x & 0x07)}</p>
     *
     * @return The X-coordinate of the camera window.
     * @see #getPPUv()
     * @see #getPPUx()
     * @see #setCameraX(int)
     * @see #setCameraY(int)
     * @see #getCameraY()
     */
    int getCameraX();

    /**
     * Sets the horizontal coordinate of the left side of the rectangular
     * region of the nametables surface that is visible to the user. Within each
     * scanline, this method will only function properly after dot 257, i.e.,
     * after the PPU copies all bits related to horizontal position from register
     * <i>t</i> to register <i>v</i>. The easiest way to ensure that is by
     * calling this method from a {@link ScanlineListener} or a
     * {@link ScanlineCycleListener}. This method updates registers <i>v</i> and
     * <i>x</i> via the following code:
     * <pre>{@code
     * x = cameraX & 0x07;
     * v = (v & 0x7BE0) | ((cameraX & 0x100) << 2) | ((cameraX & 0xF8) >> 3);
     * }</pre>
     *
     * @param cameraX The X-coordinate of the camera window.
     * @see #getCameraX()
     * @see #getCameraY()
     * @see #setCameraY(int)
     * @see #setPPUv(int)
     * @see #setPPUx(int)
     */
    void setCameraX(int cameraX);

    /**
     * <p>Returns the vertical coordinate of the top of the rectangular region of
     * the nametables surface that is visible to the user. Within each scanline,
     * this method will only function properly after dot 257, i.e., after the PPU
     * copies all bits related to horizontal position from register <i>t</i> to
     * register <i>v</i>. The easiest way to ensure that is by calling this method
     * from a {@link ScanlineListener} or a {@link ScanlineCycleListener}. The
     * returned value is extracted from registers <i>v</i> and <i>x</i> via the
     * following code:</p>
     *
     * <p>{@code ((v >> 2) & 0x100) | ((v << 3) & 0xF8) | (x & 0x07)}</p>
     *
     * @return The Y-coordinate of the camera window.
     * @see #setCameraY(int)
     * @see #setCameraX(int)
     * @see #getCameraX()
     * @see #getPPUv()
     * @see #getPPUx()
     */
    int getCameraY();

    /**
     * Sets the vertical coordinate of the top of the rectangular region of the
     * nametables surface that is visible to the user. Within each scanline, this
     * method will only function properly after dot 257, i.e., after the PPU
     * copies all bits related to horizontal position from register <i>t</i> to
     * register <i>v</i>. The easiest way to ensure that is by calling this method
     * from a {@link ScanlineListener} or a {@link ScanlineCycleListener}. This
     * method updates registers <i>v</i> and <i>x</i> via the following code:
     * <pre>{@code
     * x = cameraX & 0x07;
     * v = (v & 0x7BE0) | ((cameraX & 0x100) << 2) | ((cameraX & 0xF8) >> 3);
     * }</pre>
     *
     * @param cameraY The Y-coordinate of the camera window.
     * @see #getCameraY()
     * @see #getCameraX()
     * @see #setCameraX(int)
     * @see #setPPUv(int)
     * @see #setPPUx(int)
     */
    void setCameraY(int cameraY);

    /**
     * Returns the current scanline number. {@code -1} refers to the pre-render
     * scanline. The maximum value is equal to {@code getScanlineCount() - 2 }.
     *
     * @return The index of the scanline currently being processed by the PPU.
     * @see #getDot()
     * @see #getScanlineCount()
     */
    int getScanline();

    /**
     * Returns the current scanline cycle number, a value in the range [0, 340].
     *
     * @return The index of the dot along the scanline currently being processed
     * by the PPU.
     * @see #getScanline()
     */
    int getDot();

    /**
     * Returns the value of the sprite zero hit flag, which indicates if an
     * opaque pixel of sprite 0 overlapped an opaque pixel of the background in
     * the current frame (the flag is reset on dot 1 of the pre-render scanline).
     * This value can also be obtained from bit 6 of PPUSTATUS ($2002).
     *
     * @return The sprite zero hit flag value.
     * @see #setSpriteZeroHit(boolean)
     */
    boolean isSpriteZeroHit();

    /**
     * <p>Assigns a value to the sprite zero hit flag. Modifying this flag affects
     * bit 6 of PPUSTATUS ($2002). Normally, the flag is set when an opaque pixel
     * of sprite 0 overlaps an opaque pixel of the background in the current
     * frame; and, it is reset on dot 1 of the pre-render scanline.</p>
     *
     * <p>Setting the sprite zero hit flag will <u>not</u> trigger
     * {@link SpriteZeroListener}s.</p>
     *
     * @param spriteZeroHit The sprite zero hit flag value.
     * @see #isSpriteZeroHit()
     */
    void setSpriteZeroHit(final boolean spriteZeroHit);

    /**
     * Returns the total number of scanlines in the current TV system.
     *
     * @return 262 for NTSC, 312 for PAL and Dendy.
     */
    int getScanlineCount();

    /**
     * Requests an interrupt. This method pulls the
     * <span style="text-decoration: overline;">IRQ</span> line low. The line will
     * remain low until {@link #acknowledgeInterrupt() acknowledgeInterrupt()} is
     * invoked (assuming that nothing else is pulling it low and that the mapper
     * does not acknowledge it earlier).
     *
     * @see #acknowledgeInterrupt()
     */
    void requestInterrupt();

    /**
     * Acknowledges an interrupt. {@link #requestInterrupt() requestInterrupt()}
     * forces the <span style="text-decoration: overline;">IRQ</span> line low
     * and it will remain low until this method is called. However, the mapper
     * has control of the same request and acknowledgement mechanism. And, other
     * components are connected to the line.
     *
     * @see #requestInterrupt()
     */
    void acknowledgeInterrupt();

    /**
     * Reads a byte from CPU Memory without triggering side effects. For instance,
     * peeking from the controller port registers will <u>not</u> modify the state
     * of the controllers.
     *
     * @param address Only the lower 16 bits are used.
     * @return The read value is contained within the lower 8 bits.
     * @see #peekCPU16(int)
     * @see #peekCPU32(int)
     * @see #readCPU(int)
     */
    int peekCPU(int address);

    /**
     * Reads a byte from CPU Memory. Reading may have side effects. For instance,
     * reading from the controller port registers will modify the state of the
     * controllers. This can be avoided by using {@link #peekCPU(int) peekCpu}.
     *
     * @param address Only the lower 16 bits are used.
     * @return The read value is contained within the lower 8 bits.
     * @see #readCPU16(int)
     * @see #readCPU32(int)
     * @see #peekCPU(int)
     */
    int readCPU(int address);

    /**
     * Writes a byte to CPU Memory.
     *
     * @param address Only the lower 16 bits are used.
     * @param value   The lower 8 bits contains the value to write.
     * @see #writeCPU16(int, int)
     * @see #writeCPU32(int, int)
     * @see #readCPU(int)
     * @see #peekCPU(int)
     */
    void writeCPU(int address, int value);

    /**
     * <p>Reads a 16-bit little-endian word from CPU Memory without side effects.
     * For instance, peeking from the controller port registers will <u>not</u>
     * modify the state of the controllers.</p>
     * <p>Bytes are read from sequentially increasing memory addresses.</p>
     *
     * @param address Only the lower 16 bits are used.
     * @return The read value is contained within the lower 16 bits.
     */
    int peekCPU16(int address);

    /**
     * <p>Reads a 16-bit little-endian word from CPU Memory. Bytes are read from
     * sequentially increasing memory addresses.</p>
     *
     * <p>Reading may have side effects. For instance, reading from the controller
     * port registers will modify the state of the controllers. This can be
     * avoided by using {@link #peekCPU16(int) peekCpu16}.</p>
     *
     * @param address Only the lower 16 bits are used.
     * @return The read value is contained within the lower 16 bits.
     * @see #peekCPU16(int)
     */
    int readCPU16(int address);

    /**
     * Writes a 16-bit little-endian word to CPU Memory. For instance, writing
     * the value {@code 0xAABB} to address {@code 0x0100} is equivalent to:
     * <pre>{@code
     * writeCpu(0x0100, 0xBB);
     * writeCpu(0x0101, 0xAA);
     * }</pre>
     *
     * @param address Only the lower 16 bits are used.
     * @param value   The lower 16 bits contains the value to write.
     * @see #writeCPU(int, int)
     */
    void writeCPU16(int address, int value);

    /**
     * <p>Reads a 32-bit little-endian word from CPU Memory without side effects.
     * For instance, peeking from the controller port registers will <u>not</u>
     * modify the state of the controllers.</p>
     * <p>Bytes are read from sequentially increasing memory addresses.</p>
     *
     * @param address Only the lower 16 bits are used.
     * @return The read value.
     */
    int peekCPU32(int address);

    /**
     * <p>Reads a 32-bit little-endian word from CPU Memory. Bytes are read from
     * sequentially increasing memory addresses.</p>
     *
     * <p>Reading may have side effects. For instance, reading from the controller
     * port registers will modify the state of the controllers. This can be
     * avoided by using {@link #peekCPU32(int) peekCpu32}.</p>
     *
     * @param address The lower 32 bits contains the memory address.
     * @return The read value.
     * @see #peekCPU32(int)
     */
    int readCPU32(int address);

    /**
     * Writes a 32-bit little-endian word to CPU Memory. For instance, writing
     * the value {@code 0xAABBCCDD} to address {@code 0x0100} is equivalent to:
     * <pre>{@code
     * writeCpu(0x0100, 0xDD);
     * writeCpu(0x0101, 0xCC);
     * writeCpu(0x0102, 0xBB);
     * writeCpu(0x0103, 0xAA);
     * }</pre>
     *
     * @param address Only the lower 16 bits are used.
     * @param value   The value to write.
     * @see #writeCPU(int, int)
     */
    void writeCPU32(int address, int value);

    /**
     * Reads a byte from PPU Memory.
     *
     * @param address The lower 14 bits contains the memory address.
     * @return The read value is contained within the lower 8 bits.
     */
    int readPPU(int address);

    /**
     * Writes a byte to PPU Memory.
     *
     * @param address The lower 14 bits contains the memory address.
     * @param value   The lower 8 bits contains the value to write.
     */
    void writePPU(int address, int value);

    /**
     * Reads a byte from Palette RAM. Addresses divisible by 4 map to the same
     * memory location.
     *
     * @param address The lower 5 bits contains the Palette RAM address.
     * @return The read value is contained within the lower 8 bits.
     */
    int readPaletteRAM(int address);

    /**
     * Writes a byte to Palette RAM. Addresses divisible by 4 map to the same
     * memory location.
     *
     * @param address The lower 5 bits contains the Palette RAM address.
     * @param value   The lower 8 bits contains the value to write.
     */
    void writePaletteRAM(int address, int value);

    /**
     * Reads a byte from Object Attribute Memory.
     *
     * @param address The lower 8 bits contain the OAM address.
     * @return The read value is contained within the lower 8 bits.
     */
    int readOAM(int address);

    /**
     * Writes a byte to Object Attribute Memory.
     *
     * @param address The lower 8 bits contain the OAM address.
     * @param value   The lower 8 bits contains the value to write.
     */
    void writeOAM(int address, int value);

    /**
     * Gets a gamepad button value. If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, it will return one of the
     * cached button values that be applied in the subsequent frame generation
     * period. If it is invoked prior to the listener callback, then the returned
     * value is the one being used in the current frame generation period.
     * However, this method also returns changes made via
     * {@link #writeGamepad(int, int, boolean) writeGamepad}, which might
     * apply to the next frame generation period, depending on when it was called.
     *
     * @param gamepad 0&ndash;3 (inclusive); 2 and 3 are applicable in multitap
     *                games.
     * @param button  One of the {@link GamepadButtons} constants.
     * @return {@code true} is pressed; {@code false} is released.
     * @see #writeGamepad(int, int, boolean)
     * @see ControllersListener#controllersProbed()
     * @see GamepadButtons
     */
    boolean readGamepad(int gamepad, int button);

    /**
     * <p>Sets a gamepad button value. If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, the modified value will
     * be applied in the subsequent frame generation period. If it is invoked
     * prior to the listener callback, the change will <u>not</u> be applied
     * immediately; rather, it will be applied automatically within the next
     * {@link ControllersListener#controllersProbed()} callback.</p>
     *
     * <p>The cached button values can be obtained via
     * {@link #readGamepad(int, int) readGamepad}.</p>
     *
     * @param gamepad 0&ndash;3 (inclusive); 2 and 3 are applicable in multitap
     *                games.
     * @param button  One of the {@link GamepadButtons} constants
     * @param value   {@code true} is pressed; {@code false} is released.
     * @see #readGamepad(int, int)
     * @see ControllersListener#controllersProbed()
     * @see GamepadButtons
     */
    void writeGamepad(int gamepad, int button, boolean value);

    /**
     * Gets the state of the Zapper trigger. If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, it will return the
     * cached trigger value that be applied in the subsequent frame generation
     * period. If it is invoked prior to the listener callback, then the returned
     * value is the one being used in the current frame generation period.
     * However, this method also returns changes made via
     * {@link #setZapperTrigger(boolean) setZapperTrigger}, which might
     * apply to the next frame generation period, depending on when it was called.
     *
     * @return {@code true} is pulled; {@code false} is released.
     * @see #setZapperTrigger(boolean)
     * @see ControllersListener#controllersProbed()
     */
    boolean isZapperTrigger();

    /**
     * <p>Sets the state of the Zapper trigger. If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, the modified value will
     * be applied in the subsequent frame generation period. If it is invoked
     * prior to the listener callback, the change will <u>not</u> be applied
     * immediately; rather, it will be applied automatically within the next
     * {@link ControllersListener#controllersProbed()} callback.</p>
     *
     * <p>The cached trigger value can be obtained via
     * {@link #isZapperTrigger() isZapperTrigger}.</p>
     *
     * @param zapperTrigger {@code true} is pulled; {@code false} is released.
     * @see #isZapperTrigger()
     * @see ControllersListener#controllersProbed()
     */
    void setZapperTrigger(boolean zapperTrigger);

    /**
     * Gets the Zapper X-coordinate or {@code -1} if the Zapper is not pointed
     * at the screen. If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, it will return the
     * cached coordinate value that be applied in the subsequent frame generation
     * period. If it is invoked prior to the listener callback, then the returned
     * value is the one being used in the current frame generation period.
     * However, this method also returns changes made via
     * {@link #setZapperX(int) setZapperX}, which might apply to the next frame
     * generation period, depending on when it was called.
     *
     * @return A value in the range 0&ndash;255 (inclusive) or {@code -1} if the
     * Zapper is not pointed at the screen.
     * @see #setZapperX(int)
     * @see ControllersListener#controllersProbed()
     */
    int getZapperX();

    /**
     * <p>Sets the Zapper X-coordinate. Use {@code -1} to indicate that the Zapper
     * is not pointed at the screen.</p>
     *
     * <p>If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, the modified value will
     * be applied in the subsequent frame generation period. If it is invoked
     * prior to the listener callback, the change will <u>not</u> be applied
     * immediately; rather, it will be applied automatically within the next
     * {@link ControllersListener#controllersProbed()} callback.</p>
     *
     * @param x A value in the range 0&ndash;255 (inclusive) or {@code -1} if the
     *          Zapper is not pointed at the screen.
     * @see #getZapperX()
     * @see ControllersListener#controllersProbed()
     */
    void setZapperX(int x);

    /**
     * Gets the Zapper Y-coordinate or {@code -1} if the Zapper is not pointed
     * at the screen. If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, it will return the
     * cached coordinate value that be applied in the subsequent frame generation
     * period. If it is invoked prior to the listener callback, then the returned
     * value is the one being used in the current frame generation period.
     * However, this method also returns changes made via
     * {@link #setZapperY(int) setZapperY}, which might apply to the next frame
     * generation period, depending on when it was called.
     *
     * @return A value in the range 0&ndash;239 (inclusive) or {@code -1} if the
     * Zapper is not pointed at the screen.
     * @see #setZapperY(int)
     * @see ControllersListener#controllersProbed()
     */
    int getZapperY();

    /**
     * <p>Sets the Zapper Y-coordinate. Use {@code -1} to indicate that the Zapper
     * is not pointed at the screen.</p>
     *
     * <p>If this method is invoked within
     * {@link ControllersListener#controllersProbed()}, the modified value will
     * be applied in the subsequent frame generation period. If it is invoked
     * prior to the listener callback, the change will <u>not</u> be applied
     * immediately; rather, it will be applied automatically within the next
     * {@link ControllersListener#controllersProbed()} callback.</p>
     *
     * @param y A value in the range 0&ndash;239 (inclusive) or {@code -1} if the
     *          Zapper is not pointed at the screen.
     * @see #getZapperY()
     * @see ControllersListener#controllersProbed()
     */
    void setZapperY(int y);

    /**
     * <p>Gets the drawing/filling color, a 9-bit extended palette index.</p>
     *
     * @return The lower 6 bits contain the palette index and the upper 3 bits
     * contain the color emphasis. {@link Colors Colors} provides constants
     * for the non-emphasized colors.
     * @see #setColor(int)
     * @see Colors
     */
    int getColor();

    /**
     * <p>Sets the drawing/filling color to the specified 9-bit extended
     * palette index. Subsequent graphics operations will use it.</p>
     *
     * @param color The lower 6 bits contain the palette index and the upper 3
     *              bits contain the color emphasis. {@link Colors Colors} provides constants
     *              for the non-emphasized colors.
     * @see #getColor()
     * @see Colors
     */
    void setColor(int color);

    /**
     * <p>Sets the current clip to the rectangle specified by the given
     * coordinates. This method sets the user clip, which is independent of the
     * clipping associated with device bounds and window visibility. Rendering
     * operations have no effect outside of the clipping area.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * it will take effect as soon as the listener returns. If it is
     * invoked prior to the listener callback, it will be done automatically
     * within the next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the new clip rectangle.
     * @param y      The Y-coordinate of the new clip rectangle.
     * @param width  The width of the new clip rectangle.
     * @param height The height of the new clip rectangle.
     * @see #clipRect(int, int, int, int)
     * @see #resetClip()
     */
    void setClip(int x, int y, int width, int height);

    /**
     * <p>Intersects the current clip with the specified rectangle. The resulting
     * clipping area is the intersection of the current clipping area and the
     * specified rectangle. If there is no current clipping area, either because
     * the clip has never been set, or the clip has been cleared using
     * {@link #resetClip() resetClip()}, the specified rectangle becomes the
     * new clip. This method sets the user clip, which is independent of the
     * clipping associated with device bounds and window visibility. This method
     * can only be used to make the current clip smaller. To set the current clip
     * larger, use {@link #setClip(int, int, int, int) setClip} methods.
     * Rendering operations have no effect outside of the clipping area.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * it will take effect as soon as the listener returns. If it is
     * invoked prior to the listener callback, it will be done automatically
     * within the next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the rectangle to intersect the clip with.
     * @param y      The Y-coordinate of the rectangle to intersect the clip with.
     * @param width  The width of the rectangle to intersect the clip with.
     * @param height The height of the rectangle to intersect the clip with.
     * @see #setClip(int, int, int, int)
     * @see #resetClip()
     */
    void clipRect(int x, int y, int width, int height);

    /**
     * <p>Removes the current clipping area. This method resets the user clip,
     * which is independent of the clipping associated with the device bounds
     * and window visibility. Rendering operations have no effect outside of the
     * clipping area.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * it will take effect as soon as the listener returns. If it is
     * invoked prior to the listener callback, it will be done automatically
     * within the next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @see #setClip(int, int, int, int)
     * @see #clipRect(int, int, int, int)
     */
    void resetClip();

    /**
     * <p>Copies an area by a distance specified by {@code dx} and {@code dy}.
     * From the point specified by {@code x} and {@code y}, this method copies
     * downwards and to the right. To copy an area to the left or upwards, specify
     * a negative value for {@code dx} or {@code dy}. If a portion of the source
     * rectangle lies outside the window bounds or it is obscured by another
     * window or component {@code copyArea} will be unable to copy the associated
     * pixels.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the source rectangle.
     * @param y      The Y-coordinate of the source rectangle.
     * @param width  The width of the source rectangle.
     * @param height The height of the source rectangle.
     * @param dx     The horizontal distance to copy the pixels.
     * @param dy     The vertical distance to copy the pixels.
     * @see #getPixels(int[])
     */
    void copyArea(int x, int y, int width, int height, int dx, int dy);

    /**
     * <p>Draws a colored line between the points {@code (x1, y1)} and
     * {@code (x2, y2)}.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x1 The first point's X-coordinate.
     * @param y1 The first point's Y-coordinate.
     * @param x2 The second point's X-coordinate.
     * @param y2 The second point's Y-coordinate.
     * @see #setPixel(int, int, int)
     */
    void drawLine(int x1, int y1, int x2, int y2);

    /**
     * <p>Draws the outline of an oval. The result is a circle or ellipse that
     * fits within the rectangle specified by the x, y, width, and height
     * arguments.</p>
     *
     * <p>The oval covers an area that is {@code width + 1} pixels wide and
     * {@code height + 1} pixels tall.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the upper-left corner of the oval to be drawn.
     * @param y      The Y-coordinate of the upper-left corner of the oval to be drawn.
     * @param width  The width of the oval to be drawn.
     * @param height The height of the oval to be drawn.
     * @see #fillOval(int, int, int, int)
     */
    void drawOval(int x, int y, int width, int height);

    /**
     * <p>Draws a closed polygon defined by arrays of <i>x</i> and <i>y</i>
     * coordinates. Each pair of <i>(x, y)</i> coordinates defines a point.</p>
     *
     * <p>This method draws the polygon defined by {@code nPoint} line segments,
     * where the first {@code nPoint - 1} line segments are line segments from
     * {@code (xPoints[i - 1], yPoints[i - 1])} to
     * {@code (xPoints[i], yPoints[i])}, for 1 &le; i &le; {@code nPoint}. The
     * figure is automatically closed by drawing a line connecting the final point
     * to the first point, if those points are different.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param xPoints An array of X-coordinates.
     * @param yPoints An array of Y-coordinates.
     * @param nPoints The total number of points.
     * @see #fillPolygon(int[], int[], int)
     * @see #drawPolyline(int[], int[], int)
     */
    void drawPolygon(int[] xPoints, int[] yPoints, int nPoints);

    /**
     * <p>Draws a sequence of connected lines defined by arrays of <i>x</i> and
     * <i>y</i> coordinates. Each pair of <i>(x, y)</i> coordinates defines a
     * point. The figure is not closed if the first point differs from the last
     * point.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param xPoints An array of X-coordinates.
     * @param yPoints An array of Y-coordinates.
     * @param nPoints The total number of points.
     * @see #drawPolygon(int[], int[], int)
     */
    void drawPolyline(int[] xPoints, int[] yPoints, int nPoints);

    /**
     * <p>Draws the outline of the specified rectangle. The left and right edges
     * of the rectangle are at {@code x} and {@code x + width}. The top and bottom
     * edges are at {@code y} and {@code y + height}.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the rectangle to be drawn.
     * @param y      The Y-coordinate of the rectangle to be drawn.
     * @param width  The width of the rectangle to be drawn.
     * @param height The height of the rectangle to be drawn.
     * @see #fillRect(int, int, int, int)
     */
    void drawRect(int x, int y, int width, int height);

    /**
     * <p>Draws an outlined round-cornered rectangle using in the current color.
     * The left and right edges of the rectangle are at {@code x} and
     * {@code x + width}, respectively. The top and bottom edges of the rectangle
     * are at {@code y} and {@code y + height}.
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x         The X-coordinate of the rectangle to be drawn.
     * @param y         The Y-coordinate of the rectangle to be drawn.
     * @param width     The width of the rectangle to be drawn.
     * @param height    The height of the rectangle to be drawn.
     * @param arcWidth  The horizontal diameter of the arc at the four corners.
     * @param arcHeight The vertical diameter of the arc at the four corners.
     * @see #fillRoundRect(int, int, int, int, int, int)
     */
    void drawRoundRect(int x, int y, int width, int height, int arcWidth,
                       int arcHeight);

    /**
     * <p>Draws a character in the current color. <i>(x, y)</i> is position for
     * the upper-left corner of the character.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param c The character to be drawn.
     * @param x The X-coordinate of the left side of the character.
     * @param y The Y-coordinate of the top of the character.
     * @see #drawChars(char[], int, int, int, int, boolean)
     * @see #drawString(java.lang.String, int, int, boolean)
     */
    void drawChar(char c, int x, int y);

    /**
     * <p>Draws text in the current color using either a monospaced or a
     * proportional font. <i>(x, y)</i> is position for the upper-left
     * corner of the first character in the array.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param chars      The array of characters to be drawn.
     * @param offset     The start offset in the data.
     * @param length     The number of characters to be drawn.
     * @param x          The X-coordinate of the left side of the first character.
     * @param y          The Y-coordinate of the top of the first character.
     * @param monospaced The font type. {@code true} for monospaced; {@code false}
     *                   for proportional.
     * @see #drawChar(char, int, int)
     * @see #drawString(java.lang.String, int, int, boolean)
     */
    void drawChars(char[] chars, int offset, int length, int x, int y,
                   boolean monospaced);

    /**
     * <p>Draws text in the current color using either a monospaced or a
     * proportional font. <i>(x, y)</i> is position for the upper-left corner of
     * the first character in the string.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param str        The string to be drawn.
     * @param x          The X-coordinate of the left side of the first character.
     * @param y          The Y-coordinate of the top of the first character.
     * @param monospaced The font type. {@code true} for monospaced; {@code false}
     *                   for proportional.
     * @see #drawChar(char, int, int)
     * @see #drawChars(char[], int, int, int, int, boolean)
     */
    void drawString(String str, int x, int y, boolean monospaced);

    /**
     * Creates a sprite with the specified {@code width}, {@code height} and
     * 9-bit extended palette index values. The provided {@code id} is used to
     * reference the sprite for drawing or deleting. Completely transparent
     * regions are supported, but translucent regions are not.
     *
     * @param id     An identifier for the sprite used for drawing or deleting. If a
     *               sprite with the provided identifier already exists, it will be replaced.
     * @param width  The width of the sprite.
     * @param height The height of the sprite.
     * @param pixels The lower 6 bits of each {@code int} is the palette index
     *               and the upper 3 is the color emphasis. {@code -1} indicates transparency.
     * @see #setColor(int)
     * @see #getColor()
     * @see #drawSprite(int, int, int)
     * @see #deleteSprite(int)
     * @see Colors
     */
    void createSprite(int id, int width, int height, int[] pixels);

    /**
     * <p>Draws a sprite with its upper-left corner at <i>(x, y)</i>. Transparent
     * pixels in the sprite do not affect whatever pixels are already there.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param id The sprite to draw. This identifier was specified
     *           {@link #createSprite(int, int, int, int[]) when the sprite was created}.
     * @param x  The X-coordinate for upper-left corner of the sprite to be drawn.
     * @param y  The Y-coordinate for upper-left corner of the sprite to be drawn.
     * @see #createSprite(int, int, int, int[])
     * @see #deleteSprite(int)
     */
    void drawSprite(int id, int x, int y);

    /**
     * Removes the sprite with the specified identifier.
     *
     * @param id The sprite to delete. This identifier is same as the one
     *           provided {@link #createSprite(int, int, int, int[]) when the sprite was
     *           created}.
     * @see #createSprite(int, int, int, int[])
     * @see #drawSprite(int, int, int)
     */
    void deleteSprite(int id);

    /**
     * <p>Draws a 3-D highlighted outline of the specified rectangle. The edges of
     * the rectangle are highlighted so that they appear to be beveled and lit
     * from the upper-left corner.</p>
     *
     * <p>The colors used for the highlighting effect are determined based on the
     * current color. The resulting rectangle covers an area that is
     * {@code width + 1} pixels wide by {@code height + 1} pixels tall.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the rectangle to be drawn.
     * @param y      The Y-coordinate of the rectangle to be drawn.
     * @param width  The width of the rectangle to be drawn.
     * @param height The height of the rectangle to be drawn.
     * @param raised A boolean that determines whether the rectangle appears to be
     *               raised above the surface or sunk into the surface.
     * @see #fill3DRect(int, int, int, int, boolean)
     */
    void draw3DRect(int x, int y, int width, int height, boolean raised);

    /**
     * <p>Draws the outline of a circular or elliptical arc covering the specified
     * rectangle.</p>
     *
     * <p>The resulting arc begins at {@code startAngle} and extends for
     * {@code arcAngle} degrees, using the current color. Angles are interpreted
     * such that 0 degrees is at the 3 o'clock position. A positive value
     * indicates a counter-clockwise rotation while a negative value indicates a
     * clockwise rotation.</p>
     *
     * <p>The center of the arc is the center of the rectangle whose origin is
     * <i>(x, y)</i> and whose size is specified by the {@code width} and
     * {@code height} arguments.</p>
     *
     * <p>The resulting arc covers an area {@code width + 1} pixels wide by
     * {@code height + 1} pixels tall.</p>
     *
     * <p>The angles are specified relative to the non-square extents of the
     * bounding rectangle such that 45 degrees always falls on the line from the
     * center of the ellipse to the upper right corner of the bounding rectangle.
     * As a result, if the bounding rectangle is noticeably longer in one axis
     * than the other, the angles to the start and end of the arc segment will be
     * skewed farther along the longer axis of the bounds.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x          The X-coordinate of the upper-left corner of the arc to be drawn.
     * @param y          The Y-coordinate of the upper-left corner of the arc to be drawn.
     * @param width      The width of the arc to be drawn.
     * @param height     The height of the arc to be drawn.
     * @param startAngle The beginning angle.
     * @param arcAngle   The angular extent of the arc, relative to the start angle.
     * @see #fillArc(int, int, int, int, int, int)
     */
    void drawArc(int x, int y, int width, int height, int startAngle,
                 int arcAngle);

    /**
     * <p>Paints a 3-D highlighted rectangle filled with the current color. The
     * edges of the rectangle will be highlighted so that it appears as if the
     * edges were beveled and lit from the upper-left corner. The colors used for
     * the highlighting effect will be determined from the current color.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the rectangle to be filled.
     * @param y      The Y-coordinate of the rectangle to be filled.
     * @param width  The width of the rectangle to be filled.
     * @param height The height of the rectangle to be filled.
     * @param raised A boolean value that determines whether the rectangle appears
     *               to be raised above the surface or etched into the surface.
     * @see #draw3DRect(int, int, int, int, boolean)
     */
    void fill3DRect(int x, int y, int width, int height, boolean raised);

    /**
     * <p>Fills a circular or elliptical arc covering the specified rectangle.</p>
     *
     * <p>The resulting arc begins at {@code startAngle} and extends for
     * {@code arcAngle} degrees, using the current color. Angles are interpreted
     * such that 0 degrees is at the 3 o'clock position. A positive value
     * indicates a counter-clockwise rotation while a negative value indicates a
     * clockwise rotation.</p>
     *
     * <p>The center of the arc is the center of the rectangle whose origin is
     * <i>(x, y)</i> and whose size is specified by the {@code width} and
     * {@code height} arguments.</p>
     *
     * <p>The resulting arc covers an area {@code width + 1} pixels wide by
     * {@code height + 1} pixels tall.</p>
     *
     * <p>The angles are specified relative to the non-square extents of the
     * bounding rectangle such that 45 degrees always falls on the line from the
     * center of the ellipse to the upper right corner of the bounding rectangle.
     * As a result, if the bounding rectangle is noticeably longer in one axis
     * than the other, the angles to the start and end of the arc segment will be
     * skewed farther along the longer axis of the bounds.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x          The X-coordinate of the upper-left corner of the arc to be filled.
     * @param y          The Y-coordinate of the upper-left corner of the arc to be filled.
     * @param width      The width of the arc to be filled.
     * @param height     The height of the arc to be filled.
     * @param startAngle The beginning angle.
     * @param arcAngle   The angular extent of the arc, relative to the start angle.
     * @see #drawArc(int, int, int, int, int, int)
     */
    void fillArc(int x, int y, int width, int height, int startAngle,
                 int arcAngle);

    /**
     * <p>Fills an oval bounded by the specified rectangle with the current
     * color.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the upper-left corner of the oval to be
     *               filled.
     * @param y      The Y-coordinate of the upper-left corner of the oval to be
     *               filled.
     * @param width  The width of the oval to be filled.
     * @param height The height of the oval to be filled.
     * @see #drawOval(int, int, int, int)
     */
    void fillOval(int x, int y, int width, int height);

    /**
     * <p>Fills a closed polygon defined by arrays of <i>x</i> and <i>y</i>
     * coordinates.</p>
     *
     * <p>This method draws the polygon defined by {@code nPoint} line segments,
     * where the first {@code nPoint - 1} line segments are line segments from
     * {@code (xPoints[i - 1], yPoints[i - 1])} to
     * {@code (xPoints[i], yPoints[i])}, for 1 &le; i &le; {@code nPoints}. The
     * figure is automatically closed by drawing a line connecting the final point
     * to the first point, if those points are different.</p>
     *
     * <p>The area inside the polygon is defined using an even-odd fill rule, also
     * known as the alternating rule.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param xPoints An array of X-coordinates.
     * @param yPoints An array of Y-coordinates.
     * @param nPoints The total number of points.
     * @see #drawPolygon(int[], int[], int)
     * @see #drawPolyline(int[], int[], int)
     */
    void fillPolygon(int[] xPoints, int[] yPoints, int nPoints);

    /**
     * <p>Fills the specified rectangle. The left and right edges of the rectangle
     * are at {@code x} and {@code x + width - 1}. The top and bottom edges are at
     * {@code y} and {@code y + height - 1}. The resulting rectangle covers an
     * area {@code width} pixels wide by {@code height} pixels tall. The rectangle
     * is filled using the current color.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x      The X-coordinate of the rectangle to be filled.
     * @param y      The Y-coordinate of the rectangle to be filled.
     * @param width  The width of the rectangle to be filled.
     * @param height The height of the rectangle to be filled.
     * @see #drawRect(int, int, int, int)
     */
    void fillRect(int x, int y, int width, int height);

    /**
     * <p>Fills the specified rounded corner rectangle with the current color. The
     * left and right edges of the rectangle are at {@code x} and
     * {@code x + width - 1}, respectively. The top and bottom edges of the
     * rectangle are at {@code y} and {@code y + height - 1}.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x         The X-coordinate of the rectangle to be filled.
     * @param y         The Y-coordinate of the rectangle to be filled.
     * @param width     The width of the rectangle to be filled.
     * @param height    The height of the rectangle to be filled.
     * @param arcWidth  The horizontal diameter of the arc at the four corners.
     * @param arcHeight The vertical diameter of the arc at the four corners.
     * @see #drawRoundRect(int, int, int, int, int, int)
     */
    void fillRoundRect(int x, int y, int width, int height, int arcWidth,
                       int arcHeight);

    /**
     * <p>Draws a pixel at the specified coordinates and in the provided
     * color.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the drawing will be displayed as soon as the listener returns. If it is
     * invoked prior to the listener callback, the drawing will <u>not</u> be
     * rendered immediately; rather, it will be done automatically within the
     * next {@link FrameListener#frameRendered()} callback.</p>
     *
     * @param x     The X-coordinate of the pixel to be drawn, a value in the range
     *              [0, 255].
     * @param y     The Y-coordinate of the pixel to be drawn, a value in the range
     *              [0, 239].
     * @param color The lower 6 bits is the palette index and the upper 3 bits is
     *              the color emphasis. The associated color is opaque (alpha is not
     *              supported).
     * @see #setColor(int)
     * @see #getColor()
     * @see #getPixel(int, int)
     * @see #getPixels(int[])
     */
    void setPixel(int x, int y, int color);

    /**
     * <p>Obtains the extended palette index of a pixel at the specified
     * coordinates.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the returned value will be from the frame that is about to be displayed. If
     * it is invoked prior to the listener callback, the returned value could be
     * from a prior frame.</p>
     *
     * @param x The X-coordinate of the pixel, a value in the range [0, 255].
     * @param y The Y-coordinate of the pixel, a value in the range [0, 239].
     * @return The lower 6 bits contain the palette index and the upper 3 bits
     * contain the color emphasis.
     * @see #setColor(int)
     * @see #getColor()
     * @see #setPixel(int, int, int)
     * @see #getPixels(int[])
     */
    int getPixel(int x, int y);

    /**
     * <p>Captures the entire surface into the provided array.</p>
     *
     * <p>If this method is invoked within {@link FrameListener#frameRendered()},
     * the pixel values will be from the frame that is about to be displayed. If
     * it is invoked prior to the listener callback, some of the values could be
     * from a prior frame.</p>
     *
     * @param pixels The array to fill with 9-bit extended palette indices. It
     *               must have a length of 256 &times; 240 = 61440 to capture the entire
     *               surface. The lower 6 bits of each {@code int} contain the palette index and
     *               the upper 3 contain the color emphasis.
     * @see #getPixel(int, int)
     * @see #setPixel(int, int, int)
     * @see Colors
     */
    void getPixels(int[] pixels);

    /**
     * Sets the emulation speed as a percentage of normal speed.
     *
     * @param percent The relative speed. 100 is normal speed. Greater values
     *                speed up emulation. Lesser values slow it down, with the exception of 0 (or
     *                below), which represents maximum speed.
     */
    void setSpeed(int percent);

    /**
     * Resumes emulation for one frame. When the emulator is paused, this method
     * can be used to step forward by a frame.
     *
     * @see #setPaused(boolean)
     * @see #isPaused()
     */
    void stepToNextFrame();

    /**
     * Briefly displays a message to the user.
     *
     * @param message The message to display.
     * @see #drawString(java.lang.String, int, int, boolean)
     */
    void showMessage(String message);

    /**
     * Returns the directory containing the emulator resources.
     *
     * @return The working directory.
     * @see #getContentDirectory()
     */
    String getWorkingDirectory();

    /**
     * Returns the configured generated content directory. By default, this is
     * the emulator process working directory.
     *
     * @return The content directory.
     * @see #getWorkingDirectory()
     */
    String getContentDirectory();

    /**
     * Loads a file and starts emulation.
     *
     * @param fileName The file to load. This is an absolute file path or a path
     *                 relative to the emulator's working directory. iNES, NES 2.0, Famicom Disk
     *                 System, UNIF, and NES Sound files are supported.
     * @see #openArchiveEntry(java.lang.String, java.lang.String)
     * @see #openDefaultArchiveEntry(java.lang.String)
     * @see #close()
     * @see #getWorkingDirectory()
     */
    void open(String fileName);

    /**
     * Loads a file from an archive and starts emulation.
     *
     * @param archiveFileName The archive file. This is an absolute file path or a
     *                        path relative to the emulator's working directory. Several archive formats
     *                        are supported including zip, rar, 7z, and various tar formats.
     * @param entryFileName   The entry file. This is a path relative to the root of
     *                        the archive. iNES, NES 2.0, Famicom Disk System, UNIF, and NES Sound files
     *                        are supported.
     * @see #open(java.lang.String)
     * @see #openDefaultArchiveEntry(java.lang.String)
     * @see #close()
     * @see #getWorkingDirectory()
     */
    void openArchiveEntry(String archiveFileName, String entryFileName);

    /**
     * Returns the list of files within a specified archive that can be opened.
     *
     * @param archiveFileName The archive file. This is an absolute file path or a
     *                        path relative to the emulator's working directory. Several archive formats
     *                        are supported including zip, rar, 7z, and various tar formats.
     * @return The file list. The paths are relative to the root of the archive.
     * Only files with recognized extensions are included in the list.
     * @see #getDefaultArchiveEntry(java.lang.String)
     */
    String[] getArchiveEntries(String archiveFileName);

    /**
     * Returns the preferred entry file name within a specified archive, which is
     * based on the configured region and other file codes.
     *
     * @param archiveFileName The archive file. This is an absolute file path or a
     *                        path relative to the emulator's working directory. Several archive formats
     *                        are supported including zip, rar, 7z, and various tar formats.
     * @return The default entry file. This is a path relative to the root of
     * the archive. iNES, NES 2.0, Famicom Disk System, UNIF, and NES Sound files
     * are supported.
     * @see #getArchiveEntries(java.lang.String)
     */
    String getDefaultArchiveEntry(String archiveFileName);

    /**
     * Loads the preferred file from an archive and starts emulation. The
     * preferred file is based on the configured region and other file codes.
     *
     * @param archiveFileName The archive file. This is an absolute file path or a
     *                        path relative to the emulator's working directory. Several archive formats
     *                        are supported including zip, rar, 7z, and various tar formats.
     * @see #getDefaultArchiveEntry(java.lang.String)
     * @see #open(java.lang.String)
     * @see #openArchiveEntry(java.lang.String, java.lang.String)
     * @see #close()
     */
    void openDefaultArchiveEntry(String archiveFileName);

    /**
     * Terminates the currently running file.
     *
     * @see #open(java.lang.String)
     * @see #openArchiveEntry(java.lang.String, java.lang.String)
     * @see #openDefaultArchiveEntry(java.lang.String)
     */
    void close();

    /**
     * Captures the current emulation state into a state file.
     *
     * @param stateFileName The state file. This is an absolute file path or a
     *                      path relative to the emulator's working directory. Preferably, the file
     *                      name should end with the .save extension.
     * @see #quickSaveState(int)
     * @see #loadState(java.lang.String)
     * @see #quickLoadState(int)
     */
    void saveState(String stateFileName);

    /**
     * Resumes emulation from a saved state file.
     *
     * @param stateFileName The state file. This is an absolute file path or a
     *                      path relative to the emulator's working directory. The file name should end
     *                      with the .save extension.
     * @see #quickLoadState(int)
     * @see #saveState(java.lang.String)
     * @see #quickSaveState(int)
     */
    void loadState(String stateFileName);

    /**
     * Captures the current emulation state into a state slot.
     *
     * @param slot Slots are numbered 1 to 9. 0 indicates the least recently used
     *             slot.
     * @see #quickLoadState(int)
     * @see #loadState(java.lang.String)
     * @see #saveState(java.lang.String)
     */
    void quickSaveState(int slot);

    /**
     * Resumes emulation from a saved state slot.
     *
     * @param slot Slots are numbered 1 to 9. 0 indicates the most recently used
     *             slot.
     * @see #quickSaveState(int)
     * @see #loadState(java.lang.String)
     * @see #saveState(java.lang.String)
     */
    void quickLoadState(int slot);

    /**
     * Returns the television encoding system.
     *
     * @return Either "NTSC", "PAL" or "Dendy".
     */
    String getTVSystem();

    /**
     * Sets the television encoding system.
     *
     * @param tvSystem Supports "NTSC", "PAL" and "Dendy".
     */
    void setTVSystem(String tvSystem);

    /**
     * Provides the total number of Famicom Disk Card sides containing data. The
     * total number of disks is this value divided by 2 rounded up to the nearest
     * integer.
     *
     * @return The number of disk sides.
     * @see #insertDisk(int, int)
     * @see #flipDiskSide()
     * @see #ejectDisk()
     */
    int getDiskSides();

    /**
     * Inserts a specified Famicom Disk Card into the drive flipped to a
     * particular side.
     *
     * @param disk The disk number, starting from 0.
     * @param side 0 indicates side A and 1 indicates side B.
     * @see #getDiskSides()
     * @see #flipDiskSide()
     * @see #ejectDisk()
     */
    void insertDisk(int disk, int side);

    /**
     * Ejects the currently inserted Famicom Disk Card, turns it over to the
     * opposite side and reinserts it into the drive.
     *
     * @see #getDiskSides()
     * @see #insertDisk(int, int)
     * @see #ejectDisk()
     */
    void flipDiskSide();

    /**
     * Ejects the currently inserted Famicom Disk Card.
     *
     * @see #getDiskSides()
     * @see #insertDisk(int, int)
     * @see #flipDiskSide()
     */
    void ejectDisk();

    /**
     * Inserts a coin into the VS. System.
     *
     * @see #pressServiceButton()
     */
    void insertCoin();

    /**
     * Presses the VS. System service button.
     *
     * @see #insertCoin()
     */
    void pressServiceButton();

    /**
     * Briefly yells into the controller 2 microphone on the traditional Famicom.
     */
    void screamIntoMicrophone();

    /**
     * Induces a graphical glitch.
     */
    void glitch();

    /**
     * Provides a detailed description of the currently loaded file.
     *
     * @return The file information.
     */
    String getFileInfo();

    /**
     * Enters or exits fullscreen exclusive mode.
     *
     * @param fullscreenMode {@code true} for fullscreen mode; {@code false} for
     *                       window mode.
     */
    void setFullscreenMode(boolean fullscreenMode);

    /**
     * Captures a screenshot to the configured screenshots directory.
     */
    void saveScreenshot();

    /**
     * <p>Adds a new cheat or if the {@code address}, {@code value} and
     * {@code compare} match an existing cheat, updates the existing cheat
     * with the specified {@code description} and {@code enabled} values.</p>
     *
     * <p>Cheats intercept CPU reads and conditionally substitute the read value
     * with an alternative value.</p>
     *
     * @param address     The CPU read address. This can be any value in the range
     *                    {@code $0000-FFFF}, not just {@code $8000} or above.
     * @param value       The alternative value conditionally read at the CPU address.
     * @param compare     If the actual value at the CPU read address matches this
     *                    one, then the provided alternative value is substituted in its place.
     *                    {@code -1} indicates no comparison; the alternative value will always be
     *                    used.
     * @param description A summary of what the cheat does.
     * @param enabled     Activates or deactivates the cheat.
     */
    void addCheat(int address, int value, int compare, String description,
                  boolean enabled);

    /**
     * Deletes all cheats matching the specified parameters.
     *
     * @param address The CPU read address. This can be any value in the range
     *                {@code $0000-FFFF}, not just {@code $8000} or above.
     * @param value   The alternative value conditionally read at the CPU address.
     * @param compare If the actual value at the CPU read address matches this
     *                one, then the provided alternative value is substituted in its place.
     *                {@code -1} indicates no comparison; the alternative value will always be
     *                used.
     */
    void removeCheat(int address, int value, int compare);

    /**
     * Adds a new Game Genie code or if the code matches an existing one, updates
     * the existing {@code description} and {@code enabled} values.
     *
     * @param gameGenieCode The Game Genie code.
     * @param description   A summary of what the Game Genie code does.
     * @param enabled       Activates or deactivates the Game Genie code.
     */
    void addGameGenie(String gameGenieCode, String description, boolean enabled);

    /**
     * Deletes a Game Genie code.
     *
     * @param gameGenieCode The Game Genie code to remove.
     */
    void removeGameGenie(String gameGenieCode);

    /**
     * Adds a new Pro Action Rocky code or if the code matches an existing one,
     * updates the existing {@code description} and {@code enabled} values.
     *
     * @param proActionRockyCode The Pro Action Rocky code.
     * @param description        A summary of what the Pro Action Rocky code does.
     * @param enabled            Activates or deactivates the Pro Action Rocky code.
     */
    void addProActionRocky(String proActionRockyCode, String description,
                           boolean enabled);

    /**
     * Deletes a Pro Action Rocky code.
     *
     * @param proActionRockyCode The Pro Action Rocky code to remove.
     */
    void removeProActionRocky(String proActionRockyCode);

    /**
     * Provides the number of bytes of PRG ROM or 0 if PRG ROM is not present.
     *
     * @return PRG ROM data size in bytes.
     */
    int getPrgRomSize();

    /**
     * Reads a byte from PRG ROM. PRG ROM is treated as a single contiguous block
     * of memory.
     *
     * @param index The offset into PRG ROM from which to read.
     * @return The byte value at the specified PRG ROM {@code index} or {@code -1}
     * if PRG ROM is not available or the {@code index} is out of range.
     */
    int readPrgRom(int index);

    /**
     * Writes a byte to PRG ROM. PRG ROM is treated as a single contiguous block
     * of memory.
     *
     * @param index The offset into PRG ROM to which to write.
     * @param value They byte value to write at the specified PRG ROM
     *              {@code index}.
     */
    void writePrgRom(int index, int value);

    /**
     * Provides the number of bytes of CHR ROM or 0 if CHR ROM is not present.
     *
     * @return CHR ROM data size in bytes.
     */
    int getChrRomSize();

    /**
     * Reads a byte from CHR ROM. CHR ROM is treated as a single contiguous block
     * of memory.
     *
     * @param index The offset into CHR ROM from which to read.
     * @return The byte value at the specified CHR ROM {@code index} or {@code -1}
     * if CHR ROM is not available or the {@code index} is out of range.
     */
    int readChrRom(int index);

    /**
     * Writes a byte to CHR ROM. CHR ROM is treated as a single contiguous block
     * of memory.
     *
     * @param index The offset into CHR ROM to which to write.
     * @param value They byte value to write at the specified CHR ROM
     *              {@code index}.
     */
    void writeChrRom(int index, int value);

    /**
     * Provides the pixel width of the specified {@code String} measured using
     * either a monospaced or a proportional font. The height is always 8.
     *
     * @param str        The {@code String} to measure.
     * @param monospaced The font type. {@code true} for monospaced; {@code false}
     *                   for proportional.
     * @return The pixel width of the specified string.
     * @see #getCharsWidth(char[], boolean)
     * @see #drawChars(char[], int, int, int, int, boolean)
     * @see #drawString(java.lang.String, int, int, boolean)
     */
    int getStringWidth(String str, boolean monospaced);

    /**
     * Provides the pixel width of the specified characters measured using either
     * a monospaced or a proportional font. The height is always 8.
     *
     * @param chars      The characters to measure.
     * @param monospaced The font type. {@code true} for monospaced; {@code false}
     *                   for proportional.
     * @return The pixel width of the specified characters.
     * @see #getStringWidth(java.lang.String, boolean)
     * @see #drawChars(char[], int, int, int, int, boolean)
     * @see #drawString(java.lang.String, int, int, boolean)
     */
    int getCharsWidth(char[] chars, boolean monospaced);
}
