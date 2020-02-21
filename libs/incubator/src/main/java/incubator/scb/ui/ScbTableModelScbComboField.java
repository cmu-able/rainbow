package incubator.scb.ui;

import incubator.Pair;
import incubator.obscol.ObservableList;
import incubator.obscol.WrapperObservableList;
import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbContainer;
import incubator.scb.ScbContainerToListSynchronizer;
import incubator.scb.ScbField;
import incubator.scb.ValidationResult;
import incubator.ui.AutoUpdateJComboBox;
import incubator.ui.AutoUpdateJComboBox.NullSupport;

import java.util.ArrayList;
import java.util.Comparator;

import org.jdesktop.swingx.renderer.StringValue;

/**
 * Table model field that shows a combo box whose values are derived from an
 * SCB container.
 * @param <T> the type of SCB being edited
 * @param <V> the type of the SCB field
 */
public class ScbTableModelScbComboField<T, V extends Scb<V>>
		extends ScbTableModelField<T, V, ScbField<T, V>> {
	/**
	 * An observable list with the container data.
	 */
	private ObservableList<V> m_list_data;
	
	/**
	 * The combo box.
	 */
	private AutoUpdateJComboBox<V> m_combo;
	
	/**
	 * Creates a new field representing a combo box.
	 * @param cof the configuration object field
	 * @param values the container with values to show in the combo box
	 * @param comparator a comparator to sort the combo box values
	 * @param ns <code>null</code> support in the combo box
	 * @param sv a converter of the objects to string
	 */
	public ScbTableModelScbComboField(ScbField<T, V> cof,
			ScbContainer<V> values, Comparator<V> comparator, NullSupport ns,
			StringValue sv) {
		super(cof, true);
		
		Ensure.not_null(values, "values == null");
		Ensure.not_null(comparator, "comparator == null");
		Ensure.not_null(ns, "ns == null");
		Ensure.not_null(sv, "sv == null");
		
		m_list_data = new WrapperObservableList<>(new ArrayList<V>());
		@SuppressWarnings("unused")
		ScbContainerToListSynchronizer<V> synchronizer =
				new ScbContainerToListSynchronizer<>(values, m_list_data,
				comparator);
		
		m_combo = new AutoUpdateJComboBox<>(m_list_data, sv);
	}
	
	@Override
	public Object display_object(T obj) {
		m_combo.setSelected(cof().get(obj));
		return m_combo;
	}
	
	@Override
	public Pair<ValidationResult, V> from_display(T obj, Object display) {
		return new Pair<>(ValidationResult.make_valid(), m_combo.getSelected());
	}
}
