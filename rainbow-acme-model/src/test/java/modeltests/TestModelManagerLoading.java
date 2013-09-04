package modeltests;

import java.io.File;

import junit.framework.TestCase;

import org.acmestudio.acme.element.IAcmeSystem;
import org.junit.Test;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.models.IModelInstance;

public class TestModelManagerLoading extends TestCase {

    @Test
    public void test () throws Throwable {

        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());

        RainbowMaster master = new RainbowMaster ();
        master.initialize ();
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        IModelInstance<IAcmeSystem> modelInstance = master.modelsManager ().<IAcmeSystem> getModelInstance ("Acme",
                "ZNewsSys");
        assertNotNull (modelInstance);
        IAcmeSystem system = modelInstance.getModelInstance ();
        assertNotNull (system);

    }

}
