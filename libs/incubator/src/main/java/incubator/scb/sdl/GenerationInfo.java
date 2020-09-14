package incubator.scb.sdl;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Information created when generating information.
 */
public class GenerationInfo {
	/**
	 * The result of generating information.
	 */
	private GenerationResult m_result;
	
	/**
	 * An optional message provided with the generation result.
	 */
	private String m_message;
	
	/**
	 * Creates a new generation information.
	 * @param r the generation result
	 */
	public GenerationInfo(GenerationResult r) {
		Ensure.not_null(r, "r == null");
		
		m_result = r;
	}
	
	/**
	 * Creates a generation info built from other generation infos. The result
	 * of this generation will be {@link GenerationResult#NOTHING_TO_DO} if
	 * there are no infos all they all are
	 * {@link GenerationResult#NOTHING_TO_DO}. The result will be
	 * {@link GenerationResult#GENERATED_CODE} if any of the infos is
	 * {@link GenerationResult#GENERATED_CODE}. The result will be
	 * {@link GenerationResult#CANNOT_RUN} otherwise.
	 * @param infos the informations to generate this information from
	 */
	public GenerationInfo(List<GenerationInfo> infos) {
		Ensure.not_null(infos, "infos == null");
		
		List<String> failed = new ArrayList<>();
		boolean generated_code = false;
		
		for(GenerationInfo i : infos) {
			switch (i.result()) {
			case CANNOT_RUN:
				failed.add(i.message());
				break;
			case GENERATED_CODE:
				generated_code = true;
				break;
			case NOTHING_TO_DO:
				break;
			}
		}
		
		if (generated_code) {
			m_result = GenerationResult.GENERATED_CODE;
			m_message = null;
		} else if (failed.size() > 0) {
			m_result = GenerationResult.CANNOT_RUN;
			m_message = StringUtils.join(failed, ",");
		} else {
			m_result = GenerationResult.NOTHING_TO_DO;
			m_message = null;
		}
	}
	
	/**
	 * Creates a new generation information.
	 * @param r the generation result
	 * @param message a generation mesage
	 */
	public GenerationInfo(GenerationResult r, String message) {
		Ensure.not_null(r, "r == null");
		
		m_result = r;
		m_message = message;
	}
	
	/**
	 * Obtains the generation result.
	 * @return the generation result
	 */
	public GenerationResult result() {
		return m_result;
	}
	
	/**
	 * Obtains a generation message.
	 * @return the generation message
	 */
	public String message() {
		return m_message;
	}
}
