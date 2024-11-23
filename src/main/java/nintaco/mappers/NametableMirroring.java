package nintaco.mappers;

public interface NametableMirroring {

    int MAPPER_CONTROLLED = -1;

    int VERTICAL = 0;
    int HORIZONTAL = 1;
    int ONE_SCREEN_A = 2;
    int ONE_SCREEN_B = 3;
    int FOUR_SCREEN = 4;
    int DIAGONAL = 5;
    int L_SHAPED = 6;
    int R_SHAPED = 7;

    static String toString(int mirroring) {
        switch (mirroring) {
            case VERTICAL:
                return "Vertical";
            case HORIZONTAL:
                return "Horizontal";
            case ONE_SCREEN_A:
                return "1-Screen A";
            case ONE_SCREEN_B:
                return "1-Screen B";
            case FOUR_SCREEN:
                return "4-Screen";
            case DIAGONAL:
                return "Diagonal";
            case L_SHAPED:
                return "L-Shaped";
            case R_SHAPED:
                return "R-Shaped";
            default:
                return "Mapper Controlled";
        }
    }
}
