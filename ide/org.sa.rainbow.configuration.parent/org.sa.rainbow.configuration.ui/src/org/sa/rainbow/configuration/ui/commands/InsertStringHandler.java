package org.sa.rainbow.configuration.ui.commands;
/*
Copyright 2020 Carnegie Mellon University

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software 
without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.
 */
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.sa.rainbow.configuration.ui.TokenTypeToPartitionMapper;

/**
 * Handler to allow easy typing of guillemet characters on windows
 * and linux, too.
 */
public abstract class InsertStringHandler extends AbstractHandler {

	public static final char LEFT_GUILLEMET = '\u00ab';
	public static final char RIGHT_GUILLEMET = '\u00bb';

	public static class LeftGuillemet extends InsertStringHandler {
		public LeftGuillemet() {
			super(LEFT_GUILLEMET);
		}
	}

	public static class RightGuillemet extends InsertStringHandler {
		public RightGuillemet() {
			super(RIGHT_GUILLEMET);
		}
	}

	private char replaceChar;

	protected InsertStringHandler(char replaceChar) {
		this.replaceChar = replaceChar;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
		
		
		if (editor != null) {
			String type = IDocument.DEFAULT_CONTENT_TYPE;
			try {
				int offset = editor.getInternalSourceViewer().getTextWidget().getCaretOffset();
				type = TextUtilities.getContentType(editor.getInternalSourceViewer().getDocument(), IDocumentExtension3.DEFAULT_PARTITIONING, offset, false);
			}
			catch (Exception e) {}
			// Hack, would be nicer with document edits, but this way we don't loose auto edit
			StyledText textWidget = editor.getInternalSourceViewer().getTextWidget();
			Event e = new Event();
			e.character = replaceChar;
			e.type = SWT.KeyDown;
			e.doit = true;
			textWidget.notifyListeners(SWT.KeyDown, e);
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(type)) {
				e = new Event();
				e.character = replaceChar;
				e.type = SWT.KeyDown;
				e.doit = true;
				textWidget.notifyListeners(SWT.KeyDown, e);
			}
		}
		return null;
	}
}