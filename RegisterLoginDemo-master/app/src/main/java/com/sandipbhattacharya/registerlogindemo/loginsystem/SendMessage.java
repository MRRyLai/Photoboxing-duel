package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendMessage extends AsyncTask<String , Void, String> {
    private Exception exception;
    private JSONObject jsonWrite, jsonRead;
    private Socket socket;
//                25.74.103.210 Ha網路
//                192.168.43.241 手機網路
//                192.168.0.10 家裡網路
    public static final String SERVER_IP = "192.168.0.5";
    public static final int SERVER_PORT = 6666;
    String outMsg,inMsg;
    PrintWriter outToServer;
    BufferedWriter bw;
    BufferedReader br;
    String line = null;

    protected String doInBackground(String... params){
        int i=0;
        try{
            try{
                socket = new Socket(SERVER_IP,SERVER_PORT);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));

                Log.i("Connect", "Socket Connected");
                //outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                outMsg = params[0];
                bw.write(outMsg);
                Log.i("TcpClient", "sent: " + outMsg);
                bw.flush();
//                Log.i("TcpClient", "receive: " + line);
                line=br.readLine();
//                LoginFromTcp.msg = line;
                Log.i("TcpClient", "opponent: " + line);
                br.close();
                bw.close();
//                socket.close();
                Log.i("TcpClient", "close connect");
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch (Exception e){
            this.exception = e;
            Log.e("text","Socket連線="+e.toString());
            return null;

        }
        return line;

    }

    protected void onPostExecute(String result){
        super.onPostExecute(result);

    }

}
