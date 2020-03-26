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
UCloudRtc.initWithAppid(appid, appKey)
```
#### 加入房间 joinRoomWithRoomid
```
UCloudRtc.joinRoomWithRoomid(roomId, userId, token);
```
#### 离开房间
```
UCloudRtc.leaveRoom();
```