package nintaco.gui.debugger;

import nintaco.*;
import nintaco.disassembler.AddressLabel;
import nintaco.disassembler.AddressTextRange;
import nintaco.disassembler.Disassembler;
import nintaco.disassembler.Instruction;
import nintaco.gui.MetricsTextArea;
import nintaco.gui.asmdasm.AsmDasmFrame;
import nintaco.gui.debugger.addresslabel.AddressLabelDialog;
import nintaco.gui.debugger.breakpoint.BreakpointDialog;
import nintaco.gui.debugger.logger.LoggerDialog;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.QuickSaveListener;
import nintaco.gui.image.QuickSaveStateInfo;
import nintaco.mappers.Mapper;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nintaco.PPU.*;
import static nintaco.PauseStepType.*;
import static nintaco.gui.hexeditor.DataSource.CpuMemory;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.ParseErrors.*;
import static nintaco.util.StringUtil.isBlank;
import static nintaco.util.StringUtil.parseInt;

public class DebuggerFrame extends javax.swing.JFrame {

    private final AdjustmentListener adjustmentListener;
    private final ImageFrame imageFrame;
    private volatile MachineRunner machineRunner;
    private volatile CPU cpu;
    private volatile PPU ppu;
    private volatile Mapper mapper;
    private volatile boolean textAreaRefreshRequested;
    private volatile List<Instruction> instructions = new ArrayList<>();
    private volatile int pcValue;
    private volatile int aValue;
    private volatile int xValue;
    private volatile int yValue;
    private volatile int sValue;
    private volatile int ppuVValue;
    private volatile int ppuTValue;
    private volatile int ppuXValue;
    private volatile int cameraXValue;
    private volatile int cameraYValue;
    private volatile boolean nValue;
    private volatile boolean vValue;
    private volatile boolean dValue;
    private volatile boolean iValue;
    private volatile boolean zValue;
    private volatile boolean cValue;
    private volatile boolean ppuWValue;
    private volatile boolean sprite0Value;
    private volatile boolean valuesAcquired;
    private volatile boolean pcModified;
    private volatile boolean aModified;
    private volatile boolean xModified;
    private volatile boolean yModified;
    private volatile boolean sModified;
    private volatile boolean nModified;
    private volatile boolean vModified;
    private volatile boolean dModified;
    private volatile boolean iModified;
    private volatile boolean zModified;
    private volatile boolean cModified;
    private volatile boolean ppuVModified;
    private volatile boolean ppuTModified;
    private volatile boolean ppuXModified;
    private volatile boolean ppuWModified;
    private volatile boolean cameraXModified;
    private volatile boolean cameraYModified;
    private volatile boolean sprite0Modified;
    private volatile boolean officialsOnly = true;
    private int visibleLines = -1;
    private int scrollValue;
    private AddressTextRange lastRange;
    private List<QuickSaveStateInfo> quickSaveStateInfos;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aLabel;
    private javax.swing.JTextField aTextField;
    private javax.swing.JMenuItem addBreakpointMenuItem;
    private javax.swing.JMenuItem addressLabelMenuItem;
    private javax.swing.JComboBox addressTypeComboBox;
    private javax.swing.JButton assemblerButton;
    private javax.swing.JComboBox bookmarksComboBox;
    private javax.swing.JLabel bookmarksLabel;
    private javax.swing.JComboBox branchesComboBox;
    private javax.swing.JButton breakpointsButton;
    private javax.swing.JButton brkButton;
    private javax.swing.JCheckBox cCheckBox;
    private javax.swing.JLabel cameraXNameLabel;
    private javax.swing.JTextField cameraXTextField;
    private javax.swing.JLabel cameraYNameLabel;
    private javax.swing.JTextField cameraYTextField;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton configLoggerButton;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JCheckBox dCheckBox;
    private javax.swing.JPopupMenu debugPopupMenu;
    private javax.swing.JScrollPane debugScrollPane;
    private javax.swing.JTextArea debugTextArea;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JButton dotButton;
    private javax.swing.JLabel dotLabel;
    private javax.swing.JLabel dotNameLabel;
    private javax.swing.JTextField dotTextField;
    private javax.swing.JMenuItem hexEditorMenuItem;
    private javax.swing.JCheckBox iCheckBox;
    private javax.swing.JCheckBox inspectionsCheckBox;
    private javax.swing.JButton instructionsButton;
    private javax.swing.JTextField instructionsTextField;
    private javax.swing.JButton irqButton;
    private javax.swing.JCheckBox labelsCheckBox;
    private javax.swing.JButton loadButton;
    private javax.swing.JToggleButton loggerToggleButton;
    private javax.swing.JCheckBox machineCodeCheckBox;
    private javax.swing.JButton manageAddressLabelsButton;
    private javax.swing.JCheckBox nCheckBox;
    private javax.swing.JButton nextFrameButton;
    private javax.swing.JButton nmiButton;
    private javax.swing.JButton opcodeButton;
    private javax.swing.JTextField opcodeTextField;
    private javax.swing.JCheckBox pcCheckBox;
    private javax.swing.JLabel pcLabel;
    private javax.swing.JTextField pcTextField;
    private javax.swing.JLabel ppu2002Label;
    private javax.swing.JLabel ppu2002NameLabel;
    private javax.swing.JLabel ppu2004Label;
    private javax.swing.JLabel ppu2004NameLabel;
    private javax.swing.JLabel ppu2007Label;
    private javax.swing.JLabel ppu2007NameLabel;
    private javax.swing.JLabel ppuTNameLabel;
    private javax.swing.JTextField ppuTTextField;
    private javax.swing.JLabel ppuVNameLabel;
    private javax.swing.JTextField ppuVTextField;
    private javax.swing.JCheckBox ppuWCheckBox;
    private javax.swing.JLabel ppuXNameLabel;
    private javax.swing.JTextField ppuXTextField;
    private javax.swing.JButton rstButton;
    private javax.swing.JLabel sLabel;
    private javax.swing.JTextField sTextField;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox saveSlotComboBox;
    private final QuickSaveListener quickSaveListener = this::onQuickSaveChanged;
    private javax.swing.JLabel saveSlotLabel;
    private javax.swing.JButton scanlineButton;
    private javax.swing.JLabel scanlineLabel;
    private javax.swing.JLabel scanlineNameLabel;
    private javax.swing.JTextField scanlineTextField;
    private javax.swing.JScrollBar scrollBar;
    private javax.swing.JButton seekPcButton;
    private javax.swing.JButton seekToButton;
    private javax.swing.JMenuItem seekToMenuItem;
    private javax.swing.JTextField seekToTextField;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JPopupMenu.Separator separator1;
    private javax.swing.JCheckBox sprite0CheckBox;
    private javax.swing.JButton sprite0HitButton;
    private javax.swing.JScrollPane stackScrollPane;
    private javax.swing.JTextArea stackTextArea;
    private javax.swing.JButton stepIntoButton;
    private javax.swing.JButton stepOutButton;
    private javax.swing.JButton stepOverButton;
    private javax.swing.JButton stepToButton;
    private javax.swing.JMenuItem stepToMenuItem;
    private javax.swing.JTextField stepToTextField;
    private javax.swing.JToggleButton suspendToggleButton;
    private javax.swing.JCheckBox unofficialOpcodesCheckBox;
    private javax.swing.JCheckBox vCheckBox;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField xTextField;
    private javax.swing.JLabel yLabel;
    private javax.swing.JTextField yTextField;
    private javax.swing.JCheckBox zCheckBox;

    public DebuggerFrame(final MachineRunner machineRunner) {
        initComponents();
        initTextComponents();
        imageFrame = App.getImageFrame();
        imageFrame.addQuickSaveListener(quickSaveListener);
        adjustmentListener = e -> onScrollBarAdjusted();
        scrollBar.addAdjustmentListener(adjustmentListener);
        loadFields();
        setMachineRunner(machineRunner);
        scaleFonts(this);
        setTextAreaSize(debugTextArea, 50, 72);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initTextComponents() {
        debugTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        makeMonospaced(stackTextArea);
        makeMonospaced(debugTextArea);
        makeMonospaced(scanlineLabel);
        makeMonospaced(dotLabel);
        makeMonospaced(ppu2002Label);
        makeMonospaced(ppu2004Label);
        makeMonospaced(ppu2007Label);

        addLoseFocusListener(this, seekToTextField);
        addLoseFocusListener(this, stepToTextField);
        addLoseFocusListener(this, scanlineTextField);
        addLoseFocusListener(this, dotTextField);
        addLoseFocusListener(this, opcodeTextField);
        addLoseFocusListener(this, instructionsTextField);
        addLoseFocusListener(this, pcTextField);
        addLoseFocusListener(this, aTextField);
        addLoseFocusListener(this, xTextField);
        addLoseFocusListener(this, yTextField);
        addLoseFocusListener(this, sTextField);
        addLoseFocusListener(this, ppuVTextField);
        addLoseFocusListener(this, ppuTTextField);
        addLoseFocusListener(this, ppuXTextField);
        addLoseFocusListener(this, cameraXTextField);
        addLoseFocusListener(this, cameraYTextField);
    }

    public void refreshAddressLabels() {
        initAddressLabels();
        requestTextAreaRefresh();
    }

    private void initAddressLabels() {
        final Map<Integer, AddressLabel> codeLabels = new HashMap<>();
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        synchronized (GamePrefs.class) {
            for (final AddressLabel label : GamePrefs.getInstance()
                    .getDebuggerGamePrefs().getAddressLabels()) {
                if (label.isCode()) {
                    codeLabels.put(label.getKey(), label);
                }
                if (label.isBookmark()) {
                    model.addElement(label);
                }
            }
        }
        Disassembler.setLabels(codeLabels);
        bookmarksComboBox.setModel(model);
        updateBookmarksComboBox();
    }

    private void updateBookmarksComboBox() {
        final boolean enabled = machineRunner != null
                && bookmarksComboBox.getModel().getSize() > 0;
        bookmarksComboBox.setEnabled(enabled);
        bookmarksLabel.setEnabled(enabled);
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getDebuggerAppPrefs());
    }

