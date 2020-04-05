package com.ucloud_demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;

import java.util.UUID;

public class RoomViewActivity extends ReactActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
    }

    private void startRoomActivity(String mRoomid,String mUserId,String mAppid,String mRoomToken){
        Intent intent = new Intent(this, RoomViewActivity.class);
        intent.putExtra("room_id", mRoomid);
        String autoGenUserId = "android_"+ UUID.randomUUID().toString().replace("-", "");
        intent.putExtra("user_id", mUserId);
        intent.putExtra("app_id", mAppid);
        intent.putExtra("token", mRoomToken);
        Handler mMainHandler = new Handler(Looper.getMainLooper());

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        },500);
    }

}
