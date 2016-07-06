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
package org.sa.rainbow.core.ports.eseb;

import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.jconv.DefaultTypelibJavaConverter;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConversionRule;
import edu.cmu.cs.able.typelib.parser.DefaultTypelibParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.ports.eseb.converters.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;

public class ESEBProvider {

    /** The set of BusServers on the local machine, keyed by the port **/
    private static final Map<Short, EventBus> s_servers = new HashMap<> ();
    /** The set of BusClients already created, keyed by host:port **/
    private static final Map<String, BusConnection> s_clients = new HashMap<> ();

    /**
     * Connections are reused for the same host,port pair, so keep a count of the number of references (for the server
     * and the client) so that they can be better managed
     */
    private static final Map<BusConnection, Integer> s_clientReferences = new HashMap<> ();
    private static final Map<EventBus, Integer> s_serverReferences = new HashMap<> ();
    /**
     * Return the cached BusServer for this port, creating a new one if it doesn't yet exist
     * 
     * @param port
     *            The port that the server is connected to
     * @return the cached or newly created BusServer
     * @throws IOException
     */
    static EventBus getBusServer (short port) {
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
        return remoteHost + ":" + remotePort;
    }

    static final PrimitiveScope                        SCOPE             = new PrimitiveScope ();
    static final DefaultTypelibJavaConverter CONVERTER = DefaultTypelibJavaConverter.make (SCOPE);
    private static final Set<String> REGISTERED_CONVERTERS = new HashSet<> ();
    private static List<TypelibJavaConversionRule> RULES;
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
            parser.parse (new ParsecFileReader ().read_memory ("enum exit_state {destruct; restart; sleep; abort;}"),
                    context);
            parser.parse (
                    new ParsecFileReader ()
                    .read_memory ("struct rainbow_model {string name; string type; string source; string cls; string system_name; string serialization; string additional_info;}"),
                    context);
            parser.parse (new ParsecFileReader ()
                    .read_memory ("struct model_reference {string model_name; string model_type;}"), context);
            RULES = new LinkedList<> ();
            RULES.add (new CollectionConverter ());
            RULES.add (new TypedAttributeConverter (ESEBProvider.SCOPE));
            RULES.add (new CommandRepresentationConverter (ESEBProvider.SCOPE));
            RULES.add (new GaugeStateConverter (ESEBProvider.SCOPE));
            RULES.add (new DescriptionAttributesConverter (ESEBProvider.SCOPE));
            RULES.add (new GaugeInstanceDescriptionConverter (ESEBProvider.SCOPE));
            RULES.add (new OutcomeConverter (ESEBProvider.SCOPE));
            RULES.add (new ExitStateConverter (ESEBProvider.SCOPE));
            RULES.add (new OperationResultConverter (ESEBProvider.SCOPE));
            for (TypelibJavaConversionRule r : RULES) {
                CONVERTER.add (r);
            }
        }
        catch (LocalizedParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    public static void registerConverter (Class<TypelibJavaConversionRule> converterClass) {
        if (!REGISTERED_CONVERTERS.contains (converterClass.getCanonicalName ())) {
            try {
                Constructor<?> constructor = converterClass.getConstructor (PrimitiveScope.class);
                TypelibJavaConversionRule r = (TypelibJavaConversionRule )constructor.newInstance (SCOPE);
                REGISTERED_CONVERTERS.add (converterClass.getCanonicalName ());
                RULES.add (r);
                CONVERTER.add (r);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                ESEBConnector.LOGGER.error (MessageFormat.format ("Could not construct model converter ''{0}''.",
                        converterClass.getCanonicalName ()));
            }
        }
    }

    public static List<? extends TypelibJavaConversionRule> getConversionRules () {
        return RULES;
    }

    public static short getESEBClientPort () {
        String port = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
        if (port == null) {
            port = "1234";
        }
        return Short.valueOf (port);
    }

    public static short getESEBClientPort (String property) {
        String port = Rainbow.instance ().getProperty (property);
        if (port == null) {
            port = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        return Short.valueOf (port);
    }

    public static String getESEBClientHost (String property) {
        String host = Rainbow.instance ().getProperty (property);
        if (host == null) {
            host = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION);
        }
        return host;
    }

    public static String getESEBClientHost () {
        return Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION, "localhost");
    }

    /**
     * Release a client connection. If there are no more references left, then stop the client altogether
     * 
     * @param client
     */
    public static void releaseClient (BusConnection client) {
        Integer counts = s_clientReferences.get (client);
        if (counts != null) {
            counts--;
            if (counts == 0) {
                client.stop ();
            }
            else {
                s_clientReferences.put (client, counts);
            }
        }
    }

    /**
     * Register the use of a client.
     * 
     * @param client
     */
    public static void useClient (BusConnection client) {
        Integer counts = s_clientReferences.get (client);
        if (counts == null) {
            counts = 1;
        }
        s_clientReferences.put (client, counts);
    }

    /**
     * Register a use of the server
     * 
     * @param srvr
     */
    public static void useServer (EventBus srvr) {
        Integer counts = s_serverReferences.get (srvr);
        if (counts == null) {
            counts = 1;
        }
        s_serverReferences.put (srvr, counts);
    }

    /**
     * Release a server. If there are no more references left, then stop the server
     * 
     * @param srvr
     */
    public static void releaseServer (EventBus srvr) {
        Integer counts = s_serverReferences.get (srvr);
        if (counts != null) {
            counts--;
            if (counts == 0) {
                try {
                    srvr.close ();
                }
                catch (IOException e) {
                    ESEBConnector.LOGGER.error (e.getMessage (), e);
                }
            }
            else {
                s_serverReferences.put (srvr, counts);
            }
        }
    }

}
