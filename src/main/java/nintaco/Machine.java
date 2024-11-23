package nintaco;

import java.io.*;

import nintaco.apu.*;
import nintaco.mappers.*;
import nintaco.mappers.nintendo.vs.*;

import static nintaco.mappers.nintendo.vs.VsPPU.*;

public class Machine implements Serializable {

    private static final long serialVersionUID = 0;

    private final Mapper mapper;
    private final CPU cpu;
    private final PPU ppu;
    private final APU apu;
    private final boolean vsDualSystem;

    public Machine(final Mapper mapper, final VsGame vsGame) {

        this.mapper = mapper;

        if (vsGame != null) {
            vsDualSystem = vsGame.isDualSystemGame();
            if (vsDualSystem) {
                cpu = new MainCPU(((MainVsDualSystem) mapper).getSubVsDualSystem());
                apu = new DualAPU((DualAPU) ((MainCPU) cpu).getSubAPU());
            } else {
                cpu = new CPU();
                apu = new APU();
            }
            switch (vsGame.getPPU()) {
                case RC2C05_01:
                case RC2C05_02:
                case RC2C05_03:
                case RC2C05_04:
                case RC2C05_05:
                    ppu = new PPU2C05(vsGame.getPpuStatusID());
                    break;
                default:
                    ppu = new PPU();
                    break;
            }
        } else {
            vsDualSystem = false;
            cpu = new CPU();
            ppu = new PPU();
            apu = new APU();
        }

        cpu.setMachine(this);
        ppu.setMachine(this);
        apu.setMachine(this);
        apu.getDMC().setCPU(cpu);
        apu.reset();
    }

    // For Force Ejection
    public Machine(final Mapper mapper, final Machine machine) {

        mapper.copyMemory(machine.getMapper());
        this.mapper = mapper;
        this.vsDualSystem = machine.isVsDualSystem();

        cpu = machine.getCPU();
        apu = machine.getAPU();
        ppu = machine.getPPU();

        cpu.setMachine(this);
        ppu.setMachine(this);
        apu.setMachine(this);
        apu.getDMC().setCPU(cpu);
    }

    public Mapper getMapper() {
        return mapper;
    }

    public CPU getCPU() {
        return cpu;
    }

    public PPU getPPU() {
        return ppu;
    }

    public APU getAPU() {
        return apu;
    }

    public boolean isVsDualSystem() {
        return vsDualSystem;
    }
}
