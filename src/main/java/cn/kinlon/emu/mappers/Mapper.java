package cn.kinlon.emu.mappers;

import java.io.*;
import java.util.*;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.PPU;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.cpu.CPU;
import cn.kinlon.emu.cpu.Register;
import cn.kinlon.emu.files.*;
import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.Ports;
import cn.kinlon.emu.mappers.ae.Mapper228;
import cn.kinlon.emu.mappers.american.DeathRace;
import cn.kinlon.emu.mappers.ave.Maxi15;
import cn.kinlon.emu.mappers.ave.NINA001;
import cn.kinlon.emu.mappers.ave.NINA003006;
import cn.kinlon.emu.mappers.ave.NINA06;
import cn.kinlon.emu.mappers.bandai.*;
import cn.kinlon.emu.mappers.bitcorp.CrimeBusters;
import cn.kinlon.emu.mappers.bitcorp.Mapper357;
import cn.kinlon.emu.mappers.bitcorp.Mapper360;
import cn.kinlon.emu.mappers.capcom.UN1ROM;
import cn.kinlon.emu.mappers.carson.Carson;
import cn.kinlon.emu.mappers.carson.Mapper291;
import cn.kinlon.emu.mappers.ce.Mapper240;
import cn.kinlon.emu.mappers.ce.Mapper244;
import cn.kinlon.emu.mappers.ce.Mapper246;
import cn.kinlon.emu.mappers.codemasters.BF909x;
import cn.kinlon.emu.mappers.codemasters.GoldenFive;
import cn.kinlon.emu.mappers.codemasters.Mapper232;
import cn.kinlon.emu.mappers.colordreams.ColorDreams;
import cn.kinlon.emu.mappers.colordreams.RumbleStation;
import cn.kinlon.emu.mappers.daouinfosys.DaouInfosys;
import cn.kinlon.emu.mappers.daouinfosys.Mapper515;
import cn.kinlon.emu.mappers.daouinfosys.Mapper517;
import cn.kinlon.emu.mappers.frontfareast.Mapper006;
import cn.kinlon.emu.mappers.frontfareast.Mapper008;
import cn.kinlon.emu.mappers.frontfareast.Mapper017;
import cn.kinlon.emu.mappers.fukutakeshoten.StudyBox;
import cn.kinlon.emu.mappers.henggedianzi.Henggedianzi177;
import cn.kinlon.emu.mappers.henggedianzi.Henggedianzi179;
import cn.kinlon.emu.mappers.homebrew.*;
import cn.kinlon.emu.mappers.irem.*;
import cn.kinlon.emu.mappers.jaleco.*;
import cn.kinlon.emu.mappers.jy.*;
import cn.kinlon.emu.mappers.kaiser.*;
import cn.kinlon.emu.mappers.konami.VRC1;
import cn.kinlon.emu.mappers.konami.VRC2And4;
import cn.kinlon.emu.mappers.konami.VRC3;
import cn.kinlon.emu.mappers.konami.vrc6.VRC6a;
import cn.kinlon.emu.mappers.konami.vrc6.VRC6b;
import cn.kinlon.emu.mappers.konami.vrc7.VRC7;
import cn.kinlon.emu.mappers.magicseries.Mapper107;
import cn.kinlon.emu.mappers.namco.*;
import cn.kinlon.emu.mappers.nanjing.Mapper534;
import cn.kinlon.emu.mappers.nanjing.Nanjing;
import cn.kinlon.emu.mappers.nihonbussan.CrazyClimber;
import cn.kinlon.emu.mappers.nintendo.*;
import cn.kinlon.emu.mappers.nintendo.mmc5.MMC5;
import cn.kinlon.emu.mappers.nintendo.vs.MainVsDualSystem;
import cn.kinlon.emu.mappers.nintendo.vs.VsGame;
import cn.kinlon.emu.mappers.nintendo.vs.VsUniSystem;
import cn.kinlon.emu.mappers.nitra.Mapper250;
import cn.kinlon.emu.mappers.ntdec.*;
import cn.kinlon.emu.mappers.pirate.*;
import cn.kinlon.emu.mappers.piratemmc3.*;
import cn.kinlon.emu.mappers.pyramid.PEC586;
import cn.kinlon.emu.mappers.racermate.RacerMate;
import cn.kinlon.emu.mappers.rare.AxROM;
import cn.kinlon.emu.mappers.rare.TQROM;
import cn.kinlon.emu.mappers.sachen.*;
import cn.kinlon.emu.mappers.subor.DANCE2000;
import cn.kinlon.emu.mappers.subor.Mapper166;
import cn.kinlon.emu.mappers.subor.Mapper167;
import cn.kinlon.emu.mappers.subor.SuborKaraoke;
import cn.kinlon.emu.mappers.sunsoft.*;
import cn.kinlon.emu.mappers.sunsoft.fme7.SunsoftFME7;
import cn.kinlon.emu.mappers.taito.TC0690;
import cn.kinlon.emu.mappers.taito.X1005;
import cn.kinlon.emu.mappers.taito.X1005b;
import cn.kinlon.emu.mappers.taito.X1017;
import cn.kinlon.emu.mappers.tengen.RAMBO1;
import cn.kinlon.emu.mappers.tengen.T800037;
import cn.kinlon.emu.mappers.thq.CPROM;
import cn.kinlon.emu.mappers.txc.*;
import cn.kinlon.emu.mappers.unif.bmc.*;
import cn.kinlon.emu.mappers.unif.btl.BTL900218;
import cn.kinlon.emu.mappers.unif.btl.MARIO1MALEE2;
import cn.kinlon.emu.mappers.unif.unl.*;
import cn.kinlon.emu.mappers.unif.unl.dripgame.DripGame;
import cn.kinlon.emu.mappers.waixing.*;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.preferences.GamePrefs;
import cn.kinlon.emu.serializer.Transients;
import cn.kinlon.emu.tv.TVSystem;

import static cn.kinlon.emu.utils.BitUtil.*;
import static cn.kinlon.emu.utils.StreamUtil.*;
import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.tv.TVSystem.*;

public abstract class Mapper implements Serializable, Transients {

    private static final long serialVersionUID = 0;

    protected static final int INSERT_COIN_FRAMES = 6;

    protected transient int[] memory = new int[0x10000];
    protected transient int[] vram = new int[0x4000];
    protected transient int[] xram;
    protected transient int[] xChrRam;

    protected final int[] nametableMappings = {0x2000, 0x2000, 0x2000, 0x2000};

    protected final int prgShift;
    protected final int prgAddressMask;
    protected final int chrShift;
    protected final int chrAddressMask;
    protected final int minRegisterAddress;
    protected final int minRomAddress;
    protected final int chrRamSizeMask;

    protected CPU cpu;
    protected PPU ppu;
    protected APU apu;
    protected Register reg;
    protected TVSystem preferredTVSystem;
    protected TVSystem tvSystem;
    protected boolean ntsc;
    protected int nametableMirroring;
    protected boolean vsSystem;
    protected boolean nametableMappingEnabled = true;
    protected boolean chrRamPresent;
    protected boolean nonVolatilePrgRamPresent;
    protected String romFileName;

    protected transient int[] prgROM;
    protected transient int[] chrROM;
    protected int[] prgBanks;
    protected int[] chrBanks;
    protected int prgRomSizeMask;
    protected int chrRomSizeMask;
    protected int prgRomLength;
    protected int chrRomLength;

    protected transient int[][] diskData;
    protected transient int[][] sourceDiskData;

    protected int vramMask = 0x3FFF;

    protected Ports ports = Ports.DEFAULTS;
    protected DeviceMapper[] deviceMappers = new DeviceMapper[0];
    protected int coinInserted;
    protected int coinMask;
    protected int serviceButtonPressed;
    protected int serviceButtonMask;
    protected int screamedIntoMicrophone;
    protected int dipSwitchesValue;

    protected volatile int buttons;

    public Mapper(final NsfFile nsfFile) {
        prgBanks = new int[16];
        prgShift = 12;
        prgAddressMask = 0x0FFF;
        minRegisterAddress = 0x8000;
        minRomAddress = 0x8000;
        chrBanks = new int[0];
        chrShift = chrAddressMask = 0;
        chrRamPresent = true;
        preferredTVSystem = nsfFile.getTvSystem();
        setBanks(prgBanks, 0, 0, 16, 0x1000);
        romFileName = nsfFile.getFileName();
        restore(nsfFile);
        prgRomSizeMask = prgROM.length - 1;
        chrRamSizeMask = 0x1FFF;
        initializeRAM();
    }

