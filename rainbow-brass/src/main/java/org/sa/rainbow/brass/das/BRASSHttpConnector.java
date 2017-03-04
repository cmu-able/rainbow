package org.sa.rainbow.brass.das;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BRASSHttpConnector /*extends AbstractRainbowRunnable*/ implements IBRASSConnector {

    public static final MediaType     JSON               = MediaType.parse ("application/json");
    public static final OkHttpClient  CLIENT             = new OkHttpClient ();
    public static final DateFormat    DF                 = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final String        STATUS_SERVER      = "http://localhost:5000";
    private static BRASSHttpConnector s_instance;
    public Queue<Request>             m_requestQ         = new SynchronousQueue<> ();
    private Callback                  m_responseCallback = new Callback () {

        @Override
        public void onResponse (Call call, Response response)
                throws IOException {
            response.close ();

        }

        @Override
        public void onFailure (Call call, IOException e) {
            System.err.println ("Failed to connect to shim");
        }
    };
    private Gson                      m_gsonPP;

    protected BRASSHttpConnector () {
        // TODO Auto-generated constructor stub
//        super ("BRASSConnector");
//        setSleepTime (1000);
        m_gsonPP = new GsonBuilder ().setPrettyPrinting ().create ();
    }

    public static BRASSHttpConnector instance () {
        if (s_instance == null) {
            s_instance = new BRASSHttpConnector ();
//            s_instance.start ();
        }
        return s_instance;
    }

    @Override
    public void reportReady (boolean ready) {
//        try {
        JsonObject json = getTimeJSON ();
        json.addProperty ("STATUS", "RAINBOW_READY");
        json.addProperty ("MESSAGE", "Rainbow is receiving information from Robot");
        String jsonStr = m_gsonPP.toJson (json);
        System.out.println ("Reporting ready: " + jsonStr);
        RequestBody body = RequestBody.create (JSON, jsonStr);
        Request request = new Request.Builder ().url (STATUS_SERVER + "/internal/status").post (body).build ();
        CLIENT.newCall (request).enqueue (m_responseCallback);

//            m_requestQ.offer (request);
//        }
//        catch (IOException e) {
//        }
    }

    JsonObject getTimeJSON () {
        JsonObject t = new JsonObject ();
        t.addProperty ("TIME", DF.format (new Date ()));
        return t;
    }

    @Override
    public void reportStatus (DASStatusT status, String message) {
//        try {
        JsonObject json = getTimeJSON ();
        JsonObject msg = new JsonObject ();
        msg.addProperty ("msg", message);
        msg.addProperty ("sim_time", "");
        json.addProperty ("STATUS", status.name ());
        json.add ("MESSAGE", msg);

//        json.addProperty ("MESSAGE", message);
        RequestBody body = RequestBody.create (JSON, m_gsonPP.toJson (json));
        Request request = new Request.Builder ().url (STATUS_SERVER + "/internal/status").post (body).build ();
        CLIENT.newCall (request).enqueue (m_responseCallback);

//            m_requestQ.offer (request);
//        }
//        catch (IOException e) {
//        }

    }

    @Override
    public void reportDone (boolean failed, String message) {
//        try {

        JsonObject json = getTimeJSON ();
        json.addProperty ("STATUS", failed ? DASStatusT.MISSION_ABORTED.name () : "MISSION_COMPLETED");
        json.addProperty ("MESSAGE", message);
        RequestBody body = RequestBody.create (JSON, m_gsonPP.toJson (json));
        Request request = new Request.Builder ().url (STATUS_SERVER + "/internal/status").post (body).build ();
        CLIENT.newCall (request).enqueue (m_responseCallback);
//            m_requestQ.offer (request);
//        }
//        catch (IOException e) {
//        }
    }

//    @Override
//    protected void runAction () {
//        Request r;
//        while ((r = m_requestQ.poll ()) != null) {
//            try {
//                CLIENT.newCall (r).execute ();
//            }
//            catch (IOException e) {
//                e.printStackTrace ();
//            }
//        }
//
//    }

//    @Override
//    public void dispose () {
//        m_requestQ.clear ();
//    }
//
//    @Override
//    protected void log (String txt) {
//        m_reportingPort.info (RainbowComponentT.MASTER, txt);
//    }
//
//    @Override
//    public RainbowComponentT getComponentType () {
//        return RainbowComponentT.MASTER;
//    };

    public static void main (String[] args) {
        BRASSHttpConnector conn = new BRASSHttpConnector ();
        JsonObject j = conn.getTimeJSON ();
        System.out.println (conn.m_gsonPP.toJson (j));
    }
}
