package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.StringType;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.struct.StructureDataValue;
import edu.cmu.cs.able.typelib.struct.UnknownFieldException;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Delegate text encoding that encodes structures.
 */
public class StructureDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Creates a new encoding.
	 */
	public StructureDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof StructureDataType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		Ensure.is_instance(v, StructureDataValue.class);
		
		StructureDataType type = (StructureDataType) v.type();
		Set<Field> fields = type.fields();
		
		/*
		 * Search for the int 32 and string data types which should be in the
		 * root of the data type namespace.
		 */
		Int32Type i32_type = null;
		StringType string_type = null;
		
		try {
			i32_type = (Int32Type) v.type().parent_dts().find(
				new HierarchicalName(true, Int32Type.NAME));
			string_type = (StringType) v.type().parent_dts().find(
					new HierarchicalName(true, StringType.NAME));
		} catch (AmbiguousNameException e) {
			Ensure.never_thrown(e);
		}
		
		Int32Value count = i32_type.make(fields.size());
		enc.encode(count, w);
		
		for (Field f : fields) {
			enc.encode(string_type.make(f.name()), w);
			enc.encode(((StructureDataValue) v).value(f), w);
		}
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.not_null(enc);
		Ensure.is_instance(type, StructureDataType.class);
		
		Set<Field> fields = ((StructureDataType) type).fields();
		
		Int32Value v = (Int32Value) enc.decode(r, dts);
		if (v.value() != fields.size()) {
			throw new InvalidEncodingException("Decoding of structure of "
					+ "type '" + type.name() + "' failed: " + fields.size()
					+ " expected fields but " + v.value() + " found.");
		}
		
		Map<Field, DataValue> mv = new HashMap<>();
		for (int i = 0; i < v.value(); i++) {
			StringValue fname = (StringValue) enc.decode(r, dts);
			try {
				Field f = ((StructureDataType) type).field(fname.value());
				if (mv.containsKey(f)) {
					throw new InvalidEncodingException("Field '" + f.name()
							+ "' already defined in structure.");
				}
				
				DataValue fv = enc.decode(r, dts);
				mv.put(f, fv);
			} catch (AmbiguousNameException|UnknownFieldException e) {
				throw new InvalidEncodingException("No field named '"
						+ fname.value() + "' found in structure '"
						+ type.name() + "'.");
			}
		}
		
		return ((StructureDataType) type).make(mv);
	}
}
