package nintaco.input;

import nintaco.App;
import nintaco.Machine;
import nintaco.input.arkanoid.ArkanoidMapper;
import nintaco.input.bandaihypershot.BandaiHyperShotMapper;
import nintaco.input.barcodebattler.BarcodeBattlerMapper;
import nintaco.input.battlebox.BattleBoxMapper;
import nintaco.input.dongda.DongdaPEC586KeyboardMapper;
import nintaco.input.doremikkokeyboard.DoremikkoKeyboardMapper;
import nintaco.input.excitingboxing.ExcitingBoxingMapper;
import nintaco.input.familybasic.datarecorder.DataRecorderMapper;
import nintaco.input.familybasic.keyboard.KeyboardMapper;
import nintaco.input.familybasic.transformer.TransformerMapper;
import nintaco.input.familytrainermat.FamilyTrainerMatMapper;
import nintaco.input.gamepad.GamepadMapper;
import nintaco.input.glasses.GlassesMapper;
import nintaco.input.horitrack.HoriTrackMapper;
import nintaco.input.konamihypershot.KonamiHyperShotMapper;
import nintaco.input.mahjong.MahjongMapper;
import nintaco.input.miraclepiano.MiraclePianoMapper;
import nintaco.input.multitap.Famicom4PlayersAdapterMapper;
import nintaco.input.multitap.NESFourScoreMapper;
import nintaco.input.none.NoneMapper;
import nintaco.input.oekakids.OekaKidsMapper;
import nintaco.input.pachinko.PachinkoMapper;
import nintaco.input.partytap.PartyTapMapper;
import nintaco.input.powerglove.PowerGloveMapper;
import nintaco.input.powerpad.PowerPadMapper;
import nintaco.input.racermate.RacerMateMapper;
import nintaco.input.snesmouse.SnesMouseMapper;
import nintaco.input.subor.SuborMapper;
import nintaco.input.taptapmat.TapTapMatMapper;
import nintaco.input.topriderbike.TopRiderBikeMapper;
import nintaco.input.turbofile.TurboFileMapper;
import nintaco.input.uforce.UForceMapper;
import nintaco.input.zapper.VsZapperMapper;
import nintaco.input.zapper.ZapperMapper;

import java.io.Serializable;

import static nintaco.input.InputDevices.*;

public abstract class DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    static DeviceMapper createDeviceMapper(final int port,
                                           final int inputDevice) {

        switch (inputDevice) {
            case Gamepad1:
            case Gamepad2:
            case Gamepad3:
            case Gamepad4:
            case CrazyClimberLeft:
            case CrazyClimberRight:
                return new GamepadMapper(port);
            case Zapper: {
                if (App.isVsUniSystem()) {
                    return new VsZapperMapper(port);
                } else {
                    return new ZapperMapper(port);
                }
            }
            case Arkanoid:
                return new ArkanoidMapper(port);
            case BandaiHyperShot:
                return new BandaiHyperShotMapper();
            case BarcodeBattler:
                return new BarcodeBattlerMapper();
            case BattleBox:
                return new BattleBoxMapper();
            case DataRecorder:
                return new DataRecorderMapper();
            case DongdaPEC586Keyboard:
                return new DongdaPEC586KeyboardMapper();
            case DoremikkoKeyboard:
                return new DoremikkoKeyboardMapper();
            case ExcitingBoxing:
                return new ExcitingBoxingMapper();
            case FamilyTrainerMat:
                return new FamilyTrainerMatMapper();
            case Glasses:
                return new GlassesMapper();
            case Keyboard:
                return new KeyboardMapper();
            case KonamiHyperShot:
                return new KonamiHyperShotMapper();
            case HoriTrack:
                return new HoriTrackMapper();
            case Mahjong:
                return new MahjongMapper();
            case MiraclePiano:
                return new MiraclePianoMapper();
            case OekaKids:
                return new OekaKidsMapper();
            case Pachinko:
                return new PachinkoMapper();
            case PartyTap:
                return new PartyTapMapper();
            case PowerGlove:
                return new PowerGloveMapper(port);
            case PowerPad:
                return new PowerPadMapper(port);
            case RacerMate1:
            case RacerMate2:
                return new RacerMateMapper(port);
            case SnesMouse:
                return new SnesMouseMapper(port);
            case Subor:
                return new SuborMapper();
            case TapTapMat:
                return new TapTapMatMapper();
            case TopRiderBike:
                return new TopRiderBikeMapper();
            case TransformerKeyboard:
                return new TransformerMapper();
            case TurboFile:
                return new TurboFileMapper();
            case UForce:
                return new UForceMapper();
            case NESFourScore1:
            case NESFourScore2:
                return new NESFourScoreMapper(port);
            case Famicom4PlayersAdapter1:
            case Famicom4PlayersAdapter2:
                return new Famicom4PlayersAdapterMapper(port);
            default:
                return new NoneMapper();
        }
    }

    public abstract int getInputDevice();

    public abstract void update(final int buttons);

    public abstract void writePort(final int value);

    public abstract int readPort(final int portIndex);

    public abstract int peekPort(final int portIndex);

    public void setMachine(final Machine machine) {
    }

    public void render(final int[] screen) {
    }

    public void close(final boolean saveNonVolatileData) {
    }
}