package com.sandipbhattacharya.registerlogindemo.mlkit;

import static com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity.ClientStage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sandipbhattacharya.registerlogindemo.R;
import com.sandipbhattacharya.registerlogindemo.ShowScore;
import com.sandipbhattacharya.registerlogindemo.loginsystem.Login;
import com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity;
import com.sandipbhattacharya.registerlogindemo.loginsystem.Register;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnectandWait extends AppCompatActivity {

    private Thread thread;
    private Socket clientSocket;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String message,get_rev,put_send,ClientConnectandWiait_gameready,ClientConnectandWiait_name="";
    private String[] info;
    private int ClientConnectandWiait_opposite_score = 0,user_score=0, opencount=0;

    String[] mode = {"mode1","mode2"};
    String gamemode="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_connectand_wait);

        Intent intent = getIntent();
        gamemode = intent.getStringExtra("Gamemode");

        thread = new Thread(Connection);
        GetServerCommunication();
    }

    private Runnable Connection=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress serverIp = InetAddress.getByName(MainActivity.TCP_SERVER_IP);
                //自訂所使用的 Port(1024 ~ 65535)
                int serverPort = MainActivity.TCP_SERVER_PORT;
                //建立連線
                clientSocket = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
                //取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //檢查是否已連線
//                Log.v("Client_Register","Connect:"+clientSocket.isConnected());
                while (clientSocket.isConnected()){
//                    Log.v("ClientConnect&Wait_tcp", "inthreadstatus:"+ thread.getState());
                    if(put_send!=null){
                        Log.v("ClientConnect&Wait_tcp","send:"+put_send);
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }

//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    get_rev = br.readLine();
                    info = get_rev.split(", ");//info[0]:mode(遊戲模式)
                    Log.v("ClientConnect&Wait_tcp","GetAllmsg"+get_rev);
                    if(get_rev!=null){
                        //將取到的String抓取{}範圍資料
                        Log.v("ClientConnect&Wait_tcp","revive:"+get_rev);
                        ClientConnectandWiait_gameready = info[0];

                        get_rev=null;

                        if(ClientConnectandWiait_gameready.equals("mode1")){
                            Intent intent = new Intent(ClientConnectandWait.this, CameraXLivePreviewActivity.class);//記得改成拍照模式
                            startActivity(intent);
                            finish();
                        }else if(ClientConnectandWiait_gameready.equals("mode2")){
                            Intent intent = new Intent(ClientConnectandWait.this, CameraXLivePreviewActivity.class);//進入即時偵測模式
                            startActivity(intent);
                            finish();
                        }
                        //從java伺服器取得值後做拆解,可使用switch做不同動作的處理
                    }
                }

                bw.close();
                br.close();
                clientSocket.close();

            }catch(Exception e){
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

    private void GetServerCommunication(){
        if(!Login.log_username.equals("")){
            ClientConnectandWiait_name = Login.log_username;
        }else{
            ClientConnectandWiait_name = Register.reg_username;
        }
        ClientStage = "playerwaiting";
        message = ClientStage+"\n"+ MainActivity.Main_name+"\n"+MainActivity.Main_email+"\n"+gamemode+"\n";
        put_send = message;
        if(thread.getState().toString().equals("NEW")){
            Log.v("ClientConnect&Wait_tcp", "threadstatus:"+ thread.getState());
            thread.start();
        }else if(thread.getState().toString().equals("RUNNABLE")){
            Log.v("ClientConnect&Wait_tcp", "threadstatus:"+ thread.getState());
            thread = Thread.currentThread();
        }
    }
}