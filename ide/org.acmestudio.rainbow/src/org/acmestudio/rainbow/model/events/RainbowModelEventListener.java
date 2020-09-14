package org.acmestudio.rainbow.model.events;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelInstanceProvider;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBChangeBusAnnouncePort;
import org.sa.rainbow.core.ports.eseb.ESEBGaugeModelUSBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBModelChangeBusSubscriptionPort;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class RainbowModelEventListener {

    private Map<String, ESEBModelChangeBusSubscriptionPort> m_ports         = new HashMap<>();

    private final String                                    m_rainbowHost;
    private final short                                     m_rainbowPort;

    private Map<String, IModelUSBusPort>                    m_announcePorts = new HashMap<>();

    public RainbowModelEventListener(String rainbowHost, short rainbowPort) {
        m_rainbowHost = rainbowHost;
        m_rainbowPort = rainbowPort;
    }

    /**
     * Makes AcmeStudio listen to events on the model change bus that are associated with the
     * system, so that changes reported on the model change bus (reflecting changes on the model in
     * Rainbow) are made on the model in AcmeStudio.
     * 
     * @param systemName
     * @param system
     * @throws IOException
     */
    public void attachToSystem (final String systemName, final IAcmeSystem system) throws IOException {

        // Cache the port
        ESEBModelChangeBusSubscriptionPort port = m_ports.get(systemName);
        if (port == null) {

            // Create the port to the model change bus
            port = new ESEBModelChangeBusSubscriptionPort(m_rainbowHost, m_rainbowPort, new IModelInstanceProvider() {
                // This is a dummy model instance that must be supplied to the port. But seeing the
                // model is already in AcmeStudio, we don't want to get it directly from Rainbow
                @Override
                public <T> IModelInstance<T> getModelInstance (String type, String name) {
                    if ("Acme".equals(type) && name.equals(systemName)) {
                        return (IModelInstance<T>) new AcmeModelInstance(system, "AcmeStudio") {

                            @Override
                            protected AcmeModelInstance generateInstance (IAcmeSystem arg0) {
                                return null;
                            }

                            @Override
                            public AcmeModelCommandFactory getCommandFactory () {
                                return null;
                            }

                        };
                    }
                    return null;
                }
            });
            m_ports.put(systemName, port);

            // Subscribe to changes on the model change bus, getting events that are on Acme models
            // with the same name as the one we are attached to.
            port.subscribe(new IRainbowChangeBusSubscription() {

                Map<String, String> parents = new HashMap<>();

                @Override
                public boolean matches (IRainbowMessage msg) {
                    String eventType = (String) msg.getProperty(IRainbowMessageFactory.EVENT_TYPE_PROP);
                    try {
                        AcmeModelEventType et = AcmeModelEventType.valueOf(eventType);
                        if (systemName.equals(msg.getProperty(IRainbowMessageFactory.MODEL_NAME_PROP))
                                && "Acme".equals(msg.getProperty(IRainbowMessageFactory.MODEL_TYPE_PROP))) {
                            return true;
                        }
                    }
                    catch (Exception e) {
                        // The event is not an Acme event
                    }
                    return false;

                }
            }, new IRainbowModelChangeCallback<IAcmeSystem>() {

                @Override
                public void onEvent (IModelInstance<IAcmeSystem> arg0, IRainbowMessage arg1) {
                    processEvent(arg0.getModelInstance(), arg1);
                }
            });
        }

        // AcmeStudio can make changes to the model in Rainbow also, but these go through the Model
        // US Bus
        IModelUSBusPort announcePort = m_announcePorts.get(systemName);
        if (announcePort == null) {
            announcePort = new ESEBGaugeModelUSBusPort(new Identifiable() {

                @Override
                public String id () {
                    return "AcmeStudio";
                }
            }, m_rainbowHost, m_rainbowPort);
            m_announcePorts.put(systemName, announcePort);
        }
    }

    // Processes a model change bus event by turning it into an AcmeCommand and executing it on the
    // model in AcmeStudio
    protected void processEvent (IAcmeSystem system, IRainbowMessage msg) {
        AcmeEventDeserializer ds = new AcmeEventDeserializer();
        try {
            IAcmeCommand<?> command = ds.deserialize(msg, system);
            command.execute();
        }
        catch (RainbowDeserializationException | IllegalStateException | AcmeException e) {
            e.printStackTrace();
        }
    }

    public void disconnect () {
        for (ESEBModelChangeBusSubscriptionPort port : m_ports.values()) {

        }
    }

    public IModelUSBusPort getAnnouncePort (String name) {
        return m_announcePorts.get(name);
    }

}
