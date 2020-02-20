/*
 * generated by Xtext 2.19.0
 */
package org.sa.rainbow.configuration.ui.quickfix

/*
 * Copyright 2020 Carnegie Mellon University

 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 *  persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

import java.util.ArrayList
import java.util.LinkedList
import java.util.regex.Pattern
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.nodemodel.ILeafNode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.ui.editor.model.IXtextDocument
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider
import org.eclipse.xtext.ui.editor.quickfix.Fix
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor
import org.eclipse.xtext.validation.Issue
import org.sa.rainbow.configuration.rcl.Assignment
import org.sa.rainbow.configuration.rcl.Component
import org.sa.rainbow.configuration.rcl.DeclaredProperty
import org.sa.rainbow.configuration.rcl.Effector
import org.sa.rainbow.configuration.rcl.EffectorBody
import org.sa.rainbow.configuration.rcl.Gauge
import org.sa.rainbow.configuration.rcl.GaugeBody
import org.sa.rainbow.configuration.rcl.Probe
import org.sa.rainbow.configuration.rcl.RclPackage
import org.sa.rainbow.configuration.validation.RclValidator

/**
 * Custom quickfixes.
 * 
 * See https://www.eclipse.org/Xtext/documentation/310_eclipse_support.html#quick-fixes
 */
class RclQuickfixProvider extends DefaultQuickfixProvider {

	@Fix(RclValidator.COMMAND_NOT_IN_GAUGE_TYPE)
	@Fix(RclValidator.NOT_VALID_GAUGE_COMMAND)
	def renameCommand(Issue issue, IssueResolutionAcceptor acceptor) {
		if (issue?.data?.length === 0) {
			return
		}
		val possibleRenames = new LinkedList<String>();
		for (name : issue.data.get(0).split(",")) {
			possibleRenames.add(name)
		}

		for (name : possibleRenames) {
			acceptor.accept(issue, "Rename to " + name, "Rename the command", null) [ context |
				val xtextDocument = context.xtextDocument
				xtextDocument.replace(issue.offset, issue.length, name);
			]
		}
	}

