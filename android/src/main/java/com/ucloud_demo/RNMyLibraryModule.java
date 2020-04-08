package com.ucloud_demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.IntDef;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.ucloud_demo.utils.CommonUtils;
import com.ucloud_demo.utils.PermissionUtils;
import com.ucloud_demo.utils.SuperLog;
import com.ucloud_demo.utils.ToastUtils;
import com.ucloud_demo.view.URTCVideoViewInfo;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEngine;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEnv;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkAudioDevice;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkAuthInfo;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkErrorCode;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkLogLevel;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMediaType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMode;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkRoomType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkScaleType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStats;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamInfo;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamRole;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkTrackType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkVideoProfile;
import com.ucloudrtclib.sdkengine.listener.UCloudRtcSdkEventListener;


import static android.app.Activity.RESULT_OK;

public class RNMyLibraryModule extends ReactContextBaseJavaModule {
    private ReactApplicationContext mContext;
    private final String TAG = this.getClass().getSimpleName();
    private boolean isNeesScreenCapture = false;
    private String roomId;
    private String userId;
    private String mToken;
    private String mAppId;
    private Handler mHandler;
    private Activity mActivity;
    private UCloudRtcSdkEngine sdkEngine;
    public static RNMyVideoView rnMyVideoView;
    //尝试初始化一个UCloudRtcSurfaceVideoView用作渲染流和传入RN进行展示
    private static UCloudRtcSdkSurfaceVideoView mRenderView;

