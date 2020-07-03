package incubator.bpref;


/**
 * Implementation of a property handler that handles strings.
 */
class StringPropertyHandler extends AsStringPropertyHandler<String> {
	/**
	 * Creates a new property handler.
	 */
	StringPropertyHandler() {
		super(String.class);
	}
	
	@Override
	protected String convertFromString(String s) throws Exception {
		return s;
	}

	@Override
	protected String convertToString(String t) throws Exception {
		return t;
	}

}
