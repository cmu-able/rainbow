package org.sa.rainbow.brass.model.map;

public class EnvMapArc {
    public String m_source, m_target;
    public double  m_distance;
    public boolean m_enabled;

    public EnvMapArc (String m_source, String m_target, double m_distance, boolean m_enabled) {
        super();
        this.m_source = m_source;
        this.m_target = m_target;
        this.m_distance = m_distance;
        this.m_enabled = m_enabled;
    }
    public String getSource() {
        return m_source;
    }
    public void setSource(String m_source) {
        this.m_source = m_source;
    }
    public String getTarget() {
        return m_target;
    }
    public void setTarget(String m_target) {
        this.m_target = m_target;
    }

    public double getDistance () {
        return m_distance;
    }

    public void setDistance (double m_distance) {
        this.m_distance = m_distance;
    }
    public boolean isEnabled() {
        return m_enabled;
    }
    public void setEnabled(boolean m_enabled) {
        this.m_enabled = m_enabled;
    }
    public boolean includesNode(String node){
        return (m_source.equals(node) || m_target.equals(node));
    }
    public boolean includesNodes(String nodea, String nodeb){
        return (includesNode(nodea) && includesNode(nodeb));
    }

}