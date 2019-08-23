import static org.junit.Assert.*;

import org.junit.Test;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.core.models.ModelReference;

public class TestEnvMap {

	@Test
	public void testInsertNode() {
		EnvMap em = new EnvMap(new ModelReference("test", "test"));
		em.AddNode("l1", 0, 0);
		em.AddNode("l2", 10,0);
		EnvMapArc arc1 = em.addArc("l1", "l2", 10, true);
		EnvMapArc arc2 = em.addArc("l2","l1", 10, true);
		
		arc1.addSuccessRate("All", 0.95);
		arc2.addSuccessRate("All", 0.95);
		arc1.addHitRate("All", 0.15);
		arc2.addHitRate("All", 0.15);
		arc1.addTime("All", 5.5);
		arc2.addTime("All", 5.5);
		
		String label = em.insertNode("l3", "l1", "l2", 5, 0, false);
		assertEquals("l3", label);
		
		EnvMapArc narc1 = em.getArc("l3", "l1");
		assertEquals(new Double(0.95),narc1.getSuccessRate("All"));
		assertEquals(new Double(0.15), narc1.getHitRate("All"));
		assertEquals(new Double(0.0), narc1.getTime("All"));
		
	}

}
