package org.sa.rainbow.core.ports.eseb.rpc;

import java.util.List;

import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBEffectorExecutionRemoteInterface extends IEffectorExecutionPort {

    @Override
    @ReturnTypeMapping ("outcome")
    @ParametersTypeMapping ({ "list<string>" })
    public Outcome execute (List<String> args);
}
