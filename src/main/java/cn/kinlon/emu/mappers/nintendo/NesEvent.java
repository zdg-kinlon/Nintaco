package cn.kinlon.emu.mappers.nintendo;

import cn.kinlon.emu.files.CartFile;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class NesEvent extends MMC1 {

    private static final long serialVersionUID = 0;

    private static final int DIP_SWITCH_SETTING = 0b0100;
    private static final int IRQ_VALUE = 0x20000000 | (DIP_SWITCH_SETTING << 25);

    private int updatesEnableCounter = 2;
    private int prgRegA;
    private int irqCounter;
    private boolean irqEnabled;
    private boolean prgMode;
    private boolean updatesEnabled;

    public NesEvent(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        super.init();
        setPrgBank(2, 0);
        setPrgBank(3, 1);
    }

    @Override
    protected void writeChrBankReg(final int bank, final int value) {
        if (bank == 0) {
            irqEnabled = !getBitBool(value, 4);
            prgMode = getBitBool(value, 3);
            prgRegA = value & 0x06;

            if (!irqEnabled) {
                irqCounter = 0;
                cpu.setMapperIrq(false);
                if (updatesEnableCounter == 1) {
                    updatesEnableCounter = 0;
                    updatesEnabled = true;
                }
            } else if (updatesEnableCounter == 2) {
                updatesEnableCounter = 1;
            }

            updateBanks();
        }
    }

    @Override
    protected void writePrgBankReg(final int value) {
        prgBankReg = (value & 0x07) | 0x08;
        prgRamEnabled = !getBitBool(value, 4);
        updateBanks();
    }

    @Override
    public void update() {
        if (irqEnabled && ++irqCounter == IRQ_VALUE) {
            cpu.setMapperIrq(true);
            irqEnabled = false;
            irqCounter = 0;
        }
    }

    @Override
    protected void updateBanks() {
        if (updatesEnabled) {
            if (prgMode) {
                super.updateBanks();
            } else {
                setPrgBank(2, prgRegA);
                setPrgBank(3, prgRegA | 1);
            }
        }
    }
}