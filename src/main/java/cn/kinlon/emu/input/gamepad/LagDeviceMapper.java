package cn.kinlon.emu.input.gamepad;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.PPU;
import cn.kinlon.emu.cartdb.Cart;
import cn.kinlon.emu.cartdb.CartDB;
import cn.kinlon.emu.input.DeviceMapper;

import java.io.Serializable;

import static cn.kinlon.emu.App.getCart;

public abstract class LagDeviceMapper extends DeviceMapper
        implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int LAG_SCANLINE = 128;

    protected int buttons;

    private int buttonsBuffer;
    private boolean lagButtons;
    private volatile PPU ppu;

    public LagDeviceMapper() {
        final Cart cart = getCart();
        if (cart != null && CartDB.isEnabled()) {
            lagButtons = cart.isLagButtons();
        }
    }

    @Override
    public void setMachine(final Machine machine) {
        if (machine == null) {
            ppu = null;
        } else {
            ppu = machine.getPPU();
        }
    }

    @Override
    public void update(final int buttons) {
        if (lagButtons) {
            this.buttons = buttonsBuffer;
            this.buttonsBuffer = buttons;
        } else {
            this.buttons = buttons;
        }
    }

    protected void updateButtons() {
        if (lagButtons) {
            final PPU p = ppu;
            if (p != null && p.getScanline() >= LAG_SCANLINE) {
                buttons = buttonsBuffer;
            }
        }
    }
}
