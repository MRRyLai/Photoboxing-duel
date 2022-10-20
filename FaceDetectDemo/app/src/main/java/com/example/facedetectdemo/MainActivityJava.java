package com.example.facedetectdemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.aip.util.Base64Util;
import com.example.facedetectdemo.bean.DetectFaceBean;
import com.example.facedetectdemo.helper.CameraHelperJava;
import com.example.facedetectdemo.helper.CameraListenerJava;
import com.example.facedetectdemo.util.AipFaceObjectJava;
import com.example.facedetectdemo.util.PermissionUtils;
import com.example.facedetectdemo.widget.CircleTextureBorderViewJava;
import com.example.facedetectdemo.widget.RoundTextureView;
import com.example.facedetectdemo.widget.RoundTextureViewJava;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivityJava extends AppCompatActivity implements CameraListenerJava {

    private static final String TAG = "MainActivityJava";
    private static final String CAMERA_ID = CameraHelperJava.CAMERA_ID_BACK;

    private RoundTextureViewJava mTextureView;
    private CircleTextureBorderViewJava mBorderView;
    private Button mBtnSubmit;
    private LinearLayout mLayScores;
    private TextView mTvFaceScores;

    private CameraHelperJava mCameraHelper;
    private boolean isTakingPhoto = false;
    private boolean isCountDown = false;
    private long countDownTime = 30000;//倒计时数 30秒
    private int faceScores = 0;//检测到人脸分数

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);
        getSupportActionBar().hide();
        //隐藏系统状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initViews();
    }

    private void initViews() {
        mTextureView = findViewById(R.id.round_texture_view);
        mBorderView = findViewById(R.id.border_view);

        mLayScores = findViewById(R.id.lay_scores);
        mTvFaceScores = findViewById(R.id.tv_face_scores);

        mBtnSubmit = findViewById(R.id.btn_submit);

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isTakingPhoto = true;
                mBorderView.setTipsText("提交数据中...");
                if (mCameraHelper != null) {
                    mCameraHelper.takePhoto();
                }
                mBtnSubmit.setEnabled(false);



                if(!isCountDown){
                    isCountDown = true;
                    mBtnSubmit.setText("拍照（30s）");
                    faceScores = 0;
                    mLayScores.setVisibility(View.GONE);

                    new CountDownTimer(30000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            mBtnSubmit.setText("拍照（" + (millisUntilFinished / 1000 + 1)  + "）");
                        }
                        public void onFinish() {
                            mLayScores.setVisibility(View.VISIBLE);
                            mTvFaceScores.setText(String.valueOf(faceScores));
                            mBtnSubmit.setText("计时开始");
                            isCountDown = false;
                            mBtnSubmit.setEnabled(true);
                            setResumePreview();
                        }
                    }.start();
                }
            }
        });

        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnSubmit.setEnabled(true);
                setResumePreview();
            }
        });

        mTextureView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTextureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
                int sideLength = Math.min(mTextureView.getWidth(), mTextureView.getHeight() * 3 / 4);
                params.width = sideLength;
                params.height = sideLength;
                mTextureView.setLayoutParams(params);
                mTextureView.turnRound();
                mBorderView.setCircleTextureWidth(sideLength);
                if (PermissionUtils.Companion.isGranted(Manifest.permission.CAMERA, getApplicationContext())) {
                    initCamera();
                } else {
                    PermissionUtils.Companion.getInstance().with(MainActivityJava.this).permissions(Manifest.permission.CAMERA)
                            .requestCode(PermissionUtils.CODE_CAMERA)
                            .request(new PermissionUtils.PermissionCallback() {
                                @Override
                                public void denied() {
                                    PermissionUtils.Companion.getInstance().showDialog();
                                }

                                @Override
                                public void granted() {
                                    initCamera();
                                }
                            });
                }


            }
        });
    }

    private void setResumePreview() {
        this.isTakingPhoto = false;
        switchText("請點擊按鈕拍照");
        if (mCameraHelper != null) {
            mCameraHelper.stop();
            mCameraHelper.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isTakingPhoto) {
            if (mCameraHelper != null) {
                mCameraHelper.stop();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.e(TAG, "main currentThread = ${Thread.currentThread().name}");
        if (!isTakingPhoto) {
            if (mCameraHelper != null) {
                mCameraHelper.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
        PermissionUtils.Companion.getInstance().destroy();
        super.onDestroy();
    }

    private void initCamera() {
        if (mTextureView == null) {
            return;
        }
//        mTextureView ?: return

        int rotation = 0;
        WindowManager wm = getWindowManager();
        if (wm != null && wm.getDefaultDisplay() != null) {
            rotation = wm.getDefaultDisplay().getRotation();
        }

        mCameraHelper = new CameraHelperJava.Builder()
                .cameraListener(this)
                .specificCameraId(CAMERA_ID)
                .mContext(getApplicationContext())
                .previewOn(mTextureView)
                .previewViewSize(
                        new Point(
                                mTextureView.getLayoutParams().width,
                                mTextureView.getLayoutParams().height
                        )
                )
                .rotation(rotation)
                .build();
        mCameraHelper.start();
        switchText("请点击按钮拍照");
    }

    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, final Size previewSize, final int displayOrientation, boolean isMirror) {
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.getWidth() + " x " + previewSize.getHeight());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
                if (displayOrientation % 180 == 0) {
                    params.height = params.width * previewSize.getHeight() / previewSize.getWidth();
                }
                else {
                    params.height = params.width * previewSize.getWidth() / previewSize.getHeight();
                }
                mTextureView.setLayoutParams(params);
            }
        });
    }

    @Override
    public void onPreview(byte[] byteArray) {
        Log.i(TAG, "onPreview: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switchText("检测人脸中..");
            }
        });

        final String postImage = Base64Util.encode(byteArray);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (detectFace(postImage)) {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    @Override
    public void onCameraClosed() {

    }

    @Override
    public void onCameraError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivityJava.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchText(final String shadowContent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (shadowContent != null && shadowContent.length() > 0) {
                    mBorderView.setTipsText(shadowContent);
                }
            }
        });
    }

    private boolean detectFace(String postImage) throws JSONException {
        boolean bSuccess = false;
        HashMap<String, String> detectOptions = new HashMap<>();
        detectOptions.put("face_field", "age,gender,race,expression,beauty");
        detectOptions.put("face_type", "LIVE");

        JSONObject detectRes = AipFaceObjectJava.getClient().detect(postImage, "BASE64", detectOptions);

        String mShadowText = "";

        if (detectRes.getInt("error_code") == 0) {
            bSuccess = true;
            DetectFaceBean detectBean = new Gson().fromJson(detectRes.getJSONObject("result").toString(), DetectFaceBean.class);
            /*<DetectFaceBean>(
            detectRes.getJSONObject("result").toString(), DetectFaceBean::class.java)*/
            Log.e(TAG, "detect beauty=" +
                    detectBean.getFace_list().get(0).getBeauty() + " and" +
                    " expression=" +
                    detectBean.getFace_list().get(0).getExpression().getType() + " and" +
                    " age = " +
                    detectBean.getFace_list().get(0).getAge());
            mShadowText = "成功检测到人脸";
            faceScores++;
        } else {
            mShadowText = "检测失败";
        }

        final String finalMShadowText = mShadowText;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnSubmit.setEnabled(true);
                setResumePreview();

                switchText(finalMShadowText);
            }
        });
        return bSuccess;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }
}
