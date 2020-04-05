package com.ucloud_demo;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ucloud_demo.adapter.RemoteVideoAdapter;
import com.ucloud_demo.utils.CommonUtils;
import com.ucloud_demo.utils.ToastUtils;
import com.ucloud_demo.view.URTCVideoViewInfo;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEngine;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEnv;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkAudioDevice;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkAuthInfo;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkErrorCode;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMediaType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkRoomType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkScaleType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStats;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamInfo;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamRole;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkTrackType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkVideoProfile;
import com.ucloudrtclib.sdkengine.define.UcloudRtcSdkCaptureMode;
import com.ucloudrtclib.sdkengine.define.UcloudRtcSdkRecordProfile;
import com.ucloudrtclib.sdkengine.listener.UCloudRtcSdkEventListener;
import com.ucloudrtclib.sdkengine.openinterface.UcloudRTCDataProvider;
import com.ucloudrtclib.sdkengine.openinterface.UcloudRTCDataReceiver;
import com.ucloudrtclib.sdkengine.openinterface.UcloudRTCSceenShot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ucloudrtclib.sdkengine.define.UCloudRtcSdkErrorCode.NET_ERR_CODE_OK;
import static com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN;
import static com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO;


public class RoomActivity extends AppCompatActivity {
    private static final String TAG = "RoomActivity";

    private String mUserid = "test001";
    private String mRoomid = "urtc1";
    private String mRoomToken = "test token";
    private String mAppid = "";
    private String mBucket = "urtc-test";
    private String mRegion = "cn-bj";
    private boolean mIsRecording = false;
    private boolean mAtomOpStart = false;

    UCloudRtcSdkSurfaceVideoView localrenderview = null;
    ProgressBar localprocess = null;

    private RemoteVideoAdapter mVideoAdapter;
    UCloudRtcSdkEngine sdkEngine = null;

    ImageButton mHangup = null;
    ImageButton mSwitchcam = null;
    ImageButton mMuteMic = null;
    ImageButton mLoudSpkeader = null;
    ImageButton mMuteCam = null;
    int mCaptureMode;
    int mVideoProfile;
    @CommonUtils.PubScribeMode
    int mPublishMode;
    @CommonUtils.PubScribeMode
    int mScribeMode;
    UCloudRtcSdkStreamRole mRole;
    UCloudRtcSdkRoomType mClass;
    boolean isScreenCaptureSupport;
    private List<UCloudRtcSdkStreamInfo> mSteamList;
    private UCloudRtcSdkStreamInfo mLocalStreamInfo;
    private boolean mRemoteVideoMute;
    private boolean mRemoteAudioMute;
    private UCloudRtcSdkSurfaceVideoView mMuteView = null;
    private boolean mPFlag = false;
    private ArrayBlockingQueue<RGBSourceData> mQueue = new ArrayBlockingQueue(2);
    // 定义一个nv21 的
    private Thread mCreateImgThread;
    private boolean startCreateImg = true;

    enum BtnOp{
        OP_LOCAL_RECORD,
        OP_REMOTE_RECORD,
        OP_SEND_MSG
    }
    class RGBSourceData{
        Bitmap srcData;
        int width;
        int height;
        int type;

