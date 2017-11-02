package incubator.scb.sql;

import incubator.scb.ScbTextField;

import java.sql.SQLException;

/**
 * SQL database field that contains text.
 * @param <T> the SCB object type
 */
public class SqlTextField<T>
		extends SqlField<T, String, ScbTextField<T>, String> {
	/**
	 * Creates a new text field.
	 * @param sql_name the SQL database field name
	 * @param field the configuration object field
	 */
	public SqlTextField(String sql_name, ScbTextField<T> field) {
		super(sql_name, field, String.class);
	}

	@Override
	public String creation_sql() {
		return sql_name() + " varchar(1000)";
	}

	@Override
	public String from_sql_type(String value) throws SQLException {
		return value;
	}

	@Override
	public String to_sql_type(String v) {
		return v;
	}
}
