package com.example.facedetectdemo.util;

import com.baidu.aip.face.AipFace;

public class AipFaceObjectJava {
    private static AipFace mClient = null;

    private AipFaceObjectJava() {}

    public static AipFace getClient() {
        if (mClient == null) {
            mClient = new AipFace(Const.BAIDU_APP_ID, Const.BAIDU_API_KEY, Const.BAIDU_SECRET_KEY);
            mClient.setConnectionTimeoutInMillis(2 * 1000);
        }
        return mClient;
    }
}
