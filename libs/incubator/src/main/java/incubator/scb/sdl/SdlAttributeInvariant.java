package incubator.scb.sdl;

/**
 * Invariant over an attribute.
 */
public abstract class SdlAttributeInvariant {
	/**
	 * Creates a new invariant.
	 */
	public SdlAttributeInvariant() {
		/*
		 * Nothing to do.
		 */
	}
	
	/**
	 * Generates the invariant check.
	 * @param attr_name the attribute name
	 * @return the code for the check
	 */
	public abstract String generate_check(String attr_name);
}
