package nintaco.input.familybasic.transformer;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.TransformerKeyboard;

public class TransformerConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TransformerConfig() {
        super(TransformerKeyboard);
    }

    public TransformerConfig(final List<ButtonMapping> buttonMappings) {
        super(TransformerKeyboard, buttonMappings);
    }

    public TransformerConfig(final TransformerConfig transformerConfig) {
        super(transformerConfig);
    }

    @Override
    public TransformerConfig copy() {
        return new TransformerConfig(this);
    }
}
