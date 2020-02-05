package org.sa.rainbow.configuration.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.common.types.xtext.ui.JdtHyperlink;
import org.eclipse.xtext.common.types.xtext.ui.TypeAwareHyperlinkHelper;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkAcceptor;
import org.eclipse.xtext.ui.editor.hyperlinking.XtextHyperlink;
import org.sa.rainbow.configuration.rcl.Factory;
import org.sa.rainbow.configuration.rcl.Import;
import org.sa.rainbow.configuration.rcl.RclPackage;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RclHyperlinkHelper extends TypeAwareHyperlinkHelper {

//	@Inject
//	private IQualifiedNameProvider qualifiedNameProvider;
//
//	@Inject
//	private IQualifiedNameConverter qualifiedNameConverter;
//
	@Inject
	private Provider<XtextHyperlink> hyperlinkProvider;

	@Override
	public void createHyperlinksByOffset(XtextResource resource, int offset, IHyperlinkAcceptor acceptor) {
		// TODO Auto-generated method stub
		super.createHyperlinksByOffset(resource, offset, acceptor);

		EObject eObject = getEObjectAtOffsetHelper().resolveElementAt(resource, offset);
		 if (eObject instanceof Import) {
			Import import1 = (Import) eObject;
			List<INode> nodes = NodeModelUtils.findNodesForFeature(eObject, RclPackage.Literals.IMPORT__IMPORT_URI);
			if (!nodes.isEmpty()) {
				INode node = nodes.get(0);
				XtextHyperlink hyperlink = hyperlinkProvider.get();
				hyperlink.setHyperlinkRegion(new Region(node.getOffset(), node.getLength()));
				hyperlink.setHyperlinkText("Open imported file");
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject myProject = root.getProject(eObject.eResource().getURI().segment(1));
				URI cd = eObject.eResource().getURI().trimSegments(1);
				
				String location = cd.path().replace("/resource/"+eObject.eResource().getURI().segment(1) +"/", "");
				if (myProject.getFile("/" + location + "/" + import1.getImportURI()).exists()) {
					location = "platform:/resource/" + eObject.eResource().getURI().segment(1) + "/" +
							location + "/" + import1.getImportURI();
					URI importUri = URI.createURI(location);
					hyperlink.setURI(importUri); 
					acceptor.accept(hyperlink);
				}
				
			}
		}

	}
}
