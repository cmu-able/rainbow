package incubator.bpref;

/**
 * Implementation of a property handler that handles booleans.
 */
public class BooleanPropertyHandler extends AsStringPropertyHandler<Boolean> {
	/**
	 * Creates a new property handler.
	 */
	BooleanPropertyHandler() {
		super(Boolean.class);
	}
	
	@Override
	protected Boolean convertFromString(String s) throws Exception {
		if ("true".equals(s)) {
			return true;
		}
		
		if ("false".equals(s)) {
			return false;
		}
		
		throw new Exception("Cannot convert '" + s + "' to boolean.");
	}

	@Override
	protected String convertToString(Boolean t) throws Exception {
		return t.toString();
	}
}
