package edu.cmu.cs.able.eseb.filter.participant;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.parser.DefaultTypelibParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.Int64Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.struct.StructureDataValue;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Class providing easy access to participant data types. The values of the
 * constants in this class must be synchronized with the TDL descriptions.
 */
public class ParticipantTypes {
	/**
	 * Namespace of eseb data types.
	 */
	public static final String ESEB_NAMESPACE = "eseb";
	
	/**
	 * Namespace of participant data types
	 */
	public static final String PARTICIPANT_NAMESPACE = "participant";
	
	/**
	 * Name of the structure used to announce the ID of a participant.
	 */
	public static final String ANNOUNCE_ID_STRUCTURE_NAME = "announce_id";
	
	/**
	 * Name of the ID field of the announce ID structure.
	 */
	public static final String ANNOUNCE_ID_FIELD_NAME = "id";
	
	/**
	 * Name of the field of the meta data structure.
	 */
	public static final String META_DATA_FIELD_NAME = "meta_data";
	
	/**
	 * Name of resource with TDL of that defines the participant types.
	 */
	public static final String TDL_RESOURCE = "participant_type.tdl";
	
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Structure with the announce ID.
	 */
	private StructureDataType m_announce_id_structure;
	
	/**
	 * Field with the announcer ID.
	 */
	private Field m_announce_id_field;
	
	/**
	 * Field with the announcer meta data.
	 */
	private Field m_announce_meta_data_field;
	
	/**
	 * String to string map data type.
	 */
	private MapDataType m_string_to_string;
	
	/**
	 * Text encoding used to encode data type.
	 */
	private TextEncoding m_txt_encoding;
	
	/**
	 * Creates a new accessor class.
	 * @param pscope the primitive data type scope
	 * @param encoding an encoding that can convert between data tyeps and
	 * text
	 * @throws ParticipantException failed to instantiate the participant
	 * data types
	 */
	public ParticipantTypes(PrimitiveScope pscope, TextEncoding encoding)
			throws ParticipantException {
		Ensure.not_null(pscope);
		Ensure.not_null(encoding);
		
		m_pscope = pscope;
		m_txt_encoding = encoding;
		if (!read_types(pscope)) {
			load_types(pscope);
			boolean read = read_types(pscope);
			Ensure.is_true(read);
		}
	}
	
	/**
	 * Reads the data types from the scope filling in the object's instance
	 * variables.
	 * @param pscope the primitive scope
	 * @return have the data types been read?
	 * @throws ParticipantException some data already exists in the scope but
	 * is invalid
	 */
	private boolean read_types(PrimitiveScope pscope)
			throws ParticipantException {
		HierarchicalName id_structure_name = new HierarchicalName(true,
				ESEB_NAMESPACE, PARTICIPANT_NAMESPACE,
				ANNOUNCE_ID_STRUCTURE_NAME);
		m_string_to_string = MapDataType.map_of(m_pscope.string(),
				m_pscope.string(), m_pscope);
		
		try {
			DataType type = pscope.find(id_structure_name);
			if (type == null) {
				return false;
			}
			
			Ensure.is_instance(type, StructureDataType.class);
			m_announce_id_structure = (StructureDataType) type;
			
			Set<Field> fields = m_announce_id_structure.fields();
			Ensure.equals(2, fields.size());
			
			m_announce_id_field = m_announce_id_structure.field(
					ANNOUNCE_ID_FIELD_NAME);
			Ensure.not_null(m_announce_id_field);
			Ensure.equals(pscope.int64(),
					m_announce_id_field.description().type());
			m_announce_meta_data_field = m_announce_id_structure.field(
					META_DATA_FIELD_NAME);
			Ensure.not_null(m_announce_meta_data_field);
			Ensure.equals(m_string_to_string,
					m_announce_meta_data_field.description().type());
		} catch (AmbiguousNameException e) {
			throw new ParticipantException("Failed to find data structure '"
					+ id_structure_name + "' with fields '"
					+ ANNOUNCE_ID_FIELD_NAME + "' and '" + META_DATA_FIELD_NAME
					+ "'.", e);
		}
		
		return true;
	}
	
