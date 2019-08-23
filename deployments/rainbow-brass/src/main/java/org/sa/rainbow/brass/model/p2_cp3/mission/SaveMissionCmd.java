package org.sa.rainbow.brass.model.p2_cp3.mission;

import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel.TimeStamped;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by schmerl on 12/27/2016.
 */
public class SaveMissionCmd extends AbstractSaveModelCmd<MissionState> {
    public SaveMissionCmd (IModelsManager mm, String resource, OutputStream os, String source) {
        super ("saveMissionState", mm, resource, os, source);
    }

    @Override
    public Object getResult () throws IllegalStateException {
        return null;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference ("", "MissionState");
    }

    @Override
    protected void subExecute () throws RainbowException {
        MissionState model = getModelContext ().getModelInstance ();
        try (PrintStream ps = new PrintStream (getStream ())) {
            for (TimeStamped<MissionState.LocationRecording> l : model.m_locationHistory) {
                ps.print ("(");
                ps.print (l.data.getX ());
                ps.print (",");
                ps.print (l.data.getY ());
                ps.print (",");
                ps.print(l.data.getHeading().name());
                ps.print (")@");
                ps.print (l.timestamp);
                ps.print ("\n");
            }
        }
    }

    @Override
    protected void subRedo () throws RainbowException {

    }

    @Override
    protected void subUndo () throws RainbowException {

    }

    @Override
    protected boolean checkModelValidForCommand (MissionState missionState) {
        return true;
    }
}
