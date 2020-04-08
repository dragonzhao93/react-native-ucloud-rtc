package com.ucloud_demo;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.ucloud_demo.utils.CommonUtils;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEnv;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkLogLevel;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMode;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

//import com.facebook.react.PackageList;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost =
      new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
          return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
//          @SuppressWarnings("UnnecessaryLocalVariable")
//          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // packages.add(new MyReactNativePackage());
//            packages.add(new MainReactPackage());
//            packages.add(new RNMyLibraryPackage());
            return Arrays.<ReactPackage>asList(
//                    new MainReactPackage(),
//                    new RNMyLibraryPackage()
            );
//            return Arrays.<ReactPackage>asList(
//            new UcloudPackage()
//      );
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }
      };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    initializeFlipper(this); // Remove this line if you don't want Flipper enabled
      initUCloudSDK();
  }


  private void initUCloudSDK(){
      UCloudRtcSdkEnv.initEnv(getApplicationContext(), this);
      UCloudRtcSdkEnv.setWriteToLogCat(true);
      UCloudRtcSdkEnv.setLogLevel(UCloudRtcSdkLogLevel.UCLOUD_RTC_SDK_LogLevelInfo);
      UCloudRtcSdkEnv.setSdkMode(UCloudRtcSdkMode.UCLOUD_RTC_SDK_MODE_TRIVAL);
      UCloudRtcSdkEnv.setTokenSeckey(CommonUtils.SEC_KEY);
      WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
      DisplayMetrics outMetrics = new DisplayMetrics();
      windowManager.getDefaultDisplay().getMetrics(outMetrics);
      CommonUtils.mItemWidth = (outMetrics.widthPixels - UiHelper.dipToPx(this, 15)) / 3;
      CommonUtils.mItemHeight = CommonUtils.mItemWidth;
  }
  /**
   * Loads Flipper in React Native templates.
   *
   * @param context
   */
  private static void initializeFlipper(Context context) {
    if (BuildConfig.DEBUG) {
      try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
        Class<?> aClass = Class.forName("com.facebook.flipper.ReactNativeFlipper");
        aClass.getMethod("initializeFlipper", Context.class).invoke(null, context);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }



}
