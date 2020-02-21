package incubator.scb.sdl;

import java.util.Map;

import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaPackage;

/**
 * Generator that is invoked to generate code in a bean.
 */
public interface SdlBeanGenerator {
	/**
	 * Invoked to generate code for the bean. 
	 * @param b the bean
	 * @param jc the code to generate
	 * @param jp the package where the bean should be generated
	 * @param properties generation properties
	 * @return the generation result
	 * @throws SdlGenerationException failed to generate the SDL code
	 */
	GenerationInfo generate(SdlBean b, JavaCode jc, JavaPackage jp,
			Map<String, String> properties)
			throws SdlGenerationException;
}
