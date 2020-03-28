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
UCloudRtc.initWithAppid(appid, appKey).then(res => {
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
 subscribeRemoteStream
 ```
#### 取消订阅远程流
 ```
 unSubscribeRemoteStream
 ```
#### 发布本地流
 ```
 subscribeLocalStream
 ```
#### 取消发布本地流
 ```
 unSubscribeLocalStream
 ```
#### 录制音视频
 ```
 startRecordLocalStreamWithType
 ```
#### 停止录制
 ```
 stopRecordLocalStream
```