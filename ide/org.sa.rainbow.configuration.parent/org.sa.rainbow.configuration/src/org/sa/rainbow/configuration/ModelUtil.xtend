package org.sa.rainbow.configuration

import java.util.Collection
import java.util.List
import org.eclipse.xtext.common.types.JvmType
import org.sa.rainbow.configuration.rcl.Component
import org.sa.rainbow.configuration.rcl.ComponentType
import org.sa.rainbow.configuration.rcl.DeclaredProperty
import org.sa.rainbow.configuration.rcl.Factory
import org.sa.rainbow.configuration.rcl.FormalParam
import org.sa.rainbow.configuration.rcl.PropertyReference
import org.sa.rainbow.configuration.rcl.Reference
import org.acme.acme.AnyTypeRef

class ModelUtil {
	
	static class CommandRep {
		public var String hasTarget = null;
		public var List<String> formalNames = newLinkedList()
		public var name = ""
		
		
	}
	
	def static List<CommandRep> getCommandsFromReference(PropertyReference reference, Collection<String> include, Collection<String> filterOut, boolean type) {
		val referable = reference.referable
		val List<CommandRep> cmds = newLinkedList()
		switch referable {
			Factory: {
				val mf = referable.defn
				val methods = mf.commands.filter[include === null || include.empty?true:include.contains(it.name)].filter[!filterOut?.contains(it.name)]
				methods.forEach[
					val rep = new CommandRep()
					val tParam = it.formal.findFirst[it.name=='target'] 
					rep.name = it.name
					rep.hasTarget = tParam !== null?(type?getTypeName(tParam):"^target"):null
					rep.formalNames = it.formal.filter[it.name != 'target'].map[type?getTypeName(it):it.name].toList
					cmds.add(rep)
				]
			}
			DeclaredProperty : {
				if (ComponentType.MODEL == referable.component) {
					if (referable?.value?.value instanceof Component) {
						val factoryProp = (referable?.value?.value as Component).assignment.findFirst[it.name=='factory'].value.value
						if (factoryProp instanceof PropertyReference) {
							cmds.addAll(getCommandsFromReference(factoryProp as PropertyReference, include, filterOut, type))
						}
						else if (factoryProp instanceof Reference) {
							
						}
					}
				}
			}
		}
		cmds
	}
	
	static def getTypeName(FormalParam param) {
		if (param.type.ref instanceof JvmType) {
			return (param.type.ref as JvmType).simpleName
		}
		if (param.type.ref instanceof AnyTypeRef) {
			return XtendUtils.getAcmeTypeName((param.type.ref as AnyTypeRef))
		}
//		if (param.type.acme !== null) {
//			val ar = param.type.acme.referable
//			return XtendUtils.getAcmeTypeName(ar)
//		}
//		if (param.type.java != null) {
//			return param.type.java.referable.simpleName
//		}
		if (param.type.base !== null) {
			return param.type.base.getName()
		}

	}
	
}