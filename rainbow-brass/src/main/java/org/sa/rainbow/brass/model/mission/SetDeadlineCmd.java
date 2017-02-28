package org.sa.rainbow.brass.model.mission;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetDeadlineCmd extends AbstractRainbowModelOperation<Date, MissionState> {

    private Date m_date;

    public SetDeadlineCmd (MissionStateModelInstance model, String target, String date) {
        super ("setDeadline", model, target, date);
        try {
            m_date = MissionState.BRASS_DATE_FORMAT.parse (date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Date getResult () throws IllegalStateException {
        return m_date;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setDeadlineCharge");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setDeadline (m_date);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().m_deadlineHistory.pop ();
    }

    @Override
    protected boolean checkModelValidForCommand (MissionState model) {
        return true;
    }

}
