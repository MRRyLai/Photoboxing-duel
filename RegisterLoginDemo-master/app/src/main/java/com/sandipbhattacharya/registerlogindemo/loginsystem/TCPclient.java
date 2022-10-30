package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPclient {
    public static final String TAG = TCPclient.class.getSimpleName();
    public static final String SERVER_IP = "192.168.43.241"; //server IP address
    public static final int SERVER_PORT = 6666;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;


    public TCPclient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void sendMessage(final String message) {
        Log.d("In sendMessage()", "enter"+mBufferOut);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d("abc","Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Log.d("Out sendMessage()", "out");
    }
    public void stopClient() {
        Log.d("In stopClient()", "enter");
        mRun = false;

        if (mBufferOut == null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;

        Log.d("Out stopClient()", "out");
    }

    public void run() {
        Log.d("In run()", "enter");
        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            Log.d("TCP Client", "Create socket.");
            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//                Log.d("TCP Client", "PrintWriter.");
                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                Log.d("TCP Client", "BufferReader.");
                Log.d("TCP Client", "mBufferOut:"+mBufferOut+"mBufferIn:"+mBufferIn);
                //in this while the client listens for the messages sent by the server
                while (mRun) {
//                    Log.d("TCP Client", "In while...");
                    Log.d("TCP Client", "Waiting message...");
                    mServerMessage = mBufferIn.readLine();
                    Log.d("TCP Client", "Get message"+mServerMessage);
                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
        Log.d("Out run()", "out");
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);

    }
}
