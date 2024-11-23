package nintaco.mappers.nintendo.vs;

import nintaco.files.NesFile;
import nintaco.input.dipswitches.DipSwitch;

import java.util.ArrayList;
import java.util.List;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.mappers.nintendo.vs.VsHardware.*;
import static nintaco.mappers.nintendo.vs.VsPPU.*;

public enum VsGame {

    // VS. DualSystem games
    BalloonFight(99, FOUR_SCREEN, RP2C04_0003, VS_DUALSYSTEM_NORMAL, false,
            false),
    Baseball(99, FOUR_SCREEN, RP2C04_0001, VS_DUALSYSTEM_NORMAL, false, false),
    IceClimberDual(99, FOUR_SCREEN, RP2C04_0004, VS_DUALSYSTEM_NORMAL, true,
            false),
    Mahjong(99, FOUR_SCREEN, RP2C03B, VS_DUALSYSTEM_NORMAL, false, false),
    RaidOnBungelingBay(99, FOUR_SCREEN, RP2C04_0002,
            VS_DUALSYSTEM_RAID_ON_BUNGELING_BAY, true, false),
    Tennis(99, FOUR_SCREEN, RP2C03B, VS_DUALSYSTEM_NORMAL, false, false),
    WreckingCrew(99, FOUR_SCREEN, RP2C04_0002, VS_DUALSYSTEM_NORMAL, false,
            false),

    // VS. UniSystem games
    BattleCity(99, FOUR_SCREEN, RP2C04_0001, VS_UNISYSTEM_NORMAL, false, false),
    Castlevania(2, VERTICAL, RP2C04_0002, VS_UNISYSTEM_NORMAL, false, false),
    CluCluLand(99, FOUR_SCREEN, RP2C04_0004, VS_UNISYSTEM_NORMAL, true, false),
    DrMario(1, HORIZONTAL, RP2C04_0003, VS_UNISYSTEM_NORMAL, true, false),
    DuckHunt(99, FOUR_SCREEN, RP2C03B, VS_UNISYSTEM_NORMAL, false, true),
    Excitebike(99, FOUR_SCREEN, RP2C04_0003, VS_UNISYSTEM_NORMAL, false, false),
    ExcitebikeJapan(99, FOUR_SCREEN, RP2C04_0004, VS_UNISYSTEM_NORMAL, false,
            false),
    FreedomForce(4, VERTICAL, RP2C04_0001, VS_UNISYSTEM_NORMAL, false, true),
    Goonies(75, FOUR_SCREEN, RP2C04_0003, VS_UNISYSTEM_NORMAL, false, false),
    Gradius(75, FOUR_SCREEN, RP2C04_0001, VS_UNISYSTEM_NORMAL, true, false),
    Gumshoe(99, FOUR_SCREEN, RC2C05_03, VS_UNISYSTEM_NORMAL, false, true, 0x1C),
    HogansAlley(99, FOUR_SCREEN, RP2C04_0001, VS_UNISYSTEM_NORMAL, false, true),
    IceClimber(99, FOUR_SCREEN, RP2C04_0004, VS_UNISYSTEM_NORMAL, true, false),
    IceClimber_2(99, FOUR_SCREEN, RP2C04_0004, VS_UNISYSTEM_ICE_CLIMBER_JAPAN,
            true, false),
    LadiesGolf(99, FOUR_SCREEN, RP2C04_0002, VS_UNISYSTEM_NORMAL, true, false),
    MachRider(99, FOUR_SCREEN, RP2C04_0002, VS_UNISYSTEM_NORMAL, false, false),
    MachRider_2(99, FOUR_SCREEN, RP2C04_0001, VS_UNISYSTEM_NORMAL, false, false),
    MachRiderJapan(99, FOUR_SCREEN, RP2C04_0001, VS_UNISYSTEM_NORMAL, false,
            false),
    MightyBombJack(0, FOUR_SCREEN, RC2C05_02, VS_UNISYSTEM_NORMAL, false, false,
            0x3D),
    NinjaJajamaruKun(99, FOUR_SCREEN, RC2C05_01, VS_UNISYSTEM_NORMAL, true,
            false),
    Pinball(99, FOUR_SCREEN, RP2C04_0001, VS_UNISYSTEM_NORMAL, false, false),
    PinballJapan(99, FOUR_SCREEN, RP2C03B, VS_UNISYSTEM_NORMAL, false, false),
    Platoon(68, VERTICAL, RP2C04_0001, VS_UNISYSTEM_NORMAL, false, false),
    RbiBaseball(206, VERTICAL, RP2C04_0002, VS_UNISYSTEM_RBI_BASEBALL, true,
            false),
    RbiBaseball_2(206, VERTICAL, RP2C04_0001, VS_UNISYSTEM_RBI_BASEBALL, true,
            false),
    SkateKids(99, FOUR_SCREEN, RP2C04_0004, VS_UNISYSTEM_NORMAL, false, false),
    Slalom(0, FOUR_SCREEN, RP2C04_0002, VS_UNISYSTEM_NORMAL, false, false),
    Soccer(99, FOUR_SCREEN, RP2C04_0003, VS_UNISYSTEM_NORMAL, true, false),
    Soccer_2(99, FOUR_SCREEN, RP2C04_0002, VS_UNISYSTEM_NORMAL, true, false),
    StarLuster(99, FOUR_SCREEN, RP2C03B, VS_UNISYSTEM_NORMAL, false, false),
    StrokeAndMatchGolf(99, FOUR_SCREEN, RP2C04_0002, VS_UNISYSTEM_NORMAL, true,
            false),
    StrokeAndMatchGolfJapan(99, FOUR_SCREEN, RP2C03B, VS_UNISYSTEM_NORMAL, true,
            false),
    SuperMarioBros(99, FOUR_SCREEN, RP2C04_0004, VS_UNISYSTEM_NORMAL, false,
            false),
    SuperSkyKid(206, VERTICAL, RP2C04_0002, VS_UNISYSTEM_NORMAL, true, false),
    SuperXevious(206, HORIZONTAL, RP2C04_0002, VS_UNISYSTEM_SUPER_XEVIOUS, false,
            false),
    Tetris(99, FOUR_SCREEN, RP2C04_0002, VS_UNISYSTEM_NORMAL, true, false),
    TkoBoxing(206, VERTICAL, RP2C03B, VS_UNISYSTEM_TKO_BOXING, false, false),
    TopGun(2, HORIZONTAL, RC2C05_04, VS_UNISYSTEM_NORMAL, false, false, 0x1B);

