package org.sa.rainbow.configuration.ui;

import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultSemanticHighlightingCalculator;
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.util.CancelIndicator;
import org.sa.rainbow.configuration.ConfigAttributeConstants;
import org.sa.rainbow.configuration.XtendUtils;
import org.sa.rainbow.configuration.configModel.Assignment;
import org.sa.rainbow.configuration.configModel.Component;
import org.sa.rainbow.configuration.configModel.ConfigModelPackage;
import org.sa.rainbow.configuration.configModel.DeclaredProperty;
import org.sa.rainbow.configuration.configModel.Effector;
import org.sa.rainbow.configuration.configModel.Gauge;
import org.sa.rainbow.configuration.configModel.GaugeTypeBody;
import org.sa.rainbow.configuration.configModel.Probe;
import org.sa.rainbow.configuration.configModel.RichStringLiteral;
import org.sa.rainbow.configuration.configModel.RichStringPart;

public class ConfigModelHighlighter extends DefaultSemanticHighlightingCalculator {
	@Override
	protected void doProvideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor,
			CancelIndicator cancelIndicator) {
		EObject rootObject = resource.getParseResult().getRootASTElement();
		
		for (RichStringLiteral rs : EcoreUtil2.getAllContentsOfType(rootObject, RichStringLiteral.class)) {
			for (INode node : NodeModelUtils.findNodesForFeature(rs, ConfigModelPackage.Literals.RICH_STRING_LITERAL__VALUE)) {
				acceptor.addPosition(node.getOffset(), node.getLength(), DefaultHighlightingConfiguration.STRING_ID);
			}
		}
		for (RichStringPart rs : EcoreUtil2.getAllContentsOfType(rootObject,RichStringPart.class)) {
			for (INode node : NodeModelUtils.findNodesForFeature(rs,  ConfigModelPackage.Literals.RICH_STRING_PART__REFERABLE)) {
				acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.PROPERTY_REFERENCE_ID);
			}
		}
		for (Gauge g : EcoreUtil2.getAllContentsOfType(rootObject, Gauge.class)) {
			for (Assignment a : EcoreUtil2.getAllContentsOfType(g, Assignment.class)) {
//				if (a.eContainer().eContainer() instanceof Gauge) {
					if (ConfigAttributeConstants.GAUGE_KEYWORDS.contains(a.getName())) {
						for (INode node : NodeModelUtils.findNodesForFeature(a, ConfigModelPackage.Literals.ASSIGNMENT__NAME)) {
							acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.SOFT_KEYWORD_ID);
						}
					}
//				}
			}
		}
		for (GaugeTypeBody g : EcoreUtil2.getAllContentsOfType(rootObject, GaugeTypeBody.class)) {
			for (Assignment a : EcoreUtil2.getAllContentsOfType(g, Assignment.class)) {
//				if (a.eContainer().eContainer() instanceof Gauge) {
					if (ConfigAttributeConstants.GAUGE_KEYWORDS.contains(a.getName())) {
						for (INode node : NodeModelUtils.findNodesForFeature(a, ConfigModelPackage.Literals.ASSIGNMENT__NAME)) {
							acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.SOFT_KEYWORD_ID);
						}
					}
//				}
			}
		}
		for (Probe g : EcoreUtil2.getAllContentsOfType(rootObject, Probe.class)) {
			for (Assignment a : EcoreUtil2.getAllContentsOfType(g, Assignment.class)) {
//				if (a.eContainer().eContainer() instanceof Gauge) {
					if (ConfigAttributeConstants.PROBE_KEYWORDS.contains(a.getName())) {
						for (INode node : NodeModelUtils.findNodesForFeature(a, ConfigModelPackage.Literals.ASSIGNMENT__NAME)) {
							acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.SOFT_KEYWORD_ID);
						}
					}
//				}
			}
		}
		for (Effector e : EcoreUtil2.getAllContentsOfType(rootObject, Effector.class)) {
			for (Assignment a : EcoreUtil2.getAllContentsOfType(e, Assignment.class)) {
				if (ConfigAttributeConstants.EFFECTOR_KEYOWRDS.contains(a.getName())) {
					for (INode node : NodeModelUtils.findNodesForFeature(a, ConfigModelPackage.Literals.ASSIGNMENT__NAME)) {
						acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.SOFT_KEYWORD_ID);
					}
				}
			}
		}
		for (Component c : EcoreUtil2.getAllContentsOfType(rootObject, Component.class)) {
			DeclaredProperty p = EcoreUtil2.getContainerOfType(c, DeclaredProperty.class);
			if (p != null) {
				for (Assignment a : EcoreUtil2.getAllContentsOfType(p, Assignment.class)) {
					Set<String> kw = Collections.<String>emptySet();
					switch (p.getComponent()) {
					case ANALYSIS:
						kw = ConfigAttributeConstants.ANALYSIS_KEYWORDS;
						highlightSoftKeyword(acceptor, a, kw);
						break;
					case EFFECTORMANAGER:
						kw = ConfigAttributeConstants.EFFECTOR_MANAGER_KEYWORDS;
						highlightSoftKeyword(acceptor, a, kw);
						break;
					case EXECUTOR:
						kw = ConfigAttributeConstants.EXECUTOR_KEYWORDS;
						highlightSoftKeyword(acceptor, a, kw);
						break;
					case GUI:
						if (XtendUtils.isKeyProperty(ConfigAttributeConstants.GUI_PROPERTY_TUPES, a)) {
							for (INode node : NodeModelUtils.findNodesForFeature(a, ConfigModelPackage.Literals.ASSIGNMENT__NAME)) {
								acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.SOFT_KEYWORD_ID);
							}
						}
						break;
					case MANAGER:
						kw = ConfigAttributeConstants.MANAGER_KEYWORDS;
						highlightSoftKeyword(acceptor, a, kw);
						break;
					case MODEL:
						kw = ConfigAttributeConstants.MODEL_KEYWORDS;
						highlightSoftKeyword(acceptor, a, kw);
						break;
					case UTILITY:
						kw = ConfigAttributeConstants.UTILITY_KEYWORDS;
						highlightSoftKeyword(acceptor, a, kw);
					case PROPERTY:
						break;
					default:
						;
					
					}
				}
			}
			
		}
		super.doProvideHighlightingFor(resource, acceptor, cancelIndicator);
	}

	protected void highlightSoftKeyword(IHighlightedPositionAcceptor acceptor, Assignment a, Set<String> kw) {
		if (kw.contains(a.getName())) {
			for (INode node : NodeModelUtils.findNodesForFeature(a, ConfigModelPackage.Literals.ASSIGNMENT__NAME)) {
				acceptor.addPosition(node.getOffset(), node.getLength(), ConfigModelHighlightingConfiguration.SOFT_KEYWORD_ID);
			}
		}
	}
}
