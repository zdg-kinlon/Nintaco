package nintaco.gui.archive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;

import static java.awt.RenderingHints.*;
import static nintaco.util.StringUtil.isBlank;

public class SearchTextField extends JTextField implements FocusListener {

    public SearchTextField() {
        addFocusListener(this);
    }

    @Override
    protected void paintComponent(final Graphics G) {
        super.paintComponent(G);

        if (!hasFocus() && isBlank(getText())) {
            final Graphics2D g = (Graphics2D) G;
            final Color color = g.getColor();
            final Font font = g.getFont();
            final RenderingHints hints = g.getRenderingHints();
            final AffineTransform transform = g.getTransform();

            g.setFont(getFont());
            g.setColor(UIManager.getColor("textInactiveText"));
            g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
            final Insets insets = getInsets();
            final int ascent = g.getFontMetrics().getAscent();
            final double scale = (getHeight() - 1.25 * (insets.top + insets.bottom))
                    / 13.0;
            final int y = insets.top + (getHeight() - insets.top - insets.bottom
                    - g.getFontMetrics().getHeight()) / 2;
            g.drawString("Search", insets.left + (int) (19 * scale), y + ascent);

            g.translate(1.25 * insets.left, 1.25 * insets.top);
            g.scale(scale, scale);
            g.setStroke(new BasicStroke(2));
            g.drawOval(0, 0, 10, 10);
            g.drawLine(9, 9, 13, 13);

            g.setTransform(transform);
            g.setFont(font);
            g.setRenderingHints(hints);
            g.setColor(color);
        }
    }

    @Override
    public void focusGained(final FocusEvent e) {
        repaint();
    }

    @Override
    public void focusLost(final FocusEvent e) {
        repaint();
    }
}
