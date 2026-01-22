package cn.kinlon.emu.cpu;

import cn.kinlon.emu.ServicedType;

public class Interrupt {

    private boolean resetRequested;
    private boolean nmiRequested;
    private int irqRequested;
    private boolean triggerNMI;
    private boolean triggerIRQ;
    private ServicedType serviced;

    public void resetRequested(boolean resetRequested) {
        this.resetRequested = resetRequested;
    }

    public void nmiRequested(boolean nmiRequested) {
        this.nmiRequested = nmiRequested;
    }

    public void irqRequested(int irqRequested) {
        this.irqRequested = irqRequested;
    }

    public void triggerNMI(boolean triggerNMI) {
        this.triggerNMI = triggerNMI;
    }

    public void triggerIRQ(boolean triggerIRQ) {
        this.triggerIRQ = triggerIRQ;
    }

    public void serviced(ServicedType serviced) {
        this.serviced = serviced;
    }

    public boolean resetRequested() {
        return resetRequested;
    }

    public boolean nmiRequested() {
        return nmiRequested;
    }

    public int irqRequested() {
        return irqRequested;
    }

    public boolean triggerNMI() {
        return triggerNMI;
    }

    public boolean triggerIRQ() {
        return triggerIRQ;
    }

    public ServicedType serviced() {
        return serviced;
    }
}
