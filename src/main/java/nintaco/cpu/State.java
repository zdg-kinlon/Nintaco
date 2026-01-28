package nintaco.cpu;

import java.util.concurrent.atomic.AtomicInteger;

public class State {
    private long cycleCounter;
    private boolean running = true;
    private AtomicInteger instructionsCounter = new AtomicInteger(0);
    private int dmcCycle;
    private int dmcAddress;

    public long cycleCounter() {
        return cycleCounter;
    }

    public void cycleCounterInc() {
        cycleCounter++;
    }

    public boolean running() {
        return running;
    }

    public void running(boolean running) {
        this.running = running;
    }

    public int instructionsCounter() {
        return instructionsCounter.get();
    }

    public int instructionsCounterInc() {
        return instructionsCounter.incrementAndGet();
    }

    public void setDmcCycle(int dmcCycle) {
        this.dmcCycle = dmcCycle;
    }

    public boolean hasDmcCycle() {
        return dmcCycle > 0;
    }

    public int dmcCycleDec() {
        return dmcCycle > 0 ? --dmcCycle : (dmcCycle = 0);
    }

    public int dmcAddress() {
        return dmcAddress;
    }

    public void dmcAddress(int dmcAddress) {
        this.dmcAddress = dmcAddress;
    }
}
