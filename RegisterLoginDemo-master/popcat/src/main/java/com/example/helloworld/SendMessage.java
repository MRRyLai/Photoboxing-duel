package com.example.helloworld;

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

public class SendMessage extends AsyncTask<String , Void, Void> {
    private Exception exception;
    private JSONObject jsonWrite, jsonRead;
    private Socket socket;
    String data;
    PrintWriter outToServer;
    BufferedWriter bw;
    BufferedReader br;
    protected Void doInBackground(String... params){
        try{
            try{
                //25.74.103.210

                socket = new Socket("192.168.43.241",6666);
//                bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
//                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (socket.isConnected()){
                    Log.i("Connect", "Socket Connected");
                    outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    outToServer.flush();
                    //data = br.readLine();
//目前我想做出可以雙向溝通的的client server，所以正在嘗試寫出client等待server的回應readline，而server目前只會收取資料不傳送資料

                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch (Exception e){
            this.exception = e;
            Log.e("text","Socket連線="+e.toString());
            return null;

        }
//        try {
//            //傳送離線 Action 給 Server 端
//            jsonWrite = new JSONObject();
//            jsonWrite.put("action","離線");
//
//            //寫入
//            bw.write(jsonWrite + "\n");
//            //立即發送
//            bw.flush();
//
//            //關閉輸出入串流後,關閉Socket
//            bw.close();
//            br.close();
//            socket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;

    }

}
