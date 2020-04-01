//
//  RNMyLib.m
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RNMyLib.h"

#import "RNMyVideoView.h"


@interface RNMyLib ()<UCloudRtcEngineDelegate>
/// 可以订阅的远程流
@property (nonatomic, strong) NSMutableArray<UCloudRtcStream*> *canSubstreamList;
/// 已订阅的远程流
@property (nonatomic, strong) NSMutableArray<UCloudRtcStream*> *substreamList;
/// 目标流
@property (nonatomic, strong) UCloudRtcStream *targetStream;

// 初始化
/// appid
@property (nonatomic, copy) NSString *appid;
/// appkey
@property (nonatomic, copy) NSString *appkey;
/// 初始化成功的回调
@property (nonatomic, copy) RCTPromiseResolveBlock initResolve;
/// 初始化失败的回调
@property (nonatomic, copy) RCTPromiseRejectBlock initReject;

@end


@implementation RNMyLib{
    BOOL _hasListeners;
}

+(id)allocWithZone:(NSZone *)zone {
  static RNMyLib *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [super allocWithZone:zone];
  });
  return sharedInstance;
}

+(instancetype)sharedLib {
    static id instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (dispatch_queue_t)methodQueue{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

/**
 @brief 初始化UCloudRtcEngine
 @param appid 分配得到的应用ID
 @param appKey 分配得到的appkey
 */
RCT_EXPORT_METHOD(initWithAppid:(NSString *)appid andAppkey:(NSString *)appkey andResolve:(RCTPromiseResolveBlock)resolve
andReject:(RCTPromiseRejectBlock)reject){
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    sharedLib.appid = appid;
    sharedLib.appkey = appkey;
    sharedLib.initResolve = resolve;
    sharedLib.initReject = reject;
    sharedLib.engine = [[UCloudRtcEngine alloc]initWithAppID:appid appKey:appkey completionBlock:^(int errorCode) {
        if (errorCode) {
            reject(@(errorCode).stringValue,@"init fail", nil);
        } else {
            resolve(@"init success");
        }
    }];
    sharedLib.engine.delegate = sharedLib;
}

/**
 @brief 加入房间
 @param roomid 即将加入的房间ID
 @param userid 当前用户的ID
 @param token  生成的token
*/
RCT_EXPORT_METHOD(joinRoomWithRoomid:(NSString *)roomid andUserid:(NSString *)userid andToken:(NSString *)token andResolve:(RCTPromiseResolveBlock)resolve
andReject:(RCTPromiseRejectBlock)reject){
    //设置本地预览模式：等比缩放填充整View，可能有部分被裁减
    [[RNMyLib sharedLib].engine setPreviewMode:UCloudRtcVideoViewModeScaleAspectFill];
    //设置远端预览模式：等比缩放填充整View，可能有部分被裁减
    [[RNMyLib sharedLib].engine setRemoteViewMode:UCloudRtcVideoViewModeScaleAspectFill];
    [[RNMyLib sharedLib].engine joinRoomWithRoomId:roomid userId:userid token:token completionHandler:^(NSDictionary * _Nonnull response, int errorCode) {
        NSLog(@"response:%@",response);
        NSLog(@"errorCode:%d",errorCode);
        if (errorCode) { // 加入房间失败
            reject(@(errorCode).stringValue,@"joinRoom fail", nil);
        } else { //加入房间成功
            resolve(@"joinRoom success");
        }
    }];
}

/**
 @brief 退出房间
*/
RCT_EXPORT_METHOD(leaveRoom){
    [[RNMyLib sharedLib].engine leaveRoom];
    // 释放engine
    [RNMyLib sharedLib].engine = nil;
}

/**
 @brief 订阅远程流
*/
RCT_EXPORT_METHOD(subscribeRemoteStream){
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine subscribeMethod:sharedLib.targetStream];
    NSLog(@"subscribeRemoteStream");
}

/**
 @brief 取消订阅远程流
*/
RCT_EXPORT_METHOD(unSubscribeRemoteStream){
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine unSubscribeMethod:sharedLib.targetStream];
}

/**
 @brief 发布本地流
 @param cameraEnable设置本地流是否启用相机(YES为音视频  NO是纯音频)
*/
RCT_EXPORT_METHOD(subscribeLocalStreamWithCameraEnable:(BOOL)cameraEnable){
    [[RNMyLib sharedLib].engine openCamera:cameraEnable];
    [[RNMyLib sharedLib].engine publish];
    [[RNMyLib sharedLib].engine setLocalPreview:[RNMyVideoView sharedView]];
}

