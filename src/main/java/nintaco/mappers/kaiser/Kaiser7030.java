package nintaco.mappers.kaiser;

// TODO SWITCH TO DISABLE FDS SOUND

// TODO WIP

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

// TODO OVERRIDE getPrgBank

public class Kaiser7030 extends Mapper {

    private static final long serialVersionUID = 0;

//  private final FdsAudio audio = new FdsAudio();

    private int bank1;
    private int bank2;

    public Kaiser7030(final CartFile cartFile) {
        super(cartFile, 1, 1);
    }

    @Override
    public void init() {
//    writePrgRomBankSelect1(-1);
//    writePrgRomBankSelect2(-1);
        bank1 = bank2 = 0xFF;
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeMirroringSelect(final int value) {
        setNametableMirroring(getBit(value, 3));
    }

    private void writePrgRomBankSelect1(final int value) {
        bank1 = (value & 0x07) << 12;
    }

    private void writePrgRomBankSelect2(final int value) {
        bank2 = (value & 0x0F) << 12;
    }

    @Override
    public int readMemory(final int address) {

//    if (address >= 0xD800 || (address >= 0x8000 && address < 0xB800)) {
//      return prgROM[0x10000 + address];
//    } else if ((address & 0xF000) == 0x7000) {
//      return prgROM[bank1 + address - 0x7000];
//    } else if (address >= 0x6C00 && address < 0x7000) {
//      return prgROM[bank2 + address + 0x1400];
//    } else if (address >= 0xC000 && address < 0xCC00) {
//      return prgROM[bank2 + address - 0x3C00];
//    } 

        if (address >= 0x6000 && address <= 0x6BFF) {
            return memory[address];
        } else if (address >= 0x6C00 && address <= 0x6FFF) {
            return prgROM[(address - 0x6C00 + 0x1000 * bank2 + 0x08000) & prgRomSizeMask];
        } else if (address >= 0x7000 && address <= 0x7FFF) {
            return prgROM[(address - 0x7000 + 0x1000 * bank1) & prgRomSizeMask];
        } else if (address >= 0xB800 && address <= 0xBFFF) {
            return memory[address];
        } else if (address >= 0xC000 && address <= 0xCBFF) {
            return prgROM[(address - 0xC000 + 0x1000 * bank2 + 0x08400) & prgRomSizeMask];
        } else if (address >= 0xCC00 && address <= 0xD7FF) {
            return memory[address];
        } else if (address >= 0x8000) {
            return prgROM[(address - 0x8000 + 0x18000) & prgRomSizeMask];
        }

//    final int value = audio.readRegister(address);
//    return (value >= 0) ? value : memory[address];
        return memory[address];
    }

    @Override
    public void writeMemory(final int address, int value) {
        memory[address] = value;

        if (address == 0x4025) {
            setNametableMirroring((value & 8) != 0 ? 1 : 0);
        }

        if (address >= 0x6000 && address <= 0x6BFF) {
            memory[address] = value;
        } else if (address >= 0xB800 && address <= 0xBFFF) {
            memory[address] = value;
        } else if (address >= 0xCC00 && address <= 0xD7FF) {
            memory[address] = value;
        } else if (address >= 0x8000 && address <= 0x8FFF) {
            bank1 = value & 7;
        } else if (address >= 0x9000 && address <= 0x9FFF) {
            bank2 = value & 15;
        }


//    if (!audio.writeRegister(address, value)) {
//      switch(address & 0xF000) {
//        case 0x4000:
//          if (address == 0x4025) {
//            writeMirroringSelect(value);
//          }
//          break;        
//        case 0x8000:
//          writePrgRomBankSelect1(value);
//          break;
//        case 0x9000:
//          writePrgRomBankSelect2(value);
//          break;
//      }
//    }
    }

//  @Override public void update() {
//    audio.update();
//  }
//
//  @Override public float getAudioSample() { 
//    return audio.getAudioSample();		
//  }
//
//  @Override public int getAudioMixerScale() {
//    return audio.getAudioMixerScale();
//  }  
}
