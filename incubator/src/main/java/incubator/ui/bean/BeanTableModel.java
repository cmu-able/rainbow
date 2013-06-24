package incubator.ui.bean;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;

/**
 * Table model that uses a set of beans as data.
 */
public class BeanTableModel extends AbstractTableModel {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Class of the model data.
	 */
	private Class<?> m_clazz;
	
	/**
	 * Bean properties that are used to obtains the table values.
	 */
	private ColumnData m_columns[];
	
	/**
	 * Property by which we order (<code>null</code> if we have a customized
	 * sorter).
	 */
	private String m_order_by;
	
	/**
	 * Comparator used to sort data.
	 */
	private Comparator<Object> m_comparator;
	
	/**
	 * Model objects.
	 */
	private List<Object> m_data;
	
	/**
	 * Indexes of the properties that define identity. 
	 */
	private int[] m_identity;
	
	/**
	 * Is sorting ascending?
	 */
	private boolean m_sort_ascending;
	
	/**
	 * Maps helper names in helpers.
	 */
	private Map<String, Object> m_helpers;
	
	/**
	 * Maps helper names in the classes that they define.
	 */
	private Map<String, Class<?>> m_helper_classes;
	
	
	/**
	 * Creates a new model.
	 * @param resource the resource name where model configuration should be
	 * read from
	 * @param loader the class loader which is responsible for loading
	 * resources and searching for classes
	 * @throws Exception loading failed 
	 */
	public BeanTableModel(String resource, ClassLoader loader)
			throws Exception {
		if (resource == null) {
			throw new IllegalArgumentException("resource == null");
		}
		
		if (loader == null) {
			throw new IllegalArgumentException("loader == null");
		}
		
		Properties p = new Properties();
		try (InputStream is = getClass().getResourceAsStream(resource)) {
			if (is == null) {
				throw new IllegalArgumentException("Resource '" + resource
						+ "' not found.");
			}
			
			p.load(is);
		}
		
		String clazzName = p.getProperty("bean.class");
		if (clazzName == null) {
			throw new IllegalArgumentException("Configuration resource '"
					+ resource + "' does not define property 'bean.class'");
		}
		
		m_clazz = loader.loadClass(clazzName);
		m_helpers = new HashMap<>();
		m_helper_classes = new HashMap<>();
		
		/*
		 * Search for helpers.
		 */
		Pattern helperType = Pattern.compile("helper.(\\w+)\\.class");
		for (Iterator<Object> it = p.keySet().iterator(); it.hasNext(); ) {
			String pk = (String) it.next();
			Matcher m = helperType.matcher(pk);
			if (m.matches()) {
				String hname = m.group(1);
				String hclass = p.getProperty(pk);
				m_helper_classes.put(hname, Class.forName(hclass));
			}
		}
		
		List<ColumnData> cols = new ArrayList<>();
		for (int i = 0; p.getProperty("column." + i + ".name") != null; i++) {
			String name = p.getProperty("column." + i + ".name");
			String value = p.getProperty("column." + i + ".value");
			if (value == null) {
				value = name;
			}
			
			String title = p.getProperty("column." + i + ".title");
			String provider = p.getProperty("column." + i + ".provider");
			String visibleStr = p.getProperty("column." + i + ".visible");
			boolean visible = true;
			if (visibleStr != null && !visibleStr.equals("true")) {
				visible = false;
			}
			
			ColumnData col = new ColumnData(name, value, title, m_clazz,
					provider, visible);
			cols.add(col);
			
			/*
			 * Search for hints for this column.
			 */
			for (Iterator<Object> it = p.keySet().iterator(); it.hasNext(); ) {
				String pkey = (String) it.next();
				String pfx = "column." + i + ".hint.";
				if (pkey.startsWith(pfx) && pkey.length() > pfx.length()) {
					String hkey = pkey.substring(pfx.length());
					String hval = p.getProperty(pkey);
					col.add_hint(hkey, hval);
				}
			}
		}
		
		m_columns = cols.toArray(new ColumnData[0]);
		if (m_columns.length == 0) {
			throw new IllegalArgumentException("Configuration does not define "
					+ "any columns.");
		}
		
		String sort = p.getProperty("default.sort");
		if (sort == null) {
			sort = m_columns[0].m_name;
		}
		
		m_order_by = sort;
		
		List<Integer> idents = new ArrayList<>();
		for (int i = 0; p.getProperty("identity." + i) != null; i++) {
			String name = p.getProperty("identity." + i);
			int found = -1;
			for (int j = 0; j < m_columns.length; j++) {
				if (m_columns[j].m_name.equals(name)) {
					found = j;
					break;
				}
			}
			
			if (found == -1) {
				throw new IllegalArgumentException("Column '" + name + "' not "
						+ "found but specified as identity.");
			}
			
			idents.add(new Integer(found));
		}
		
		m_identity = new int[idents.size()];
		for (int i = 0; i < idents.size(); i++) {
			m_identity[i] = idents.get(i);
		}
		
		m_sort_ascending = true;
		create_comparator();
		
		m_data = new ArrayList<>();
	}
	
