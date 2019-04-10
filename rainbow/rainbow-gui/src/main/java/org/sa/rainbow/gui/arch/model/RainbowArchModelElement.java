package org.sa.rainbow.gui.arch.model;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.sa.rainbow.gui.arch.controller.IRainbowUIController;

public abstract class RainbowArchModelElement {

	public static final String ERROR_PROPERTY = "error";
	public  static final String LOCATION_PROPERTY = "location";
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private Point m_location;
	private boolean m_hasError;
	private String m_errorMessage;

	private IRainbowUIController m_cntrller;

	public RainbowArchModelElement() {
		super();
	
	}
	
	public void setController (IRainbowUIController cntrl) {
		pcs.addPropertyChangeListener(cntrl);
		m_cntrller = cntrl;
	}

	public void setUserLocation(Point location) {
		Point oldLoc = m_location;
		m_location = location;
		pcs.firePropertyChange(LOCATION_PROPERTY, oldLoc, m_location);
	}

	public Point getLocation() {
		return m_location;
	}

	public IRainbowUIController getController() {
		return m_cntrller;
	}

	public void setError(String errorMessage) {
		boolean hadError = m_hasError;
		m_errorMessage = errorMessage;
		m_hasError = true;
		if (!hadError)
			pcs.firePropertyChange(ERROR_PROPERTY, hadError, m_hasError);
	}

	public boolean hasError() {
		return m_hasError;
	}

	public String getErrorMessage() {
		return m_errorMessage;
	}

	public void clearError() {
		boolean hadError = m_hasError;
		m_hasError = false;
		m_errorMessage = null;
		if (hadError)
			pcs.firePropertyChange(ERROR_PROPERTY, hadError, m_hasError);
	}

	public abstract String getId();

	public void addPropertyChangeListener (PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

}
