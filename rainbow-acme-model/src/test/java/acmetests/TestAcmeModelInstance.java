package acmetests;
import java.io.IOException;
import java.util.Set;

import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeComponentType;
import org.acmestudio.acme.element.IAcmeElementTypeRef;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.models.IModelInstance;

import auxtestlib.DefaultTCase;


public class TestAcmeModelInstance extends DefaultTCase {

    @Test
    public void testCopyModelInstanceWithFamily () throws ParsingFailureException, IOException, RainbowCopyException {
        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString (
                "src/test/resources/acme/inbuilt-system.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        AcmeModelInstance mi = new BareAcmeModelInstance (sys);

        IModelInstance<IAcmeSystem> copy = mi.copyModelInstance ("S2");
        assertEquals ("S2", copy.getModelInstance ().getName ());
        assertNotEquals (sys, mi);
        assertEquals (1, sys.getDeclaredTypes ().size ());
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        assertEquals (1, sys.getComponents ().size ());
        assertNotNull (copy.getModelInstance ().getComponent ("c"));
        Set<? extends IAcmeElementTypeRef<IAcmeComponentType>> declaredTypes = copy.getModelInstance ()
                .getComponent ("c")
                .getDeclaredTypes ();
        assertEquals (1, declaredTypes.size ());
        assertTrue (declaredTypes.iterator ().next ().isSatisfied ());
    }

    @Test
    public void testCopyModelInstanceStandalone () throws ParsingFailureException, IOException, RainbowCopyException {
        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString (
                "src/test/resources/acme/self-contained.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        AcmeModelInstance mi = new BareAcmeModelInstance (sys);

        IAcmeSystem copy = mi.copyModelInstance ("S2").getModelInstance ();
        assertNotEquals (sys, mi);
        assertEquals (1, sys.getComponents ().size ());
        assertNotNull (copy.getComponent ("c"));
    }

    @Test
    public void testCopyModelInstanceImports () throws ParsingFailureException, IOException, RainbowCopyException {
        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString (
                "src/test/resources/acme/imports.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        AcmeModelInstance mi = new BareAcmeModelInstance (sys);

        IAcmeSystem copy = mi.copyModelInstance ("S2").getModelInstance ();
        assertNotEquals (sys, mi);
        assertEquals (1, sys.getDeclaredTypes ().size ());
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        assertEquals (1, sys.getComponents ().size ());
        assertNotNull (copy.getComponent ("c"));
        Set<? extends IAcmeElementTypeRef<IAcmeComponentType>> declaredTypes = copy.getComponent ("c").getDeclaredTypes ();
        assertEquals (1, declaredTypes.size ());
        assertTrue (declaredTypes.iterator ().next ().isSatisfied ());
    }

}
