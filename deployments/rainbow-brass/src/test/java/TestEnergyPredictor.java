import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.brass.analyses.p2_cp1.EnergyConsumptionPredictor;
import org.sa.rainbow.brass.confsynthesis.PropertiesSimpleConfigurationStore;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.core.models.ModelReference;

public class TestEnergyPredictor {

	private EnvMap m_e;
	private MissionState m_m;
	private SimpleConfigurationStore m_p;

	@Before
	public void setUp() {
		m_e = new EnvMap(new ModelReference("test", "Map"));
		m_e.loadFromFile("src/test/resources/cp1_map.json");
		m_m = new MissionState(new ModelReference("test", "MissionState"));
		Properties props = new Properties();
		props.setProperty(PropertiesSimpleConfigurationStore.CONFIGURATIONS_SOURCE_PROPKEY, "src/test/resources/config_list.json");
		m_p = new SimpleConfigurationStore(props);
		m_p.populate();
		EnvMapNode envMapNode = m_e.getNodes().get("l1");
		m_m.setCurrentPose(envMapNode.getX(), envMapNode.getY(), 0.0);
	}
	
	@Test
	public void testHasEnoughEnergy() {
		InstructionGraphProgress ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test"),"P(V(1, do MoveAbsH(-21.08, 11.08, 0.68, -0.0125) then 2),\r\n" + 
				"V(2, do MoveAbsH(0.54, 10.81, 0.68, 0.0000) then 3)::\r\n" + 
				"V(3, do MoveAbsH(22.97, 10.81, 0.68, -1.5470) then 4)::\r\n" + 
				"V(4, do MoveAbsH(23.24, -0.54, 0.68, -1.5470) then 5)::\r\n" + 
				"V(5, end)::\r\n" + 
				"nil)");
		configureIGWaypointInfo(ip);
		m_m.setTargetWaypoint("l9");


		
		EnergyConsumptionPredictor predictor = new EnergyConsumptionPredictor(m_e, m_m, m_p);
		predictor.setConfig("sol_7");
		IInstruction firstInstruction = ip.getInstruction("1");
		ip.setExecutingInstruction("1", "");
		double energyRequired = predictor.getPlanEnergyConsumption(firstInstruction, ip.getRemainingInstructions(), "l9");
		assertTrue(energyRequired > 0);
		assertTrue(energyRequired < MapTranslator.ROBOT_BATTERY_RANGE_MAX);
	}

	private void configureIGWaypointInfo(InstructionGraphProgress ip) {
		String currentSrc = m_e.getNode(m_m.getCurrentPose().getX(), m_m.getCurrentPose().getY()).getLabel();

		Collection<? extends IInstruction> instructions = ip
				.getInstructions();
		for (IInstruction i : instructions) {
			if (i instanceof MoveAbsHInstruction) {
				MoveAbsHInstruction mai = (MoveAbsHInstruction) i;
				mai.setSourceWaypoint(currentSrc);
				String tgtWp = m_e.getNode(mai.getTargetX(), mai.getTargetY()).getLabel();
				mai.setTargetWaypoint(tgtWp);
				currentSrc = tgtWp;
			}
		}
	}

}
