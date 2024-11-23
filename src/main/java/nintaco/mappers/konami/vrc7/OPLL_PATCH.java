package nintaco.mappers.konami.vrc7;

import java.io.Serializable;

public final class OPLL_PATCH implements Serializable {

    private static final long serialVersionUID = 0;

    public int TL;
    public int FB;
    public int EG;
    public int ML;
    public int AR;
    public int DR;
    public int SL;
    public int RR;
    public int KR;
    public int KL;
    public int AM;
    public int PM;
    public int WF;

    public static void copy(OPLL_PATCH source, OPLL_PATCH destination) {
        destination.TL = source.TL;
        destination.FB = source.FB;
        destination.EG = source.EG;
        destination.ML = source.ML;
        destination.AR = source.AR;
        destination.DR = source.DR;
        destination.SL = source.SL;
        destination.RR = source.RR;
        destination.KR = source.KR;
        destination.KL = source.KL;
        destination.AM = source.AM;
        destination.PM = source.PM;
        destination.WF = source.WF;
    }
}
