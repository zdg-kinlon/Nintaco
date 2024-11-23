package nintaco.files;

import nintaco.cartdb.Cart;
import nintaco.mappers.nintendo.vs.VsGame;
import nintaco.tv.TVSystem;

import java.io.Serializable;

public interface CartFile extends IFile, Serializable {

    long serialVersionUID = 0;

    String getEntryFileName();

    String getArchiveFileName();

    String getFileName();

    boolean isChrRamPresent();

    boolean isNonVolatilePrgRamPresent();

    TVSystem getTvSystem();

    int getMirroring();

    int[] getPrgROM();

    int[] getChrROM();

    int getPrgRomLength();

    int getChrRomLength();

    int getFileCRC();

    int getVsHardware();

    Cart getCart();

    VsGame getVsGame();

    boolean isVsSystem();

    boolean isVsUniSystem();

    boolean isVsDualSystem();

    int getMapperNumber();

    int getSubmapperNumber();

    boolean isTrainerPresent();

    int getTrainerSize();

    int[] getTrainer();

    int getChrRamSize();

    int[] getFileContents();

    int getConsole();

    int getExtendedConsole();
}
