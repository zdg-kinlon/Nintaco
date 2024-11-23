package nintaco.input;

import nintaco.input.arkanoid.ArkanoidConfig;
import nintaco.input.bandaihypershot.BandaiHyperShotConfig;
import nintaco.input.barcodebattler.BarcodeBattlerConfig;
import nintaco.input.battlebox.BattleBoxConfig;
import nintaco.input.crazyclimber.CrazyClimberLeftConfig;
import nintaco.input.crazyclimber.CrazyClimberRightConfig;
import nintaco.input.dongda.DongdaPEC586KeyboardConfig;
import nintaco.input.doremikkokeyboard.DoremikkoKeyboardConfig;
import nintaco.input.excitingboxing.ExcitingBoxingConfig;
import nintaco.input.familybasic.datarecorder.DataRecorderConfig;
import nintaco.input.familybasic.keyboard.KeyboardConfig;
import nintaco.input.familybasic.transformer.TransformerConfig;
import nintaco.input.familytrainermat.FamilyTrainerMatConfig;
import nintaco.input.gamepad.Gamepad1Config;
import nintaco.input.gamepad.Gamepad2Config;
import nintaco.input.gamepad.Gamepad3Config;
import nintaco.input.gamepad.Gamepad4Config;
import nintaco.input.glasses.GlassesConfig;
import nintaco.input.horitrack.HoriTrackConfig;
import nintaco.input.konamihypershot.KonamiHyperShotConfig;
import nintaco.input.mahjong.MahjongConfig;
import nintaco.input.miraclepiano.MiraclePianoConfig;
import nintaco.input.oekakids.OekaKidsConfig;
import nintaco.input.pachinko.PachinkoConfig;
import nintaco.input.partytap.PartyTapConfig;
import nintaco.input.powerglove.PowerGloveConfig;
import nintaco.input.powerpad.PowerPadConfig;
import nintaco.input.racermate.RacerMate1Config;
import nintaco.input.racermate.RacerMate2Config;
import nintaco.input.snesmouse.SnesMouseConfig;
import nintaco.input.subor.SuborConfig;
import nintaco.input.taptapmat.TapTapMatConfig;
import nintaco.input.topriderbike.TopRiderBikeConfig;
import nintaco.input.turbofile.TurboFileConfig;
import nintaco.input.uforce.UForceConfig;
import nintaco.input.zapper.ZapperConfig;

public interface InputDevices {

    int None = -1;
    int NESFourScore1 = -2;
    int NESFourScore2 = -3;
    int Famicom4PlayersAdapter1 = -4;
    int Famicom4PlayersAdapter2 = -5;

    int Gamepad1 = 0;
    int Gamepad2 = 1;
    int Gamepad3 = 2;
    int Gamepad4 = 3;
    int Zapper = 4;
    int Arkanoid = 5;
    int BandaiHyperShot = 6;
    int BarcodeBattler = 7;
    int BattleBox = 8;
    int CrazyClimberLeft = 9;
    int CrazyClimberRight = 10;
    int DataRecorder = 11;
    int DongdaPEC586Keyboard = 12;
    int DoremikkoKeyboard = 13;
    int ExcitingBoxing = 14;
    int FamilyTrainerMat = 15;
    int Glasses = 16;
    int HoriTrack = 17;
    int Keyboard = 18;
    int KonamiHyperShot = 19;
    int Mahjong = 20;
    int MiraclePiano = 21;
    int OekaKids = 22;
    int Pachinko = 23;
    int PartyTap = 24;
    int PowerGlove = 25;
    int PowerPad = 26;
    int RacerMate1 = 27;
    int RacerMate2 = 28;
    int SnesMouse = 29;
    int Subor = 30;
    int TapTapMat = 31;
    int TopRiderBike = 32;
    int TransformerKeyboard = 33;
    int TurboFile = 34;
    int UForce = 35;

    int DevicesCount = 36;

    static boolean isGamepad(final int inputDevice) {
        return inputDevice == Gamepad1 || inputDevice == Gamepad2
                || inputDevice == Gamepad3 || inputDevice == Gamepad4;
    }

    static DeviceConfig newDeviceConfig(final int inputDevice) {
        switch (inputDevice) {
            case Gamepad1:
                return new Gamepad1Config();
            case Gamepad2:
                return new Gamepad2Config();
            case Gamepad3:
                return new Gamepad3Config();
            case Gamepad4:
                return new Gamepad4Config();
            case Zapper:
                return new ZapperConfig();
            case Arkanoid:
                return new ArkanoidConfig();
            case BandaiHyperShot:
                return new BandaiHyperShotConfig();
            case BarcodeBattler:
                return new BarcodeBattlerConfig();
            case BattleBox:
                return new BattleBoxConfig();
            case CrazyClimberLeft:
                return new CrazyClimberLeftConfig();
            case CrazyClimberRight:
                return new CrazyClimberRightConfig();
            case DataRecorder:
                return new DataRecorderConfig();
            case DongdaPEC586Keyboard:
                return new DongdaPEC586KeyboardConfig();
            case DoremikkoKeyboard:
                return new DoremikkoKeyboardConfig();
            case ExcitingBoxing:
                return new ExcitingBoxingConfig();
            case FamilyTrainerMat:
                return new FamilyTrainerMatConfig();
            case Glasses:
                return new GlassesConfig();
            case HoriTrack:
                return new HoriTrackConfig();
            case Keyboard:
                return new KeyboardConfig();
            case KonamiHyperShot:
                return new KonamiHyperShotConfig();
            case Mahjong:
                return new MahjongConfig();
            case MiraclePiano:
                return new MiraclePianoConfig();
            case OekaKids:
                return new OekaKidsConfig();
            case Pachinko:
                return new PachinkoConfig();
            case PartyTap:
                return new PartyTapConfig();
            case PowerGlove:
                return new PowerGloveConfig();
            case PowerPad:
                return new PowerPadConfig();
            case RacerMate1:
                return new RacerMate1Config();
            case RacerMate2:
                return new RacerMate2Config();
            case Subor:
                return new SuborConfig();
            case SnesMouse:
                return new SnesMouseConfig();
            case TapTapMat:
                return new TapTapMatConfig();
            case TopRiderBike:
                return new TopRiderBikeConfig();
            case TransformerKeyboard:
                return new TransformerConfig();
            case TurboFile:
                return new TurboFileConfig();
            case UForce:
                return new UForceConfig();
            default:
                return null;
        }
    }
}