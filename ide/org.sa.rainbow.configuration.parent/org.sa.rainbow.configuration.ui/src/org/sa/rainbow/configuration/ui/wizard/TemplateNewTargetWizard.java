package org.sa.rainbow.configuration.ui.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.xtext.ui.wizard.template.AbstractFileTemplate;
import org.eclipse.xtext.ui.wizard.template.IFileGenerator;
import org.eclipse.xtext.ui.wizard.template.TemplateFileInfo;
import org.eclipse.xtext.ui.wizard.template.TemplateNewFileWizard;
import org.eclipse.xtext.util.StringInputStream;

public class TemplateNewTargetWizard extends TemplateNewFileWizard {
	public class TargetFileGenerator extends WorkspaceModifyOperation implements IFileGenerator {
		private final SortedMap<String, CharSequence> files = new TreeMap<>();

		@Override
		public void generate(CharSequence path, CharSequence content) {
			files.put(path.toString(), content);
		}
		
		

		@Override
		protected void execute(IProgressMonitor monitor)
				throws CoreException, InvocationTargetException, InterruptedException {
			SubMonitor subMonitor = SubMonitor.convert(monitor, files.size());
			try {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				for (Map.Entry<String, CharSequence> fileEntry : files.entrySet()) {
					
					
					Path path = new Path(fileEntry.getKey());
					IFile file = workspace.getRoot().getFile(path);
					File pt = new File(workspace.getRoot().getLocation().toFile(), file.getFullPath().toFile().getPath());
					
					File parent = pt.getParentFile();
					Stack<String> dir = new Stack<>();
					IContainer parent2 = file.getParent();
					while (!parent2.exists()) {
						dir.push(parent2.getName());
						parent2 = parent2.getParent();
					}
					while (!dir.isEmpty()) {
						parent2 = ((IFolder )parent2).getFolder(dir.pop());
						((IFolder )parent2).create(true, true, subMonitor);
					}
					
					file.create(new StringInputStream(fileEntry.getValue().toString()), true, subMonitor);
				}

			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			finally {
				subMonitor.done();
			}
			
		}

	}

	private static final Logger logger = Logger.getLogger(TemplateNewFileWizard.class);

	@Override
	protected void doFinish(TemplateFileInfo info, IProgressMonitor monitor) {
		try {
			AbstractFileTemplate fileTemplate = info.getFileTemplate();
			TargetFileGenerator fileGenerator = new TargetFileGenerator();

			fileTemplate.generateFiles(fileGenerator);
			fileGenerator.run(monitor);
		}  catch (final InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		} catch (final InterruptedException e) {
			// cancelled by user, ok
			return;
		}
	}
}
