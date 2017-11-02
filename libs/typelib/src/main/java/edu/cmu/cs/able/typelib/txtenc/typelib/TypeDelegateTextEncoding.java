package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.TypeType;
import edu.cmu.cs.able.typelib.prim.TypeValue;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.HNameAsciiEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes an decodes type references.
 */
public class TypeDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Divider to place at the end of the type name marking its end.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * Creates a new encoding.
	 */
	public TypeDelegateTextEncoding() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof TypeType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.is_instance(v, TypeValue.class);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		TypeValue tv = (TypeValue) v;
		HierarchicalName ref_name = tv.value().absolute_hname();
		String ref_name_enc = new HNameAsciiEncoding().encode(ref_name);
		w.write(ref_name_enc);
		w.write(DIVIDER);
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.is_instance(type, TypeType.class);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		String hname_enc = ReaderUtils.read_until(r, DIVIDER);
		HierarchicalName hname = new HNameAsciiEncoding().decode(hname_enc);
		DataType dt = null;
		try {
			dt = dts.find(hname);
		} catch (AmbiguousNameException e) {
			/*
			 * An ambiguous name means there is no type there.
			 */
		}
		
		if (dt == null) {
			throw new InvalidEncodingException("Type '" + hname +"' was "
					+ "not found in scope.");
		}
		
		return ((TypeType) type).make(dt);
	}
}
