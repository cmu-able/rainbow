package incubator.qxt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.Date;

public class SampleQxtBean implements Cloneable {
	private PropertyChangeSupport pcs;
	private int id;
	private int age;
	private String name;
	private boolean intelligent;
	private String sex;
	private Date lastAccess;

	public SampleQxtBean() {
		pcs = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		int oldId = this.id;

		this.id = id;

		pcs.firePropertyChange("id", oldId, id);
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) throws PropertyVetoException {
		if (age > 150 || age < 0) {
			PropertyChangeEvent pce = new PropertyChangeEvent(this, "age",
					this.age, age);
			throw new PropertyVetoException("Illegal age change.", pce);
		}

		int oldAge = this.age;

		this.age = age;

		pcs.firePropertyChange("age", oldAge, age);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;

		this.name = name;

		pcs.firePropertyChange("name", oldName, name);
	}

	public boolean isIntelligent() {
		return intelligent;
	}

	public void setIntelligent(boolean intelligent) {
		boolean oldIntelligent = this.intelligent;

		this.intelligent = intelligent;

		pcs.firePropertyChange("intelligent", oldIntelligent, intelligent);
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		String oldSex = this.sex;

		this.sex = sex;

		pcs.firePropertyChange("sex", oldSex, sex);
	}

	public Date getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Date lastAccess) {
		Date oldLastAccess = this.lastAccess;

		this.lastAccess = lastAccess;

		pcs.firePropertyChange("lastAccess", oldLastAccess, lastAccess);
	}

	@Override
	public SampleQxtBean clone() throws CloneNotSupportedException {
		SampleQxtBean b = (SampleQxtBean) super.clone();
		b.pcs = new PropertyChangeSupport(b);
		b.age = this.age;
		b.setId(getId());
		b.setIntelligent(isIntelligent());
		b.setLastAccess(getLastAccess());
		b.setName(getName());
		b.setSex(getSex());
		return b;
	}
}
