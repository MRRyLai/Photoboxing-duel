package com.sandipbhattacharya.registerlogindemo.opencv;

import static android.content.ContentValues.TAG;

import static com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity.ClientStage;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sandipbhattacharya.registerlogindemo.loginsystem.Login;
import com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity;
import com.sandipbhattacharya.registerlogindemo.loginsystem.Register;
import com.sandipbhattacharya.registerlogindemo.R;
import com.sandipbhattacharya.registerlogindemo.ShowScore;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FaceDetect extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private int FaceDetect_opposite_score = 0,user_score=0,count=0, opencount=0;
    private Thread thread;
    private Socket clientSocket3;//客戶端的socket
    private BufferedWriter bw;  //取得網路輸出串流
    private BufferedReader br;  //取得網路輸入串流
    private String message,get_rev,put_send,FaceDetect_opposite_name,FaceDetect_name=""; //做為接收時的緩存
    private String[] info;

    private int minute = 0;
    private int second = 10;
    private boolean timerstatus = false;
    private boolean IsCreateDir = false;
    private boolean inloading = false;
    private TimerTask timerTask;
    private Timer timer;

    private CameraBridgeViewBase mOpenCvCameraView;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;

    File cascFile;
    CascadeClassifier faceDetector;

    private Mat mRgba, mGray;
    private long img_number=0, gamenum = 0;
    TextView clock,face_score;
    String filename = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javacameraview);
        clock = findViewById(R.id.fd_tv_clock);
        face_score = findViewById(R.id.tv_FaceScore);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera(mOpenCvCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission_prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseCallback);
        } else {
            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        timer();
        thread = new Thread(Connection);
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
                clientSocket3 = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                bw = new BufferedWriter( new OutputStreamWriter(clientSocket3.getOutputStream()));
                //取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket3.getInputStream()));

                //檢查是否已連線
//                Log.v("Client_Register","Connect:"+clientSocket1.isConnected());
                while (clientSocket3.isConnected()){
//                    Log.v("FaceDetect_tcp", "inthreadstatus:"+ thread2.getState());
                    if(put_send!=null){
                        Log.v("FaceDetect_tcp","send:"+put_send);
                        bw.write(put_send);
                        bw.flush();
                        put_send = null;
                    }

//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    get_rev = br.readLine();
                    info = get_rev.split(", ");
                    Log.v("FaceDetect_tcp","GetAllmsg"+get_rev);
                    if(get_rev!=null){
                        //將取到的String抓取{}範圍資料
                        Log.v("FaceDetect_tcp","revive:"+get_rev);
                        FaceDetect_opposite_name = info[0];
                        FaceDetect_opposite_score = Integer.parseInt(info[1]);
                        get_rev=null;

                        if(FaceDetect_opposite_score>0){

                            Intent intent = new Intent(FaceDetect.this, ShowScore.class);
                            intent.putExtra("OppositeName", FaceDetect_opposite_name);
                            intent.putExtra("OppositeScore", FaceDetect_opposite_score);
                            intent.putExtra("UserScore",user_score);
                            startActivity(intent);
                            finish();
                        }
                        //從java伺服器取得值後做拆解,可使用switch做不同動作的處理
                    }
                }

                bw.close();
                br.close();
                clientSocket3.close();

            }catch(Exception e){
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

    private void GetServerCommunication(){
        if(Login.log_username!=""){
            FaceDetect_name = Login.log_username;
        }else{
            FaceDetect_name = Register.reg_username;
        }
        ClientStage = "score";
        user_score = count;
        message = ClientStage+"\n"+MainActivity.Main_email+"\n"+MainActivity.Main_name+"\n"+count;
        put_send = message;
        if(thread.getState().toString().equals("NEW")){
            Log.v("popcat_tcp", "threadstatus:"+ thread.getState());
            thread.start();
        }else if(thread.getState().toString().equals("RUNNABLE")){
            Log.v("popcat_tcp", "threadstatus:"+ thread.getState());
            thread = Thread.currentThread();
        }
    }


    private void timer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
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

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (minute == 0) {
                if (second == 0) {
                    clock.setText("Time out!");
                    GetServerCommunication();
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    if (timerTask != null) {
                        timerTask = null;
                    }
                } else {
                    second--;
                    if (second >= 10) {
                        clock.setText("0" + minute + ":" + second);
                    } else {
                        clock.setText("0" + minute + ":0" + second);
                    }
                }
            } else {
                if (second == 0) {
                    second = 59;
                    minute--;
                    if (minute >= 10) {
                        clock.setText(minute + ":" + second);
                    } else {
                        clock.setText("0" + minute + ":" + second);
                    }
                } else {
                    second--;
                    if (second >= 10) {
                        if (minute >= 10) {
                            clock.setText(minute + ":" + second);
                        } else {
                            clock.setText("0" + minute + ":" + second);
                        }
                    } else {
                        if (minute >= 10) {
                            clock.setText(minute + ":0" + second);
                        } else {
                            clock.setText("0" + minute + ":0" + second);
                        }
                    }
                }
            }
        }
    };

    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGray = new Mat();
    }

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    /**
     * This method is invoked when delivery of the frame needs to be done.
     * The returned values - is a modified frame which needs to be displayed on the screen.
     * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
     *
     * @param inputFrame
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(minute==0&&second==0){

            if(!inloading){
                opencount++;
                inloading=true;
                Intent intent = new Intent(FaceDetect.this,FaceDetectToScore_Loading.class);
                Log.d("Loading","Send count:"+opencount);
                intent.putExtra("opencount:",opencount);
                startActivity(intent);
            }
        }else{
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();
//        Log.d("TAG", "RGB_image:" + mRgba);

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(mRgba, faceDetections,1.2,3);//調整人臉辨識的準確度 scaleFactor:比例因數 minNeighbors:辨識門檻

            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0));

                Log.d("TAG", "FaceDetected. score:"+count);
                count++;//偵測到臉得1分
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        face_score.setText(String.valueOf(count));
                    }
                });
//                Toaster.toast("+1");
                String img_number_str = "_"+img_number;
                filename = System.currentTimeMillis()+img_number_str;

                Mat srcGray = new Mat ();
                Imgproc.cvtColor(mRgba, srcGray,Imgproc.COLOR_BGR2BGRA);
                saveImage(MatToBitmap(srcGray));
//                storeImage(filename, srcGray);
                img_number++;
            }

        }

        return mRgba;
    }

    private BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascFile = new File(cascadeDir, "lbpcascade_frontalface.xml");

                    FileOutputStream fos = new FileOutputStream(cascFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.close();
                    mOpenCvCameraView.enableView();
                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());

                    if (faceDetector.empty()) {
                        faceDetector = null;
                    } else {
                        cascadeDir.delete();
                    }
                    break;

                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeCamera(CameraBridgeViewBase mOpenCvCameraView, int activeCamera) {

        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setCameraIndex(activeCamera);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    private void saveImage(Bitmap bitmap){//目前進度:API>29可以創造一個資料夾儲存照片，但無法創造兩個資料夾，API<29 可以創造兩個資料夾儲存照片
        if(Build.VERSION.SDK_INT>=29){// getExternalStorageDirectory is deprecated in API 29
            ContentValues values = new ContentValues();
            String gameplay = "GAME_" + gamenum;
            File dir = new File(this.getExternalFilesDir(null).getAbsolutePath(), "PictureBoxing");///storage/emulated/0/Android/data/com.sandipbhattacharya.registerlogindemo/files
            Log.d("TAG_SaveImage","auto_get_dir:"+dir);

            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PictureBoxing");
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Log.d("TAG_SaveImage","Dir:"+MediaStore.Images.Media.RELATIVE_PATH);
            Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    if (this.getContentResolver().openOutputStream(uri) != null) {
                        try {
                            Log.d("TAG_SaveImage", "Image saved.");
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this.getContentResolver().openOutputStream(uri));
                            this.getContentResolver().openOutputStream(uri).close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    this.getContentResolver().update(uri, values, null, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

        }else{
            String Dir = Environment.getExternalStorageDirectory().toString()+"/PictureBoxing";
            File myDir = new File(Dir);
            if(myDir.exists() && myDir.isDirectory()){
                while (true) {

                    String gameplay = "GAME_" + gamenum;
                    Log.d("TAG_SaveImage", "While stage.");
                    File imgFile1 = new File(Dir + "/" + gameplay);
                    Log.d("TAG_SaveImage", "Now dir:" + Dir + "/" + gameplay);

                    if (imgFile1.exists() && imgFile1.isDirectory()) {
                        if (IsCreateDir == true) {
                            Log.d("TAG_SaveImage", "Store img in dir:" + Dir + "/" + gameplay + "/" + filename + ".jpg");
                            try {
                                FileOutputStream out = new FileOutputStream(Dir + "/" + gameplay + "/" + filename + ".jpg");
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        } else {
                            Log.d("TAG_SaveImage", "dir:" + gameplay + " is already exist.");
                            gamenum++;
                        }

                    } else {
                        imgFile1.mkdir();
                        IsCreateDir = true;
                        Log.d("TAG_SaveImage", "Store img in dir:" + Dir + "/" + gameplay + "/" + filename + ".jpg");
                        try {
                            FileOutputStream out = new FileOutputStream(Dir + "/" + gameplay + "/" + filename + ".jpg");
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }else{
                myDir.mkdirs();
                Log.d("TAG_SaveImage","Create directory:"+myDir);
            }
        }
    }

    private void deleteImage(Bitmap bitmap){
        if(Build.VERSION.SDK_INT>=29){

        }else{

        }
    }


    private void storeImage(String filename, Mat mat){//Only for <API 29 type Matrix image.

        String Dir = Environment.getExternalStorageDirectory().getPath();
        File imgFile = new File(Dir + "/PictureBoxing");
        Log.d("TAG_Store", "Now dir:" + Dir + "/PictureBoxing");
        if (imgFile.exists() && imgFile.isDirectory()) {

            Dir = Dir + "/PictureBoxing/";
            Log.d("TAG_Store", "dir:PictureBoxing is exist.");


            while (true) {

                String gameplay = "GAME_" + gamenum;
                Log.d("TAG_Store", "While stage.");
                File imgFile1 = new File(Dir + gameplay);
                Log.d("TAG_Store", "Now dir:" + Dir + gameplay);

                if (imgFile1.exists() && imgFile1.isDirectory()) {
                    if (IsCreateDir == true) {
                        Log.d("TAG_Store", "Store img in dir:" + Dir + gameplay + "/" + filename + ".jpg");
                        Imgcodecs.imwrite(Dir + gameplay + "/" + filename + ".jpg", mat);
                        break;
                    } else {
                        Log.d("TAG_Store", "dir:" + gameplay + " is already exist.");
                        gamenum++;
                    }

                } else {
                    imgFile1.mkdir();
                    IsCreateDir = true;
                    Log.d("TAG_Store", "Store img in dir:" + Dir + gameplay + "/" + filename + ".jpg");
                    Imgcodecs.imwrite(Dir + gameplay + "/" + filename + ".jpg", mat);
                    break;
                }
            }
        } else {
            Log.d("TAG_Store", "dir:PictureBoxing is not exist or is not a directory.");
            imgFile.mkdir();
            Log.d("TAG_Store", "dir:PictureBoxing create.");
        }

    }

    private Bitmap MatToBitmap(Mat mat){
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return  bitmap;
    }
}