    public RNMyLibraryModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        mActivity = getCurrentActivity();
        mContext.addActivityEventListener(mActivityEvent);
        mHandler = new Handler(Looper.getMainLooper());
        initUCloud();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                rnMyVideoView = RNMyVideoView.getInstance(mContext);
                //必须要Looper.prepare
                sdkEngine = UCloudRtcSdkEngine.createEngine(eventListener);
                sdkEngine.setClassType(UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_SMALL);
            }
        });
    }
    public RNMyVideoView getVideoView(){
        return rnMyVideoView;
    }
    @NonNull
    @Override
    public String getName() {
        return "RNMyLib";
    }

    /**
     * 初始化SDK
     * @params
     * @params
     */
    private void initUCloud(){
        UCloudRtcSdkEnv.initEnv(mContext.getApplicationContext());
        UCloudRtcSdkEnv.setWriteToLogCat(true);
        UCloudRtcSdkEnv.setLogLevel(UCloudRtcSdkLogLevel.UCLOUD_RTC_SDK_LogLevelInfo);
        UCloudRtcSdkEnv.setSdkMode(UCloudRtcSdkMode.UCLOUD_RTC_SDK_MODE_TRIVAL);
        UCloudRtcSdkEnv.setTokenSeckey(CommonUtils.SEC_KEY);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        CommonUtils.mItemWidth = (outMetrics.widthPixels - UiHelper.dipToPx(mContext, 15)) / 3;
        CommonUtils.mItemHeight = CommonUtils.mItemWidth;
    }


    @ReactMethod
    public void initWithAppid(String appid, String appkey, boolean isAuto, Promise promise){
        this.mAppId = appid;
        sdkEngine.setAudioOnlyMode(false) ; // 设置纯音频模式
        sdkEngine.configLocalCameraPublish(true) ; // 设置摄像头是否发布
        sdkEngine.configLocalAudioPublish(true) ; // 设置音频是否发布，用于让sdk判断自动发布的媒体类型
        sdkEngine.configLocalScreenPublish(true) ; // 设置桌面是否发布，作用同上
        sdkEngine.setStreamRole(UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_BOTH);// 流权限
        sdkEngine.setAutoPublish(false) ; // 是否自动发布
        sdkEngine.setAutoSubscribe(true) ;// 是否自动订阅
        // 摄像头输出等级
        sdkEngine.setVideoProfile(UCloudRtcSdkVideoProfile.matchValue(0)) ;
        SuperLog.e("RNMyLibraryModule","this is initWithAppid");
        promise.resolve("resolve");
    }


    private Promise roomPromise;
    @ReactMethod
    public void joinRoomWithRoomid(String roomId,String userId,String token,Promise promise){
        this.roomId = roomId;
        this.userId = userId;
        this.mToken = token;
        this.roomPromise = promise;
        startJoinChannel();
        if (isNeesScreenCapture && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //开始申请采集桌面显示的所有内容，在onActivityResult中得到回调
            Activity activity = mContext.getCurrentActivity();
            SuperLog.e(TAG,"NAME = " + activity.getLocalClassName());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UCloudRtcSdkEngine.requestScreenCapture(mContext.getCurrentActivity());
                }
            });
        }



    }

    /**
     * 重写ActivityEventListener监听onActivityResult方法
     * 不然可以在MainActivity中onActivityResult中使用
     * ReactInstanceManager.onActivityResult
     *
     * @params
     *
     */
    private final ActivityEventListener mActivityEvent = new BaseActivityEventListener(){
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (resultCode != RESULT_OK) {
                ToastUtils.shortShow(mContext,"获取桌面采集权限失败");
                return;
            }
            if (requestCode != UCloudRtcSdkEngine.SCREEN_CAPTURE_REQUEST_CODE) {
                ToastUtils.shortShow(mContext,"获取桌面采集权限失败");
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UCloudRtcSdkEngine.onScreenCaptureResult(data);
                }
            });

            startJoinChannel();
        }
    };
    /**
     * 加入房间
     * @params
     * @params
     *
     */
    private void startJoinChannel(){
        UCloudRtcSdkAuthInfo info = new UCloudRtcSdkAuthInfo();
        info.setAppId(mAppId);
        info.setToken(mToken);
        info.setRoomId(roomId);
        info.setUId(userId);
        Log.d(TAG, " roomtoken = " + mToken);
        sdkEngine.joinChannel(info);
    }


    @ReactMethod
    public void subscribeRemoteStream(){
        //订阅远程流
        SuperLog.e("RNMyLibraryModule","this is subscribeRemoteStream");
        UCloudRtcSdkStreamInfo info = new UCloudRtcSdkStreamInfo();
        info.setUid(userId);
        info.setHasAudio(true);
        info.setHasVideo(true);
        info.setMediaType(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        sdkEngine.subscribe(info);
        //不想渲染时可以调用定制渲染接口
        //sdkEngine.stopPreview(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
    }
    @ReactMethod
    public void unSubscribeRemoteStream(){
        //取消订阅远程流
        SuperLog.e("RNMyLibraryModule","this is unSubscribeRemoteStream");
        UCloudRtcSdkStreamInfo info = new UCloudRtcSdkStreamInfo();
        info.setUid(userId);
        info.setMediaType(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        sdkEngine.subscribe(info);

    }
    @ReactMethod
    public void publishLocalStreamWithCameraEnable(boolean isOpenCamera){
        if(!PermissionUtils.hasPermissions(mContext,Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ToastUtils.shortShow(mContext,"相机或存储权限未开启");
        }
        //发布本地流
        sdkEngine.publish(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true,true);
    }
    @ReactMethod
    public void unPublishLocalStream(){
        //取消发布本地流
        SuperLog.e("RNMyLibraryModule","this is unPublishLocalStream");
        sdkEngine.unPublish(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);

    }


    @ReactMethod
    public void startRecordLocalStreamWithType(int type){
        //录制本地视频
        //类型  1 音频 2 视频 3 音频+视频
        SuperLog.e("RNMyLibraryModule","this is startRecordLocalStreamWithType");
    }
    @ReactMethod
    public void stopRecordLocalStream(){
        //停止录制
        SuperLog.e("RNMyLibraryModule","this is stopRecordLocalStream");
    }
    @ReactMethod
    public void leaveRoom(){
        //离开房间
        SuperLog.e("RNMyLibraryModule","this is leaveRoom");
        sdkEngine.leaveChannel();
//        mContext.getCurrentActivity().finish();
    }
    /****************************************************************/
    @TargetApi(21)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(mContext, "获取桌面采集权限失败",
                    Toast.LENGTH_LONG).show() ;
            return;
        }
        if (requestCode != UCloudRtcSdkEngine.SCREEN_CAPTURE_REQUEST_CODE) {
            Toast.makeText(mContext, "获取桌面采集权限失败",
                    Toast.LENGTH_LONG).show() ;
            return;
        }
        UCloudRtcSdkEngine.onScreenCaptureResult(data);
        startJoinChannel();
    }




    UCloudRtcSdkEventListener eventListener = new UCloudRtcSdkEventListener() {

        @Override
        public void onServerDisconnect() {
            //
        }

        @Override
        public void onJoinRoomResult(int i, String s, String s1) {
            Log.e(TAG,"JOIN = " + i + ", msg = " + s + ", msg1 = " + s1);
            if(i == 0 && roomPromise != null){
                roomPromise.resolve("resolve");
            }
            ToastUtils.shortShow(mContext,"进入房间");
        }

        @Override
        public void onLeaveRoomResult(int i, String s, String s1) {
            ToastUtils.shortShow(mContext,"离开房间");
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    UCloudRtcSdkEngine.destory();
                }
            });

        }

        @Override
        public void onRejoiningRoom(String s) {

        }

        @Override
        public void onRejoinRoomResult(String s) {

        }

        @Override
        public void onLocalPublish(int i, String s, UCloudRtcSdkStreamInfo info) {
            //发布流

            mContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rnMyVideoView.setBackgroundColor(Color.TRANSPARENT);
                    UCloudRtcSdkSurfaceVideoView localrenderview = rnMyVideoView.getVideoView();
                    Log.e(TAG,"onLocalPublish");
                    info.setHasVideo(true);
                    info.setHasAudio(true);
                    info.setHasData(true);
                    rnMyVideoView.getVideoView().getSurfaceView().setNeedFullScreen(true);
                    sdkEngine.startPreview(info.getMediaType(), rnMyVideoView.getVideoView(),UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT,null);
                }
            });
        }

        @Override
        public void onLocalUnPublish(int i, String s, UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            //取消发布媒体流
            Log.e(TAG,"onLocalUnPublish");
        }

        @Override
        public void onRemoteUserJoin(String s) {

        }

        @Override
        public void onRemoteUserLeave(String s, int i) {

        }

        @Override
        public void onRemotePublish(UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            //开通自动订阅之后，当订阅的用户推流后会自动调用本方法
            userId = uCloudRtcSdkStreamInfo.getUId();
            //订阅远程流
            SuperLog.e("RNMyLibraryModule","this is subscribeRemoteStream");
            UCloudRtcSdkStreamInfo info = new UCloudRtcSdkStreamInfo();
            info.setUid(userId);
            info.setHasAudio(true);
            info.setHasVideo(true);
            info.setMediaType(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
            sdkEngine.subscribe(info);
        }

        @Override
        public void onRemoteUnPublish(UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {

        }

        @Override
        public void onSubscribeResult(int i, String s, UCloudRtcSdkStreamInfo info) {
            //订阅媒体流结果
            mContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    URTCVideoViewInfo vinfo = new URTCVideoViewInfo(null);
                    UCloudRtcSdkSurfaceVideoView videoView = new UCloudRtcSdkSurfaceVideoView(mContext.getApplicationContext());;

                    Log.d(TAG, " subscribe info: " + info.getUId() + " hasvideo " + info.isHasVideo());
                    if (info.isHasVideo()) {
                        videoView.init(false);
                        videoView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
                        vinfo.setmRenderview(videoView);
                        videoView.setTag(info);
                        videoView.setId(R.id.video_view);
                    }
                    vinfo.setmUid(info.getUId());
                    vinfo.setmMediatype(info.getMediaType());
                    vinfo.setmEanbleVideo(info.isHasVideo());
                    vinfo.setEnableAudio(info.isHasAudio());
                    String mkey = info.getUId() + info.getMediaType().toString();
                    vinfo.setKey(mkey);
                    //默认输出，和外部输出代码二选一
                    Log.e(TAG,"s = " + s  + ",i = " + i + ",info = " + info.toString());
//                    info.setHasVideo(true);
//                    info.setHasAudio(true);
//                    info.setHasData(true);
                    rnMyVideoView.setBackgroundColor(Color.TRANSPARENT);
                    //进行媒体流渲染
                    rnMyVideoView.getVideoView().getSurfaceView().setNeedFullScreen(false);
                    sdkEngine.startRemoteView(info, rnMyVideoView.getVideoView(),UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT,null);

                }
            });

        }

        @Override
        public void onUnSubscribeResult(int i, String s, UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            //取消媒体订阅流

        }

        @Override
        public void onLocalStreamMuteRsp(int i, String s, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkTrackType uCloudRtcSdkTrackType, boolean b) {

        }

        @Override
        public void onRemoteStreamMuteRsp(int i, String s, String s1, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkTrackType uCloudRtcSdkTrackType, boolean b) {

        }

        @Override
        public void onRemoteTrackNotify(String s, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkTrackType uCloudRtcSdkTrackType, boolean b) {

        }

        @Override
        public void onSendRTCStats(UCloudRtcSdkStats uCloudRtcSdkStats) {

        }

        @Override
        public void onRemoteRTCStats(UCloudRtcSdkStats uCloudRtcSdkStats) {

        }

        @Override
        public void onLocalAudioLevel(int i) {

        }

        @Override
        public void onRemoteAudioLevel(String s, int i) {

        }

        @Override
        public void onKickoff(int i) {

        }

        @Override
        public void onWarning(int i) {

        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onRecordStart(int i, String s) {

        }

        @Override
        public void onRecordStop(int i) {

        }

        @Override
        public void onMixStart(int i, String s) {

        }

        @Override
        public void onMixStop(int i, String s) {

        }

        @Override
        public void onAddStreams(int i, String s) {

        }

        @Override
        public void onDelStreams(int i, String s) {

        }

        @Override
        public void onMsgNotify(int i, String s) {

        }

        @Override
        public void onServerBroadCastMsg(String s, String s1) {

        }

        @Override
        public void onAudioDeviceChanged(UCloudRtcSdkAudioDevice uCloudRtcSdkAudioDevice) {

        }

        @Override
        public void onPeerLostConnection(int i, UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {

        }
    };


}

