package com.ucloud_demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ucloud_demo.R;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkScaleType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;


class RNMyVideoView extends LinearLayout {
    private Context mContext;
    private  UCloudRtcSdkSurfaceVideoView mLocalView;

    private static RNMyVideoView mInstance;


    public static RNMyVideoView getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RNMyVideoView.class) {
                if (mInstance == null) {
                    mInstance = new RNMyVideoView(context);
                }
            }
        }
        return mInstance;
    }



    public RNMyVideoView(Context context){
        super(context);
        this.mContext = context;
        init();
        mLocalView = new UCloudRtcSdkSurfaceVideoView(mContext.getApplicationContext());
        mLocalView.init(true);
        mLocalView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
        mLocalView.setId(R.id.video_view);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,UiHelper.dipToPx(mContext,200));

        mLocalView.setLayoutParams(params);
        this.addView(mLocalView);

    }
    private void init(){

//        initView();
    }

    public UCloudRtcSdkSurfaceVideoView getVideoView(){
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(new Runnable() {
//
//                         @Override
//                         public void run() {
////                             mLocalView = new UCloudRtcSdkSurfaceVideoView(mContext.getApplicationContext());
//                             mLocalView.init(true);
//                             mLocalView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
//                             mLocalView.setId(R.id.video_view);
//                             //远端截图
//                             mLocalView.setOnClickListener(null);
//                         }
//                     });

        return mLocalView;
    }

    private void initView(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                mLocalView.init(true);
//        mLocalView.init(true, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio});
                mLocalView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
                mLocalView.setZOrderMediaOverlay(false);
                mLocalView.setMirror(false);
            }
        });
    }


    //以下代码修复通过动态 addView 后看不到的问题

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

}