    public Mapper(final FdsFile fdsFile) {
        prgShift = 0;
        prgAddressMask = 0xFFFF;
        chrShift = 0;
        chrAddressMask = 0;
        minRegisterAddress = 0x8000;
        minRomAddress = 0x8000;
        chrRamPresent = true;
        preferredTVSystem = TVSystem.NTSC;
        romFileName = fdsFile.getFileName();
        chrRamSizeMask = 0x1FFF;
        restore(fdsFile);
        initializeRAM();
    }

    public Mapper(final CartFile cartFile, final int prgBanksSize,
                  final int chrBanksSize) {
        this(cartFile, prgBanksSize, chrBanksSize, 0x8000, 0x8000);
    }

    public Mapper(final CartFile cartFile, int prgBanksSize,
                  final int chrBanksSize, final int minRegisterAddress,
                  final int minRomAddress) {

        romFileName = cartFile.getFileName();
        restore(cartFile);
        prgRomLength = cartFile.getPrgRomLength();
        chrRomLength = cartFile.getChrRomLength();
        prgRomSizeMask = prgROM.length - 1;
        chrRomSizeMask = chrROM.length - 1;
        chrRamPresent = cartFile.isChrRamPresent();
        nonVolatilePrgRamPresent = cartFile.isNonVolatilePrgRamPresent();
        preferredTVSystem = cartFile.getTvSystem();
        setNametableMirroring(cartFile.getMirroring());

        vsSystem = cartFile.isVsSystem();

        boolean usesPrgBanks = true;
        if (prgBanksSize == 0) {
            usesPrgBanks = false;
            prgBanksSize = 2;
        }
        prgBanks = new int[prgBanksSize];
        if (isBase2(prgBanksSize)) {
            final int power = log2(prgBanksSize);
            prgShift = 16 - power;
            prgAddressMask = 0xFFFF >> power;
            if (usesPrgBanks) {
                setBanks(prgBanks, 0, 0, prgBanksSize, prgAddressMask + 1);
            }
        } else {
            prgShift = prgAddressMask = 0;
        }

        chrBanks = new int[chrBanksSize];
        if (isBase2(chrBanksSize)) {
            if (chrBanksSize > 0) {
                final int power = log2(chrBanksSize);
                chrShift = 13 - power;
                chrAddressMask = 0x1FFF >> power;
            } else {
                chrShift = chrAddressMask = 0;
            }
            setBanks(chrBanks, 0, 0, chrBanksSize, chrAddressMask + 1);
        } else {
            chrShift = chrAddressMask = 0;
        }

        this.minRegisterAddress = minRegisterAddress;
        this.minRomAddress = minRomAddress;
        initializeRAM();

        if (cartFile.isTrainerPresent()) {
            System.arraycopy(cartFile.getTrainer(), 0, memory, 0x7000,
                    cartFile.getTrainerSize());
        }

        final int cRamSize = getChrRamSize(cartFile);
        if (cRamSize > 0x2000) {
            chrRamPresent = true;
            xChrRam = new int[cRamSize];
            chrRamSizeMask = xChrRam.length - 1;
        } else {
            chrRamSizeMask = 0x1FFF;
        }
    }

    protected int getChrRamSize(final CartFile cartFile) {
        return cartFile.getChrRamSize();
    }

    private void initializeRAM() {
        switch (AppPrefs.getInstance().getUserInterfacePrefs()
                .getInitialRamState()) {
            case AllFF:
                for (int i = memory.length - 1; i >= 0; i--) {
                    memory[i] = 0xFF;
                }
                break;
            case Random: {
                final Random random = new Random();
                for (int i = memory.length - 1; i >= 0; i--) {
                    memory[i] = 0xFF & random.nextInt();
                }
                break;
            }
        }
    }

    // For Force Eject
    public void copyMemory(final Mapper mapper) {
        System.arraycopy(mapper.memory, 0, memory, 0, memory.length);
        System.arraycopy(mapper.vram, 0, vram, 0, vram.length);
    }

    public void restore(final NsfFile nsfFile) {
        if (nsfFile != null) {
            prgROM = nsfFile.getPrgROM();
        }
    }

    public void restore(final CartFile cartFile) {
        if (cartFile != null) {
            prgROM = cartFile.getPrgROM();
            chrROM = cartFile.getChrROM();
        }
    }

    public void restore(final FdsFile fdsFile) {
        if (fdsFile != null) {
            System.arraycopy(fdsFile.getBios(), 0, memory, 0xE000, 0x2000);
            sourceDiskData = fdsFile.getDiskData();
            if (diskData == null) {
                diskData = new int[sourceDiskData.length][0x10000];
                for (int i = sourceDiskData.length - 1; i >= 0; i--) {
                    System.arraycopy(sourceDiskData[i], 0, diskData[i], 0, 0x10000);
                }
            } else {
                for (int i = sourceDiskData.length - 1; i >= 0; i--) {
                    for (int j = 0xFFFF; j >= 0; j--) {
                        if (diskData[i][j] < 0) {
                            diskData[i][j] = sourceDiskData[i][j];
                        }
                    }
                }
            }
        }
    }

    public void init() {
        writeRegister(minRegisterAddress, 0);
    }

    public void resetting() {
    }

    public void setMachine(final Machine machine) {
        this.cpu = machine.getCPU();
        this.ppu = machine.getPPU();
        this.apu = machine.getAPU();
        this.reg = machine.getCPU().getRegister();
    }

    public void setCPU(final CPU cpu) {
        this.cpu = cpu;
    }

    public void setPPU(final PPU ppu) {
        this.ppu = ppu;
    }

    public void setAPU(final APU apu) {
        this.apu = apu;
    }

    public void setTVSystem(final TVSystem tvSystem) {
        this.tvSystem = tvSystem;
        this.ntsc = tvSystem == NTSC;
    }

    public TVSystem getTVSystem() {
        return tvSystem;
    }

    public TVSystem getPreferredTVSystem() {
        return preferredTVSystem;
    }

    public void setVramMask(final int vramMask) {
        this.vramMask = vramMask;
    }

    public void setNsfOptions(final boolean automaticallyAdvanceTrack,
                              final int idleSeconds, final boolean defaultTrackLength,
                              final int trackLengthMinutes) {
    }

    public void setSongPaused(final boolean songPaused) {
    }

    public void requestSong(final int songNumber) {
    }

    public void screamIntoMicrophone() {
        screamedIntoMicrophone = 600;
    }

    public void insertCoin(final int vsSystem, final int coinSlot) {
        coinInserted = INSERT_COIN_FRAMES;
        coinMask = 0x20 << (coinSlot & 1);
    }

    public void pressServiceButton(final int vsSystem) {
        serviceButtonPressed = INSERT_COIN_FRAMES;
        serviceButtonMask = 0x04;
    }

    public void setDipSwitchesValue(final int dipSwitchesValue) {
        this.dipSwitchesValue = dipSwitchesValue;
    }

    public void setNametable(final int index, final int value) {
        nametableMappings[index] = 0x2000 | (value << 10);
    }

    public void setNametables(final int v0, final int v1, final int v2,
                              final int v3) {
        setNametable(0, v0);
        setNametable(1, v1);
        setNametable(2, v2);
        setNametable(3, v3);
    }

