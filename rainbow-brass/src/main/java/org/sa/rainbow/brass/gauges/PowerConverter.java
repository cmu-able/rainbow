package org.sa.rainbow.brass.gauges;

public class PowerConverter {
    public static final int       MIN_VOLTAGE                = 104;
    public static final int       MAX_VOLTAGE                = 166;
    public static final double    BATTERY_CAPACITY /* mwh */ = 32560.0;
    private static final double[] percent_of_v               = new double[] { 5.941770647654998e-05,
            0.0005941770647652778, 0.0011289364230541166, 0.0017231134878193943, 0.002376708259061222,
            0.0029708853238265, 0.0036838978015448776, 0.004278074866310155, 0.005050505050505083, 0.00588235294117645,
            0.006654783125371377, 0.007546048722519294, 0.00855614973262031, 0.009566250742721327, 0.010635769459298894,
            0.011883541295306, 0.013309566250742755, 0.01479500891265595, 0.016280451574569255, 0.0179441473559121,
            0.02032085561497321, 0.022162804515745704, 0.025074272133095654, 0.027094474153297687, 0.030540701128936476,
            0.033333333333333326, 0.037017231134878203, 0.041592394533571, 0.04771241830065365, 0.05846702317290553,
            0.0682115270350564, 0.08787878787878789, 0.11556743909685085, 0.1557338086749851, 0.17700534759358288,
            0.21152703505644677, 0.26215092097445036, 0.32739156268568037, 0.39185977421271534, 0.4639928698752228,
            0.5144385026737968, 0.5621509209744504, 0.6068924539512774, 0.6352346999405822, 0.6746286393345217,
            0.6897801544860369, 0.7156268568033274, 0.7393939393939394, 0.7710635769459299, 0.7874628639334522,
            0.8143196672608437, 0.8401069518716577, 0.8636957813428402, 0.8871657754010696, 0.9098039215686274,
            0.9406417112299466, 0.9527035056446821, 0.9739156268568033, 0.9932857991681521, 0.9998217468805705, 1.0,
            1.0, 1.0 };

    private static final int     NUM_V_DATA = 16830;

//    private static short[] v_data = new short[16380];
//    static {
//        // Copy over split up data model to counter 64k static initializer limitation
//        System.out.println ("Initializing...");
//        System.arraycopy (V_DATA_1.v_data, 0, v_data, 0, V_DATA_1.v_data.length);
//        System.arraycopy (V_DATA_2.v_data, 0, v_data, V_DATA_1.v_data.length, V_DATA_2.v_data.length);
//        System.arraycopy (V_DATA_3.v_data, 0, v_data, V_DATA_1.v_data.length + V_DATA_2.v_data.length,
//                V_DATA_3.v_data.length);
//    }
//
//    public static short charge2Voltage (double charge) {
//        double pct = charge / BATTERY_CAPACITY;
//        double idx_d = pct * NUM_V_DATA;
//        long idx = Math.round (idx_d);
//        return v_data[(int )idx];
//    }

    public static double voltage2Charge (int voltage) {
        double pct = percent_of_v[voltage - MIN_VOLTAGE];
        return pct * BATTERY_CAPACITY;
    }

    public static void main (String[] args) {
        System.out.println ("Voltage 164 = " + voltage2Charge (164) + ", should be " + BATTERY_CAPACITY);
        System.out.println ("Voltage " + MIN_VOLTAGE + " = " + voltage2Charge (MIN_VOLTAGE) + ", should be 0");

        for (int i = MIN_VOLTAGE; i <= MAX_VOLTAGE; i++) {
            System.out.println ("Voltage " + i + " = " + voltage2Charge (i));
        }
    }
}
