package org.sa.rainbow.configuration

import org.acme.acme.AcmeComponentTypeDeclaration
import org.acme.acme.AcmeConnectorTypeDeclaration
import org.acme.acme.AcmeElementTypeDeclaration
import org.acme.acme.AcmeGroupTypeDeclaration
import org.acme.acme.AcmePortTypeDeclaration
import org.acme.acme.AcmeRoleTypeDeclaration
import org.acme.acme.AnyTypeRef
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmPrimitiveType
import org.sa.rainbow.configuration.configModel.FormalParam

class XtendUtils {
		static def formalTypeName(FormalParam fp, boolean keepSimple) {
		if (fp.type.java !== null)
			keepSimple?fp.type.java.referable.simpleName:fp.type.java.referable.qualifiedName
		else if (fp.type.acme !== null) {
			switch fp.type.acme.referable {
				AcmeComponentTypeDeclaration: return keepSimple?"IAcmeComponent":"org.acmestudio.acme.element.IAcmeComponent"
				AcmeConnectorTypeDeclaration: return keepSimple?"IAcmeConnector":"org.acmestudio.acme.element.IAcmeConnector"
				AcmePortTypeDeclaration: return keepSimple?"IAcmePort":"org.acmestudio.acme.element.IAcmePort"
				AcmeRoleTypeDeclaration: return keepSimple?"IAcmeRole":"org.acmestudio.acme.element.IAcmeRole"
				AcmeElementTypeDeclaration: return keepSimple?"IAcmeElement":"org.acmestudio.acme.element.IAcmeElement"
				AcmeGroupTypeDeclaration: return keepSimple?"IAcmeGroup":"org.acmestudio.acme.element.IAcmeGroup"
				default: return keepSimple?"IAcmeElement":"org.acmestudio.acme.element.IAcmeElement"
			}
		} 
		else if(fp.type.base !== null) fp.type.base.name()
	}
	
	def static getAcmeTypeName(AnyTypeRef ref) {
		switch ref {
			AcmeElementTypeDeclaration: ref.name
			AcmeComponentTypeDeclaration: ref.name
			AcmeConnectorTypeDeclaration: ref.name
			AcmePortTypeDeclaration: ref.name
			AcmeRoleTypeDeclaration: ref.name
			AcmeGroupTypeDeclaration: ref.name
		}
	}
	
	def static convertToString(FormalParam p) {
		
		if (p.type.acme !== null) {
			'''«p.name».getQualifiedName()'''
		}
		else if (p.type.java !== null) {
			val type = p.type.java.referable
			switch type {
				JvmPrimitiveType: 
					switch type.simpleName {
						 case "double", 
						 case "Double": '''Double.toString(«p.name»)'''
						 case "int",
						 case "Integer": '''Integer.toString(«p.name»)'''
						 case "String": p.name
						 case "boolean",
						 case "Boolean" : '''Boolean.toString(«p.name»)'''
						 case "char" : '''Character.toString(«p.name»)'''
					}
				JvmDeclaredType: '''«p.name».toString()'''
			}
		}
		else {
			throw new IllegalArgumentException ("Don't know how to convert parameter " + p.name)
		}
	}
	
}