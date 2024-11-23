package nintaco.gui.ramsearch;

public class RamRow {

    protected int current;
    protected int prior;
    protected int changes;
    protected boolean flagged;

    public void resetChanges() {
        setChanges(0);
    }

    public void incrementChanges() {
        setChanges(changes + 1);
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getPrior() {
        return prior;
    }

    public void setPrior(int prior) {
        this.prior = prior;
    }

    public int getChanges() {
        return changes;
    }

    public void setChanges(int changes) {
        this.changes = changes;
    }
}
