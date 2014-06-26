package org.sa.rainbow.core;

import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;

public interface IRainbowMaster {

//    public abstract UtilityPreferenceDescription preferenceDesc ();

    public abstract GaugeDescription gaugeDesc ();

    public abstract EffectorDescription effectorDesc ();

    public abstract ProbeDescription probeDesc ();

    public abstract ModelsManager modelsManager ();

}
