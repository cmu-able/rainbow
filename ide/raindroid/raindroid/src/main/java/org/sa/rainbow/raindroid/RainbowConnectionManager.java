package org.sa.rainbow.raindroid;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by schmerl on 12/14/2015.
 */
public class RainbowConnectionManager {

    public static final int SERVERPORT = 5011;
    public static final String SERVER_IP = "128.2.219.56";
    public  SocketThread m_socketThread;

    private static RainbowConnectionManager s_instance;


    protected RainbowConnectionManager () {}

    public static RainbowConnectionManager instance () {
        if (s_instance == null)
            s_instance = new RainbowConnectionManager();
        return s_instance;
    }



    public  void sendPackageInfo(String[] packages) {

        for (String p : packages) {
            getRainbowOutputStream("I[" + p + "]");
        }
    }


    public  void appInstalled (String p) {
        getRainbowOutputStream("I["+p+"]");
    }

    public  void appUninstalled (String p) {
        getRainbowOutputStream("U[" + p + "]");
    }

    public void appActivated (String p) {
        getRainbowOutputStream("A[" + p + "]");
    }

    public void appDeactivated (String p) {
        getRainbowOutputStream ("D[" + p + "]");
    }


    protected void getRainbowOutputStream(String w) {
        Log.d("RAINDORID", w);
//        return;
        if (m_socketThread != null) {
            synchronized (m_socketThread) {
                m_socketThread.m_pw.println (w);
            }
        } else {
            m_socketThread = new SocketThread(this);
            new Thread(m_socketThread).start();
            synchronized (m_socketThread) {
                try {
                    m_socketThread.wait(5000);
                    if (m_socketThread.m_pw == null) {
                        Log.d("RAINDROID", "Could not open socket");
                        m_socketThread = null;
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                m_socketThread.m_pw.println (w);
            }
        }

    }


}
