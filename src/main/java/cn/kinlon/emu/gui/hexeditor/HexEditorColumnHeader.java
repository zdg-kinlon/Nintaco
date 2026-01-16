package cn.kinlon.emu.gui.hexeditor;

import cn.kinlon.emu.gui.StyleListener;
import cn.kinlon.emu.utils.EDT;

import javax.swing.*;
import java.awt.*;

import static cn.kinlon.emu.gui.hexeditor.DataSource.*;
import static cn.kinlon.emu.utils.GuiUtil.getDefaultFont;
import static cn.kinlon.emu.utils.GuiUtil.scaleFont;

public class HexEditorColumnHeader extends JComponent implements StyleListener {

    private static final int MARGIN = 1;

    private Font font;
    private int charWidth = 1;
    private int charHeight = 1;
    private int charAscent;
    private FontMetrics metrics;
    private Dimension preferredSize = new Dimension(1, 1);
    private String dataSourceName;

    public HexEditorColumnHeader() {
        styleChanged();
        setDataSource(CpuMemory);
    }

    public final void setDataSource(final int dataSource) {
        EDT.async(() -> {
            switch (dataSource) {
                case CpuMemory:
                    dataSourceName = " CPU";
                    break;
                case PpuMemory:
                    dataSourceName = " PPU";
                    break;
                case FileContents:
                    dataSourceName = "File";
                    break;
            }
            repaint();
        });
    }

    @Override
    public final void styleChanged() {
        font = scaleFont(new Font(Font.MONOSPACED, Font.PLAIN,
                getDefaultFont(new JTextArea("M")).getSize()));
        setFont(font);
        metrics = null;
    }

    @Override
    protected void paintComponent(final Graphics g) {

        final JViewport viewport = (JViewport) getParent();
        final JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        if (metrics == null) {
            metrics = g.getFontMetrics(font);
            charWidth = metrics.getWidths()['M'];
            charHeight = metrics.getHeight();
            charAscent = metrics.getAscent();
            preferredSize = new Dimension((MARGIN << 1) + 76 * charWidth,
                    (MARGIN << 1) + charHeight);
            EDT.async(scrollPane::updateUI);
        }

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.BLUE);
        g.drawString(dataSourceName, MARGIN + 3 * charWidth, MARGIN + charAscent);
        g.drawString("00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F    "
                + "0123456789ABCDEF", MARGIN + 9 * charWidth, MARGIN + charAscent);
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }
}
