package incubator.qxt;

import java.beans.PropertyVetoException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

public class SampleQxtBeanCreator {
	static int nextId = 1;
	public static List<String> sexes;
	
	public static SampleQxtBean create() {
		SampleQxtBean b = new SampleQxtBean();
		b.setId(nextId++);
		
		updateRandomly(b);
		
		return b;
	}
	
	public static void updateRandomly(SampleQxtBean b) {
		try {
			b.setAge(RandomUtils.nextInt(100));
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		b.setName(RandomStringUtils.randomAlphabetic(10
				+ RandomUtils.nextInt(10)));
		b.setIntelligent(RandomUtils.nextBoolean());
		b.setSex(sexes.get(RandomUtils.nextInt(sexes.size())));
		Date d = new Date();
		final int dif = 1000;
		final long mul = 1000;
		d.setTime(d.getTime() -
				(dif * mul) + mul * 2 * RandomUtils.nextInt(dif));
		b.setLastAccess(d);
	}
}
