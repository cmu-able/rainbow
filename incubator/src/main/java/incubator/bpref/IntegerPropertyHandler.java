package incubator.bpref;


/**
 * Implementation of a property handler that handles integers.
 */
class IntegerPropertyHandler extends AsStringPropertyHandler<Integer> {
	/**
	 * Creates a new property handler.
	 */
	IntegerPropertyHandler() {
		super(Integer.class);
	}
	
	@Override
	protected Integer convertFromString(String s) throws Exception {
		return Integer.parseInt(s);
	}

	@Override
	protected String convertToString(Integer t) throws Exception {
		return t.toString();
	}
}
