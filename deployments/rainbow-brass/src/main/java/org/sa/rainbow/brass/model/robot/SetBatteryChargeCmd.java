package org.sa.rainbow.brass.model.robot;

import java.util.List;

import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetBatteryChargeCmd extends AbstractRainbowModelOperation<Double, RobotState> {

    private double m_charge;

    public SetBatteryChargeCmd (RobotStateModelInstance model, String target, String charge) {
        super ("setBatteryCharge", model, target, charge);
        m_charge = Double.parseDouble (charge);
    }

    @Override
    public Double getResult () throws IllegalStateException {
        return getModelContext ().getModelInstance ().getCharge ();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setBatteryCharge");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setCharge (m_charge);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().getCharge();
    }

    @Override
    protected boolean checkModelValidForCommand (RobotState model) {
        return model == getModelContext ().getModelInstance ();
    }

}
