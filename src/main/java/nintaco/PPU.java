package nintaco;

import java.io.*;
import java.util.*;

import nintaco.api.local.*;
import nintaco.cpu.CPU;
import nintaco.gui.image.preferences.*;
import nintaco.gui.rob.*;
import nintaco.input.*;
import nintaco.input.zapper.*;
import nintaco.mappers.*;
import nintaco.palettes.*;
import nintaco.preferences.*;
import nintaco.tv.*;

import static nintaco.tv.TVSystem.*;
import static nintaco.util.BitUtil.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.MathUtil.*;

public class PPU implements Serializable {

    private static final long serialVersionUID = 0;

    public static final int[] INVERSE_PALETTE = {
            11, 39, 40, 57, 58, 44, 60, 16, 45, 52, 34, 0, 38, 32, 48, 56,
            7, 24, 42, 41, 43, 59, 33, 28, 17, 35, 36, 37, 23, 55, 53, 51,
            13, 22, 10, 25, 26, 27, 12, 1, 2, 19, 18, 20, 5, 8, 49, 54,
            14, 46, 62, 31, 9, 30, 47, 29, 15, 3, 4, 21, 6, 63, 50, 61,};

    public static final int PRE_RENDER_SCANLINE = -1;

    private static final int SECONDARY_OAM_OFFSET = 0x100;
    private static final int OAM_DUMMY_BYTE_INDEX = 0x120;
    private static final int OAM_DUMMY_BYTE_VALUE = 0xFF;

    private static final int EVAL_STATE_COPY_Y = 0;
    private static final int EVAL_STATE_COPY_REMAINING = 1;
    private static final int EVAL_STATE_SPRITE_8_Y = 2;
    private static final int EVAL_STATE_SPRITE_8_REMAINING = 3;
    private static final int EVAL_STATE_SPIN = 4;

    private static final int BACKGROUND_NAMETABLE_ADDRESS = 0;
    private static final int BACKGROUND_ATTRIBUTES = 1;
    private static final int BACKGROUND_BITMAP_0 = 2;
    private static final int BACKGROUND_BITMAP_1 = 3;

    private static final int SPRITE_Y = 0;
    private static final int SPRITE_TILE = 1;
    private static final int SPRITE_ATTRIBUTES = 2;
    private static final int SPRITE_X = 3;

    private static final int SPRITE_DATA_ATTRIBUTES = 0;
    private static final int SPRITE_DATA_X = 1;

    private static final int ATTRIBUTES_PALETTE_0 = 0;
    private static final int ATTRIBUTES_PALETTE_1 = 1;
    private static final int ATTRIBUTES_BEHIND_BACKGROUND = 5;
    private static final int ATTRIBUTES_HORIZONTAL_FLIP = 6;
    private static final int ATTRIBUTES_VERTICAL_FLIP = 7;

    public static final int REG_PPU_CTRL = 0x2000;
    public static final int REG_PPU_MASK = 0x2001;
    public static final int REG_PPU_STATUS = 0x2002;
    public static final int REG_OAM_ADDR = 0x2003;
    public static final int REG_OAM_DATA = 0x2004;
    public static final int REG_PPU_SCROLL = 0x2005;
    public static final int REG_PPU_ADDR = 0x2006;
    public static final int REG_PPU_DATA = 0x2007;

    private static final long[] BITMAP_0_BITS = new long[256];
    private static final long[] BITMAP_1_BITS = new long[256];

