package incubator.scb.sql;

import incubator.exh.LocalCollector;
import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbContainerListener;
import incubator.scb.ScbFactoryContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that performs a mapping between an SCB object container and a
 * database table. Every time an object is created, deleted or updated, the
 * database will be updated. When the connection to the database is established,
 * the container is cleared and loaded from the configuration. When the
 * connection to the database is cleared, the container is cleared.
 * @param <T> the SCB object type
 */
public class SqlContainerTableMapping<T extends Scb<T>> {
	/**
	 * The name of the SQL table.
	 */
	private String m_table_name;
	
	/**
	 * The database connection which may be <code>null</code> if we're not
	 * connected to a database.
	 */
	private Connection m_dbc;
	
	/**
	 * The container object.
	 */
	private ScbFactoryContainer<T> m_container;
	
	/**
	 * Database fields.
	 */
	private Set<SqlField<T, ?, ?, ?>> m_fields;
	
	/**
	 * IDs of objects in the database.
	 */
	private Map<T, Integer> m_ids;
	
	/**
	 * Exception collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * Creates a new mapping which is not currently connected to a database.
	 * @param table_name the table name
	 * @param container the container
	 */
	public SqlContainerTableMapping(String table_name,
			ScbFactoryContainer<T> container) {
		Ensure.notNull(table_name);
		Ensure.notNull(container);
		
		m_table_name = table_name;
		m_dbc = null;
		m_container = container;
		m_fields = new HashSet<>();
		m_ids = new HashMap<>();
		m_collector = new LocalCollector("SQL container '" + table_name
				+ "'.");
		
		m_container.add_listener(new ScbContainerListener<T>() {
			@Override
			public void scb_added(T t) {
				created(t);
			}

			@Override
			public void scb_removed(T t) {
				deleted(t);
			}

			@Override
			public void scb_updated(T t) {
				updated(t);
			}
		});
	}
	
	/**
	 * Adds a new field to the mapping.
	 * @param f the field
	 */
	protected synchronized void add_field(SqlField<T, ?, ?, ?> f) {
		Ensure.notNull(f);
		m_fields.add(f);
	}
	
	/**
	 * Obtains the table name.
	 * @return the name
	 */
	public String table_name() {
		return m_table_name;
	}
	
	/**
	 * Sets the database connection. The container will be cleared. If the
	 * connection is not <code>null</code> then the SCB objects will be
	 * loaded into the container
	 * @param c the connection, <code>null</code> means no connection
	 * @throws SQLException failed to load the data
	 */
	public synchronized void connection(Connection c) throws SQLException {
		if (m_dbc == c) {
			return;
		}
		
		if (m_dbc != null) {
			for(T t : m_container.all_scbs()) {
				m_container.remove_scb(t);
			}
		}
		
		if (c != null) {
			/*
			 * Load the database data. First check if we need to create the
			 * table.
			 */
			boolean found = false;
			try (ResultSet rs = c.getMetaData().getTables(null,
					null, null, null)) {
				while (rs.next()) {
					if (rs.getString("TABLE_NAME").equalsIgnoreCase(
							m_table_name)) {
						found = true;
					}
				}
			}
			
			if (!found) {
				create_table(c);
			}
			
			/*
			 * Load all the data.
			 */
			load_data(c);
		}
		
		m_dbc = c;
	}
	
	/**
	 * Creates the container table.
	 * @param c the database connection
	 * @throws SQLException failed to create the table
	 */
	private void create_table(Connection c) throws SQLException {
		String sql = "create table " + m_table_name + "(";
		sql += "id integer not null primary key";
		for (SqlField<T, ?, ?, ?> f : m_fields) {
			sql += ", ";
			sql += f.creation_sql();
		}
		
		sql += ")";
		
		try (Statement stmt = c.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}
	
	/**
	 * Loads the database data.
	 * @param c the database connection
	 * @throws SQLException failed to load the data
	 */
	private void load_data(Connection c) throws SQLException {
		try (Statement stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery("select * from "
				+ m_table_name)) {
			while (rs.next()) {
				int id = rs.getInt("id");
				T obj = m_container.new_scb();
				for (SqlField<T, ?, ?, ?> f : m_fields) {
					f.load(obj, rs.getObject(f.sql_name()));
				}
				
				m_ids.put(obj, id);
			}
		}
	}
	
	/**
	 * Invoked when an object has been created. If the object is already known
	 * (we already have an ID for it) then ignore it as it is a spurious
	 * creation. It may happen because of load.
	 * @param t the object
	 */
	private synchronized void created(T t) {
		Ensure.notNull(t);
		
		if (m_ids.containsKey(t) || m_dbc == null) {
			/*
			 * Object already in the database or no connection.
			 */
			return;
		}
		
		int new_id = 1;
		for (Integer i : m_ids.values()) {
			if (i >= new_id) {
				new_id = i + 1;
			}
		}
		
		m_ids.put(t, new_id);
		insert_sql(new_id, t);
	}
	
	/**
	 * Execute the SQL to insert an object in the database.
	 * @param id the object ID
	 * @param t the object
	 */
	private void insert_sql(int id, T t) {
		String sql = "insert into " + m_table_name + " ( id";
		String values = "?";
		List<Object> obj_values = new ArrayList<>();
		for (SqlField<T, ?, ?, ?> f : m_fields) {
			sql += ", " + f.sql_name();
			values += ", ?";
			obj_values.add(f.store(t));
		}

		sql += ") values (" + values + ")";
System.out.println("Inserting " + id + ": " + sql);
		try (PreparedStatement pstmt = m_dbc.prepareStatement(sql)) {
			 pstmt.setInt(1, id);
			 for (int i = 0; i < obj_values.size(); i++) {
				 pstmt.setObject(2 + i, obj_values.get(i));
			 }
			 
			 pstmt.executeUpdate();
		} catch (SQLException e) {
			m_collector.collect(e, "Insert SQL");
		}
	}

	/**
	 * Execute the SQL to delete an object from the database.
	 * @param id the object ID
	 */
	private void delete_sql(int id) {
		String sql = "delete from  " + m_table_name + " where id = " + id;
System.out.println("Deleting: " + sql);
		try (Statement stmt = m_dbc.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			m_collector.collect(e, "Delete SQL");
		}
	}

	/**
	 * Invoked when an object has been updated.
	 * @param t the object
	 */
	private synchronized void updated(T t) {
		Ensure.notNull(t);
		
		if (m_dbc == null || !m_ids.containsKey(t)) {
			/*
			 * We don't care about updated objects we don't know about or if
			 * we're not connected to the database.
			 */
			return;
		}
		
		delete_sql(m_ids.get(t));
		insert_sql(m_ids.get(t), t);
	}
	
	/**
	 * Invoked when an object has been deleted.
	 * @param t the object
	 */
	private synchronized void deleted(T t) {
		Ensure.notNull(t);
		if (m_dbc == null || !m_ids.containsKey(t)) {
			/*
			 * We don't care if we don't know the object or if we're not
			 * connected to the database.
			 */
			return;
		}
		
		delete_sql(m_ids.get(t));
		m_ids.remove(t);
	}
}
