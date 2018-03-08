package org.sa.rainbow.brass.gauges.p2_cp3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class RobotStateGauge extends RegularPatternGauge{
	
	private static final String NAME = "Robot State Gauge";
	
	protected static final String CHARGE = "BatteryCharge";
	protected static final String KINECT = "KinectStatus";
	protected static final String LIDAR = "LidarStatus";
	protected static final String HEADLAMP = "HeadlampStatus";
	protected static final String BUMP = "BumpStatus";
	protected static final String SPEED = "Speed";
	
    protected static final String CHARGE_PATTERN      = "topic: /energy_monitor/charge.*\\n.*data: (.*)\\n(.*)";
    protected static final String KINECT_PATTERN = "topic: /mobile_base/kinect/status.*\\n.*data: (.*)";
    protected static final String LIDAR_PATTERN = "topic: /mobile_base/lidar/status.*\\n.*data: (.*)";
    protected static final String HEADLAMP_PATTERN = "topic: /mobile_base/headlamp/status.*\\n.*data: (.*)";

	private double last_charge = 0;

	private int reported_kinect_mode = -1;

	private boolean reported_lidar_mode;

	private boolean reported_headlamp_mode;
    
	public RobotStateGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
		addPattern(CHARGE, Pattern.compile(CHARGE_PATTERN, Pattern.DOTALL));
		addPattern(KINECT, Pattern.compile(KINECT_PATTERN, Pattern.DOTALL));
		addPattern(LIDAR, Pattern.compile(LIDAR_PATTERN, Pattern.DOTALL));
		addPattern(HEADLAMP, Pattern.compile(HEADLAMP_PATTERN, Pattern.DOTALL));
	}

	@Override
	protected void doMatch(String matchName, Matcher m) {
		if (CHARGE.equals (matchName)) {
            double charge = Double.parseDouble(m.group(1));

            if (chargeDifferent (charge)) {
                IRainbowOperation op = m_commands.get ("charge");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], Double.toString (charge));
                issueCommand (op, pMap);
            }
		}
		if (KINECT.equals(matchName)) {
			int mode = Integer.parseInt(m.group(1));
			
			if (reported_kinect_mode != mode) {
				reported_kinect_mode = mode;
				if (mode == 2) {
					IRainbowOperation cameraOp = m_commands.get("sensor");
					Map<String,String> cameraMap = new HashMap<> ();
					cameraMap.put(cameraOp.getParameters()[0], Sensors.BACK_CAMERA.name());
					cameraMap.put(cameraOp.getParameters()[1], Boolean.toString(true));
					
					OperationRepresentation kinectOp = new OperationRepresentation(cameraOp);
					Map<String,String> kinectMap = new HashMap<> ();
					kinectMap.put(kinectOp.getParameters()[0], Sensors.KINECT.name());
					kinectMap.put(kinectOp.getParameters()[1], Boolean.toString(false));
					
					List<IRainbowOperation> ops = Arrays.asList(new IRainbowOperation[] {cameraOp, kinectOp});
					List<Map<String,String>> params = Arrays.asList(new Map[] {cameraMap, kinectMap});
					issueCommands(ops, params);
				}
				else if (mode == 1) {
					IRainbowOperation cameraOp = m_commands.get("sensor");
					Map<String,String> cameraMap = new HashMap<> ();
					cameraMap.put(cameraOp.getParameters()[0], Sensors.BACK_CAMERA.name());
					cameraMap.put(cameraOp.getParameters()[1], Boolean.toString(false));
					
					OperationRepresentation kinectOp = new OperationRepresentation(cameraOp);
					Map<String,String> kinectMap = new HashMap<> ();
					kinectMap.put(kinectOp.getParameters()[0], Sensors.KINECT.name());
					kinectMap.put(kinectOp.getParameters()[1], Boolean.toString(true));
					
					List<IRainbowOperation> ops = Arrays.asList(new IRainbowOperation[] {cameraOp, kinectOp});
					List<Map<String,String>> params = Arrays.asList(new Map[] {cameraMap, kinectMap});
					issueCommands(ops, params);
				}
				else if (mode == 0) {
					IRainbowOperation cameraOp = m_commands.get("sensor");
					Map<String,String> cameraMap = new HashMap<> ();
					cameraMap.put(cameraOp.getParameters()[0], Sensors.BACK_CAMERA.name());
					cameraMap.put(cameraOp.getParameters()[1], Boolean.toString(false));
					
					OperationRepresentation kinectOp = new OperationRepresentation(cameraOp);
					Map<String,String> kinectMap = new HashMap<> ();
					kinectMap.put(kinectOp.getParameters()[0], Sensors.KINECT.name());
					kinectMap.put(kinectOp.getParameters()[1], Boolean.toString(false));
					
					List<IRainbowOperation> ops = Arrays.asList(new IRainbowOperation[] {cameraOp, kinectOp});
					List<Map<String,String>> params = Arrays.asList(new Map[] {cameraMap, kinectMap});
					issueCommands(ops, params);
				}
			}
			if (LIDAR.equals(matchName)) {
				boolean lMode = Boolean.parseBoolean(m.group(1));
				if (lMode != reported_lidar_mode) {
					reported_lidar_mode = lMode;
					IRainbowOperation lidarOp = m_commands.get("sensor");
					Map<String,String> lidarParams = new HashMap<> ();
					lidarParams.put(lidarOp.getParameters()[0], Sensors.LIDAR.name());
					lidarParams.put(lidarOp.getParameters()[1], Boolean.toString(lMode));
					issueCommand(lidarOp, lidarParams);
				}
			}
			if (HEADLAMP.equals(matchName)) {
				boolean hMode = Boolean.parseBoolean(m.group(1));
				if (hMode != reported_headlamp_mode) {
					reported_headlamp_mode = hMode;
					IRainbowOperation lidarOp = m_commands.get("sensor");
					Map<String,String> lidarParams = new HashMap<> ();
					lidarParams.put(lidarOp.getParameters()[0], Sensors.HEADLAMP.name());
					lidarParams.put(lidarOp.getParameters()[1], Boolean.toString(hMode));
					issueCommand(lidarOp, lidarParams);
				}
			}
		}
	}
	
    private boolean chargeDifferent (double charge) {
        if (Math.round (last_charge) != Math.round (charge)) {
            last_charge = charge;
            return true;
        }
        return false;
    }
}
