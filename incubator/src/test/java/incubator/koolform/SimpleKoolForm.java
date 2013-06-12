package incubator.koolform;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import incubator.koolform.KoolForm;

public class SimpleKoolForm {
	public static void main(String[] args) throws Exception {
		JFrame fr = new JFrame("Simple form");
		fr.setLayout(new BorderLayout());
		fr.add(new SimpleForm(), BorderLayout.CENTER);
		fr.pack();
		fr.setVisible(true);

		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private static class SimpleForm extends KoolForm {
		private static final long serialVersionUID = 1L;

		private SimpleForm() {
			renameSection("Section 1");
			addComponent("Text field", makeTextFieldLimited(10, 20));
			addComponent("Password field", makePasswordFieldLimited(10, 20));
			addScrollableComponent("Text area", makeTextAreaLimited(5, 15,
					500));
			addComponent("Dates", makeDatePicker());
			advanceColumn();
			addComponent("Text field 2", makeTextFieldLimited(10, 30));
			addScrollableComponent("Text area 2", makeTextAreaLimited(7, 25,
					100));
			advanceSection("Section 2");
			addComponent("Date 3", makeDatePicker());
		}
	}
}
