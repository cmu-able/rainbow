package org.sa.rainbow.brass.gauges;

import java.util.Arrays;

public class PowerConverter {
    public static final int    MIN_VOLTAGE                = 104;
    public static final int    MAX_VOLTAGE                = 166;
    public static final double BATTERY_CAPACITY /* mwh */ = 32560.0;

    private static final double[] percent_of_v_avg = new double[] { 2.970885323827499e-05, 0.0002970885323826389,
            0.0006535947712418277, 0.005555555555555591, 0.01752822341057636, 0.027005347593582862,
            0.037849079025549626, 0.0497623291740939, 0.061467617349970305, 0.07338086749851452, 0.08796791443850266,
            0.09910873440285206, 0.1126559714795009, 0.12849079025549615, 0.1387106357694593, 0.152020202020202,
            0.1690433749257279, 0.17964943553178847, 0.1952762923351159, 0.21197266785502084, 0.23351158645276288,
            0.2579025549613785, 0.29780154486036836, 0.3344622697563874, 0.3665478312537136, 0.40029708853238266,
            0.41847890671420085, 0.4347890671420083, 0.4619132501485443, 0.4748366013071895, 0.49313725490196075,
            0.5114676173499704, 0.5308080808080808, 0.5545454545454545, 0.5677064765300059, 0.5873737373737373,
            0.614468211527035, 0.6487225193107546, 0.6823826500297088, 0.7197860962566844, 0.7462269756387403,
            0.77106357694593, 0.7944741532976827, 0.8094771241830065, 0.8299762329174094, 0.8382947118241236,
            0.8519310754604872, 0.8643790849673203, 0.8807486631016043, 0.889453357100416, 0.9033868092691621,
            0.9167260843731432, 0.9289067142008318, 0.9410576351752822, 0.9527629233511586, 0.9684789067142008,
            0.9748663101604278, 0.985769459298871, 0.9957813428401663, 0.9993464052287582, 0.9997029114676174,
            0.9999702911467617, 1.0 };

    private static final double[] percent_of_v_opt = new double[] { 5.941770647654998e-05, 0.0005941770647652778,
            0.0011289364230541166, 0.0017231134878193943, 0.002376708259061222, 0.0029708853238265,
            0.0036838978015448776, 0.004278074866310155, 0.005050505050505083, 0.00588235294117645,
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

    private static final double[] percent_of_v_pess = new double[] { 0.0, 5.9417706476530004e-05, 0.0005941770647653001,
            0.0011289364230540702, 0.0017231134878193703, 0.0023767082590612004, 0.0029708853238265003,
            0.00368389780154486, 0.0042780748663101605, 0.005050505050505051, 0.0058823529411764705,
            0.006654783125371361, 0.0075460487225193105, 0.008556149732620321, 0.00956625074272133,
            0.010635769459298871, 0.011764705882352941, 0.013190730837789662, 0.014676173499702912, 0.01628045157456922,
            0.01794414735591206, 0.020023767082590613, 0.021984551396316103, 0.024420677361853833, 0.027094474153297684,
            0.029946524064171122, 0.033214497920380275, 0.03677956030897207, 0.041592394533571005, 0.04664289958407605,
            0.053951277480689244, 0.0649435531788473, 0.08193701723113488, 0.10879382055852645, 0.12388591800356506,
            0.17201426024955438, 0.20005941770647653, 0.23273915626856803, 0.29744503862150923, 0.3581699346405229,
            0.4294711824123589, 0.5063576945929887, 0.5532976827094475, 0.5939988116458704, 0.6257278669043375,
            0.655496137849079, 0.6752228163992869, 0.707843137254902, 0.7332144979203803, 0.752584670231729,
            0.7832442067736185, 0.8093285799168152, 0.8307189542483661, 0.8591206179441474, 0.8821152703505645,
            0.9047534165181224, 0.9279857397504456, 0.9489601901366608, 0.9673202614379085, 0.9906120023767082,
            0.9998217468805705, 1.0, 1.0 };

    private static final int NUM_V_DATA = 16830;

    private static short[] concatAll (short[] first, short[]... rest) {
        int totalLength = first.length;
        for (short[] array : rest) {
            totalLength += array.length;
        }
        short[] result = Arrays.copyOf (first, totalLength);
        int offset = first.length;
        for (short[] array : rest) {
            System.arraycopy (array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    private static short[] v_data = null;
    static {
        // Copy over split up data model to counter 64k static initializer limitation
        System.out.println ("Initializing...");
        v_data = concatAll (V_DATA_1.v_data, V_DATA_2.v_data, V_DATA_3.v_data);
    }

    public static short charge2Voltage (double charge) {
        double pct = charge / BATTERY_CAPACITY;
        double idx_d = pct * NUM_V_DATA;
        long idx = Math.round (idx_d);
        return v_data[(int )(v_data.length - idx - 1)];
    }

    public static double voltage2ChargeAvg (int voltage) {
        double pct = percent_of_v_avg[voltage - MIN_VOLTAGE];
        return pct * BATTERY_CAPACITY;
    }

    public static double voltage2ChargeOpt (int voltage) {
        double pct = percent_of_v_opt[voltage - MIN_VOLTAGE];
        return pct * BATTERY_CAPACITY;
    }

    public static double voltage2ChargePess (int voltage) {
        double pct = percent_of_v_pess[voltage - MIN_VOLTAGE];
        return pct * BATTERY_CAPACITY;
    }

    public static void main (String[] args) {
        System.out.println ("Voltage " + MAX_VOLTAGE + " (avg) = " + voltage2ChargeAvg (MAX_VOLTAGE) + ", should be "
                + BATTERY_CAPACITY);
        System.out.println ("Voltage " + MAX_VOLTAGE + " (opt) = " + voltage2ChargeOpt (MAX_VOLTAGE) + ", should be "
                + BATTERY_CAPACITY);
        System.out.println ("Voltage " + MAX_VOLTAGE + " (pess) = " + voltage2ChargePess (MAX_VOLTAGE) + ", should be "
                + BATTERY_CAPACITY);

        System.out.println ("Voltage " + MIN_VOLTAGE + " = " + voltage2ChargeAvg (MIN_VOLTAGE) + ", should be 0");
        System.out.println ("Voltage " + MIN_VOLTAGE + " = " + voltage2ChargeAvg (MIN_VOLTAGE) + ", should be 0");
        System.out.println ("Voltage " + MIN_VOLTAGE + " = " + voltage2ChargeAvg (MIN_VOLTAGE) + ", should be 0");

        for (int i = MIN_VOLTAGE; i <= MAX_VOLTAGE; i++) {
            System.out.println ("Voltage (avg) " + i + " = " + voltage2ChargeAvg (i));
            System.out.println ("Voltage (opt) " + i + " = " + voltage2ChargeOpt (i));
            System.out.println ("Voltage (pess) " + i + " = " + voltage2ChargePess (i));
            System.out.println ("");
        }
    }
}
