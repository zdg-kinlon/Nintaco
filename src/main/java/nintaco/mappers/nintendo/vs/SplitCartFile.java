package nintaco.mappers.nintendo.vs;

import nintaco.cartdb.Cart;
import nintaco.files.CartFile;
import nintaco.tv.TVSystem;

public class SplitCartFile implements CartFile {

    private static final long serialVersionUID = 0;

    private final CartFile cartFile;
    private final int[] prgROM;
    private final int[] chrROM;

    public SplitCartFile(final CartFile cartFile, final boolean main) {

        this.cartFile = cartFile;

        final int[] _prgROM = cartFile.getPrgROM();
        prgROM = new int[_prgROM.length >> 1];
        System.arraycopy(_prgROM, main ? 0 : prgROM.length, prgROM, 0,
                prgROM.length);

        final int[] _chrROM = cartFile.getChrROM();
        if (_chrROM.length == 0x8000) {
            chrROM = new int[_chrROM.length >> 1];
            System.arraycopy(_chrROM, main ? 0 : chrROM.length, chrROM, 0,
                    chrROM.length);
        } else {
            chrROM = _chrROM;
        }
    }

    @Override
    public int[] getPrgROM() {
        return prgROM;
    }

    @Override
    public int[] getChrROM() {
        return chrROM;
    }

    @Override
    public int getPrgRomLength() {
        return prgROM.length;
    }

    @Override
    public int getChrRomLength() {
        return chrROM.length;
    }

    @Override
    public int getChrRamSize() {
        return cartFile.getChrRamSize();
    }

    @Override
    public int[] getFileContents() {
        return cartFile.getFileContents();
    }

    @Override
    public String getEntryFileName() {
        return cartFile.getEntryFileName();
    }

    @Override
    public String getArchiveFileName() {
        return cartFile.getArchiveFileName();
    }

    @Override
    public String getFileName() {
        return cartFile.getFileName();
    }

    @Override
    public boolean isChrRamPresent() {
        return cartFile.isChrRamPresent();
    }

    @Override
    public boolean isNonVolatilePrgRamPresent() {
        return cartFile.isNonVolatilePrgRamPresent();
    }

    @Override
    public TVSystem getTvSystem() {
        return cartFile.getTvSystem();
    }

    @Override
    public int getMirroring() {
        return cartFile.getMirroring();
    }

    @Override
    public int getFileCRC() {
        return cartFile.getFileCRC();
    }

    @Override
    public int getVsHardware() {
        return cartFile.getVsHardware();
    }

    @Override
    public Cart getCart() {
        return cartFile.getCart();
    }

    @Override
    public VsGame getVsGame() {
        return cartFile.getVsGame();
    }

    @Override
    public boolean isVsSystem() {
        return cartFile.isVsSystem();
    }

    @Override
    public boolean isVsUniSystem() {
        return cartFile.isVsUniSystem();
    }

    @Override
    public boolean isVsDualSystem() {
        return cartFile.isVsDualSystem();
    }

    @Override
    public int getMapperNumber() {
        return cartFile.getMapperNumber();
    }

    @Override
    public int getSubmapperNumber() {
        return cartFile.getSubmapperNumber();
    }

    @Override
    public boolean isTrainerPresent() {
        return cartFile.isTrainerPresent();
    }

    @Override
    public int getTrainerSize() {
        return cartFile.getTrainerSize();
    }

    @Override
    public int[] getTrainer() {
        return cartFile.getTrainer();
    }

    @Override
    public int getFileType() {
        return cartFile.getFileType();
    }

    @Override
    public int getConsole() {
        return cartFile.getConsole();
    }

    @Override
    public int getExtendedConsole() {
        return cartFile.getExtendedConsole();
    }
}