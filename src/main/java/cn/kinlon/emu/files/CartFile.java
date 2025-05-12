package cn.kinlon.emu.files;

import cn.kinlon.emu.cartdb.Cart;
import cn.kinlon.emu.mappers.nintendo.vs.VsGame;
import cn.kinlon.emu.tv.TVSystem;

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

    boolean isVsDualSystem();

    int getMapperNumber();

    int getSubmapperNumber();

    boolean isTrainerPresent();

    int getTrainerSize();

    int[] getTrainer();

    int getChrRamSize();

    int getConsole();

    int getExtendedConsole();
}
