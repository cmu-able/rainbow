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
	private JLabel m_alias;
	private JTextField m_aliasTextField;

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

		JLabel locationLabel = new JLabel("Location:");
		GridBagConstraints labelGbc_1 = new GridBagConstraints();
		labelGbc_1.anchor = GridBagConstraints.EAST;
		labelGbc_1.insets = new Insets(5, 0, 5, 5);
		labelGbc_1.gridx = 2;
		labelGbc_1.gridy = 0;
		add(locationLabel, labelGbc_1);

		m_locationJTextField = new JTextField();
		m_locationJTextField.setEditable(false);
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
		m_nameJTextField.setEditable(false);
		GridBagConstraints gbc_nameJTextField = new GridBagConstraints();
		gbc_nameJTextField.insets = new Insets(5, 0, 5, 5);
		gbc_nameJTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameJTextField.gridx = 1;
		gbc_nameJTextField.gridy = 0;
		add(m_nameJTextField, gbc_nameJTextField);

		JLabel typeLabel = new JLabel("Type:");
		GridBagConstraints gbc_typeLabel = new GridBagConstraints();
		gbc_typeLabel.anchor = GridBagConstraints.EAST;
		gbc_typeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_typeLabel.gridx = 0;
		gbc_typeLabel.gridy = 1;
		add(typeLabel, gbc_typeLabel);

		m_typeJTextField = new JTextField();
		m_typeJTextField.setEditable(false);
		GridBagConstraints gbc_typeJTextField = new GridBagConstraints();
		gbc_typeJTextField.insets = new Insets(5, 0, 5, 5);
		gbc_typeJTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_typeJTextField.gridx = 1;
		gbc_typeJTextField.gridy = 1;
		add(m_typeJTextField, gbc_typeJTextField);

		m_alias = new JLabel("Reporting As:");
		GridBagConstraints gbc_alias = new GridBagConstraints();
		gbc_alias.anchor = GridBagConstraints.EAST;
		gbc_alias.insets = new Insets(5, 0, 5, 5);
		gbc_alias.gridx = 2;
		gbc_alias.gridy = 1;
		add(m_alias, gbc_alias);

		m_aliasTextField = new JTextField();
		m_aliasTextField.setEditable(false);
		GridBagConstraints gbc_aliasTextField = new GridBagConstraints();
		gbc_aliasTextField.insets = new Insets(5, 0, 5, 0);
		gbc_aliasTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_aliasTextField.gridx = 3;
		gbc_aliasTextField.gridy = 1;
		add(m_aliasTextField, gbc_aliasTextField);
		m_aliasTextField.setColumns(10);

		JLabel kindNameLabel = new JLabel("Kind");
		GridBagConstraints labelGbc_0 = new GridBagConstraints();
		labelGbc_0.anchor = GridBagConstraints.EAST;
		labelGbc_0.insets = new Insets(5, 5, 5, 5);
		labelGbc_0.gridx = 0;
		labelGbc_0.gridy = 2;
		add(kindNameLabel, labelGbc_0);

		m_kindNameJTextField = new JTextField();
		m_kindNameJTextField.setEditable(false);
		GridBagConstraints componentGbc_0 = new GridBagConstraints();
		componentGbc_0.insets = new Insets(5, 0, 5, 5);
		componentGbc_0.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_0.gridx = 1;
		componentGbc_0.gridy = 2;
		add(m_kindNameJTextField, componentGbc_0);

		if (m_probeAttributes != null) {
			initDataBindings();
		}
	}

	protected void initDataBindings() {
		m_kindNameJTextField.setText(m_probeAttributes.kindName);
		m_locationJTextField.setText(m_probeAttributes.location);
		m_nameJTextField.setText(m_probeAttributes.name);;
		m_typeJTextField.setText("n/a");
		m_aliasTextField.setText(m_probeAttributes.alias);
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
		initDataBindings();
	}

}
