package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.parser.DataTypeNameParser;
import edu.cmu.cs.able.typelib.parser.ParseException;
import edu.cmu.cs.able.typelib.parser.TokenMgrError;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Default implementation of a text encoding which adds all known delegate
 * text encodings provided by the <code>txtenc.typelib</code> package. It
 * also creates data types which were not found but could be parsed using
 * {@link DataTypeNameParser}.
 */
public class DefaultTextEncoding extends TextEncoding {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Creates a new text encoding.
	 * @param pscope the primitive scope to use
	 */
	public DefaultTextEncoding(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		
		m_pscope = pscope;
		
		add(new BooleanDelegateTextEncoding());
		add(new IntegerDelegateTextEncoding());
		add(new StringDelegateTextEncoding());
		add(new FloatDelegateTextEncoding());
		add(new TimeDelegateTextEncoding());
		add(new CollectionDelegateTextEncoding());
		add(new OptionalDelegateTextEncoding());
		add(new TupleDelegateTextEncoding());
		add(new MapDelegateTextEncoding());
		add(new StructureDelegateTextEncoding());
		add(new TypeDelegateTextEncoding());
		add(new EnumerationDelegateTextEncoding());
	}

	@Override
	protected DataType find_data_type(HierarchicalName name,
			DataTypeScope scope) throws InvalidEncodingException {
		DataType found_by_super = super.find_data_type(name, scope);
		if (found_by_super != null) {
			return found_by_super;
		}
		
		try {
			/*
			 * Find the scope the name refers to.
			 */
			DataTypeScope nscope = scope;
			while (!name.leaf()) {
				nscope = (DataTypeScope) nscope.find_scope(name.peek());
				if (nscope == null) {
					return null;
				}
				
				name = name.pop_first();
			}
			
			Ensure.is_null(nscope.find(name));
			
			/*
			 * See if we can parse the name.
			 */
			DataTypeNameParser p = new DataTypeNameParser();
			return p.parse(name.peek(), m_pscope, nscope);
		} catch (TokenMgrError | ParseException | AmbiguousNameException e) {
			return null;
		}
	}
}
