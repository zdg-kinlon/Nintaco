package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.InputUtil;
import nintaco.input.OtherInput;

public class TransferBarcode implements OtherInput {

    private static final long serialVersionUID = 0;

    private final String barcode;

    public TransferBarcode(final String barcode) {
        this.barcode = barcode;
    }

    @Override
    public void run(final Machine machine) {
        if (machine != null) {
            InputUtil.setBarcode(barcode);
        }
    }
}
