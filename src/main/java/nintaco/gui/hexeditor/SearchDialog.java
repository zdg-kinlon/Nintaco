package nintaco.gui.hexeditor;

import nintaco.gui.CustomFocusTraversalPolicy;
import nintaco.gui.InformationDialog;
import nintaco.gui.hexeditor.preferences.Search;
import nintaco.preferences.AppPrefs;
import nintaco.util.EDT;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import static nintaco.gui.hexeditor.SearchQuery.*;

public class SearchDialog extends javax.swing.JDialog {

    private static final int MAX_REPLACES = 10_000;
    private static final String PROTOTYPE_DISPLAY_VALUE
            = "00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF 00";
    private HexEditorView hexEditorView;
    private CharTable charTable;
    private boolean hexSelected = true;
    private SearchText findText;
    private SearchText replaceText;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allViewsRadioButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JRadioButton currentViewRadioButton;
    private javax.swing.ButtonGroup dataButtonGroup;
    private javax.swing.ButtonGroup directionButtonGroup;
    private javax.swing.JRadioButton downRadioButton;
    private javax.swing.JComboBox findComboBox;
    private javax.swing.JButton findNextButton;
    private javax.swing.JLabel findWhatLabel;
    private javax.swing.JRadioButton hexRadioButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JCheckBox matchCaseCheckBox;
    private javax.swing.JCheckBox regExCheckBox;
    private javax.swing.JButton replaceAllButton;
    private javax.swing.JComboBox replaceComboBox;
    private javax.swing.JButton replaceNextButton;
    private javax.swing.JLabel replaceWithLabel;
    private javax.swing.ButtonGroup scopeButtonGroup;
    private javax.swing.JRadioButton selectionRadioButton;
    private javax.swing.JRadioButton textRadioButton;
    private javax.swing.JRadioButton upRadioButton;
    private javax.swing.JCheckBox wrapSearchesCheckBox;

    public SearchDialog(final java.awt.Frame parent) {
        super(parent, false);
        initComponents();
        findComboBox.setPrototypeDisplayValue(PROTOTYPE_DISPLAY_VALUE);
        replaceComboBox.setPrototypeDisplayValue(PROTOTYPE_DISPLAY_VALUE);
        addComboBoxListener(findComboBox, this::findComboBoxEdited);
        addComboBoxListener(replaceComboBox, this::replaceComboBoxEdited);
        getRootPane().setDefaultButton(findNextButton);
    }

    public void setData(final Data data) {
        if (data == Data.Hex) {
            hexRadioButton.setSelected(true);
        } else {
            textRadioButton.setSelected(true);
        }
        setConditionsMode(data == Data.Text);
    }

    public void setScope(final Scope scope) {
        switch (scope) {
            case CurrentView:
                currentViewRadioButton.setSelected(true);
                break;
            case Selection:
                selectionRadioButton.setSelected(true);
                break;
            case AllViews:
                allViewsRadioButton.setSelected(true);
                break;
        }
    }

    public void setFindWhat(final String findWhat) {
        setComboBoxText(findComboBox, findWhat);
    }

    public void setReplaceWith(final String replaceWith) {
        setComboBoxText(replaceComboBox, replaceWith);
    }

    private void setComboBoxText(final JComboBox comboBox, final String text) {
        comboBox.setSelectedItem(text);
        comboBox.getEditor().selectAll();
        comboBox.requestFocus();
    }

    private void updateComboBoxModel(final JComboBox comboBox,
                                     final List<SearchText> values) {
        final boolean hexString = hexRadioButton.isSelected();
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (final SearchText value : values) {
            model.addElement(value.toString(hexString, charTable));
        }
        comboBox.setModel(model);
    }

    private String getComboBoxText(final JComboBox comboBox) {
        final Object selectedItem = comboBox.getSelectedItem();
        return selectedItem == null ? "" : selectedItem.toString();
    }

    private void findComboBoxEdited(final String value) {
        findText = null;
        comboBoxesEdited(value, getComboBoxText(replaceComboBox));
    }

    private void replaceComboBoxEdited(final String value) {
        replaceText = null;
        comboBoxesEdited(getComboBoxText(findComboBox), value);
    }

    private void comboBoxesEdited(final String findValue,
                                  final String replaceValue) {
        findNextButton.setEnabled(!findValue.isEmpty());
        final boolean replaceEnabled
                = !(findValue.isEmpty() || replaceValue.isEmpty());
        replaceAllButton.setEnabled(replaceEnabled);
        replaceNextButton.setEnabled(replaceEnabled);
    }