    public void setNametableMirroring(final int nametableMirroring) {
        this.nametableMirroring = nametableMirroring;
        switch (nametableMirroring) {
            case VERTICAL:
                nametableMappings[0] = nametableMappings[2] = 0x2000;
                nametableMappings[1] = nametableMappings[3] = 0x2400;
                break;
            case HORIZONTAL:
                nametableMappings[0] = nametableMappings[1] = 0x2000;
                nametableMappings[2] = nametableMappings[3] = 0x2400;
                break;
            case ONE_SCREEN_A:
                nametableMappings[0] = nametableMappings[1] = nametableMappings[2]
                        = nametableMappings[3] = 0x2000;
                break;
            case ONE_SCREEN_B:
                nametableMappings[0] = nametableMappings[1] = nametableMappings[2]
                        = nametableMappings[3] = 0x2400;
                break;
            case FOUR_SCREEN:
                nametableMappings[0] = 0x2000;
                nametableMappings[1] = 0x2400;
                nametableMappings[2] = 0x2800;
                nametableMappings[3] = 0x2C00;
                break;
            case DIAGONAL:
                nametableMappings[0] = nametableMappings[3] = 0x2000;
                nametableMappings[1] = nametableMappings[2] = 0x2400;
                break;
            case L_SHAPED:
                nametableMappings[0] = 0x2000;
                nametableMappings[1] = nametableMappings[2] = nametableMappings[3]
                        = 0x2400;
                break;
            case R_SHAPED:
                nametableMappings[0] = nametableMappings[1] = nametableMappings[2]
                        = 0x2000;
                nametableMappings[3] = 0x2400;
                break;
        }
    }

    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= minRegisterAddress) {
            writeRegister(address, value);
        }
    }

    public int readMemory(final int address) {
        if (address >= minRomAddress) {
            return prgROM[(prgBanks[address >> prgShift] | (address & prgAddressMask))
                    & prgRomSizeMask];
        } else {
            return memory[address];
        }
    }

    public void writePrgRom(final int address, final int value) {
        if (address >= 0 && address < prgROM.length) {
            prgROM[address] = value;
        }
    }

    public void writeChrRom(int address, int value) {
        if (address >= 0 && address < chrROM.length) {
            chrROM[address] = value;
        }
    }

    public int getPrgBankCount() {
        return prgROM == null ? 0 : prgROM.length / getPrgBankSize();
    }

    public int getPrgBankSize() {
        return prgROM == null ? 0 : prgAddressMask + 1;
    }

    public int getChrBankCount() {
        return chrROM == null ? 0 : chrROM.length / getChrBankSize();
    }

    public int getChrBankSize() {
        return chrROM == null ? 0 : chrAddressMask + 1;
    }

    // TODO OVERRIDE IN SOME MAPPERS
    public int getPrgRomIndex(int address) {
        if (prgBanks != null && address >= minRomAddress) {
            return (prgBanks[address >> prgShift] | (address & prgAddressMask))
                    & prgRomSizeMask;
        } else {
            return -1;
        }
    }

    public int maskAddress(int address) {
        address &= 0xFFFF;
        if (address < 0x2000) {
            return address & 0x07FF;
        } else if (address < 0x4000) {
            return 0x2000 | (address & 7);
        } else {
            return address;
        }
    }

    public int readCpuMemory(final int address) {

        final int maskedAddress = maskAddress(address);
        int value;
        switch (maskedAddress) {
            case PPU.REG_PPU_CTRL:
            case PPU.REG_PPU_MASK:
            case PPU.REG_PPU_STATUS:
            case PPU.REG_OAM_ADDR:
            case PPU.REG_OAM_DATA:
            case PPU.REG_PPU_SCROLL:
            case PPU.REG_PPU_ADDR:
            case PPU.REG_PPU_DATA:
                value = ppu.readRegister(maskedAddress);
                break;
            case APU.REG_APU_STATUS:
                value = apu.readStatus();
                break;
            case CPU.REG_INPUT_PORT_1:
                value = readInputPort(0);
                break;
            case CPU.REG_INPUT_PORT_2:
                value = readInputPort(1);
                break;
            default:
                value = readMemory(maskedAddress);
                break;
        }

        return value;
    }

    public int peekWord(final int address) {
        return (peekCpuMemory(address + 1) << 8) | peekCpuMemory(address);
    }

    public int peekCpuMemory(final int address) {

        int maskedAddress = maskAddress(address);
        int value;
        switch (maskedAddress) {
            case PPU.REG_PPU_CTRL:
            case PPU.REG_PPU_MASK:
            case PPU.REG_PPU_STATUS:
            case PPU.REG_OAM_ADDR:
            case PPU.REG_OAM_DATA:
            case PPU.REG_PPU_SCROLL:
            case PPU.REG_PPU_ADDR:
            case PPU.REG_PPU_DATA:
                value = ppu.peekRegister(maskedAddress);
                break;
            case APU.REG_APU_STATUS:
                value = apu.peekStatus();
                break;
            case CPU.REG_INPUT_PORT_1:
                value = peekInputPort(0);
                break;
            case CPU.REG_INPUT_PORT_2:
                value = peekInputPort(1);
                break;
            default:
                value = readMemory(maskedAddress);
                break;
        }

        return value;
    }

    public void writeWord(final int address, final int value) {
        writeMemory(address, value & 0xFF);
        writeMemory(address + 1, (value >> 8) & 0xFF);
    }

    public void writeCpuMemory(int address, int value) {
        value &= 0xFF;
        address = maskAddress(address);
        switch (address) {
            case PPU.REG_PPU_CTRL:
            case PPU.REG_PPU_MASK:
            case PPU.REG_PPU_STATUS:
            case PPU.REG_OAM_ADDR:
            case PPU.REG_OAM_DATA:
            case PPU.REG_PPU_SCROLL:
            case PPU.REG_PPU_ADDR:
            case PPU.REG_PPU_DATA:
                ppu.writeRegister(address, value);
                break;
            case APU.REG_APU_PULSE1_ENVELOPE:
                apu.pulse1.writeEnvelope(value);
                break;
            case APU.REG_APU_PULSE1_SWEEP:
                apu.pulse1.writeSweep(value);
                break;
            case APU.REG_APU_PULSE1_TIMER_RELOAD_LOW:
                apu.pulse1.writeTimerReloadLow(value);
                break;
            case APU.REG_APU_PULSE1_TIMER_RELOAD_HIGH:
                apu.pulse1.writeTimerReloadHigh(value);
                break;
            case APU.REG_APU_PULSE2_ENVELOPE:
                apu.pulse2.writeEnvelope(value);
                break;
            case APU.REG_APU_PULSE2_SWEEP:
                apu.pulse2.writeSweep(value);
                break;
            case APU.REG_APU_PULSE2_TIMER_RELOAD_LOW:
                apu.pulse2.writeTimerReloadLow(value);
                break;
            case APU.REG_APU_PULSE2_TIMER_RELOAD_HIGH:
                apu.pulse2.writeTimerReloadHigh(value);
                break;
            case APU.REG_APU_TRIANGLE_LINEAR_COUNTER:
                apu.triangle.writeLinearCounter(value);
                break;
            case APU.REG_APU_TRIANGLE_TIMER_RELOAD_LOW:
                apu.triangle.writeTimerReloadLow(value);
                break;
            case APU.REG_APU_TRIANGLE_TIMER_RELOAD_HIGH:
                apu.triangle.writeTimerReloadHigh(value);
                break;
            case APU.REG_APU_NOISE_ENVELOPE:
                apu.noise.writeEnvelope(value);
                break;
            case APU.REG_APU_NOISE_MODE_AND_PERIOD:
                apu.noise.writeModeAndPeriod(value);
                break;
            case APU.REG_APU_NOISE_LENGTH_COUNTER:
                apu.noise.writeLengthCounter(value);
                break;
            case APU.REG_APU_DMC_FLAGS_AND_FREQUENCY:
                apu.dmc.writeFlagsAndFrequency(value);
                break;
            case APU.REG_APU_DMC_DIRECT_LOAD:
                apu.dmc.writeDirectLoad(value);
                break;
            case APU.REG_APU_DMC_SAMPLE_ADDRESS:
                apu.dmc.writeSampleAddress(value);
                break;
            case APU.REG_APU_DMC_SAMPLE_LENGTH:
                apu.dmc.writeSampleLength(value);
                break;
            case CPU.REG_OAM_DMA:
                cpu.oamTransfer(value);
                break;
            case APU.REG_APU_STATUS:
                apu.writeStatus(value);
                break;
            case CPU.REG_OUTPUT_PORT:
                writeOutputPort(value);
                break;
            case APU.REG_APU_FRAME_COUNTER:
                apu.writeFrameCounter(value);
                break;
            default:
                writeMemory(address, value);
                break;
        }
    }

    public int maskVRAMAddress(int address) {

        address &= vramMask;

        if (nametableMappingEnabled && address >= 0x2000 && address <= 0x3EFF) {
            address = nametableMappings[(address >> 10) & 3] | (address & 0x03FF);
        }

        return address;
    }

    public void handleFrameRendered() {
    }

    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {
    }

    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000 && chrRamPresent && chrAddressMask != 0) {
            (xChrRam != null ? xChrRam : vram)[(chrBanks[address >> chrShift]
                    | (address & chrAddressMask)) & chrRamSizeMask] = value;
        } else {
            vram[address] = value;
        }
    }

    public int readVRAM(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            if (chrRamPresent) {
                return (xChrRam != null ? xChrRam : vram)[(chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & chrRamSizeMask];
            } else {
                return chrROM[(chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & chrRomSizeMask];
            }
        } else {
            return vram[address];
        }
    }

    public int peekVRAM(final int address) {
        return readVRAM(address);
    }

    // TODO OVERRIDE IN SOME MAPPERS
    public int getChrRomIndex(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            if (chrRamPresent) {
                return (chrBanks[address >> chrShift] | (address & chrAddressMask))
                        & 0x1FFF;
            } else {
                return (chrBanks[address >> chrShift] | (address & chrAddressMask))
                        & chrRomSizeMask;
            }
        } else {
            return -1;
        }
    }

    public void update() {
    }

    public void updateButtons(final int buttons) {
        this.buttons = buttons;
        final DeviceMapper[] mappers = deviceMappers;
        for (int i = mappers.length - 1; i >= 0; i--) {
            mappers[i].update(buttons);
        }

        if (coinInserted > 0 && --coinInserted == 0) {
            coinMask = 0x00;
        }
        if (serviceButtonPressed > 0 && --serviceButtonPressed == 0) {
            serviceButtonMask = 0x00;
        }
    }

    public Ports getPorts() {
        return ports;
    }

    public void setPorts(final Ports ports) {
        this.ports = ports;
    }

    public boolean hasDeviceMapper(final int inputDevice) {
        return getDeviceMapper(inputDevice) != null;
    }

    public DeviceMapper getDeviceMapper(final int inputDevice) {
        final DeviceMapper[] mappers = deviceMappers;
        if (mappers == null || mappers.length == 0) {
            return null;
        }
        for (int i = mappers.length - 1; i >= 0; i--) {
            if (mappers[i].getInputDevice() == inputDevice) {
                return mappers[i];
            }
        }
        return null;
    }

    public DeviceMapper[] getDeviceMappers() {
        return deviceMappers;
    }

    public void setDeviceMappers(final DeviceMapper[] deviceMappers) {
        this.deviceMappers = deviceMappers;
    }

    public int getAudioMixerScale() {
        return 0xFFFF;
    }

    public float getAudioSample() {
        return 0f;
    }

    public boolean isNsfMapper() {
        return false;
    }

    public boolean isFdsMapper() {
        return false;
    }

    public boolean isDiskActivity() {
        return false;
    }

    public int getDiskSideCount() {
        return 0;
    }

    public int getDiskSide() {
        return 0;
    }

    public void setDiskSide(int side) {
    }

    public void ejectDisk() {
    }

    public boolean isVsDualSystem() {
        return false;
    }

    protected void setBanks(final int[] banks, final int index, final int value,
                            final int length, final int bankSize) {
        for (int i = 0, offset = value; i < length; i++, offset += bankSize) {
            banks[index + i] = offset;
        }
    }

    protected void setPrgBanks(final int firstBank, final int numberOfBanks,
                               final int firstValue) {
        for (int i = numberOfBanks - 1; i >= 0; i--) {
            setPrgBank(firstBank + i, firstValue + i);
        }
    }

    protected void set2PrgBanks(final int firstBank, final int firstValue) {
        setPrgBank(firstBank, firstValue);
        setPrgBank(firstBank + 1, firstValue + 1);
    }

    protected void set4PrgBanks(final int firstBank, final int firstValue) {
        setPrgBank(firstBank, firstValue);
        setPrgBank(firstBank + 1, firstValue + 1);
        setPrgBank(firstBank + 2, firstValue + 2);
        setPrgBank(firstBank + 3, firstValue + 3);
    }

    protected void setChrBanks(final int firstBank, final int numberOfBanks,
                               final int firstValue) {
        for (int i = numberOfBanks - 1; i >= 0; i--) {
            setChrBank(firstBank + i, firstValue + i);
        }
    }

    protected void set2ChrBanks(final int firstBank, final int firstValue) {
        setChrBank(firstBank, firstValue);
        setChrBank(firstBank + 1, firstValue + 1);
    }

    protected void set4ChrBanks(final int firstBank, final int firstValue) {
        setChrBank(firstBank, firstValue);
        setChrBank(firstBank + 1, firstValue + 1);
        setChrBank(firstBank + 2, firstValue + 2);
        setChrBank(firstBank + 3, firstValue + 3);
    }

    protected void set8ChrBanks(final int firstBank, final int firstValue) {
        setChrBank(firstBank, firstValue);
        setChrBank(firstBank + 1, firstValue + 1);
        setChrBank(firstBank + 2, firstValue + 2);
        setChrBank(firstBank + 3, firstValue + 3);
        setChrBank(firstBank + 4, firstValue + 4);
        setChrBank(firstBank + 5, firstValue + 5);
        setChrBank(firstBank + 6, firstValue + 6);
        setChrBank(firstBank + 7, firstValue + 7);
    }

    protected void setPrgBank(final int value) {
        setPrgBank(1, value);
    }

    protected void setPrgBank(final int bank, final int value) {
        if (prgROM == null) {
            prgBanks[bank] = value;
        } else if (value < 0) {
            prgBanks[bank] = prgRomLength + (value << prgShift);
        } else {
            prgBanks[bank] = value << prgShift;
        }
    }

    protected void setChrBank(final int value) {
        setChrBank(0, value);
    }

    protected void setChrBank(final int bank, final int value) {
        if (chrROM == null) {
            chrBanks[bank] = value;
        } else if (value < 0) {
            chrBanks[bank] = chrRomLength + (value << chrShift);
        } else {
            chrBanks[bank] = value << chrShift;
        }
    }

    protected void writeRegister(final int address, final int value) {
    }

    public void writeTransients(final DataOutput out) throws IOException {
        writeSparseByteArray(out, memory);
        writeSparseByteArray(out, vram);
        writeSparseByteArray(out, xram);
        writeSparseByteArray(out, xChrRam);

        if (diskData != null) {
            out.write(diskData.length);
            for (int i = 0; i < diskData.length; i++) {
                for (int j = 0; j < 64; j++) {
                    writeSparseBlock(out, diskData[i], sourceDiskData[i], j << 10);
                }
            }
        } else {
            out.write(0);
        }
    }

    public void readTransients(final DataInput in) throws IOException {
        memory = readSparseByteArray(in);
        vram = readSparseByteArray(in);
        xram = readSparseByteArray(in);
        xChrRam = readSparseByteArray(in);

        final int diskDataLength = in.readUnsignedByte();
        if (diskDataLength > 0) {
            diskData = new int[diskDataLength][0x10000];
            for (int i = 0; i < diskDataLength; i++) {
                for (int j = 0; j < 64; j++) {
                    readSparseBlock(in, diskData[i], j << 10, -1);
                }
            }
        }
    }

    public void writeOutputPort(final int value) {
        final DeviceMapper[] mappers = deviceMappers;
        for (int i = mappers.length - 1; i >= 0; i--) {
            mappers[i].writePort(value);
        }
    }

    public int readInputPort(final int portIndex) {
        final DeviceMapper[] mappers = deviceMappers;
        int value;
        if (vsSystem) {
            if (portIndex == 0) {
                value = coinMask | ((dipSwitchesValue & 0x03) << 3) | serviceButtonMask;
            } else {
                value = dipSwitchesValue & 0xFC;
            }
        } else {
            value = 0x40;
            if (portIndex == 0 && screamedIntoMicrophone > 0) {
                --screamedIntoMicrophone;
                value |= ((screamedIntoMicrophone & 1) << 2);
            }
        }
        for (int i = mappers.length - 1; i >= 0; i--) {
            value |= mappers[i].readPort(portIndex);
        }
        return value;
    }

    public int peekInputPort(final int portIndex) {
        final DeviceMapper[] mappers = deviceMappers;
        int value;
        if (vsSystem) {
            if (portIndex == 0) {
                value = coinMask | ((dipSwitchesValue & 0x03) << 3) | serviceButtonMask;
            } else {
                value = dipSwitchesValue & 0xFC;
            }
        } else {
            value = 0x40;
        }
        for (int i = mappers.length - 1; i >= 0; i--) {
            value |= mappers[i].peekPort(portIndex);
        }
        return value;
    }

    public boolean isNonVolatilePrgRamPresent() {
        return nonVolatilePrgRamPresent;
    }

    public void loadNonVolatilePrgRam() {
        if (nonVolatilePrgRamPresent) {
            synchronized (GamePrefs.class) {
                final GamePrefs prefs = GamePrefs.getInstance();
                final int[] sram = prefs.getNonVolatilePrgRam();
                if (sram != null && sram.length == 0x2000) {
                    System.arraycopy(sram, 0x0000, memory, 0x6000, 0x2000);
                }

                if (xram != null) {
                    final int[] _xram = prefs.getNonVolatileXRam();
                    if (_xram != null && _xram.length == xram.length) {
                        System.arraycopy(_xram, 0, xram, 0, xram.length);
                    }
                }
            }
        }
    }

    protected void saveNonVolatilePrgRam() {
        if (nonVolatilePrgRamPresent) {
            synchronized (GamePrefs.class) {
                final GamePrefs prefs = GamePrefs.getInstance();
                int[] sram = prefs.getNonVolatilePrgRam();
                if (sram == null || sram.length != 0x2000) {
                    sram = new int[0x2000];
                }
                System.arraycopy(memory, 0x6000, sram, 0x0000, 0x2000);
                prefs.setNonVolatilePrgRam(sram);

                if (xram != null) {
                    int[] _xram = prefs.getNonVolatileXRam();
                    if (_xram == null || _xram.length != xram.length) {
                        _xram = new int[xram.length];
                    }
                    System.arraycopy(xram, 0, _xram, 0, xram.length);
                    prefs.setNonVolatileXRam(_xram);
                }
            }
            GamePrefs.save();
        }
    }

    public void close(final boolean saveNonVolatileData) {
        if (saveNonVolatileData) {
            saveNonVolatilePrgRam();
        }
        final DeviceMapper[] mappers = deviceMappers;
        for (int i = mappers.length - 1; i >= 0; i--) {
            mappers[i].close(saveNonVolatileData);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        readTransients(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeTransients(out);
    }

    public static Mapper create(final UnifFile unifFile) {
        final String board = unifFile.getBoard();
        if (board == null) {
            return null;
        }
        switch (board.trim().toUpperCase(Locale.ENGLISH)) {
            case "10-24-C-A1":
                return new BMC1024CA1(unifFile);
            case "11160":
                return new BMC11160(unifFile);
            case "8-IN-1":
            case "NEWSTAR-GRM070-8IN1":
                return new BMC8In1(unifFile);
            case "12-IN-1":
                return new BMC12In1(unifFile);
            case "13IN1JY110":
                return new Mapper295(unifFile);
            case "158B":
                return new UNL158B(unifFile);
            case "190IN1":
                return new BMC190In1(unifFile);
            case "22211":
                return new UNL22211(unifFile);
            case "3D-BLOCK":
                return new _3D_Block(unifFile);
            case "411120-C":
            case "K-3088":
                return new BMC411120C(unifFile);
            case "42IN1RESETSWITCH":
                return new Mapper233(unifFile);
            case "43272":
                return new UNL43272(unifFile);
            case "603-5052":
                return new UNL603_5052(unifFile);
            case "64IN1NOREPEAT":
                return new BMC64In1NoRepeat(unifFile);
            case "70IN1":
                return new BMC70In1(unifFile);
            case "70IN1B":
                return new BMC70In1B(unifFile);
            case "80013-B":
                return new BMC8013B(unifFile);
            case "810544-C-A1":
                return new BMC810544CA1(unifFile);
            case "8157":
                return new BMC8157(unifFile);
            case "8237":
                return new UNL8237(unifFile);
            case "8237A":
                return new UNL8237A(unifFile);
            case "830118C":
                return new BMC830118C(unifFile);
            case "830134C":
                return new BMC830134C(unifFile);
            case "830425C-4391T":
                return new BMC830425C4391T(unifFile);
            case "831128C":
                return new UNL831128C(unifFile);
            case "891227":
                return new BMC891227(unifFile);
            case "900218":
                return new BTL900218(unifFile);
            case "A65AS":
                return new A65AS(unifFile);
            case "AC08":
                return new AC08(unifFile);
            case "ANROM":
                return new AxROM(unifFile);
            case "AX-40G":
                return new AX40G(unifFile);
            case "AX5705":
                return new AX5705(unifFile);
            case "BB":
                return new BB(unifFile);
            case "BJ-56":
                return new BJ56(unifFile);
            case "BS-5":
                return new BS5(unifFile);
            case "CC-21":
                return new CC21(unifFile);
            case "CHINA_ER_SAN2":
                return new NamcoX(unifFile);
            case "CITYFIGHT":
                return new CityFighter(unifFile);
            case "COOLGIRL":
                return new COOLGIRL(unifFile);
            case "COOLBOY":
                return new COOLBOY(unifFile);
            case "CPROM":
                return new CPROM(unifFile);
            case "CNROM":
                return new CNROM(unifFile);
            case "CTC-09":
                return new BMCCTC09(unifFile);
            case "CTC-12IN1":
                return new BMCCTC12IN1(unifFile);
            case "D1038":
                return new D1038(unifFile);
            case "DANCE":
            case "ONEBUS":
                return new Mapper256(unifFile);
            case "DANCE2000":
                return new DANCE2000(unifFile);
            case "DRAGONFIGHTER":
                return new DRAGONFIGHTER(unifFile);
            case "DREAMTECH01":
                return new DREAMTECH01(unifFile);
            case "DRIPGAME":
                return new DripGame(unifFile);
            case "EDU2000":
                return new EDU2000(unifFile);
            case "EH8813A":
                return new EH8813A(unifFile);
            case "EKROM":
            case "ELROM":
            case "ETROM":
            case "EWROM":
                return new MMC5(unifFile);
            case "F-15":
                return new BMCF15(unifFile);
            case "FARID_SLROM_8-IN-1":
                return new FARID_SLROM_8IN1(unifFile);
            case "FARID_UNROM_8-IN-1":
                return new FARID_UNROM_8IN1(unifFile);
            case "FK23C":
                return new FK23C(unifFile, false);
            case "FK23CA":
                return new FK23C(unifFile, true);
            case "FS304":
                return new FS304(unifFile);
            case "G-146":
                return new G146(unifFile);
            case "GHOSTBUSTERS63IN1":
                return new Ghostbusters63In1(unifFile);
            case "GN-26":
                return new BMCGN26(unifFile);
            case "GN-45":
                return new GN45(unifFile);
            case "GS-2004":
                return new GS2004(unifFile);
            case "GS-2013":
                return new GS2013(unifFile);
            case "GNROM":
            case "MHROM":
                return new GxROM(unifFile);
            case "H2288":
                return new H2288(unifFile);
            case "HP898F":
                return new HP898F(unifFile);
            case "HPxx":
                return new BMCHPxx(unifFile);
            case "K-3006":
                return new BMCK3006(unifFile);
            case "K-3033":
                return new BMCK3033(unifFile);
            case "K-3036":
                return new BMCK3036(unifFile);
            case "K-3046":
                return new BMCK3046(unifFile);
            case "KOF97":
                return new KOF97(unifFile);
            case "KS106C":
                return new Kaiser106C(unifFile);
            case "KS7012":
                return new Kaiser7012(unifFile);
            case "KS7013B":
                return new Kaiser7013B(unifFile);
            case "KS7016":
                return new Kaiser7016(unifFile);
            case "KS7017":
                return new Kaiser7017(unifFile);
            case "KS7021A":
                return new Kaiser7021A(unifFile);
            case "KS7030":
                return new Kaiser7030(unifFile);
            case "KS7031":
                return new Kaiser7031(unifFile);
            case "KS7032":
                return new Kaiser7032(unifFile);
            case "KS7037":
                return new Kaiser7037(unifFile);
            case "KS7057":
                return new Kaiser7057(unifFile);
            case "L6IN1":
                return new BMCL6IN1(unifFile);
            case "LH10":
                return new LH10(unifFile);
            case "LH32":
                return new LH32(unifFile);
            case "LH51":
                return new LH51(unifFile);
            case "MALISB":
                return new MALISB(unifFile);
            case "MARIO1-MALEE2":
                return new MARIO1MALEE2(unifFile);
            case "N625092":
                return new N625092(unifFile);
            case "NOVELDIAMOND9999999IN1":
                return new NovelDiamond9999999In1(unifFile);
            case "NTBROM":
                return new Sunsoft4(unifFile);
            case "NTD-03":
                return new NTD03(unifFile);
            case "RROM":
            case "RROM-128":
            case "NROM":
            case "NROM-128":
            case "NROM-256":
                return new NROM(unifFile);
            case "RESET-TXROM":
                return new ResetTxROM(unifFile);
            case "RT-01":
                return new RT01(unifFile);
            case "PEC-586":
                return new PEC586(unifFile);
            case "SA-0036":
                return new SA0036(unifFile);
            case "SA-0037":
            case "SA-004":
                return new SA0037(unifFile);
            case "SA005-A":
                return new BMCSA005A(unifFile);
            case "SA-014":
            case "SA-NROM":
            case "TC-A001-72P":
                return new SANROM(unifFile);
            case "SA-010-1":
            case "SA-016-1M":
            case "TC-3015-72P-VX":
                return new NINA06(unifFile);
            case "HKROM":
            case "TBROM":
            case "TEROM":
            case "TFROM":
            case "TGROM":
            case "TKROM":
            case "TK1ROM":
            case "TKSROM":
            case "TLROM":
            case "TL1ROM":
            case "TL2ROM":
            case "TLSROM":
            case "TNROM":
            case "TQROM":
            case "TR1ROM":
            case "TSROM":
            case "TVROM":
                return new MMC3(unifFile);
            case "SA-72007":
                return new SA72007(unifFile);
            case "SA-72008":
                return new SA72008(unifFile);
            case "SA-72008-VX":
            case "SACHEN-74LS374N":
                return new Sachen74LS374Nb(unifFile);
            case "SA-9602B":
                return new SA9602B(unifFile);
            case "SACHEN-8259A":
                return new Sachen8259A(unifFile);
            case "SACHEN-8259B":
                return new Sachen8259B(unifFile);
            case "SACHEN-8259C":
                return new Sachen8259C(unifFile);
            case "SACHEN-8259D":
                return new Sachen8259D(unifFile);
            case "SB-5013":
                return new BMCSB5013(unifFile);
            case "SHERO":
                return new SHERO(unifFile);
            case "SL1632":
                return new Mapper014(unifFile);
            case "SAROM":
            case "SBROM":
            case "SCROM":
            case "SC1ROM":
            case "SEROM":
            case "SFROM":
            case "SF1ROM":
            case "SFEXPROM":
            case "SGROM":
            case "SHROM":
            case "SH1ROM":
            case "SIROM":
            case "SJROM":
            case "SKROM":
            case "SLROM":
            case "SL1ROM":
            case "SL2ROM":
            case "SL3ROM":
            case "SLRROM":
            case "SMROM":
            case "SNROM":
            case "SOROM":
            case "SUROM":
            case "SXROM":
                return new MMC1(unifFile);
            case "SMB2J":
                return new SMB2J(unifFile);
            case "SUPER24IN1SC03":
                return new Super24In1SC03(unifFile);
            case "SUPERHIK8IN1":
                return new Mapper045(unifFile);
            case "SUPERVISION16IN1":
                return new Supervision16In1(unifFile);
            case "T-230":
                return new T230(unifFile);
            case "T-262":
                return new T262(unifFile);
            case "TC-U01-1.5M":
                return new Mapper147(unifFile);
            case "TEK90":
                return new JY(unifFile, 90);
            case "TF1201":
                return new TF1201(unifFile);
            case "TH2131-1":
                return new UNLTH2131_1(unifFile);
            case "TJ-03":
                return new BMCTJ03(unifFile);
            case "TRANSFORMER":
                return new Transformer(unifFile);
            case "UN1ROM":
                return new UN1ROM(unifFile);
            case "UNROM":
            case "UOROM":
                return new UxROM(unifFile);
            case "UNROM-512-8":
            case "UNROM-512-16":
            case "UNROM-512-32":
                return new UNROM512(unifFile);
            case "VRC7":
                return new VRC7(unifFile);
            case "WAIXING-FW01":
                return new Mapper227(unifFile);
            case "WS":
                return new WS(unifFile);
            case "YOKO":
                return new YOKO(unifFile);
            default:
                return null;
        }
    }

    public static Mapper create(final NesFile nesFile) {

        switch (nesFile.getMapperNumber()) {
            case 0:
                return new NROM(nesFile);
            case 1:
                return new MMC1(nesFile);
            case 2:
                if (nesFile.getFileCRC() == 0xF956FCEA) {
                    return new DREAMTECH01(nesFile);
                } else {
                    return new UxROM(nesFile);
                }
            case 3:
                return new CNROM(nesFile);
            case 4:
                return new MMC3(nesFile);
            case 5:
                return new MMC5(nesFile);
            case 6:
                return new Mapper006(nesFile);
            case 7:
                return new AxROM(nesFile);
            case 8:
                return new Mapper008(nesFile);
            case 9:
                return new MMC2(nesFile);
            case 10:
                return new MMC4(nesFile);
            case 11:
                return new ColorDreams(nesFile);
            case 12:
                return new DragonBallZ5(nesFile);
            case 13:
                return new CPROM(nesFile);
            case 14:
                return new Mapper014(nesFile);
            case 15:
                return new Mapper015(nesFile);
            case 16:
                return new Mapper016(nesFile);
            case 17:
                return new Mapper017(nesFile);
            case 18:
                return new SS88006(nesFile);
            case 19:
                return new NamcoX(nesFile);
            case 20:
                return null; // not supported
            case 21:
                return new VRC2And4(nesFile);
            case 22:
                return new VRC2And4(nesFile);
            case 23:
                return new VRC2And4(nesFile);
            case 24:
                return new VRC6a(nesFile);
            case 25:
                return new VRC2And4(nesFile);
            case 26:
                return new VRC6b(nesFile);
            case 27:
                return new VRC2And4(nesFile);
            case 28:
                return new Streemerz(nesFile);
            case 29:
                return new Glider(nesFile);
            case 30:
                return new UNROM512(nesFile);
            case 31:
                return new Puritans(nesFile);
            case 32:
                return new G101(nesFile);
            case 33:
                return new TC0690(nesFile);
            case 34:
                switch (nesFile.getSubmapperNumber()) {
                    case 1:
                        return new NINA001(nesFile);
                    case 2:
                        return new BxROM(nesFile);
                    default:
                        return (nesFile.getChrRomLength() <= 0x2000)
                                ? new BxROM(nesFile) : new NINA001(nesFile);
                }
            case 35:
                return new Mapper035(nesFile);
            case 36:
                return new Txc22000(nesFile); // TODO WIP
            case 37:
                return new Mapper037(nesFile);
            case 38:
                return new CrimeBusters(nesFile);
            case 39:
                return new BxROM(nesFile);
            case 40:
                return new Mapper040(nesFile);
            case 41:
                return new Mapper041(nesFile);
            case 42:
                return new Mapper042(nesFile);
            case 43:
                return new MrMary2(nesFile);
            case 44:
                return new Mapper044(nesFile);
            case 45:
                return new Mapper045(nesFile);
            case 46:
                return new RumbleStation(nesFile);
            case 47:
                return new Mapper047(nesFile);
            case 48:
                return new TC0690(nesFile);
            case 49:
                return new Mapper049(nesFile);
            case 50:
                return new Mapper050(nesFile);
            case 51:
                return new BMC051(nesFile);
            case 52:
                return new Mapper052(nesFile);
            case 53:
                return new Supervision(nesFile);
            case 54:
                return new NovelDiamond9999999In1(nesFile);
            case 55:
                return null; // not supported
            case 56:
                return new Kaiser7032(nesFile);
            case 57:
                return new Mapper057(nesFile);
            case 58:
                return new Mapper058(nesFile);
            case 59:
                return null; // not supported
            case 60:
                return new Mapper060(nesFile);
            case 61:
                return new Mapper061(nesFile);
            case 62:
                return new Mapper062(nesFile);
            case 63:
                return new BMC063(nesFile);
            case 64:
                return new RAMBO1(nesFile);
            case 65:
                return new H3001(nesFile);
            case 66:
                return new GxROM(nesFile);
            case 67:
                return new Sunsoft3(nesFile);
            case 68:
                return new Sunsoft4(nesFile);
            case 69:
                return new SunsoftFME7(nesFile);
            case 70:
                return new Bandai74161_7432(nesFile, false);
            case 71:
                return new BF909x(nesFile);
            case 72:
                return new JF17(nesFile);
            case 73:
                return new VRC3(nesFile);
            case 74:
                return new Mapper074(nesFile);
            case 75:
                return new VRC1(nesFile);
            case 76:
                return new Mapper076(nesFile);
            case 77:
                return new Mapper077(nesFile);
            case 78:
                return new Mapper078(nesFile);
            case 79:
                return new NINA003006(nesFile);
            case 80:
                return new X1005(nesFile);
            case 81:
                return new Mapper081(nesFile);
            case 82:
                return new X1017(nesFile);
            case 83:
                return new Mapper083(nesFile);
            case 84:
                return null; // not supported
            case 85:
                return new VRC7(nesFile);
            case 86:
                return new Moero(nesFile);
            case 87:
                return new Mapper087(nesFile);
            case 88:
                return new Mapper088(nesFile);
            case 89:
                return new Sunsoft2(nesFile);
            case 90:
                return new JY(nesFile, 90);
            case 91:
                return new Mapper091(nesFile);
            case 92:
                return new Mapper092(nesFile);
            case 93:
                return new Sunsoft2IC(nesFile);
            case 94:
                return new UN1ROM(nesFile);
            case 95:
                return new NAMCOT3425(nesFile);
            case 96:
                return new OekaKidsTablet(nesFile);
            case 97:
                return new TAMS1(nesFile);
            case 98:
                return null; // not supported
            case 99: {
                final VsGame vsGame = nesFile.getVsGame();
                if (vsGame != null && vsGame.isDualSystemGame()) {
                    return new MainVsDualSystem(nesFile);
                } else {
                    return new VsUniSystem(nesFile);
                }
            }
            case 100:
                return null; // not supported
            case 101:
                return new JF10(nesFile);
            case 102:
                return null; // not supported
            case 103:
                return new Mapper103(nesFile);
            case 104:
                return new GoldenFive(nesFile);
            case 105:
                return new NesEvent(nesFile);
            case 106:
                return new Mapper106(nesFile);
            case 107:
                return new Mapper107(nesFile);
            case 108:
                return new Mapper108(nesFile);
            case 109:
                return null; // not supported
            case 110:
                return null; // not supported
            case 111:
                return null; // not supported
            case 112:
                return new Mapper112(nesFile);
            case 113:
                return new NINA06(nesFile);
            case 114:
                return new Mapper114(nesFile);
            case 115:
                return new Carson(nesFile);
            case 116:
                return new Mapper116(nesFile);
            case 117:
                return new Mapper117(nesFile);
            case 118:
                return new TxSROM(nesFile);
            case 119:
                return new TQROM(nesFile);
            case 120:
                return new Mapper120(nesFile);
            case 121:
                return new Mapper121(nesFile);
            case 122:
                return null; // not supported
            case 123:
                return new Mapper123(nesFile);
            case 124:
                return null; // not supported
            case 125:
                return new LH32(nesFile);
            case 126:
                return new PowerJoy(nesFile);
            case 127:
                return null; // not supported
            case 128:
                return null; // not supported
            case 129:
                return null; // not supported
            case 130:
                return null; // not supported
            case 131:
                return null; // not supported
            case 132:
                return new Txc22211A(nesFile);
            case 133:
                return new SA72008(nesFile);
            case 134:
                return new Mapper134(nesFile);
            case 135:
                return null; // not supported
            case 136:
                return new Sachen136(nesFile);
            case 137:
                return new Sachen8259D(nesFile);
            case 138:
                return new Sachen8259B(nesFile);
            case 139:
                return new Sachen8259C(nesFile);
            case 140:
                return new JF11(nesFile);
            case 141:
                return new Sachen8259A(nesFile);
            case 142:
                return new Kaiser7032(nesFile);
            case 143:
                return new SANROM(nesFile);
            case 144:
                return new DeathRace(nesFile);
            case 145:
                return new SA72007(nesFile);
            case 146:
                return new NINA06(nesFile);
            case 147:
                return new Mapper147(nesFile);
            case 148:
                return new SA0037(nesFile);
            case 149:
                return new SA0036(nesFile);
            case 150:
                return new Sachen74LS374Nb(nesFile);
            case 151:
                return new VRC1(nesFile);
            case 152:
                return new Bandai74161_7432(nesFile, true);
            case 153:
                return new Mapper153(nesFile);
            case 154:
                return new NAMCOT3453(nesFile);
            case 155:
                return new MMC1A(nesFile);
            case 156:
                return new DaouInfosys(nesFile);
            case 157:
                return new Mapper157(nesFile);
            case 158:
                return new T800037(nesFile);
            case 159:
                return new Mapper159(nesFile);
            case 160:
                return null; // not supported
            case 161:
                return null; // not supported
            case 162:
                return new Mapper162(nesFile);
            case 163:
                return new Nanjing(nesFile);
            case 164:
                return new Mapper164(nesFile);
            case 165:
                return new Mapper165(nesFile);
            case 166:
                return new Mapper166(nesFile);
            case 167:
                return new Mapper167(nesFile);
            case 168:
                return new RacerMate(nesFile);
            case 169:
                return null; // not supported
            case 170:
                return new Mapper170(nesFile);
            case 171:
                return new Kaiser7058(nesFile);
            case 172:
                return new Txc22211B(nesFile);
            case 173:
                return new Txc22211C(nesFile);
            case 174:
                return new Mapper174(nesFile);
            case 175:
                return new Kaiser7022(nesFile);
            case 176:
                return new Mapper176(nesFile);
            case 177:
                return new Henggedianzi177(nesFile);
            case 178:
                return new Education(nesFile);
            case 179:
                return new Henggedianzi179(nesFile);
            case 180:
                return new CrazyClimber(nesFile);
            case 181:
                return null; // not supported
            case 182:
                return new Mapper182(nesFile);
            case 183:
                return new ShuiGuanPipe(nesFile);
            case 184:
                return new Sunsoft1(nesFile);
            case 185:
                return new Mapper185(nesFile);
            case 186:
                return new StudyBox(nesFile);
            case 187:
                return new Mapper187(nesFile);
            case 188:
                return new KaraokeStudio(nesFile);
            case 189:
                return new Mapper189(nesFile);
            case 190:
                return new MagicKidGooGoo(nesFile);
            case 191:
                return new Mapper191(nesFile);
            case 192:
                return new Mapper192(nesFile);
            case 193:
                return new TC112(nesFile);
            case 194:
                return new Mapper194(nesFile);
            case 195:
                return new Mapper195(nesFile);
            case 196:
                return new Mapper196(nesFile);
            case 197:
                return new Mapper197(nesFile);
            case 198:
                return new Mapper198(nesFile);
            case 199:
                return new Mapper199(nesFile);
            case 200:
                return new Mapper200(nesFile);
            case 201:
                return new Mapper201(nesFile);
            case 202:
                return new Mapper202(nesFile);
            case 203:
                return new Mapper203(nesFile);
            case 204:
                return new Mapper204(nesFile);
            case 205:
                return new Mapper205(nesFile);
            case 206:
                return new DxROM(nesFile);
            case 207:
                return new X1005b(nesFile);
            case 208:
                return new Mapper208(nesFile);
            case 209:
                return new JY(nesFile, 209);
            case 210:
                return new NamcoX(nesFile);
            case 211:
                return new JY(nesFile, 211);
            case 212:
                return new Mapper212(nesFile);
            case 213:
                return new Mapper213(nesFile);
            case 214:
                return new Mapper214(nesFile);
            case 215:
                if (nesFile.getPrgRomLength() == 0x40000
                        && nesFile.getChrRomLength() == 0x80000) {
                    return new UNL8237(nesFile);
                } else {
                    return new Boogerman(nesFile);
                }
            case 216:
                return new Mapper216(nesFile);
            case 217:
                return new Mapper217(nesFile);
            case 218:
                return new MagicFloor(nesFile);
            case 219:
                return new Mapper219(nesFile);
            case 220:
                return new Gyruss(nesFile);
            case 221:
                return new Mapper221(nesFile);
            case 222:
                return new Mapper222(nesFile);
            case 223:
                return new Mapper074(nesFile);
            case 224:
                return null; // not supported
            case 225:
                return new Mapper225(nesFile);
            case 226:
                return new Mapper226(nesFile);
            case 227:
                return new Mapper227(nesFile);
            case 228:
                return new Mapper228(nesFile);
            case 229:
                return new Mapper229(nesFile);
            case 230:
                return new Mapper230(nesFile);
            case 231:
                return new Mapper231(nesFile);
            case 232:
                return new Mapper232(nesFile);
            case 233:
                return new Mapper233(nesFile);
            case 234:
                return new Maxi15(nesFile);
            case 235:
                return new BMC235(nesFile);
            case 236:
                return new BMC70In1(nesFile);
            case 237:
                return new Teletubbies(nesFile);
            case 238:
                return new Mapper238(nesFile);
            case 239:
                return null; // not supported
            case 240:
                return new Mapper240(nesFile);
            case 241:
                return new Mapper241(nesFile);
            case 242:
                return new Mapper242(nesFile);
            case 243:
                return new Sachen74LS374N(nesFile);
            case 244:
                return new Mapper244(nesFile);
            case 245:
                return new Mapper245(nesFile);
            case 246:
                return new Mapper246(nesFile);
            case 247:
                return null; // not supported
            case 248:
                return new Carson(nesFile);
            case 249:
                return new Mapper249(nesFile);
            case 250:
                return new Mapper250(nesFile);
            case 251:
                return null; // not supported
            case 252:
                return new Mapper252(nesFile);
            case 253:
                return new Mapper253(nesFile);
            case 254:
                return new Mapper254(nesFile);
            case 255:
                return new BMC255(nesFile);

            case 256:
                return new Mapper256(nesFile); // TODO WIP
            case 257:
                return new PEC586(nesFile);
            case 258:
                return new UNL158B(nesFile);
            case 259:
                return new BMCF15(nesFile);
            case 260:
                return new BMCHPxx(nesFile);
            case 261:
                return new BMC810544CA1(nesFile);
            case 262:
                return new SHERO(nesFile);
            case 263:
                return new KOF97(nesFile);
            case 264:
                return new YOKO(nesFile);
            case 265:
                return new T262(nesFile);
            case 266:
                return new CityFighter(nesFile);
            case 267:
                return new Mapper267(nesFile);
            case 268:
                return new COOLBOY(nesFile);
            case 269:
                return new Mapper269(nesFile);
            case 270:
                return null; // not supported
            case 271:
                return new Txc22026(nesFile);
            case 272:
                return new Mapper272(nesFile);
            case 273:
                return new Mapper273(nesFile);
            case 274:
                return new BMC8013B(nesFile);
            case 275:
                return null; // not supported
            case 276:
                return null; // not supported
            case 277:
                return null; // not supported
            case 278:
                return null; // not supported
            case 279:
                return null; // not supported
            case 280:
                return null; // not supported
            case 281:
                return new Mapper281(nesFile);
            case 282:
                return new Mapper282(nesFile); // TODO NOT FULLY WORKING
            case 283:
                return new GS2004(nesFile);
            case 284:
                return new DripGame(nesFile);
            case 285:
                return new A65AS(nesFile);
            case 286:
                return new BS5(nesFile);
            case 287:
                return new BMC411120C(nesFile);
            case 288:
                return new GKCX1(nesFile);
            case 289:
                return new BMC60311C(nesFile);
            case 290:
                return new NTD03(nesFile);
            case 291:
                return new Mapper291(nesFile);
            case 292:
                return new DRAGONFIGHTER(nesFile);
            case 293:
                return new Mapper293(nesFile);
            case 294:
                return new Mapper294(nesFile);
            case 295:
                return new Mapper295(nesFile); // TODO MALFUNCTIONING
            case 296:
                return null; // not supported
            case 297:
                return new Txc01_22110_000(nesFile);
            case 298:
                return new TF1201(nesFile);
            case 299:
                return new BMC11160(nesFile);
            case 300:
                return new BMC190In1(nesFile);
            case 301:
                return new BMC8157(nesFile);
            case 302:
                return new Kaiser7057(nesFile);
            case 303:
                return new Kaiser7017(nesFile);
            case 304:
                return new SMB2J(nesFile);
            case 305:
                return new Kaiser7031(nesFile);
            case 306:
                return new Kaiser7016(nesFile);
            case 307:
                return new Kaiser7037(nesFile);
            case 308:
                return new UNLTH2131_1(nesFile);
            case 309:
                return new LH51(nesFile);
            case 310:
                return null; // not supported
            case 311:
                return null; // not supported
            case 312:
                return new Kaiser7013B(nesFile);
            case 313:
                return new ResetTxROM(nesFile);
            case 314:
                return new BMC64In1NoRepeat(nesFile);
            case 315:
                return new BMC830134C(nesFile);
            case 316:
                return null; // not supported
            case 317:
                return null; // not supported
            case 319:
                return new HP898F(nesFile);
            case 320:
                return new BMC830425C4391T(nesFile);
            case 321:
                return null; // not supported
            case 322:
                return new BMCK3033(nesFile);
            case 323:
                return new FARID_SLROM_8IN1(nesFile);
            case 324:
                return new FARID_UNROM_8IN1(nesFile);
            case 325:
                return new MALISB(nesFile);
            case 326:
                return new Mapper326(nesFile);
            case 327:
                return new BMC1024CA1(nesFile);
            case 328:
                return new RT01(nesFile);
            case 329:
                return new EDU2000(nesFile);
            case 330:
                return new Mapper330(nesFile);
            case 331:
                return new BMC12In1(nesFile);
            case 332:
                return new WS(nesFile);
            case 333:
                return new BMC8In1(nesFile);
            case 334:
                return new Mapper334(nesFile);
            case 335:
                return new BMCCTC09(nesFile);
            case 336:
                return new BMCK3046(nesFile);
            case 337:
                return new BMCCTC12IN1(nesFile);
            case 338:
                return new BMCSA005A(nesFile);
            case 339:
                return new BMCK3006(nesFile);
            case 340:
                return new BMCK3036(nesFile);
            case 341:
                return new BMCTJ03(nesFile);
            case 342:
                return new COOLGIRL(nesFile);
            case 343:
                return new Mapper343(nesFile);
            case 344:
                return new BMCGN26(nesFile);
            case 345:
                return new BMCL6IN1(nesFile);
            case 346:
                return new Kaiser7012(nesFile);
            case 347:
                return new Kaiser7030(nesFile); // TODO WIP
            case 348:
                return new BMC830118C(nesFile);
            case 349:
                return new G146(nesFile);
            case 350:
                return new BMC891227(nesFile);
            case 351:
                return new Mapper351(nesFile);
            case 352:
                return new Kaiser106C(nesFile);
            case 353:
                return new Mapper353(nesFile);
            case 354:
                return new Mapper354(nesFile);
            case 355:
                return new _3D_Block(nesFile);
            case 356:
                return new Mapper356(nesFile);
            case 357:
                return new Mapper357(nesFile);
            case 358:
                return new Mapper358(nesFile);
            case 359:
                return new BMCSB5013(nesFile);
            case 360:
                return new Mapper360(nesFile);
            case 361:
                return new Mapper361(nesFile);
            case 362:
                return new Mapper362(nesFile); // TODO WIP
            case 363:
                return new Mapper363(nesFile); // TODO WIP
            case 364:
                return new Mapper364(nesFile);
            case 365:
                return new AsderPC95(nesFile);
            case 366:
                return new GN45(nesFile);

            case 512:
                return new Mapper512(nesFile); // TODO WIP
            case 513:
                return new SA9602B(nesFile);
            case 514:
                return new SuborKaraoke(nesFile);
            case 515:
                return new Mapper515(nesFile);
            case 516:
                return new Mapper516(nesFile);
            case 517:
                return new Mapper517(nesFile);
            case 518:
                return new DANCE2000(nesFile); // TODO NOT FULLY WORKING
            case 519:
                return new EH8813A(nesFile);
            case 520:
                return new Mapper520(nesFile);
            case 521:
                return new DREAMTECH01(nesFile);
            case 522:
                return new LH10(nesFile);
            case 523:
                return null; // not supported
            case 524:
                return new BTL900218(nesFile);
            case 525:
                return new Kaiser7021A(nesFile);
            case 526:
                return new BJ56(nesFile);
            case 527:
                return new AX40G(nesFile);
            case 528:
                return new UNL831128C(nesFile); // TODO WIP
            case 529:
                return new T230(nesFile);
            case 530:
                return new AX5705(nesFile);
            case 531:
                return new PC95KO01(nesFile); // TODO WIP
            case 532:
                return new NamcoX(nesFile); // TODO WIP
            case 533:
                return new Sachen3014(nesFile);
            case 534:
                return new Mapper534(nesFile);

            default:
                return null;
        }
    }
}
