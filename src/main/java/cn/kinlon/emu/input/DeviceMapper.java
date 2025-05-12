package cn.kinlon.emu.input;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.arkanoid.ArkanoidMapper;
import cn.kinlon.emu.input.bandaihypershot.BandaiHyperShotMapper;
import cn.kinlon.emu.input.barcodebattler.BarcodeBattlerMapper;
import cn.kinlon.emu.input.battlebox.BattleBoxMapper;
import cn.kinlon.emu.input.dongda.DongdaPEC586KeyboardMapper;
import cn.kinlon.emu.input.doremikkokeyboard.DoremikkoKeyboardMapper;
import cn.kinlon.emu.input.excitingboxing.ExcitingBoxingMapper;
import cn.kinlon.emu.input.familybasic.datarecorder.DataRecorderMapper;
import cn.kinlon.emu.input.familybasic.keyboard.KeyboardMapper;
import cn.kinlon.emu.input.familybasic.transformer.TransformerMapper;
import cn.kinlon.emu.input.familytrainermat.FamilyTrainerMatMapper;
import cn.kinlon.emu.input.gamepad.GamepadMapper;
import cn.kinlon.emu.input.glasses.GlassesMapper;
import cn.kinlon.emu.input.horitrack.HoriTrackMapper;
import cn.kinlon.emu.input.konamihypershot.KonamiHyperShotMapper;
import cn.kinlon.emu.input.mahjong.MahjongMapper;
import cn.kinlon.emu.input.miraclepiano.MiraclePianoMapper;
import cn.kinlon.emu.input.multitap.Famicom4PlayersAdapterMapper;
import cn.kinlon.emu.input.multitap.NESFourScoreMapper;
import cn.kinlon.emu.input.none.NoneMapper;
import cn.kinlon.emu.input.oekakids.OekaKidsMapper;
import cn.kinlon.emu.input.pachinko.PachinkoMapper;
import cn.kinlon.emu.input.partytap.PartyTapMapper;
import cn.kinlon.emu.input.powerglove.PowerGloveMapper;
import cn.kinlon.emu.input.powerpad.PowerPadMapper;
import cn.kinlon.emu.input.racermate.RacerMateMapper;
import cn.kinlon.emu.input.snesmouse.SnesMouseMapper;
import cn.kinlon.emu.input.subor.SuborMapper;
import cn.kinlon.emu.input.taptapmat.TapTapMatMapper;
import cn.kinlon.emu.input.topriderbike.TopRiderBikeMapper;
import cn.kinlon.emu.input.turbofile.TurboFileMapper;
import cn.kinlon.emu.input.uforce.UForceMapper;
import cn.kinlon.emu.input.zapper.VsZapperMapper;
import cn.kinlon.emu.input.zapper.ZapperMapper;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.*;

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