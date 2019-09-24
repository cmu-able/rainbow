package org.sa.rainbow.brass.das;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class BRASSHttpConnector /*extends AbstractRainbowRunnable*/ implements IBRASSConnector {

    private static final String STATUS_ENDPOINT = "/internal-status";
	public static final MediaType     JSON               = MediaType.parse ("application/json");
    public static final OkHttpClient  CLIENT             = new OkHttpClient ();
    public static final String        STATUS_SERVER      = "http://localhost:5000";
	public static final String LEARN_ENDPOINT = "/internal-status";
    private static BRASSHttpConnector s_instance;
    public Queue<Request>             m_requestQ         = new SynchronousQueue<> ();
    private Callback                  m_responseCallback = new Callback () {

        @Override
        public void onResponse (Call call, Response response)
                throws IOException {
        	System.out.println("Received response from shim: " + response.body().string());
            response.close ();

        }

        @Override
        public void onFailure (Call call, IOException e) {
        	try {
	        	Request rb = call.request();
	            Request copy = rb.newBuilder().build();
	            Buffer buffer = new Buffer();
	            copy.body().writeTo(buffer);
	            System.out.println("Failed to connect to shim with request" + buffer.readUtf8());
        	} catch (IOException exc) {
        		
        	}
        }
    };
    private Gson                      m_gsonPP;
	private Phases m_phase;
	private ClockedModel m_clock;

    protected BRASSHttpConnector (Phases phase) {
        m_phase = phase;
		// TODO Auto-generated constructor stub
//        super ("BRASSConnector");
//        setSleepTime (1000);
        m_gsonPP = new GsonBuilder ().setPrettyPrinting ().create ();
    }

    public static BRASSHttpConnector instance (Phases phase) {
        if (s_instance == null) {
            s_instance = new BRASSHttpConnector (phase);
//            s_instance.start ();
        }
        return s_instance;
    }
    
    
    public void setClock(ClockedModel clock) {
		m_clock = clock;
    }
    
    String getRainbowReady() {
    	return m_phase == Phases.Phase1?DASPhase1StatusT.RAINBOW_READY.name():DASPhase2StatusT.RAINBOW_READY.name();
    }

    @Override
    public void reportReady (boolean ready) {
//        try {
        JsonObject json = getTimeJSON ();
        addFieldsToStatus (getRainbowReady(), "Rainbow is receiving information from Robot", json);
        String jsonStr = m_gsonPP.toJson (json);
        System.out.println ("Reporting ready: " + jsonStr);
        RequestBody body = RequestBody.create (JSON, jsonStr);
        Request request = new Request.Builder ().url (STATUS_SERVER + STATUS_ENDPOINT).post (body).build ();
        CLIENT.newCall (request).enqueue (m_responseCallback);

//            m_requestQ.offer (request);
//        }
//        catch (IOException e) {
//        }
    }

    JsonObject getTimeJSON () {
        JsonObject t = new JsonObject ();
        return t;
    }

    @Override
    public void reportStatus (String status, String message) {
        try {
        JsonObject json = getTimeJSON ();
        addFieldsToStatus (status, message, json);

//        json.addProperty ("MESSAGE", message);
        RequestBody body = RequestBody.create (JSON, m_gsonPP.toJson (json));
        Request request = new Request.Builder ().url (STATUS_SERVER + STATUS_ENDPOINT).post (body).build ();
        CLIENT.newCall (request).enqueue (m_responseCallback);

//            m_requestQ.offer (request);
        }
        catch (Exception e) {
        	System.out.print(message);
        }

    }

    void addFieldsToStatus (String status, String message, JsonObject json) {
//        JsonObject msg = new JsonObject ();
        json.addProperty ("message", message);
        json.addProperty ("sim-time", m_clock==null?-1:Math.round(m_clock.clockTime()));
        json.addProperty ("status", status);
//        json.add ("message", msg);
    }
    
	public void reportNewPlan(ArrayList<String> planToTA) {
		JsonObject json = getTimeJSON();
		StringBuffer planJson = new StringBuffer();
		planJson.append("[");
		boolean first = true;
		for (String l : planToTA) {
			if (!first) 
				planJson.append(", ");
			first = false;
			planJson.append("'");
			planJson.append(l);
			planJson.append("'");
		}
		planJson.append("]");
		reportStatus("PLAN", planJson.toString());
//		addFieldsToStatus("PLAN", planJson.toString(), json);
//	    RequestBody body = RequestBody.create (JSON, m_gsonPP.toJson (json));
//        Request request = new Request.Builder ().url (STATUS_SERVER + STATUS_ENDPOINT).post (body).build ();
//        CLIENT.newCall (request).enqueue (m_responseCallback);
	}

    @Override
    public void reportDone (boolean failed, String message) {
//        try {

        JsonObject json = getTimeJSON ();
        addFieldsToStatus (failed ? missionFailed() : missionSucceeded(), message, json);
        RequestBody body = RequestBody.create (JSON, m_gsonPP.toJson (json));
        Request request = new Request.Builder ().url (STATUS_SERVER + STATUS_ENDPOINT).post (body).build ();
        CLIENT.newCall (request).enqueue (m_responseCallback);
//            m_requestQ.offer (request);
//        }
//        catch (IOException e) {
//        }
    }
    
    boolean m_onlineLearning = false;
    
    public boolean requestOnlineLearning() {
    	final Object lock = new Object();
    	JsonObject json = getTimeJSON();
    	addFieldsToStatus("learning-requested", "requesting online learning", json);
    	RequestBody body = RequestBody.create(JSON, m_gsonPP.toJson(json));
    	Request request = new Request.Builder().url(STATUS_SERVER + LEARN_ENDPOINT).post(body).build();
    	
    	
    	CLIENT.newCall(request).enqueue(new Callback() {
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
	        	System.out.println("Received response from shim: " + response.body().string());
				boolean wait = false;
				try {
					JsonObject j = new JsonParser().parse(response.body().string()).getAsJsonObject();
					wait = j.get("success").getAsBoolean();
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				}
				m_onlineLearning = wait;
				synchronized (lock) {
					lock.notifyAll();
				} 
				
			}
			
			@Override
			public void onFailure(Call call, IOException e) {
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		});
    	
    	// Wait for result
    	synchronized (lock) {
    		try {
				lock.wait();
			} catch (InterruptedException e1) {
			}
    	}
    	return m_onlineLearning;
    }

	private String missionSucceeded() {
		return m_phase==Phases.Phase1?DASPhase1StatusT.MISSION_COMPLETED.name():DASPhase2StatusT.MISSION_SUCCEEDED.name();
	}

	private String missionFailed() {
		return m_phase==Phases.Phase1?DASPhase1StatusT.MISSION_ABORTED.name():DASPhase2StatusT.MISSION_FAILED.name();
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
        BRASSHttpConnector conn = new BRASSHttpConnector (Phases.Phase2);
        JsonObject j = conn.getTimeJSON ();
        System.out.println (conn.m_gsonPP.toJson (j));
    }


}
