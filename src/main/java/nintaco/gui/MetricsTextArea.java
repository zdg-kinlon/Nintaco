package nintaco.gui;

import javax.swing.*;
import java.awt.*;

import static nintaco.util.MathUtil.roundUpDivision;

public class MetricsTextArea extends JTextArea {

    private int visibleColumns;
    private int visibleLines;

    public int getVisibleColumns() {
        return visibleColumns;
    }

    public int getVisibleLines() {
        return visibleLines;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final FontMetrics fontMetrics = g.getFontMetrics();
        final int charWidth = fontMetrics.charWidth('M');
        final int charHeight = fontMetrics.getHeight();
        final int prefWidth = getPreferredSize().width;
        final int prefHeight = getParent().getHeight();
        visibleColumns = roundUpDivision(prefWidth, charWidth);
        visibleLines = roundUpDivision(prefHeight, charHeight);
    }
}
