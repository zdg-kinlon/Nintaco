package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.OtherInput;
import nintaco.input.familybasic.datarecorder.DataRecorderMapper;
import nintaco.preferences.AppPrefs;

public class SetDataRecorderMode implements OtherInput {

    private static final long serialVersionUID = 0;

    private final int dataRecorderMode;
    private final int samplingPeriod;

    public SetDataRecorderMode(final int dataRecorderMode) {
        this.dataRecorderMode = dataRecorderMode;
        this.samplingPeriod = AppPrefs.getInstance().getFamilyBasicPrefs()
                .getDataRecorderSamplingPeriod();
    }

    @Override
    public void run(final Machine machine) {
        final DataRecorderMapper recorder = machine.getMapper().getDataRecorder();
        if (recorder != null) {
            recorder.setDataRecorderMode(dataRecorderMode, samplingPeriod);
        }
    }
}
