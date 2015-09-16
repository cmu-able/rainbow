package org.sa.rainbow.translator.znn.gauges;

import edu.cmu.cs.able.typelib.type.DataType;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGauge;
import org.sa.rainbow.core.gauges.CommandRepresentation;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiagnosisGauge extends AbstractGauge {

    static class RequestData {
        public Date time;
        public String ip;
    }

    static class DiagnosisResult {
        public Map<String, Double> maliciousness = new HashMap<String, Double>();
    }

    public static final String NAME = "G - Diagnosis";
    private static final int TIME_WINDOW_SECONDS = 10;
    private static final String CLIENTS_CONFIG_PARAM_NAME = "clients";
    private boolean m_configured = false;
    private boolean m_computingDiagnosis;

    /*
     * Maps IP addresses to client names.
     */
    private Map<String, String> m_clients;
    private List<RequestData> m_requests;

    public DiagnosisGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws IOException, RainbowException {
        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams,
                mappings);

        m_clients = new HashMap<String, String>();
        m_computingDiagnosis = false;
        m_requests = new LinkedList<RequestData>();
        new EventReader();
        new RequestEventReader();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        trimWindow();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public synchronized boolean configureGauge(
            List<TypedAttributeWithValue> configParams) {
        // Possible race condition on m_configured, doesn't really matter
        // because runAction gets called periodically
        if (configParams.isEmpty())
            return false;
        m_configured = super.configureGauge(configParams);

        if (!m_configured)
            return m_configured;
        String clients_param = null;
        for (TypedAttributeWithValue avt : configParams) {
            if (avt.getName ().equals (CLIENTS_CONFIG_PARAM_NAME)) {
                clients_param = avt.getValue ().toString ();
            }
            else if (avt.getName ().equals ("reportingPeriod")) {
                setSleepTime (Long.valueOf (avt.getValue ().toString ()));
            }
        }

        if (clients_param == null) {

            String msg = MessageFormat
                    .format("{0}: No parameter ''{1}'' found. Diagnosis gauge won''t report anything useful.",
                            this.id(), CLIENTS_CONFIG_PARAM_NAME);
            getReportingPort ().error (RainbowComponentT.GAUGE,msg);
            m_configured = false;
        } else {
            String[] client_defs = clients_param.split(",");
            Pattern p = Pattern.compile("([^=]+)=(.+)");
            for (String s : client_defs) {
                Matcher m = p.matcher(s);
                if (!m.matches()) {
                    String msg = MessageFormat
                            .format("{0}: Invalid client configuration ignored: ''{1}''.",
                                    this.id(), s);
                    getReportingPort ().error (RainbowComponentT.GAUGE,msg);
                } else {
                    String name = m.group(1);
                    String value = m.group(2);
                    if (m_clients.containsKey(value)) {
                        log("Duplicate IP address '" + value + "' ignored.");
                    } else {
                        m_clients.put(value, name);
                        log("Client '" + name + "' has IP address '" + value
                                + "'.");
                    }
                }
            }
        }

        return m_configured;

    }

    @Override
    protected void runAction() {
        super.runAction();
        if (m_configured) {
            startComputeAndReportDiagnosis();
        }
    }

    private synchronized void startComputeAndReportDiagnosis() {
        /*
         * If computation is still ongoing, we'll skip it.
         */
        if (m_computingDiagnosis) return;

        m_computingDiagnosis = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                computeAndReportDiagnosis();
            }
        }).start();
    }

    private void computeAndReportDiagnosis() {
        try {
            Map<String, Integer> counts = new HashMap<String, Integer>();
            for (String c : m_clients.values()) {
                counts.put(c, 0);
            }

            synchronized (this) {
                for (RequestData rd : m_requests) {
                    String client = m_clients.get(rd.ip);
                    if (client == null) {
                        log("I do not know who " + rd.ip + " is.");
                    } else {
                        int cnt = counts.get(client);
                        cnt++;
                        counts.put(client, cnt);
                    }
                }
            }

            DiagnosisResult r = new DiagnosisResult();
            log("Diagnosis Gauge data for reporting:");
            for (Map.Entry<String, Integer> c : counts.entrySet()) {
                double maliciousness = c.getValue() > 20 ? 0.9 : 0.1;
                log("    - Client (" + c.getKey() + "): " + maliciousness
                        + " (request count = " + c.getValue() + ")");
                r.maliciousness.put(c.getKey(), maliciousness);
            }

            /*
             * Send the diagnosis.
             */
            List<TypedAttributeWithValue> toSend = new ArrayList<TypedAttributeWithValue> ();
            for (Entry<String, Double> e : r.maliciousness.entrySet()) {
                IRainbowOperation cmd = m_commands.get (0);
                Map<String, String> pMap = new HashMap<> ();
                OperationRepresentation crep = new OperationRepresentation (cmd.getName (), cmd.getModelReference (), cmd.getTarget ()


                        cmd.getName (), cmd.getCommandName (),
                        cmd.getModelName (), cmd.getModelType (), e.getKey (), cmd.getParameters ());
                pMap.put (cmd.getParameters ()[0], Double.toString (e.getValue ()));
                issueCommand (crep, pMap);
            }
        } catch (Exception e) {
            log("Diagnosis computation failed with " + e.getClass() + ": "
                    + e.getMessage());
            e.printStackTrace();
        } finally {
            synchronized (this) {
                m_computingDiagnosis = false;
            }
        }
    }

    private synchronized void trimWindow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -TIME_WINDOW_SECONDS);
        Date limit = cal.getTime();

        while (m_requests.size() > 0 && m_requests.get(0).time.before(limit)) {
            m_requests.remove(0);
        }
    }

    class RequestEventReader extends Thread {
        private SubscribeConnection m_subscribeConnection;
        private BufferedWriter m_output;

        private RequestEventReader() {
            String location = Rainbow.property(Rainbow.PROPKEY_MASTER_LOCATION);
            m_subscribeConnection = new SubscribeConnection(location,
                    (short) 2244);
            start();
        }

        @Override
        public void run() {
            File file = new File ("diagnosis.out");
            try {
                m_output = new BufferedWriter(new FileWriter(
                        file));
                boolean ok = true;
                while (ok) {
                    try {
                        DataType d;
                        while ((d = m_subscribeConnection.receive()) != null) {
                            if (!(d instanceof EventDataType)) {
                                continue;
                            }

                            EventDataType edt = (EventDataType) d;
                            if (edt.getType().equals("http::http_response")) {
                                String ip = ((StringDataType) edt.getRecord()
                                        .get("ip_address")).getValue();
                                if (edt.getRecord().get("status") != null) {
                                    int code = ((Int32DataType) edt.getRecord()
                                            .get("status")).getValue();
                                    Date time = edt.getEndTime();
                                    m_output.write(Long.toString(time.getTime()));
                                    m_output.write(",");
                                    m_output.write(ip);
                                    m_output.write(",");
                                    m_output.write(Integer.toString(code));
                                    m_output.write("\n");
                                }
                                else {
                                    m_output.write(Long.toString (edt.getEndTime().getTime ()));
                                    m_output.write (",");
                                    m_output.write (ip);
                                    m_output.write (",");
                                    m_output.write ("No status code");
                                }
                            }
                        }
                        m_output.flush ();
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    m_output.close ();
                    m_output = null;
                } catch (IOException e) {
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if (m_output != null) {
                m_output.flush();
                m_output.close ();
            }
            super.finalize();
        }

    }

    class EventReader extends Thread {
        private SubscribeConnection m_subscribeConnection;

        private EventReader() {
            String location = Rainbow.property(Rainbow.PROPKEY_MASTER_LOCATION);
            m_subscribeConnection = new SubscribeConnection(location,
                    (short) 2244);
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DataType d;
                    while ((d = m_subscribeConnection.receive()) != null) {
                        if (!(d instanceof EventDataType)) {
                            continue;
                        }

                        EventDataType edt = (EventDataType) d;
                        if (edt.getType().equals("http::http_request")) {
                            String ip = ((StringDataType) edt.getRecord().get(
                                    "ip_address")).getValue();
                            RequestData rd = new RequestData();
                            rd.time = edt.getEndTime();
                            rd.ip = ip;

                            synchronized (this) {
                                m_requests.add(rd);
                            }
                        }
                    }

                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
