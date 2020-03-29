//
//  RNMyLib.m
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RNMyLib.h"

#import "RNMyVideoView.h"
//#import "RNEventManager.h"

@interface RNMyLib ()<UCloudRtcEngineDelegate>
/// 可以订阅的远程流
@property (nonatomic, strong) NSMutableArray<UCloudRtcStream*> *canSubstreamList;
/// 已订阅的远程流
@property (nonatomic, strong) NSMutableArray<UCloudRtcStream*> *substreamList;
/// 目标流
@property (nonatomic, strong) UCloudRtcStream *targetStream;
/// 是否开启事件监听
@property (nonatomic, assign) BOOL hasListeners;
@end


@implementation RNMyLib
+(instancetype)sharedLib {
    static id instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}


RCT_EXPORT_MODULE()


/// 通过appid和appkey初始化
/// @param appkey
RCT_EXPORT_METHOD(initWithAppid:(NSString *)appid andAppkey:(NSString *)appkey andResolve:(RCTPromiseResolveBlock)resolve
andReject:(RCTPromiseRejectBlock)reject){
    
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    sharedLib.engine = [[UCloudRtcEngine alloc]initWithAppID:appid appKey:appkey completionBlock:^(int errorCode) {
        if (errorCode) {
            reject(@(errorCode).stringValue,@"init fail", nil);
        } else {
            resolve(@"init success");
        }
    }];
    sharedLib.engine.isAutoPublish = NO;//加入房间后将自动发布本地音视频 默认为YES
    sharedLib.engine.delegate = sharedLib;
    //[[sharedLib.engine] setPreviewMode:UCloudRtcVideoViewModeScaleAspectFit];
}

/// 加入房间
RCT_EXPORT_METHOD(joinRoomWithRoomid:(NSString *)roomid andUserid:(NSString *)userid andToken:(NSString *)token andResolve:(RCTPromiseResolveBlock)resolve
andReject:(RCTPromiseRejectBlock)reject){
    //[RNMyLib sharedLib].engine.isAutoSubscribe = NO;//取消自动订阅
    [[RNMyLib sharedLib].engine joinRoomWithRoomId:roomid userId:userid token:@"" completionHandler:^(NSDictionary * _Nonnull response, int errorCode) {
        NSLog(@"join room suceess!!!");
       // [[RNMyLib sharedLib].engine setLocalPreview:[RNMyVideoView sharedView]];
        //[[RNMyLib sharedLib].engine.localStream renderOnView:[RNMyVideoView sharedView]];
        NSLog(@"response:%@",response);
        NSLog(@"errorCode:%d",errorCode);
        if (errorCode) { // 加入房间失败
            reject(@(errorCode).stringValue,@"joinRoom fail", nil);
        } else { //加入房间成功
            resolve(@"joinRoom success");
        }
    }];
}

/// 离开房间
RCT_EXPORT_METHOD(leaveRoom){
    [[RNMyLib sharedLib].engine leaveRoom];
}

/// 订阅远程流
RCT_EXPORT_METHOD(subscribeRemoteStream){
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine subscribeMethod:sharedLib.targetStream];
    // 渲染到指定视图
    [sharedLib.targetStream renderOnView:[RNMyVideoView sharedView]];
    NSLog(@"subscribeRemoteStream and  render");
}

/// 取消订阅远程流
RCT_EXPORT_METHOD(unSubscribeRemoteStream){
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine unSubscribeMethod:sharedLib.targetStream];
}

/// 发布本地流
RCT_EXPORT_METHOD(subscribeLocalStream) {
    [[RNMyLib sharedLib].engine publish];
}

/// 取消发布本地流
RCT_EXPORT_METHOD(unSubscribeLocalStream) {
    [[RNMyLib sharedLib].engine unPublish];
}

/// //录制类型  1 音频 2 视频 3 音频+视频
RCT_EXPORT_METHOD(startRecordLocalStreamWithType:(NSInteger)type) {
    UCloudRtcRecordConfig *config = [UCloudRtcRecordConfig new];
    config.mimetype = type;
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine startRecord:config];
}


/// 停止录制
RCT_EXPORT_METHOD(stopRecordLocalStream) {
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.engine stopRecord];
}


#pragma mark - UCloudRtcEngineDelegate

/**收到远程流*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager receiveRemoteStream:(UCloudRtcStream *_Nonnull)stream{
    [RNMyLib sharedLib].targetStream = stream;
    
    BOOL isMainThread = [NSThread isMainThread];
    if (isMainThread) {
      [sharedLib.targetStream renderOnView:[RNMyVideoView sharedView]];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            // 需要在主线程执行的代码
            // 渲染到指定视图
            [[RNMyLib sharedLib].targetStream renderOnView:[RNMyVideoView sharedView]];
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
        //NSLog(@"streamInfo:  streamId = %@   userId = %@",info.streamId,info.userId);
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
    //新成员加入提示
//    NSString *message = [NSString stringWithFormat:@"用户:%@ 加入房间",memberInfo[@"user_id"]];
//    [RNMyLib showMessageWithCode:900002 andMessage:message];
    
    // 发送事件
    [self sendEventWithName:@"event_memberDidJoinRoom" body:memberInfo];
}

/**成员退出*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager memberDidLeaveRoom:(NSDictionary *_Nonnull)memberInfo{
    //新成员加入提示
//    NSString *message = [NSString stringWithFormat:@"用户:%@ 加入房间",memberInfo[@"user_id"]];
//    [RNMyLib showMessageWithCode:900003 andMessage:message];
    
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
  hasListeners = YES;
}

- (void)stopObserving {
  hasListeners = NO;
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