    private static final Object[][] NameMappings = {
            {"rbi", RbiBaseball},
            {"r.b.i.", RbiBaseball},
            {"balloon", BalloonFight},
            {"baseball", Baseball},
            {"battle", BattleCity},
            {"land", CluCluLand},
            {"dr", DrMario},
            {"duck", DuckHunt},
            {"eb4-4", ExcitebikeJapan},
            {"excite", Excitebike},
            {"force", FreedomForce},
            {"goonies", Goonies},
            {"gradius", Gradius},
            {"gumshoe", Gumshoe},
            {"alley", HogansAlley},
            {"ic4-4", IceClimber_2},
            {"climber dual", IceClimberDual},
            {"climber", IceClimber},
            {"mr4-1", MachRiderJapan},
            {"rider", MachRider},
            {"ladies", LadiesGolf},
            {"mahjong", Mahjong},
            {"mighty", MightyBombJack},
            {"ninja", NinjaJajamaruKun},
            {"pinball", Pinball},
            {"platoon", Platoon},
            {"skate", SkateKids},
            {"sc4-2", Soccer_2},
            {"soccer", Soccer},
            {"luster", StarLuster},
            {"stroke", StrokeAndMatchGolf},
            {"kid", SuperSkyKid},
            {"xevious", SuperXevious},
            {"tennis", Tennis},
            {"tetris", Tetris},
            {"top", TopGun},
            {"castle", Castlevania},
            {"slalom", Slalom},
            {"mario", SuperMarioBros},
            {"crew", WreckingCrew},
            {"tko", TkoBoxing},
            {"t.k.o.", TkoBoxing},
            {"raid", RaidOnBungelingBay},
    };