	@Fix(RclValidator.ONLY_EXTEND_PROBE_TYPES)
	@Fix(RclValidator.ONLY_EXTEND_EFFECTOR_TYPES)
	def renameToProbeType(Issue issue, IssueResolutionAcceptor acceptor) {
		if (issue?.data?.length == 0) {
			return
		}
		var reference = "type"
		if(issue.data.length == 2) reference = issue.data.get(1)
		val possiblePTs = new LinkedList<String>();
		for (name : issue.data.get(0).split(",")) {
			acceptor.accept(issue, "Rename to " + name, "Refer to " + reference, null) [ context |
				context.xtextDocument.replace(issue.offset, issue.length, name)
			]

		}
	}

//	@Inject
//	@Named(value="jvmtypes")
//	private IJvmTypeProvider.Factory jvmTypeProviderFactory;
//
//	@Inject
//	private IJavaProjectProvider projectProvider
//	
//	@Inject
//	private RawSuperTypes superTypeCollector;
//  # From JdtTypesProvider
//	@Fix(RclValidator.MUST_SUBCLASS)
//	def getSubclasses(Issue issue, IssueResolutionAcceptor acceptor) {
//		if (issue?.data?.length == 0) {
//			return
//		}
//		val scName = issue.data.get(0)
//
//		val modificationContext = modificationContextFactory.createModificationContext(issue);
//		val xtextDocument = modificationContext.xtextDocument
//		if(xtextDocument === null) return;
//
//		xtextDocument.tryReadOnly(new CancelableUnitOfWork<java.lang.Void, XtextResource>() {
//
//			var IssueResolutionAcceptor myAcceptor;
//
//			override exec(XtextResource state, CancelIndicator cancelIndicator) throws Exception {
//				myAcceptor = getCancelableAcceptor(acceptor, cancelIndicator);
//				val target = state.getEObject(issue.uriToProblem.fragment);
//				val project = projectProvider.getJavaProject(target.eResource.resourceSet)
//				if (project === null) {
//					return null
//				}
//				val typeProvider = jvmTypeProviderFactory.createTypeProvider(target.eResource.resourceSet)
//				val superclass = typeProvider.findTypeByName(scName)
//				val superTypeIdentifier = superclass.identifier
//				try {
//					val superTypeNames = newHashSet
//					val scope = createSearchScope(project, superclass, superTypeNames);
//					searchAndCreateFixes(scope, myAcceptor, issue, new ITypesProposalProvider.Filter() {
//						
//						override accept(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
//							if (path == null || path.endsWith(".class") || path.endsWith(".java")) { // Java index match
//								val identifier = getIdentifier(packageName, simpleTypeName, enclosingTypeNames);
//								if (!superTypeNames.contains(identifier)) {
//									return true;
//								}
//								return false;
//							} else { // accept match from dirty state
//								val identifier = getIdentifier(packageName, simpleTypeName, enclosingTypeNames);
//								if (identifier.equals(superTypeIdentifier)) {
//									return true;
//								}
//								val candidate = typeProvider.findTypeByName(identifier, true);
//								if (superTypeCollector.collect(candidate).contains(superclass)) {
//									return true;
//								}
//								return false;
//							}
//						}
//						
//						
//						
//						override getSearchFor() {
//							return IJavaSearchConstants.CLASS;
//						}
//						
//					}
//					)
//				}
//				catch (JavaModelException ex) {}
//				return null;
//
//			}
//			
//			
//
//		})
//
//	}
//	
//	def searchAndCreateFixes(IJavaSearchScope scope, IssueResolutionAcceptor acceptor, Issue issue, Filter filter) {
//		val prefix = context.prefix
//	}
//	
//	def getIdentifier(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames) {
//		val result = new StringBuilder(packageName.length  + simpleTypeName.length + 1)
//		if (packageName.length !== 0) {
//			result.append(packageName)
//			result.append('.')
//		}
//		for (enclosingType : enclosingTypeNames) {
//			result.append(enclosingType)
//			result.append("$")
//		}
//		result.append(simpleTypeName)
//		result.toString
//	}
//
//	def createSearchScope(IJavaProject project, JvmType superType, HashSet<String> superTypeNames) {
//		val type = project.findType(superType.getIdentifier())
//		if (type == null) {
//			return new IntersectingJavaSearchScope
//		}
//		try {
//			val result = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null)
//			return result
//		} catch (JavaModelException e) {
//			val superTypes = superTypeCollector.collect(superType)
//			for (collectedSuperType : superTypes) {
//				superTypeNames.add(collectedSuperType.identifier)
//			}
//			superTypeNames.remove(superType.identifier)
//			val hierarchyScope = SearchEngine.createHierarchyScope(type)
//			val projectScope = SearchEngine.createJavaSearchScope(#[project])
//			return new IntersectingJavaSearchScope(projectScope, hierarchyScope)
//		}
//	}

//	@Fix(RclValidator.INVALID_NAME)
//	def capitalizeName(Issue issue, IssueResolutionAcceptor acceptor) {
//		acceptor.accept(issue, 'Capitalize name', 'Capitalize the name.', 'upcase.png') [
//			context |
//			val xtextDocument = context.xtextDocument
//			val firstLetter = xtextDocument.get(issue.offset, 1)
//			xtextDocument.replace(issue.offset, 1, firstLetter.toUpperCase)
//		]
//	}
//	@Fix(RclValidator.MUST_SUBCLASS)
//	def chooseSubclass(Issue issue, IssueResolutionAcceptor acceptor) {
//		if (issue.data?.length != 1) return
//		val subclasses = new LinkedList<String> ();
//		if (!issue.data.get(0).contains(",")) {
//			subclasses.add(issue.data.get(0));
//		}
//		else {
//			for (sc : issue.data.get(0).split(",")) {
//				subclasses.add(sc);
//			}
//		}
//		for (sc : subclasses) {
//			
//		}
//	}
//	@Inject
//	private ReplacingAppendable.Factory appendableFactory;
	@Fix(RclValidator.MISSING_PROPERTY)
	def addMissingProperty(Issue issue, IssueResolutionAcceptor acceptor) {
		if (issue.data?.length === 0) {
			return;
		}
		val possibleProperties = new LinkedList<String>()
		for (prop : issue.data.get(0).split(",")) {
			possibleProperties.add(prop);
		}
		for (prop : possibleProperties) {
			acceptor.accept(
				issue,
				"Add '" + prop + '"',
				"Add '" + prop + '"',
				null,
				new ISemanticModification() {

					override apply(EObject element, IModificationContext context) throws Exception {
						val document = context.xtextDocument
						if (element instanceof Gauge) {
							val gb = (element as Gauge).body
							val insertOffset = getInsertOffset(gb)
							val indent = getIndent(element as Gauge, insertOffset)
							var t = "\n" + indent + prop + " = "
							if (issue.data.length == 2 && issue.data.get(1) == Component.name) {
								t = t + "{\n" + indent + "\t\n" + indent + "}"

							}
							document.replace(insertOffset, 0, t);
						} else if (element instanceof DeclaredProperty &&
							(element as DeclaredProperty).value.value instanceof Component) {
							insertProperty((element as DeclaredProperty).value.value as Component, element, prop, document)

						} else if (element instanceof Probe) {
							insertProperty((element as Probe).properties, element, prop, document)
						} else if (element instanceof Effector) {
							val eb = (element as Effector).body
							val insertOffset = getInsertOffset(eb)
							val indent = getIndent(element, insertOffset)
							var t = "\n" + indent + prop + " = "
							if (issue.data.length == 2 && issue.data.get(1) == Component.name) {
								t = t + "{\n" + indent + "\t\n" + indent + "}"

							}
							document.replace(insertOffset, 0, t);
						}
						else if (element instanceof Assignment && (element as Assignment)?.value?.value instanceof Component) {
							insertProperty(((element as Assignment)?.value?.value as Component), element, prop, document)
						}
					}

					def insertProperty(Component into, EObject element, String prop, IXtextDocument document) {
						val insertOffset = getInsertOffset(into)
						val indent = getIndent(element, insertOffset)
						var t = "\n" + indent + (into.assignment.empty ? "\t" : "") + prop + " = "
						document.replace(insertOffset, 0, t)
					}

					def getIndent(EObject g, int insertOffset) {
						val root = NodeModelUtils.findActualNodeFor(g)
						val r = determineOffset(root, insertOffset)

						// go backwards until first linewrap
						val p = Pattern.compile("(\\n|\\r)([ \\t]*)");
						for (var i = r.size() - 1; i >= 0; i--) {
							val m = p.matcher(r.get(i).getText());
							if (m.find()) {
								var ind = m.group(2);
								while (m.find())
									ind = m.group(2);
								return ind;
							}
						}
						return "";

					}

					def determineOffset(ICompositeNode root, int fromOffset) {
						val r = new ArrayList<ILeafNode>();
						// add all nodes until fromOffset
						for (l : root.getLeafNodes()) {
							if (l.getOffset() >= fromOffset) {
								return r;

							} else {
								r.add(l);
							}
						}
					}

					def getInsertOffset(Component c) {
						if (c.assignment.empty) {
							val node = NodeModelUtils.findActualNodeFor(c)
							val openingBraceNode = node.leafNodes.findFirst[text == "{"]
							if (openingBraceNode !== null) {
								openingBraceNode.offset + 1
							} else
								node.offset
						} else {
							NodeModelUtils.findActualNodeFor(c.assignment.last).endOffset
						}
					}

					def getInsertOffset(EffectorBody gb) {
						if (gb.assignment.empty) {
							val node = NodeModelUtils.findActualNodeFor(gb)
							val openingBraceNode = node.leafNodes.findFirst[text == "{"]
							if (openingBraceNode !== null) {
								openingBraceNode.offset + 1
							} else
								node.offset
						} else {
							NodeModelUtils.findActualNodeFor(gb.command).endOffset
						}
					}

					def getInsertOffset(GaugeBody gb) {
						if (gb.assignment.empty) {
							val node = NodeModelUtils.findActualNodeFor(gb)
							val openingBraceNode = node.leafNodes.findFirst[text == "{"]
							if (openingBraceNode !== null) {
								openingBraceNode.offset + 1
							} else
								node.offset
						} else if (!gb.commands.empty) {
							NodeModelUtils.findActualNodeFor(gb.commands.last).endOffset
						} else if (gb.modeltype !== null) {
							NodeModelUtils.findNodesForFeature(gb, RclPackage.Literals.GAUGE_BODY__MODELTYPE).last.
								endOffset
						}
						return NodeModelUtils.findNodesForFeature(gb, RclPackage.Literals.GAUGE_BODY__REF).last.
							endOffset

					}

				}
			)
		}
	}
}