    static {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 8; j++) {
                BITMAP_0_BITS[i] |= ((long) getBit(i, j)) << (j << 3);
            }
            BITMAP_1_BITS[i] = BITMAP_0_BITS[i] << 1;
        }
    }

    private static boolean spritesEnabled = true;
    private static boolean spriteBoxesEnabled;
    private static boolean backgroundEnabled = true;
    private static boolean showInputDevices;
    private static int highlightedSpriteX = -1;
    private static int highlightedSpriteY = -1;
    private static int highlightColor;
    private static int zapperLightDetectionMargin = 3;

    private CPU cpu;
    private Mapper mapper;
    private transient volatile MachineRunner machineRunner;
    private transient ScreenRenderer screenRenderer;
    private transient int[] lastScreen;
    private transient int[] screen;

    private final int[] OAM = new int[0x121];
    private final int[] xOAM = new int[0x100];
    private final int[] paletteRAM = new int[0x20];
    private int grayscale;
    private int emphasis;

    private int vramAddressIncrement;
    private int spritePatternTableAddress;
    private int backgroundPatternTableAddress;
    private boolean showLeftmostBackground;
    private boolean showLeftmostSprites;
    private boolean showBackground;
    private boolean showSprites;
    private boolean sprite0Hit;
    private boolean NMI_occurred;
    private boolean NMI_output;
    private int ppuDataReadBuffer;
    private boolean ntsc;
    private boolean pal;
    private int palCounter;

    private ZapperMapper zapper;
    private boolean zapperLightNotDetected;
    private int zapperMinScanline;
    private int zapperMaxScanline;
    private int zapperMinScanlineCycle;
    private int zapperMaxScanlineCycle;

    private RobController rob;

    private int V;          // current VRAM address (15 bits)
    private int T;          // temporary VRAM address (15 bits)
    private int X;          // fine X scroll (3 bits)
    private boolean W;      // write toggle

    private final long[] backgroundTiles = new long[34];
    private final int[] vramData = new int[4];
    private int tileBitmap;

    private int lastReadValue;

    public boolean frameRendering = true;
    private volatile int frameCounter;

    private boolean evenFrame = true;
    private int scanline = PRE_RENDER_SCANLINE;
    private int scanlineCount;
    private int scanlineCycle;
    private int scanlineCycleCount;
    private int nmiScanline;
    private boolean rendering;
    private int screenIndex;

    private int vramAddress;
    private int patternTableAddress;

    private final long[] spriteTiles = new long[64];
    private final int[][] spriteDatas = new int[64][2];

    private int primaryOamIndex;
    private int oamIndex;
    private int oamValue;
    private int secondaryOamIndex;
    private int spriteEvalState;
    private boolean evalSprite0InScanline;
    private int evalSpriteCount;
    private boolean spriteOverflow;
    private boolean spriteSize8x16;
    private int spriteBottomOffset;
    private int spriteCount;
    private boolean sprite0InScanline;
    private boolean noSpriteLimit;

    //  private int vCopyDelay;
    private int writeDelay;
    private int readDelay;
    private int ioValue;
    private int ioAddress;

    protected transient volatile LocalAPI localAPI;

    public static void setSpritesEnabled(final boolean spritesEnabled) {
        PPU.spritesEnabled = spritesEnabled;
    }

    public static void setSpriteBoxesEnabled(final boolean spriteBoxesEnabled) {
        PPU.spriteBoxesEnabled = spriteBoxesEnabled;
    }

    public static void setBackgroundEnabled(final boolean backgroundEnabled) {
        PPU.backgroundEnabled = backgroundEnabled;
    }

    public static void setShowInputDevices(final boolean showInputDevices) {
        PPU.showInputDevices = showInputDevices;
    }

    public static void setHighlightedSprite(final int highlightedSpriteX,
                                            final int highlightedSpriteY) {
        PPU.highlightedSpriteX = highlightedSpriteX;
        PPU.highlightedSpriteY = highlightedSpriteY;
    }

    public static void clearHighlightedSprite() {
        setHighlightedSprite(-1, -1);
    }

    public static void setZapperLightDetectionMargin(
            final int zapperLightDetectionMargin) {
        PPU.zapperLightDetectionMargin = zapperLightDetectionMargin;
    }

    public static void init() {
        final AppPrefs prefs = AppPrefs.getInstance();
        final View view = prefs.getView();
        setBackgroundEnabled(view.isBackgroundEnabled());
        setSpritesEnabled(view.isSpritesEnabled());
        setShowInputDevices(view.isShowInputDevices());
        setZapperLightDetectionMargin(prefs.getInputs()
                .getZapperLightDetectionMargin());
    }

    public PPU() {
        reset();
    }

    public void reset() {
        Arrays.fill(OAM, 0);
        Arrays.fill(xOAM, 0);
        Arrays.fill(paletteRAM, 0);
        Arrays.fill(backgroundTiles, 0);
        Arrays.fill(vramData, 0);
        Arrays.fill(spriteTiles, 0);
        for (int i = spriteDatas.length - 1; i >= 0; i--) {
            Arrays.fill(spriteDatas[i], 0);
        }

        grayscale = 0;
        emphasis = 0;
        vramAddressIncrement = 0;
        spritePatternTableAddress = 0;
        backgroundPatternTableAddress = 0;
        showLeftmostBackground = false;
        showLeftmostSprites = false;
        showBackground = false;
        showSprites = false;
        sprite0Hit = false;
        NMI_occurred = false;
        NMI_output = false;
        ppuDataReadBuffer = 0;
        palCounter = 0;
        zapperLightNotDetected = false;
        zapperMinScanline = 0;
        zapperMaxScanline = 0;
        zapperMinScanlineCycle = 0;
        zapperMaxScanlineCycle = 0;
        V = 0;
        T = 0;
        X = 0;
        W = false;
        tileBitmap = 0;
        lastReadValue = 0;
        frameRendering = true;
        scanline = PRE_RENDER_SCANLINE;
        scanlineCycle = 0;
        scanlineCycleCount = 0;
        rendering = false;
        screenIndex = 0;
        vramAddress = 0;
        patternTableAddress = 0;
        primaryOamIndex = 0;
        oamIndex = 0;
        oamValue = 0;
        secondaryOamIndex = 0;
        spriteEvalState = 0;
        evalSprite0InScanline = false;
        evalSpriteCount = 0;
        spriteOverflow = false;
        spriteSize8x16 = false;
        spriteBottomOffset = 0;
        spriteCount = 0;
        sprite0InScanline = false;
        writeDelay = 0;
        readDelay = 0;
        ioValue = 0;
        ioAddress = 0;
//    vCopyDelay = 0;

        OAM[OAM_DUMMY_BYTE_INDEX] = OAM_DUMMY_BYTE_VALUE;
        writePPUCtrl(0);
        writePPUMask(0);
    }

    public void setMachine(final Machine machine) {
        this.cpu = machine.cpu();
        this.mapper = machine.getMapper();
    }

    public void setCPU(final CPU cpu) {
        this.cpu = cpu;
    }

    public void setMapper(final Mapper mapper) {
        this.mapper = mapper;
    }

    public void setMachineRunner(final MachineRunner machineRunner) {
        this.machineRunner = machineRunner;
    }

    public void setTVSystem(final TVSystem tvSystem) {
        ntsc = tvSystem == NTSC;
        pal = tvSystem == PAL;
        scanlineCount = tvSystem.getScanlineCount();
        nmiScanline = tvSystem.getNmiScanline();
        if (scanline >= scanlineCount - 1) {
            scanline = scanlineCount - 2;
        }
    }

    public void setZapper(final ZapperMapper zapper) {
        this.zapper = zapper;
        if (zapper == null) {
            zapperLightNotDetected = false;
        } else {
            final int coordinates = zapper.getCoordinates();
            if (coordinates == 0xFFFF) {
                zapperLightNotDetected = false;
            } else {
                zapperLightNotDetected = true;

                final int x = coordinates & 0xFF;
                zapperMinScanlineCycle = x - zapperLightDetectionMargin;
                if (zapperMinScanlineCycle < 0) {
                    zapperMinScanlineCycle = 0;
                }
                zapperMaxScanlineCycle = x + zapperLightDetectionMargin;
                if (zapperMaxScanlineCycle > 255) {
                    zapperMaxScanlineCycle = 255;
                }

                final int y = (coordinates >> 8) & 0xFF;
                zapperMinScanline = y - zapperLightDetectionMargin;
                if (zapperMinScanline < 0) {
                    zapperMinScanline = 0;
                }
                zapperMaxScanline = y + zapperLightDetectionMargin;
                if (zapperMaxScanline > 239) {
                    zapperMaxScanline = 239;
                }
            }
        }
    }

    public ZapperMapper getZapper() {
        return zapper;
    }

    public RobController getRob() {
        return rob;
    }

    public void setRob(final RobController rob) {
        this.rob = rob;
    }

    public boolean isFrameRendering() {
        return frameRendering;
    }

    public void setFrameRendering(final boolean frameRendering) {
        this.frameRendering = frameRendering;
    }

    public int getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(final int frameCounter) {
        this.frameCounter = frameCounter;
    }

    public void setScreenRenderer(final ScreenRenderer renderer) {
        lastScreen = screen;
        this.screenRenderer = renderer;
        this.screen = renderer.render();
    }

    public Mapper getMapper() {
        return mapper;
    }

    public int[] getOAM() {
        return OAM;
    }

    public boolean isRendering() {
        return rendering;
    }

    public boolean isShowSprites() {
        return showSprites;
    }

    public boolean isShowBackground() {
        return showBackground;
    }

    public boolean isSpriteSize8x16() {
        return spriteSize8x16;
    }

    public int getSpritePatternTableAddress() {
        return spritePatternTableAddress;
    }

    public int getScanline() {
        return scanline;
    }

    public int getScanlineCycle() {
        return scanlineCycle;
    }

    public boolean isSprite0Hit() {
        return sprite0Hit;
    }

    public void setSprite0Hit(final boolean sprite0Hit) {
        this.sprite0Hit = sprite0Hit;
    }

    public int getScanlineCount() {
        return scanlineCount;
    }

    public boolean isNoSpriteLimit() {
        return noSpriteLimit;
    }

    public void setNoSpriteLimit(final boolean noSpriteLimit) {
        this.noSpriteLimit = noSpriteLimit;
    }

    public void setV(final int V) {
        this.V = V & 0x7FFF;
    }

    public int getV() {
        return V;
    }

    public void setT(final int T) {
        this.T = T & 0x7FFF;
    }

    public int getT() {
        return T;
    }

    public void setX(final int X) {
        this.X = X & 0x07;
    }

    public int getX() {
        return X;
    }

    public void setW(final boolean W) {
        this.W = W;
    }

    public boolean isW() {
        return W;
    }

    public void writeRegister(final int register, final int value) {
        lastReadValue = value;
        switch (register) {
            case REG_PPU_CTRL:
                writePPUCtrl(value);
                break;
            case REG_PPU_MASK:
                writePPUMask(value);
                break;
            case REG_OAM_ADDR:
                writeOAMAddr(value);
                break;
            case REG_OAM_DATA:
                writeOAMData(value);
                break;
            case REG_PPU_SCROLL:
                writePPUScroll(value);
                break;
            case REG_PPU_ADDR:
                writePPUAddr(value);
                break;
            case REG_PPU_DATA:
                writePPUData(value);
                break;
        }
    }

    public int peekRegister(final int register) {
        switch (register) {
            case REG_PPU_STATUS:
                return peekPPUStatus();
            case REG_OAM_DATA:
                return peekOAMData();
            case REG_PPU_DATA:
                return peekPPUData();
            default:
                return lastReadValue;
        }
    }

    public int readRegister(final int register) {
        switch (register) {
            case REG_PPU_STATUS:
                return readPPUStatus();
            case REG_OAM_DATA:
                return readOAMData();
            case REG_PPU_DATA:
                return readPPUData();
            default:
                return lastReadValue;
        }
    }

    public void update() {
        if (pal) {
            if (palCounter == 4) {
                palCounter = 0;
                executeCycle();
            } else {
                palCounter++;
            }
        }
        executeCycle();
        executeCycle();
        executeCycle();
    }

    private void incrementCoarseX() {
        if ((V & 0x001F) == 0x001F) {
            V ^= 0x041F;
        } else {
            V++;
        }
    }

    private void incrementY() {
        if ((V & 0x7000) == 0x7000) {
            final int y = V & 0x03E0;
            V &= 0x0FFF;
            if (y == 0x03A0) {
                V ^= 0x0BA0;
            } else if (y == 0x03E0) {
                V ^= 0x03E0;
            } else {
                V += 0x0020;
            }
        } else {
            V += 0x1000;
        }
    }

    private void incrementVRAMAddress() {
        if (rendering) {
            if ((scanlineCycle & 7) != 3 || (scanlineCycle > 251
                    && (scanlineCycle < 320 || scanlineCycle >= 337))) {
                incrementCoarseX();
            }
            if (scanlineCycle != 251) {
                incrementY();
            }
        } else {
            V = (V + vramAddressIncrement) & 0x7FFF;
        }
    }

    private void evaluateSprites() {

        final boolean readCycle = isOdd(scanlineCycle);

        if (scanlineCycle == 0) {
            if (scanline == PRE_RENDER_SCANLINE && (primaryOamIndex & 0xF8) != 0) {
                System.arraycopy(OAM, primaryOamIndex & 0xF8, OAM, 0, 8);
            }
            oamIndex = primaryOamIndex = secondaryOamIndex = 0;
        } else if (scanlineCycle <= 64) {
            if (readCycle) {
                oamIndex = OAM_DUMMY_BYTE_INDEX;
                oamValue = OAM[oamIndex];
            } else {
                oamIndex = SECONDARY_OAM_OFFSET | secondaryOamIndex;
                OAM[oamIndex] = oamValue;
                secondaryOamIndex = (secondaryOamIndex + 1) & 0x1F;
            }
            if (scanlineCycle == 64) {
                spriteEvalState = EVAL_STATE_COPY_Y;
                evalSpriteCount = 0;
                evalSprite0InScanline = false;
            }
        } else if (scanlineCycle <= 256) {
            if (readCycle) {
                oamIndex = primaryOamIndex;
                oamValue = OAM[oamIndex];
            } else {
                switch (spriteEvalState) {
                    case EVAL_STATE_COPY_Y:
                        oamIndex = SECONDARY_OAM_OFFSET | secondaryOamIndex;
                        OAM[oamIndex] = oamValue;
                        if ((scanline >= oamValue) && (scanline <= oamValue
                                + spriteBottomOffset)) {
                            evalSpriteCount++;
                            if (primaryOamIndex == 0) {
                                evalSprite0InScanline = true;
                            }
                            spriteEvalState = EVAL_STATE_COPY_REMAINING;
                            primaryOamIndex = (primaryOamIndex + 1) & 0xFF;
                            secondaryOamIndex = (secondaryOamIndex + 1) & 0x1F;
                        } else {
                            primaryOamIndex = (primaryOamIndex + 4) & 0xFC;
                            if (primaryOamIndex == 0) {
                                spriteEvalState = EVAL_STATE_SPIN;
                            }
                        }
                        break;
                    case EVAL_STATE_COPY_REMAINING:
                        oamIndex = SECONDARY_OAM_OFFSET | secondaryOamIndex;
                        OAM[oamIndex] = oamValue;
                        primaryOamIndex = (primaryOamIndex + 1) & 0xFF;
                        secondaryOamIndex = (secondaryOamIndex + 1) & 0x1F;
                        if ((secondaryOamIndex & 0x03) == 0) {
                            if (primaryOamIndex < 4) {
                                spriteEvalState = EVAL_STATE_SPIN;
                            } else if (secondaryOamIndex == 0) {
                                spriteEvalState = EVAL_STATE_SPRITE_8_Y;
                            } else {
                                spriteEvalState = EVAL_STATE_COPY_Y;
                            }
                        }
                        break;
                    case EVAL_STATE_SPRITE_8_Y:
                        oamIndex = SECONDARY_OAM_OFFSET | secondaryOamIndex;
                        if ((scanline >= oamValue) && (scanline <= oamValue
                                + spriteBottomOffset)) {
                            spriteEvalState = EVAL_STATE_SPRITE_8_REMAINING;
                            spriteOverflow = true;
                            primaryOamIndex = (primaryOamIndex + 1) & 0xFF;
                            secondaryOamIndex = 1;
                        } else {
                            primaryOamIndex = (primaryOamIndex + (((primaryOamIndex & 0x03)
                                    == 0x03) ? 1 : 5)) & 0xFF;
                            if (primaryOamIndex < 4) {
                                spriteEvalState = EVAL_STATE_SPIN;
                            }
                        }
                        break;
                    case EVAL_STATE_SPRITE_8_REMAINING:
                        oamIndex = SECONDARY_OAM_OFFSET;
                        primaryOamIndex = (primaryOamIndex + 1) & 0xFF;
                        if (++secondaryOamIndex == 4) {
                            secondaryOamIndex = 0;
                            if ((primaryOamIndex & 0x03) != 0) {
                                primaryOamIndex &= 0xFC;
                            } else {
                                primaryOamIndex = (primaryOamIndex + 4) & 0xFC;
                            }
                            spriteEvalState = EVAL_STATE_SPIN;
                        }
                        break;
                    case EVAL_STATE_SPIN:
                        oamIndex = SECONDARY_OAM_OFFSET | secondaryOamIndex;
                        primaryOamIndex = (primaryOamIndex + 4) & 0xFC;
                        break;
                }
            }
        } else if (scanlineCycle <= 320) {
            if (scanlineCycle == 257) {
                spriteCount = evalSpriteCount << 2;
                sprite0InScanline = evalSprite0InScanline;
                primaryOamIndex = secondaryOamIndex = 0;
                out1:
                if (noSpriteLimit) {
                    out2:
                    if (spriteCount == 32) {
                        final int y = OAM[SECONDARY_OAM_OFFSET | 0x1C];
                        final int x = OAM[SECONDARY_OAM_OFFSET | 0x1C | SPRITE_X];
                        // Don't compare against first sprite for Felix the Cat.
                        for (int i = 6; i > 0; i--) {
                            final int address = SECONDARY_OAM_OFFSET | (i << 2);
                            if (y != OAM[address] || x != OAM[address | SPRITE_X]) {
                                break out2;
                            }
                        }
                        break out1;
                    }
                    System.arraycopy(OAM, SECONDARY_OAM_OFFSET, xOAM, 0, spriteCount);
                    int x = 0;
                    for (int p = 0; p < 256; p += 4) {
                        final int spriteY = OAM[p];
                        if ((scanline >= spriteY) && (scanline <= spriteY
                                + spriteBottomOffset)) {
                            if (x >= spriteCount) {
                                xOAM[x] = OAM[p];
                                xOAM[x | 1] = OAM[p | 1];
                                xOAM[x | 2] = OAM[p | 2];
                                xOAM[x | 3] = OAM[p | 3];
                            }
                            x += 4;
                        }
                    }
                    if (x > spriteCount) {
                        spriteCount = x;
                    }
                }
            }
            oamIndex = SECONDARY_OAM_OFFSET | secondaryOamIndex;
            if (((scanlineCycle - 1) & 4) < 4) {
                secondaryOamIndex = (secondaryOamIndex + 1) & 0x1F;
            }
        } else {
            oamIndex = SECONDARY_OAM_OFFSET;
        }
    }

    private void executeCycle() {

        scanlineCycle++;
        if (scanlineCycle == 256) {
            if (scanline < 240) {
                for (int i = backgroundTiles.length - 1; i >= 0; i--) {
                    backgroundTiles[i] = 0L;
                }
            }
        } else if (scanlineCycle >= 279 && scanlineCycle <= 303) {
            if (rendering && scanline == PRE_RENDER_SCANLINE) {
                V = (V & 0x841F) | (T & 0x7BE0);
            }
        } else if (scanlineCycle == 338) {
            scanlineCycleCount = scanline == PRE_RENDER_SCANLINE
                    && evenFrame && rendering && ntsc ? 340 : 341;
        } else if (scanlineCycle == scanlineCycleCount) {
            scanlineCycle = 0;
            scanline++;
            if (scanline == 240) {
                final MachineRunner runner = machineRunner;
                drawHighlightedSprite();
                if (showInputDevices) {
                    final DeviceMapper[] deviceMappers = mapper.getDeviceMappers();
                    if (deviceMappers != null) {
                        for (int i = deviceMappers.length - 1; i >= 0; i--) {
                            deviceMappers[i].render(screen);
                        }
                    }
                }
                final LocalAPI api = localAPI;
                if (api != null) {
                    api.frameRendered(screen);
                }
                if (rob != null) {
                    rob.signal(PaletteUtil.ROB_COLORS[screen[0]]);
                }
                if (runner != null) {
                    runner.frameRendered(screen);
                }
                lastScreen = screen;
                screen = screenRenderer.render();
            } else if (scanline == nmiScanline) {
                NMI_occurred = true;
                if (NMI_output) {
                    cpu.interrupt().setNMI(true);
                }
            } else if (scanline == scanlineCount - 1) {
                scanline = PRE_RENDER_SCANLINE;
                evenFrame = !evenFrame;
                screenIndex = 0;
                frameRendering = false;
                frameCounter++;
            }
            if (zapper != null) {
                zapper.handleScanline();
            }
            updateRendering();
        } else if (scanline == PRE_RENDER_SCANLINE && scanlineCycle == 1) {
            spriteOverflow = sprite0Hit = NMI_occurred = false;
        }

        if (rendering) {
            evaluateSprites();
            final boolean writeCycle = isOdd(scanlineCycle);
            if (writeCycle) {
                if (writeDelay > 0 || readDelay > 0) {
                    vramData[(scanlineCycle >> 1) & 3] = ioValue = 0;
                    if (writeDelay == 1) {
                        writeVRAM(vramAddress, 0);
                    }
                } else {
                    vramData[(scanlineCycle >> 1) & 3] = readVRAM(vramAddress);
                }
            }
            if (scanlineCycle < 256) {
                switch (scanlineCycle & 7) {
                    case 0:
                        vramAddress = 0x2000 | (V & 0x0FFF);
                        break;
                    case 1:
                        patternTableAddress = (vramData[BACKGROUND_NAMETABLE_ADDRESS]
                                << 4) | (V >> 12) | backgroundPatternTableAddress;
                        break;
                    case 2:
                        vramAddress = 0x23C0 | (V & 0x0C00) | ((V & 0x0380) >> 4)
                                | ((V & 0x001C) >> 2);
                        break;
                    case 3:
                        backgroundTiles[(scanlineCycle + 13) >> 3]
                                = ((vramData[BACKGROUND_ATTRIBUTES]
                                >> (((V & 0x0040) >> 4) | (V & 0x0002))) & 3)
                                * 0x0404040404040404L;
                        incrementCoarseX();
                        if (scanlineCycle == 251) {
                            incrementY();
                        }
                        break;
                    case 4:
                        vramAddress = patternTableAddress;
                        break;
                    case 5:
                        tileBitmap = reverseBits(vramData[BACKGROUND_BITMAP_0]);
                        backgroundTiles[(scanlineCycle + 11) >> 3]
                                |= BITMAP_0_BITS[tileBitmap];
                        break;
                    case 6:
                        vramAddress = patternTableAddress | 0x0008;
                        break;
                    case 7:
                        tileBitmap = reverseBits(vramData[BACKGROUND_BITMAP_1]);
                        backgroundTiles[(scanlineCycle + 9) >> 3]
                                |= BITMAP_1_BITS[tileBitmap];
                        break;
                }
            } else if (scanlineCycle < 320) {
                switch (scanlineCycle & 7) {
                    case 0:
                        vramAddress = 0x2000 | (V & 0x0FFF);
                        break;
                    case 1:
                        if (scanlineCycle == 257) {
                            V = (V & 0x7BE0) | (T & 0x041F);
                        }
                        break;
                    case 2:
                        vramAddress = 0x2000 | (V & 0x0FFF);
                        break;
                    case 3: {
                        final int spriteOffset = (scanlineCycle >> 1) & 0x1C;
                        tileBitmap = OAM[SECONDARY_OAM_OFFSET | spriteOffset | SPRITE_TILE];
                        final int spriteScanline = scanline
                                - OAM[SECONDARY_OAM_OFFSET | spriteOffset];
                        if (spriteSize8x16) {
                            patternTableAddress = ((tileBitmap & 0xFE) << 4)
                                    | ((tileBitmap & 0x01) << 12)
                                    | ((spriteScanline & 7) ^ (getBitBool(OAM[SECONDARY_OAM_OFFSET
                                    | spriteOffset | SPRITE_ATTRIBUTES], ATTRIBUTES_VERTICAL_FLIP)
                                    ? 0x17 : 0x00) ^ ((spriteScanline & 0x08) << 1));
                        } else {
                            patternTableAddress = (tileBitmap << 4) | ((spriteScanline & 7)
                                    ^ (getBitBool(OAM[SECONDARY_OAM_OFFSET | spriteOffset
                                    | SPRITE_ATTRIBUTES], ATTRIBUTES_VERTICAL_FLIP)
                                    ? 0x07 : 0x00)) | spritePatternTableAddress;
                        }
                        break;
                    }
                    case 4:
                        vramAddress = patternTableAddress;
                        break;
                    case 5: {
                        final int spriteTileOffset = (scanlineCycle >> 1) & 0x1E;
                        if ((OAM[SECONDARY_OAM_OFFSET | spriteTileOffset] & 0x40) != 0) {
                            tileBitmap = vramData[BACKGROUND_BITMAP_0];
                        } else {
                            tileBitmap = reverseBits(vramData[BACKGROUND_BITMAP_0]);
                        }
                        spriteTiles[spriteTileOffset >> 2] = BITMAP_0_BITS[tileBitmap]
                                | ((OAM[SECONDARY_OAM_OFFSET | spriteTileOffset] & 3)
                                * 0x0404040404040404L);
                        break;
                    }
                    case 6:
                        vramAddress = patternTableAddress | 0x0008;
                        break;
                    case 7: {
                        final int spriteTileOffset = (scanlineCycle >> 1) & 0x1E;
                        if ((OAM[SECONDARY_OAM_OFFSET | spriteTileOffset] & 0x40) != 0) {
                            tileBitmap = vramData[BACKGROUND_BITMAP_1];
                        } else {
                            tileBitmap = reverseBits(vramData[BACKGROUND_BITMAP_1]);
                        }
                        final int spriteIndex = spriteTileOffset >> 2;
                        spriteTiles[spriteIndex] |= BITMAP_1_BITS[tileBitmap];
                        spriteDatas[spriteIndex][SPRITE_DATA_ATTRIBUTES]
                                = OAM[SECONDARY_OAM_OFFSET | spriteTileOffset];
                        spriteDatas[spriteIndex][SPRITE_DATA_X]
                                = OAM[SECONDARY_OAM_OFFSET | spriteTileOffset | 1];
                        break;
                    }
                }

                if (noSpriteLimit && scanlineCycle == 319 && spriteCount > 32) {
                    for (int i = 0; i < spriteCount; i += 4) {
                        int bitmap = xOAM[i | SPRITE_TILE];
                        final int spriteScanline = scanline - xOAM[i];
                        final int address;
                        if (spriteSize8x16) {
                            address = ((bitmap & 0xFE) << 4)
                                    | ((bitmap & 0x01) << 12)
                                    | ((spriteScanline & 7) ^ (getBitBool(xOAM[i
                                    | SPRITE_ATTRIBUTES], ATTRIBUTES_VERTICAL_FLIP)
                                    ? 0x17 : 0x00) ^ ((spriteScanline & 0x08) << 1));
                        } else {
                            address = (bitmap << 4) | ((spriteScanline & 7)
                                    ^ (getBitBool(xOAM[i | SPRITE_ATTRIBUTES],
                                    ATTRIBUTES_VERTICAL_FLIP) ? 0x07 : 0x00))
                                    | spritePatternTableAddress;
                        }

                        final boolean flipHorizontally = (xOAM[i | SPRITE_ATTRIBUTES]
                                & 0x40) != 0;
                        int value = peekVRAM(address);
                        if (flipHorizontally) {
                            bitmap = value;
                        } else {
                            bitmap = reverseBits(value);
                        }
                        final int spriteIndex = i >> 2;
                        spriteTiles[spriteIndex] = BITMAP_0_BITS[bitmap]
                                | ((xOAM[i | SPRITE_ATTRIBUTES] & 3)
                                * 0x0404040404040404L);

                        value = peekVRAM(address | 0x0008);
                        if (flipHorizontally) {
                            bitmap = value;
                        } else {
                            bitmap = reverseBits(value);
                        }
                        spriteTiles[spriteIndex] |= BITMAP_1_BITS[bitmap];
                        spriteDatas[spriteIndex][SPRITE_DATA_ATTRIBUTES]
                                = xOAM[i | SPRITE_ATTRIBUTES];
                        spriteDatas[spriteIndex][SPRITE_DATA_X] = xOAM[i | SPRITE_X];
                    }
                }
            } else if (scanlineCycle < 337) {
                switch (scanlineCycle & 7) {
                    case 0:
                        vramAddress = 0x2000 | (V & 0x0FFF);
                        break;
                    case 1:
                        patternTableAddress = (vramData[BACKGROUND_NAMETABLE_ADDRESS]
                                << 4) | (V >> 12) | backgroundPatternTableAddress;
                        if (scanlineCycle == 321) {
                            App.scanlineRendered(scanline);
                        }
                        break;
                    case 2:
                        vramAddress = 0x23C0 | (V & 0x0C00) | ((V & 0x0380) >> 4)
                                | ((V & 0x001C) >> 2);
                        break;
                    case 3:
                        backgroundTiles[(scanlineCycle - 323) >> 3]
                                = ((vramData[BACKGROUND_ATTRIBUTES]
                                >> (((V & 0x0040) >> 4) | (V & 0x0002))) & 3)
                                * 0x0404040404040404L;
                        incrementCoarseX();
                        break;
                    case 4:
                        vramAddress = patternTableAddress;
                        break;
                    case 5:
                        tileBitmap = reverseBits(vramData[BACKGROUND_BITMAP_0]);
                        backgroundTiles[(scanlineCycle - 325) >> 3]
                                |= BITMAP_0_BITS[tileBitmap];
                        break;
                    case 6:
                        vramAddress = patternTableAddress | 0x0008;
                        break;
                    case 7:
                        tileBitmap = reverseBits(vramData[BACKGROUND_BITMAP_1]);
                        backgroundTiles[(scanlineCycle - 327) >> 3]
                                |= BITMAP_1_BITS[tileBitmap];
                        break;
                }
            } else if (scanlineCycle == 338) {
                vramAddress = 0x2000 | (V & 0x0FFF);
            }

            if (!writeCycle) {
                handlePpuCycle(scanline, scanlineCycle, vramAddress, rendering);
                if (writeDelay == 1) {
                    writeVRAM(vramAddress, vramAddress & 0x00FF);
                }
            }
        }
        if (!rendering && (writeDelay == 3 || readDelay == 3)) {
            handlePpuCycle(scanline, scanlineCycle, ioAddress, rendering);
        }
        if (writeDelay > 0 && --writeDelay == 0 && !rendering) {
            writeVRAM(ioAddress, ioValue);
        }
        if (readDelay > 0 && --readDelay == 0 && !rendering) {
            ppuDataReadBuffer = readVRAM(ioAddress);
        }
        if (!rendering && (writeDelay | readDelay) == 0) {
            handlePpuCycle(scanline, scanlineCycle, V, rendering);
        }
        if (scanline < 240 && scanline != PRE_RENDER_SCANLINE
                && scanlineCycle < 256) {
            if (showBackground && (scanlineCycle >= 8 || showLeftmostBackground)) {
                final int index = scanlineCycle + X;
                tileBitmap = (byte) (backgroundTiles[index >> 3] >> ((index & 7) << 3));
            } else {
                tileBitmap = 0;
            }
            boolean spriteRendered = false;
            boolean invertPalette = false;
            if (showSprites && (scanlineCycle >= 8 || showLeftmostSprites)) {
                for (int y = 0; y < spriteCount; y += 4) {
                    final int spritePixel = scanlineCycle
                            - spriteDatas[y >> 2][SPRITE_DATA_X];
                    if ((spritePixel & ~7) != 0) {
                        continue;
                    }
                    final int spriteTile = (byte) (spriteTiles[y >> 2]
                            >> (spritePixel << 3));
                    if ((spriteTile & 3) != 0) {
                        if (sprite0InScanline && y == 0 && (tileBitmap & 3) != 0
                                && scanlineCycle < 255) {
                            sprite0Hit = true;
                            sprite0InScanline = false;
                            final LocalAPI api = localAPI;
                            if (api != null) {
                                api.spriteZeroHit(scanline, scanlineCycle);
                            }
                        }
                        if (spritesEnabled && ((tileBitmap & 3) == 0
                                || (spriteDatas[y >> 2][SPRITE_DATA_ATTRIBUTES] & 0x20) == 0)) {
                            tileBitmap = spriteTile | 0x10;
                            spriteRendered = true;
                        }
                        break;
                    }
                    invertPalette = spriteBoxesEnabled;
                }
            }
            if (!(spriteRendered || backgroundEnabled)) {
                tileBitmap = 0;
            }

            final int paletteIndex;
            if (!rendering && (V & 0x3F00) == 0x3F00) {
                paletteIndex = paletteRAM[V & 0x1F];
            } else if ((tileBitmap & 3) != 0) {
                paletteIndex = paletteRAM[tileBitmap & 0x1F];
            } else {
                paletteIndex = paletteRAM[0];
            }
            if (zapperLightNotDetected
                    && scanline >= zapperMinScanline && scanline <= zapperMaxScanline
                    && scanlineCycle >= zapperMinScanlineCycle
                    && scanlineCycle <= zapperMaxScanlineCycle
                    && PaletteUtil.ZAPPER_COLORS[paletteIndex]) {
                zapperLightNotDetected = false;
                zapper.handleLightDetected();
            }
            screen[screenIndex++] = emphasis | (invertPalette
                    ? INVERSE_PALETTE[paletteIndex & grayscale]
                    : (paletteIndex & grayscale));
        }
//    if (vCopyDelay > 0 && --vCopyDelay == 0) {
//      V = T;
//    }
    }

    private void drawHighlightedSprite() {
        final int spriteX = highlightedSpriteX;
        if (spriteX < 0) {
            return;
        }
        final int spriteY = highlightedSpriteY;
        if (spriteY >= 240) {
            return;
        }
        drawRect(screen, spriteX, spriteY, 8, spriteSize8x16 ? 16 : 8,
                highlightColor);
        highlightColor = (highlightColor + 1) & 0x1F;
    }

    private void handlePpuCycle(final int scanline, final int scanlineCycle,
                                final int address, final boolean rendering) {
        mapper.handlePpuCycle(scanline, scanlineCycle, address, rendering);
        final LocalAPI api = localAPI;
        if (api != null) {
            api.cyclePerformed(scanline, scanlineCycle, address, rendering);
        }
    }

    public int[] getScreen() {
        return screen;
    }

    public int[] getLastScreen() {
        return lastScreen;
    }

    public LocalAPI getLocalAPI() {
        return localAPI;
    }

    public void setLocalAPI(final LocalAPI localAPI) {
        this.localAPI = localAPI;
    }

    public void clearLocalAPI() {
        setLocalAPI(null);
    }

    public int getNextScanline() {
        final int value = scanline + 1;
        return value == scanlineCount - 1 ? PRE_RENDER_SCANLINE : value;
    }

    public int getNextScanlineCycle() {
        final int value = scanlineCycle + 1;
        return value >= (scanline == PRE_RENDER_SCANLINE && evenFrame && rendering
                && ntsc ? 340 : 341) ? 0 : value;
    }

    public int[] getPaletteRAM() {
        return paletteRAM;
    }

    public int getPaletteRamValue(final int index) {
        return paletteRAM[((index & 3) == 0) ? 0 : (index & 0x1F)];
    }

    public void setPaletteRamValue(final int index, final int value) {
        paletteRAM[((index & 3) == 0) ? 0 : (index & 0x1F)] = value;
    }

    public int getBackgroundPatternTableAddress() {
        return backgroundPatternTableAddress;
    }

    public int getScrollX() {
        return ((V >> 2) & 0x100) | ((V << 3) & 0xF8) | (X & 0x07);
    }

    public void setScrollX(final int scrollX) {
        X = scrollX & 0x07;
        V = (V & 0x7BE0) | ((scrollX & 0x100) << 2) | ((scrollX & 0xF8) >> 3);
    }

    public int getScrollY() {
        final int scrollY = ((V >> 3) & 0x100) | ((V >> 2) & 0xF8)
                | ((V >> 12) & 0x07);
        return (scrollY >= 240) ? (scrollY - 16) : scrollY;
    }

    public void setScrollY(int scrollY) {
        scrollY &= 0x1FF;
        if (scrollY >= 240) {
            scrollY += 16;
        }
        V = (V & 0x0C1F) | ((scrollY & 0x100) << 3) | ((scrollY & 0xF8) << 2)
                | ((scrollY & 0x07) << 12);
    }

    private void writePPUCtrl(final int value) {

        final boolean nextNmiOutput = getBitBool(value, 7);
        if (nextNmiOutput) {
            if (!NMI_output && NMI_occurred) {
                cpu.interrupt().setNMI(true);
            }
        } else if (scanline == nmiScanline && scanlineCycle < 3) {
            cpu.interrupt().setNMI(false);
        }

        vramAddressIncrement = getBitBool(value, 2) ? 32 : 1;
        spritePatternTableAddress = getBit(value, 3) << 12;
        backgroundPatternTableAddress = getBit(value, 4) << 12;
        spriteSize8x16 = getBitBool(value, 5);
        spriteBottomOffset = spriteSize8x16 ? 15 : 7;
        NMI_output = nextNmiOutput;
        T = (T & 0x73FF) | ((value & 3) << 10);
    }

    private void writePPUMask(final int value) {
        grayscale = getBitBool(value, 0) ? 0x30 : 0x3F;
        showLeftmostBackground = getBitBool(value, 1);
        showLeftmostSprites = getBitBool(value, 2);
        showBackground = getBitBool(value, 3);
        showSprites = getBitBool(value, 4);
        emphasis = (value & 0xE0) << 1;
        updateRendering();
    }

    private int readPPUStatus() {
        int value = lastReadValue & 0x1F;
        if (NMI_occurred) {
            value |= 0x80;
        }
        if (sprite0Hit) {
            value |= 0x40;
        }
        if (spriteOverflow) {
            value |= 0x20;
        }
        NMI_occurred = false;
        if (scanline == nmiScanline) {
            if (scanlineCycle == 0) {
                value &= 0x7F;
            }
            if (scanlineCycle < 3) {
                cpu.interrupt().setNMI(false);
            }
        }
        W = false;
        return lastReadValue = value;
    }

    private int peekPPUStatus() {
        int value = lastReadValue & 0x1F;
        if (NMI_occurred) {
            value |= 0x80;
        }
        if (sprite0Hit) {
            value |= 0x40;
        }
        if (spriteOverflow) {
            value |= 0x20;
        }
        if (scanline == nmiScanline && scanlineCycle == 0) {
            value &= 0x7F;
        }
        return value;
    }

    private void writeOAMAddr(final int value) {
        primaryOamIndex = value;
    }

    private int readOAMData() {
        return lastReadValue = OAM[rendering ? oamIndex : primaryOamIndex];
    }

    private int peekOAMData() {
        return OAM[rendering ? oamIndex : primaryOamIndex];
    }

    private void writeOAMData(int value) {
        if (rendering) {
            value = 0xFF;
        }
        if ((primaryOamIndex & 0x03) == 0x02) {
            value &= 0xE3;
        }
        OAM[primaryOamIndex] = value;
        primaryOamIndex = (primaryOamIndex + 1) & 0xFF;
    }

    private void writePPUScroll(final int value) {
        if (W) {
            T = (T & 0x0C1F) | ((value & 7) << 12) | ((value & 0xF8) << 2);
        } else {
            T = (T & 0x7FE0) | (value >> 3);
            X = value & 7;
        }
        W = !W;
    }

    private void writePPUAddr(final int value) {
        if (W) {
//      vCopyDelay = 3;
            V = T = (T & 0x7F00) | value;
        } else {
            T = (T & 0x00FF) | ((value & 0x3F) << 8);
        }
        W = !W;
    }

    private void writePPUData(int value) {
        if ((V & 0x3F00) == 0x3F00) {
            final int address = V & 0x001F;
            value &= 0x3F;
            paletteRAM[address] = value;
            if ((address & 0x0003) == 0) {
                paletteRAM[address ^ 0x0010] = value;
            }
        } else {
            ioAddress = V;
            ioValue = value;
            writeDelay = 3;
        }
        incrementVRAMAddress();
    }

    private int readPPUData() {
        readDelay = 3;
        ioAddress = V;
        incrementVRAMAddress();
        if ((ioAddress & 0x3F00) == 0x3F00) {
            lastReadValue &= 0xC0;
            if (grayscale == 0x30) {
                lastReadValue |= paletteRAM[ioAddress & 0x1F] & 0x30;
            } else {
                lastReadValue |= paletteRAM[ioAddress & 0x1F];
            }
        } else {
            lastReadValue = ppuDataReadBuffer;
        }
        return lastReadValue;
    }

    private int peekPPUData() {
        int value = lastReadValue;
        if ((ioAddress & 0x3F00) == 0x3F00) {
            value = (value & 0xC0) | paletteRAM[ioAddress & 0x1F];
        } else {
            value = ppuDataReadBuffer;
        }
        return value;
    }

    public int peekVRAM(final int address) {
        return mapper.peekVRAM(mapper.maskVRAMAddress(address));
    }

    private int readVRAM(final int address) {
        return mapper.readVRAM(mapper.maskVRAMAddress(address));
    }

    public void writeVRAM(final int address, final int value) {
        mapper.writeVRAM(mapper.maskVRAMAddress(address), value);
    }

    private void updateRendering() {
        rendering = (showSprites || showBackground) && scanline < 240;
    }
}