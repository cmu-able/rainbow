/**
 * Created July 4, 2006.
 */
package org.sa.rainbow.stitch.model;

import java.io.File;
import java.io.IOException;


/**
 * A repository interface defining method for accessing models, used by the
 * Stitch language module to: <ul>
 * <li> query for model from the core module on model import statement
 * <li> acquire a model snapshot
 * <li> mark (tactic) execution disruption to system utility
 * <li> obtain file location for recording and persisting tactic execution time
 * </ul>
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface ModelRepository {

	/**
	 * A dummy implementation of the model repository that only provides a File
	 * object corresponding to the resource name.
	 */
	public static final ModelRepository NULL_REPO = new ModelRepository() {
		public Object getModelForResource (String resName) throws IOException {
			return new File(resName);
		}
		public Object getSnapshotModel (Object model) {
			return model;  // no snapshot support
		}
		public void markDisruption (double level) {
		}
		public File tacticExecutionHistoryFile() {
			return null;
		}
	};

	public Object getModelForResource (String resName) throws IOException;

	public Object getSnapshotModel (Object model);

	public void markDisruption (double level);

	public File tacticExecutionHistoryFile ();

}
