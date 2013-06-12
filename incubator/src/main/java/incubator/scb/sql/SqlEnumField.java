package incubator.scb.sql;

import java.sql.SQLException;

import incubator.pval.Ensure;
import incubator.scb.ScbEnumField;

/**
 * Database field that corresponds to an enum field in an SCB.
 * @param <T> the SCB type
 * @param <E> the enumeration field type
 */
public class SqlEnumField<T, E extends Enum<E>>
		extends SqlField<T, E, ScbEnumField<T, E>, String> {
	/**
	 * The enum class.
	 */
	private Class<E> m_ecls;
	
	/**
	 * Creates a new enumeration field.
	 * @param sql_name the SQL field name
	 * @param f the SCB field
	 * @param ecls the enum class
	 */
	public SqlEnumField(String sql_name, ScbEnumField<T, E> f, Class<E> ecls) {
		super(sql_name, f, String.class);
		Ensure.notNull(ecls);
		m_ecls = ecls;
	}

	@Override
	public String creation_sql() {
		return sql_name() + " varchar(100)";
	}

	@Override
	public E from_sql_type(String value) throws SQLException {
		E[] ee = m_ecls.getEnumConstants();
		for (E e : ee) {
			if (e.name().equals(value)) {
				return e;
			}
		}
		
		throw new SQLException("Invalid value '" + value + "' in SQL field '"
				+ sql_name() + ".");
	}

	@Override
	public String to_sql_type(E v) {
		return v.name();
	}
}
