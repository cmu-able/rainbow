package org.acme.xtext.tests

import com.google.inject.Inject
import org.acme.acme.AcmeCompUnit
import org.acme.acme.AcmePackage
import org.acme.acme.AcmePropertyDeclaration
import org.acme.validation.Diagnostics
import org.eclipse.xtext.diagnostics.Diagnostic
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.assertNotNull

@RunWith(XtextRunner)
@InjectWith(AcmeInjectorProvider)
class AcmeSimpleInstancePropertyTest {

	@Inject extension ParseHelper<AcmeCompUnit> parserHelper

	@Inject extension ValidationTestHelper

	@Test
	def void testPropertyIntInInstance() {
		'''
		system sys = {
			property prop : int = 2;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyStringInInstance() {
		'''
		system sys = {
			property prop : String = "foo";
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyBooleanInInstance() {
		'''
		system sys = {
			property prop : boolean = true;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyFloatWFloatInInstance() {
		'''
		system sys = {
			property prop : float = 1.23;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyFloatWIntInInstance() {
		'''
		system sys = {
			property prop : float = 1;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyDoubleWFloatInInstance() {
		'''
		system sys = {
			property prop : double = 1.23;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyDoubleWIntInInstance() {
		'''
		system sys = {
			property prop : double = 1;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyAnyInInstance() {
		'''
		system sys = {
			property prop : any = 1;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyIntInSuperType() {
		'''
		family fam = {
			property prop : int;
		}
		
		system sys : fam = new fam extended with {
			property prop = 2;
		}'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testPropertyWrongValue() {
		'''
		system sys = {
			property prop : string = 1;
		}'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}

	@Test
	def void testPropertyWrongValueST() {
		'''
		family fam = {
					property prop : string;
				}
				
		system sys : fam = new fam extended with {
			property prop  = 1;
		}'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}

	@Test
	def void testUnificationError() {
		'''
		family fam = {
					property prop : string;
				}
				
		system sys : fam = new fam extended with {
			property prop : int = 1;
		}'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.UNIFICATION_ERROR)
		]
	}

	@Test
	def void testUnificationErrorTyping() {
		'''
		family fam1 = {
			property prop : string;
		}
		
		family fam2 extends fam1 with {
			property prop : int;
		}'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.UNIFICATION_ERROR)
		]
	}

	@Test
	def void testNoProblemWithPropertyInComponent() {
		'''
			system sys = {
				property prop : string = "foo";
				component comp = {
					property prop : int = 1;
				}
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	def generateElementPropertyUnificationTest(boolean ok, boolean portOrRole, String... element) '''
		family fam = {
			«element.get(portOrRole?1:0)» type compt = {
				property prop : int = 1;
			}
		}
		system sys : fam = new fam extended with {
			«IF portOrRole»
				«element.get(0)» comp = {
					«element.get(1)» sub : compt = new compt extended with {
						«IF ok»
							property prop : int = 1;
						«ELSE»
							property prop : string = "hello";
						«ENDIF»
					}
				}
				
			«ELSE»
				«element.get(0)» comp : compt = new compt extended with {
					«IF ok»
						property prop : int = 1;
					«ELSE»
						property prop : string="hello";
					«ENDIF»
				}
			«ENDIF»
		}
	'''

	@Test
	def void testComponentUnificationError() {
		val submap = #{'component' -> 'port', 'connector' -> 'role'}

		for (element : #{'component', 'connector', 'group'}) {
			var toParse = generateElementPropertyUnificationTest(true, false, element)
			toParse.parse => [
				assertNotNull('''«element» test failed to parse''', it)
				assertNoErrors()
			]
			toParse = generateElementPropertyUnificationTest(false, false, element)
			toParse.parse => [
				assertNotNull('''«element» test failed to parse''', it)
				assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.UNIFICATION_ERROR)

			]
			if (submap.containsKey(element)) {
				val sub = submap.get(element)
				toParse = generateElementPropertyUnificationTest(true, true, element, sub)
				toParse.parse => [
					assertNotNull('''«sub» test failed to parse''', it)
					assertNoErrors()
				]
				toParse = generateElementPropertyUnificationTest(false, true, element, sub)
				toParse.parse => [
					assertNotNull('''«sub» test failed to parse''', it)
					assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.UNIFICATION_ERROR)
				]
			}
		}
	}
	
	@Test
	def testUnificationErrorsOfAllTypes() {
		val types =#{'int'->'20', 'boolean' -> 'false', 'string' -> '"foobar"', 'float'->'1.23', 'double' -> '4.56'}
		
		for (e1 : types.entrySet) {
			for (e2 : types.entrySet) {
				'''
				family fam = {
					component type compt = {
						property prop : «e1.key» = «e1.value»;	
					}	
					
					component comp : compt = new compt extended with {
						property prop : «e2.key» = «e2.value»;	
					}
				}'''.parse => [
					try {
					assertNotNull('''«e1.key»X«e2.key»''')
					if (e1.key != e2.key) {
						assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.UNIFICATION_ERROR)
					}
					else {
						assertNoErrors
					}
					}
					catch (Exception e) {
						System.out.println('''Failed «e1.key»X«e2.key»''')
						throw e;
					}
				]
			}
		}
	}
	
}
