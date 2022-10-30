package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerCommunicationThread extends Thread{
    public final static int TCP_SERVER_PORT = 6666;
    public final static String TCP_SERVER_IP = "192.168.0.5";
    private ArrayList<String> mMessages = new ArrayList<>();
    private String Messages;
    Socket s;

    private boolean mRun = true;

    public ServerCommunicationThread(){}

    @Override
    public void run() {

        while (mRun) {
//            Socket s = null;
            try {
                s = new Socket(TCP_SERVER_IP, TCP_SERVER_PORT);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                Log.i("TcpClient_Thread", "CreateSocket.");
                while (mRun) {
                    String message;

                    // Wait for message
                    synchronized (mMessages) {
                        while (mMessages.isEmpty()) {
                            try {
                                mMessages.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        // Get message and remove from the list
                        message = mMessages.get(0);
                        mMessages.remove(0);
                    }

                    //send output msg
                    String outMsg = message;
                    Log.i("TcpClient_Thread","Send:"+outMsg);
                    out.write(outMsg);
                    out.flush();
                    out.close();
                    Log.i("TcpClient_Thread", "Waiting...");
                    Messages = in.readLine();
                    Log.i("TcpClient_Thread", "opponent: " + Messages);
                    in.close();
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //close connection
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(String message) {
        Log.d("TcpClient_Thread", "Send: " + message);
        synchronized (mMessages) {
            mMessages.add(message);
            mMessages.notify();
        }
    }

    public void close() {
        mRun = false;
    }
}
