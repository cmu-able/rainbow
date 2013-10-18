package org.sa.rainbow.translator.znn.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class CaptchaGauge extends RegularPatternGauge {
    public static final String    NAME       = "G - Captcha Enablement";

    private static final String   OFF        = "off";
    private static final String   ON         = "on";

    /** List of values reported by this Gauge */
    private static final String[] valueNames = { "enabled" };

    public CaptchaGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, List<IRainbowModelCommandRepresentation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (ON, Pattern.compile ("^on$"));
        addPattern (OFF, Pattern.compile ("^off$"));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        boolean captchaOn = ON.equals (matchName);
        IRainbowModelCommandRepresentation cmd = m_commands.values ().iterator ().next ();
        Map<String, String> pMap = new HashMap<String, String> ();
        pMap.put (cmd.getParameters ()[0], Boolean.toString (captchaOn));
        issueCommand (cmd, pMap);
    }

    @Override
    protected void initProperty (String name, Object value) {
        // TODO Auto-generated method stub

    }

}
