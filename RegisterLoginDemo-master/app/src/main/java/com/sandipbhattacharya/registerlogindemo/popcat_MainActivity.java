package com.sandipbhattacharya.registerlogindemo;

import static com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity.ClientStage;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sandipbhattacharya.registerlogindemo.loginsystem.Login;
import com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity;
import com.sandipbhattacharya.registerlogindemo.loginsystem.Register;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class popcat_MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private int minute = 0;
    private int second = 15;
    private int popcat_opposite_score = 0,user_score=0;
    private Timer timer;
    private TimerTask timerTask;
    private String pop_name = "";
    private Thread thread2;
    private Socket clientSocket2;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String message,get_rev,put_send,popcat_opposite_name; //做為接收時的緩存
    private String[] info;
    private boolean timerstatus=false;

    TextView textView, counterView, counterNumberView,clock;
    ImageButton imageButton,imageButton2;
    Button button,button_reset,btnSend;
    MediaPlayer music ;
    int count=0;
    String To_srv_msg;
    boolean MainFlag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popcat_activity_main);
        init();
    }

    private void init() {
        textView=findViewById(R.id.textView);
        imageButton=findViewById(R.id.ImageButton);
        button=findViewById(R.id.button);
        imageButton2=findViewById(R.id.ImageButton2);
        button.setOnTouchListener(this);
        counterView=findViewById(R.id.counterView);
        counterNumberView=findViewById(R.id.counterNumberView);
        button_reset = findViewById(R.id.button_reset);
        button_reset.setOnTouchListener(this);
        clock = findViewById(R.id.tv_clock);
        btnSend = findViewById(R.id.btnSend);
        pop_name = Login.log_username;
        if(pop_name==""){
            pop_name = Register.reg_username;
        }
        thread2=new Thread(Connection1);
//        timer();

    }

    private  void timer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        if(timerTask != null){
            timerTask = null;
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(minute == 0){
                if(second == 0){
                    clock.setText("Time out!");
                    if(timer != null){
                        timer.cancel();
                        timer = null;
                    }
                    if(timerTask != null){
                        timerTask = null;
                    }
                } else{
                    second--;
                    if(second >= 10){
                        clock.setText("0" + minute + ":" + second);
                    } else{
                        clock.setText("0" + minute + ":0" + second);
                    }
                }
            } else{
                if(second == 0){
                    second = 59;
                    minute--;
                    if(minute >= 10){
                        clock.setText(minute + ":" + second);
                    } else{
                        clock.setText("0" + minute + ":" + second);
                    }
                } else{
                    second--;
                    if(second >= 10){
                        if(minute >= 10){
                            clock.setText(minute + ":" + second);
                        } else{
                            clock.setText("0" + minute + ":" + second);
                        }
                    } else{
                        if(minute >= 10){
                            clock.setText(minute + ":0" + second);
                        } else{
                            clock.setText("0" + minute + ":0" + second);
                        }
                    }
                }
            }
        }
    };


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()){
            case R.id.button:
                if(second==0&&minute==0){
                    button.setClickable(false);
                    Toast.makeText(this, "Please send the score.", Toast.LENGTH_SHORT).show();
                }
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.v("壓住","clicked");
                        imageButton.setVisibility(View.GONE);
                        imageButton2.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!timerstatus){
                            timerstatus=true;
                            timer();
                        }
                        Log.v("放開","clicked");
                        imageButton2.setVisibility(View.GONE);
                        imageButton.setVisibility(View.VISIBLE);
                        count++;
                        counterNumberView.setText(String.valueOf(count));
                        //new SendMessage().execute("From Android "+count);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v("壓住移動","clicked");
                        imageButton.setVisibility(View.GONE);
                        imageButton2.setVisibility(View.VISIBLE);
                        break;
                }
                break;
            case R.id.button_reset:
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.v("壓住","reseting");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.v("放開","reseted");
                        timerstatus=true;
                        count=0;
                        minute = 0;
                        second = 15;
                        clock.setText(minute + ":" + second);
                        timer();
                        counterNumberView.setText(String.valueOf(count));
                        //new SendMessage().execute("From Android "+count);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v("壓住移動","reseting");
                        break;
                }
                break;
        }
        return false;
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
                clientSocket2 = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                bw = new BufferedWriter( new OutputStreamWriter(clientSocket2.getOutputStream()));
                //取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));

                //檢查是否已連線
//                Log.v("Client_Register","Connect:"+clientSocket1.isConnected());
                while (clientSocket2.isConnected()){
//                    Log.v("popcat_tcp", "inthreadstatus:"+ thread2.getState());
                    if(put_send!=null){
                        Log.v("popcat_tcp","send:"+put_send);
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }

//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    get_rev = br.readLine();
                    info = get_rev.split(", ");
                    Log.v("popcat_tcp","GetAllmsg"+get_rev);
                    if(get_rev!=null){
                        //將取到的String抓取{}範圍資料
                        Log.v("popcat_tcp","revive:"+get_rev);
                        popcat_opposite_name = info[0];
                        popcat_opposite_score = Integer.parseInt(info[1]);
                        get_rev=null;

                        if(popcat_opposite_score>0){

                            Intent intent = new Intent(popcat_MainActivity.this, ShowScore.class);
                            intent.putExtra("OppositeName", popcat_opposite_name);
                            intent.putExtra("OppositeScore", popcat_opposite_score);
                            intent.putExtra("UserScore",user_score);
                            startActivity(intent);
                            finish();
                        }
                        //從java伺服器取得值後做拆解,可使用switch做不同動作的處理
                    }
                }

                bw.close();
                br.close();
                clientSocket2.close();

            }catch(Exception e){
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

    public void pop_onClick(View v) {

        switch (v.getId()) {
            case R.id.btnSend:
                if(second==0&&minute==0){

                    if(Login.log_username!=""){
                        pop_name = Login.log_username;
                    }else{
                        pop_name = Register.reg_username;
                    }
                    ClientStage = "score";
                    user_score = count;
                    message = ClientStage+"\n"+MainActivity.Main_email+"\n"+MainActivity.Main_name+"\n"+count;
                    put_send = message;
                    if(thread2.getState().toString().equals("NEW")){
                        Log.v("popcat_tcp", "threadstatus:"+ thread2.getState());
                        thread2.start();
                    }else if(thread2.getState().toString().equals("RUNNABLE")){
                        Log.v("popcat_tcp", "threadstatus:"+ thread2.getState());
                        thread2 = Thread.currentThread();
                    }



//                    new SendMessage().execute(pop_name + count);
//                    Log.v("傳送",pop_name + count);
//                    btnSend.setClickable(false);
                }
                else{
                    Toast.makeText(this, "Please wait for the time to end.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}