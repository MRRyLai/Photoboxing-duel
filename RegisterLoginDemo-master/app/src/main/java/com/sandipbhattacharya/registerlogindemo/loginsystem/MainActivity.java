package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sandipbhattacharya.registerlogindemo.mlkit.CameraXLivePreviewActivity;
import com.sandipbhattacharya.registerlogindemo.opencv.FaceDetect;
import com.sandipbhattacharya.registerlogindemo.R;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends Activity {

    static public String ClientStage = "",Main_name = "", Main_email = "";
    public final static int TCP_SERVER_PORT = 6666;
    public final static String TCP_SERVER_IP = "192.168.0.10";
//    26.72.187.29 VPN
//    192.168.0.10 家用
//    192.168.43.241 手機

    static{
        if(OpenCVLoader.initDebug()){
            Log.d("TAG","Opencv is loaded.");
        }else{
            Log.d("TAG", "Opencv failed to load.");
        }
    }
//    static public TCPclient mTcpClient;
//    static public TCPclient.OnMessageReceived mMessageListener = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!Main_name.equals("")&&!Main_email.equals("")){
            Intent intent = new Intent(this, CameraXLivePreviewActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this,LoginFromTcp.class);
            startActivity(intent);
            finish();
        }


    }

}
