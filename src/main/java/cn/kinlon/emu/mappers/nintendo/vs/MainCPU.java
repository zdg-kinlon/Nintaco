package cn.kinlon.emu.mappers.nintendo.vs;

import cn.kinlon.emu.cpu.CPU;
import cn.kinlon.emu.ppu.PPU;
import cn.kinlon.emu.ScreenRenderer;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.tv.TVSystem;

import java.io.IOException;
import java.io.ObjectInputStream;

import static cn.kinlon.emu.gui.image.ImagePane.IMAGE_HEIGHT;
import static cn.kinlon.emu.gui.image.ImagePane.IMAGE_WIDTH;

// Main CPU for VS. DualSystem
public class MainCPU extends CPU {

    private static final long serialVersionUID = 0;
    private final CPU subCPU;
    private final PPU subPPU;
    private final APU subAPU;
    private transient int[] screen = new int[IMAGE_WIDTH * IMAGE_HEIGHT];

    public MainCPU(final SubVsDualSystem subVsDualSystem) {

        subCPU = new CPU();
        subPPU = new PPU();
        subAPU = new DualAPU();

        subVsDualSystem.setTVSystem(TVSystem.NTSC);

        subCPU.setMapper(subVsDualSystem);
        subCPU.setPPU(subPPU);
        subCPU.setAPU(subAPU);

        subPPU.setMapper(subVsDualSystem);
        subPPU.setTVSystem(TVSystem.NTSC);
        subPPU.setCPU(subCPU);
        clearScreenRenderer();

        subAPU.setCPU(subCPU);
        subAPU.getDMC().setCPU(subCPU);
        subAPU.setMapper(subVsDualSystem);
        subAPU.getDMC().setPAL(false);
        subAPU.setTVSystem(TVSystem.NTSC);
        subAPU.reset();
    }

    @Override
    public void reset() {
        super.reset();
        subCPU.reset();
    }

    @Override
    public void executeInstruction() {
        super.executeInstruction();

        // Keep Sub CPU lagging by about 2 instructions to ensure that the
        // Main PPU always finishes rendering a frame slightly before the
        // Sub PPU. This is required for rewind time and movies.
        final long mainCycleCounter = getCycleCounter() - 16;
        while (subCPU.getCycleCounter() < mainCycleCounter) {
            subCPU.executeInstruction();
        }
    }

    public CPU getSubCPU() {
        return subCPU;
    }

    public PPU getSubPPU() {
        return subPPU;
    }

    public APU getSubAPU() {
        return subAPU;
    }

    public void clearScreenRenderer() {
        setScreenRenderer(null);
    }

    public void setScreenRenderer(final ScreenRenderer screenRenderer) {
        if (screenRenderer == null) {
            subPPU.setScreenRenderer(() -> screen);
        } else {
            subPPU.setScreenRenderer(screenRenderer);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        screen = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
        subPPU.setScreenRenderer(() -> screen);
    }
}