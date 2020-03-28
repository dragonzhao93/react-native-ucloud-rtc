package com.ucloud_demo;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEngine;

public class RNMyLibraryModule extends ReactContextBaseJavaModule {
    private ReactApplicationContext mContext;
    public RNMyLibraryModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNMyLib";
    }

    @ReactMethod
    public void initWithUsreId(){
        Log.e("RNMyLibraryModule","this is initWithUsreId");
    }

    @ReactMethod
    public void initSDKWithUserIDappID(String appid,String appkey){
        String verCode = UCloudRtcSdkEngine.getSdkVersion();
        Log.e("RNMyLibraryModule","this is initSDKWithUserIDappID and SDK Ver = " + verCode);
    }

    @ReactMethod
    public void joinRoomWithRoomid(String roomId,String userId,String token){
        Toast.makeText(mContext, "tes", Toast.LENGTH_SHORT).show();
        Log.e("RNMyLibraryModule","this is joinRoomroomID");
    }
    @ReactMethod
    public void joinRoom(){
        Toast.makeText(mContext, "tes", Toast.LENGTH_SHORT).show();
        Log.e("RNMyLibraryModule","this is joinRoom");
    }
    @ReactMethod
    public void leaveRoom(){
        //离开房间
        Log.e("RNMyLibraryModule","this is leaveRoom");
    }
    @ReactMethod
    public void subscribeRemoteStream(){
        //订阅远程流
        Log.e("RNMyLibraryModule","this is subscribeRemoteStream");
    }
    @ReactMethod
    public void unSubscribeRemoteStream(){
        //取消订阅远程流
        Log.e("RNMyLibraryModule","this is unSubscribeRemoteStream");
    }
    @ReactMethod
    public void subscribeLocalStream(){
        //发布本地流
        Log.e("RNMyLibraryModule","this is subscribeLocalStream");
    }
    @ReactMethod
    public void unSubscribeLocalStream(){
        //取消发布本地流
        Log.e("RNMyLibraryModule","this is unSubscribeLocalStream");
    }
    @ReactMethod
    public void startRecordLocalStreamWithType(int type){
        //录制本地视频
        //类型  1 音频 2 视频 3 音频+视频
        Log.e("RNMyLibraryModule","this is startRecordLocalStreamWithType");
    }
    @ReactMethod
    public void stopRecordLocalStream(){
        //停止录制
        Log.e("RNMyLibraryModule","this is stopRecordLocalStream");
    }

}

