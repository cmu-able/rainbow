package edu.cmu.cs.able.typelib.txtenc;

import incubator.pval.Ensure;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.AsciiEncoding;
import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Encoding that converts all data values into text. The text encoding does
 * not know, by itself, how to encode any data types, it just provides basic
 * infrastructure to encode and decode. Support for types is done through
 * {@link DelegateTextEncoding} objects: each delegate supports one or more
 * data types. When encoding or decoding a type, the encoding will search for
 * a delegate encoding that supports the type (and one must be found).
 */
/*
 * Encoding of each data value is done by encoding the absolute name of the
 * data type followed by a marker string followed by the value's encoding.
 */
public class TextEncoding implements DataValueEncoding {
	/**
	 * Marker used to end the type name. This marker may not be part of an
	 * absolute hierarchical name.
	 */
	private static final String NAME_ENCODING_END = "|";
	
	/**
	 * Known encoders.
	 */
	private Set<DelegateTextEncoding> m_encoders;
	
	/**
	 * Creates a new text encoding.
	 */
	public TextEncoding() {
		m_encoders = new HashSet<>();
	}
	
	/**
	 * Adds an encoding that may be used for encoding if one of the data types
	 * it supports is to be encoded.
	 * @param c the encoding
	 */
	public void add(DelegateTextEncoding c) {
		Ensure.not_null(c);
		m_encoders.add(c);
	}

	@Override
	public void encode(DataValue value, final DataOutputStream os)
			throws IOException {
		Ensure.not_null(value);
		Ensure.not_null(os);
		
		OutputStream delegate = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				os.write(b);
			}

			@Override
			public void write(byte[] b) throws IOException {
				os.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				os.write(b, off, len);
			}
		};
		
		try (Writer w = new OutputStreamWriter(delegate)) {
			encode(value, w);
		}
	}
	
	/**
	 * Encodes a data value to a writer.
	 * @param value the value to encode
	 * @param w the writer to write the output of the encoding
	 * @throws IOException failed to write
	 */
	public void encode(DataValue value, Writer w) throws IOException {
		Ensure.not_null(value);
		Ensure.not_null(w);
		
		DataType dt = value.type();
		HierarchicalName hname = dt.absolute_hname();
		String hname_enc = new HNameAsciiEncoding().encode(hname);
		Ensure.is_false(hname_enc.contains(NAME_ENCODING_END));
		
		String dtn = dt.absolute_hname().toString();
		Ensure.is_true(AsciiEncoding.is_ascii(dtn));
		
		DelegateTextEncoding sc = find(dt);
		/*
		 * If this fails, then there is no encoding that supports the data
		 * type.
		 */
		Ensure.not_null(sc);
		
		w.write(hname_enc);
		w.write(NAME_ENCODING_END);
		sc.encode(value, w, this);
	}

	@Override
	public DataValue decode(final DataInputStream is, DataTypeScope scope)
			throws IOException, InvalidEncodingException {
		Ensure.not_null(is);
		Ensure.not_null(scope);
		
		class InnerReader extends Reader {
			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				int r;
				for (r = 0; r < len; r++) {
					int ch = is.read();
					if (ch == -1) {
						break;
					}
					
					if (ch > 127) {
						throw new IOException("Invalid character code " + ch
								+ ".");
					}
					
					cbuf[off + r] = (char) ch;
				}
				
				if (r == 0) {
					throw new EOFException();
				}
				
				return r;
			}
			
			@Override
			public void close() throws IOException {
				/*
				 * Nothing to do.
				 */
			}
		};
		
		try (Reader r = new InnerReader()) {
			return decode(r, scope);
		}
	}

	/**
	 * Decodes a data value from a character stream.
	 * @param r the stream to read the data value from
	 * @param scope the scope where to find data types
	 * @return the decoded value
	 * @throws IOException failed to read data from the stream
	 * @throws InvalidEncodingException the encoding in the stream is not
	 * valid
	 */
	public DataValue decode(Reader r, DataTypeScope scope)
			throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(scope);
		
		/*
		 * First read data until we found the NAME_ENCODING_END.
		 */
		Ensure.equals(1, NAME_ENCODING_END.length());
		StringBuilder nb = new StringBuilder();
		int c;
		while ((c = r.read()) != -1) {
			if (c == NAME_ENCODING_END.charAt(0)) {
				break;
			} else {
				nb.append((char) c);
			}
		}
		
		if (c == -1) {
			if (nb.length() > 0) {
				throw new InvalidEncodingException("EOF found while reading "
						+ "data type name.");
			} else {
				throw new EOFException();
			}
		}
		
		Ensure.equals(NAME_ENCODING_END.charAt(0), (char) c);
		String data_type_name = nb.toString();
		
		if (data_type_name.length() == 0) {
			throw new InvalidEncodingException("Empty data type name.");
		}
		
		HierarchicalName hn = new HNameAsciiEncoding().decode(data_type_name);
		
		/*
		 * Find the data type.
		 */
		DataType dt = null;
		try {
			dt = scope.find(hn);
		} catch (AmbiguousNameException e) {
			throw new InvalidEncodingException("Cannot find data type '"
					+ hn.toString() + "'.", e);
		}
		
		DelegateTextEncoding sc = find(dt);
		Ensure.not_null(sc);
		
		return sc.decode(r, dt, scope, this);
	}
	
	/**
	 * Finds a delegate encoding for the given data type.
	 * @param dt the data type
	 * @return the encoding found or <code>null</code> if none
	 */
	private DelegateTextEncoding find(DataType dt) {
		Ensure.not_null(dt);
		
		for (DelegateTextEncoding e : m_encoders) {
			if (e.supports(dt)) {
				return e;
			}
		}
		
		return null;
	}
}