    static {

        BalloonFight.add("Coinage")
                .add("1 Coin / 1 Credit", 0x0000)
                .add("1 Coin / 2 Credits", 0x0004)
                .add("1 Coin / 3 Credits", 0x0002)
                .add("1 Coin / 4 Credits", 0x0006)
                .add("2 Coins / 1 Credit", 0x0001)
                .add("3 Coins / 1 Credit", 0x0005)
                .add("4 Coins / 1 Credit", 0x0003)
                .add("Free Play", 0x0007);
        BalloonFight.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x0008);
        BalloonFight.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x0010);
        BalloonFight.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x0020);
        BalloonFight.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x0040);
        BalloonFight.add("Mode")
                .add("Game", 0x0000)
                .add("Test", 0x0080);
        BalloonFight.add("Lives")
                .add("3", 0x0000)
                .add("4", 0x0200)
                .add("5", 0x0100)
                .add("6", 0x0300);
        BalloonFight.add("Difficulty", 1)
                .add("Easy", 0x0000)
                .add("Normal", 0x0800)
                .add("Medium", 0x0400)
                .add("Hard", 0x0C00);
        BalloonFight.add("Enemy Regeneration")
                .add("Low", 0x0000)
                .add("High", 0x1000);
        BalloonFight.add("Bonus", 1)
                .add("10k", 0x6000)
                .add("20k", 0x2000)
                .add("40k", 0x4000)
                .add("None", 0x0000);
        BalloonFight.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x8000);

        Baseball.add("Player Defense Strength", 1)
                .add("Weak", 0x0000)
                .add("Normal", 0x0002)
                .add("Medium", 0x0001)
                .add("Strong", 0x0003);
        Baseball.add("Player Offense Strength", 1)
                .add("Weak", 0x0000)
                .add("Normal", 0x0008)
                .add("Medium", 0x0004)
                .add("Strong", 0x000C);
        Baseball.add("Computer Defense Strength", 1)
                .add("Weak", 0x0000)
                .add("Normal", 0x0020)
                .add("Medium", 0x0010)
                .add("Strong", 0x0030);
        Baseball.add("Computer Offense Strength", 1)
                .add("Weak", 0x0000)
                .add("Normal", 0x0080)
                .add("Medium", 0x0040)
                .add("Strong", 0x00C0);
        Baseball.add("Mode")
                .add("Game", 0x0000)
                .add("Test", 0x0100);
        Baseball.add("Coinage")
                .add("1 Coin / 1 Credit", 0x0000)
                .add("1 Coin / 2 Credits", 0x0400)
                .add("2 Coins / 1 Credit", 0x0200)
                .add("Free Play", 0x0600);
        Baseball.add("Starting Points")
                .add("80", 0x0000)
                .add("100", 0x2000)
                .add("150", 0x1000)
                .add("200", 0x3000)
                .add("250", 0x0800)
                .add("300", 0x2800)
                .add("350", 0x1800)
                .add("400", 0x3800);
        Baseball.add("Bonus Play", 1)
                .add("Off", 0x4000)
                .add("On", 0x0000);
        Baseball.add("Demo Sounds", 1)
                .add("Off", 0x0000)
                .add("On", 0x8000);

        BattleCity.add("Credits for 2 Players", 1)
                .add("1", 0x00)
                .add("2", 0x01);
        BattleCity.add("Lives")
                .add("3", 0x00)
                .add("5", 0x02);
        BattleCity.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x04);
        BattleCity.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        BattleCity.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        BattleCity.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        BattleCity.add("PPU")
                .add("RP2C04-0001", 0x00)
                .add("RP2C04-0002", 0x40)
                .add("RP2C04-0003", 0x80)
                .add("RP2C04-0004", 0xC0);

        Castlevania.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Castlevania.add("Lives", 1)
                .add("2", 0x08)
                .add("3", 0x00);
        Castlevania.add("Bonus")
                .add("100k", 0x00)
                .add("200k", 0x20)
                .add("300k", 0x10)
                .add("400k", 0x30);
        Castlevania.add("Difficulty")
                .add("Normal", 0x00)
                .add("Hard", 0x40);

        CluCluLand.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        CluCluLand.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        CluCluLand.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        CluCluLand.add("Lives", 1)
                .add("2", 0x60)
                .add("3", 0x00)
                .add("4", 0x40)
                .add("5", 0x20);
        CluCluLand.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        DrMario.add("Drop Rate Increases After")
                .add("7 Pills", 0x00)
                .add("8 Pills", 0x01)
                .add("9 Pills", 0x02)
                .add("10 Pills", 0x03);
        DrMario.add("Virus Level")
                .add("1", 0x00)
                .add("3", 0x04)
                .add("5", 0x08)
                .add("7", 0x0C);
        DrMario.add("Drop Speed Up")
                .add("Slow", 0x00)
                .add("Medium", 0x10)
                .add("Fast", 0x20)
                .add("Fastest", 0x30);
        DrMario.add("Free Play")
                .add("Off", 0x00)
                .add("On", 0x40);
        DrMario.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x80);

        DuckHunt.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        DuckHunt.add("Difficulty", 1)
                .add("Easy", 0x00)
                .add("Normal", 0x08)
                .add("Hard", 0x10)
                .add("Very Hard", 0x18);
        DuckHunt.add("Misses per Game", 1)
                .add("3", 0x00)
                .add("5", 0x20);
        DuckHunt.add("Bonus Life")
                .add("30000", 0x00)
                .add("50000", 0x40)
                .add("80000", 0x80)
                .add("100000", 0xC0);

        Excitebike.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Excitebike.add("Bonus")
                .add("100k and Every 50k", 0x00)
                .add("Every 100k", 0x10)
                .add("100k Only", 0x08)
                .add("None", 0x18);
        Excitebike.add("1st Half Qualifying Time")
                .add("Normal", 0x00)
                .add("Hard", 0x20);
        Excitebike.add("2nd Half Qualifying Time")
                .add("Normal", 0x00)
                .add("Hard", 0x40);

        ExcitebikeJapan.addAll(Excitebike.getDipSwitches());

        FreedomForce.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        FreedomForce.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        FreedomForce.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        FreedomForce.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        FreedomForce.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        FreedomForce.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        LadiesGolf.add("Coinage")
                .add("1 Coin / 1 Credit", 0x01)
                .add("1 Coin / 2 Credits", 0x06)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x04)
                .add("2 Coins / 1 Credit", 0x05)
                .add("3 Coins / 1 Credit", 0x03)
                .add("4 Coins / 1 Credit", 0x07)
                .add("Free Play", 0x00);
        LadiesGolf.add("Hole Size")
                .add("Large", 0x00)
                .add("Small", 0x08);
        LadiesGolf.add("Points per Stroke")
                .add("Easier", 0x00)
                .add("Harder", 0x10);
        LadiesGolf.add("Starting Points")
                .add("10", 0x00)
                .add("13", 0x40)
                .add("16", 0x20)
                .add("20", 0x60);
        LadiesGolf.add("Difficulty Vs. Computer")
                .add("Easy", 0x00)
                .add("Hard", 0x80);

        Goonies.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Goonies.add("Lives")
                .add("3", 0x00)
                .add("2", 0x08);
        Goonies.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        Goonies.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        Goonies.add("Timer")
                .add("Normal", 0x00)
                .add("Fast", 0x40);
        Goonies.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x80);

        Gradius.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Gradius.add("Lives")
                .add("3", 0x08)
                .add("4", 0x00);
        Gradius.add("Bonus")
                .add("100k", 0x00)
                .add("200k", 0x20)
                .add("300k", 0x10)
                .add("400k", 0x30);
        Gradius.add("Difficulty")
                .add("Normal", 0x00)
                .add("Hard", 0x40);
        Gradius.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x80);

        Gumshoe.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Gumshoe.add("Difficulty", 1)
                .add("Easy", 0x00)
                .add("Normal", 0x08)
                .add("Hard", 0x10)
                .add("Very Hard", 0x18);
        Gumshoe.add("Lives", 1)
                .add("3", 0x10)
                .add("5", 0x00);
        Gumshoe.add("Bullets per Balloon", 1)
                .add("2", 0x40)
                .add("3", 0x00);
        Gumshoe.add("Bonus Life")
                .add("80000", 0x00)
                .add("100000", 0x80);

        HogansAlley.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        HogansAlley.add("Difficulty", 1)
                .add("Easy", 0x00)
                .add("Normal", 0x08)
                .add("Hard", 0x10)
                .add("Very Hard", 0x18);
        HogansAlley.add("Misses per Game", 1)
                .add("3", 0x00)
                .add("5", 0x20);
        HogansAlley.add("Bonus Life")
                .add("30000", 0x00)
                .add("50000", 0x40)
                .add("80000", 0x80)
                .add("100000", 0xC0);

        IceClimber.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        IceClimber.add("Lives")
                .add("3", 0x00)
                .add("4", 0x10)
                .add("5", 0x08)
                .add("7", 0x18);
        IceClimber.add("Difficulty")
                .add("Normal", 0x00)
                .add("Hard", 0x20);
        IceClimber.add("Time before the bear")
                .add("Long", 0x00)
                .add("Short", 0x40);

        IceClimber_2.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        IceClimber_2.add("Lives")
                .add("3", 0x00)
                .add("4", 0x10)
                .add("5", 0x08)
                .add("7", 0x18);
        IceClimber_2.add("Difficulty")
                .add("Normal", 0x00)
                .add("Hard", 0x20);
        IceClimber_2.add("Time before the bear")
                .add("Long", 0x00)
                .add("Short", 0x40);

        IceClimberDual.add("Coinage (Left Side)")
                .add("1 Coin / 1 Credit", 0x0000)
                .add("1 Coin / 2 Credits", 0x0004)
                .add("1 Coin / 3 Credits", 0x0002)
                .add("1 Coin / 4 Credits", 0x0006)
                .add("2 Coins / 1 Credit", 0x0001)
                .add("3 Coins / 1 Credit", 0x0005)
                .add("4 Coins / 1 Credit", 0x0003)
                .add("Free Play", 0x0007);
        IceClimberDual.add("Lives (Left Side)")
                .add("3", 0x0000)
                .add("4", 0x0010)
                .add("5", 0x0008)
                .add("7", 0x0018);
        IceClimberDual.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0020);
        IceClimberDual.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0040);
        IceClimberDual.add("Mode (Left Side)")
                .add("Game", 0x0000)
                .add("Test", 0x0080);
        IceClimberDual.add("Coinage (Right Side)")
                .add("1 Coin / 1 Credit", 0x0000)
                .add("1 Coin / 2 Credits", 0x0400)
                .add("1 Coin / 3 Credits", 0x0200)
                .add("1 Coin / 4 Credits", 0x0600)
                .add("2 Coins / 1 Credit", 0x0100)
                .add("3 Coins / 1 Credit", 0x0500)
                .add("4 Coins / 1 Credit", 0x0300)
                .add("Free Play", 0x0700);
        IceClimberDual.add("Lives (Right Side)")
                .add("3", 0x0000)
                .add("4", 0x1000)
                .add("5", 0x0800)
                .add("7", 0x1800);
        IceClimberDual.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x2000);
        IceClimberDual.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x4000);
        IceClimberDual.add("Mode (Right Side)")
                .add("Game", 0x0000)
                .add("Test", 0x8000);

        MachRider.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        MachRider.add("Time")
                .add("280", 0x00)
                .add("250", 0x10)
                .add("220", 0x08)
                .add("200", 0x18);
        MachRider.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        MachRider.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        MachRider.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        MachRider_2.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        MachRider_2.add("Time")
                .add("280", 0x00)
                .add("250", 0x10)
                .add("220", 0x08)
                .add("200", 0x18);
        MachRider_2.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        MachRider_2.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        MachRider_2.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        MachRiderJapan.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        MachRiderJapan.add("Km 1st Race")
                .add("12", 0x00)
                .add("15", 0x10);
        MachRiderJapan.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        MachRiderJapan.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        MachRiderJapan.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0001);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0002);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0004);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0008);
        Mahjong.add("Time", 3)
                .add("30", 0x0030)
                .add("45", 0x0010)
                .add("60", 0x0020)
                .add("90", 0x0000);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0040);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0080);
        Mahjong.add("Mode")
                .add("Game", 0x0000)
                .add("Test", 0x0100);
        Mahjong.add("Coinage")
                .add("1 Coin / 1 Credit", 0x0000)
                .add("1 Coin / 2 Credits", 0x0400)
                .add("2 Coins / 1 Credit", 0x0200)
                .add("Free Play", 0x0600);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x0800);
        Mahjong.add("Unknown/Unused")
                .add("Off", 0x0000)
                .add("On", 0x1000);
        Mahjong.add("Starting Points", 1)
                .add("15k", 0x6000)
                .add("20k", 0x2000)
                .add("25k", 0x4000)
                .add("30k", 0x0000);
        Mahjong.add("Demo Sounds", 1)
                .add("Off", 0x0000)
                .add("On", 0x8000);

        MightyBombJack.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("5 Coins / 1 Credit", 0x07);
        MightyBombJack.add("Lives")
                .add("2", 0x10)
                .add("3", 0x00)
                .add("4", 0x08)
                .add("5", 0x18);
        MightyBombJack.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        MightyBombJack.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        MightyBombJack.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        NinjaJajamaruKun.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        NinjaJajamaruKun.add("Lives")
                .add("3", 0x00)
                .add("4", 0x10)
                .add("5", 0x08);
        NinjaJajamaruKun.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        NinjaJajamaruKun.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        NinjaJajamaruKun.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x80);

        Pinball.add("Coinage")
                .add("1 Coin / 1 Credit", 0x01)
                .add("1 Coin / 2 Credits", 0x06)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x04)
                .add("2 Coins / 1 Credit", 0x05)
                .add("3 Coins / 1 Credit", 0x03)
                .add("4 Coins / 1 Credit", 0x07)
                .add("Free Play", 0x00);
        Pinball.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        Pinball.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        Pinball.add("Balls", 1)
                .add("2", 0x60)
                .add("3", 0x00)
                .add("4", 0x40)
                .add("5", 0x20);
        Pinball.add("Ball Speed")
                .add("Normal", 0x00)
                .add("Fast", 0x80);

        PinballJapan.addAll(Pinball.getDipSwitches());

        Platoon.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x01);
        Platoon.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x02);
        Platoon.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x04);
        Platoon.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        Platoon.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        Platoon.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x20)
                .add("1 Coin / 3 Credits", 0x40)
                .add("2 Coins / 1 Credit", 0x60)
                .add("3 Coins / 1 Credit", 0x80)
                .add("4 Coins / 1 Credit", 0xA0)
                .add("5 Coins / 1 Credit", 0xC0)
                .add("Free Play", 0xE0);

        RaidOnBungelingBay.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x03)
                .add("3 Coins / 1 Credit", 0x05)
                .add("Free Play", 0x07);
        RaidOnBungelingBay.add("Lives")
                .add("2", 0x00)
                .add("3", 0x08);
        RaidOnBungelingBay.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        RaidOnBungelingBay.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x20);
        RaidOnBungelingBay.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        RaidOnBungelingBay.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        RbiBaseball.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x01)
                .add("2 Coins / 1 Credit", 0x02)
                .add("3 Coins / 1 Credit", 0x03);
        RbiBaseball.add("Max. 1p/in, 2p/in, Min", 1)
                .add("2, 1, 3", 0x04)
                .add("2, 2, 4", 0x0C)
                .add("3, 2, 6", 0x00)
                .add("4, 3, 7", 0x08);
        RbiBaseball.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x10);
        RbiBaseball.add("PPU", 2)
                .add("RP2C03", 0x20)
                .add("RP2C04-0001", 0x00)
                .add("RP2C04-0002", 0x40)
                .add("RP2C04-0003", 0x80)
                .add("RP2C04-0004", 0xC0);

        RbiBaseball_2.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x01)
                .add("2 Coins / 1 Credit", 0x02)
                .add("3 Coins / 1 Credit", 0x03);
        RbiBaseball_2.add("Max. 1p/in, 2p/in, Min", 1)
                .add("2, 1, 3", 0x04)
                .add("2, 2, 4", 0x0C)
                .add("3, 2, 6", 0x00)
                .add("4, 3, 7", 0x08);
        RbiBaseball_2.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x10);
        RbiBaseball_2.add("PPU", 1)
                .add("RP2C03", 0x20)
                .add("RP2C04-0001", 0x00)
                .add("RP2C04-0002", 0x40)
                .add("RP2C04-0003", 0x80)
                .add("RP2C04-0004", 0xC0);

        SkateKids.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x06)
                .add("1 Coin / 3 Credits", 0x01)
                .add("1 Coin / 4 Credits", 0x05)
                .add("1 Coin / 5 Credits", 0x03)
                .add("2 Coins / 1 Credit", 0x04)
                .add("3 Coins / 1 Credit", 0x02)
                .add("Free Play", 0x07);
        SkateKids.add("Lives", 1)
                .add("2", 0x08)
                .add("3", 0x00);
        SkateKids.add("Bonus Life")
                .add("100", 0x00)
                .add("150", 0x20)
                .add("200", 0x10)
                .add("250", 0x30);
        SkateKids.add("Timer")
                .add("Normal", 0x00)
                .add("Fast", 0x40);
        SkateKids.add("Continue Lives")
                .add("3", 0x80)
                .add("4", 0x00);

        Slalom.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Slalom.add("Freestyle Points")
                .add("Left / Right", 0x00)
                .add("Hold Time", 0x08);
        Slalom.add("Difficulty", 1)
                .add("Easy", 0x00)
                .add("Normal", 0x10)
                .add("Hard", 0x20)
                .add("Hardest", 0x30);
        Slalom.add("Allow Continue", 1)
                .add("No", 0x40)
                .add("Yes", 0x00);
        Slalom.add("Inverted input")
                .add("Off", 0x00)
                .add("On", 0x80);

        Soccer.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Soccer.add("Points Timer", 2)
                .add("600 Pts", 0x00)
                .add("800 Pts", 0x10)
                .add("1000 Pts", 0x08)
                .add("1200 Pts", 0x18);
        Soccer.add("Difficulty", 1)
                .add("Easy", 0x00)
                .add("Normal", 0x40)
                .add("Hard", 0x20)
                .add("Very Hard", 0x60);

        Soccer_2.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("1 Coin / 4 Credits", 0x06)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x05)
                .add("4 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        Soccer_2.add("Points Timer", 2)
                .add("600 Pts", 0x00)
                .add("800 Pts", 0x10)
                .add("1000 Pts", 0x08)
                .add("1200 Pts", 0x18);
        Soccer_2.add("Difficulty", 1)
                .add("Easy", 0x00)
                .add("Normal", 0x40)
                .add("Hard", 0x20)
                .add("Very Hard", 0x60);

        StarLuster.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x03);
        StarLuster.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x04);
        StarLuster.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        StarLuster.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        StarLuster.add("Palette Color")
                .add("Black", 0x40)
                .add("Green", 0x20)
                .add("Grey", 0x60);
        StarLuster.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        StrokeAndMatchGolf.addAll(LadiesGolf.getDipSwitches());

        StrokeAndMatchGolfJapan.addAll(LadiesGolf.getDipSwitches());

        SuperMarioBros.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x06)
                .add("1 Coin / 3 Credits", 0x01)
                .add("1 Coin / 4 Credits", 0x05)
                .add("1 Coin / 5 Credits", 0x03)
                .add("2 Coins / 1 Credit", 0x04)
                .add("3 Coins / 1 Credit", 0x02)
                .add("Free Play", 0x07);
        SuperMarioBros.add("Lives", 1)
                .add("2", 0x08)
                .add("3", 0x00);
        SuperMarioBros.add("Bonus Life")
                .add("100", 0x00)
                .add("150", 0x20)
                .add("200", 0x10)
                .add("250", 0x30);
        SuperMarioBros.add("Timer")
                .add("Normal", 0x00)
                .add("Fast", 0x40);
        SuperMarioBros.add("Continue Lives")
                .add("3", 0x80)
                .add("4", 0x00);

        SuperSkyKid.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x01);
        SuperSkyKid.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x02);
        SuperSkyKid.add("Lives")
                .add("2", 0x00)
                .add("3", 0x04);
        SuperSkyKid.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x08)
                .add("2 Coins / 1 Credit", 0x10)
                .add("3 Coins / 1 Credit", 0x18);
        SuperSkyKid.add("PPU", 2)
                .add("RP2C03", 0x20)
                .add("RP2C04-0001", 0x00)
                .add("RP2C04-0002", 0x40)
                .add("RP2C04-0003", 0x80)
                .add("RP2C04-0004", 0xC0);

        SuperXevious.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x01);
        SuperXevious.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x02);
        SuperXevious.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x04);
        SuperXevious.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        SuperXevious.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x10)
                .add("2 Coins / 1 Credit", 0x20)
                .add("3 Coins / 1 Credit", 0x30);
        SuperXevious.add("PPU", 1)
                .add("RP2C04-0001", 0x00)
                .add("RP2C04-0002", 0x40)
                .add("RP2C04-0003", 0x80)
                .add("RP2C04-0004", 0xC0);

        Tennis.add("Difficulty vs. Computer")
                .add("Easy", 0x0000)
                .add("Normal", 0x0002)
                .add("Medium", 0x0001)
                .add("Hard", 0x0003);
        Tennis.add("Difficulty vs. Player")
                .add("Easy", 0x0000)
                .add("Normal", 0x0008)
                .add("Medium", 0x0004)
                .add("Hard", 0x000C);
        Tennis.add("Raquet Size")
                .add("Large", 0x0000)
                .add("Small", 0x0010);
        Tennis.add("Extra Score")
                .add("1 Set", 0x0000)
                .add("1 Game", 0x0020);
        Tennis.add("Court Color")
                .add("Green", 0x0000)
                .add("Blue", 0x0040);
        Tennis.add("Copyright")
                .add("Japan", 0x0000)
                .add("USA", 0x0080);
        Tennis.add("Mode")
                .add("Game", 0x0000)
                .add("Test", 0x0100);
        Tennis.add("Coinage")
                .add("1 Coin / 1 Credit", 0x0000)
                .add("1 Coin / 2 Credits", 0x0400)
                .add("2 Coins / 1 Credit", 0x0200)
                .add("Free Play", 0x0600);
        Tennis.add("Game Mode - Credits (1VsC/2VsC/1Vs1/2Vs2)", 2)
                .add("A - 1/1/1/1", 0x0000)
                .add("B - 1/2/1/2", 0x1000)
                .add("C - 2/2/2/2", 0x0800)
                .add("D - 2/2/4/4", 0x1800);
        Tennis.add("Rackets Per Game", 1)
                .add("2", 0x6000)
                .add("3", 0x0000)
                .add("4", 0x4000)
                .add("5", 0x2000);
        Tennis.add("Demo Sounds", 1)
                .add("Off", 0x0000)
                .add("On", 0x8000);

        Tetris.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x01)
                .add("3 Coins / 1 Credit", 0x03);
        Tetris.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x04);
        Tetris.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        Tetris.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        Tetris.add("Palette Color")
                .add("Black", 0x40)
                .add("Green", 0x20)
                .add("Grey", 0x60);
        Tetris.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        TkoBoxing.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x01)
                .add("2 Coins / 1 Credit", 0x02)
                .add("3 Coins / 1 Credit", 0x03);
        TkoBoxing.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x04);
        TkoBoxing.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x08);
        TkoBoxing.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x10);
        TkoBoxing.add("Palette Color", 1)
                .add("Black", 0x00)
                .add("White", 0x20);
        TkoBoxing.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x40);
        TkoBoxing.add("Unknown/Unused")
                .add("Off", 0x00)
                .add("On", 0x80);

        TopGun.add("Coinage")
                .add("1 Coin / 1 Credit", 0x00)
                .add("1 Coin / 2 Credits", 0x04)
                .add("1 Coin / 3 Credits", 0x02)
                .add("2 Coins / 1 Credit", 0x06)
                .add("3 Coins / 1 Credit", 0x01)
                .add("4 Coins / 1 Credit", 0x05)
                .add("5 Coins / 1 Credit", 0x03)
                .add("Free Play", 0x07);
        TopGun.add("Lives per Coin")
                .add("3 - 12 Max", 0x00)
                .add("2 - 9 Max", 0x08);
        TopGun.add("Bonus")
                .add("30k and every 50k", 0x00)
                .add("50k and every 100k", 0x20)
                .add("100k and every 150k", 0x10)
                .add("200k and every 200k", 0x30);
        TopGun.add("Difficulty")
                .add("Normal", 0x00)
                .add("Hard", 0x40);
        TopGun.add("Demo Sounds", 1)
                .add("Off", 0x00)
                .add("On", 0x80);

        WreckingCrew.add("Lives")
                .add("3", 0x0000)
                .add("4", 0x0002)
                .add("5", 0x0001)
                .add("6", 0x0003);
        WreckingCrew.add("1st Bonus Life", 3)
                .add("20k", 0x0000)
                .add("30k", 0x0010)
                .add("40k", 0x0008)
                .add("50k", 0x0018)
                .add("70k", 0x0004)
                .add("80k", 0x0014)
                .add("100k", 0x000C)
                .add("None", 0x001C);
        WreckingCrew.add("Additional Bonus Lives", 7)
                .add("20k", 0x0000)
                .add("30k", 0x0080)
                .add("40k", 0x0040)
                .add("50k", 0x00C0)
                .add("70k", 0x0020)
                .add("80k", 0x0060)
                .add("100k", 0x00A0)
                .add("None", 0x00E0);
        WreckingCrew.add("Coinage")
                .add("1 Coin / 1 Credit", 0x0100)
                .add("1 Coin / 2 Credits", 0x0600)
                .add("1 Coin / 3 Credits", 0x0200)
                .add("1 Coin / 4 Credits", 0x0400)
                .add("2 Coins / 1 Credit", 0x0500)
                .add("3 Coins / 1 Credit", 0x0300)
                .add("4 Coins / 1 Credit", 0x0700)
                .add("Free Play", 0x0000);
        WreckingCrew.add("Difficulty", 1)
                .add("Easy", 0x0000)
                .add("Normal", 0x0800)
                .add("Medium", 0x1000)
                .add("Hard", 0x1800);
        WreckingCrew.add("Copyright")
                .add("Japan", 0x0000)
                .add("USA", 0x2000);
        WreckingCrew.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x4000);
        WreckingCrew.add("Unused")
                .add("Off", 0x0000)
                .add("On", 0x8000);
    }

    private final List<DipSwitch> dipSwitches = new ArrayList<>();
    private final int mapper;
    private final int mirroring;
    private final int ppu;
    private final int hardware;
    private final boolean swapControllers;
    private final boolean zapperGame;
    private final int ppuStatusID;

    VsGame(final int mapper, final int mirroring, final int ppu,
           final int hardware, final boolean swapControllers,
           final boolean zapperGame) {
        this(mapper, mirroring, ppu, hardware, swapControllers, zapperGame, 0);
    }

    VsGame(final int mapper, final int mirroring, final int ppu,
           final int hardware, final boolean swapControllers,
           final boolean zapperGame, final int ppuStatusID) {
        this.mapper = mapper;
        this.mirroring = mirroring;
        this.ppu = ppu;
        this.hardware = hardware;
        this.swapControllers = swapControllers;
        this.zapperGame = zapperGame;
        this.ppuStatusID = ppuStatusID;
    }

    public static VsGame getVsGame(final NesFile nesFile) {

        switch (nesFile.getFileCRC()) {
            case 0x2C1CC6DA: // NES 2.0
                return BalloonFight;
            case 0x38C1D042: // NES 2.0
            case 0x57685E6C: // NES 2.0
            case 0xA21330FE: // NES 2.0
            case 0xDAB703D7: // GoodNES Synthesized NES 2.0      
                return Baseball;
            case 0xBFD87C08:
            case 0xB2816BF9:
            case 0x01357944:
                return BattleCity;
            case 0xBAB3DDB9:
            case 0xFFBEF374:
                return Castlevania;
            case 0x0FA322C2:
                return CluCluLand;
            case 0x2B85420E:
                return DrMario;
            case 0xC1D6411F:
            case 0xABE1A0C2:
                return DuckHunt;
            case 0xC95321A8:
            case 0xF06FC822:
                return Excitebike;
            case 0xFB0DDDE7:
                return ExcitebikeJapan;
            case 0x832CF592:
                return FreedomForce;
            case 0x5E35AD1D:
            case 0x0D9EEFFE:
            case 0xF6DE2AA2:
            case 0x74F713B4:
                return Goonies;
            case 0x3FBEFD71:
            case 0xEE8AF512:
            case 0xF735D926:
                return Gradius;
            case 0x0056CC9F:
            case 0x07A2F3B2:
            case 0x412E5A0D:
            case 0x74C78E8C:
            case 0x7C7D861A:
            case 0xB194CA80:
                return Gumshoe;
            case 0x16AA4E2D:
            case 0xC31845AB:
                return HogansAlley;
            case 0xCBBEFD1F:
            case 0x32CD7DE2:
            case 0xEF7AF338:
                return IceClimber;
            case 0x1CA45A6D:
                return IceClimber_2;
            case 0x5FD6F603: // NES 2.0      
                return IceClimberDual;
            case 0xE98A8A4D:
            case 0xE8B20197:
                return MachRider;
            case 0xABE8E174:
                return MachRider_2;
            case 0x51C76943: // NES 2.0
            case 0x792CF17F: // NES 2.0 
            case 0xDEC03107: // GoodNES Synthesized NES 2.0
                return Mahjong;
            case 0x535E6152:
                return MightyBombJack;
            case 0xC20E9CA7:
                return NinjaJajamaruKun;
            case 0xCA3E9B1A:
                return Pinball;
            case 0x66471EFE:
                return PinballJapan;
            case 0x0A36E7CE:
            case 0x31678411:
            case 0x2019FE65:
                return Platoon;
            case 0x70FFB591: // NES 2.0
            case 0xF9DB47BD: // NES 2.0
                return RaidOnBungelingBay;
            case 0x252B5E51:
                return RbiBaseball;
            case 0xE45485A5: // NES 2.0
            case 0x8337E123: // NES 2.0
                return RbiBaseball_2;
            case 0x40EF00FE:
                return SkateKids;
            case 0x9AE2BAA0:
                return Slalom;
            case 0x766C2CAC:
                return Soccer;
            case 0xE26593E1:
                return Soccer_2;
            case 0x159EF3C1:
                return StarLuster;
            case 0x1DCE31E1:
            case 0x48361E79:
            case 0x63ABF889:
            case 0x889A02A9:
            case 0xB1C4C508:
            case 0xE85C74D8:
                return StrokeAndMatchGolf;
            case 0x9768E5E0:
                return StrokeAndMatchGolfJapan;
            case 0x7CFF0F84:
            case 0x1C7430F9:
            case 0x4B79ED83:
            case 0x4BF3972D:
            case 0x98E3C75A:
                return SuperMarioBros;
            case 0x21A653C7:
                return SuperSkyKid;
            case 0xDF07C203:
            case 0x89B56859:
            case 0x12012CD9:
                return SuperXevious;
            case 0x071CC00B: // NES 2.0
            case 0x67BAC152: // NES 2.0      
            case 0xCD059AEA: // NES 2.0
                return Tennis;
            case 0x6B6F2442:
            case 0xAECA9D42:
            case 0xC492B4D1:
            case 0xE9A6F17D:
                return Tetris;
            case 0x8C0C2DF5:
                return TopGun;
            case 0x52C501D0:
                return TkoBoxing;
            case 0x2BF21F5F: // NES 2.0
                return WreckingCrew;
        }

        final String fileName = nesFile.getFileName();
        if (fileName.contains("(vs)") || nesFile.isVsSystem()) {
            for (int i = 0; i < NameMappings.length; i++) {
                final Object[] mapping = NameMappings[i];
                if (fileName.contains((String) mapping[0])) {
                    final VsGame game = (VsGame) mapping[1];
                    switch (game) {
                        case Excitebike:
                            if (isJapan(fileName)) {
                                return ExcitebikeJapan;
                            }
                            break;
                        case MachRider:
                            if (isJapan(fileName)) {
                                return MachRiderJapan;
                            }
                            break;
                        case Pinball:
                            if (isJapan(fileName)) {
                                return PinballJapan;
                            }
                            break;
                        case StrokeAndMatchGolf:
                            if (isJapan(fileName)) {
                                return StrokeAndMatchGolfJapan;
                            }
                            break;
                    }
                    return game;
                }
            }
        }

        return null;
    }

    private static boolean isJapan(final String fileName) {
        return fileName.contains("(j)") || fileName.contains("(japan")
                || fileName.contains("(as)") || fileName.contains("(asia");
    }

    private void addAll(final List<DipSwitch> dipSwitches) {
        this.dipSwitches.addAll(dipSwitches);
    }

    private DipSwitch add(final String name) {
        return add(name, 0);
    }

    private DipSwitch add(final String name, final int defaultValue) {
        final DipSwitch dipSwitch = new DipSwitch(name, defaultValue);
        dipSwitches.add(dipSwitch);
        return dipSwitch;
    }

    public List<DipSwitch> getDipSwitches() {
        return dipSwitches;
    }

    public int getMapper() {
        return mapper;
    }

    public int getMirroring() {
        return mirroring;
    }

    public int getPPU() {
        return ppu;
    }

    public int getHardware() {
        return hardware;
    }

    public boolean isNonVolatilePrgRamPresent() {

        // VS. Wrecking Crew 
        //   - High score table and credits are saved
        //
        // VS. Balloon Fight / VS. Ice Climber (Japan) 
        //   - Coin slot distribution saved 
        //   - High score table is not saved
        //
        // VS. Baseball / VS. Tennis 
        //   - Provides battery tester 
        //   - Saves nothing (doesn't even show high score table)
        //
        // VS. Raid on Bungeling Bay / VS. Mahjong 
        //   - Saves nothing    

        return isDualSystemGame() && this != Mahjong && this != RaidOnBungelingBay;
    }

    public boolean isProtected() {
        return hardware == VS_UNISYSTEM_ICE_CLIMBER_JAPAN
                || hardware == VS_DUALSYSTEM_RAID_ON_BUNGELING_BAY;
    }

    public boolean isSwapControllers() {
        return swapControllers;
    }

    public boolean isZapperGame() {
        return zapperGame;
    }

    public int getPpuStatusID() {
        return ppuStatusID;
    }

    public boolean isUniSystemGame() {
        return hardware < VS_DUALSYSTEM_NORMAL;
    }

    public boolean isDualSystemGame() {
        return hardware >= VS_DUALSYSTEM_NORMAL;
    }
}