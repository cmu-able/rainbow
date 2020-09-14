package incubator.scb.sdl;

import incubator.pval.Ensure;

/**
 * Invariant that ensures that the attribute is not null.
 */
public class SdlAttributeNotNullInvariant extends SdlAttributeInvariant {
	/**
	 * Creates a new invariant.
	 */
	public SdlAttributeNotNullInvariant() {
		/*
		 * Nothing to do.
		 */
	}
	
	@Override
	public String generate_check(String attr_name) {
		Ensure.not_null(attr_name, "attr_name == null");
		return "incubator.pval.Ensure.not_null(" + attr_name
				+ ", \"" + attr_name + " == null\");\n";
	}
}
