package incubator.scb.sdl;

/**
 * Result of generating code (output of a {@link SdlBeanGenerator}).
 */
public enum GenerationResult {
	/**
	 * Some code was generated but more may need to be generated if other
	 * generators change the code.
	 */
	GENERATED_CODE,
	
	/**
	 * There was nothing to do.
	 */
	NOTHING_TO_DO,
	
	/**
	 * Preconditions for execution were not met. Some code may have been
	 * generated.
	 */
	CANNOT_RUN
}
