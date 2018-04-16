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
	
	public static class MovingAverage {
		private double [] window;
		private int n, insert;
		private double sum;
		
		public MovingAverage(int size) {
			window = new double[size];
			insert = 0;
			sum = 0;
		}
		
		public double next (double val) {
			if (n < window.length) n++;
			sum -= window[insert];
			sum += val;
			window[insert] = val;
			insert = (insert + 1) % window.length;
			return (double )sum/n;
		}
	}
	
	private static final String NAME = "Robot State Gauge";
	
	protected static final String CHARGE = "BatteryCharge";
	protected static final String KINECT = "KinectStatus";
	protected static final String LIDAR = "LidarStatus";
	protected static final String HEADLAMP = "HeadlampStatus";
	protected static final String BUMP = "BumpStatus";
	protected static final String SPEED = "Speed";
	protected static final String LIGHTING = "Lighting"; 
	
    protected static final String CHARGE_PATTERN      = "topic: /energy_monitor/energy_level.*\\n.*data: (.*)";
    protected static final String KINECT_PATTERN = "topic: /mobile_base/kinect/status.*\\n.*data: (.*)";
    protected static final String LIDAR_PATTERN = "topic: /mobile_base/lidar/status.*\\n.*data: (.*)";
    protected static final String HEADLAMP_PATTERN = "topic: /mobile_base/headlamp/status.*\\n.*data: (.*)";
    protected static final String LIGHTING_PATTERN = "topic: /mobile_base/sensors/light_sensor.*\\n.*illuminance: (.*)v.*";
    protected static final String SPEED_PATTERN = "topic: /odom/twist/twist/linear.*\\nx: (.*)\\ny: (.*)\\n";
    protected static final String BUMP_PATTERN = "topic: /mobile_base/events/bumper.*\\nstate: (.*)\\n";

	private double last_charge = 0;

	private int reported_kinect_mode = -1;

	private boolean reported_lidar_mode;

	private boolean reported_headlamp_mode;

	private double last_lighting = -1;
	private MovingAverage lightingTracker = new MovingAverage(10);

	private double reported_speed;

	private boolean reported_bump;
    
	public RobotStateGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
		addPattern(CHARGE, Pattern.compile(CHARGE_PATTERN, Pattern.DOTALL));
		addPattern(KINECT, Pattern.compile(KINECT_PATTERN, Pattern.DOTALL));
		addPattern(LIDAR, Pattern.compile(LIDAR_PATTERN, Pattern.DOTALL));
		addPattern(HEADLAMP, Pattern.compile(HEADLAMP_PATTERN, Pattern.DOTALL));
		addPattern(LIGHTING, Pattern.compile(LIGHTING_PATTERN, Pattern.DOTALL));
		addPattern(SPEED, Pattern.compile(SPEED_PATTERN, Pattern.DOTALL));
		addPattern(BUMP, Pattern.compile(BUMP_PATTERN, Pattern.DOTALL));
	}

	@Override
	protected void doMatch(String matchName, Matcher m) {
		if (CHARGE.equals (matchName)) {
            double charge = Double.parseDouble(m.group(1).trim());

            if (chargeDifferent (charge)) {
                IRainbowOperation op = m_commands.get ("charge");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], Double.toString (charge));
                issueCommand (op, pMap);
            }
		}
		else if (LIGHTING.equals(matchName)) {
			double lighting = lightingTracker.next(Double.parseDouble(m.group(1).trim()));
			if (lightingDifferent(lighting)) {
				IRainbowOperation op = m_commands.get("lighting");
				Map<String,String> pMap = new HashMap<> ();
				pMap.put(op.getParameters()[0], Double.toString(lighting));
				issueCommand(op, pMap);
			}
		}
		else if (KINECT.equals(matchName)) {
			int mode = Integer.parseInt(m.group(1).trim());
			
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
			else if (LIDAR.equals(matchName)) {
				boolean lMode = Boolean.parseBoolean(m.group(1).trim().toLowerCase());
				if (lMode != reported_lidar_mode) {
					reported_lidar_mode = lMode;
					IRainbowOperation lidarOp = m_commands.get("sensor");
					Map<String,String> lidarParams = new HashMap<> ();
					lidarParams.put(lidarOp.getParameters()[0], Sensors.LIDAR.name());
					lidarParams.put(lidarOp.getParameters()[1], Boolean.toString(lMode));
					issueCommand(lidarOp, lidarParams);
				}
			}
			else if (HEADLAMP.equals(matchName)) {
				boolean hMode = Boolean.parseBoolean(m.group(1).trim().toLowerCase());
				if (hMode != reported_headlamp_mode) {
					reported_headlamp_mode = hMode;
					IRainbowOperation lidarOp = m_commands.get("sensor");
					Map<String,String> lidarParams = new HashMap<> ();
					lidarParams.put(lidarOp.getParameters()[0], Sensors.HEADLAMP.name());
					lidarParams.put(lidarOp.getParameters()[1], Boolean.toString(hMode));
					issueCommand(lidarOp, lidarParams);
				}
			}
			else if (SPEED.equals(matchName)) {
				double x = Double.parseDouble(m.group(1).trim ());
				double y = Double.parseDouble(m.group(2).trim());
				
				double speed = Math.round(Math.sqrt(x*x + y*y) * 100)/100d;
				if (speed != reported_speed) {
					reported_speed = speed;
					IRainbowOperation speedOp = m_commands.get("speed");
					Map<String,String> params = new HashMap<> ();
					params.put(speedOp.getParameters()[0], Double.toString(speed));
					issueCommand(speedOp, params);
				}		
			}
			else if (BUMP.equals(matchName)) {
				int status = Integer.parseInt(m.group(1).trim());
				boolean bump = status == 1;
				if (bump != reported_bump) {
					reported_bump = bump;
					IRainbowOperation bumpOp = m_commands.get("bump");
					Map<String,String> params = new HashMap<> ();
					params.put(bumpOp.getParameters()[0], Boolean.toString(bump));
					issueCommand(bumpOp, params);
				}
			}
		}
	}
	
    private boolean lightingDifferent(double lighting2) {
		if (Math.round(last_lighting) != Math.round(lighting2)) {
			last_lighting  = lighting2;
			return true;
		}
		return false;
	}

	private boolean chargeDifferent (double charge) {
        if (Math.round (last_charge) != Math.round (charge)) {
            last_charge = charge;
            return true;
        }
        return false;
    }
	
	@Override
	protected boolean shouldProcess() {
		return !isRainbowAdapting();
	}
}
