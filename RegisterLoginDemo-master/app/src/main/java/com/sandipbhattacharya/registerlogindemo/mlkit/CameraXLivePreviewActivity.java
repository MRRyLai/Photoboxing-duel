/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sandipbhattacharya.registerlogindemo.mlkit;

import static com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity.ClientStage;
import static com.sandipbhattacharya.registerlogindemo.mlkit.FaceDetectorProcessor.gamescore;

import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.demo.CameraXViewModel;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.VisionImageProcessor;
//import com.google.mlkit.vision.demo.java.facedetector.FaceDetectorProcessor;
import com.google.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.demo.preference.SettingsActivity;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.sandipbhattacharya.registerlogindemo.ShowScore;
import com.sandipbhattacharya.registerlogindemo.loginsystem.Login;
import com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity;
import com.sandipbhattacharya.registerlogindemo.loginsystem.Register;
import com.sandipbhattacharya.registerlogindemo.opencv.FaceDetect;
import com.sandipbhattacharya.registerlogindemo.opencv.FaceDetectToScore_Loading;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Live preview demo app for ML Kit APIs using CameraX. */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXLivePreviewActivity extends AppCompatActivity
        implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

  private Thread thread;
  private Socket clientSocket;//客戶端的socket
  private BufferedWriter bw;  //取得網路輸出串流
  private BufferedReader br;  //取得網路輸入串流
  private String message,get_rev,put_send,FaceDetector_opposite_name,FaceDetector_name="";
  private String[] info;
  private int FaceDetector_opposite_score = 0,user_score=0, opencount=0;

  public static int count = 0;
  public static int minutes = 0;
  public static int seconds = 30;
  static boolean inloading=false;

  TextView face_score,roundclock;

  private static TimerTask timerTask;
  private static Timer timer;

  private static final String TAG = "CameraXLivePreview";

  private static final String FACE_DETECTION = "Face Detection";
//  private static final String POSE_DETECTION = "Pose Detection";

  private static final String STATE_SELECTED_MODEL = "selected_model";

  private PreviewView previewView;
  private GraphicOverlay graphicOverlay;

  @Nullable private ProcessCameraProvider cameraProvider;
  @Nullable private Preview previewUseCase;
  @Nullable private ImageAnalysis analysisUseCase;
  @Nullable private VisionImageProcessor imageProcessor;
  private boolean needUpdateGraphicOverlayImageSourceInfo;

  private String selectedModel = FACE_DETECTION;
  private int lensFacing = CameraSelector.LENS_FACING_BACK;
  private CameraSelector cameraSelector;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    minutes = 0;
    seconds = 30;
    count=0;
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    if (savedInstanceState != null) {
      selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, FACE_DETECTION);
    }
    cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
    setContentView(R.layout.activity_vision_camerax_live_preview);
    previewView = findViewById(R.id.preview_view);
    roundclock = findViewById(R.id.gametime);
    gamescore = findViewById(R.id.tv_facescore);

    if (previewView == null) {
      Log.d(TAG, "previewView is null");
    }
    FaceDetectorProcessor.clock = findViewById(R.id.tv_getdetectinterval);
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    Spinner spinner = findViewById(R.id.spinner);
    List<String> options = new ArrayList<>();
    options.add(FACE_DETECTION);
//    options.add(POSE_DETECTION);


    // Creating adapter for spinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    spinner.setAdapter(dataAdapter);
    spinner.setOnItemSelectedListener(this);

    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
    facingSwitch.setOnCheckedChangeListener(this);

    new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
            .get(CameraXViewModel.class)
            .getProcessCameraProvider()
            .observe(
                    this,
                    provider -> {
                      cameraProvider = provider;
                      bindAllCameraUseCases();
                    });

    ImageView settingsButton = findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(
            v -> {
              Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
              intent.putExtra(
                      SettingsActivity.EXTRA_LAUNCH_SOURCE,
                      SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW);
              startActivity(intent);
            });

    thread = new Thread(Connection);
    timer();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putString(STATE_SELECTED_MODEL, selectedModel);
  }

  @Override
  public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selectedModel = parent.getItemAtPosition(pos).toString();
    Log.d(TAG, "Selected model: " + selectedModel);
    bindAnalysisUseCase();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (cameraProvider == null) {
      return;
    }
    int newLensFacing =
            lensFacing == CameraSelector.LENS_FACING_FRONT
                    ? CameraSelector.LENS_FACING_BACK
                    : CameraSelector.LENS_FACING_FRONT;
    CameraSelector newCameraSelector =
            new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
    try {
      if (cameraProvider.hasCamera(newCameraSelector)) {
        Log.d(TAG, "Set facing to " + newLensFacing);
        lensFacing = newLensFacing;
        cameraSelector = newCameraSelector;
        bindAllCameraUseCases();
        return;
      }
    } catch (CameraInfoUnavailableException e) {
      // Falls through
    }
    Toast.makeText(
            getApplicationContext(),
            "This device does not have lens with facing: " + newLensFacing,
            Toast.LENGTH_SHORT)
            .show();
  }
