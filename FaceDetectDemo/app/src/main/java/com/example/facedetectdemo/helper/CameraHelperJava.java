package com.example.facedetectdemo.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;

public class CameraHelperJava {
    private static final String TAG = "CameraHelperJava";

    private static final int MAX_PERVIEW_WIDTH = 2560;
    private static final int MAX_PREVIEW_HEIGHT = 2560;
    private static final int MIN_PREVIEW_WIDTH = 720;
    private static final int MIN_PREVIEW_HEIGHT = 720;

    public static final String CAMERA_ID_FRONT = "1";
    public static final String CAMERA_ID_BACK = "0";

    private String mCameraId;
    private String specificCameraId;
    private CameraListenerJava cameraListenerJava;
    private TextureView mTextureView;
    private int rotation;
    private Point previewViewSize;
    private Point specificPreviewSize;
    private boolean isMirror;
    private Context mContext;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private int mSensorOrientation;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "onSurfaceTextureAvailable: ");
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(TAG, "onSurfaceTextureDestroyed: ");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "onOpened: ");
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenLock.release();
            mCameraDevice = camera;
            createCameraPreviewSession();
            if (mPreviewSize != null) {
                if (cameraListenerJava != null) {
                    cameraListenerJava.onCameraOpened(camera, mCameraId, mPreviewSize, getCameraOri(rotation, mCameraId), isMirror);
                }
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i(TAG, "onDisconnected: ");
            mCameraOpenLock.release();
            camera.close();
            mCameraDevice = null;
            if (cameraListenerJava != null) {
                cameraListenerJava.onCameraClosed();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.i(TAG, "onError: ");
            mCameraOpenLock.release();
            camera.close();
            mCameraDevice = null;
            if (cameraListenerJava != null) {
                cameraListenerJava.onCameraError(new Exception("error occurred, code is $error"));
            }
        }
    };

    private CameraCaptureSession.StateCallback mCaptureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "onConfigured: ");
            // The camera is already closed
            CameraDevice cameraDevice = mCameraDevice;
            if (cameraDevice == null) {
                return;
            }

            // When the session is ready, we start displaying the preview
            mCaptureSession = session;
            try {
                CaptureRequest.Builder builder = mPreviewRequestBuilder;
                if (builder != null) {
                    CameraCaptureSession cameraCaptureSession = mCaptureSession;
                    if (cameraCaptureSession != null) {
                        cameraCaptureSession.setRepeatingRequest(builder.build(),
                                new CameraCaptureSession.CaptureCallback() {}, mBackgroundHandler);
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "onConfigureFailed: ");
            if (cameraListenerJava != null) {
                cameraListenerJava.onCameraError(new Exception("configuredFailed"));
            }
        }
    };
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public void onImageAvailable(ImageReader reader) {
            // 这里做保存工作
            Log.e("CameraHelperJava", "onImageAvailable");
            Image image = reader != null ? reader.acquireNextImage() : null;
            if (CameraHelperJava.this.cameraListenerJava != null) {
                if (image != null) {
                    if (image.getFormat() == 256) {
                        Image.Plane[] planes = image.getPlanes();
                        this.lock.lock();
                        Image.Plane var10000 = planes[0];
                        Intrinsics.checkExpressionValueIsNotNull(planes[0], "planes[0]");
                        ByteBuffer byteBuffer = var10000.getBuffer();
                        byte[] byteArray = new byte[byteBuffer.remaining()];
                        byteBuffer.get(byteArray);
                        CameraListenerJava var6 = CameraHelperJava.this.cameraListenerJava;
                        if (var6 != null) {
                            var6.onPreview(byteArray);
                        }

                        this.lock.unlock();
                    }
                }
            }

            if (image != null) {
                image.close();
            }
        }
    };
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private Semaphore mCameraOpenLock = new Semaphore(1);

    public static final class Builder {
        @Nullable
        private TextureView previewDisplayView;
        private boolean isMirror;
        @NotNull
        public String specificCameraId;
        @Nullable
        private CameraListenerJava cameraListenerJava;
        @Nullable
        private Point previewViewSize;
        private int rotation;
        @Nullable
        private Point previewSize;
        @Nullable
        private Context mContext;

        public Builder previewOn(TextureView value) {
            previewDisplayView = value;
            return this;
        }

        public Builder isMirror(boolean value) {
            isMirror = value;
            return this;
        }

        public Builder previewSize(Point value) {
            previewSize = value;
            return this;
        }

        public Builder previewViewSize(Point value) {
            previewViewSize = value;
            return this;
        }

        public Builder rotation(int value) {
            rotation = value;
            return this;
        }

        public Builder specificCameraId(String value) {
            specificCameraId = value;
            return this;
        }

        public Builder cameraListener(CameraListenerJava value) {
            cameraListenerJava = value;
            return this;
        }

        public Builder mContext(Context value) {
            mContext = value;
            return this;
        }

        public CameraHelperJava build() {
            if (previewViewSize == null) {
                Log.e(TAG, "previewViewSize is null, now use default previewSize.");
            }

            if (cameraListenerJava == null){
                Log.e(TAG, "cameraListenerJava is null, callback will not be called.");
            }

            if (previewDisplayView == null) {
                throw new RuntimeException("you must preview on a textureView or a surfaceView.");
            }

            return new CameraHelperJava(this);
        }
    }

    private CameraHelperJava(Builder builder) {
        mTextureView = builder.previewDisplayView;
        specificCameraId = builder.specificCameraId;
        cameraListenerJava = builder.cameraListenerJava;
        rotation = builder.rotation;
        previewViewSize = builder.previewViewSize;
        specificPreviewSize = builder.previewSize;
        isMirror = builder.isMirror;
        mContext = builder.mContext;
        if (isMirror && mTextureView != null) mTextureView.setScaleX(-1F);
    }

    private int getCameraOri(int rotation, String cameraId) {
        int degrees = rotation * 90;
        switch(rotation) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
        }

        int result = Intrinsics.areEqual("1", cameraId) ? (360 - (this.mSensorOrientation + degrees) % 360) % 360 : (this.mSensorOrientation - degrees + 360) % 360;
        Log.i("CameraHelperJava", "getCameraOri: " + rotation + ' ' + result + ' ' + this.mSensorOrientation);
        return result;
    }

    private Size getBestSupportedSize(List<Size> sizes) {
        Size[] tmpSizes = (Size[]) sizes.toArray(new Size[0]);
        Arrays.sort(tmpSizes, new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                if (o1 != null && o2 != null) {
                    if (o1.getWidth() > o2.getWidth()) {
                        return -1;
                    } else if (o1.getWidth() == o2.getWidth()) {
                        if (o1.getHeight() > o2.getHeight()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return 1;
                    }
                } else {
                    Log.e(TAG, "can't compare");
                    return -1;
                }
            }
        });
        List<Size> mSizes = Arrays.asList(tmpSizes);
        Size bestSize = mSizes.get(0);
        float previewViewRatio = previewViewSize != null ? (float) previewViewSize.x / previewViewSize.y
                : (float) bestSize.getWidth() / bestSize.getHeight();
        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio;
        }

        for (Size size : mSizes) {
            if (specificPreviewSize != null && specificPreviewSize.x == size.getWidth() && specificPreviewSize.y == size.getHeight()) {
                return size;
            }
            if (size.getWidth() > MAX_PERVIEW_WIDTH || size.getHeight() > MAX_PREVIEW_HEIGHT ||
                    size.getWidth() < MIN_PREVIEW_WIDTH || size.getHeight() < MIN_PREVIEW_HEIGHT) {
                continue;
            }
            if (Math.abs((float) size.getHeight() / size.getWidth() - previewViewRatio) <
                    Math.abs((float) bestSize.getHeight() / bestSize.getWidth() - previewViewRatio)) {
                bestSize = size;
            }
        }
        return bestSize;
    }

    public synchronized void start() {
        Log.i(TAG, "start");
        if (mCameraDevice != null) return;

        startBackgroundThread();
        // When the screen is turned off the turned back on, the SurfaceTexture is already available,
        // and "onSurfaceTextureAvailable" will not be called. In that case, we can open a camera
        // and start preview from here (otherwise, we wait until the surface is ready in the
        // SurfaceTextureListener).
        if (mTextureView != null) {
            if(mTextureView.isAvailable()) {
                openCamera();
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }
    }

    public synchronized void stop() {
        Log.i(TAG, "stop");
        if (this.mCameraDevice != null) {
            this.closeCamera();
            this.stopBackgroundThread();
        }
    }

    public void release() {
        this.stop();
        this.mTextureView = (TextureView)null;
        this.cameraListenerJava = (CameraListenerJava)null;
        this.mContext = (Context)null;
    }

    private void setUpCameraOutputs(CameraManager cameraManager) {
        Log.i(TAG, "setUpCameraOutputs step 1");
        try {
            if (configCameraParams(cameraManager, this.specificCameraId)) {
                return;
            }

            Log.i(TAG, "setUpCameraOutputs step 2");
            String[] cameraIdList = cameraManager.getCameraIdList();
            int length = cameraIdList.length;

            for(int i = 0; i < length; ++i) {
                String cameraId = cameraIdList[i];
                Intrinsics.checkExpressionValueIsNotNull(cameraId, "cameraId");
                if (this.configCameraParams(cameraManager, cameraId)) {
                    return;
                }
            }
            Log.i(TAG, "setUpCameraOutputs step 3");
        } catch (CameraAccessException var6) {
            var6.printStackTrace();
        } catch (NullPointerException var7) {
            CameraListenerJava listener = this.cameraListenerJava;
            if (listener != null) {
                listener.onCameraError((Exception)var7);
            }
        }

    }

    private boolean configCameraParams(CameraManager manager, String cameraId) throws CameraAccessException {
        CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
        // kotlin 转换过来时 会自带有的判空
        Intrinsics.checkExpressionValueIsNotNull(cameraCharacteristics, "manager.getCameraCharacteristics(cameraId)");
        CameraCharacteristics characteristics = cameraCharacteristics;
        StreamConfigurationMap map = (StreamConfigurationMap)characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            Intrinsics.checkExpressionValueIsNotNull(sizes, "map.getOutputSizes(SurfaceTexture::class.java)");
            this.mPreviewSize = this.getBestSupportedSize(new ArrayList(ArraysKt.asList(sizes)));
            // 这里也是kt转换java时会带有的判空机制 目的是强保证获取宽高时非空
            Size sizeCheck1 = this.mPreviewSize;
            if (sizeCheck1 == null) {
                Intrinsics.throwNpe();
            }

            int sizeCheck1Width = sizeCheck1.getWidth();
            Size sizeCheck2 = this.mPreviewSize;
            if (sizeCheck2 == null) {
                Intrinsics.throwNpe();
            }

            this.mImageReader = ImageReader.newInstance(sizeCheck1Width, sizeCheck2.getHeight(), ImageFormat.JPEG, 2);
            ImageReader imageReader = this.mImageReader;
            if (imageReader != null) {
                imageReader.setOnImageAvailableListener(this.mOnImageAvailableListener, this.mBackgroundHandler);
            }

            Integer orientation = (Integer)characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (orientation != null) {
                Integer var5 = orientation;
//                boolean var6 = false;
//                boolean var7 = false;
                Intrinsics.checkExpressionValueIsNotNull(var5, "it");
                this.mSensorOrientation = var5;
            }

            this.mCameraId = cameraId;
            return true;
        } else {
            return false;
        }
    }

    private void openCamera() {
        if (mContext == null) throw new RuntimeException("context param should not be null.");
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            return;
        }
        Log.e(TAG, "openCamera");
        setUpCameraOutputs(cameraManager);
        if (mTextureView != null) {
            configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
        }
        try {
            if (!mCameraOpenLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(mCameraId, mDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException | InterruptedException e) {
            if (cameraListenerJava != null) {
                cameraListenerJava.onCameraError(e);
            }
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenLock.acquire();

            if (mCaptureSession != null) {
                mCaptureSession.close();
            }
            mCaptureSession = null;

            if (mCameraDevice != null) {
                mCameraDevice.close();
            }
            mCameraDevice = null;

            if (cameraListenerJava != null) {
                cameraListenerJava.onCameraClosed();
            }

            if (mImageReader != null) {
                mImageReader.close();
            }
            mImageReader = null;
        } catch (InterruptedException e) {
            if (cameraListenerJava != null) {
                cameraListenerJava.onCameraError(e);
            }
        } finally {
            mCameraOpenLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            if (mTextureView == null) throw new RuntimeException("mTextureView param should not be null");
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert(texture != null);

            // We configure the size of default buffer to be the size of camera preview we want.
            if (mPreviewSize != null) {
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }

            // This is the output Surface we need to start preview !!!
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface
            if (mCameraDevice != null) {
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            }

            // 自动对焦
            if (mPreviewRequestBuilder != null) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // 增加曝光度
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getRange());

                mPreviewRequestBuilder.addTarget(surface);

                // Here, we create a CameraCaptureSession for camera preview.
                if (mImageReader != null) {
                    mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                            mCaptureStateCallback, mBackgroundHandler);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to 'mTextureView'.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of 'mTextureView' is fixed.
     *
     * @param viewWidth   The width of 'mTextureView'
     * @param viewHeight  The height of 'mTextureView'
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (mTextureView == null) return;
//        mTextureView?: return
        if (mPreviewSize == null) return;
//                mPreviewSize?: return
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0F, 0F, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0F, 0F, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(viewHeight / mPreviewSize.getHeight(),
                    viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate((90 * (rotation - 2F)) % 360, centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180F, centerX, centerY);
        }
        Log.i(TAG, "configureTransform: ${getCameraOri(rotation, mCameraId)}  ${rotation * 90}");
        mTextureView.setTransform(matrix);
    }

    public void takePhoto() {
        // 一定要在这里加 不然回调不了 如果在上面加 会导致一直在捕获图像
        if (mImageReader != null) {
            if (mPreviewRequestBuilder != null) {
                mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            }
        }

        // 这里保存时要选sensorOrientation(照相机的方向)
        if (mPreviewRequestBuilder != null) {
            mPreviewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mSensorOrientation);

            // 设置对焦触发器为空闲状态
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        }


        if (mCaptureSession != null) {
            try {
                mCaptureSession.stopRepeating();
                // 开始拍照
                if (mPreviewRequestBuilder != null) {
                    mCaptureSession.capture(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                cameraListenerJava.onCameraError(e);
            }
        }
    }

    private Range<Integer> getRange() {
        if (mContext == null) {
            throw new RuntimeException("context param should not be null");
        }
        CameraManager mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            throw new RuntimeException("cameraManager init load failed");
        }
        CameraCharacteristics chars = null;
//        var chars: CameraCharacteristics? = null
        try {
            chars = mCameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (chars != null) {
            Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            if (ranges != null) {
                Range<Integer> result = null;
                for (Range<Integer> range : ranges) {
                    if (range.getLower() < 10) {
                        continue;
                    }
                    if (result == null) {
                        result = range;
                    } else if (range.getLower() <= 15 && (range.getUpper() - range.getLower())
                            > result.getUpper() - result.getLower()) {
                        result = range;
                    }
                }
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
