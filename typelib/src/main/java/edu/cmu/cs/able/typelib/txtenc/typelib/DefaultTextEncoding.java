package edu.cmu.cs.able.typelib.txtenc.typelib;

import edu.cmu.cs.able.typelib.txtenc.TextEncoding;

/**
 * Default implementation of a text encoding which adds all known delegate
 * text encodings provided by the <code>txtenc.typelib</code> package.
 */
public class DefaultTextEncoding extends TextEncoding {
	/**
	 * Creates a new text encoding.
	 */
	public DefaultTextEncoding() {
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
	}
}
