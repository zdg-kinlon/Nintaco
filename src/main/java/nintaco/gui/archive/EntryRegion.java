package nintaco.gui.archive;

import java.util.Locale;

public enum EntryRegion {

    Australia("A", "Australia"),
    Asia("As", "Asia"),
    Brazil("B", "Brazil"),
    Canada("C", "Canada"),
    China("Ch", "China"),
    DutchNetherlands("D", "Dutch (Netherlands)"),
    Europe("E", "Europe"),
    France("F", "France"),
    Germany("G", "Germany"),
    Greece("Gr", "Greece"),
    HongKong("HK", "Hong Kong"),
    Italy("I", "Italy"),
    Japan("J", "Japan"),
    JapanEurope("JE", "Japan & Europe"),
    JapanUSA("JU", "Japan & USA"),
    JapanUSAEurope("JUE", "Japan, USA & Europe"),
    Korea("K", "Korea"),
    Netherlands("Nl", "Netherlands"),
    Norway("No", "Norway"),
    PublicDomain("PD", "Public domain"),
    Russia("R", "Russia"),
    Spain("S", "Spain"),
    Sweden("Sw", "Sweden"),
    USA("U", "USA"),
    USAEurope("UE", "USA & Europe"),
    UnitedKingdom("UK", "United Kingdom"),
    Unknown("Unk", "Unknown country"),
    Unlicensed("Unl", "Unlicensed"),
    World("W", "World");

    private static final String[] DEFAULT_REGIONS = {"(uk)", "(e)", "(ch)",
            "(unl)", "(je)", "(j)", "(jue)", "(ue)", "(ju)", "(u)", "(w)", "",};

    private static final String[] CHINA_REGIONS = {"(uk)", "(e)", "(je)",
            "(j)", "(jue)", "(ue)", "(ju)", "(u)", "(w)", "(unl)", "(ch)",};
    private static final String[] EUROPE_REGIONS = {"(ch)", "(unl)",
            "(j)", "(ju)", "(u)", "(w)", "(jue)", "(je)", "(ue)", "(uk)", "(e)",};
    private static final String[] JAPAN_REGIONS = {"(uk)", "(e)", "(ch)",
            "(unl)", "(ue)", "(w)", "(u)", "(je)", "(jue)", "(ju)", "(j)",};
    private static final String[] JAPAN_EUROPE_REGIONS = {"(ch)", "(unl)",
            "(u)", "(w)", "(ue)", "(uk)", "(e)", "(ju)", "(j)", "(jue)", "(je)",};
    private static final String[] JAPAN_USA_REGIONS = {"(ch)", "(unl)", "(uk)",
            "(e)", "(w)", "(ue)", "(u)", "(je)", "(j)", "(jue)", "(ju)",};
    private static final String[] JAPAN_USA_EUROPE_REGIONS = {"(ch)", "(unl)",
            "(uk)", "(e)", "(w)", "(ue)", "(u)", "(je)", "(j)", "(ju)", "(jue)",};
    private static final String[] USA_REGIONS = {"(uk)", "(e)", "(ch)",
            "(unl)", "(je)", "(j)", "(w)", "(ue)", "(jue)", "(ju)", "(u)",};
    private static final String[] USA_EUROPE_REGIONS = {"(ch)", "(unl)", "(j)",
            "(w)", "(je)", "(ju)", "(uk)", "(e)", "(u)", "(jue)", "(ue)",};
    private static final String[] UNITED_KINGDOM_REGIONS = {"(ch)", "(unl)",
            "(j)", "(ju)", "(u)", "(w)", "(jue)", "(je)", "(ue)", "(e)", "(uk)",};
    private static final String[] UNLICENSED_REGIONS = {"(uk)", "(e)", "(je)",
            "(j)", "(jue)", "(ue)", "(ju)", "(u)", "(w)", "(ch)", "(unl)",};
    private static final String[] WORLD_REGIONS = {"(uk)", "(e)", "(ch)",
            "(unl)", "(je)", "(j)", "(ue)", "(jue)", "(ju)", "(u)", "(w)",};
    private final String code;
    private final String name;
    EntryRegion(final String code, final String name) {
        this.name = name;
        this.code = code;
    }

    public static String[] getPrioritizedRegions(final EntryRegion entryRegion) {
        switch (entryRegion) {
            case China:
                return CHINA_REGIONS;
            case Europe:
                return EUROPE_REGIONS;
            case Japan:
                return JAPAN_REGIONS;
            case JapanEurope:
                return JAPAN_EUROPE_REGIONS;
            case JapanUSA:
                return JAPAN_USA_REGIONS;
            case JapanUSAEurope:
                return JAPAN_USA_EUROPE_REGIONS;
            case USA:
                return USA_REGIONS;
            case USAEurope:
                return USA_EUROPE_REGIONS;
            case UnitedKingdom:
                return UNITED_KINGDOM_REGIONS;
            case Unlicensed:
                return UNLICENSED_REGIONS;
            case World:
                return WORLD_REGIONS;
            default:
                DEFAULT_REGIONS[DEFAULT_REGIONS.length - 1] = String.format("(%s)",
                        entryRegion.getCode().toLowerCase(Locale.ENGLISH));
                return DEFAULT_REGIONS;
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