//
//  @Override
//  protected void onStart() {
//    super.onStart();
//
//  }

  @Override
  public void onResume() {
    super.onResume();
    bindAllCameraUseCases();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (imageProcessor != null) {
      imageProcessor.stop();
    }

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (imageProcessor != null) {
      imageProcessor.stop();
    }
  }

  private void bindAllCameraUseCases() {
    if (cameraProvider != null) {
      // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
      cameraProvider.unbindAll();
      bindPreviewUseCase();
      bindAnalysisUseCase();
    }
  }

  private void bindPreviewUseCase() {
    if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
      return;
    }
    if (cameraProvider == null) {
      return;
    }
    if (previewUseCase != null) {
      cameraProvider.unbind(previewUseCase);
    }

    Preview.Builder builder = new Preview.Builder();
    Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
    if (targetResolution != null) {
      builder.setTargetResolution(targetResolution);
    }
    previewUseCase = builder.build();
    previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
  }

  private void bindAnalysisUseCase() {

    if (cameraProvider == null) {
      return;
    }
    if (analysisUseCase != null) {
      cameraProvider.unbind(analysisUseCase);
    }
    if (imageProcessor != null) {
      imageProcessor.stop();
    }

    try {
      switch (selectedModel) {
        case FACE_DETECTION:
          Log.i(TAG, "Using Face Detector Processor");
          imageProcessor = (VisionImageProcessor) new FaceDetectorProcessor(this);
          break;
//        case POSE_DETECTION:
//          PoseDetectorOptionsBase poseDetectorOptions =
//                  PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
//          boolean shouldShowInFrameLikelihood =
//                  PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
//          boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
//          boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
//          boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
//          imageProcessor =
//                  (VisionImageProcessor) new PoseDetectorProcessor(
//                          this,
//                          poseDetectorOptions,
//                          shouldShowInFrameLikelihood,
//                          visualizeZ,
//                          rescaleZ,
//                          runClassification,
//                          /* isStreamMode = */ true);
//          break;
        default:
          throw new IllegalStateException("Invalid model name");
      }
    } catch (Exception e) {
      Log.e(TAG, "Can not create image processor: " + selectedModel, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getLocalizedMessage(),
              Toast.LENGTH_LONG)
              .show();
      return;
    }

    ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
    Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
    if (targetResolution != null) {
      builder.setTargetResolution(targetResolution);
    }
    analysisUseCase = builder.build();

    needUpdateGraphicOverlayImageSourceInfo = true;
    analysisUseCase.setAnalyzer(

            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            imageProxy -> {
              if (needUpdateGraphicOverlayImageSourceInfo) {
                boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                Log.v("CameraX","Degree test:"+rotationDegrees);
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                  graphicOverlay.setImageSourceInfo(
                          imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                } else {
                  graphicOverlay.setImageSourceInfo(
                          imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                }
                needUpdateGraphicOverlayImageSourceInfo = false;
              }
              try {
                imageProcessor.processImageProxy(imageProxy, graphicOverlay);
              } catch (MlKitException e) {
                Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                        .show();
              }
            });

    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
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
//                Log.v("Client_Register","Connect:"+clientSocket1.isConnected());
        while (clientSocket.isConnected()){
//                    Log.v("CameraXLivePreview_tcp", "inthreadstatus:"+ thread.getState());
          if(put_send!=null){
            Log.v("CameraXLivePreview_tcp","send:"+put_send);
            bw.write(put_send);
            bw.flush();
            put_send = null;
          }

//宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
          get_rev = br.readLine();
          info = get_rev.split(", ");
          Log.v("CameraXLivePreview_tcp","GetAllmsg"+get_rev);
          if(get_rev!=null){
            //將取到的String抓取{}範圍資料
            Log.v("CameraXLivePreview_tcp","revive:"+get_rev);
            FaceDetector_opposite_name = info[0];
            FaceDetector_opposite_score = Integer.parseInt(info[1]);
            get_rev=null;

            if(FaceDetector_opposite_score>=0){
              Intent intent = new Intent(CameraXLivePreviewActivity.this, ShowScore.class);
              intent.putExtra("OppositeName", FaceDetector_opposite_name);
              intent.putExtra("OppositeScore", FaceDetector_opposite_score);
              intent.putExtra("UserScore",user_score);
              startActivity(intent);
              finish();
//              if(seconds==0&&minutes==0){
//                if(!inloading) {
//                  inloading=true;
//
//                }
//              }
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
      if (minutes == 0) {
        if (seconds == 0) {
          roundclock.setText("Time out!");
          GetServerCommunication();
          if (timer != null) {
            timer.cancel();
            timer = null;
          }
          if (timerTask != null) {
            timerTask = null;
          }
        } else {
          seconds--;
          if (seconds >= 10) {
            roundclock.setText("0" + minutes + ":" + seconds);
          } else {
            roundclock.setText("0" + minutes + ":0" + seconds);
          }
        }
      } else {
        if (seconds == 0) {
          seconds = 59;
          minutes--;
          if (minutes >= 10) {
            roundclock.setText(minutes + ":" + seconds);
          } else {
            roundclock.setText("0" + minutes + ":" + seconds);
          }
        } else {
          seconds--;
          if (seconds >= 10) {
            if (minutes >= 10) {
              roundclock.setText(minutes + ":" + seconds);
            } else {
              roundclock.setText("0" + minutes + ":" + seconds);
            }
          } else {
            if (minutes >= 10) {
              roundclock.setText(minutes + ":0" + seconds);
            } else {
              roundclock.setText("0" + minutes + ":0" + seconds);
            }
          }
        }
      }
    }
  };

  private void GetServerCommunication(){
    if(Login.log_username!=""){
      FaceDetector_name = Login.log_username;
    }else{
      FaceDetector_name = Register.reg_username;
    }
    ClientStage = "score";
    user_score = FaceDetectorProcessor.count;
    message = ClientStage+"\n"+ MainActivity.Main_email+"\n"+MainActivity.Main_name+"\n"+user_score;
    put_send = message;
    if(thread.getState().toString().equals("NEW")){
      Log.v("popcat_tcp", "threadstatus:"+ thread.getState());
      thread.start();
    }else if(thread.getState().toString().equals("RUNNABLE")){
      Log.v("popcat_tcp", "threadstatus:"+ thread.getState());
      thread = Thread.currentThread();
    }
  }

}
