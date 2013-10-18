package acmetests;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.acmestudio.acme.core.exception.AcmeVisitorException;
import org.acmestudio.acme.core.globals.AcmeCategory;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeComponentType;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementTypeRef;
import org.acmestudio.acme.element.IAcmeReference;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.util.AcmeElementCollector;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import auxtestlib.DefaultTCase;


public class TestAcmeModelInstance extends DefaultTCase {

    @Test
    public void testCopyModelInstanceComplextFamilies () throws ParsingFailureException, IOException,
            RainbowCopyException, AcmeVisitorException {
        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString (
                "src/test/resources/acme/ZNewsSys.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        AcmeModelInstance mi = new BareAcmeModelInstance (sys);

        AcmeElementCollector allElementCollector = new AcmeElementCollector (EnumSet.of (AcmeCategory.ACME_COMPONENT,
                AcmeCategory.ACME_CONNECTOR, AcmeCategory.ACME_PORT, AcmeCategory.ACME_ROLE));
        Set<IAcmeElement> elements = new HashSet<> ();
        allElementCollector.visit (sys, elements);

        for (IAcmeElement element : elements) {
            IAcmeElementInstance i = (IAcmeElementInstance )element;
            for (Object r : i.getDeclaredTypes ()) {
                assertTrue (
                        i.getQualifiedName () + " refers to unresolved declared type: "
                                + ((IAcmeReference )r).getReferencedName (), ((IAcmeReference )r).isSatisfied ());
            }
            for (Object r : i.getInstantiatedTypes ()) {
                assertTrue (
                        i.getQualifiedName () + " refers to unresolved instantiated type: "
                                + ((IAcmeReference )r).getReferencedName (), ((IAcmeReference )r).isSatisfied ());
            }
        }

        IModelInstance<IAcmeSystem> copy = mi.copyModelInstance ("ZNewsSysSnapshot");

        elements = new HashSet<> ();
        allElementCollector.visit (copy.getModelInstance (), elements);

        for (IAcmeElement element : elements) {
            IAcmeElementInstance i = (IAcmeElementInstance )element;
            for (Object r : i.getDeclaredTypes ()) {
                assertTrue (
                        i.getQualifiedName () + " refers to unresolved declared type: "
                                + ((IAcmeReference )r).getReferencedName (), ((IAcmeReference )r).isSatisfied ());
            }
            for (Object r : i.getInstantiatedTypes ()) {
                assertTrue (
                        i.getQualifiedName () + " refers to unresolved instantiated type: "
                                + ((IAcmeReference )r).getReferencedName (), ((IAcmeReference )r).isSatisfied ());
            }
        }
    }

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
