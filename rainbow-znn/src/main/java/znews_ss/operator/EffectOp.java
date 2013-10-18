package znews_ss.operator;

import java.util.HashMap;
import java.util.Map;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeElement;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.stitch.adaptation.IGenericArchOperators;

public class EffectOp {
    // This needs to be changed to commands
    public static void blackhole (Object lb, String client) {
        Map<String,String> pairs = new HashMap<String,String> ();
        pairs.put("client", client);
        IGenericArchOperators opProvider = (IGenericArchOperators )Rainbow.instance ().getRainbowMaster ()
                .strategyExecutor (ModelHelper.getAcmeSystem (((IAcmeElement )lb)).getName () + ":Acme");
        opProvider.changeState ("blackhole", lb, pairs);
    }
}
