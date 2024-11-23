package nintaco.gui.historyeditor;

import nintaco.gui.historyeditor.change.HistoryChange;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ChangeListModel extends AbstractListModel<HistoryChange> {

    private final List<HistoryChange> changes = new ArrayList<>();

    private int changesIndex;

    public List<HistoryChange> getChanges() {
        return new ArrayList<>(changes);
    }

    public void setChanges(final List<HistoryChange> changes,
                           final int changesIndex) {
        this.changesIndex = changesIndex;
        final int priorLastIndex = this.changes.size() - 1;
        this.changes.clear();
        if (priorLastIndex >= 0) {
            fireIntervalRemoved(this, 0, priorLastIndex);
        }
        this.changes.addAll(changes);
        fireIntervalAdded(this, 0, this.changes.size() - 1);
    }

    public int getChangesIndex() {
        return changesIndex;
    }

    public void setChangesIndex(final int changesIndex) {

        final int minChangesIndex;
        final int maxChangesIndex;

        if (changesIndex > this.changesIndex) {
            minChangesIndex = this.changesIndex;
            maxChangesIndex = changesIndex;
        } else {
            minChangesIndex = changesIndex;
            maxChangesIndex = this.changesIndex;
        }

        this.changesIndex = changesIndex;
        fireContentsChanged(this, minChangesIndex, maxChangesIndex);
    }

    public void incrementChangesIndex() {
        setChangesIndex(changesIndex + 1);
    }

    public void decrementChangesIndex() {
        if (changesIndex > 0) {
            setChangesIndex(changesIndex - 1);
        }
    }

    public void addChange(final HistoryChange change) {
        final int priorSize = changes.size();
        while (changes.size() > changesIndex) {
            changes.remove(changes.size() - 1);
        }
        if (priorSize > changes.size()) {
            fireIntervalRemoved(this, changes.size(), priorSize - 1);
        }
        changes.add(change);
        fireIntervalAdded(this, changes.size() - 1, changes.size() - 1);
        setChangesIndex(changes.size());
    }

    public void clear() {

        if (changes.isEmpty()) {
            return;
        }

        if (changes.size() > 1) {
            changesIndex = 1;
            final HistoryChange change = changes.get(0);
            final int lastIndex = changes.size() - 1;
            changes.clear();
            changes.add(change);
            fireContentsChanged(this, 0, lastIndex);
        }
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    @Override
    public int getSize() {
        return changes.size();
    }

    @Override
    public HistoryChange getElementAt(final int index) {
        return changes.get(index);
    }
}