	/**
	 * Defines a helper of the model. A helper is an object that can be used
	 * to obtain column values.
	 * @param name the helper name; if a helper already exists with this name,
	 * it will be replaced
	 * @param helper the helper
	 */
	public void setHelper(String name, Object helper) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
		if (helper == null) {
			throw new IllegalArgumentException("helper == null");
		}
		
		m_helpers.put(name, helper);
	}
	
	/**
	 * Creates the comparator to sort by the field defined as sorting field.
	 * @throws IllegalArgumentException if the field is not sortable (only
	 * objects that are comparable can be sorted)
	 */
	private void create_comparator() {
		for (int i = 0; i < m_columns.length; i++) {
			final int compareProperty = i;
			if (m_columns[i].m_name.equals(m_order_by)) {
				if (Comparable.class.isAssignableFrom(m_columns[i].getType())) {
					m_comparator = new Comparator<Object>() {
						int pidx = compareProperty;
						@Override
						public int compare(Object arg0, Object arg1) {
							Object v0, v1;
							v0 = m_columns[pidx].value(arg0);
							v1 = m_columns[pidx].value(arg1);
							
							if (v0 == null && v1 == null) {
								return 0;
							}
							
							if (v0 == null) {
								return -1 * (m_sort_ascending? 1 : -1);
							}
							
							if (v1 == null) {
								return 1 * (m_sort_ascending? 1 : -1);
							}
							
							@SuppressWarnings("unchecked")
							Comparable<Object> c0 = (Comparable<Object>) v0;
							
							int cmp = c0.compareTo(v1);
							return m_sort_ascending? cmp : -cmp;
						}
					};
					
					return;
				} else {
					throw new IllegalArgumentException("Class '"
							+ m_columns[i].getType() + "' is not comparable.");
				}
			}
		}
		
		throw new IllegalArgumentException("Unknown property '" + m_order_by
				+ "'.");
	}
	
	@Override
	public int getColumnCount() {
		int cnt = 0;
		for (int i = 0; i < m_columns.length; i++) {
			if (m_columns[i].m_visible) {
				cnt++;
			}
		}
		
		return cnt;
	}
	
	@Override
	public String getColumnName(int col) {
		col = get_real_column_index(col);
		
		return m_columns[col].m_title;
	}
	
	@Override
	public int getRowCount() {
		return m_data.size();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		col = get_real_column_index(col);
		
		Object bean = bean(row);
		
		try {
			/*
			 * We will always return a renderer information.
			 */
			BeanRendererInfo rendererInfo = new BeanRendererInfo(bean,
					m_columns[col].m_name, m_columns[col].value(bean));
			rendererInfo.provider(m_columns[col].m_provider);
			for (Iterator<?> it = m_columns[col].m_hints.entrySet().iterator();
					it.hasNext(); ) {
				@SuppressWarnings("unchecked")
				Entry<String, String> e = (Entry<String, String>) it.next();
				rendererInfo.add_hint(e.getKey(), e.getValue()); 
			}
			
			return rendererInfo;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Obtains the object that is in the given row.
	 * @param row the row
	 * @return the object
	 */
	public Object bean(int row) {
		if (row < 0 || row >= m_data.size()) {
			throw new IllegalArgumentException("Row out of range.");
		}
		
		return m_data.get(row);
	}
	
	/**
	 * Searches the line in which an object is.
	 * @param obj the object to look for
	 * @return the line or <code>-1</code> if the object was not found
	 */
	public int find_object_row(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		for (int i = 0; i < m_data.size(); i++) {
			if (is_same(obj, m_data.get(i))) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Synchronizes the model data with the collection given. All objects
	 * that are not in the collection are removed and all objects that are
	 * not in the model are added. Other object are possibly updated.
	 * @param c the collection
	 */
	public void synchronize(Collection<Object> c) {
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}
		
		/*
		 * Start by looking for all objects we need to remove.
		 */
		for (int i = 0; i < m_data.size(); i++) {
			boolean found = false;
			for (Iterator<Object> it = c.iterator(); it.hasNext(); ) {
				if (is_same(m_data.get(i), it.next())) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				remove(i);
				i--;
			}
		}
		
		/*
		 * We update the objects that have changed and add the new ones.
		 */
		for (Iterator<Object> it = c.iterator(); it.hasNext(); ) {
			Object obj = it.next();
			if (!(m_clazz.isInstance(obj))) {
				throw new IllegalArgumentException("Object is not of model "
						+ "bean type.");
			}
			
			int row = find_object_row(obj);
			if (row == -1) {
				add(obj);
			} else {
				iupdate(row, obj);
			}
		}
	}
	
	/**
	 * Removes a line from the model.
	 * @param row the line to remove
	 */
	public void remove(int row) {
		if (row < 0 || row >= m_data.size()) {
			throw new IllegalArgumentException("Row out of range.");
		}
		
		m_data.remove(row);
		fireTableRowsDeleted(row, row);
	}
	
	/**
	 * Adds a new object to the model.
	 * @param obj the object to add
	 */
	public void add(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		int pos = iadd(obj);
		fireTableRowsInserted(pos, pos);
	}
	
	/**
	 * Adds an object without firing information to the listeners.
	 * @param obj the object to add
	 * @return the position where the object was inserted
	 */
	private int iadd(Object obj) {
		int pos;
		for (pos = 0; pos < m_data.size(); pos++) {
			int r = m_comparator.compare(obj, m_data.get(pos));
			if (r < 0) {
				break;
			}
		}
		
		m_data.add(pos, obj);
		return pos;
	}
	
	/**
	 * Updates, if necessary, the data of an object.
	 * @param obj the object which must belong to the model
	 */
	public void update(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		int row = find_object_row(obj);
		if (row == -1) {
			throw new IllegalArgumentException("Object does not belong to "
					+ "model.");
		}
		
		iupdate(row, obj);
	}
	
	/**
	 * Checks if it is necessary to update an object.
	 * @param row the object's row
	 * @param obj the new representation of the object
	 */
	private void iupdate(int row, Object obj) {
		for (int i = 0; i < m_columns.length; i++) {
			Object v0 = m_columns[i].value(m_data.get(row));
			Object v1 = m_columns[i].value(obj);
			if (!ObjectUtils.equals(v0, v1)) {
				m_data.set(row, obj);
				fireTableRowsUpdated(row, row);
				return;
			}
		}
	}
	
	/**
	 * Checks if two objects represent the same identity (defined by the
	 * identity properties).
	 * @param obj1 the first object
	 * @param obj2 the second object
	 * @return do both objects represent the same identity?
	 */
	private boolean is_same(Object obj1, Object obj2) {
		for (int i = 0; i < m_identity.length; i++) {
			try {
				Object v1 = m_columns[m_identity[i]].value(obj1);
				Object v2 = m_columns[m_identity[i]].value(obj2);
				if (!ObjectUtils.equals(v1, v2)) {
					return false;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return true;
	}
	
	/**
	 * Sorts by the column with the given index
	 * @param column the column
	 */
	public void sort(int column) {
		column = get_real_column_index(column);
		if (m_columns[column].m_name.equals(m_order_by)) {
			return;
		}
		
		m_order_by = m_columns[column].m_name;
		create_comparator();
		resort();
	}
	
	/**
	 * Sorts by a column and, in case it is already sorted by that column,
	 * inverts the sorting order (ascending vs descending).
	 * @param column the column
	 */
	public void invert_sort(int column) {
		column = get_real_column_index(column);
		if (m_columns[column].m_name.equals(m_order_by)) {
			m_sort_ascending = !m_sort_ascending;
			resort();
			return;
		}
		
		sort(column);
	}
	
	/**
	 * Resorts all lines of the model.
	 */
	private void resort() {
		List<Object> save = m_data;
		m_data = new ArrayList<>();
		for (Iterator<Object> it = save.iterator(); it.hasNext(); ) {
			iadd(it.next());
		}
		
		fireTableDataChanged();
	}
	
	/**
	 * Obtains the index of the column that corresponds to the given
	 * visible column.
	 * @param vis_column the column index in the list of visible columns
	 * @return the column's real index
	 */
	private int get_real_column_index(int vis_column) {
		for (int i = 0; i < m_columns.length; i++) {
			if (m_columns[i].m_visible) {
				vis_column--;
			}
			
			if (vis_column < 0) {
				return i;
			}
		}
		
		throw new IllegalArgumentException("No such visible column: "
				+ vis_column);
	}
	
	/**
	 * Data of a model column.
	 */
	private class ColumnData {
		/**
		 * Column name.
		 */
		private String m_name;
		
		/**
		 * Column title.
		 */
		private String m_title;
		
		/**
		 * Syntax tree that allows access to the property value.
		 */
		private SyntaxNode m_syntax;
		
		/**
		 * Name of the provider to use to do rendering.
		 */
		private String m_provider;
		
		/**
		 * Is the column visible?
		 */
		private boolean m_visible;
		
		/**
		 * Hints to this column renderer.
		 */
		private Map<String, String> m_hints;
		
		/**
		 * Creates a column representation.
		 * @param name column name
		 * @param value column value
		 * @param title column title
		 * @param clazz bean class
		 * @param provider name of the provider used for rendering
		 * @param visible is the column visible?
		 */
		private ColumnData(String name, String value, String title,
				Class<?> clazz, String provider, boolean visible) {
			this.m_name = name;
			this.m_title = title;
			this.m_provider = provider;
			this.m_hints = new HashMap<>();
			this.m_visible = visible;
			
			try {
				m_syntax = evaluate_expression(value, clazz);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		/**
		 * Obtains the syntax tree that represents the given text.
		 * @param text the text to parse
		 * @param bean_class the bean class
		 * @return the syntax tree root
		 * @throws Exception failed to evaluate
		 */
		private SyntaxNode evaluate_expression(String text, Class<?> bean_class)
				throws Exception {
			SyntaxNode root = null;
			
			/*
			 * If the text starts with ${...} then we start with a helper and
			 * not with a bean.
			 */
			if (text.startsWith("${")) {
				String matched = match(text.substring(1));
				if (matched.length() == 0) {
					throw new Exception("${} must contain a helper name "
							+ "enclosed.");
				}
				
				root = new HelperNode(matched);
				
				text = text.substring(3 + matched.length());
				/*
				 * There is nothing following the helper.
				 */
				if (text.length() == 0) {
					return root;
				}
				
				if (!text.startsWith(".")) {
					throw new Exception("Helper definition must start with .");
				}
				
				text = text.substring(1);
			} else if (text.startsWith("$$")) {
				/*
				 * $$ is the bean.
				 */
				
				root = new BeanNode(bean_class);
				text = text.substring(2);
				if (text.length() == 0) {
					return root;
				}
				
				/*
				 * If we have something following, it must start with ".".
				 */
				if (!text.startsWith(".")) {
					throw new Exception("Helper definition must start with .");
				}
				
				text = text.substring(1);
			} else {
				root = new BeanNode(bean_class);
			}
			
			int cpos = 0;
			while (cpos < text.length()) {
				int endidx = compute_expr_length(text, cpos);
				String parse = text.substring(cpos, endidx);
				cpos = endidx + 1;

				/*
				 * Method or property?
				 */
				boolean is_method = false;
				is_method = parse.endsWith(")");

				if (is_method) {
					int mname_end = parse.indexOf('(');
					String mname = parse.substring(0, mname_end);
					String params = match(parse.substring(mname_end));
					List<SyntaxNode> mpar = new ArrayList<>(); 
					if (params.length() > 0) {
						/*
						 * Ok, we've got parameters for the method. Lets
						 * try to figure out how many are and what is the
						 * expression of each one of them.
						 */
						int pidxend = compute_expr_length(params, 0);
						mpar.add(evaluate_expression(params.substring(
								0, pidxend), bean_class));
						/*
						 * We we haven't finished by the end, we need to jump
						 * the separator.
						 */
						if (pidxend < params.length()) {
							pidxend++;
						}
						params = params.substring(pidxend);
					}					
					root = new DirectMethodInvocation(root, mname, mpar);
				} else {
					root = new PropertyInvocation(root, parse);
				}
			}
			
			return root;
		}
		
		/**
		 * Checks parenthesis match and returns the contents of the
		 * parenthesis match that is in the first position.
		 * @param text the text
		 * @return the text within parenthesis
		 * @throws Exception match failed
		 */
		private String match(String text) throws Exception {
			char c0 = text.charAt(0);
			char c1;
			if (c0 == '{') {
				c1 = '}';
			} else if (c0 == '(') {
				c1 = ')';
			} else {
				throw new Exception("Invalid match character: '" + c0 + "'");
			}
			
			for (int i = 1; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == c1) {
					return text.substring(1, i);
				} else if (c == '{' || c == '(') {
					i += match(text.substring(i)).length() + 1;
				}
			}
			
			throw new Exception("Unmatched: '" + c0 + "'.");
		}
		
		/**
		 * Finds out where an expression that starts in a given position
		 * ends.
		 * @param text the text to parse
		 * @param pos the position where analysis should start
		 * @return the first position after the end of the expression
		 * @throws Exception parse failed
		 */
		private int compute_expr_length(String text, int pos)
				throws Exception {
			int dotidx = text.indexOf('.', pos);
			if (dotidx == -1) {
				dotidx = text.length();
			}
			
			int opidx = text.indexOf('(', pos);
			if (opidx == -1) {
				opidx = text.length();
			}
			
			int comidx = text.indexOf(',', pos);
			if (comidx == -1) {
				comidx = text.length();
			}
			
			/*
			 * Search the least of all.
			 */
			int idx = dotidx;
			if (idx > comidx) {
				idx = comidx;
			}
			
			if (idx > opidx) {
				/*
				 * If what we have is open parenthesis, the expression will
				 * end in the close parenthesis.
				 */
				idx = opidx + match(text.substring(opidx)).length() + 2;
			}
			
			return idx;
		}
		
		/**
		 * Obtains the value of this column for the given object.
		 * @param obj the object
		 * @return the column value
		 */
		private Object value(Object obj) {
			Map<String, Object> context = new HashMap<>();
			context.put(SyntaxNode.BEAN, obj);
			context.put(SyntaxNode.HELPERS, m_helpers);
			try {
				return m_syntax.eval(context);
			} catch (Exception e) {
				return null;
			}
		}
		
		/**
		 * Adds a hint to draw the value.
		 * @param key the hint key
		 * @param value the hint value
		 */
		private void add_hint(String key, String value) {
			m_hints.put(key, value);
		}
		
		/**
		 * Obtains the column data type.
		 * @return the column data type
		 */
		private Class<?> getType() {
			return m_syntax.get_return_type();
		}
	}
	
	/**
	 * Superclass for all objects of the syntax tree in the column definition.
	 */
	private interface SyntaxNode {
		/**
		 * Bean key in the execution context.
		 */
		public static final String BEAN = "bean";
		
		/**
		 * Helpers key in the execution context.
		 */
		public static final String HELPERS = "helpers";
		
		/**
		 * Obtains the result of the execution.
		 * @param context the execution context
		 * @return the result
		 * @throws Exception failed to evaluate
		 */
		public Object eval(Map<String, Object> context) throws Exception;
		
		/**
		 * Obtains the type returned by the element
		 * @return the type returned
		 */
		public Class<?> get_return_type();
	}
	
	/**
	 * Node in the syntax tree that represents chained elements. A chained
	 * element is a node whose execution depends on the result of execution
	 * of another node.
	 */
	private abstract class ChainedNode implements SyntaxNode{
		/**
		 * Inner element which is executed before this one and whose result
		 * is used to evaluate this one.
		 */
		private SyntaxNode m_inner;
		
		/**
		 * Creates a new node.
		 * @param inner the inner node
		 */
		private ChainedNode(SyntaxNode inner) {
			this.m_inner = inner;
		}
		
		@Override
		public Object eval(Map<String, Object> context) throws Exception {
			return execute(m_inner.eval(context), context);
		}
		
		/**
		 * Executes this node (obtains its value).
		 * @param value the value used for execution returned by the inner
		 * node
		 * @param context execution context
		 * @return execution value
		 * @throws Exception execution failed
		 */
		public abstract Object execute(Object value,
				Map<String, Object> context) throws Exception;
		
		@Override
		public abstract Class<?> get_return_type();
	}
	
	/**
	 * Syntax tree element that represents a method invocation.
	 */
	private class MethodInvocation extends ChainedNode {
		/**
		 * Method to invoke.
		 */
		private Method m_method;
		
		/**
		 * Returned data type.
		 */
		private Class<?> m_return_type;
		
		/**
		 * List of expressions that allow evaluating the method arguments.
		 */
		private List<SyntaxNode> m_arguments;
		
		/**
		 * Creates a new node.
		 * @param name name of the method to invoke
		 * @param inner inner element to be executed before this one
		 * @param arguments list of expressions that should be used to evaluate
		 * the method arguments
		 * @throws Exception failed to analyze the class
		 */
		private MethodInvocation(SyntaxNode inner, String name,
				List<SyntaxNode> arguments)
				throws Exception {
			this(inner, new String[] { name }, arguments);
		}
		
		/**
		 * Creates a new node.
		 * @param inner inner element to be executed before this one
		 * @param names possible names for the methods. All names are tried
		 * by the order they are defined.
		 * @param arguments list of expressions that should be used to evaluate
		 * the method arguments
		 * @throws Exception failed to analyze the class
		 */
		private MethodInvocation(SyntaxNode inner, String[] names,
				List<SyntaxNode> arguments) throws Exception {
			super(inner);
			
			m_method = null;
			this.m_arguments = arguments;
			Class<?> rtype = inner.get_return_type();
			
			Class<?> argTypes[] = new Class[arguments.size()];
			for (int i = 0; i < arguments.size(); i++) {
				argTypes[i] = arguments.get(i).get_return_type();
			}
			
			for (int i = 0; i < names.length; i++) {
				try {
					m_method = rtype.getDeclaredMethod(names[i], argTypes);
					break;
				} catch (NoSuchMethodException e) {
					/*
					 * We'll try the next one.
					 */
				}
			}
			
			if (m_method == null) {
				StringBuffer names_string = new StringBuffer();
				names_string.append("{");
				for (int i = 0; i < names.length; i++) {
					if (i > 0) {
						names_string.append(",");
					}
					names_string.append("'");
					names_string.append(names[i]);
					names_string.append("(");
					for (int j = 0; j < argTypes.length; j++) {
						if (j > 0) {
							names_string.append(',');
						}
						
						names_string.append(argTypes[j].getName());
					}
					names_string.append(")'");
				}
				
				names_string.append("}");
				
				throw new Exception("Method not found: " + names_string
						+ " in class '" + rtype.getName() + "'.");
			}
			
			m_method.setAccessible(true);
			m_return_type = m_method.getReturnType();
			m_return_type = ClassUtils.primitiveToWrapper(m_return_type);
		}
		
		@Override
		public Object execute(Object value, Map<String, Object> context)
				throws Exception {
			/*
			 * Evaluate arguments.
			 */
			Object args[] = new Object[m_arguments.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = m_arguments.get(i).eval(context);
			}
			
			if (value == null) {
				return null;
			}
			
			return m_method.invoke(value, args);
		}
		
		@Override
		public Class<?> get_return_type() {
			return m_return_type;
		}
	}
	
	/**
	 * Syntax tree element when defining a column that represents obtaining
	 * a property value.
	 */
	private class PropertyInvocation extends MethodInvocation {
		/**
		 * Creates a new element.
		 * @param inner that element that should be invoked before this one
		 * @param property the property
		 * @throws Exception analysis failed
		 */
		private PropertyInvocation(SyntaxNode inner, String property)
				throws Exception {
			super(inner, new String[]{ "get"
					+ (Character.toUpperCase(property.charAt(0)))
					+ property.substring(1),
					"is" + (Character.toUpperCase(property.charAt(0)))
					+ property.substring(1) }, new ArrayList<SyntaxNode>());
		}
	}
	
	/**
	 * Syntax tree element that represents a method execution.
	 */
	private class DirectMethodInvocation extends MethodInvocation {
		/**
		 * Creates a new node.
		 * @param inner inner element to be executed before this one
		 * @param name name of the method to invoke
		 * @param arguments list of expressions that should be used to evaluate
		 * the method arguments
		 * @throws Exception failed to analyze the class
		 */
		private DirectMethodInvocation(SyntaxNode inner, String name,
				List<SyntaxNode> arguments) throws Exception {
			super(inner, name, arguments);
		}
	}
	
	/**
	 * Tree node whose execution results in obtaining the value of a bean.
	 */
	private class BeanNode implements SyntaxNode {
		/**
		 * Bean class.
		 */
		private Class<?> bean_class;
		
		/**
		 * Creates the tree node.
		 * @param bean_class bean class
		 */
		private BeanNode(Class<?> bean_class) {
			this.bean_class = bean_class;
		}

		@Override
		public Object eval(Map<String, Object> context) throws Exception {
			return context.get(BEAN);
		}

		@Override
		public Class<?> get_return_type() {
			return bean_class;
		}
	}
	
	/**
	 * Node in the syntax tree whose execution results in obtaining a helper.
	 */
	private class HelperNode implements SyntaxNode {
		/**
		 * Helper name.
		 */
		private String m_helper_name;
		
		/**
		 * Helper class.
		 */
		private Class<?> m_helper_class;
		
		
		/**
		 * Creates a new node. The helper doesn't have to be defined when
		 * the node is created although it has to be when it executes. However,
		 * the class definition of the helper must exist.
		 * @param helper the helper name
		 * @throws Exception helper was not found
		 */
		private HelperNode(String helper) throws Exception {
			m_helper_name = helper;
			m_helper_class = m_helper_classes.get(helper);
			if (m_helper_class == null) {
				throw new Exception("Helper '" + helper + "' not found.");
			}
		}

		@Override
		public Object eval(Map<String, Object> context) throws Exception {
			@SuppressWarnings("unchecked")
			Map<String, Object> helpers =
					(Map<String, Object>) context.get(HELPERS);
			Object obj = helpers.get(m_helper_name);
			if (obj == null) {
				throw new Exception("Helper '" + m_helper_name
						+ "' not defined.");
			}
			
			return obj;
		}

		@Override
		public Class<?> get_return_type() {
			return m_helper_class;
		}
	}
}
