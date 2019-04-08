package org.sa.rainbow.gui.arch.controller;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public interface IRainbowUIController extends PropertyChangeListener{
	
	JInternalFrame createView(JDesktopPane parent);
	
	void setModel(RainbowArchModelElement model);

	RainbowArchModelElement getModel();

	JComponent getView();

	void move(Point2D realPoint, boolean b);
}
