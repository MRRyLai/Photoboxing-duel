package com.sandipbhattacharya.registerlogindemo.loginsystem;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.sandipbhattacharya.registerlogindemo.mlkit.CameraXLivePreviewActivity;
import com.sandipbhattacharya.registerlogindemo.opencv.FaceDetect;
import com.sandipbhattacharya.registerlogindemo.R;

import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import xdroid.toaster.Toaster;

public class LoginFromTcp extends AppCompatActivity {
    private Thread thread;
    private Socket clientSocket;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String tmp,get_rev,put_send;         //做為接收時的緩存
    private String[] info;
    private Button btnlogin;
    private EditText etEmail, etPassword;
    private String message="",email, password;
    static public String Login_name = "",Login_email = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_from_tcp);

        Log.d("TAG","OPenCV Loading Status:"+ OpenCVLoader.initDebug());


        email = password = "";
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnlogin = findViewById(R.id.btn_login);
    }
//    https://xnfood.com.tw/socket/ TCP客戶端的寫法
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
                while (clientSocket.isConnected()) {

                    if(put_send!=null){
//                        Log.v("Client_Login","send:"+put_send);
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }
//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    get_rev = br.readLine();

                    if(get_rev!=null){
                        //將取到的String抓取{}範圍資料
//                        Log.v("Client_Login","revive:"+get_rev);
                        info = get_rev.split(", ");//info 依序從0到4為(回傳狀態、ID、姓名、信箱、密碼)
                        if (info[0].equals("success")) {
                            MainActivity.Main_email = email;
                            MainActivity.Main_name = info[2];
                            Toaster.toast("玩家: "+MainActivity.Main_name+" 登入成功");
                            Intent intent = new Intent(LoginFromTcp.this, CameraXLivePreviewActivity.class);
                            startActivity(intent);
                            finish();
                            bw.close();
                            br.close();
                            clientSocket.close();
                        }
                        else if(info[0].equals("failure")){
                            Toaster.toast("email或密碼輸入錯誤");
                        }
                        get_rev=null;
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

//    @Override
//    protected void onDestroy() {//登入功能正常，註冊功能沒辦法使用
//        super.onDestroy();
//        try {
//            //傳送離線 Action 給 Server 端
//            //寫入
////            bw.write("離線" + "\n");
////            //立即發送
////            bw.flush();
//
//            //關閉輸出入串流後,關閉Socket
////            bw.close();
////            br.close();
////            clientSocket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public void loginTcp(View view) {
        thread=new Thread(Connection);

        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if(!email.equals("") && !password.equals("") && email.contains("@")){
            MainActivity.ClientStage = "login";

            message = MainActivity.ClientStage + "\n" + email + "\n" + password;
            put_send = message;
            thread.start();
//        ServerCommunicationThread thread = new ServerCommunicationThread();
//        thread.start();
//        thread.send(message);
//        thread.getState();
//        Log.d("Login Send",message);
//        new SendMessage().execute(message);
//        Log.d("Login Get",msg);
        }else if(email.equals("") || password.equals("")){
            Toaster.toast("信箱或密碼不得為空");
        }else if(!email.contains("@")){
            Toaster.toast("信箱輸入錯誤");
        }
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterFromTcp.class);
        startActivity(intent);
        finish();
    }
}