        public RGBSourceData(Bitmap srcData, int width, int height,int type) {
            this.srcData = srcData;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public Bitmap getSrcData() {
            return srcData;
        }

        public void setSrcData(Bitmap srcData) {
            this.srcData = srcData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getType() {
            return type;
        }
    }

    class NVSourceData{
        ByteBuffer srcData;
        int width;
        int height;
        int type;

        public NVSourceData(ByteBuffer srcData, int width, int height,int type) {
            this.srcData = srcData;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public ByteBuffer getSrcData() {
            return srcData;
        }

        public void setSrcData(ByteBuffer srcData) {
            this.srcData = srcData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getType() {
            return type;
        }
    }

    private UcloudRTCDataProvider mUcloudRTCDataProvider = new UcloudRTCDataProvider() {
        private ByteBuffer cacheBuffer;
        private RGBSourceData rgbSourceData;
        private NVSourceData nvSourceData;

        @Override
        public ByteBuffer provideRGBData(List<Integer> params) {
            rgbSourceData = mQueue.poll();
            if(rgbSourceData == null){
                Log.d(TAG, "provideRGBData: " + null);
                return null;
            }else{
                Log.d(TAG, "provideRGBData: ! = null");
                params.add(rgbSourceData.getType());
                params.add(rgbSourceData.getWidth());
                params.add(rgbSourceData.getHeight());
                if(cacheBuffer == null){
                    cacheBuffer = sdkEngine.getNativeOpInterface().
                            createNativeByteBuffer(2560*720*4);
                }else{
                    cacheBuffer.clear();
                }
                cacheBuffer.limit(rgbSourceData.getWidth()*rgbSourceData.getHeight()*4);
                rgbSourceData.getSrcData().copyPixelsToBuffer(cacheBuffer);
                recycleBitmap(rgbSourceData.getSrcData());
                rgbSourceData.srcData = null;
                rgbSourceData = null;
                Log.d("YUVCapture", "provideRGBData finish" + Thread.currentThread());
                cacheBuffer.position(0);
                return cacheBuffer;
            }
        }

        public void releaseBuffer(){
            if(rgbSourceData != null && !rgbSourceData.getSrcData().isRecycled()){
                rgbSourceData.getSrcData().recycle();
                rgbSourceData.srcData = null;
                rgbSourceData = null;
            }
            if(cacheBuffer != null){
                sdkEngine.getNativeOpInterface().realeaseNativeByteBuffer(cacheBuffer);
            }
        }
    };

    private UcloudRTCDataReceiver mUcloudRTCDataReceiver = new UcloudRTCDataReceiver() {
        private int limit = 0;
        private ByteBuffer cache;

        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(rgbBuffer);
            String name = "/mnt/sdcard/yuvrgba"+ limit+".jpg";
            if (limit++ < 5) {
                File file = new File(name);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getType() {
            return UcloudRTCDataReceiver.I420_TO_ABGR;
        }

        @Override
        public ByteBuffer getCacheBuffer() {
            if(cache == null){
                //根据需求来，设置最大的可能用到的buffersize，后续回调会复用这块内存
                int size = 4096*2160*4;
                cache = sdkEngine.getNativeOpInterface().
                        createNativeByteBuffer(4096*2160*4);
            }
            cache.clear();
            return cache;
        }

        @Override
        public void releaseBuffer() {
            if(cache != null)
            sdkEngine.getNativeOpInterface().realeaseNativeByteBuffer(cache);
            cache = null;
        }
    };

    private View.OnClickListener mSwapRemoteLocalListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof UCloudRtcSdkSurfaceVideoView) {
                String key = ((URTCVideoViewInfo) v.getTag(R.id.index)).getKey();
                if (mVideoAdapter.checkCanSwap(key)) {
                    boolean state = mVideoAdapter.checkState(key);
                    if (!state) {
                        UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) v.getTag();
                        sdkEngine.stopRemoteView(remoteStreamInfo);
                        if (mLocalStreamInfo != null) {
                            sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
                            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), (UCloudRtcSdkSurfaceVideoView) v);
                            v.setTag(R.id.swap_info, mLocalStreamInfo);
                        }
                        sdkEngine.startRemoteView(remoteStreamInfo, (UCloudRtcSdkSurfaceVideoView) localrenderview);
                        ((UCloudRtcSdkSurfaceVideoView) v).refreshRemoteOp(View.INVISIBLE);
                        ((UCloudRtcSdkSurfaceVideoView) localrenderview).refreshRemoteOp(View.VISIBLE);
                        localrenderview.setTag(R.id.swap_info, remoteStreamInfo);
                        if (mClass == UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_LARGE) {
                            localrenderview.setVisibility(View.VISIBLE);
                            localrenderview.setTag(R.id.view_info, v);
                            localrenderview.setBackgroundColor(Color.TRANSPARENT);
                            v.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        //有交换过
                        UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) v.getTag();
                        //停止交换过的大窗渲染远端
                        sdkEngine.stopRemoteView(remoteStreamInfo);
                        //停止本地视频渲染
                        sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
                        sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview);
                        sdkEngine.startRemoteView(remoteStreamInfo, (UCloudRtcSdkSurfaceVideoView) v);
                        ((UCloudRtcSdkSurfaceVideoView) v).refreshRemoteOp(View.VISIBLE);
                        ((UCloudRtcSdkSurfaceVideoView) localrenderview).refreshRemoteOp(View.INVISIBLE);
                        v.setTag(R.id.swap_info, null);
                        localrenderview.setTag(R.id.swap_info, null);
                    }
                    mVideoAdapter.reverseState(key);
                } else {
                    ToastUtils.shortShow(RoomActivity.this, "其它窗口已经交换过，请先交换回来");
                }
            }
        }
    };

    private View.OnClickListener mScreenShotOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             addScreenShotCallBack((UCloudRtcSdkSurfaceVideoView)v);
        }
    };

    private UCloudRtcSdkSurfaceVideoView.RemoteOpTrigger mOnRemoteOpTrigger = new UCloudRtcSdkSurfaceVideoView.RemoteOpTrigger() {
        @Override
        public void onRemoteVideo(View v, UCloudRtcSdkSurfaceVideoView parent) {
            if (parent.getTag(R.id.swap_info) != null) {
                UCloudRtcSdkStreamInfo swapStreamInfo = (UCloudRtcSdkStreamInfo) parent.getTag(R.id.swap_info);
                sdkEngine.muteRemoteVideo(swapStreamInfo.getUId(), !mRemoteVideoMute);
            } else if (parent.getTag() != null) {
                UCloudRtcSdkStreamInfo streamInfo = (UCloudRtcSdkStreamInfo) parent.getTag();
                sdkEngine.muteRemoteVideo(streamInfo.getUId(), !mRemoteVideoMute);
            }
            mMuteView = parent;
        }

        @Override
        public void onRemoteAudio(View v, UCloudRtcSdkSurfaceVideoView parent) {
            if (parent.getTag(R.id.swap_info) != null) {
                UCloudRtcSdkStreamInfo swapStreamInfo = (UCloudRtcSdkStreamInfo) parent.getTag(R.id.swap_info);
                sdkEngine.muteRemoteAudio(swapStreamInfo.getUId(), !mRemoteAudioMute);
            } else if (parent.getTag() != null) {
                UCloudRtcSdkStreamInfo streamInfo = (UCloudRtcSdkStreamInfo) parent.getTag();
                sdkEngine.muteRemoteAudio(streamInfo.getUId(), !mRemoteAudioMute);
            }
            mMuteView = parent;
        }
    };

    private RemoteVideoAdapter.RemoveRemoteStreamReceiver mRemoveRemoteStreamReceiver = new RemoteVideoAdapter.RemoveRemoteStreamReceiver() {
        @Override
        public void onRemoteStreamRemoved(boolean swaped) {
            if (swaped) {
                if (mClass == UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_SMALL) {
                    sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
                    sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview);
                } else if (localrenderview.getTag(R.id.swap_info) != null) {
                    UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) localrenderview.getTag(R.id.swap_info);
                    sdkEngine.stopRemoteView(remoteStreamInfo);
                }
            }
        }
    };


    UCloudRtcSdkEventListener eventListener = new UCloudRtcSdkEventListener() {
        @Override
        public void onServerDisconnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 服务器已断开");
                    onMediaServerDisconnect();
                }
            });
        }

        @Override
        public void onJoinRoomResult(int code, String msg, String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        ToastUtils.shortShow(RoomActivity.this, " 加入房间成功");
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, " 加入房间失败 " +
                                code + " errmsg " + msg);
                        onMediaServerDisconnect();
                        finish();
                    }

                }
            });
        }

        @Override
        public void onLeaveRoomResult(int code, String msg, String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 离开房间 " +
                            code + " errmsg " + msg);
