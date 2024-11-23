package nintaco.files;

public interface Console {

    int REGULAR = 0;
    int VS_SYSTEM = 1;
    int PLAYCHOICE_10 = 2;
    int EXTENDED = 3;

    String[] NAMES = {
            "Regular NES/Famicom/Dendy",  // 0
            "VS. System",                 // 1
            "PlayChoice-10",              // 2
            "Extended Console Type",      // 3
    };

    static String toString(final int console) {
        return (console < 0 || console >= NAMES.length) ? "Other"
                : NAMES[console];
    }
}
