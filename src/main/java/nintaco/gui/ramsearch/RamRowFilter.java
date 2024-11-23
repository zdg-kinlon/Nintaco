package nintaco.gui.ramsearch;

public interface RamRowFilter {
    boolean filter(int current, int previous, int changes, int address, int a,
                   int b);
}