	/**
	 * Loads the data types into the scope reading them from the definition
	 * in the resources.
	 * @param pscope the primitive scope
	 * @throws ParticipantException failed to read the data types
	 */
	private void load_types(PrimitiveScope pscope)
			throws ParticipantException {
		Ensure.not_null(pscope);
		
		String types_text;
		try (InputStream is = getClass().getResourceAsStream(TDL_RESOURCE);
				InputStreamReader isr = new InputStreamReader(is);
				StringWriter sw = new StringWriter()) {
			
			int ch;
			while ((ch = isr.read()) != -1) {
				sw.write(ch);
			}
			
			types_text = sw.toString();
		} catch (IOException e) {
			throw new ParticipantException("Failed to load resource '"
					+ TDL_RESOURCE + "'.", e);
		}
		
		TypelibParsingContext context = new TypelibParsingContext(pscope,
				pscope);
		DefaultTypelibParser dtp = DefaultTypelibParser.make();
		
		try {
			dtp.parse(new ParsecFileReader().read_memory(types_text), context);
		} catch (LocalizedParseException e) {
			throw new ParticipantException("Failed to parse '" + TDL_RESOURCE
					+ "' resource.", e);
		}
	}
	
	/**
	 * Creates a new data value with an announce data type.
	 * @param id the participant ID
	 * @param meta_data the participant meta data
	 * @return the data value
	 */
	public DataValue announce(long id, Map<String, DataValue> meta_data) {
		Map<Field, DataValue> values = new HashMap<>();
		
		if (meta_data == null) {
			meta_data = new HashMap<>();
		}
		
		MapDataValue mdv = m_string_to_string.make();
		for (Map.Entry<String, DataValue> e : meta_data.entrySet()) {
			Ensure.not_null(e.getKey());
			Ensure.not_null(e.getValue());
			
			StringWriter sw = new StringWriter();
			
			try {
				m_txt_encoding.encode(e.getValue(), sw);
			} catch (IOException ex) {
				Ensure.never_thrown(ex);
			}
			
			mdv.put(m_pscope.string().make(e.getKey()),
					m_pscope.string().make(sw.toString()));
		}
		
		values.put(m_announce_id_field, m_pscope.int64().make(id));
		values.put(m_announce_meta_data_field, mdv);
		
		return m_announce_id_structure.make(values);
	}
	
	/**
	 * Checks if a data value is an announce structure.
	 * @param v the data value to check
	 * @return is it an announce structure?
	 */
	public boolean is_announce(DataValue v) {
		Ensure.not_null(v);
		return m_announce_id_structure.is_instance(v);
	}
	
	/**
	 * Obtains the ID contained in an announce structure.
	 * @param v the structure
	 * @return the ID
	 */
	public long announce_id(DataValue v) {
		Ensure.not_null(v);
		Ensure.is_true(m_announce_id_structure.is_instance(v));
		DataValue idv = ((StructureDataValue) v).value(m_announce_id_field);
		
		Ensure.not_null(idv);
		Ensure.is_true(m_pscope.int64().is_instance(idv));
		return ((Int64Value) idv).value();
	}
	
	/**
	 * Obtains all meta data keys in an announce structure.
	 * @param v the structure
	 * @return all keys in the structure
	 */
	public Set<String> announce_meta_data_keys(DataValue v) {
		Ensure.not_null(v);
		Ensure.is_true(m_announce_id_structure.is_instance(v));
		MapDataValue mdv = (MapDataValue) ((StructureDataValue) v).value(
				m_announce_meta_data_field);
		Map<DataValue, DataValue> all = mdv.all();
		Set<String> keys = new HashSet<>();
		for (DataValue k : all.keySet()) {
			keys.add(((StringValue) k).value());
		}
		
		return keys;
	}
	
	/**
	 * Obtains a meta data in an announce structure.
	 * @param v the structure
	 * @param k the meta data key
	 * @return the meta data value
	 * @throws InvalidEncodingException failed to decode the meta data
	 */
	public DataValue announce_meta_data(DataValue v, String k)
			throws InvalidEncodingException {
		Ensure.not_null(v);
		Ensure.not_null(k);
		Ensure.is_true(m_announce_id_structure.is_instance(v));
		MapDataValue mdv = (MapDataValue) ((StructureDataValue) v).value(
				m_announce_meta_data_field);
		StringValue v_enc = (StringValue) mdv.get(m_pscope.string().make(k));
		DataValue r = null;
		try {
			r = m_txt_encoding.decode(new StringReader(v_enc.value()),
					m_pscope);
		} catch (IOException e) {
			Ensure.never_thrown(e);
		}
		
		return r;
	}
}
