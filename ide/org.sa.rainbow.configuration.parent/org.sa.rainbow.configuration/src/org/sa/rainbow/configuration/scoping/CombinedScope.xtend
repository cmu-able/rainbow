package org.sa.rainbow.configuration.scoping

import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.resource.IEObjectDescription

class CombinedScope implements IScope {

	IScope[] m_scopes

	new(IScope... scopes) {
		m_scopes = scopes;
	}

	override getAllElements() {
		m_scopes.map[it.getAllElements()].flatten
	}

	override getElements(QualifiedName name) {
		m_scopes.map[it.getElements(name)].flatten
	}

	override getElements(EObject object) {
		m_scopes.map[it.getElements(object)].flatten
	}

	override getSingleElement(QualifiedName name) {
		var IEObjectDescription se = null;
		for (var i = 0; i < m_scopes.length && se === null; i++) {
			se = m_scopes.get(i).getSingleElement(name);
		}
		se
	}

	override getSingleElement(EObject object) {
		var IEObjectDescription se = null;
		for (var i = 0; i < m_scopes.length && se === null; i++) {
			se = m_scopes.get(i).getSingleElement(object);
		}
		se
	}

}