//                    Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
                    onMediaServerDisconnect();
//                    startActivity(intent);
//                    finish();
                }
            });
        }

        @Override
        public void onRejoiningRoom(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "rejoining room");
                    ToastUtils.shortShow(RoomActivity.this, " 服务器重连中…… ");
                }
            });
        }

        @Override
        public void onRejoinRoomResult(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, "服务器重连成功");
                }
            });
        }

        @Override
        public void onLocalPublish(int code, String msg, UCloudRtcSdkStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        int mediatype = info.getMediaType().ordinal();
                        if (mediatype == UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO.ordinal()) {
                            if (!sdkEngine.isAudioOnlyMode()) {
                                localrenderview.setBackgroundColor(Color.TRANSPARENT);
                                sdkEngine.startPreview(info.getMediaType(),
                                        localrenderview);
                                mLocalStreamInfo = info;
                                localrenderview.setTag(mLocalStreamInfo);
                                localrenderview.refreshRemoteOp(View.INVISIBLE);
                                //和交换大小功能触发重叠，App使用者可以另行定义触发
                                localrenderview.setOnClickListener(mScreenShotOnClickListener);
                            }

                        } else if (mediatype == UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN.ordinal()) {
                            if (mCaptureMode == CommonUtils.screen_capture_mode) {
                                sdkEngine.startPreview(info.getMediaType(), localrenderview);
                            }
                        }

                    } else {
                        ToastUtils.shortShow(RoomActivity.this,
                                "发布视频失败 " + code + " errmsg " + msg);
                    }

                }
            });
        }

        @Override
        public void onLocalUnPublish(int code, String msg, UCloudRtcSdkStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        if (info.getMediaType() == UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO) {
                            if (localrenderview != null) {
                                localrenderview.refresh();
                            }
                        } else if (info.getMediaType() == UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN) {
                            if (mCaptureMode == CommonUtils.screen_capture_mode) {
                                if (localrenderview != null) {
                                    localrenderview.refresh();
                                }
                            }
                        }
                        ToastUtils.shortShow(RoomActivity.this, "取消发布视频成功");
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, "取消发布视频失败 "
                                + code + " errmsg " + msg);
                    }
                }
            });
        }

        @Override
        public void onRemoteUserJoin(String uid) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 用户 "
                            + uid + " 加入房间 ");
                }
            });
        }

        @Override
        public void onRemoteUserLeave(String uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "remote user " + uid + "leave ,reason: " + reason);
                    onUserLeave(uid);
                    ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                            uid + " 离开房间，离开原因： " + reason);
                }
            });
        }

        @Override
        public void onRemotePublish(UCloudRtcSdkStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //特殊情况下，譬如客户端在断网情况下离开房间，服务端可能还持有流，并没有超时，客户端就会收到自己的userid,
                    // 如果客户端是固定userid就可以过滤掉，如果不是，等待服务端超时也会删除流
                    Log.d(TAG, "onRemotePublish: " + info.getUId() + " me : " + mUserid);
                    if(!mUserid.equals(info.getUId())){
                        mSteamList.add(info);
                        if (sdkEngine.isAutoSubscribe()) {
                            sdkEngine.subscribe(info);
                        } else {
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoteUnPublish(UCloudRtcSdkStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, " onRemoteUnPublish " + info.getMediaType() + " " + info.getUId());
                    ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                            info.getUId() + " 取消媒体流 " + info.getMediaType());
                    String mkey = info.getUId() + info.getMediaType().toString();
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(mkey);
                    }

                }
            });
        }

        @Override
        public void onSubscribeResult(int code, String msg, UCloudRtcSdkStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        URTCVideoViewInfo vinfo = new URTCVideoViewInfo(null);
                        UCloudRtcSdkSurfaceVideoView videoView = new UCloudRtcSdkSurfaceVideoView(getApplicationContext());;

                        Log.d(TAG, " subscribe info: " + info.getUId() + " hasvideo " + info.isHasVideo());
                        if (info.isHasVideo()) {
//                            videoView.init(false, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio});
                            videoView.init(false);
                            videoView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
                            vinfo.setmRenderview(videoView);
                            videoView.setTag(info);
                            videoView.setId(R.id.video_view);
                            //设置交换
//                          videoView.setOnClickListener(mSwapRemoteLocalListener);
                            //远端截图
                            videoView.setOnClickListener(mScreenShotOnClickListener);
                        }
                        vinfo.setmUid(info.getUId());
                        vinfo.setmMediatype(info.getMediaType());
                        vinfo.setmEanbleVideo(info.isHasVideo());
                        vinfo.setEnableAudio(info.isHasAudio());
                        String mkey = info.getUId() + info.getMediaType().toString();
                        vinfo.setKey(mkey);
                        //默认输出，和外部输出代码二选一
                        if (mVideoAdapter != null) {
                            mVideoAdapter.addStreamView(mkey, vinfo, info);
                        }
                        if (vinfo != null && videoView != null) {
                            sdkEngine.startRemoteView(info, videoView);
                            videoView.refreshRemoteOp(View.VISIBLE);
                        }
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, " 订阅用户  " +
                                info.getUId() + " 流 " + info.getMediaType() + " 失败 " +
                                " code " + code + " msg " + msg);
                    }
                }
            });
        }

        @Override
        public void onUnSubscribeResult(int code, String msg, UCloudRtcSdkStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 取消订阅用户 " +
                            info.getUId() + " 类型 " + info.getMediaType());
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(info.getUId() + info.getMediaType().toString());
                    }
                }
            });
        }

        @Override
        public void onLocalStreamMuteRsp(int code, String msg, UCloudRtcSdkMediaType mediatype, UCloudRtcSdkTrackType tracktype, boolean mute) {
            Log.d(TAG, " code " + code + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        if (mediatype == UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO) {
                            if (tracktype == UCloudRtcSdkTrackType.UCLOUD_RTC_SDK_TRACK_TYPE_AUDIO) {
                                onMuteMicResult(mute);
                            } else if (tracktype == UCloudRtcSdkTrackType.UCLOUD_RTC_SDK_TRACK_TYPE_VIDEO) {
                                onMuteCamResult(mute);
                            }
                        } else if (mediatype == UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN) {
                            onMuteCamResult(mute);
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoteStreamMuteRsp(int code, String msg, String uid, UCloudRtcSdkMediaType mediatype, UCloudRtcSdkTrackType tracktype, boolean mute) {
            Log.d(TAG, " code " + code + " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        String mkey = uid + mediatype.toString();
                        Log.d(TAG, " onRemoteStreamMuteRsp " + mkey + " " + mVideoAdapter);
                        if (tracktype == UCloudRtcSdkTrackType.UCLOUD_RTC_SDK_TRACK_TYPE_AUDIO) {
                            mRemoteAudioMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteAudio(mute);
                            }
                            if (mMuteView == localrenderview) {
                            } else {
                                localrenderview.refreshRemoteAudio(mute);
                            }
                        } else if (tracktype == UCloudRtcSdkTrackType.UCLOUD_RTC_SDK_TRACK_TYPE_VIDEO) {
                            mRemoteVideoMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteVideo(mute);
                            }
                            if (mMuteView == localrenderview) {
                            } else {
                                localrenderview.refreshRemoteVideo(mute);
                            }
                        }

                    } else {
                        ToastUtils.shortShow(RoomActivity.this, "mute " + mediatype + "failed with code: " + code);
                    }
                }
            });
        }

        @Override
        public void onRemoteTrackNotify(String uid, UCloudRtcSdkMediaType mediatype, UCloudRtcSdkTrackType tracktype, boolean mute) {
            Log.d(TAG, " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediatype == UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO) {
                        String cmd = mute ? "关闭" : "打开";
                        if (tracktype == UCloudRtcSdkTrackType.UCLOUD_RTC_SDK_TRACK_TYPE_AUDIO) {
                            ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                    uid + cmd + " 麦克风");
                        } else if (tracktype == UCloudRtcSdkTrackType.UCLOUD_RTC_SDK_TRACK_TYPE_VIDEO) {
                            ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                    uid + cmd + " 摄像头");
                        }

                    } else if (mediatype == UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN) {
                        String cmd = mute ? "关闭" : "打开";
                        ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                uid + cmd + " 桌面流");
                    }
                }
            });
        }

        @Override
        public void onSendRTCStats(UCloudRtcSdkStats rtstats) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onRemoteRTCStats(UCloudRtcSdkStats rtstats) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onLocalAudioLevel(int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onRemoteAudioLevel(String uid, int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mVideoAdapter != null) {
                        String mkey = uid + UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO.toString();
                    }
                }
            });
        }

        @Override
        public void onKickoff(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RoomActivity.this, " 被踢出会议 code " +
                            code);
                    Log.d(TAG, " user kickoff reason " + code);
                    onMediaServerDisconnect();
                    finish();
                }
            });
        }

        @Override
        public void onWarning(int warn) {

        }

        @Override
        public void onError(int error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error == UCloudRtcSdkErrorCode.NET_ERR_SDP_SWAP_FAIL.ordinal()) {
                        ToastUtils.shortShow(RoomActivity.this, "sdp swap failed");
                    }
                }
            });
        }

        @Override
        public void onRecordStart(int code,String fileName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(code == NET_ERR_CODE_OK.ordinal()){
                        String videoPath = "http://"+ mBucket + "."+ mRegion +".ufileos.com/" + fileName+".mp4";
                        Log.d(TAG,videoPath);
                        ToastUtils.longShow(RoomActivity.this, "观看地址: " +videoPath );
                        mIsRecording = true;
                        if(mAtomOpStart)
                            mAtomOpStart = false;
                    }else{
                        ToastUtils.longShow(RoomActivity.this, "录制开始失败: 原因：" +code );
                    }
                }
            });
        }

        @Override
        public void onRecordStop(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RoomActivity.this, "录制结束: " + (code == NET_ERR_CODE_OK.ordinal()?"成功":"失败: "+ code));
                    if(mIsRecording){
                        mIsRecording = false;
                    }
                }
            });
        }

        @Override
        public void onMsgNotify(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onMsgNotify: code: " + code + "msg: " + msg);
                }
            });
        }

        @Override
        public void onServerBroadCastMsg(String uid, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, ": uid: " + uid + "msg: " + msg);
                }
            });
        }

        @Override
        public void onAudioDeviceChanged(UCloudRtcSdkAudioDevice device) {
            defaultAudioDevice = device;
//            URTCLogUtils.d(TAG,"URTCAudioManager: room change device to "+ defaultAudioDevice);
            if (defaultAudioDevice == UCloudRtcSdkAudioDevice.UCLOUD_RTC_SDK_AUDIODEVICE_SPEAKER) {
                mSpeakerOn = true;
            } else {
                mSpeakerOn = false;
            }
        }
    };
    private int mSelectPos;

    private void onUserLeave(String uid) {
        if (mVideoAdapter != null) {
            mVideoAdapter.removeStreamView(uid + UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
            mVideoAdapter.removeStreamView(uid + UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN);
        }
    }

    private void onMediaServerDisconnect() {
        localrenderview.release();
        clearGridItem();
//        UCloudRtcSdkEngine.destory();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
//        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);



        mCaptureMode = 1;
        mVideoProfile = 1;


        mVideoAdapter = new RemoteVideoAdapter(this);
        mVideoAdapter.setRemoveRemoteStreamReceiver(mRemoveRemoteStreamReceiver);


        sdkEngine = UCloudRtcSdkEngine.createEngine(eventListener);



        mUserid = getIntent().getStringExtra("user_id");
        mRoomid = getIntent().getStringExtra("room_id");
        mRoomToken = getIntent().getStringExtra("token");
        mAppid = getIntent().getStringExtra("app_id");


        mHangup = findViewById(R.id.button_call_disconnect);
        mSwitchcam = findViewById(R.id.button_call_switch_camera);
        mMuteMic = findViewById(R.id.button_call_toggle_mic);
        mLoudSpkeader = findViewById(R.id.button_call_loundspeaker);
        mMuteCam = findViewById(R.id.button_call_toggle_cam);



        mSteamList = new ArrayList<>();
        //手动发布
        mHangup.setOnClickListener(v -> callHangUp());

        mSwitchcam.setOnClickListener(v -> switchCamera());

        mMuteMic.setOnClickListener(v -> onToggleMic());

        mLoudSpkeader.setOnClickListener(v ->
                onLoudSpeaker(!mSpeakerOn));

        mMuteCam.setOnClickListener(v -> onToggleCamera());


        localrenderview = findViewById(R.id.localview);
        localrenderview.init(true);
//        localrenderview.init(true, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio});
        localrenderview.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
        localrenderview.setZOrderMediaOverlay(false);
        localrenderview.setMirror(false);
        localprocess = findViewById(R.id.processlocal);
        isScreenCaptureSupport = UCloudRtcSdkEnv.isSuportScreenCapture();
        switch (mCaptureMode) {
            case CommonUtils.audio_capture_mode:
                sdkEngine.setAudioOnlyMode(true);
                sdkEngine.configLocalCameraPublish(false);
                sdkEngine.configLocalAudioPublish(true);
                sdkEngine.configLocalScreenPublish(false);
                break;
            case CommonUtils.camera_capture_mode:
                sdkEngine.configLocalCameraPublish(true);
                sdkEngine.configLocalAudioPublish(true);
                sdkEngine.configLocalScreenPublish(false);
                break;
            case CommonUtils.screen_capture_mode:
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(false);
                } else {
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                    sdkEngine.configLocalScreenPublish(false);
                }
                break;
            case CommonUtils.screen_Audio_mode:
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(true);
                } else {
                    sdkEngine.configLocalScreenPublish(false);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(true);
                }
                break;
            case CommonUtils.multi_capture_mode:
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                } else {
                    sdkEngine.configLocalScreenPublish(false);
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                }
                break;
        }

        defaultAudioDevice = sdkEngine.getDefaultAudioDevice();
        if (defaultAudioDevice == UCloudRtcSdkAudioDevice.UCLOUD_RTC_SDK_AUDIODEVICE_SPEAKER) {
            mSpeakerOn = true;
        } else {
            mSpeakerOn = false;
        }
        int role = UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_BOTH.ordinal();
        mRole = UCloudRtcSdkStreamRole.valueOf(role);
        sdkEngine.setStreamRole(mRole);
        int classType = UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_SMALL.ordinal();
        mClass = UCloudRtcSdkRoomType.valueOf(classType);
        sdkEngine.setClassType(mClass);
        mPublishMode = 1;
        //mPublishMode == CommonUtils.AUTO_MODE ? true : false
        sdkEngine.setAutoPublish(true);
        mScribeMode = 1;
        sdkEngine.setAutoSubscribe(mScribeMode == CommonUtils.AUTO_MODE ? true : false);
        //设置sdk 外部扩展模式及其采集的帧率，同时sdk内部会自动调整初始码率和最小码率
        //扩展模式只支持720p的分辨率及以下，若要自定义更高分辨率，请联系UCloud商务定制，否则sdk会抛出异常，终止运行。
