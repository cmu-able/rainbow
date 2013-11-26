package edu.cmu.cs.able.typelib.struct;

import incubator.pval.Ensure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.typelib.prim.AnyType;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.CyclicScopeLinkageException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A structure is a data type that may contain fields. Structures may be
 * defined hierarchically.
 */
public class StructureDataType extends DataType {
	/**
	 * Is the structure abstract?
	 */
	private boolean m_abstract;
	
	/**
	 * The structure's scope that contains the fields.
	 */
	private Scope<Field> m_scope;
	
	/**
	 * Creates a new structure.
	 * @param name the structure name
	 * @param abs is the structure abstract?
	 * @param fields the structure's fields
	 * @param any the super class for the structure
	 * @throws InvalidTypeDefinitionException the structure is incorrectly
	 * defined
	 */
	public StructureDataType(String name, boolean abs,
			Set<FieldDescription> fields, AnyType any)
			throws InvalidTypeDefinitionException {
		super(name, new HashSet<>(Arrays.asList((DataType) any)));
		init(name, abs, fields, new HashSet<StructureDataType>(), any);
	}
	
	/**
	 * Creates a new structure.
	 * @param name the structure name
	 * @param abs is the structure abstract?
	 * @param fields the structure's fields
	 * @param parents the structure's parents
	 * @throws InvalidTypeDefinitionException the structure is incorrectly
	 * defined
	 */
	public StructureDataType(String name, boolean abs,
			Set<FieldDescription> fields, Set<StructureDataType> parents)
			throws InvalidTypeDefinitionException {
		super(name, new HashSet<DataType>(parents));
		Ensure.greater(parents.size(), 0);
		init(name, abs, fields, parents, null);
	}
	
	/**
	 * Initializes a new structure.
	 * @param name the structure name
	 * @param abs is the structure abstract?
	 * @param fields the structure's fields
	 * @param parents the structure's parents
	 * @param any the super data type if there are no parents
	 * @throws InvalidTypeDefinitionException the structure is incorrectly
	 * defined
	 */
	private void init(String name, boolean abs, Set<FieldDescription> fields,
			Set<StructureDataType> parents, AnyType any)
			throws InvalidTypeDefinitionException {
		Ensure.not_null(fields, "fields == null");
		Ensure.not_null(parents, "parents == null");
		m_abstract = abs;
		m_scope = new Scope<>(null);
		
		Set<String> names = new HashSet<>();
		for (FieldDescription fd : fields) {
			Ensure.not_null(fd, "field is null");
			
			if (names.contains(fd.name())) {
				throw new InvalidTypeDefinitionException("Duplicate field '"
				+ name + "' in structure '" + name + "'.");
			}
			
			names.add(fd.name());
			Field f = new Field(fd, this);
			m_scope.add(f);
		}
		
		for (StructureDataType p : parents) {
			Ensure.not_null(p, "parent is null");
			try {
				m_scope.link(p.m_scope);
				
				/*
				 * Add the parent scope to our own so that finding using the
				 * parent's name prefix may work.
				 */
				Scope<Field> p_prefix_scope = new Scope<>(p.name());
				p_prefix_scope.link(p.m_scope);
				m_scope.add(p_prefix_scope);
			} catch (CyclicScopeLinkageException e) {
				/*
				 * This never happens.
				 */
				assert false;
			}
		}
	}
	
	/**
	 * Checks whether this structure is abstract.
	 * @return is the structure abstract?
	 */
	@Override
	public boolean is_abstract() {
		return m_abstract;
	}
	
	/**
	 * Obtains the set of all fields in the structure and parent structures.
	 * @return all fields
	 */
	public Set<Field> fields() {
		return m_scope.all_recursive();
	}
	
	/**
	 * Obtains a field.
	 * @param hn the field's name
	 * @return the field
	 * @throws AmbiguousNameException the given field name is ambiguous
	 * @throws UnknownFieldException the given field does not exist
	 */
	public Field field(HierarchicalName hn) throws AmbiguousNameException,
			UnknownFieldException {
		Ensure.not_null(hn);
		Field f = m_scope.find(hn);
		if (f == null) {
			throw new UnknownFieldException("There is no field '"
					+ hn.toString() + "' in structure '" + name() + "'.");
		}
		
		return f;
	}
	
	/**
	 * Obtains a field.
	 * @param name the field's name
	 * @return the field
	 * @throws AmbiguousNameException the given field name is ambiguous
	 * @throws UnknownFieldException the given field does not exist
	 */
	public Field field(String name) throws AmbiguousNameException,
			UnknownFieldException {
		Ensure.not_null(name);
		return field(new HierarchicalName(false, name));
	}
	
	/**
	 * Creates a new instance of a structure. This method simply invokes
	 * {@link StructureDataValue#StructureDataValue(StructureDataType, Map)}.
	 * Sub classes which implement types based on the structure data type
	 * will generally want to provide their own factory methods overriding this
	 * one.
	 * @param values the structure values
	 * @return the new structure instance
	 */
	public StructureDataValue make(Map<Field, DataValue> values) {
		return new StructureDataValue(this, values);
	}
}
