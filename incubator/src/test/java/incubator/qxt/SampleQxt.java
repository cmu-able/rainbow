package incubator.qxt;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.apache.commons.lang.RandomStringUtils;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import incubator.obscol.ObservableList;
import incubator.obscol.WrapperObservableList;
import incubator.qxt.LineEditorListener;
import incubator.qxt.LineFactory;
import incubator.qxt.PopupProvider;
import incubator.qxt.QxtCachedComputedProperty;
import incubator.qxt.QxtCheckBoxBooleanProperty;
import incubator.qxt.QxtComboBoxProperty;
import incubator.qxt.QxtComputedProperty;
import incubator.qxt.QxtDateProperty;
import incubator.qxt.QxtIntegerProperty;
import incubator.qxt.QxtStringProperty;
import incubator.qxt.QxtTable;
import incubator.qxt.Validator;

/*
 * Some notes: age > 150 is vetoed by the bean. The validator does not
 * accept age >= 100. Cannot commit beans whose name has < 2 characters.
 */
public class SampleQxt extends JXFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final QxtTable<SampleQxtBean> qxt;
	private final ObservableList<SampleQxtBean> beans;
	private final ObservableList<String> sexes;
	private final transient Validator<SampleQxtBean> validator;
	private final transient PopupProvider<SampleQxtBean> popupp;

	public SampleQxt() {
		super("Sample QXT");

		beans = new WrapperObservableList<>(
				new ArrayList<SampleQxtBean>());
		sexes = new WrapperObservableList<>(new ArrayList<String>());
		sexes.add("Male");
		sexes.add("Female");
		sexes.add("Undefined");
		SampleQxtBeanCreator.sexes = sexes;

		validator = new Validator<SampleQxtBean>() {
			@Override
			public boolean isValid(SampleQxtBean t) {
				if (t != null && t.getAge() >= 0 && t.getAge() < 100) {
					return true;
				} else {
					return false;
				}
			}
		};

		popupp = new PopupProvider<SampleQxtBean>() {
			private JPopupMenu global;
			private JPopupMenu specific;
			private JMenuItem ageItem;

			@Override
			public JPopupMenu getNonRowMenu() {
				if (global == null) {
					global = new JPopupMenu("Global");
					JMenuItem globalMi = new JMenuItem(
							"Please select something");
					global.add(globalMi);
				}

				return global;
			}

			@Override
			public JPopupMenu getRowMenu(SampleQxtBean t) {
				if (specific == null) {
					specific = new JPopupMenu("Specific");
					ageItem = new JMenuItem();
					specific.add(ageItem);
				}

				ageItem.setText("Increase age from " + t.getAge());

				return specific;
			}

			@Override
			public boolean popupRequiresSelection() {
				return true;
			}

		};

		JMenuItem create10top = new JMenuItem("Create 10 items on top");
		create10top.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(5000);
						} catch (Exception e) {
							e.printStackTrace();
						}

						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 10; i++) {
									beans.add(0, SampleQxtBeanCreator
											.create());
								}
							}
						});
					}
				}).start();
			}
		});

		JMenuItem create10bot = new JMenuItem("Create 10 items on bottom");
		create10bot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 10; i++) {
					beans.add(SampleQxtBeanCreator.create());
				}
			}
		});

		JMenuItem deleteSelected = new JMenuItem("Delete selected");
		deleteSelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (SampleQxtBean b : qxt.listSelected()) {
					beans.remove(b);
				}
			}
		});

		JMenuItem changeSelected = new JMenuItem("Change selected");
		changeSelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (SampleQxtBean b : qxt.listSelected()) {
					SampleQxtBeanCreator.updateRandomly(b);
				}
			}
		});

		JMenuItem generateNewSex = new JMenuItem("Create new sex");
		generateNewSex.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sexes.add("Sex " + RandomStringUtils.randomAlphabetic(3));
			}
		});

		final JCheckBoxMenuItem lineMode = new JCheckBoxMenuItem("Line mode");
		lineMode.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				qxt.setInLineMode(lineMode.isSelected());
			}
		});

		JMenuItem multipleSelection = new JMenuItem("Activate multiple "
				+ "selection");
		multipleSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				qxt.getSelectionModel().setSelectionMode(
						ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			}
		});

		final JCheckBoxMenuItem newLine = new JCheckBoxMenuItem("New line");
		newLine.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				qxt.setAutomaticLineCreationEnabled(newLine.isSelected());
			}
		});

		final JCheckBoxMenuItem showStatus = new JCheckBoxMenuItem(
				"Show status");
		showStatus.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				qxt.setShowStatusColumn(showStatus.isSelected());
			}
		});

		final JCheckBoxMenuItem validatorMi = new JCheckBoxMenuItem(
				"Validator");
		validatorMi.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				qxt
						.setValidator(validatorMi.isSelected() ? validator
								: null);
			}
		});

		final JCheckBoxMenuItem autoCopy = new JCheckBoxMenuItem("Auto copy");
		autoCopy.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				qxt.setAutoBeanCopyMode(autoCopy.isSelected());
			}
		});

		final JCheckBoxMenuItem popup = new JCheckBoxMenuItem("Popup");
		popup.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				qxt.setPopupProvider(popup.isSelected() ? popupp : null);
			}
		});

		JMenu m = new JMenu("QXT");
		m.add(create10top);
		m.add(create10bot);
		m.add(deleteSelected);
		m.add(changeSelected);
		m.add(generateNewSex);
		m.add(lineMode);
		m.add(multipleSelection);
		m.add(newLine);
		m.add(showStatus);
		m.add(validatorMi);
		m.add(autoCopy);
		m.add(popup);

		JMenuBar mb = new JMenuBar();
		mb.add(m);

		setJMenuBar(mb);

		JScrollPane sp = new JScrollPane();
		sp.setSize(800, 600);
		setLayout(new BorderLayout());
		add(sp, BorderLayout.CENTER);

		QxtIntegerProperty idProperty = new QxtIntegerProperty("id", "ID");
		idProperty.setReadOnly(true);
		QxtStringProperty nameProperty = new QxtStringProperty("name",
				"Name");
		QxtIntegerProperty ageProperty = new QxtIntegerProperty("age", "Age");
		QxtCheckBoxBooleanProperty intelProperty = new QxtCheckBoxBooleanProperty(
				"intelligent", "Intelligent");
		QxtComboBoxProperty<String> sexesProperty = new QxtComboBoxProperty<>(
				"sex", "Sex", String.class, sexes);
		QxtDateProperty laProperty = new QxtDateProperty("lastAccess",
				"Last Access", DateFormat.getDateInstance());
		QxtComputedProperty<Integer> c1 = new QxtCachedComputedProperty<Integer>(
				"age_plus_5", "Age + 5", Integer.class) {
			@Override
			public Integer computeValue(Object bean) {
				System.out.println("*** Computing property ***");
				return ((SampleQxtBean) bean).getAge() + 5;
			}
		};

		qxt = new QxtTable<>(beans, SampleQxtBean.class,
				idProperty, nameProperty, ageProperty, intelProperty,
				sexesProperty, laProperty, c1);
		sp.setViewportView(qxt);
		qxt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		qxt.addLineEditorListener(new LineEditorListener<SampleQxtBean>() {
			@Override
			public void lineEditingCanceled(SampleQxtBean t, int line) {
				System.out.println("Canceled editing line " + line);
			}

			@Override
			public void lineEditingCommitted(SampleQxtBean t, int line) {
				System.out.println("Committed editing line " + line + " ("
						+ "new line? " + newLine + ")");
			}

			@Override
			public void lineEditingStarted(SampleQxtBean t, int line) {
				System.out.println("Starting editing line " + line);
			}

			@Override
			public void lineEditingCommitFailed(SampleQxtBean t, int line) {
				System.out.println("Commit of line " + line + " failed");
			}

			@Override
			public boolean tryLineEditingCommitted(SampleQxtBean t, int line) {
				System.out.println("Try commit line " + line);
				if (t.getName() == null || t.getName().length() < 2) {
					return false;
				} else {
					return true;
				}
			}

		});

		qxt.setLineFactory(new LineFactory<SampleQxtBean>() {
			@Override
			public void destroyLine(SampleQxtBean line) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public SampleQxtBean makeLine() {
				return new SampleQxtBean();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		qxt.setColumnControlVisible(true);
		qxt.setHighlighters(HighlighterFactory.createAlternateStriping());
		qxt.setConfigurationCode("example");
		qxt.loadConfiguration();

		pack();
		setVisible(true);
	}

	@SuppressWarnings("unused")
	public static void main(String args[]) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		new SampleQxt();
	}
}
