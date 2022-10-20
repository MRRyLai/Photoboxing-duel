package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sandipbhattacharya.registerlogindemo.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Activity {
    private Thread thread;
    private Socket clientSocket;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String tmp,get_rev,put_send;         //做為接收時的緩存
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thread=new Thread(Connection);
        thread.start();
    }

    //連結socket伺服器做傳送與接收
    private Runnable Connection=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress serverIp = InetAddress.getByName("10.0.2.2");
                //自訂所使用的 Port(1024 ~ 65535)
                int serverPort = 5050;
                //建立連線
                clientSocket = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
                //取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //檢查是否已連線
                while (clientSocket.isConnected()) {
                    //宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    while(put_send!=null){
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }

                    tmp = br.readLine();

                    if(tmp!=null){
                        //將取到的String抓取{}範圍資料
                        get_rev = tmp;
                        tmp=null;
                        //從java伺服器取得值後做拆解,可使用switch做不同動作的處理
                    }
                }
            }catch(Exception e){
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //傳送離線 Action 給 Server 端
            //寫入
            bw.write("離線" + "\n");
            //立即發送
            bw.flush();

            //關閉輸出入串流後,關閉Socket
            bw.close();
            br.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
