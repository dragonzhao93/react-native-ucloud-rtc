package com.ucloud_demo;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;

public class UCloudVideoViewManager extends SimpleViewManager<RNMyVideoView> {
    @NonNull
    @Override
    public String getName() {
        return "RNMyVideoView";
    }

    @NonNull
    @Override
    protected RNMyVideoView createViewInstance(@NonNull ThemedReactContext reactContext) {

//        LinearLayout linearLayout = new LinearLayout(reactContext);
//        linearLayout.setBackgroundColor(Color.BLACK);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
//        linearLayout.setLayoutParams(params);
//        return linearLayout;
        return RNMyLibraryModule.rnMyVideoView;
    }

    /**
     * 接收传输的颜色参数
     */
    @ReactProp(name = "color")
    public void setColor(View view, String color) {
        view.setBackgroundColor(Color.parseColor(color));
    }


}
