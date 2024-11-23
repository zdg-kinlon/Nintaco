package nintaco.gui;

import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class MaxLengthDocument extends PlainDocument {

    private final int maxLength;

    public MaxLengthDocument(final int maxLength) {
        this(maxLength, null);
    }

    public MaxLengthDocument(final int maxLength, DocumentListener listener) {
        this.maxLength = maxLength;
        if (listener != null) {
            addDocumentListener(listener);
        }
    }

    @Override
    public void insertString(final int offset, final String str,
                             final AttributeSet attr) throws BadLocationException {

        if (str == null) {
            return;
        }

        if ((getLength() + str.length()) <= maxLength) {
            super.insertString(offset, str, attr);
        }
    }
}
