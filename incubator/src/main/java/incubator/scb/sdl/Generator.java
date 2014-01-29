package incubator.scb.sdl;

/**
 * Interface of a generator that can perform some generation.
 */
public interface Generator {
	/**
	 * Performs the generation.
	 * @return the result of generation
	 * @throws SdlGenerationException failed to generate
	 */
	GenerationInfo generate() throws SdlGenerationException;
}
