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

package com.google.mlkit.vision.demo.java.facedetector;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/** Face Detector Demo. */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

  private static final String TAG = "FaceDetectorProcessor";
  private static int count=0;

  private static double second = 5.0;
  private static double d = (double) (Math.round(second*10.0)/10.0);
  private static TimerTask timerTask;
  private static Timer timer;
  public static TextView clock;



  private final FaceDetector detector;
  private static boolean facekeepstatus;
  private static Ob_timer t = new Ob_timer();

  private static final Handler handler1 = new Handler();
  private static final Runnable runnable = new Runnable() {
    public void run() {
      d = Math.round(d*10.0)/10.0;
      Log.v(MANUAL_TESTING_LOG,"runnable timer:"+d);
      Log.v(MANUAL_TESTING_LOG,"runnable DetectorStatus:"+facekeepstatus);
      if(facekeepstatus){
        if (d == 0) {
//          clock.setText("+1!");
//          count++;
        } else {
          d-=0.1;
          d = Math.round(d*10.0)/10.0;
          if (d > 0.0) {
//            clock.setText(Double.toString(d));
          } else {
//            clock.setText("+1!");
            d=5.0;
//            count++;
          }
        }
      }else{
        d = 5.0;
      }
      handler.postDelayed(this, 100);
    }
  };

  public FaceDetectorProcessor(Context context) {
    super(context);
    FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
    detector = FaceDetection.getClient(faceDetectorOptions);
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<Face>> detectInImage(InputImage image) {
    return detector.process(image);
  }

  @Override
  protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
    for (Face face : faces) {
      graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
      logExtrasForTesting(face);
    }
  }


  private static void logExtrasForTesting(Face face) {
    facekeepstatus = false;
    if (face != null) {
      facekeepstatus=true;

      Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

      // All landmarks
      int[] landMarkTypes =
          new int[] {
            FaceLandmark.MOUTH_BOTTOM,
            FaceLandmark.MOUTH_RIGHT,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EAR,
            FaceLandmark.LEFT_EAR,
            FaceLandmark.RIGHT_CHEEK,
            FaceLandmark.LEFT_CHEEK,
            FaceLandmark.NOSE_BASE
          };
      String[] landMarkTypesStrings =
          new String[] {
            "MOUTH_BOTTOM",
            "MOUTH_RIGHT",
            "MOUTH_LEFT",
            "RIGHT_EYE",
            "LEFT_EYE",
            "RIGHT_EAR",
            "LEFT_EAR",
            "RIGHT_CHEEK",
            "LEFT_CHEEK",
            "NOSE_BASE"
          };
      for (int i = 0; i < landMarkTypes.length; i++) {
        FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
        if (landmark == null) {
          facekeepstatus = false;
          Log.v(
              MANUAL_TESTING_LOG,
              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
        } else {
          PointF landmarkPosition = landmark.getPosition();
          String landmarkPositionStr = String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
//          Log.v(
//              MANUAL_TESTING_LOG,
//              "Position for face landmark: "
//                  + landMarkTypesStrings[i]
//                  + " is :"
//                  + landmarkPositionStr);
        }
      }

//      Log.v(
//          MANUAL_TESTING_LOG,
//          "face left eye open probability: " + face.getLeftEyeOpenProbability());
//      Log.v(
//          MANUAL_TESTING_LOG,
//          "face right eye open probability: " + face.getRightEyeOpenProbability());
//      Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());

      if (face.getTrackingId() != null){
        if (t.getTrack_id().equals(face.getTrackingId())){
        }else{
          t.setTrack_id(String.valueOf(face.getTrackingId()));
        }
      }
      Log.v(MANUAL_TESTING_LOG,"Count:"+count);
      Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
    }else{
      Log.v(MANUAL_TESTING_LOG, "Face:" + null);
      handler1.removeCallbacks(runnable);
    }
    runnable.run();
    timer();
  }

  private static void timer() {
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
    timer.schedule(timerTask, 0, 100);
  }

  private static Handler handler = new Handler() {
    public void handleMessage(Message msg) {
      d = Math.round(d*10.0)/10.0;
      Log.v(MANUAL_TESTING_LOG,"timer:"+d);
      Log.v(MANUAL_TESTING_LOG,"DetectorStatus:"+facekeepstatus);
      if(facekeepstatus){
        if (d == 0) {
          clock.setText("+1!");
          count++;
          if (timer != null) {
            timer.cancel();
            timer = null;
          }
          if (timerTask != null) {
            timerTask = null;
          }
        } else {
          d-=0.1;
          d = Math.round(d*10.0)/10.0;
          if (d > 0.0) {
            clock.setText(Double.toString(d));
          } else {
            clock.setText("+1!");
            d=5.0;
            count++;
            if (timer != null) {
              timer.cancel();
              timer = null;
            }
            if (timerTask != null) {
              timerTask = null;
            }
          }
        }
      }else{
        if (timer != null) {
          timer.cancel();
          timer = null;
        }
        if (timerTask != null) {
          timerTask = null;
        }
        d = 5.0;
      }
    }
  };


  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}

