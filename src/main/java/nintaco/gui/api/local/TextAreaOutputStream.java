package nintaco.gui.api.local;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

public class TextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;

    public TextAreaOutputStream(final JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(final int b) throws IOException {
        if (EventQueue.isDispatchThread()) {
            textArea.append(String.valueOf((char) b));
        } else {
            EventQueue.invokeLater(() -> {
                try {
                    write(b);
                } catch (final Throwable t) {
                }
            });
        }
    }
}
