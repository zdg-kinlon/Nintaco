package nintaco.input.other;

import nintaco.App;
import nintaco.Machine;
import nintaco.MessageException;
import nintaco.input.OtherInput;
import nintaco.input.familybasic.FamilyBasicUtil;

import static nintaco.util.GuiUtil.displayError;

public class PasteProgram implements OtherInput {

    private static final long serialVersionUID = 0;

    private final String program;

    public PasteProgram(final String program) {
        this.program = program;
    }

    @Override
    public void run(final Machine machine) {
        App.setNoStepPause(true);
        try {
            FamilyBasicUtil.pasteProgram(program);
        } catch (final MessageException e) {
            displayError(App.getImageFrame(), e.getMessage());
        } finally {
            App.setNoStepPause(false);
        }
    }
}
