package nintaco.cartdb;

import nintaco.tv.TVSystem;

import java.io.Serializable;

public class Cart implements Serializable {

    private static final long serialVersionUID = 0;

    private final int crc;
    private final int mapper;
    private final int submapper;
    private final TVSystem tvSystem;
    private final int device;
    private final int mirroring;
    private final boolean lagButtons;

    public Cart(
            final int crc,
            final int mapper,
            final int submapper,
            final TVSystem tvSystem,
            final int device,
            final int mirroring,
            final boolean lagButtons) {
        this.crc = crc;
        this.mapper = mapper;
        this.submapper = submapper;
        this.tvSystem = tvSystem;
        this.device = device;
        this.mirroring = mirroring;
        this.lagButtons = lagButtons;
    }

    public int getCRC() {
        return crc;
    }

    public int getMapper() {
        return mapper;
    }

    public int getSubmapper() {
        return submapper;
    }

    public TVSystem getTVSystem() {
        return tvSystem;
    }

    public int getDevice() {
        return device;
    }

    public int getMirroring() {
        return mirroring;
    }

    public boolean isLagButtons() {
        return lagButtons;
    }

    @Override
    public String toString() {
        return String.format("crc = %08X, mapper = %d, submapper = %d, "
                        + "tvSystem = %s, device = %d, mirroring = %d, lagButtons = %b", crc,
                mapper, submapper, tvSystem, device, mirroring, lagButtons);
    }
}
