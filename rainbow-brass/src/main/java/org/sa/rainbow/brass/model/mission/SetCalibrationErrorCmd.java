package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.brass.model.mission.MissionState.CalibrationError;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetCalibrationErrorCmd extends AbstractRainbowModelOperation<CalibrationError, MissionState> {

    private CalibrationError m_error;

    public SetCalibrationErrorCmd (MissionStateModelInstance model, String target, String r, String r_scale, String t,
            String t_scale, String v) {
        super ("setCalibrationError", model, target, t, r);
        m_error = new CalibrationError ();
        m_error.translational_error = Double.parseDouble (t);
        m_error.translational_scale = Double.parseDouble (t_scale);
        m_error.rotational_error = Double.parseDouble (r);
        m_error.rotational_scale = Double.parseDouble (r_scale);
        m_error.velocity_at_time_of_error = Double.parseDouble (v);
    }

    @Override
    public CalibrationError getResult () throws IllegalStateException {
        return m_error;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setCalibrationError");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().addCalibrationErrorSample (m_error);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().m_calibarationErrorHistory.pop ();
    }

    @Override
    protected boolean checkModelValidForCommand (MissionState model) {
        return true;
    }

}
