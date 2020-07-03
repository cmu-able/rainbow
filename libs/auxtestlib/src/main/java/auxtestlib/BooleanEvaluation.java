package auxtestlib;

/**
 * Evaluation that returns a boolean value.
 */
public interface BooleanEvaluation {
	/**
	 * Performs the evaluation.
	 * @return result of the evaluation
	 * @throws Exception failed to perform the computation
	 */
	boolean evaluate () throws Exception;
}
