import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.sa.rainbow.brass.model.instructions.ChargeInstruction;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.core.models.ModelReference;

public class TestIGProgress {

	@Test
	public void testParseFromStringString() {
		InstructionGraphProgress ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test"),
                " P(V(1, do KillNodes(%laserscanNodelet%) then 2), V(2, do SetSensor (%KINECT%, %off%) then 3):: V(3, do SetSensor (%CAMERA%, %on%) then 4):: V(4, do SetSensor (%HEADLAMP%, %on%) then 5):: V(5, do StartNodes(%aruco%) then 6):: V(6, do MoveAbsH(-6.22, 0.00, 0.68, 1.5708) then 7):: V(7, do MoveAbsH(-6.22, 10.27, 0.68, 1.5708) then 8):: V(8, end):: nil)");
		assertTrue(ip.getInstructions().size() == 7);
		
		
	}

	@Test
	public void testSegmentByInstructionType() {
		InstructionGraphProgress ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test")," P(V(1, do KillNodes(%laserscanNodelet%) then 2), V(2, do SetSensor (%KINECT%, %off%) then 3):: V(3, do SetSensor (%CAMERA%, %on%) then 4):: V(4, do SetSensor (%HEADLAMP%, %on%) then 5):: V(5, do Charge(200) then 6):: V(6, do MoveAbsH(-6.22, 0.00, 0.68, 1.5708) then 7):: V(7, do MoveAbsH(-6.22, 10.27, 0.68, 1.5708) then 8):: V(8, end):: nil)");
		List<List<? extends IInstruction>> s = InstructionGraphProgress.segmentByInstructionType(ip.getInstructions(), ChargeInstruction.class);
		assertTrue(s.size() == 2);
		// Two consecutive charges
		ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test")," P(V(1, do KillNodes(%laserscanNodelet%) then 2), V(2, do SetSensor (%KINECT%, %off%) then 3):: V(3, do SetSensor (%CAMERA%, %on%) then 4):: V(4, do SetSensor (%HEADLAMP%, %on%) then 5):: V(5, do Charge(200) then 6):: V(6, do Charge(20) then 7):: V(7, do MoveAbsH(-6.22, 10.27, 0.68, 1.5708) then 8):: V(8, end):: nil)");
		s = InstructionGraphProgress.segmentByInstructionType(ip.getInstructions(), ChargeInstruction.class);
		assertTrue(s.size() == 2);
		
		// Two charges not consecutive
		ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test")," P(V(1, do KillNodes(%laserscanNodelet%) then 2), V(2, do SetSensor (%KINECT%, %off%) then 3):: V(3, do Charge(34) then 4):: V(4, do SetSensor (%HEADLAMP%, %on%) then 5):: V(5, do Charge(200) then 6):: V(6, do MoveAbseH(-6, -7, -9) then 7):: V(7, do MoveAbsH(-6.22, 10.27, 0.68, 1.5708) then 8):: V(8, end):: nil)");
		s = InstructionGraphProgress.segmentByInstructionType(ip.getInstructions(), ChargeInstruction.class);
		assertTrue(s.size() == 3);
		
		// Charge at end only
		ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test")," P(V(1, do KillNodes(%laserscanNodelet%) then 2), V(2, do SetSensor (%KINECT%, %off%) then 3):: V(3, do SetSensor (%CAMERA%, %on%) then 4):: V(4, do SetSensor (%HEADLAMP%, %on%) then 5):: V(5, do StartNodes(%aruco%) then 6):: V(6, do MoveAbsH(-6.22, 0.00, 0.68, 1.5708) then 7):: V(7, do Charge(2) then 8):: V(8, end):: nil)");
		s = InstructionGraphProgress.segmentByInstructionType(ip.getInstructions(), ChargeInstruction.class);
		assertTrue(s.size() == 1);
		
		// Charge at begin and end
		ip = InstructionGraphProgress.parseFromString (new ModelReference ("test", "test")," P(V(1, do Charge(2) then 2), V(2, do SetSensor (%KINECT%, %off%) then 3):: V(3, do SetSensor (%CAMERA%, %on%) then 4):: V(4, do SetSensor (%HEADLAMP%, %on%) then 5):: V(5, do StartNodes(%aruco%) then 6):: V(6, do MoveAbsH(-6.22, 0.00, 0.68, 1.5708) then 7):: V(7, do Charge(2) then 8):: V(8, end):: nil)");
		s = InstructionGraphProgress.segmentByInstructionType(ip.getInstructions(), ChargeInstruction.class);
		assertTrue(s.size() == 1);
		
	}

}