/**
 @brief 取消发布本地流
*/
RCT_EXPORT_METHOD(unSubscribeLocalStream) {
    [[RNMyLib sharedLib].engine unPublish];
}

/**
 @brief 录制
 @param type 录制类型 1音频 2 视频 3 音视频
*/
RCT_EXPORT_METHOD(startRecordLocalStreamWithType:(NSInteger)type) {
    UCloudRtcRecordConfig *config = [UCloudRtcRecordConfig new];
    config.mimetype = type;
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine startRecord:config];
}

/**
@brief 停止录制
*/
RCT_EXPORT_METHOD(stopRecordLocalStream) {
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine stopRecord];
}

#pragma mark - UCloudRtcEngineDelegate

/**收到远程流*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager receiveRemoteStream:(UCloudRtcStream *_Nonnull)stream{
    
    BOOL isMainThread = [NSThread isMainThread];
    if (isMainThread) {
      [stream renderOnView:[RNMyVideoView sharedView]];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            // 渲染到指定视图
            [stream renderOnView:[RNMyVideoView sharedView]];
        });
    }
}

/**非自动订阅模式下 可订阅流加入*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)channel newStreamHasJoinRoom:(UCloudRtcStream *_Nonnull)stream {
    [RNMyLib sharedLib].targetStream = stream;
    // 渲染到指定视图
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.targetStream renderOnView:[RNMyVideoView sharedView]];
}

/**流 状态回调*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager didReceiveStreamStatus:(NSArray<UCloudRtcStreamStatsInfo*> *_Nonnull)status{
    for (int i = 0 ; i < status.count; i ++) {
        UCloudRtcStreamStatsInfo *info = status[i];
        if ([info isKindOfClass:[UCloudRtcStreamStatsInfo class]]) {
            NSLog(@"streamInfo:  streamId = %@   userId = %@",info.streamId,info.userId);
        } else {
           NSLog(@"streamInfo: %@",info);
        }
    }
}

/**流 连接失败*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager streamConnectionFailed:(NSString *_Nonnull)streamId {
    // 流链接失败,临时自定义错误码
    [RNMyLib showMessageWithCode:900001 andMessage:@"流链接失败"];
}

/**错误的回调*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager error:(UCloudRtcError *_Nonnull)error {
    [RNMyLib showMessageWithCode:error.code andMessage:error.message];
}

/**新成员加入*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager memberDidJoinRoom:(NSDictionary *_Nonnull)memberInfo{
    // 发送事件
    [self sendEventWithName:@"event_memberDidJoinRoom" body:memberInfo];
}

/**成员退出*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager memberDidLeaveRoom:(NSDictionary *_Nonnull)memberInfo{
    // 发送事件
    [self sendEventWithName:@"event_memberDidLeaveRoom" body:memberInfo];
}

#pragma mark - 事件配置
- (NSArray<NSString *> *)supportedEvents {
    // 事件注册
    return @[
      @"event_memberDidJoinRoom",
      @"event_memberDidLeaveRoom"
    ];
}

- (void)startObserving {
  _hasListeners = YES;
}

- (void)stopObserving {
  _hasListeners = NO;
}

#pragma mark - 懒加载engine
- (UCloudRtcEngine *)engine {
    if (_engine) {
        UCloudRtcEngine *engine = [[UCloudRtcEngine alloc]initWithAppID:self.appid appKey:self.appkey completionBlock:^(int errorCode) {
            if (errorCode) {
                if (!self.initReject) {
                    return;
                }
                self.initReject(@(errorCode).stringValue,@"init fail", nil);
            } else {
                if (!self.initResolve) {
                    return;
                }
                self.initResolve(@"init success");
            }
        }];
        engine.delegate = self;
        _engine = engine;
    }
    return _engine;
}



#pragma mark - 异常提示
+ (void)showMessageWithCode:(NSInteger)code andMessage:(NSString *)message {
    
    NSString *alertMessage = [NSString stringWithFormat:@"错误码:%ld\n:错误信息:%@",(long)code,message];
    
    UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:@"提示" message:alertMessage preferredStyle:UIAlertControllerStyleAlert];
    
    // 确认
    UIAlertAction *confirmAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [alertVC dismissViewControllerAnimated:YES completion:nil];
    }];
    
    // 取消
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [alertVC dismissViewControllerAnimated:YES completion:nil];
    }];
    
    [alertVC addAction:confirmAction];
    [alertVC addAction:cancelAction];
    
    [[UIApplication sharedApplication].delegate.window.rootViewController presentViewController:alertVC animated:YES completion:nil];
}





@end

