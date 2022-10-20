package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class popcat_MainActivity extends AppCompatActivity implements View.OnTouchListener{
    TextView textView, counterView, counterNumberView;
    ImageButton imageButton,imageButton2;
    Button button,button_reset;
    MediaPlayer music ;
    int count=0;
    String To_srv_msg;
    boolean MainFlag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popcat_activity_main);
        init();
        //ButtonClick();
        music=MediaPlayer.create(this, R.raw.popcat);
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
//        new CountDownTimer(30000, 1000) {
//
//            public void onTick(long millisUntilFinished) {
//                tvtimer.setText("seconds remaining: " + millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
//                tvtimer.setText("done!");
//                new SendMessage().execute("From Android " + count);
//            }
//        }.start();
    }



    private void ButtonClick() {

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!MainFlag) {
                        MainFlag = true;
                        imageButton.setVisibility(View.GONE);
                        imageButton2.setVisibility(View.VISIBLE);
                    }else{
                        MainFlag=false;
                        imageButton2.setVisibility(View.GONE);
                        imageButton.setVisibility(View.VISIBLE);
                    }
                }
            });
    }


//
//    /** A safe way to get an instance of the Camera object. */
//    public static Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            c = Camera.open(); // attempt to get a Camera instance
//        }
//        catch (Exception e){
//            // Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }
//
//    /** A basic Camera preview class */
//    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
//        private SurfaceHolder mHolder;
//        private Camera mCamera;
//
//        public CameraPreview(Context context, Camera camera) {
//            super(context);
//            mCamera = camera;
//
//            // Install a SurfaceHolder.Callback so we get notified when the
//            // underlying surface is created and destroyed.
//            mHolder = getHolder();
//            mHolder.addCallback(this);
//            // deprecated setting, but required on Android versions prior to 3.0
//            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        }
//
//        public void surfaceCreated(SurfaceHolder holder) {
//            // The Surface has been created, now tell the camera where to draw the preview.
//            try {
//                mCamera.setPreviewDisplay(holder);
//                mCamera.startPreview();
//            } catch (IOException e) {
//                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
//            }
//        }
//
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            // empty. Take care of releasing the Camera preview in your activity.
//        }
//
//        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//            // If your preview can change or rotate, take care of those events here.
//            // Make sure to stop the preview before resizing or reformatting it.
//
//            if (mHolder.getSurface() == null){
//                // preview surface does not exist
//                return;
//            }
//
//            // stop preview before making changes
//            try {
//                mCamera.stopPreview();
//            } catch (Exception e){
//                // ignore: tried to stop a non-existent preview
//            }
//
//            // set preview size and make any resize, rotate or
//            // reformatting changes here
//
//            // start preview with new settings
//            try {
//                mCamera.setPreviewDisplay(mHolder);
//                mCamera.startPreview();
//
//            } catch (Exception e){
//                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//            }
//        }
//    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()){
            case R.id.button:
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.v("壓住","clicked");
                        imageButton.setVisibility(View.GONE);
                        imageButton2.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
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
                        count=0;
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                new SendMessage().execute("From Android " + count);
                Log.v("傳送",""+count);
        }
    }
}