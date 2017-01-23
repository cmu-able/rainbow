package org.sa.rainbow.brass.model.map;

public class EnvMapNode {
    public String m_label;
    public float m_x, m_y;
    public int m_id;

	public EnvMapNode(String m_label, float m_x, float m_y, int node_id) {
		super();
		this.m_label = m_label;
		this.m_x = m_x;
		this.m_y = m_y;
		this.m_id= node_id;
	}

	public String getLabel() {
		return m_label;
	}

	public void setLabel(String m_label) {
		this.m_label = m_label;
	}

	public float getX() {
		return m_x;
	}

	public void setX(float m_x) {
		this.m_x = m_x;
	}

	public float getY() {
		return m_y;
	}

	public void setY(float m_y) {
		this.m_y = m_y;
	}
	
	public int getId() {
		return m_id;
	}
}