//        sdkEngine.setVideoProfile(UCloudRtcSdkVideoProfile.UCLOUD_RTC_SDK_VIDEO_PROFILE_EXTEND.extendParams(30,640,480));
        sdkEngine.setVideoProfile(UCloudRtcSdkVideoProfile.matchValue(mVideoProfile));
        initButtonSize();
        UCloudRtcSdkAuthInfo info = new UCloudRtcSdkAuthInfo();
        info.setAppId(mAppid);
        info.setToken(mRoomToken);
        info.setRoomId(mRoomid);
        info.setUId(mUserid);
        Log.d(TAG, " roomtoken = " + mRoomToken);
        //普通摄像头捕获方式，与扩展模式二选一
        UCloudRtcSdkEnv.setCaptureMode(
                UcloudRtcSdkCaptureMode.UCLOUD_RTC_CAPTURE_MODE_LOCAL);

        Runnable imgTask = new Runnable() {
            @Override
            public void run() {
                    while(startCreateImg){
                        try{
                            RGBSourceData sourceData;
                            Bitmap bitmap = null;
                            int type;
                            if(mPFlag){
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                type = UcloudRTCDataProvider.RGBA_TO_I420;
                            }
                            else{
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                type = UcloudRTCDataProvider.RGBA_TO_I420;
                            }
                            mPFlag = !mPFlag;
                            if(bitmap != null){
                                sourceData = new RGBSourceData(bitmap,bitmap.getWidth(),bitmap.getHeight(),type);
                                //add rgbdata
                                mQueue.put(sourceData);
                            }
                             Thread.sleep((int)(Math.random()*20));
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                    //这里在回收一遍 防止队列不阻塞了在destroy以后又产生了bitmap没回收
                    while(mQueue.size() != 0 ){
                        RGBSourceData rgbSourceData = mQueue.poll();
                        if(rgbSourceData != null){
                            recycleBitmap(rgbSourceData.getSrcData());
                            rgbSourceData.srcData = null;
                            rgbSourceData = null;
                        }
                    }
            }
        };

        if(UCloudRtcSdkEnv.getCaptureMode() == UcloudRtcSdkCaptureMode.UCLOUD_RTC_CAPTURE_MODE_EXTEND &&
                (mRole == UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_BOTH ||
                        mRole == UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_PUB)){

            mCreateImgThread = new Thread(imgTask);
            mCreateImgThread.setName("create picture");
            mCreateImgThread.start();
            UCloudRtcSdkEngine.onRGBCaptureResult(mUcloudRTCDataProvider);
        }

        sdkEngine.joinChannel(info);
    }

    private void recycleBitmap(Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
    }

    private void addScreenShotCallBack(UCloudRtcSdkSurfaceVideoView view){
        view.setScreenShotBack(new UcloudRTCSceenShot() {
            @Override
            public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
                final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(rgbBuffer);
                String name = "/mnt/sdcard/urtcscreen_"+System.currentTimeMillis() +".jpg";
                File file = new File(name);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "screen shoot : " + name);
                ToastUtils.shortShow(RoomActivity.this,"screen shoot : " + name);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        boolean hasSwap = false;
        for (String key : mVideoAdapter.getStreamViews().keySet()) {
            URTCVideoViewInfo info = mVideoAdapter.getStreamViews().get(key);
            UCloudRtcSdkSurfaceVideoView videoView = info.getmRenderview();
            UCloudRtcSdkStreamInfo videoViewStreamInfo = (UCloudRtcSdkStreamInfo) videoView.getTag();
            UCloudRtcSdkStreamInfo videoViewSwapStreamInfo = (UCloudRtcSdkStreamInfo) videoView.getTag(R.id.swap_info);
            if (videoView != null && videoViewStreamInfo != null) {
                if (videoViewSwapStreamInfo != null) {
                    //恢复交换后的小窗本地视频
                    sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), videoView);
                    //恢复交换后的大窗远程视频
                    sdkEngine.startRemoteView(videoViewStreamInfo, localrenderview);
                    hasSwap = true;
                } else {
                    sdkEngine.startRemoteView(videoViewStreamInfo, videoView);
                }
            }
        }
        if (!hasSwap) {
            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview);
//            sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop");
        for (String key : mVideoAdapter.getStreamViews().keySet()) {
            URTCVideoViewInfo info = mVideoAdapter.getStreamViews().get(key);
            UCloudRtcSdkSurfaceVideoView videoView = info.getmRenderview();
            UCloudRtcSdkStreamInfo videoViewStreamInfo = (UCloudRtcSdkStreamInfo) videoView.getTag();
            if (videoView != null && videoViewStreamInfo != null) {
                sdkEngine.stopRemoteView(videoViewStreamInfo);
            }
        }
        if (mLocalStreamInfo != null)
            sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
    }




    private void initButtonSize() {
        int screenWidth = UiHelper.getScreenPixWidth(this);
        int leftRightMargin = UiHelper.dipToPx(this, 30 * 2);
        int gap = UiHelper.dipToPx(this, 8);
        int buttonSize;
        if (mPublishMode == CommonUtils.AUTO_MODE) {
            buttonSize = (screenWidth - leftRightMargin - gap * 4) / 5;
        } else {
            buttonSize = (screenWidth - leftRightMargin - gap * 5) / 6;
        }
        setButtonSize(mHangup, buttonSize);
        setButtonSize(mLoudSpkeader, buttonSize);
        setButtonSize(mSwitchcam, buttonSize);
        setButtonSize(mMuteCam, buttonSize);
        setButtonSize(mMuteMic, buttonSize);
    }

    private void setButtonSize(View button, int buttonSize) {
        button.getLayoutParams().width = buttonSize;
        button.getLayoutParams().height = buttonSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "activity destory");
        super.onDestroy();
        localrenderview.release();
        clearGridItem();
        mVideoAdapter.setRemoveRemoteStreamReceiver(null);
        mUcloudRTCDataProvider.releaseBuffer();
        mUcloudRTCDataProvider = null;
        mUcloudRTCDataReceiver.releaseBuffer();
        mUcloudRTCDataReceiver = null;
        if(UCloudRtcSdkEnv.getCaptureMode() == UcloudRtcSdkCaptureMode.UCLOUD_RTC_CAPTURE_MODE_EXTEND &&
                (mRole == UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_BOTH ||
                        mRole == UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_PUB)) {
            startCreateImg = false;
            //这里回收一遍
            while(mQueue.size() != 0 ){
                RGBSourceData rgbSourceData = mQueue.poll();
                if(rgbSourceData != null){
                    recycleBitmap(rgbSourceData.getSrcData());
                    rgbSourceData.srcData = null;
                    rgbSourceData = null;
                }

            }
        }
        UCloudRtcSdkEngine.destory();
        System.gc();
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private void callHangUp() {
        int ret = sdkEngine.leaveChannel().ordinal();
//        if (ret != NET_ERR_CODE_OK.ordinal()) {
            onMediaServerDisconnect();
            finish();
//        }
    }

    boolean mSwitchCam = false;

    private void switchCamera() {
        sdkEngine.switchCamera();
        ToastUtils.shortShow(this, "切换摄像头");
//        mSwitchcam.setImageResource(mSwitchCam ? R.mipmap.camera_switch_front :
//                R.mipmap.camera_switch_end);
        mSwitchCam = !mSwitchCam;
    }

    boolean mMuteMicBool = false;

    private boolean onToggleMic() {
        sdkEngine.muteLocalMic(!mMuteMicBool);
        if (!mMuteMicBool) {
            ToastUtils.shortShow(RoomActivity.this, "关闭麦克风");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开麦克风");
        }
        return false;
    }

    boolean mMuteCamBool = false;

    private boolean onToggleCamera() {
        if (mCaptureMode == CommonUtils.camera_capture_mode) {
            sdkEngine.muteLocalVideo(!mMuteCamBool, UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        } else if (mCaptureMode == CommonUtils.screen_capture_mode) {
            if (isScreenCaptureSupport) {
                sdkEngine.muteLocalVideo(!mMuteCamBool, UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideo(!mMuteCamBool, UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
            }
        } else if (mCaptureMode == CommonUtils.multi_capture_mode) {
            sdkEngine.muteLocalVideo(!mMuteCamBool, UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        }
        if (!mMuteCamBool) {
            ToastUtils.shortShow(RoomActivity.this, "关闭摄像头");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开摄像头");
        }
        return false;
    }

    private void onMuteCamResult(boolean mute) {
        mMuteCamBool = mute;
        if (localrenderview.getTag(R.id.swap_info) != null) {
            UCloudRtcSdkStreamInfo remoteInfo = (UCloudRtcSdkStreamInfo) localrenderview.getTag(R.id.swap_info);
            String mkey = remoteInfo.getUId() + remoteInfo.getMediaType().toString();
        } else {
            if (mute) {
                localrenderview.refresh();
                localrenderview.setVisibility(View.INVISIBLE);
            } else {
                localrenderview.setVisibility(View.VISIBLE);
            }
        }

    }

    private void onMuteMicResult(boolean mute) {
        mMuteMicBool = mute;
    }

    boolean mSpeakerOn = true;
    UCloudRtcSdkAudioDevice defaultAudioDevice;

    private void onLoudSpeaker(boolean enable) {
        if (mSpeakerOn) {
            ToastUtils.shortShow(RoomActivity.this, "关闭喇叭");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开喇叭");
        }
        mSpeakerOn = !mSpeakerOn;
        sdkEngine.setSpeakerOn(enable);
    }

    private void clearGridItem() {
        mVideoAdapter.clearAll();
        mVideoAdapter.notifyDataSetChanged();
    }


}
