package nintaco.gui.rob;

import java.io.Serializable;

import static nintaco.gui.rob.RobGame.GYROMITE;
import static nintaco.gui.rob.RobGame.NONE;

public class RobState implements Serializable {

    private static final long serialVersionUID = 0;

    // X-coordinates -2.0 (left) -- 2.0 (right)    

    // Y-coordinates 0.0 (top) -- 5.0 (bottom)
    // Gyromite: 0, 2, 4 only 

    public final float[][] pieces = new float[5][2]; // X, Y pairs

    public int modifications;
    public int game = NONE;
    public float handsX;
    public float handsY;
    public float handsOpen;
    public float aButtonY;
    public float bButtonY;
    public boolean testing;

    public RobState() {
    }

    public RobState(final RobState source) {
        init(source);
    }

    public void init(final RobState source) {
        modifications = source.modifications;
        game = source.game;
        if (game != NONE) {
            handsX = source.handsX;
            handsY = source.handsY;
            handsOpen = source.handsOpen;
            aButtonY = source.aButtonY;
            bButtonY = source.bButtonY;
            testing = source.testing;
            for (int i = source.game == GYROMITE ? 2 : 4; i >= 0; i--) {
                pieces[i][0] = source.pieces[i][0];
                pieces[i][1] = source.pieces[i][1];
            }
        }
    }

    public void reset() {

        modifications = 0;

        handsOpen = 1f;
        handsX = 0f;
        handsY = 0f;
        testing = false;

        if (game == GYROMITE) {
            aButtonY = 3f;
            bButtonY = 3f;

            pieces[0][0] = -2f;
            pieces[0][1] = 4f;

            pieces[1][0] = -1f;
            pieces[1][1] = 4f;

            pieces[2][0] = 2f;
            pieces[2][1] = 2f;
        } else {
            for (int i = 4; i >= 0; i--) {
                pieces[i][0] = 0f;
                pieces[i][1] = i + 1;
            }
        }
    }
}
