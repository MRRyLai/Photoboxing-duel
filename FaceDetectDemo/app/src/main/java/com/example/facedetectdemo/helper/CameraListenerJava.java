package com.example.facedetectdemo.helper;

import android.hardware.camera2.CameraDevice;
import android.util.Size;


public interface CameraListenerJava {
    /**
     * 打开时执行
     *
     * @param cameraDevice    相机实例
     * @param cameraId        相机ID
     * @param isMirror        是否镜像显示
     */
    void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize,
                        int displayOrientation, boolean isMirror);

//    /**
//     * 预览数据回调
//     *
//    fun onPreview(byteArray: ByteArray)
    /**
     * 预览数据回调
     *
     * @param image     预览的Image对象
     */
    void onPreview(byte[] byteArray);

    /**
     * 当相机关闭时执行
     */
    void onCameraClosed();

    /**
     * 当出现异常
     */
    void onCameraError(Exception e);
}