    private void loadFields(final DebuggerAppPrefs prefs) {
        addressTypeComboBox.setSelectedIndex(prefs.getAddressType());
        branchesComboBox.setSelectedIndex(prefs.getBranchesType());
        pcCheckBox.setSelected(prefs.isShowPC());
        inspectionsCheckBox.setSelected(prefs.isShowInspections());
        labelsCheckBox.setSelected(prefs.isShowAddressLabels());
        machineCodeCheckBox.setSelected(prefs.isShowMachineCode());
        unofficialOpcodesCheckBox.setSelected(prefs.isShowUnofficialOpcodes());
    }

    private void saveFields() {
        final DebuggerAppPrefs prefs = AppPrefs.getInstance().getDebuggerAppPrefs();
        prefs.setAddressType(addressTypeComboBox.getSelectedIndex());
        prefs.setBranchesType(branchesComboBox.getSelectedIndex());
        prefs.setShowPC(pcCheckBox.isSelected());
        prefs.setShowInspections(inspectionsCheckBox.isSelected());
        prefs.setShowAddressLabels(labelsCheckBox.isSelected());
        prefs.setShowMachineCode(machineCodeCheckBox.isSelected());
        prefs.setShowUnofficialOpcodes(unofficialOpcodesCheckBox.isSelected());
        AppPrefs.save();
    }

    public void destroy() {
        App.disposeTraceLogger();
        imageFrame.removeQuickSaveListener(quickSaveListener);
        saveFields();
        dispose();
        App.setBreakpoints(null);
        App.setStepPause(false);
    }

    private void closeFrame() {
        App.destroyDebuggerFrame();
    }

    public void update() {
        if (textAreaRefreshRequested) {
            textAreaRefreshRequested = false;
            refreshDebugTextArea(false);
        }
    }

    public final void setMachineRunner(final MachineRunner machineRunner) {
        App.disposeTraceLogger();
        this.machineRunner = machineRunner;
        if (machineRunner == null) {
            this.cpu = null;
            this.ppu = null;
            this.mapper = null;
        } else {
            this.cpu = machineRunner.getCPU();
            this.ppu = machineRunner.getPPU();
            this.mapper = machineRunner.getMapper();
            applyBreakpoints();
            requestTextAreaRefresh();
        }
        initAddressLabels();
        EventQueue.invokeLater(this::enableComponents);
    }

    private void updateVisibleLines() {
        final int lines = ((MetricsTextArea) debugTextArea).getVisibleLines();
        if (lines != visibleLines) {
            visibleLines = lines;
            requestTextAreaRefresh();
        }
    }

    private void updateFields(final int address, final int nextScanline,
                              final int nextScanlineCycle, final int scanline,
                              final int scanlineCycle, final int ppu2002, final int ppu2004,
                              final int ppu2007) {
        if (EventQueue.isDispatchThread()) {
            stepToTextField.setText(String.format("%04X", address));
            scanlineTextField.setText(Integer.toString(nextScanline));
            dotTextField.setText(Integer.toString(nextScanlineCycle));
            scanlineLabel.setText(String.format("%3d", scanline));
            dotLabel.setText(String.format("%3d", scanlineCycle));
            ppu2002Label.setText(String.format("%02X", ppu2002));
            ppu2004Label.setText(String.format("%02X", ppu2004));
            ppu2007Label.setText(String.format("%02X", ppu2007));
        } else {
            EventQueue.invokeLater(() -> updateFields(address, nextScanline,
                    nextScanlineCycle, scanline, scanlineCycle, ppu2002, ppu2004,
                    ppu2007));
        }
    }

    private void updateStackTextArea() {
        final CPU c = cpu;
        if (c == null) {
            return;
        }
        final Mapper m = mapper;
        if (m == null) {
            return;
        }
        final StringBuilder sb = new StringBuilder();
        for (int s = c.getS(); s <= 0xFF; s++) {
            sb.append(String.format("%02X ", m.peekCpuMemory(0x0100 | s)));
        }
        EventQueue.invokeLater(() -> stackTextArea.setText(sb.toString()));
    }

    private void acquireFields() {

        acquirePC(false);
        acquireA(false);
        acquireX(false);
        acquireY(false);
        acquireS(false);

        final boolean n = nCheckBox.isSelected();
        nModified = valuesAcquired && nValue != n;
        if (nModified) {
            nValue = n;
        }
        final boolean v = vCheckBox.isSelected();
        vModified = valuesAcquired && vValue != v;
        if (vModified) {
            vValue = v;
        }
        final boolean d = dCheckBox.isSelected();
        dModified = valuesAcquired && dValue != d;
        if (dModified) {
            dValue = d;
        }
        final boolean i = iCheckBox.isSelected();
        iModified = valuesAcquired && iValue != i;
        if (iModified) {
            iValue = i;
        }
        final boolean z = zCheckBox.isSelected();
        zModified = valuesAcquired && zValue != z;
        if (zModified) {
            zValue = z;
        }
        final boolean c = cCheckBox.isSelected();
        cModified = valuesAcquired && cValue != c;
        if (cModified) {
            cValue = c;
        }
    }

    private void assignFields() {
        if (pcValue >= 0x0000 && pcValue <= 0xFFFF) {
            pcTextField.setText(String.format("%04X", pcValue));
        }
        if (aValue >= 0x00 && aValue <= 0xFF) {
            aTextField.setText(String.format("%02X", aValue));
        }
        if (xValue >= 0x00 && xValue <= 0xFF) {
            xTextField.setText(String.format("%02X", xValue));
        }
        if (yValue >= 0x00 && yValue <= 0xFF) {
            yTextField.setText(String.format("%02X", yValue));
        }
        if (sValue >= 0x00 && sValue <= 0xFF) {
            sTextField.setText(String.format("%02X", sValue));
        }
        if (ppuVValue >= 0x00 && ppuVValue <= 0x7FFF) {
            ppuVTextField.setText(String.format("%04X", ppuVValue));
        }
        if (ppuTValue >= 0x00 && ppuTValue <= 0x7FFF) {
            ppuTTextField.setText(String.format("%04X", ppuTValue));
        }
        if (ppuXValue >= 0x00 && ppuXValue <= 0x07) {
            ppuXTextField.setText(String.format("%X", ppuXValue));
        }
        if (cameraXValue >= 0x00 && cameraXValue <= 0x1FF) {
            cameraXTextField.setText(Integer.toString(cameraXValue));
        }
        if (cameraYValue >= 0x00 && cameraYValue <= 0x1FF) {
            cameraYTextField.setText(Integer.toString(cameraYValue));
        }

        nCheckBox.setSelected(nValue);
        vCheckBox.setSelected(vValue);
        dCheckBox.setSelected(dValue);
        iCheckBox.setSelected(iValue);
        zCheckBox.setSelected(zValue);
        cCheckBox.setSelected(cValue);
        ppuWCheckBox.setSelected(ppuWValue);
        sprite0CheckBox.setSelected(sprite0Value);
    }

