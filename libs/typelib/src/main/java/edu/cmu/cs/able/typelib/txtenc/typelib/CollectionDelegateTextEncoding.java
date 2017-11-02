package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.comp.CollectionDataType;
import edu.cmu.cs.able.typelib.comp.CollectionDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes collections (types that extend
 * {@link CollectionDataType}.
 */
/*
 * Collections are encoded by encoding an integer with the number of items
 * in the collection and then encoding all items in the collection.
 */
public class CollectionDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Creates a new encoding.
	 */
	public CollectionDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof CollectionDataType;
	}

	@SuppressWarnings("null")
	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.is_instance(v, CollectionDataValue.class);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		List<DataValue> snapshot = ((CollectionDataValue) v).snapshot();
		Ensure.not_null(snapshot);
		
		/*
		 * Search for the int 32 data type which should be in the root of the
		 * data type namespace.
		 */
		Int32Type i32_type = null;
		
		try {
			i32_type = (Int32Type) v.type().parent_dts().find(
				new HierarchicalName(true, Int32Type.NAME));
		} catch (AmbiguousNameException e) {
			Ensure.never_thrown(e);
		}
		
		/*
		 * If i32_type is null means we're in an invalid situation: we
		 * encoding a data type in a hierarchy which doesn't contain the
		 * primitive types.
		 */
		Ensure.not_null(i32_type);
		
		enc.encode(i32_type.make(snapshot.size()), w);
		for (DataValue snap_v : snapshot) {
			enc.encode(snap_v, w);
		}
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.is_instance(type, CollectionDataType.class);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		/*
		 * Read the first data value which should contain an int32 with the
		 * number of values in the snapshot.
		 */
		DataValue read_value = enc.decode(r, dts);
		if (!(read_value instanceof Int32Value)) {
			throw new InvalidEncodingException("The first type in the "
					+ "collection is not an int32, it is a '"
					+ read_value.type().name() + "'.");
		}
		
		int count = ((Int32Value) read_value).value();
		if (count < 0) {
			throw new InvalidEncodingException("The number of items in the "
					+ "collection is " + count + ", which is invalid.");
		}
		
		List<DataValue> values = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			values.add(enc.decode(r, dts));
		}
		
		CollectionDataValue new_col = ((CollectionDataType) type).make();
		new_col.set_contents(values);
		return new_col;
	}
}
