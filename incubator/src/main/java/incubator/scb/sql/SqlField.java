package incubator.scb.sql;

import java.sql.SQLException;

import incubator.pval.Ensure;
import incubator.scb.ScbField;

/**
 * Field representing an SCB field stored in a SQL database.
 * @param <T> the SCB type
 * @param <V> the type of the field's value
 * @param <F> the field to access in the SCB
 * @param <SV> the type of the SQL field value
 */
public abstract class SqlField<T, V, F extends ScbField<T, V>, SV> {
	/**
	 * SCB field.
	 */
	private F m_sf;
	
	/**
	 * The SV type.
	 */
	private Class<SV> m_sv_class;
	
	/**
	 * The field name in the SQL database.
	 */
	private String m_sql_name;
	
	/**
	 * Creates a new field.
	 * @param sql_name the field's name in the SQL database.
	 * @param sf the SCB field
	 * @param sv_class the type of the SQL field value
	 */
	public SqlField(String sql_name, F sf, Class<SV> sv_class) {
		Ensure.notNull(sql_name);
		Ensure.notNull(sf);
		Ensure.notNull(sv_class);
		
		m_sf = sf;
		m_sql_name = sql_name;
		m_sv_class = sv_class;
	}
	
	/**
	 * Obtains the SCB field.
	 * @return the object field
	 */
	public F sf() {
		return m_sf;
	}
	
	/**
	 * Obtains the field's SQL name.
	 * @return the SQL name
	 */
	public String sql_name() {
		return m_sql_name;
	}
	
	/**
	 * Obtains the SQL clause that creates this field.
	 * @return the SQL clause
	 */
	public abstract String creation_sql();
	
	/**
	 * Loads a value into the object.
	 * @param obj the object
	 * @param value the value obtained from the result set
	 * @throws SQLException the value in the database is invalid
	 */
	public void load(T obj, Object value) throws SQLException {
		Ensure.notNull(obj);
		if (value == null) {
			m_sf.set(obj, null);
		} else {
			if (!m_sv_class.isInstance(value)) {
				throw new SQLException("Value '" + value + "' from SQL "
						+ "field '" + sql_name() + "' has type "
						+ value.getClass().getName() + " but "
						+ m_sv_class.getName() + " expected.");
			}
			
			m_sf.set(obj, from_sql_type(m_sv_class.cast(value)));
		}
	}
	
	/**
	 * Converts a value from the SQL type into the object type.
	 * @param value the value obtained from the result set (which is not
	 * <code>null</code>)
	 * @return the value to store in the object
	 * @throws SQLException the value in the database is invalid
	 */
	public abstract V from_sql_type(SV value) throws SQLException;
	
	/**
	 * Obtains the value to store in a SQL database.
	 * @param obj the object
	 * @return the object to set into a SQL database using a prepared
	 * statement
	 */
	public Object store(T obj) {
		V value = m_sf.get(obj);
		if (value == null) {
			/*
			 * Not totally sure we can place null in a prepared statement.
			 * In theorey it depends on the driver.
			 */
			return null;
		} else {
			return to_sql_type(value);
		}
	}
	
	/**
	 * Converts a value from the object type into the SQL type.
	 * @param v the value obtained from the object (which is not
	 * <code>null</code>)
	 * @return the value to store in the result set
	 */
	public abstract SV to_sql_type(V v);
}
