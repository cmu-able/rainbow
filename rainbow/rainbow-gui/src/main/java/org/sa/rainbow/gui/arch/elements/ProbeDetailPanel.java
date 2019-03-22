package org.sa.rainbow.gui.arch.elements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

import javax.swing.SwingConstants;

public class ProbeDetailPanel extends JPanel {

	private BindingGroup m_bindingGroup;
	private org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes m_probeAttributes = new org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes();
	private JTextField m_kindNameJTextField;
	private JTextField m_locationJTextField;
	private JTextField m_nameJTextField;
	private JTextField m_typeJTextField;

	public ProbeDetailPanel(org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes newProbeAttributes) {
		this();
		setProbeAttributes(newProbeAttributes);
	}

	public ProbeDetailPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 1.0E-4 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
		setLayout(gridBagLayout);

		JLabel kindNameLabel = new JLabel("KindName:");
		GridBagConstraints labelGbc_0 = new GridBagConstraints();
		labelGbc_0.anchor = GridBagConstraints.EAST;
		labelGbc_0.insets = new Insets(5, 5, 5, 5);
		labelGbc_0.gridx = 0;
		labelGbc_0.gridy = 1;
		add(kindNameLabel, labelGbc_0);

		JLabel locationLabel = new JLabel("Location:");
		GridBagConstraints labelGbc_1 = new GridBagConstraints();
		labelGbc_1.anchor = GridBagConstraints.EAST;
		labelGbc_1.insets = new Insets(5, 0, 5, 5);
		labelGbc_1.gridx = 2;
		labelGbc_1.gridy = 0;
		add(locationLabel, labelGbc_1);

		m_locationJTextField = new JTextField();
		GridBagConstraints componentGbc_1 = new GridBagConstraints();
		componentGbc_1.insets = new Insets(5, 0, 5, 0);
		componentGbc_1.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_1.gridx = 3;
		componentGbc_1.gridy = 0;
		add(m_locationJTextField, componentGbc_1);

		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.insets = new Insets(5, 5, 5, 5);
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 0;
		add(nameLabel, gbc_nameLabel);

		m_nameJTextField = new JTextField();
		GridBagConstraints gbc_nameJTextField = new GridBagConstraints();
		gbc_nameJTextField.insets = new Insets(5, 0, 5, 5);
		gbc_nameJTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameJTextField.gridx = 1;
		gbc_nameJTextField.gridy = 0;
		add(m_nameJTextField, gbc_nameJTextField);

		m_kindNameJTextField = new JTextField();
		GridBagConstraints componentGbc_0 = new GridBagConstraints();
		componentGbc_0.insets = new Insets(5, 0, 5, 5);
		componentGbc_0.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_0.gridx = 1;
		componentGbc_0.gridy = 1;
		add(m_kindNameJTextField, componentGbc_0);

		JLabel typeLabel = new JLabel("Type:");
		GridBagConstraints gbc_typeLabel = new GridBagConstraints();
		gbc_typeLabel.anchor = GridBagConstraints.EAST;
		gbc_typeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_typeLabel.gridx = 0;
		gbc_typeLabel.gridy = 2;
		add(typeLabel, gbc_typeLabel);

		m_typeJTextField = new JTextField();
		GridBagConstraints gbc_typeJTextField = new GridBagConstraints();
		gbc_typeJTextField.insets = new Insets(5, 0, 5, 5);
		gbc_typeJTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_typeJTextField.gridx = 1;
		gbc_typeJTextField.gridy = 2;
		add(m_typeJTextField, gbc_typeJTextField);

		if (m_probeAttributes != null) {
			m_bindingGroup = initDataBindings();
		}
	}

	protected BindingGroup initDataBindings() {
		BeanProperty<org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes, java.lang.String> kindNameProperty = BeanProperty
				.create("kindName");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty = BeanProperty.create("text");
		AutoBinding<org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes, java.lang.String, javax.swing.JTextField, java.lang.String> autoBinding = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ, m_probeAttributes, kindNameProperty,
						m_kindNameJTextField, textProperty);
		autoBinding.bind();
		//
		BeanProperty<org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes, java.lang.String> locationProperty = BeanProperty
				.create("location");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty_1 = BeanProperty.create("text");
		AutoBinding<org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes, java.lang.String, javax.swing.JTextField, java.lang.String> autoBinding_1 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ, m_probeAttributes, locationProperty,
						m_locationJTextField, textProperty_1);
		autoBinding_1.bind();
		//
		BeanProperty<org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes, java.lang.String> nameProperty = BeanProperty
				.create("name");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty_2 = BeanProperty.create("text");
		AutoBinding<org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes, java.lang.String, javax.swing.JTextField, java.lang.String> autoBinding_2 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ, m_probeAttributes, nameProperty,
						m_locationJTextField, textProperty_2);
		autoBinding_2.bind();
		//
		BeanProperty<ProbeAttributes, String> typeProperty = BeanProperty.create("kind");
		BeanProperty<JTextField,String> textProperty_3 = BeanProperty.create("text");
		AutoBinding<ProbeAttributes,String,JTextField,String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, m_probeAttributes, typeProperty, m_typeJTextField, textProperty_3);
		autoBinding_3.bind();
		//
		BindingGroup bindingGroup = new BindingGroup();
		bindingGroup.addBinding(autoBinding);
		bindingGroup.addBinding(autoBinding_1);
		bindingGroup.addBinding(autoBinding_2);
		bindingGroup.addBinding(autoBinding_3);
		//

		return bindingGroup;
	}

	public org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes getProbeAttributes() {
		return m_probeAttributes;
	}

	public void setProbeAttributes(org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes newProbeAttributes) {
		setProbeAttributes(newProbeAttributes, true);
	}

	public void setProbeAttributes(org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes newProbeAttributes,
			boolean update) {
		m_probeAttributes = newProbeAttributes;
		if (update) {
			if (m_bindingGroup != null) {
				m_bindingGroup.unbind();
				m_bindingGroup = null;
			}
			if (m_probeAttributes != null) {
				m_bindingGroup = initDataBindings();
			}
		}
	}

}
