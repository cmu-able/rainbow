package org.sa.rainbow.brass.model.robot;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Deque;
import java.util.Iterator;

import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel.TimeStamped;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class SaveRobotStateCmd extends AbstractSaveModelCmd<RobotState> {

	public SaveRobotStateCmd(IModelsManager mm, String resource, OutputStream os, String source) {
		super("saveRobotState", mm, resource, os, source);
	}

	@Override
	public Object getResult() throws IllegalStateException {
		return null;
	}

	@Override
	protected void subExecute() throws RainbowException {
		RobotState model = getModelContext().getModelInstance();
		try (PrintStream ps = new PrintStream(getStream ())) {
			ps.print("{\n");
			insertRobotState(ps, model);
			ps.print("}");
		}
	}

	protected void insertRobotState(PrintStream ps, RobotState model) {
		ps.print("  chargeHistory : [\n");
		for (Iterator<TimeStamped<Double>> iterator = model.m_chargeHistory.iterator(); iterator.hasNext();) {
			TimeStamped<Double> entry = iterator.next();
			ps.print("{timestamp: " + entry.timestamp + ", charge : " + entry.data + "}");
			if (iterator.hasNext()) ps.print(", ");
		}
		ps.print("]");
		
		ps.print("  speedHistory : [\n");
		for (Iterator<TimeStamped<Double>> iterator = model.m_speedHistory.iterator(); iterator.hasNext();) {
			TimeStamped<Double> entry = iterator.next();
			ps.print("{timestamp: " + entry.timestamp + ", speed : " + entry.data + "}");
			if (iterator.hasNext()) ps.print(", ");
		}
		ps.print("]");;
	}

	@Override
	protected void subRedo() throws RainbowException {
	}

	@Override
	protected void subUndo() throws RainbowException {
	}

	@Override
	protected boolean checkModelValidForCommand(RobotState model) {
		return true;
	}

}
