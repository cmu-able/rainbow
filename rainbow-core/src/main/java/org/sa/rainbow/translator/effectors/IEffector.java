/**
 * Created November 4, 2006.
 */
package org.sa.rainbow.translator.effectors;

import java.util.List;

import org.sa.rainbow.core.ports.IRainbowReportingPort;

/**
 * General interface for the system Effector. The Identifiable.id() returns the reference ID of this effector, which is
 * the same ID used by Rainbow to obtain access to particular effectors.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IEffector extends IEffectorIdentifier, IEffectorExecutionPort {


    public static IEffector NULL_EFFECTOR = new IEffector () {
        @Override
        public String id () {
            return "NullEffector@0.0.0.0";
        }

        @Override
        public String service () {
            return "ANull";
        }

        @Override
        public Kind kind () {
            return Kind.NULL;
        }

        @Override
        public Outcome execute (List<String> args) {
            return Outcome.UNKNOWN;
        }

        @Override
        public void setReportingPort (IRainbowReportingPort port) {
        }

    };


    public void setReportingPort (IRainbowReportingPort port);

}
