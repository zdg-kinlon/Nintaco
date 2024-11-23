package nintaco.cartdb;

import nintaco.files.FileUtil;
import nintaco.tv.TVSystem;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;

import static nintaco.tv.TVSystem.*;

public final class CartDB {

    private static final Map<Integer, Cart> carts = new HashMap<>();

    private static volatile boolean enabled = true;

    private CartDB() {
    }

    public static void init() {
        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(
                FileUtil.getResourceAsStream("/db/cart-db.dat")))) {
            while (true) {
                try {
                    final int crc = in.readInt();
                    int mapper = in.readUnsignedByte();
                    int submapper = in.readUnsignedByte();
                    mapper |= (submapper & 0xC0) << 2;
                    submapper &= 0x3F;
                    final TVSystem tvSystem = getTVSystem(in.readUnsignedByte());
                    final int device = in.readByte();
                    final int mirroring = in.readByte();
                    carts.put(crc, new Cart(crc, mapper, submapper, tvSystem, device,
                            mirroring, isLagButtons(crc)));
                } catch (final EOFException eof) {
                    break;
                }
            }
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
    }

    private static boolean isLagButtons(final int crc) {
        switch (crc) {
            // Spot options menu malfunctions without a strobe lag.
            case 0x0ABDD5CA: // Spot (J)
            case 0xCFAE9DFA: // Spot (U) [!]
                return true;
            // Quattro Sports BMX Simulator will not start without a strobe lag.
            case 0x70128EBB: // Pro Tennis (Quattro Sports Hack)
            case 0xCCCAF368: // Quattro Sports (Camerica) (Aladdin) [!]
            case 0x62EF6C79: // Quattro Sports (Camerica) (Aladdin) [b1]
            case 0x0526FF0D: // Quattro Sports (Camerica) (V3 Plug-Thru Cart) [b1]
            case 0xB462718E: // Quattro Sports (Camerica) (V3 Plug-Thru Cart)
            case 0xA045FE1D: // Super Sports Challenge (CodeMasters) (V2 ...)
                return true;
            default:
                return false;
        }
    }

    private static TVSystem getTVSystem(final int tvSystem) {
        switch (tvSystem) {
            case CartTVSystem.NTSC:
                return NTSC;
            case CartTVSystem.PAL:
                return PAL;
            default:
                return Dendy;
        }
    }

    public static synchronized Cart getCart(final int crc) {
        return carts.get(crc);
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized void setEnabled(final boolean enabled) {
        CartDB.enabled = enabled;
    }
}