    private void requestTextAreaRefresh() {
        lastRange = null;
        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            refreshDebugTextArea(false);
        } else {
            textAreaRefreshRequested = true;
        }
    }

    private void refreshDebugTextArea(final boolean scrollToPC) {
        final CPU c = cpu;
        if (c == null) {
            return;
        }
        final Mapper m = mapper;
        if (m == null) {
            return;
        }

        final int PC = c.getPC();
        int pcLine = -1;
        instructions = Disassembler.disassemble(c, scrollValue, 0, visibleLines,
                officialsOnly);
        final StringBuilder sb = new StringBuilder();
        int line = 0;
        int indexOfLastVisibleInstruction = 0;
        if (!instructions.isEmpty()) {
            Instruction inst = instructions.get(0);
            inst.setStart(0);
            inst.setEnd(inst.getDescription().length());
            line += inst.getDescriptionLines();
            inst.setLine(line - 1);
            sb.append(inst.getDescription());
            if (inst.getAddress() == PC) {
                pcLine = inst.getLine();
            }
            for (int i = 1; i < instructions.size(); i++) {
                sb.append('\n');
                inst = instructions.get(i);
                inst.setStart(sb.length());
                inst.setEnd(sb.length() + inst.getDescription().length());
                line += inst.getDescriptionLines();
                inst.setLine(line - 1);
                if (inst.getLine() < visibleLines) {
                    indexOfLastVisibleInstruction = i;
                }
                if (inst.getAddress() == PC) {
                    pcLine = inst.getLine();
                }
                final AddressTextRange[] ranges = inst.getRanges();
                for (int j = 0; j < ranges.length; j++) {
                    final AddressTextRange range = ranges[j];
                    if (range == null) {
                        break;
                    } else {
                        range.setStart(range.getStart() + sb.length());
                        range.setEnd(range.getEnd() + sb.length());
                    }
                }
                sb.append(inst.getDescription());
            }
        }

        if (scrollToPC) {
            if (pcLine >= indexOfLastVisibleInstruction - 1) {
                if (pcLine == indexOfLastVisibleInstruction) {
                    EventQueue.invokeLater(this::onScrollDown);
                }
                EventQueue.invokeLater(this::onScrollDown);
            } else if (pcLine < 0) {
                scrollValue = Disassembler.getPriorAddress(m, PC, officialsOnly);
                EventQueue.invokeLater(() -> setScrollBarValue(scrollValue));
                refreshDebugTextArea(false);
            } else {
                updateDebugTextArea(sb.toString());
            }
        } else {
            updateDebugTextArea(sb.toString());
        }
    }

    private void updateDebugTextArea(final String text) {
        if (EventQueue.isDispatchThread()) {
            lastRange = null;
            debugTextArea.setText(text);
            debugTextArea.setCaretPosition(0);
        } else {
            EventQueue.invokeLater(() -> updateDebugTextArea(text));
        }
    }

    private void onScrollBarAdjusted() {
        final int value = scrollBar.getValue();
        if (value == scrollValue - 1) {
            onScrollUp();
        } else if (value == scrollValue + 1) {
            onScrollDown();
        } else {
            onScroll(value);
        }
    }

    private void onScrollUp() {
        final Mapper m = mapper;
        if (m != null) {
            setScrollBarValue(Disassembler.getPriorAddress(m, scrollValue,
                    officialsOnly));
            requestTextAreaRefresh();
        }
    }

    private void onScrollDown() {
        final Mapper m = mapper;
        if (m != null) {
            setScrollBarValue(Disassembler.getSuccessiveAddress(m, scrollValue,
                    officialsOnly));
            requestTextAreaRefresh();
        }
    }

    private void onScroll(final int value) {
        final Mapper m = mapper;
        if (m != null) {
            setScrollBarValue(Disassembler.getNearestAddress(m, value,
                    officialsOnly));
            requestTextAreaRefresh();
        }
    }

    private void setScrollBarValue(final int value) {
        scrollValue = value;
        scrollBar.removeAdjustmentListener(adjustmentListener);
        scrollBar.setValue(value);
        scrollBar.addAdjustmentListener(adjustmentListener);
    }

    private void enableComponents() {
        final MachineRunner r = machineRunner;
        final boolean machineRunnerAvailable = r != null;
        final boolean enabled = machineRunnerAvailable && r.isPaused();
        assemblerButton.setEnabled(machineRunnerAvailable);
        seekToButton.setEnabled(machineRunnerAvailable);
        seekToTextField.setEnabled(machineRunnerAvailable);
        suspendToggleButton.setSelected(enabled);
        suspendToggleButton.setEnabled(machineRunnerAvailable);
        suspendToggleButton.setText(enabled ? "Resume" : "Pause");
        stepIntoButton.setEnabled(enabled);
        stepOutButton.setEnabled(enabled);
        stepOverButton.setEnabled(enabled);
        stepToButton.setEnabled(enabled);
        stepToTextField.setEnabled(enabled);
        seekPcButton.setEnabled(enabled);
        nextFrameButton.setEnabled(enabled);
        scanlineButton.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
        scanlineNameLabel.setEnabled(enabled);
        scanlineLabel.setEnabled(enabled);
        dotNameLabel.setEnabled(enabled);
        dotLabel.setEnabled(enabled);
        dotButton.setEnabled(enabled);
        dotTextField.setEnabled(enabled);
        sprite0HitButton.setEnabled(enabled);
        opcodeButton.setEnabled(enabled);
        opcodeTextField.setEnabled(enabled);
        instructionsButton.setEnabled(enabled);
        instructionsTextField.setEnabled(enabled);
        irqButton.setEnabled(enabled);
        nmiButton.setEnabled(enabled);
        brkButton.setEnabled(enabled);
        rstButton.setEnabled(enabled);
        manageAddressLabelsButton.setEnabled(machineRunnerAvailable);
        breakpointsButton.setEnabled(machineRunnerAvailable);
        pcLabel.setEnabled(enabled);
        pcTextField.setEnabled(enabled);
        aLabel.setEnabled(enabled);
        aTextField.setEnabled(enabled);
        xLabel.setEnabled(enabled);
        xTextField.setEnabled(enabled);
        yLabel.setEnabled(enabled);
        yTextField.setEnabled(enabled);
        sLabel.setEnabled(enabled);
        sTextField.setEnabled(enabled);
        nCheckBox.setEnabled(enabled);
        vCheckBox.setEnabled(enabled);
        dCheckBox.setEnabled(enabled);
        iCheckBox.setEnabled(enabled);
        zCheckBox.setEnabled(enabled);
        cCheckBox.setEnabled(enabled);
        stackTextArea.setEnabled(enabled);
        ppu2002NameLabel.setEnabled(enabled);
        ppu2002Label.setEnabled(enabled);
        ppu2004NameLabel.setEnabled(enabled);
        ppu2004Label.setEnabled(enabled);
        ppu2007NameLabel.setEnabled(enabled);
        ppu2007Label.setEnabled(enabled);
        ppuVNameLabel.setEnabled(enabled);
        ppuVTextField.setEnabled(enabled);
        ppuTNameLabel.setEnabled(enabled);
        ppuTTextField.setEnabled(enabled);
        ppuXNameLabel.setEnabled(enabled);
        ppuXTextField.setEnabled(enabled);
        ppuWCheckBox.setEnabled(enabled);
        sprite0CheckBox.setEnabled(enabled);
        cameraXNameLabel.setEnabled(enabled);
        cameraXTextField.setEnabled(enabled);
        cameraYNameLabel.setEnabled(enabled);
        cameraYTextField.setEnabled(enabled);

        updateBookmarksComboBox();

        saveButton.setEnabled(machineRunnerAvailable);
        saveSlotLabel.setEnabled(machineRunnerAvailable);
        saveSlotComboBox.setEnabled(machineRunnerAvailable);
        updateLoadButton();

        updateLoggerButton();
        configLoggerButton.setEnabled(machineRunnerAvailable);

        scrollBar.setEnabled(machineRunnerAvailable);
        if (!machineRunnerAvailable) {
            debugTextArea.setText("");
        }
    }

    private int parseSeekToTextField() {
        return parseInt(seekToTextField.getText(), true, 0xFFFF);
    }

    private Integer parseScanlineTextField() {
        final PPU p = ppu;
        if (p == null) {
            return null;
        }
        final Integer result = parseInt(scanlineTextField.getText(), false,
                PPU.PRE_RENDER_SCANLINE, p.getScanlineCount() - 1);
        return (result != null && result == p.getScanlineCount() - 1)
                ? PPU.PRE_RENDER_SCANLINE : result;
    }

    private int parseDotTextField() {
        return parseInt(dotTextField.getText(), false, 340);
    }

    private int parseOpcodeTextField() {
        return parseInt(opcodeTextField.getText(), true, 0xFF);
    }

    private int parseInstructionsTextField() {
        return parseInt(instructionsTextField.getText(), false, Integer.MAX_VALUE);
    }

    private void acquirePC(final boolean clean) {
        final int pc = parseInt(pcTextField.getText(), true, 0xFFFF);
        if (pc < 0) {
            if (clean && valuesAcquired && pcValue >= 0x0000 && pcValue <= 0xFFFF) {
                pcTextField.setText(String.format("%04X", pcValue));
            }
        } else {
            pcModified |= valuesAcquired && pcValue != pc;
            if (pcModified) {
                pcValue = pc;
            }
        }
    }

    private void acquireA(final boolean clean) {
        final int a = parseInt(aTextField.getText(), true, 0xFF);
        if (a < 0) {
            if (clean && valuesAcquired && aValue >= 0x00 && aValue <= 0xFF) {
                aTextField.setText(String.format("%02X", aValue));
            }
        } else {
            aModified |= valuesAcquired && aValue != a;
            if (aModified) {
                aValue = a;
            }
        }
    }

    private void acquireX(final boolean clean) {
        final int x = parseInt(xTextField.getText(), true, 0xFF);
        if (x < 0) {
            if (clean && valuesAcquired && xValue >= 0x00 && xValue <= 0xFF) {
                xTextField.setText(String.format("%02X", xValue));
            }
        } else {
            xModified |= valuesAcquired && xValue != x;
            if (xModified) {
                xValue = x;
            }
        }
    }

    private void acquireY(final boolean clean) {
        final int y = parseInt(yTextField.getText(), true, 0xFF);
        if (y < 0) {
            if (clean && valuesAcquired && yValue >= 0x00 && yValue <= 0xFF) {
                yTextField.setText(String.format("%02X", yValue));
            }
        } else {
            yModified |= valuesAcquired && yValue != y;
            if (yModified) {
                yValue = y;
            }
        }
    }

    private void acquireS(final boolean clean) {
        final int s = parseInt(sTextField.getText(), true, 0xFF);
        if (s < 0) {
            if (clean && valuesAcquired && sValue >= 0x00 && sValue <= 0xFF) {
                sTextField.setText(String.format("%02X", sValue));
            }
        } else {
            sModified |= valuesAcquired && sValue != s;
            if (sModified) {
                sValue = s;
            }
        }
    }

    private void acquirePpuV(final boolean clean) {
        final int v = parseInt(ppuVTextField.getText(), true, 0xFF);
        if (v < 0) {
            if (clean && valuesAcquired && ppuVValue >= 0x00 && ppuVValue <= 0x7FFF) {
                ppuVTextField.setText(String.format("%04X", ppuVValue));
            }
        } else {
            ppuVModified |= valuesAcquired && ppuVValue != v;
            if (ppuVModified) {
                ppuVValue = v;
            }
        }
    }

    private void acquirePpuT(final boolean clean) {
        final int t = parseInt(ppuTTextField.getText(), true, 0xFF);
        if (t < 0) {
            if (clean && valuesAcquired && ppuTValue >= 0x00 && ppuTValue <= 0x7FFF) {
                ppuTTextField.setText(String.format("%04X", ppuTValue));
            }
        } else {
            ppuTModified |= valuesAcquired && ppuTValue != t;
            if (ppuTModified) {
                ppuTValue = t;
            }
        }
    }

    private void acquirePpuX(final boolean clean) {
        final int x = parseInt(ppuXTextField.getText(), true, 0xFF);
        if (x < 0) {
            if (clean && valuesAcquired && ppuXValue >= 0x00 && ppuXValue <= 0x07) {
                ppuXTextField.setText(String.format("%X", ppuXValue));
            }
        } else {
            ppuXModified |= valuesAcquired && ppuXValue != x;
            if (ppuXModified) {
                ppuXValue = x;
            }
        }
    }

    private void acquireCameraX(final boolean clean) {
        final int cameraX = parseInt(cameraXTextField.getText(), true, 0xFF);
        if (cameraX < 0) {
            if (clean && valuesAcquired && cameraXValue >= 0x00
                    && cameraXValue <= 0x1FF) {
                cameraXTextField.setText(String.format("%d", cameraXValue));
            }
        } else {
            cameraXModified |= valuesAcquired && cameraXValue != cameraX;
            if (cameraXModified) {
                cameraXValue = cameraX;
            }
        }
    }

    private void acquireCameraY(final boolean clean) {
        final int cameraY = parseInt(cameraYTextField.getText(), true, 0xFF);
        if (cameraY < 0) {
            if (clean && valuesAcquired && cameraYValue >= 0x00
                    && cameraYValue <= 0x1FF) {
                cameraYTextField.setText(String.format("%d", cameraYValue));
            }
        } else {
            cameraYModified |= valuesAcquired && cameraYValue != cameraY;
            if (cameraYModified) {
                cameraYValue = cameraY;
            }
        }
    }

    public void onPausedChanged(final boolean paused) {

        final CPU c = cpu;
        final PPU p = ppu;
        final Mapper m = mapper;
        if (c != null && p != null && m != null) {
            refreshDebugTextArea(paused);
            if (paused) {
                updateStackTextArea();
                pcValue = c.getPC();
                aValue = c.getA();
                xValue = c.getX();
                yValue = c.getY();
                sValue = c.getS();
                nValue = c.getN() == 1;
                vValue = c.getV() == 1;
                dValue = c.getD() == 1;
                iValue = c.getI() == 1;
                zValue = c.getZ() == 1;
                cValue = c.getC() == 1;
                ppuVValue = p.getV();
                ppuTValue = p.getT();
                ppuXValue = p.getX();
                ppuWValue = p.isW();
                sprite0Value = p.isSprite0Hit();
                cameraXValue = p.getScrollX();
                cameraYValue = p.getScrollY();
                valuesAcquired = true;

                EventQueue.invokeLater(this::assignFields);
                updateFields(Disassembler.getSuccessiveAddress(m, c.getPC(),
                                officialsOnly), p.getNextScanline(), p.getNextScanlineCycle(),
                        p.getScanline(), p.getScanlineCycle(),
                        p.peekRegister(REG_PPU_STATUS),
                        p.peekRegister(REG_OAM_DATA),
                        p.peekRegister(REG_PPU_DATA));
            } else {
                if (pcModified) {
                    pcModified = false;
                    c.setPC(pcValue);
                }
                if (aModified) {
                    aModified = false;
                    c.setA(aValue);
                }
                if (xModified) {
                    xModified = false;
                    c.setX(xValue);
                }
                if (yModified) {
                    yModified = false;
                    c.setY(yValue);
                }
                if (sModified) {
                    sModified = false;
                    c.setS(sValue);
                }
                if (nModified) {
                    nModified = false;
                    c.setN(nValue ? 1 : 0);
                }
                if (vModified) {
                    vModified = false;
                    c.setV(vValue ? 1 : 0);
                }
                if (dModified) {
                    dModified = false;
                    c.setD(dValue ? 1 : 0);
                }
                if (iModified) {
                    iModified = false;
                    c.setI(iValue ? 1 : 0);
                }
                if (zModified) {
                    zModified = false;
                    c.setZ(zValue ? 1 : 0);
                }
                if (cModified) {
                    cModified = false;
                    c.setC(cValue ? 1 : 0);
                }
                if (ppuVModified) {
                    ppuVModified = false;
                    p.setV(ppuVValue);
                }
                if (ppuTModified) {
                    ppuTModified = false;
                    p.setT(ppuTValue);
                }
                if (ppuXModified) {
                    ppuXModified = false;
                    p.setX(ppuXValue);
                }
                if (ppuWModified) {
                    ppuWModified = false;
                    p.setW(ppuWValue);
                }
                if (sprite0Modified) {
                    sprite0Modified = false;
                    p.setSprite0Hit(sprite0Value);
                }
                if (cameraXModified) {
                    cameraXModified = false;
                    p.setScrollX(cameraXValue);
                }
                if (cameraYModified) {
                    cameraYModified = false;
                    p.setScrollY(cameraYValue);
                }
            }
        }

        EventQueue.invokeLater(this::enableComponents);
        App.flushTraceLogger();
    }

    private void highlightAddress() {
        showCursor(debugTextArea);
        final List<Instruction> insts = instructions;
        if (insts != null) {
            final int pos = debugTextArea.getCaretPosition();
            for (final Instruction instruction : insts) {
                if (pos >= instruction.getStart() && pos <= instruction.getEnd()) {
                    for (final AddressTextRange range : instruction.getRanges()) {
                        if (range == null) {
                            break;
                        } else {
                            if (pos >= range.getStart() && pos <= range.getEnd()) {
                                debugTextArea.setSelectionStart(range.getStart());
                                debugTextArea.setSelectionEnd(range.getEnd());
                                handleAddressClick(range);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleAddressClick(final AddressTextRange range) {
        lastRange = range;
        final String address = String.format("%04X", range.getAddress());
        seekToTextField.setText(address);
        stepToTextField.setText(address);
    }

    private void handlePopupTrigger(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            final int selectionStart = debugTextArea.getSelectionStart();
            final int selectionEnd = debugTextArea.getSelectionEnd();
            final boolean rangeEnabled = lastRange != null
                    && lastRange.getStart() == selectionStart
                    && lastRange.getEnd() == selectionEnd;
            final boolean machineRunnerAvailable = machineRunner != null;
            seekToMenuItem.setEnabled(rangeEnabled && seekToButton.isEnabled());
            stepToMenuItem.setEnabled(rangeEnabled && stepToButton.isEnabled());
            addBreakpointMenuItem.setEnabled(rangeEnabled
                    && breakpointsButton.isEnabled());
            addressLabelMenuItem.setEnabled(rangeEnabled
                    && manageAddressLabelsButton.isEnabled());
            hexEditorMenuItem.setEnabled(rangeEnabled && machineRunnerAvailable);

            copyMenuItem.setEnabled(selectionStart != selectionEnd);
            debugPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void applyBreakpoints() {
        App.setBreakpoints(GamePrefs.getInstance().getDebuggerGamePrefs()
                .getBreakpoints());
    }

    private void onQuickSaveChanged(
            List<QuickSaveStateInfo> quickSaveStateInfos) {
        this.quickSaveStateInfos = quickSaveStateInfos;
        updateLoadButton();
    }

    private void updateLoadButton() {
        if (!saveButton.isEnabled()) {
            loadButton.setEnabled(false);
            return;
        }
        final int index = saveSlotComboBox.getSelectedIndex();
        if (quickSaveStateInfos != null && index < quickSaveStateInfos.size()) {
            final QuickSaveStateInfo info = quickSaveStateInfos.get(index);
            loadButton.setEnabled(info.getFile().exists());
        } else {
            loadButton.setEnabled(false);
        }
    }

    public void updateLoggerButton() {
        if (App.isTraceLoggerRunning()) {
            loggerToggleButton.setSelected(true);
            loggerToggleButton.setText("Stop Logger");
        } else {
            loggerToggleButton.setSelected(false);
            loggerToggleButton.setText("Start Logger");
        }
        loggerToggleButton.setEnabled(machineRunner != null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        debugPopupMenu = new javax.swing.JPopupMenu();
        copyMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        separator1 = new javax.swing.JPopupMenu.Separator();
        seekToMenuItem = new javax.swing.JMenuItem();
        stepToMenuItem = new javax.swing.JMenuItem();
        addBreakpointMenuItem = new javax.swing.JMenuItem();
        addressLabelMenuItem = new javax.swing.JMenuItem();
        hexEditorMenuItem = new javax.swing.JMenuItem();
        stepIntoButton = new javax.swing.JButton();
        suspendToggleButton = new javax.swing.JToggleButton();
        stepOutButton = new javax.swing.JButton();
        stepOverButton = new javax.swing.JButton();
        stepToButton = new javax.swing.JButton();
        stepToTextField = new javax.swing.JTextField();
        seekToButton = new javax.swing.JButton();
        seekToTextField = new javax.swing.JTextField();
        seekPcButton = new javax.swing.JButton();
        pcLabel = new javax.swing.JLabel();
        aLabel = new javax.swing.JLabel();
        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        sLabel = new javax.swing.JLabel();
        sTextField = new javax.swing.JTextField();
        yTextField = new javax.swing.JTextField();
        xTextField = new javax.swing.JTextField();
        aTextField = new javax.swing.JTextField();
        pcTextField = new javax.swing.JTextField();
        stackScrollPane = new javax.swing.JScrollPane();
        stackTextArea = new javax.swing.JTextArea();
        nCheckBox = new javax.swing.JCheckBox();
        vCheckBox = new javax.swing.JCheckBox();
        dCheckBox = new javax.swing.JCheckBox();
        iCheckBox = new javax.swing.JCheckBox();
        zCheckBox = new javax.swing.JCheckBox();
        cCheckBox = new javax.swing.JCheckBox();
        nextFrameButton = new javax.swing.JButton();
        scanlineButton = new javax.swing.JButton();
        scanlineTextField = new javax.swing.JTextField();
        dotButton = new javax.swing.JButton();
        dotTextField = new javax.swing.JTextField();
        scanlineNameLabel = new javax.swing.JLabel();
        scanlineLabel = new javax.swing.JLabel();
        dotNameLabel = new javax.swing.JLabel();
        dotLabel = new javax.swing.JLabel();
        sprite0HitButton = new javax.swing.JButton();
        nmiButton = new javax.swing.JButton();
        irqButton = new javax.swing.JButton();
        brkButton = new javax.swing.JButton();
        rstButton = new javax.swing.JButton();
        opcodeButton = new javax.swing.JButton();
        opcodeTextField = new javax.swing.JTextField();
        scrollBar = new javax.swing.JScrollBar();
        debugScrollPane = new javax.swing.JScrollPane();
        debugTextArea = new MetricsTextArea();
        manageAddressLabelsButton = new javax.swing.JButton();
        bookmarksComboBox = new javax.swing.JComboBox();
        breakpointsButton = new javax.swing.JButton();
        labelsCheckBox = new javax.swing.JCheckBox();
        machineCodeCheckBox = new javax.swing.JCheckBox();
        inspectionsCheckBox = new javax.swing.JCheckBox();
        addressTypeComboBox = new javax.swing.JComboBox();
        pcCheckBox = new javax.swing.JCheckBox();
        branchesComboBox = new javax.swing.JComboBox();
        instructionsButton = new javax.swing.JButton();
        instructionsTextField = new javax.swing.JTextField();
        assemblerButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        saveSlotLabel = new javax.swing.JLabel();
        saveSlotComboBox = new javax.swing.JComboBox();
        loggerToggleButton = new javax.swing.JToggleButton();
        configLoggerButton = new javax.swing.JButton();
        unofficialOpcodesCheckBox = new javax.swing.JCheckBox();
        ppu2002NameLabel = new javax.swing.JLabel();
        ppu2002Label = new javax.swing.JLabel();
        ppu2004NameLabel = new javax.swing.JLabel();
        ppu2004Label = new javax.swing.JLabel();
        ppu2007NameLabel = new javax.swing.JLabel();
        ppu2007Label = new javax.swing.JLabel();
        ppuVNameLabel = new javax.swing.JLabel();
        ppuVTextField = new javax.swing.JTextField();
        ppuTNameLabel = new javax.swing.JLabel();
        ppuTTextField = new javax.swing.JTextField();
        ppuXNameLabel = new javax.swing.JLabel();
        ppuXTextField = new javax.swing.JTextField();
        ppuWCheckBox = new javax.swing.JCheckBox();
        sprite0CheckBox = new javax.swing.JCheckBox();
        cameraXNameLabel = new javax.swing.JLabel();
        cameraXTextField = new javax.swing.JTextField();
        cameraYNameLabel = new javax.swing.JLabel();
        cameraYTextField = new javax.swing.JTextField();
        closeButton = new javax.swing.JButton();
        defaultsButton = new javax.swing.JButton();
        bookmarksLabel = new javax.swing.JLabel();

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setText("Copy");
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(copyMenuItem);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(selectAllMenuItem);
        debugPopupMenu.add(separator1);

        seekToMenuItem.setText("Seek To");
        seekToMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekToMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(seekToMenuItem);

        stepToMenuItem.setText("Step To");
        stepToMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepToMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(stepToMenuItem);

        addBreakpointMenuItem.setText("Breakpoint...");
        addBreakpointMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBreakpointMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(addBreakpointMenuItem);

        addressLabelMenuItem.setText("Address Label...");
        addressLabelMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressLabelMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(addressLabelMenuItem);

        hexEditorMenuItem.setText("Hex Editor...");
        hexEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexEditorMenuItemActionPerformed(evt);
            }
        });
        debugPopupMenu.add(hexEditorMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Debugger");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        stepIntoButton.setText("Step Into");
        stepIntoButton.setToolTipText("Runs one instruction, then pauses.");
        stepIntoButton.setFocusPainted(false);
        stepIntoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepIntoButtonActionPerformed(evt);
            }
        });

        suspendToggleButton.setText("Pause");
        suspendToggleButton.setFocusPainted(false);
        suspendToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suspendToggleButtonActionPerformed(evt);
            }
        });

        stepOutButton.setText("Step Out");
        stepOutButton.setToolTipText("Runs until the current subroutine ends.");
        stepOutButton.setFocusPainted(false);
        stepOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepOutButtonActionPerformed(evt);
            }
        });

        stepOverButton.setText("Step Over");
        stepOverButton.setToolTipText("Runs one instruction, but JSR will run until RTS.");
        stepOverButton.setFocusPainted(false);
        stepOverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepOverButtonActionPerformed(evt);
            }
        });

        stepToButton.setText("Step To:");
        stepToButton.setToolTipText("Step to specified execution address.");
        stepToButton.setFocusPainted(false);
        stepToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepToButtonActionPerformed(evt);
            }
        });

        stepToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepToTextFieldActionPerformed(evt);
            }
        });

        seekToButton.setText("Seek To:");
        seekToButton.setToolTipText("Scroll to specified address.");
        seekToButton.setFocusPainted(false);
        seekToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekToButtonActionPerformed(evt);
            }
        });

        seekToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekToTextFieldActionPerformed(evt);
            }
        });

        seekPcButton.setText("Seek PC");
        seekPcButton.setToolTipText("Scroll to current execution point.");
        seekPcButton.setFocusPainted(false);
        seekPcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekPcButtonActionPerformed(evt);
            }
        });

        pcLabel.setText("PC:");

        aLabel.setText("A:");

        xLabel.setText("X:");

        yLabel.setText("Y:");

        sLabel.setText("S:");

        sTextField.setColumns(3);
        sTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                sTextFieldFocusLost(evt);
            }
        });
        sTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sTextFieldActionPerformed(evt);
            }
        });

        yTextField.setColumns(3);
        yTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                yTextFieldFocusLost(evt);
            }
        });
        yTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yTextFieldActionPerformed(evt);
            }
        });

        xTextField.setColumns(3);
        xTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                xTextFieldFocusLost(evt);
            }
        });
        xTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xTextFieldActionPerformed(evt);
            }
        });

        aTextField.setColumns(3);
        aTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                aTextFieldFocusLost(evt);
            }
        });
        aTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aTextFieldActionPerformed(evt);
            }
        });

        pcTextField.setColumns(5);
        pcTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                pcTextFieldFocusLost(evt);
            }
        });
        pcTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pcTextFieldActionPerformed(evt);
            }
        });

        stackScrollPane.setMaximumSize(null);
        stackScrollPane.setMinimumSize(null);
        stackScrollPane.setPreferredSize(null);

        stackTextArea.setEditable(false);
        stackTextArea.setColumns(33);
        stackTextArea.setLineWrap(true);
        stackTextArea.setRows(3);
        stackTextArea.setWrapStyleWord(true);
        stackTextArea.setMaximumSize(null);
        stackTextArea.setPreferredSize(null);
        stackScrollPane.setViewportView(stackTextArea);

        nCheckBox.setText("N");
        nCheckBox.setFocusPainted(false);

        vCheckBox.setText("V");
        vCheckBox.setFocusPainted(false);

        dCheckBox.setText("D");
        dCheckBox.setFocusPainted(false);

        iCheckBox.setText("I");
        iCheckBox.setFocusPainted(false);

        zCheckBox.setText("Z");
        zCheckBox.setFocusPainted(false);

        cCheckBox.setText("C");
        cCheckBox.setFocusPainted(false);

        nextFrameButton.setText("Frame+1");
        nextFrameButton.setToolTipText("Step to next frame.");
        nextFrameButton.setFocusPainted(false);
        nextFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextFrameButtonActionPerformed(evt);
            }
        });

        scanlineButton.setText("Scanline:");
        scanlineButton.setToolTipText("Step to specified scanline.");
        scanlineButton.setFocusPainted(false);
        scanlineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanlineButtonActionPerformed(evt);
            }
        });

        scanlineTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanlineTextFieldActionPerformed(evt);
            }
        });

        dotButton.setText("Dot:");
        dotButton.setToolTipText("Step to specified scanline cycle.");
        dotButton.setFocusPainted(false);
        dotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dotButtonActionPerformed(evt);
            }
        });

        dotTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dotTextFieldActionPerformed(evt);
            }
        });

        scanlineNameLabel.setText("Scanline:");

        scanlineLabel.setText("---");

        dotNameLabel.setText("Dot:");

        dotLabel.setText("---");

        sprite0HitButton.setText("Sprite 0");
        sprite0HitButton.setToolTipText("Step to next sprite 0 hit.");
        sprite0HitButton.setFocusPainted(false);
        sprite0HitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sprite0HitButtonActionPerformed(evt);
            }
        });

        nmiButton.setText("NMI");
        nmiButton.setToolTipText("Step to next NMI.");
        nmiButton.setFocusPainted(false);
        nmiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nmiButtonActionPerformed(evt);
            }
        });

        irqButton.setText("IRQ");
        irqButton.setToolTipText("Step to next IRQ.");
        irqButton.setFocusPainted(false);
        irqButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                irqButtonActionPerformed(evt);
            }
        });

        brkButton.setText("BRK");
        brkButton.setToolTipText("Step to next BRK.");
        brkButton.setFocusPainted(false);
        brkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brkButtonActionPerformed(evt);
            }
        });

        rstButton.setText("RST");
        rstButton.setToolTipText("Step to next reset.");
        rstButton.setFocusPainted(false);
        rstButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rstButtonActionPerformed(evt);
            }
        });

        opcodeButton.setText("Opcode:");
        opcodeButton.setToolTipText("Step to next encounter of specified opcode.");
        opcodeButton.setFocusPainted(false);
        opcodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcodeButtonActionPerformed(evt);
            }
        });

        opcodeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcodeTextFieldActionPerformed(evt);
            }
        });

        scrollBar.setMaximum(65535);

        debugScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        debugScrollPane.setPreferredSize(null);
        debugScrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                debugScrollPaneComponentResized(evt);
            }

            public void componentShown(java.awt.event.ComponentEvent evt) {
                debugScrollPaneComponentShown(evt);
            }
        });

        debugTextArea.setEditable(false);
        debugTextArea.setAutoscrolls(false);
        debugTextArea.setBorder(null);
        debugTextArea.setMaximumSize(null);
        debugTextArea.setPreferredSize(null);
        debugTextArea.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                debugTextAreaMouseWheelMoved(evt);
            }
        });
        debugTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                debugTextAreaMouseClicked(evt);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                debugTextAreaMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                debugTextAreaMouseReleased(evt);
            }
        });
        debugScrollPane.setViewportView(debugTextArea);

        manageAddressLabelsButton.setText("Labels...");
        manageAddressLabelsButton.setFocusPainted(false);
        manageAddressLabelsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageAddressLabelsButtonActionPerformed(evt);
            }
        });

        bookmarksComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookmarksComboBoxActionPerformed(evt);
            }
        });

        breakpointsButton.setText("Breakpoints...");
        breakpointsButton.setFocusPainted(false);
        breakpointsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                breakpointsButtonActionPerformed(evt);
            }
        });

        labelsCheckBox.setSelected(true);
        labelsCheckBox.setText("Address labels");
        labelsCheckBox.setFocusPainted(false);
        labelsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelsCheckBoxActionPerformed(evt);
            }
        });

        machineCodeCheckBox.setSelected(true);
        machineCodeCheckBox.setText("Machine code");
        machineCodeCheckBox.setFocusPainted(false);
        machineCodeCheckBox.setMaximumSize(null);
        machineCodeCheckBox.setMinimumSize(null);
        machineCodeCheckBox.setPreferredSize(null);
        machineCodeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                machineCodeCheckBoxActionPerformed(evt);
            }
        });

        inspectionsCheckBox.setSelected(true);
        inspectionsCheckBox.setText("Inspections");
        inspectionsCheckBox.setFocusPainted(false);
        inspectionsCheckBox.setMaximumSize(null);
        inspectionsCheckBox.setMinimumSize(null);
        inspectionsCheckBox.setPreferredSize(null);
        inspectionsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inspectionsCheckBoxActionPerformed(evt);
            }
        });

        addressTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Bank:Address", "File Offset", "No Addresses"}));
        addressTypeComboBox.setFocusable(false);
        addressTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressTypeComboBoxActionPerformed(evt);
            }
        });

        pcCheckBox.setSelected(true);
        pcCheckBox.setText("PC");
        pcCheckBox.setFocusPainted(false);
        pcCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pcCheckBoxActionPerformed(evt);
            }
        });

        branchesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Absolute branches", "Hex branches", "Decimal branches"}));
        branchesComboBox.setFocusable(false);
        branchesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                branchesComboBoxActionPerformed(evt);
            }
        });

        instructionsButton.setText("Insts:");
        instructionsButton.setToolTipText("Step ahead by specified number of instructions.");
        instructionsButton.setFocusPainted(false);
        instructionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instructionsButtonActionPerformed(evt);
            }
        });

        instructionsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instructionsTextFieldActionPerformed(evt);
            }
        });

        assemblerButton.setText("Assembler...");
        assemblerButton.setFocusPainted(false);
        assemblerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assemblerButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        loadButton.setText("Load");
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        saveSlotLabel.setText("Save slot:");

        saveSlotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"}));
        saveSlotComboBox.setFocusable(false);
        saveSlotComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSlotComboBoxActionPerformed(evt);
            }
        });

        loggerToggleButton.setText("Start Logger");
        loggerToggleButton.setFocusPainted(false);
        loggerToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerToggleButtonActionPerformed(evt);
            }
        });

        configLoggerButton.setText("Config Logger...");
        configLoggerButton.setFocusPainted(false);
        configLoggerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configLoggerButtonActionPerformed(evt);
            }
        });

        unofficialOpcodesCheckBox.setText("Unofficial opcodes");
        unofficialOpcodesCheckBox.setFocusPainted(false);
        unofficialOpcodesCheckBox.setMaximumSize(null);
        unofficialOpcodesCheckBox.setMinimumSize(null);
        unofficialOpcodesCheckBox.setPreferredSize(null);
        unofficialOpcodesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unofficialOpcodesCheckBoxActionPerformed(evt);
            }
        });

        ppu2002NameLabel.setText("$2002:");

        ppu2002Label.setText("--");

        ppu2004NameLabel.setText("$2004:");

        ppu2004Label.setText("--");

        ppu2007NameLabel.setText("$2007:");

        ppu2007Label.setText("--");

        ppuVNameLabel.setText("v:");

        ppuVTextField.setColumns(5);
        ppuVTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ppuVTextFieldFocusLost(evt);
            }
        });
        ppuVTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppuVTextFieldActionPerformed(evt);
            }
        });

        ppuTNameLabel.setText("t:");

        ppuTTextField.setColumns(5);
        ppuTTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ppuTTextFieldFocusLost(evt);
            }
        });
        ppuTTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppuTTextFieldActionPerformed(evt);
            }
        });

        ppuXNameLabel.setText("x:");

        ppuXTextField.setColumns(3);
        ppuXTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ppuXTextFieldFocusLost(evt);
            }
        });
        ppuXTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppuXTextFieldActionPerformed(evt);
            }
        });

        ppuWCheckBox.setText("w");

        sprite0CheckBox.setText("Sprite 0");

        cameraXNameLabel.setText("Camera X:");

        cameraXTextField.setColumns(4);
        cameraXTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cameraXTextFieldFocusLost(evt);
            }
        });
        cameraXTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraXTextFieldActionPerformed(evt);
            }
        });

        cameraYNameLabel.setText("Camera Y:");

        cameraYTextField.setColumns(4);
        cameraYTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cameraYTextFieldFocusLost(evt);
            }
        });
        cameraYTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraYTextFieldActionPerformed(evt);
            }
        });

        closeButton.setMnemonic('C');
        closeButton.setText("Close");
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Defaults");
        defaultsButton.setFocusPainted(false);
        defaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsButtonActionPerformed(evt);
            }
        });

        bookmarksLabel.setText("Bookmarks:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(debugScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(scrollBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(loggerToggleButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(configLoggerButton))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(nmiButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(irqButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(brkButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(rstButton))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(labelsCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(machineCodeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(unofficialOpcodesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(saveButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(loadButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(saveSlotLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(saveSlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(suspendToggleButton)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(scanlineButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(dotButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(dotTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(opcodeButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(opcodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(instructionsButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(instructionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(seekToButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(seekToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(stepToButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(stepToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(stepOverButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(stepIntoButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(stepOutButton))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(nextFrameButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(sprite0HitButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(seekPcButton))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(pcLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(pcTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(aLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(aTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(xLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(xTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(yLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(yTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(sLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(sTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(nCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(vCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(dCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(iCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(zCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(cCheckBox))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(scanlineNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(scanlineLabel)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(dotNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(dotLabel)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(ppu2002NameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ppu2002Label)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(ppu2004NameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ppu2004Label)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(ppu2007NameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ppu2007Label))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(ppuVNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ppuVTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(ppuTNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ppuTTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(ppuXNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ppuXTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(ppuWCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(sprite0CheckBox))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(cameraXNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(cameraXTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(cameraYNameLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(cameraYTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(manageAddressLabelsButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(breakpointsButton)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(assemblerButton))
                                                .addComponent(stackScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(defaultsButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(closeButton))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(addressTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(branchesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(pcCheckBox)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(inspectionsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(bookmarksLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bookmarksComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, brkButton, dotButton, dotTextField, instructionsButton, instructionsTextField, irqButton, loadButton, manageAddressLabelsButton, nextFrameButton, nmiButton, opcodeButton, opcodeTextField, rstButton, saveButton, scanlineButton, scanlineTextField, seekPcButton, seekToButton, seekToTextField, sprite0HitButton, stepIntoButton, stepOutButton, stepOverButton, stepToButton, stepToTextField, suspendToggleButton);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, configLoggerButton, loggerToggleButton);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, closeButton, defaultsButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(scrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(debugScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(suspendToggleButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(stepIntoButton)
                                                        .addComponent(stepOverButton)
                                                        .addComponent(stepOutButton))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(nextFrameButton)
                                                        .addComponent(sprite0HitButton)
                                                        .addComponent(seekPcButton))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(nmiButton)
                                                        .addComponent(irqButton)
                                                        .addComponent(brkButton)
                                                        .addComponent(rstButton))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(seekToButton)
                                                        .addComponent(seekToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(stepToButton)
                                                        .addComponent(stepToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(scanlineButton)
                                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(dotButton)
                                                        .addComponent(dotTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(opcodeButton)
                                                        .addComponent(opcodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(instructionsButton)
                                                        .addComponent(instructionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(pcLabel)
                                                        .addComponent(pcTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(aLabel)
                                                        .addComponent(aTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(xLabel)
                                                        .addComponent(xTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(yLabel)
                                                        .addComponent(yTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(sLabel)
                                                        .addComponent(sTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(nCheckBox)
                                                        .addComponent(vCheckBox)
                                                        .addComponent(dCheckBox)
                                                        .addComponent(iCheckBox)
                                                        .addComponent(zCheckBox)
                                                        .addComponent(cCheckBox))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(stackScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(scanlineNameLabel)
                                                        .addComponent(scanlineLabel)
                                                        .addComponent(dotNameLabel)
                                                        .addComponent(dotLabel)
                                                        .addComponent(ppu2002NameLabel)
                                                        .addComponent(ppu2002Label)
                                                        .addComponent(ppu2004NameLabel)
                                                        .addComponent(ppu2004Label)
                                                        .addComponent(ppu2007NameLabel)
                                                        .addComponent(ppu2007Label))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(ppuVNameLabel)
                                                        .addComponent(ppuVTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(ppuTNameLabel)
                                                        .addComponent(ppuTTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(ppuXNameLabel)
                                                        .addComponent(ppuXTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(ppuWCheckBox)
                                                        .addComponent(sprite0CheckBox))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(cameraXNameLabel)
                                                        .addComponent(cameraXTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(cameraYNameLabel)
                                                        .addComponent(cameraYTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(bookmarksComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(bookmarksLabel))
                                                .addGap(8, 8, 8)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(breakpointsButton)
                                                        .addComponent(manageAddressLabelsButton)
                                                        .addComponent(assemblerButton))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(addressTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(branchesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(pcCheckBox)
                                                        .addComponent(inspectionsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labelsCheckBox)
                                                        .addComponent(machineCodeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(unofficialOpcodesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(saveButton)
                                                        .addComponent(loadButton)
                                                        .addComponent(saveSlotLabel)
                                                        .addComponent(saveSlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(loggerToggleButton)
                                                        .addComponent(configLoggerButton))
                                                .addGap(18, 18, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(closeButton)
                                                        .addComponent(defaultsButton))))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void stepIntoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepIntoButtonActionPerformed
        acquireFields();
        App.step(Into);
    }//GEN-LAST:event_stepIntoButtonActionPerformed

    private void debugTextAreaMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_debugTextAreaMouseWheelMoved
        if (evt.getWheelRotation() > 0) {
            onScrollDown();
        } else if (evt.getWheelRotation() < 0) {
            onScrollUp();
        }
    }//GEN-LAST:event_debugTextAreaMouseWheelMoved

    private void suspendToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suspendToggleButtonActionPerformed
        App.setStepPause(suspendToggleButton.isSelected());
    }//GEN-LAST:event_suspendToggleButtonActionPerformed

    private void stepOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepOutButtonActionPerformed
        acquireFields();
        App.step(Out);
    }//GEN-LAST:event_stepOutButtonActionPerformed

    private void stepOverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepOverButtonActionPerformed
        acquireFields();
        App.step(Over);
    }//GEN-LAST:event_stepOverButtonActionPerformed

    private void stepToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepToButtonActionPerformed
        acquireFields();
        final int address = parseInt(stepToTextField.getText(), true, 0xFFFF);
        if (address >= 0) {
            App.stepToAddress(address);
        }
    }//GEN-LAST:event_stepToButtonActionPerformed

    private void seekToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekToButtonActionPerformed
        final int address = parseSeekToTextField();
        if (address >= 0x0000) {
            setScrollBarValue(address);
            requestTextAreaRefresh();
        } else {
            switch (address) {
                case EMPTY:
                    displayError(this, "Please specify the seek address.");
                    break;
                case OUT_OF_RANGE:
                    displayError(this, "The seek address is out of range.");
                    break;
                case NOT_A_NUMBER:
                    displayError(this,
                            "The specified seek address is not a valid hex value.");
                    break;
            }
        }
    }//GEN-LAST:event_seekToButtonActionPerformed

    private void seekToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekToTextFieldActionPerformed
        seekToButtonActionPerformed(evt);
        requestFocusInWindow();
    }//GEN-LAST:event_seekToTextFieldActionPerformed

    private void seekPcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekPcButtonActionPerformed
        refreshDebugTextArea(true);
    }//GEN-LAST:event_seekPcButtonActionPerformed

    private void nextFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextFrameButtonActionPerformed
        acquireFields();
        App.step(Frame);
    }//GEN-LAST:event_nextFrameButtonActionPerformed

    private void scanlineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanlineButtonActionPerformed
        acquireFields();
        final Integer scanline = parseScanlineTextField();
        if (scanline == null) {
            if (isBlank(scanlineTextField.getText())) {
                displayError(this, "Please specify the target scanline.");
            } else {
                final PPU p = ppu;
                if (p == null) {
                    displayError(this, "Invalid scanline value.");
                } else {
                    displayError(this, "Enter a scanline value from %d to %d.",
                            PPU.PRE_RENDER_SCANLINE, p.getScanlineCount() - 2);
                }
            }
        } else {
            App.stepToScanline(scanline);
        }
    }//GEN-LAST:event_scanlineButtonActionPerformed

    private void scanlineTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanlineTextFieldActionPerformed
        scanlineButtonActionPerformed(evt);
        requestFocusInWindow();
    }//GEN-LAST:event_scanlineTextFieldActionPerformed

    private void dotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dotButtonActionPerformed
        acquireFields();
        final int dot = parseDotTextField();
        if (dot >= 0) {
            App.stepToDot(dot);
        } else {
            switch (dot) {
                case EMPTY:
                    displayError(this, "Please specify the scanline cycle value.");
                    break;
                case OUT_OF_RANGE:
                    displayError(this, "Enter a scanline cycle value from 0 to 340.");
                    break;
                case NOT_A_NUMBER:
                    displayError(this,
                            "The specified scanline cycle value is not a number.");
                    break;
            }
        }
    }//GEN-LAST:event_dotButtonActionPerformed

    private void dotTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dotTextFieldActionPerformed
        dotButtonActionPerformed(evt);
        requestFocusInWindow();
    }//GEN-LAST:event_dotTextFieldActionPerformed

    private void sprite0HitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sprite0HitButtonActionPerformed
        acquireFields();
        App.step(Sprite0);
    }//GEN-LAST:event_sprite0HitButtonActionPerformed

    private void nmiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nmiButtonActionPerformed
        acquireFields();
        App.step(NMI);
    }//GEN-LAST:event_nmiButtonActionPerformed

    private void irqButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_irqButtonActionPerformed
        acquireFields();
        App.step(IRQ);
    }//GEN-LAST:event_irqButtonActionPerformed

    private void brkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brkButtonActionPerformed
        acquireFields();
        App.step(BRK);
    }//GEN-LAST:event_brkButtonActionPerformed

    private void rstButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rstButtonActionPerformed
        acquireFields();
        App.step(RST);
    }//GEN-LAST:event_rstButtonActionPerformed

    private void opcodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcodeButtonActionPerformed
        acquireFields();
        final int opcode = parseOpcodeTextField();
        if (opcode >= 0) {
            App.stepToOpcode(opcode);
        } else {
            switch (opcode) {
                case EMPTY:
                    displayError(this, "Please specify the opcode value.");
                    break;
                case OUT_OF_RANGE:
                    displayError(this, "Enter a hexidecimal opcode from 00 to FF.");
                    break;
                case NOT_A_NUMBER:
                    displayError(this, "The specified opcode is not a valid hex value.");
                    break;
            }
        }
    }//GEN-LAST:event_opcodeButtonActionPerformed

    private void opcodeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcodeTextFieldActionPerformed
        opcodeButtonActionPerformed(evt);
        requestFocusInWindow();
    }//GEN-LAST:event_opcodeTextFieldActionPerformed

    private void debugScrollPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_debugScrollPaneComponentResized
        updateVisibleLines();
    }//GEN-LAST:event_debugScrollPaneComponentResized

    private void debugScrollPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_debugScrollPaneComponentShown
        updateVisibleLines();
    }//GEN-LAST:event_debugScrollPaneComponentShown

    private void debugTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_debugTextAreaMouseClicked
        highlightAddress();
    }//GEN-LAST:event_debugTextAreaMouseClicked

    private void manageAddressLabelsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageAddressLabelsButtonActionPerformed
        final AddressLabelDialog dialog = new AddressLabelDialog(this);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            initAddressLabels();
            requestTextAreaRefresh();
        }
    }//GEN-LAST:event_manageAddressLabelsButtonActionPerformed

    private void debugTextAreaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_debugTextAreaMousePressed
        handlePopupTrigger(evt);
    }//GEN-LAST:event_debugTextAreaMousePressed

    private void debugTextAreaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_debugTextAreaMouseReleased
        handlePopupTrigger(evt);
    }//GEN-LAST:event_debugTextAreaMouseReleased

    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        debugTextArea.selectAll();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed

    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        debugTextArea.copy();
    }//GEN-LAST:event_copyMenuItemActionPerformed

    private void seekToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekToMenuItemActionPerformed
        if (lastRange != null && lastRange.getAddress() >= 0x0000
                && lastRange.getAddress() <= 0xFFFF) {
            setScrollBarValue(lastRange.getAddress());
            requestTextAreaRefresh();
        }
    }//GEN-LAST:event_seekToMenuItemActionPerformed

    private void stepToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepToMenuItemActionPerformed
        if (lastRange != null && lastRange.getAddress() >= 0x0000
                && lastRange.getAddress() <= 0xFFFF) {
            App.stepToAddress(lastRange.getAddress());
        }
    }//GEN-LAST:event_stepToMenuItemActionPerformed

    private void addressLabelMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressLabelMenuItemActionPerformed
        final AddressLabelDialog dialog = new AddressLabelDialog(this);
        dialog.setRange(lastRange);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            refreshAddressLabels();
        }
    }//GEN-LAST:event_addressLabelMenuItemActionPerformed

    private void bookmarksComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookmarksComboBoxActionPerformed
        final AddressLabel label = (AddressLabel) bookmarksComboBox
                .getSelectedItem();
        if (label != null) {
            final int address = label.getAddress();
            if (address >= 0x0000 && address <= 0xFFFF) {
                setScrollBarValue(address);
                requestTextAreaRefresh();
            }
        }
    }//GEN-LAST:event_bookmarksComboBoxActionPerformed

    private void hexEditorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexEditorMenuItemActionPerformed
        if (lastRange != null && lastRange.getAddress() >= 0x0000
                && lastRange.getAddress() <= 0xFFFF) {
            App.createHexEditorFrame();
            if (lastRange.getBank() < 0) {
                EventQueue.invokeLater(() -> App.getHexEditorFrame().goToAddress(
                        CpuMemory, lastRange.getAddress()));
            } else {
                EventQueue.invokeLater(() -> App.getHexEditorFrame()
                        .goToFileContents(lastRange.getAddress()));
            }
        }
    }//GEN-LAST:event_hexEditorMenuItemActionPerformed

    private void breakpointsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_breakpointsButtonActionPerformed
        if (machineRunner != null) {
            final BreakpointDialog dialog = new BreakpointDialog(this);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                applyBreakpoints();
            }
        }
    }//GEN-LAST:event_breakpointsButtonActionPerformed

    private void addBreakpointMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBreakpointMenuItemActionPerformed
        final AddressTextRange range = lastRange;
        if (machineRunner != null) {
            final BreakpointDialog dialog = new BreakpointDialog(this);
            if (range != null) {
                dialog.setBreakpoint(new Breakpoint(BreakpointType.Execute,
                        range.getBank(), range.getAddress(), -1, true));
            }
            dialog.setVisible(true);
            if (dialog.isOk()) {
                applyBreakpoints();
            }
        }
    }//GEN-LAST:event_addBreakpointMenuItemActionPerformed

    private void labelsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelsCheckBoxActionPerformed
        Disassembler.setShowLabels(labelsCheckBox.isSelected());
        requestTextAreaRefresh();
    }//GEN-LAST:event_labelsCheckBoxActionPerformed

    private void addressTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressTypeComboBoxActionPerformed
        final int selectedIndex = addressTypeComboBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            Disassembler.setAddressType(selectedIndex);
            requestTextAreaRefresh();
        }
    }//GEN-LAST:event_addressTypeComboBoxActionPerformed

    private void machineCodeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_machineCodeCheckBoxActionPerformed
        Disassembler.setMachineCode(machineCodeCheckBox.isSelected());
        requestTextAreaRefresh();
    }//GEN-LAST:event_machineCodeCheckBoxActionPerformed

    private void inspectionsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inspectionsCheckBoxActionPerformed
        Disassembler.setInspections(inspectionsCheckBox.isSelected());
        requestTextAreaRefresh();
    }//GEN-LAST:event_inspectionsCheckBoxActionPerformed

    private void pcCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pcCheckBoxActionPerformed
        Disassembler.setShowPC(pcCheckBox.isSelected());
        requestTextAreaRefresh();
    }//GEN-LAST:event_pcCheckBoxActionPerformed

    private void branchesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_branchesComboBoxActionPerformed
        final int selectedIndex = branchesComboBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            Disassembler.setBranchesType(selectedIndex);
            requestTextAreaRefresh();
        }
    }//GEN-LAST:event_branchesComboBoxActionPerformed

    private void instructionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instructionsButtonActionPerformed
        acquireFields();
        final int insts = parseInstructionsTextField();
        if (insts > 0) {
            App.stepToInstructions(insts);
        } else {
            if (insts == 0 || insts == OUT_OF_RANGE) {
                displayError(this, "Enter a number greater-than 1.");
            } else {
                switch (insts) {
                    case EMPTY:
                        displayError(this,
                                "Please specify the number of instructions to execute.");
                        break;
                    case NOT_A_NUMBER:
                        displayError(this, "The specified value is not a valid number.");
                        break;
                }
            }
        }
    }//GEN-LAST:event_instructionsButtonActionPerformed

    private void instructionsTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instructionsTextFieldActionPerformed
        instructionsButtonActionPerformed(evt);
        requestFocusInWindow();
    }//GEN-LAST:event_instructionsTextFieldActionPerformed

    private void assemblerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assemblerButtonActionPerformed
        final List<Instruction> insts = instructions;
        if (insts != null && !insts.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final Instruction instruction : insts) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(instruction.getMachineCode());
            }
            App.createAsmDasmFrame();
            final AsmDasmFrame frame = App.getAsmDasmFrame();
            frame.setMachineCode(sb.toString());
            frame.disassemble();
        }
    }//GEN-LAST:event_assemblerButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        imageFrame.quickSaveState(saveSlotComboBox.getSelectedIndex() + 1);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        imageFrame.quickLoadState(saveSlotComboBox.getSelectedIndex() + 1);
    }//GEN-LAST:event_loadButtonActionPerformed

    private void saveSlotComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSlotComboBoxActionPerformed
        updateLoadButton();
    }//GEN-LAST:event_saveSlotComboBoxActionPerformed

    private void configLoggerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configLoggerButtonActionPerformed
        App.disposeTraceLogger();
        updateLoadButton();
        new LoggerDialog(this).setVisible(true);
    }//GEN-LAST:event_configLoggerButtonActionPerformed

    private void loggerToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerToggleButtonActionPerformed
        App.disposeTraceLogger();
        if (loggerToggleButton.isSelected()) {
            App.startTraceLogger();
        }
        updateLoggerButton();
    }//GEN-LAST:event_loggerToggleButtonActionPerformed

    private void stepToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepToTextFieldActionPerformed
        stepToButtonActionPerformed(evt);
        requestFocusInWindow();
    }//GEN-LAST:event_stepToTextFieldActionPerformed

    private void pcTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pcTextFieldActionPerformed
        acquirePC(true);
        refreshDebugTextArea(true);
        requestFocusInWindow();
    }//GEN-LAST:event_pcTextFieldActionPerformed

    private void pcTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pcTextFieldFocusLost
        acquirePC(true);
        refreshDebugTextArea(true);
    }//GEN-LAST:event_pcTextFieldFocusLost

    private void aTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aTextFieldActionPerformed
        acquireA(true);
        requestFocusInWindow();
    }//GEN-LAST:event_aTextFieldActionPerformed

    private void aTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_aTextFieldFocusLost
        acquireA(true);
    }//GEN-LAST:event_aTextFieldFocusLost

    private void xTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xTextFieldActionPerformed
        acquireX(true);
        requestFocusInWindow();
    }//GEN-LAST:event_xTextFieldActionPerformed

    private void xTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xTextFieldFocusLost
        acquireX(true);
    }//GEN-LAST:event_xTextFieldFocusLost

    private void yTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yTextFieldActionPerformed
        acquireY(true);
        requestFocusInWindow();
    }//GEN-LAST:event_yTextFieldActionPerformed

    private void yTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yTextFieldFocusLost
        acquireY(true);
    }//GEN-LAST:event_yTextFieldFocusLost

    private void sTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sTextFieldActionPerformed
        acquireS(true);
        requestFocusInWindow();
    }//GEN-LAST:event_sTextFieldActionPerformed

    private void sTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sTextFieldFocusLost
        acquireS(true);
    }//GEN-LAST:event_sTextFieldFocusLost

    private void unofficialOpcodesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unofficialOpcodesCheckBoxActionPerformed
        officialsOnly = !unofficialOpcodesCheckBox.isSelected();
        requestTextAreaRefresh();
    }//GEN-LAST:event_unofficialOpcodesCheckBoxActionPerformed

    private void ppuVTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppuVTextFieldActionPerformed
        acquirePpuV(true);
        requestFocusInWindow();
    }//GEN-LAST:event_ppuVTextFieldActionPerformed

    private void ppuVTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ppuVTextFieldFocusLost
        acquirePpuV(true);
    }//GEN-LAST:event_ppuVTextFieldFocusLost

    private void ppuTTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppuTTextFieldActionPerformed
        acquirePpuT(true);
        requestFocusInWindow();
    }//GEN-LAST:event_ppuTTextFieldActionPerformed

    private void ppuTTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ppuTTextFieldFocusLost
        acquirePpuT(true);
    }//GEN-LAST:event_ppuTTextFieldFocusLost

    private void ppuXTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppuXTextFieldActionPerformed
        acquirePpuX(true);
        requestFocusInWindow();
    }//GEN-LAST:event_ppuXTextFieldActionPerformed

    private void ppuXTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ppuXTextFieldFocusLost
        acquirePpuX(true);
    }//GEN-LAST:event_ppuXTextFieldFocusLost

    private void cameraXTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraXTextFieldActionPerformed
        acquireCameraX(true);
        requestFocusInWindow();
    }//GEN-LAST:event_cameraXTextFieldActionPerformed

    private void cameraXTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cameraXTextFieldFocusLost
        acquireCameraX(true);
    }//GEN-LAST:event_cameraXTextFieldFocusLost

    private void cameraYTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraYTextFieldActionPerformed
        acquireCameraY(true);
        requestFocusInWindow();
    }//GEN-LAST:event_cameraYTextFieldActionPerformed

    private void cameraYTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cameraYTextFieldFocusLost
        acquireCameraY(true);
    }//GEN-LAST:event_cameraYTextFieldFocusLost

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new DebuggerAppPrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
