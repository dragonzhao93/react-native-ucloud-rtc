# react-native-ucloud-rtc

## 安装使用

Install with npm:

 `npm install --save react-native-ucloud-rtc`

Or, install with yarn:

 `yarn add react-native-ucloud-rtc`

Either way, then link with:

 `react-native link react-native-ucloud-rtc`

 ## React-Native 调用

 ```
 import UCloudRtc from 'react-native-ucloud-rtc';

// 显示播放器
const RNMyVideoView = requireNativeComponent('RNMyVideoView');

...
...

UCloudRtc.initWithAppid(appid, appKey);

...
...

    <View>
      <RNMyVideoView style={styles.localVideoStyle} />
    </View>   
...
```
## API
#### 初始化 initWithAppid
```
appid: appid,
appKey: appKey,
isDebug: 是否开启日志, true-开启、false-关闭
roomType: 0小班课、视频会议、默认值  1大班课    
streamProfile: 权限  0上传权限  1下载权限  2所有权限 默认值
UCloudRtc.initWithAppid(appid, appKey, isDebug, roomType, streamProfile).then(res => {
    console.log('收到回调', res);
  }).catch(err => {
    console.log('捕获异常', err);
  });
```
#### 加入房间 joinRoomWithRoomid
```
UCloudRtc.joinRoomWithRoomid(roomId, userId, token).then(res => {
    console.log('收到回调', res);
  }).catch(err => {
    console.log('捕获异常', err);
  });
```
#### 离开房间
```
UCloudRtc.leaveRoom();
```
#### 订阅远程流
 ```
 UCloudRtc.subscribeRemoteStream()
 ```
#### 取消订阅远程流
 ```
 UCloudRtc.unSubscribeRemoteStream()
 ```
#### 发布本地流
 ```
 flag: 是否开启摄像头 true-推送音视频流 false-仅推送音频
 UCloudRtc.publishLocalStreamWithCameraEnable(flag)
 ```
#### 取消发布本地流
 ```
 UCloudRtc.unPublishLocalStream()
 ```
#### 录制音视频
 ```
 UCloudRtc.startRecordLocalStreamWithType()
 ```
#### 停止录制
 ```
 UCloudRtc.stopRecordLocalStream()
```

### 事件监听
```
  event_memberDidJoinRoom：用户进入房间
  event_memberDidLeaveRoom：用户离开房间
  event_remoteVolumeChange：声音变化回调

  const UCloudRtcEventEmitter = new NativeEventEmitter(UCloudRtc);

  
  UCloudRtcEventEmitter.addListener('event_memberDidJoinRoom', args => {
    console.log('事件event_memberDidJoinRoom', args);
  });
  UCloudRtcEventEmitter.addListener('event_memberDidLeaveRoom', args => {
    console.log('事件event_memberDidLeaveRoom', args);
  });
  UCloudRtcEventEmitter.addListener('event_remoteVolumeChange', args => {
    console.log('事件event_remoteVolumeChange', args);
  });
```