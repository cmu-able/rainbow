package incubator.jcodegen;

/**
 * Protection levels in <em>java</em>.
 */
public enum ProtectionLevel {
	/**
	 * Public.
	 */
	PUBLIC {
		@Override
		public String keyword() {
			return "public";
		}
	},
	
	/**
	 * Protected.
	 */
	PROTECTED {
		@Override
		public String keyword() {
			return "protected";
		}
	},
	
	/**
	 * Package.
	 */
	PACKAGE {
		@Override
		public String keyword() {
			return "";
		}
	},
	
	/**
	 * Private.
	 */
	PRIVATE {
		@Override
		public String keyword() {
			return "private";
		}
	};
	
	/**
	 * Obtains the keyword associated with the protection level.
	 * @return the keyword
	 */
	public abstract String keyword();
}
