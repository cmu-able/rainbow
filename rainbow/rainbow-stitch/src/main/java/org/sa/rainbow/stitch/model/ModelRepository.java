/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
    ModelRepository NULL_REPO = new ModelRepository () {
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

//	public Object getModelForResource (String resName) throws IOException;
//
//	public Object getSnapshotModel (Object model);
//
//	public void markDisruption (double level);
//
//	public File tacticExecutionHistoryFile ();

}
