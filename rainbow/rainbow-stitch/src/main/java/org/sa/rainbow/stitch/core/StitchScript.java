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
 * Created April 4, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.acmestudio.acme.model.IAcmeModel;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchScopeEstablisher.StitchImportedDirectAcmeModelInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a parsed Stitch Script scoped object.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class StitchScript extends ScopedEntity implements IScope {

    public List<Import>        imports = null;
    /**
     * Stores a mapping of renamed string to the original name string.
     */
    public Map<String, String> renames = null;

    public List<Tactic>            tactics    = null;
    public List<Strategy>          strategies = null;
    public List<Class>             ops        = null;
    public List<AcmeModelInstance> models     = null;

    private List<AcmeModelInstance> m_snapshotModels = null; // for tactic eval
    
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();


    /**
     * Main Constructor for a new StitchScript object.
     *
     * @param parent the parent scope
     * @param name   the name of this scope
     * @param stitch the Stitch evaluation context object
     */
    public StitchScript (IScope parent, String name, Stitch/*State*/ stitch) {
        super (parent, name, stitch);

        imports = new ArrayList<Import> ();
        renames = new HashMap<String, String> ();
        tactics = new ArrayList<Tactic> ();
        strategies = new ArrayList<Strategy> ();
        ops = new ArrayList<Class> ();
        models = new ArrayList<AcmeModelInstance> ();
        m_snapshotModels = new ArrayList<AcmeModelInstance> ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitchState.core.ScopedEntity#lookup(java.lang.String)
     */
    @Override
    public Object lookup (String name) {
        if (name == null) return null;

        Object obj = super.lookup (name);
        if (obj == null) {  // try list of tactics
            for (Tactic t : tactics) {
                if (name.equals (t.getName ())) {
                    obj = t;
                    break;
                }
            }
        }
        if (obj == null) { // try list of Strategies
        	for (Strategy s : strategies) {
        		if (name.equals(s.getName())) {
        			obj = s;
        			break;
        		}
        	}
        }
        // TODO: search root scope for tactic!
        if (obj == null) {  // try looking up model reference
            List<AcmeModelInstance> lookupModels = null;
            if (m_snapshotModels.size () > 0) {  // use snapshots
                lookupModels = m_snapshotModels;
            } else {
                lookupModels = models;
            }
            for (AcmeModelInstance model : lookupModels) {
                // replace renames first
                int dotIdx = name.indexOf (".");
                String rootName = null;
                if (dotIdx > -1) {  // look at first segment only
                    rootName = name.substring (0, dotIdx);
                } else {  // look at entire label for rename
                    rootName = name;
                }
                if (renames.containsKey (rootName)) {  // grab replacement
                    rootName = renames.get (rootName);
                }
                if ("getCommandFactory".equals (rootName)) {
                    try {
						AcmeModelCommandFactory commandFactory = model.getCommandFactory ();
						obj = commandFactory;
					} catch (RainbowException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                } else {// substitute
                    name = rootName + (dotIdx > -1 ? name.substring (dotIdx) : "");
                    if (model.getModelName ().equals (name)) {
                    	if (model instanceof StitchImportedDirectAcmeModelInstance) {
                    		obj = model.getModelInstance();
                    	}
                    	else 
                    		obj = model/*.getModelInstance()*/;
                    } else {
                        obj = model.getModelInstance ().lookupName (name, true);
                    }
                }
                if (obj != null) {
                    break;
                }
            }
        }

        return obj;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitchState.core.ScopedEntity#toString()
     */
    @Override
    public String toString () {
        return "script: name \"" + m_name + "\", renames " + renames.toString ();
    }

    public void addRename (String fidAST, String tidAST) {
        renames.put (fidAST, tidAST);
    }

    public boolean isApplicableForModel (IAcmeModel model) {
        return models.contains (model);
    }

    public boolean isApplicableForSystem (AcmeModelInstance system) {
        return models.contains (system);
    }

    /**
     * Creates a snapshot of the current Acme model, and causes all ensuing
     * access of model properties to go against snapshot.  This is effective
     * until model is "unfrozen."
     */
    public void freezeModel () {
//        m_snapshotModels.clear ();
//        for (AcmeModelInstance model : models) {
//            try {
//                AcmeModelInstance snapshot = (AcmeModelInstance )model.copyModelInstance (model.getModelName ()
//                        + "Snapshot");
//                m_snapshotModels.add (snapshot);
//            }
//            catch (RainbowCopyException e) {
//
//            }
//        }
    }

    /**
     * Discards the model snapshots, which amounts to clearing the snapshot list.
     */
    public void unfreezeModel () {
        for (AcmeModelInstance model : m_snapshotModels) {
            try {
                m_rainbowEnvironment.getRainbowMaster ().modelsManager ().unregisterModel (model);
            } catch (RainbowModelException e) {
            }
        }
        m_snapshotModels.clear ();

    }

}
