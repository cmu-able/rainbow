package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetBatteryChargeCmd extends AbstractRainbowModelOperation<Double, MissionState> {

    private double m_charge;

    public SetBatteryChargeCmd (MissionStateModelInstance model, String target, String charge) {
        super ("setBatteryCharge", model, target, charge);
        m_charge = Double.parseDouble (charge);
    }

    @Override
    public Double getResult () throws IllegalStateException {
        return getModelContext ().getModelInstance ().getBatteryCharge ();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setBatteryCharge");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setBatteryCharge (m_charge);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().m_chargeHistory.pop ();
    }

    @Override
    protected boolean checkModelValidForCommand (MissionState model) {
        return model == getModelContext ().getModelInstance ();
    }

}
