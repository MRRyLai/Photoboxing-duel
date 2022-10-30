package com.sandipbhattacharya.registerlogindemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Rank extends AppCompatActivity {
    private Thread thread2;
    private Socket clientSocket2;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String get_rev,put_send="rank"; //做為接收時的緩存
    private String[] RankName ={} , RankScore, RankUpdatetime, msg;
    ListView lvcheckorder ;
    ArrayList<RankItem> Ranklist = new ArrayList<RankItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        thread2=new Thread(Connection2);
        thread2.start();
        lvcheckorder = this.findViewById(R.id.rank_lv);

    }

    private Runnable Connection2=new Runnable(){
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
                    if(put_send!=null){
                        Log.v("Rank","send:"+put_send);
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }
//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    get_rev = br.readLine();
                    if(get_rev!=null){
                        //將取到的String抓取{}範圍資料
                        Log.v("Rank","revive:"+get_rev);
                        msg = get_rev.split(",");

                        RankName = msg[0].split(" ");
                        RankScore = msg[1].split(" ");
                        RankUpdatetime = msg[2].split(";");
                        Log.v("Rank","RankName:"+RankName[0]);
                        Log.v("Rank","RankScore:"+RankScore[0]);
                        Log.v("Rank","RankUpdatetime:"+RankUpdatetime[0]);
                        for (int i = 0; i < 10; i++) {
                            Log.v("Rank","Rank:"+RankName[i]+RankScore[i]+RankUpdatetime[i]);
                            Ranklist.add(new RankItem(RankName[i],RankScore[i],RankUpdatetime[i]));
                        }
                        RankArrayAdapter adapter = new RankArrayAdapter(Rank.this, R.layout.rank_list, Ranklist);
                        lvcheckorder.setAdapter(adapter);
                        get_rev=null;
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
    public void rank_click(View v){
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
        }
    }
}