    private void addComboBoxListener(final JComboBox comboBox,
                                     final TextListener listener) {
        comboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                String value = ((JTextComponent) ((JComboBox) ((Component) event
                        .getSource()).getParent()).getEditor().getEditorComponent())
                        .getText();
                if (hexRadioButton.isSelected()) {
                    value = value.trim();
                }
                listener.handleText(value);
            }
        });
    }

    public final void setShowReplace(final boolean replace) {
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy();
        replaceWithLabel.setVisible(replace);
        replaceComboBox.setVisible(replace);
        replaceNextButton.setVisible(replace);
        replaceAllButton.setVisible(replace);
        setTitle(replace ? "Replace" : "Find");
        policy.add(findComboBox);
        if (replace) {
            policy.add(replaceComboBox);
        }
        policy.add(hexRadioButton);
        policy.add(textRadioButton);
        policy.add(matchCaseCheckBox);
        policy.add(regExCheckBox);
        policy.add(wrapSearchesCheckBox);
        policy.add(upRadioButton);
        policy.add(downRadioButton);
        policy.add(currentViewRadioButton);
        policy.add(selectionRadioButton);
        policy.add(allViewsRadioButton);
        policy.add(findNextButton);
        if (replace) {
            policy.add(replaceNextButton);
            policy.add(replaceAllButton);
        }
        policy.add(closeButton);
        getContentPane().setFocusCycleRoot(true);
        getContentPane().setFocusTraversalPolicy(policy);
        revalidate();
        pack();
        repaint();
    }

    public void setCharTable(final CharTable charTable) {
        EDT.async(() -> {
            this.charTable = charTable;
            final Search search = AppPrefs.getInstance()
                    .getHexEditorPrefs().getSearch();
            updateComboBoxModel(findComboBox, search.getRecentFinds());
            updateComboBoxModel(replaceComboBox, search.getRecentReplaces());
        });
    }

    public void setHexEditorView(final HexEditorView hexEditorView) {
        this.hexEditorView = hexEditorView;
    }

    private void closeDialog() {
        setVisible(false);
    }

    private void setConditionsMode(final boolean text) {
        matchCaseCheckBox.setEnabled(text);
        regExCheckBox.setEnabled(text);

        if (findComboBox.getSelectedIndex() >= 0) {
            findText = null;
        } else if (findText == null) {
            final String value = getComboBoxText(findComboBox);
            if (!value.isEmpty()) {
                findText = new SearchText(value, hexSelected);
            }
        }

        if (replaceComboBox.getSelectedIndex() >= 0) {
            replaceText = null;
        } else if (replaceText == null) {
            final String value = getComboBoxText(replaceComboBox);
            if (!value.isEmpty()) {
                replaceText = new SearchText(value, hexSelected);
            }
        }

        final Search search = AppPrefs.getInstance().getHexEditorPrefs()
                .getSearch();
        updateComboBoxModel(findComboBox, search.getRecentFinds());
        updateComboBoxModel(replaceComboBox, search.getRecentReplaces());

        hexSelected = !text;
        if (findText != null) {
            findComboBox.setSelectedItem(findText.toString(hexSelected, charTable));
        }
        if (replaceText != null) {
            replaceComboBox.setSelectedItem(replaceText.toString(hexSelected,
                    charTable));
        }
    }

    private void updateComboBoxModels() {
        final Search search = AppPrefs.getInstance().getHexEditorPrefs()
                .getSearch();
        final boolean hex = hexRadioButton.isSelected();
        boolean save = false;
        String value = getComboBoxText(findComboBox);
        if (!value.isEmpty()) {
            save = true;
            search.addRecentFind(new SearchText(value, hex));
            updateComboBoxModel(findComboBox, search.getRecentFinds());
        }
        value = getComboBoxText(replaceComboBox);
        if (!value.isEmpty()) {
            save = true;
            search.addRecentReplace(new SearchText(value, hex));
            updateComboBoxModel(replaceComboBox, search.getRecentReplaces());
        }
        if (save) {
            AppPrefs.save();
        }
    }

    private void search(final SearchQuery.Type type) {

        final Search search = AppPrefs.getInstance().getHexEditorPrefs()
                .getSearch();
        final SearchQuery query = new SearchQuery(
                type,
                search.getMostRecentFind(),
                search.getMostRecentReplace(),
                hexRadioButton.isSelected() ? Data.Hex : Data.Text,
                upRadioButton.isSelected() ? Direction.Up : Direction.Down,
                currentViewRadioButton.isSelected() ? Scope.CurrentView
                        : selectionRadioButton.isSelected() ? Scope.Selection
                        : Scope.AllViews,
                matchCaseCheckBox.isSelected(),
                regExCheckBox.isSelected(),
                wrapSearchesCheckBox.isSelected());
        if (query.getFindWhat() == null || (type != SearchQuery.Type.FindNext
                && query.getReplaceWith() == null)) {
            return;
        }
        boolean showItemNotFound = true;
        if (query.getType() == SearchQuery.Type.ReplaceAll) {
            int count = 0;
            if (query.getScope() == Scope.Selection || !query.isWrapSearches()) {
                while (hexEditorView.search(query) != Searcher.NOT_FOUND
                        && ++count < MAX_REPLACES) {
                    showItemNotFound = false;
                }
            } else {
                final SearchQuery query2 = new SearchQuery(SearchQuery.Type.FindNext,
                        query.getFindWhat(), query.getReplaceWith(), query.getData(),
                        query.getDirection(), query.getScope(), query.isMatchCase(),
                        query.isRegularExpression(), query.isWrapSearches());
                final Searcher.Result result2 = hexEditorView.search(query2);
                if (result2 != Searcher.NOT_FOUND) {
                    showItemNotFound = false;
                    while (++count < MAX_REPLACES) {
                        final Searcher.Result result = hexEditorView.search(query);
                        if (result.equals(result2)) {
                            break;
                        }
                    }
                }
            }
        } else {
            showItemNotFound = hexEditorView.search(query) == Searcher.NOT_FOUND;
        }
        if (showItemNotFound) {
            new InformationDialog(this,
                    "End of view reached. The search item was not found.",
                    "Search Result", InformationDialog.IconType.INFORMATION)
                    .setVisible(true);
        }
    }

    public void find() {
        updateComboBoxModels();
        if (!getComboBoxText(findComboBox).isEmpty()) {
            search(SearchQuery.Type.FindNext);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        directionButtonGroup = new javax.swing.ButtonGroup();
        dataButtonGroup = new javax.swing.ButtonGroup();
        scopeButtonGroup = new javax.swing.ButtonGroup();
        findWhatLabel = new javax.swing.JLabel();
        findComboBox = new javax.swing.JComboBox();
        findNextButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        hexRadioButton = new javax.swing.JRadioButton();
        textRadioButton = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        upRadioButton = new javax.swing.JRadioButton();
        downRadioButton = new javax.swing.JRadioButton();
        closeButton = new javax.swing.JButton();
        replaceWithLabel = new javax.swing.JLabel();
        replaceComboBox = new javax.swing.JComboBox();
        replaceNextButton = new javax.swing.JButton();
        replaceAllButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        currentViewRadioButton = new javax.swing.JRadioButton();
        selectionRadioButton = new javax.swing.JRadioButton();
        allViewsRadioButton = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        matchCaseCheckBox = new javax.swing.JCheckBox();
        regExCheckBox = new javax.swing.JCheckBox();
        wrapSearchesCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Find");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        findWhatLabel.setText("Find what:");

        findComboBox.setEditable(true);
        findComboBox.setMinimumSize(null);
        findComboBox.setPreferredSize(null);
        findComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findComboBoxActionPerformed(evt);
            }
        });

        findNextButton.setMnemonic('F');
        findNextButton.setText("Find Next");
        findNextButton.setEnabled(false);
        findNextButton.setFocusPainted(false);
        findNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNextButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Data"));

        dataButtonGroup.add(hexRadioButton);
        hexRadioButton.setMnemonic('H');
        hexRadioButton.setSelected(true);
        hexRadioButton.setText("Hex");
        hexRadioButton.setFocusPainted(false);
        hexRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexRadioButtonActionPerformed(evt);
            }
        });

        dataButtonGroup.add(textRadioButton);
        textRadioButton.setMnemonic('T');
        textRadioButton.setText("Text");
        textRadioButton.setFocusPainted(false);
        textRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(hexRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textRadioButton)
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(hexRadioButton)
                                        .addComponent(textRadioButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Direction"));

        directionButtonGroup.add(upRadioButton);
        upRadioButton.setMnemonic('U');
        upRadioButton.setText("Up");
        upRadioButton.setFocusPainted(false);

        directionButtonGroup.add(downRadioButton);
        downRadioButton.setMnemonic('D');
        downRadioButton.setSelected(true);
        downRadioButton.setText("Down");
        downRadioButton.setFocusPainted(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(upRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(downRadioButton)
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(upRadioButton)
                                        .addComponent(downRadioButton))
                                .addContainerGap())
        );

        closeButton.setMnemonic('C');
        closeButton.setText("Close");
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        replaceWithLabel.setText("Replace with:");

        replaceComboBox.setEditable(true);
        replaceComboBox.setMinimumSize(null);
        replaceComboBox.setPreferredSize(null);
        replaceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceComboBoxActionPerformed(evt);
            }
        });

        replaceNextButton.setMnemonic('R');
        replaceNextButton.setText("Replace Next");
        replaceNextButton.setEnabled(false);
        replaceNextButton.setFocusPainted(false);
        replaceNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceNextButtonActionPerformed(evt);
            }
        });

        replaceAllButton.setMnemonic('A');
        replaceAllButton.setText("Replace All");
        replaceAllButton.setEnabled(false);
        replaceAllButton.setFocusPainted(false);
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllButtonActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Scope"));

        scopeButtonGroup.add(currentViewRadioButton);
        currentViewRadioButton.setSelected(true);
        currentViewRadioButton.setText("Current view");
        currentViewRadioButton.setFocusPainted(false);

        scopeButtonGroup.add(selectionRadioButton);
        selectionRadioButton.setText("Selection");
        selectionRadioButton.setFocusPainted(false);

        scopeButtonGroup.add(allViewsRadioButton);
        allViewsRadioButton.setText("All views");
        allViewsRadioButton.setFocusPainted(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(currentViewRadioButton)
                                        .addComponent(selectionRadioButton)
                                        .addComponent(allViewsRadioButton))
                                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(currentViewRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectionRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(allViewsRadioButton)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Conditions"));

        matchCaseCheckBox.setMnemonic('c');
        matchCaseCheckBox.setSelected(true);
        matchCaseCheckBox.setText("Match case");
        matchCaseCheckBox.setEnabled(false);
        matchCaseCheckBox.setFocusPainted(false);

        regExCheckBox.setMnemonic('e');
        regExCheckBox.setText("Regular expression");
        regExCheckBox.setEnabled(false);
        regExCheckBox.setFocusPainted(false);

        wrapSearchesCheckBox.setMnemonic('s');
        wrapSearchesCheckBox.setSelected(true);
        wrapSearchesCheckBox.setText("Wrap searches");
        wrapSearchesCheckBox.setFocusPainted(false);
        wrapSearchesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapSearchesCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(matchCaseCheckBox)
                                        .addComponent(regExCheckBox)
                                        .addComponent(wrapSearchesCheckBox))
                                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(matchCaseCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(regExCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(wrapSearchesCheckBox)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(18, 18, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(closeButton)
                                                        .addComponent(replaceAllButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(findWhatLabel)
                                                        .addComponent(replaceWithLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(findComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(replaceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(findNextButton)
                                                        .addComponent(replaceNextButton))))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, closeButton, findNextButton, replaceAllButton, replaceNextButton);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, jPanel1, jPanel5);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, jPanel2, jPanel3);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(findWhatLabel)
                                        .addComponent(findComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(findNextButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(replaceWithLabel)
                                        .addComponent(replaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(replaceNextButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                                .addGap(69, 69, 69)
                                .addComponent(replaceAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(closeButton)
                                .addGap(11, 11, 11))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void findNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findNextButtonActionPerformed
        find();
    }//GEN-LAST:event_findNextButtonActionPerformed

    private void replaceNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceNextButtonActionPerformed
        updateComboBoxModels();
        if (!(getComboBoxText(findComboBox).isEmpty()
                || getComboBoxText(replaceComboBox).isEmpty())) {
            search(SearchQuery.Type.ReplaceNext);
        }
    }//GEN-LAST:event_replaceNextButtonActionPerformed

    private void replaceAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllButtonActionPerformed
        updateComboBoxModels();
        if (!(getComboBoxText(findComboBox).isEmpty()
                || getComboBoxText(replaceComboBox).isEmpty())) {
            search(SearchQuery.Type.ReplaceAll);
        }
    }//GEN-LAST:event_replaceAllButtonActionPerformed

    private void hexRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexRadioButtonActionPerformed
        setConditionsMode(false);
    }//GEN-LAST:event_hexRadioButtonActionPerformed

    private void textRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textRadioButtonActionPerformed
        setConditionsMode(true);
    }//GEN-LAST:event_textRadioButtonActionPerformed

    private void wrapSearchesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrapSearchesCheckBoxActionPerformed
        if (wrapSearchesCheckBox.isSelected()) {
            allViewsRadioButton.setEnabled(true);
        } else {
            if (allViewsRadioButton.isSelected()) {
                currentViewRadioButton.setSelected(true);
            }
            allViewsRadioButton.setEnabled(false);
        }
    }//GEN-LAST:event_wrapSearchesCheckBoxActionPerformed

    private void findComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findComboBoxActionPerformed
        comboBoxesEdited(getComboBoxText(findComboBox),
                getComboBoxText(replaceComboBox));
    }//GEN-LAST:event_findComboBoxActionPerformed

    private void replaceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceComboBoxActionPerformed
        comboBoxesEdited(getComboBoxText(findComboBox),
                getComboBoxText(replaceComboBox));
    }//GEN-LAST:event_replaceComboBoxActionPerformed
    private interface TextListener {
        void handleText(String text);
    }
    // End of variables declaration//GEN-END:variables
}
