package org.sa.rainbow.stitch.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.sa.rainbow.stitch.stitch.script

@RunWith(XtextRunner)
@InjectWith(StitchTestInjectorProvider)
class StitchJavaReferenceTest {
	@Inject extension ParseHelper<script> parserHelper

	@Inject extension ValidationTestHelper
	
	@Test
	def void testSetSize() {
		'''
		module swim.strategies;
		import op "org.sa.rainbow.stitch.lib.Set";
		
		define int size = Set.size({0,1,2});
		'''.parse => [
			assertNoErrors
		]
	}
}