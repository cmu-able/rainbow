package org.sa.rainbow.raindroid;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketThread implements Runnable {


    private static final long SLEEP_TIME = 10000;
    private final RainbowConnectionManager m_manager;
    public PrintWriter m_pw;

    public SocketThread(RainbowConnectionManager manager) {
        m_manager = manager;
    }

    @Override
    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName(RainbowConnectionManager.SERVER_IP);
            Socket socket = new Socket(serverAddr, RainbowConnectionManager.SERVERPORT);
            m_pw = new PrintWriter (socket.getOutputStream(), true);
            synchronized (this) {
                this.notifyAll();
            }
            Thread.sleep(SLEEP_TIME);
            synchronized (this) {
                m_pw = null;
                socket.close();
            }
            m_manager.m_socketThread = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
