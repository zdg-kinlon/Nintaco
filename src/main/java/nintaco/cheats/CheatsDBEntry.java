package nintaco.cheats;

public class CheatsDBEntry {

    private final String description;
    private final String[][] gameGenieCodes;

    public CheatsDBEntry(final String description,
                         final String[][] gameGenieCodes) {
        this.description = description;
        this.gameGenieCodes = gameGenieCodes;
    }

    public String getDescription() {
        return description;
    }

    public String[][] getGameGenieCodes() {
        return gameGenieCodes;
    }
}
