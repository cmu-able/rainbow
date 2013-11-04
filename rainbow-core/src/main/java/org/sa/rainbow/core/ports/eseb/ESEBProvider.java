package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.ports.eseb.converters.CollectionConverter;
import org.sa.rainbow.core.ports.eseb.converters.CommandRepresentationConverter;
import org.sa.rainbow.core.ports.eseb.converters.DescriptionAttributesConverter;
import org.sa.rainbow.core.ports.eseb.converters.GaugeInstanceDescriptionConverter;
import org.sa.rainbow.core.ports.eseb.converters.GaugeStateConverter;
import org.sa.rainbow.core.ports.eseb.converters.OperationResultConverter;
import org.sa.rainbow.core.ports.eseb.converters.OutcomeConverter;
import org.sa.rainbow.core.ports.eseb.converters.TypedAttributeConverter;

import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.jconv.DefaultTypelibJavaConverter;
import edu.cmu.cs.able.typelib.parser.DefaultTypelibParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

public class ESEBProvider {

    /** The set of BusServers on the local machine, keyed by the port **/
    protected static Map<Short, EventBus>       s_servers            = new HashMap<> ();
    /** The set of BusClients already created, keyed by host:port **/
    protected static Map<String, BusConnection> s_clients            = new HashMap<> ();
    /**
     * Return the cached BusServer for this port, creating a new one if it doesn't yet exist
     * 
     * @param port
     *            The port that the server is connected to
     * @return the cached or newly created BusServer
     * @throws IOException
     */
    static EventBus getBusServer (short port) throws IOException {
        EventBus s = s_servers.get (port);
        if (s == null || s.closed ()) {
            ESEBConnector.LOGGER.debug (MessageFormat.format ("Constructing a new BusServer on port {0}", port));
            try {
                s = new EventBus (port, ESEBProvider.SCOPE);
                s_servers.put (port, s);
                s.start ();
            }
            catch (Exception e) {
                ESEBConnector.LOGGER.warn (MessageFormat.format ("BusServer could not be created on port {0}", port));
            }
        }
        return s;
    }
    /**
     * Return the cached BusClient for this host:port combination, or create a new one if it doesn't yet exist
     * 
     * @param remoteHost
     *            The host for the client
     * @param remotePort
     *            The port for the client
     * @return The cached or newly created BusClient
     */
    static BusConnection getBusClient (String remoteHost, short remotePort) {
        // Make sure that we translate host names to IPs
        remoteHost = Rainbow.canonicalizeHost2IP (remoteHost);
        String key = ESEBProvider.clientKey (remoteHost, remotePort);
        BusConnection c = s_clients.get (key);
        if (c == null || c.state () == BusConnectionState.DISCONNECTED) {
            ESEBConnector.LOGGER.debug (MessageFormat.format ("Constructing a new BusClient on {0}", key));
            c = new BusConnection (remoteHost, remotePort, ESEBProvider.SCOPE);
            s_clients.put (key, c);
            c.start ();
        }
        return c;
    }

    private static String clientKey (String remoteHost, short remotePort) {
        StringBuilder sb = new StringBuilder ();
        sb.append (remoteHost);
        sb.append (":");
        sb.append (remotePort);
        String key = sb.toString ();
        return key;
    }

    static final PrimitiveScope                        SCOPE             = new PrimitiveScope ();
    protected static final DefaultTypelibJavaConverter CONVERTER         = DefaultTypelibJavaConverter.make (SCOPE);
    static {


        DefaultTypelibParser parser = DefaultTypelibParser.make ();
        TypelibParsingContext context = new TypelibParsingContext (SCOPE, SCOPE);
        try {
            parser.parse (new ParsecFileReader ()
            .read_memory ("struct typed_attribute_with_value {string name; string type; any? value;}"), context);
            parser.parse (
                    new ParsecFileReader ()
                    .read_memory ("struct operation_representation {string target; string modelName; string modelType; string name; list<string> params;}"),
                    context);
            parser.parse (
                    new ParsecFileReader ()
                    .read_memory ("struct gauge_state {list<typed_attribute_with_value> setup; list<typed_attribute_with_value> config; list<operation_representation> commands;}"),
                    context);
            parser.parse (
                    new ParsecFileReader ()
                    .read_memory ("struct probe_description {string name; string alias; string location; string kind_name; string kind; map<string,string> info; map<string,list<string>> arrays;}"),
                    context);
            parser.parse (
                    new ParsecFileReader ()
                    .read_memory ("struct effector_description {string name; string location; string kind_name; string kind; map<string,string> info; map<string,list<string>> arrays;}"),
                    context);
            parser.parse (
                    new ParsecFileReader ()
                    .read_memory ("struct gauge_instance {string name; string comment; string type; string type_comment; string model_name; string model_type; list<typed_attribute_with_value> setup_params; map<string,operation_representation> commands;}"),
                    context);
            parser.parse (
                    new ParsecFileReader ().read_memory ("struct operation_result {string reply; string result;}"),
                    context);
            parser.parse (new ParsecFileReader ()
            .read_memory ("enum outcome {unknown; confounded; failure; success; timeout;}"), context);

            CONVERTER.add (new CollectionConverter ());
            CONVERTER.add (new TypedAttributeConverter (ESEBProvider.SCOPE));
            CONVERTER.add (new CommandRepresentationConverter (ESEBProvider.SCOPE));
            CONVERTER.add (new GaugeStateConverter (ESEBProvider.SCOPE));
            CONVERTER.add (new DescriptionAttributesConverter (ESEBProvider.SCOPE));
            CONVERTER.add (new GaugeInstanceDescriptionConverter (ESEBProvider.SCOPE));
            CONVERTER.add (new OutcomeConverter (ESEBProvider.SCOPE));
            CONVERTER.add (new OperationResultConverter (ESEBProvider.SCOPE));
        }
        catch (LocalizedParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    public static short getESEBClientPort () {
        String port = Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
        if (port == null) {
            port = "1234";
        }
        short p = Short.valueOf (port);
        return p;
    }

    public static short getESEBClientPort (String property) {
        String port = Rainbow.getProperty (property);
        if (port == null) {
            port = Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);
        return p;
    }

    public static String getESEBClientHost (String property) {
        String host = Rainbow.getProperty (property);
        if (host == null) {
            host = Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION);
        }
        return host;
    }

    public static String getESEBClientHost () {
        String host = Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION, "localhost");
        return host;
    }

}
