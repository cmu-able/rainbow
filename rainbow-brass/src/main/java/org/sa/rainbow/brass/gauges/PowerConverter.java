package org.sa.rainbow.brass.gauges;

public class PowerConverter {
    public static final int       MIN_VOLTAGE                = 104;
    public static final int       MAX_VOLTAGE                = 166;
    public static final double    BATTERY_CAPACITY /* mwh */ = 32560.0;
    private static final double[] percent_of_v               = new double[] { 2.970885323827499e-05, 0.0002970885323826389, 0.0006535947712418277, 0.005555555555555591, 0.01752822341057636, 0.027005347593582862, 0.037849079025549626, 0.0497623291740939, 0.061467617349970305, 0.07338086749851452, 0.08796791443850266, 0.09910873440285206, 0.1126559714795009, 0.12849079025549615, 0.1387106357694593, 0.152020202020202, 0.1690433749257279, 0.17964943553178847, 0.1952762923351159, 0.21197266785502084, 0.23351158645276288, 0.2579025549613785, 0.29780154486036836, 0.3344622697563874, 0.3665478312537136, 0.40029708853238266, 0.41847890671420085, 0.4347890671420083, 0.4619132501485443, 0.4748366013071895, 0.49313725490196075, 0.5114676173499704, 0.5308080808080808, 0.5545454545454545, 0.5677064765300059, 0.5873737373737373, 0.614468211527035, 0.6487225193107546, 0.6823826500297088, 0.7197860962566844, 0.7462269756387403, 0.77106357694593, 0.7944741532976827, 0.8094771241830065, 0.8299762329174094, 0.8382947118241236, 0.8519310754604872, 0.8643790849673203, 0.8807486631016043, 0.889453357100416, 0.9033868092691621, 0.9167260843731432, 0.9289067142008318, 0.9410576351752822, 0.9527629233511586, 0.9684789067142008, 0.9748663101604278, 0.985769459298871, 0.9957813428401663, 0.9993464052287582, 0.9997029114676174, 0.9999702911467617, 1.0 };

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
