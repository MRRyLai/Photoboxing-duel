package com.sandipbhattacharya.registerlogindemo.loginsystem;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sandipbhattacharya.registerlogindemo.mlkit.CameraXLivePreviewActivity;
import com.sandipbhattacharya.registerlogindemo.mlkit.GameModeSelect;
import com.sandipbhattacharya.registerlogindemo.opencv.FaceDetect;
import com.sandipbhattacharya.registerlogindemo.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import xdroid.toaster.Toaster;

public class RegisterFromTcp extends AppCompatActivity {
    private Thread thread1;
    private EditText etName, etEmail, etPassword, etReenterPassword;
    private TextView tvStatus;
    private Button btnRegister;
    private String name, email, password, reenterPassword, message;
    private Socket clientSocket1;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String tmp,get_rev,put_send;         //做為接收時的緩存
    static public String Reg_name="",Reg_email="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_from_tcp);



        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword = findViewById(R.id.etReenterPassword);
        tvStatus = findViewById(R.id.tvStatus);
        btnRegister = findViewById(R.id.btnRegister);
        name = email = password = reenterPassword = "";
    }

    private Runnable Connection1=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress serverIp = InetAddress.getByName(MainActivity.TCP_SERVER_IP);
                //自訂所使用的 Port(1024 ~ 65535)
                int serverPort = MainActivity.TCP_SERVER_PORT;
                //建立連線
                clientSocket1 = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                bw = new BufferedWriter( new OutputStreamWriter(clientSocket1.getOutputStream()));
                //取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));

                //檢查是否已連線
//                Log.v("Client_Register","Connect:"+clientSocket1.isConnected());
                while (clientSocket1.isConnected()){
                    if(put_send!=null){
                        Log.v("Client_Register","send:"+put_send);
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }
//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    get_rev = br.readLine();
                    if(get_rev!=null){
                        //將取到的String抓取{}範圍資料
                        Log.v("Client_Register","revive:"+get_rev);
                        if(get_rev.equals("success")){
                            MainActivity.Main_name = name;
                            MainActivity.Main_email = email;
                            Toaster.toast("註冊成功");
                            Intent intent = new Intent(RegisterFromTcp.this, GameModeSelect.class);
                            startActivity(intent);
                            finish();
                            bw.close();
                            br.close();
                            clientSocket1.close();
                        }
                        else if(get_rev.equals("duplicate e-mail")){
                            Toaster.toast("此信箱已註冊");
                            break;
                        }
                        else{
                            Toaster.toast("未知錯誤");
                            break;
                        }
                        get_rev=null;
                        //從java伺服器取得值後做拆解,可使用switch做不同動作的處理
                    }
                }

                bw.close();
                br.close();
                clientSocket1.close();

            }catch(Exception e){
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

//    @Override
//    protected void onPause() {
//        super.onPause();
//        try {
//            //傳送離線 Action 給 Server 端
//            //寫入
////            bw.write("離線" + "\n");
////            立即發送
////            bw.flush();
//
//            //關閉輸出入串流後,關閉Socket
//            bw.close();
//            br.close();
//            clientSocket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void saveTcp(View view){
        thread1=new Thread(Connection1);

        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        reenterPassword = etReenterPassword.getText().toString().trim();
        if(!password.equals(reenterPassword)){
            Toaster.toast("密碼不一致");
        }
        else if(!name.equals("") && !email.equals("") && !password.equals("") && email.contains("@")){
            MainActivity.ClientStage = "register";
            message = MainActivity.ClientStage+"\n"+name+"\n"+email+"\n"+password;
            put_send = message;
            thread1.start();
        }else if(email.equals("") || password.equals("")){
            Toaster.toast("名稱、信箱或密碼不得為空");
        }else if(!email.contains("@")){
            Toaster.toast("信箱輸入錯誤");
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginFromTcp.class);
        startActivity(intent);
        finish();
    }
}