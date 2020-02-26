package org.acme.xtext.tests

import com.google.inject.Inject
import org.acme.acme.AcmeCompUnit
import org.acme.acme.AcmePackage
import org.acme.validation.Diagnostics
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.assertNotNull

@RunWith(XtextRunner)
@InjectWith(AcmeInjectorProvider)
class AcmeCompoundPropertyTest {
	@Inject extension ParseHelper<AcmeCompUnit> parserHelper

	@Inject extension ValidationTestHelper

	@Test
	def void testSetWrongType() {
		'''
			system s = {
				property prop : set{string} = {"hello", 1};
			}
		'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}

	@Test
	def void testSetOK() {
		'''
			system s = {
				property prop : set{string} = {"hello", "there"};
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testSetOfSetWrongType() {
		'''
			system s = {
				property prop : set{set{double}} = {{"hello", 1}, {1.2, 3.4, 5.6, 7.8}};
			}
		'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}

	@Test
	def void testSetOfSetOK() {
		'''
			system s = {
				property prop : set{set{double}} = {{1,2,3}, {1.2, 3.4, 5.6, 7.8}};
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testSeqWrongType() {
		'''
			system s = {
				property prop : seq<string> = <"hello", 1>;
			}
		'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}

	@Test
	def void testSeqOK() {
		'''
			system s = {
				property prop : seq<string> = <"hello", "there">;
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testRecordSameOrderOK() {
		'''
			system s = {
				property prop : record[field1 : int; field2 : string;] = [field1=1; field2="hello";];
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testRecordDifferentOrderOK() {
		'''
			system s = {
				property prop : record[field1 : int; field2 : string;] = [field2="hello";field1=1;];
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void testRecordDifferentOrderFailType() {
		'''
			system s = {
				property prop : record[field1 : int; field2 : string;] = [field2="hello";field1=1.2;];
			}
		'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}

	@Test
	def void testRecordDifferentOrderSubFeldTypeOK() {
		'''
			system s = {
				property prop : record[field1 : float; field2 : string;] = [field2="hello";field1=1;];
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}

	@Test
	def void textRecordInRecordDifferentOrderSubFieldTypeOK() {
		'''
			system s = {
				property prop : record[
										a:record[a : float;
												 b : string;
												];
										b: any;] =
								[a = [a = 1; b = "me";]; b = false;];
			}
		'''.parse => [
			assertNotNull(it)
			assertNoErrors
		]
	}
	
	@Test
	def void textRecordInRecordDifferentOrderSubFieldTypeFail() {
		'''
			system s = {
				property prop : record[
										a:record[a : float;
												 b : string;
												];
										b: any;] =
								[a = [a = 1; b = false;]; b = false;];
			}
		'''.parse => [
			assertNotNull(it)
			assertError(AcmePackage::eINSTANCE.acmePropertyDeclaration, Diagnostics.TYPES_INCOMPATIBLE)
		]
	}
}
