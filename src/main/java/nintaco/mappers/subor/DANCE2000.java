package nintaco.mappers.subor;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class DANCE2000 extends Mapper {

    private static final long serialVersionUID = 0;

    private int prgBank8;
    private int prgBankC;
    private int mode;
    private int dacStatus;
    private int dacOutput;
    private int dacCount;
    private boolean horizontalMirroing;
    private boolean bankSwitchCHR;

    public DANCE2000(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    private void writePrgRomBank(final int value) {

    }

    private void writeMode(final int value) {

    }

    private void writeDAC(final int value) {

    }

    private void writeFDC(final int value) {

    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address) {
            case 0x5000:
                writePrgRomBank(value);
                break;
            case 0x5200:
                writeMode(value);
                break;
            case 0x5300:
                writeDAC(value);
                break;
            case 0x5500:
                writeFDC(value);
                break;
        }
    }
  
  /*private int prg;
  private int mode;
  private int vramOffset;
  
  public DANCE2000(final CartFile cartFile) {
    super(cartFile, 4, 0);
  }

  @Override
  public void init() {
    updateBanks();
  }

  @Override
  public int readVRAM(final int address) {
    return vram[((address & 0xF000) == 0 ? vramOffset : 0) | address];
  }

  @Override
  public void writeVRAM(final int address, final int value) {
    vram[((address & 0xF000) == 0 ? vramOffset : 0) | address] = value;
  }

  @Override
  public int readMemory(final int address) {
    if (address >= 0x8000 && getBitBool(prg, 6)) {
      return 0;
    } else {
      return super.readMemory(address);
    }
  }

  @Override
  public void writeMemory(final int address, final int value) {
    switch(address) {
      case 0x5000: 
        prg = value; 
        updateBanks(); 
        break;
      case 0x5200:         
        mode = value; 
        if (getBitBool(mode, 2)) {
          updateBanks();
        } 
        break;
      default:
        super.writeMemory(address, value);
        break;
    }
  }
  
  @Override
  public void handlePpuCycle(final int scanline, final int scanlineCycle, 
      final int address, final boolean rendering) {
    if (getBitBool(mode, 1)) {
      if ((address & 0x3000) == 0x2000) {
        vramOffset = (address & 0x0800) << 1;
      }
    } else {
      vramOffset = 0;
    }
  }
  
  private void updateBanks() {    
    setNametableMirroring(mode & 1);
    if (getBitBool(mode, 2)) {      
      final int bank = (prg & 7) << 1;
      setPrgBank(2, bank);
      setPrgBank(3, bank | 1);
    } else {
      setPrgBank(2, prg & 0x0F);
      setPrgBank(3, 0);
    }
  }*/
}
