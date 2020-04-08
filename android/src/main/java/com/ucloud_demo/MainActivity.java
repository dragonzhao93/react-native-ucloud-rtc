package com.ucloud_demo;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.ReactActivity;
import com.ucloud_demo.utils.CommonUtils;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEnv;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkLogLevel;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMode;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "ucloud_demo";
  }
}
