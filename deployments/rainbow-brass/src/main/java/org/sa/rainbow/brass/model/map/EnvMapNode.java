package org.sa.rainbow.brass.model.map;

import java.util.HashMap;
import java.util.Map;

public class EnvMapNode {
    public String m_label;
    public double m_x, m_y;
    public int m_id;
    private final Map<String,Object> m_properties;

    public EnvMapNode (String m_label, double x, double y, int node_id) {
        super();
        this.m_label = m_label;
        this.m_x = x;
        this.m_y = y;
        this.m_id = node_id;
        
        m_properties = new HashMap<> ();
    }
   
    public void setProperty(String key, Object value) {
    	m_properties.put(key, value);
    }
    
    public Object getProperty(String key) {
    	return m_properties.get(key);
    }
        
    public String getLabel() {
        return m_label;
    }

    public void setLabel(String m_label) {
        this.m_label = m_label;
    }

    public double getX () {
        return m_x;
    }

    public void setX (double m_x) {
        this.m_x = m_x;
    }

    public double getY () {
        return m_y;
    }

    public void setY (double m_y) {
        this.m_y = m_y;
    }

    public int getId() {
        return m_id;
    }
}