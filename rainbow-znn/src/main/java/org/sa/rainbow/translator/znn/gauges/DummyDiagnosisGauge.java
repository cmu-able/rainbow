package org.sa.rainbow.translator.znn.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class DummyDiagnosisGauge extends RegularPatternGauge {

    private static final String   NAME       = "G - Dummy Diagnosis Gauge";
    private static final String   DEFAULT    = "default";
    private static final String[] valueNames = { "maliciousness" };

    public DummyDiagnosisGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, List<IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern (DEFAULT, Pattern.compile ("([\\w_]+)=([\\d]+(\\.[\\d]*))"));

    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (DEFAULT.equals (matchName)) {
            String LB = m.group (1);
            IRainbowOperation cmd = m_commands.values ().iterator ().next ();
            Map<String, String> pm = new HashMap<> ();
            pm.put (cmd.getParameters ()[0], m.group (2));
            pm.put (cmd.getTarget (), LB);
            issueCommand (cmd, pm);

//            String pClient = m_modelDesc.getName () + Util.DOT + m.group (1) + Util.DOT + "maliciousness";
//            eventHandler ().reportValue (new AttributeValueTriple (pClient, valueNames[0], m.group (2)));
        }
    }

    @Override
    protected void initProperty (String name, Object value) {
        // TODO Auto-generated method stub

    }

}
