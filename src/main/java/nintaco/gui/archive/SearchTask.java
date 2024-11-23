package nintaco.gui.archive;

import nintaco.task.Task;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nintaco.util.GuiUtil.scrollToCenter;
import static nintaco.util.StringUtil.isBlank;

public class SearchTask extends Task {

    public static final int BOLD_THRESHOLD = 64;

    protected final JList<EntryElement> list;
    protected final List<String> entries;
    protected final String searchStr;

    public SearchTask(final JList<EntryElement> list, final List<String> entries,
                      final String searchStr) {
        this.list = list;
        this.entries = entries;
        this.searchStr = searchStr;
    }

    protected Pattern createPattern(String searchStr) {
        searchStr = searchStr.trim();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchStr.length(); i++) {
            final char c = searchStr.charAt(i);
            if (Character.isWhitespace(c)) {
                sb.append("\\s");
            } else {
                switch (c) {
                    case '.':
                    case '^':
                    case '$':
                    case '+':
                    case '-':
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case '{':
                    case '}':
                    case '\\':
                    case '|':
                        sb.append('\\').append(c);
                        break;
                    case '*':
                        sb.append(".*?");
                        break;
                    case '?':
                        sb.append(".");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void loop() {
        final DefaultListModel<EntryElement> model = new DefaultListModel<>();
        if (isBlank(searchStr)) {
            for (final String entry : entries) {
                if (canceled) {
                    return;
                }
                model.addElement(new EntryElement(entry, entry));
            }
        } else {
            final Pattern pattern = createPattern(searchStr);
            for (final String entry : entries) {
                if (canceled) {
                    return;
                } else if (pattern.matcher(entry).find()) {
                    model.addElement(new EntryElement(entry, entry));
                }
            }
            if (model.size() < BOLD_THRESHOLD) {
                final StringBuilder sb = new StringBuilder();
                for (int i = model.size() - 1; i >= 0; i--) {
                    if (canceled) {
                        return;
                    }
                    final EntryElement element = model.getElementAt(i);
                    final String entry = element.getEntry();
                    sb.setLength(0);
                    sb.append("<html>");
                    int index = 0;
                    final Matcher matcher = pattern.matcher(entry);
                    while (matcher.find() && !canceled) {
                        final int start = matcher.start();
                        final int end = matcher.end();
                        if (start > index) {
                            sb.append(entry, index, start);
                        }
                        index = end;
                        sb.append("<b>").append(entry, start, end).append("</b>");
                    }
                    if (index == 0) {
                        continue;
                    } else if (index < entry.length()) {
                        sb.append(entry.substring(index));
                    }
                    sb.append("</html>");
                    element.setEntryHTML(sb.toString());
                }
            }
        }
        if (!canceled) {
            EventQueue.invokeLater(() -> updateModel(model));
        }
    }

    protected void updateModel(final DefaultListModel<EntryElement> model) {
        final EntryElement selectedElement = list.getSelectedValue();
        int selectedIndex = -1;
        if (selectedElement != null) {
            final String entry = selectedElement.getEntry();
            for (int i = model.size() - 1; i >= 0; i--) {
                if (model.get(i).getEntry().equals(entry)) {
                    selectedIndex = i;
                    break;
                } else if (canceled) {
                    return;
                }
            }
        }
        if (!canceled) {
            list.setModel(model);
            if (selectedIndex < 0 && model.getSize() == 1) {
                selectedIndex = 0;
            }
            if (selectedIndex >= 0) {
                list.setSelectedIndex(selectedIndex);
                final int index = selectedIndex;
                EventQueue.invokeLater(() -> scrollToCenter(list, index));
            } else {
                list.clearSelection();
            }
        }
    }
}
