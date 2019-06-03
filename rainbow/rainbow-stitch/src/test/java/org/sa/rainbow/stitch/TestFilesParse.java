package org.sa.rainbow.stitch;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestFilesParse extends StitchTest {

//	public TestFilesParse() {
//	}
	
	@Test
	public void test() {
		File folder = new File("src/test/resources/stitch-egs/");
		File[] stitches = folder.listFiles();
		
		for (File file : stitches) {
			try {
				loadScript(file.getAbsolutePath(), false, false);
			} catch (IOException e) {
				assertTrue(false);
			}
		}
	}

}
