package edu.cmu.cs.able.typelib.struct;

import edu.cmu.cs.able.typelib.scope.ScopedObject;

/**
 * A field represents a named part of a structure.
 */
public class Field extends ScopedObject {
	/**
	 * The field's description.
	 */
	private FieldDescription m_description;
	
	/**
	 * The structure to which this field belongs.
	 */
	private StructureDataType m_structure;
	
	/**
	 * Creates a new field.
	 * @param description the field's description
	 * @param structure the structure where this field belongs
	 */
	Field(FieldDescription description, StructureDataType structure) {
		super(description.name());
		m_description = description;
		m_structure = structure;
	}
	
	/**
	 * Obtains the field's description.
	 * @return the field's description
	 */
	public FieldDescription description() {
		return m_description;
	}
	
	/**
	 * Obtains the structure where this field belongs.
	 * @return the structure
	 */
	public StructureDataType structure() {
		return m_structure;
	}
}
