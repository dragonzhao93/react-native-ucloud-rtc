package com.ucloud_demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
        mLocalView = new UCloudRtcSdkSurfaceVideoView(mContext.getApplicationContext());
        mLocalView.init(true);
        mLocalView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
        mLocalView.setId(R.id.video_view);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,UiHelper.dipToPx(mContext, ViewGroup.LayoutParams.MATCH_PARENT));

        mLocalView.setLayoutParams(params);
        this.addView(mLocalView);

    }

    public UCloudRtcSdkSurfaceVideoView getVideoView(){
        return mLocalView;
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
