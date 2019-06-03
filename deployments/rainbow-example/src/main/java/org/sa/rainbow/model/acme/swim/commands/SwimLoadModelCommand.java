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
package org.sa.rainbow.model.acme.swim.commands;

import java.io.InputStream;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.model.acme.AbstractAcmeLoadModelCmd;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.swim.SwimModelUpdateOperatorsImpl;

public class SwimLoadModelCommand extends AbstractAcmeLoadModelCmd {

	public SwimLoadModelCommand(String systemName, IModelsManager mm, InputStream is, String source) {
		super(systemName, mm, is, source);
	}

	@Override
	protected AcmeModelInstance createAcmeModelInstance(IAcmeSystem system) {
		return new SwimModelUpdateOperatorsImpl(system, getOriginalSource());
	}

}

//public class SwimLoadModelCommand extends AbstractLoadModelCmd<IAcmeSystem> {
//
//    public class AcmePropertySubstitutionVisitor extends AbstractAcmeElementVisitor {
//
//        protected List<IAcmeCommand<?>> m_commands = new LinkedList<> ();
//
//        public IAcmeCommand<?> getCommand () {
//            if (m_commands.isEmpty ()) return null;
//            if (m_commands.size () == 1) return m_commands.get (0);
//            return m_commands.get (0).getCommandFactory ().compoundCommand (m_commands);
//        }
//
//        @Override
//        public Object visitIAcmeProperty (IAcmeProperty property, Object data) throws AcmeVisitorException {
//            if (property.getValue () instanceof IAcmeStringValue) {
//                IAcmeStringValue val = (IAcmeStringValue )property.getValue ();
//                String origVal = val.getValue ();
//                String newVal = Util.evalTokens (origVal);
//                if (!newVal.equals (origVal)) {
//                    IAcmePropertyCommand cmd = property.getCommandFactory ().propertyValueSetCommand (property,
//                            new UMStringValue (newVal));
//                    m_commands.add (cmd);
//                }
//            }
//            return data;
//        }
//    }
//
//    private String                      m_systemName;
//    private SwimModelUpdateOperatorsImpl m_result;
//
//    public SwimLoadModelCommand (String systemName, IModelsManager mm, InputStream is, String source) {
//        super ("loadSwimModel", mm, systemName, is, source);
//        m_systemName = systemName;
//    }
//
//    @Override
//    public ModelReference getModelReference () {
//        return new ModelReference (m_systemName, "Acme");
//    }
//
//
//    @Override
//    protected void subExecute () throws RainbowException {
//        try {
//            IAcmeResource resource = StandaloneResourceProvider.instance ()
//                    .acmeResourceForObject (
//                            new File (getOriginalSource ()));
//            m_result = new SwimModelUpdateOperatorsImpl (resource.getModel ().getSystem (m_systemName),
//                    getOriginalSource ());
//
//            // Do property substitution
//            try {
//                AcmePropertySubstitutionVisitor visitor = new AcmePropertySubstitutionVisitor ();
//                m_result.getModelInstance ().visit (visitor, null);
//                IAcmeCommand<?> cmd = visitor.getCommand ();
//                cmd.execute ();
//            }
//            catch (IllegalStateException | AcmeException e) {
//                e.printStackTrace ();
//            }
//
//            doPostExecute ();
//        }
//        catch (ParsingFailureException | IOException e) {
//            throw new RainbowException (e);
//        }
//    }
//
//    @Override
//    protected void subRedo () throws RainbowException {
//        doPostExecute ();
//    }
//
//    @Override
//    protected void subUndo () throws RainbowException {
//        doPostUndo ();
//    }
//
//    @Override
//    public IModelInstance<IAcmeSystem> getResult () {
//        return m_result;
//    }
//
//    @Override
//    public String getName () {
//        return "LoadSwimModel";
//    }
//
//    @Override
//    protected boolean checkModelValidForCommand (Object model) {
//        return true;
//    }
//
//}
