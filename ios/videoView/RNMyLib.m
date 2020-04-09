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
/// 展示预览图
@property (nonatomic, strong) UIView *localPreview;
///  是否开启本地摄像头
@property (nonatomic, assign) BOOL cameraEnable;

@property (nonatomic, strong) NSString *appid,*appkey,*roomid,*userid,*token;
@end


@implementation RNMyLib

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
@param isDebug 是否打印日志开关  YES开  NO关
@param roomType 房间类型  0小班课、视频会议、默认值  1大班课
@param streamProfile 权限  0上传权限  1下载权限  2所有权限 默认值
*/
RCT_EXPORT_METHOD(initWithAppid:(NSString *)appid andAppkey:(NSString *)appkey andDebug:(BOOL)isDebug andRoomType:(NSInteger)roomType andStreamProfile:(NSInteger)streamProfile andResolve:(RCTPromiseResolveBlock)resolve
andReject:(RCTPromiseRejectBlock)reject){
    self.appid = appid;
    self.appkey = appkey;
    NSLog(@"sdk版本号：%@",[UCloudRtcEngine currentVersion]);
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    if (sharedLib.engine) {
        sharedLib.engine = nil;
    }
    if (nil==sharedLib.engine) {
        sharedLib.engine = [[UCloudRtcEngine alloc]initWithAppID:appid appKey:appkey completionBlock:^(int errorCode) {
            if (errorCode && reject) {
                reject(@(errorCode).stringValue,@"init fail", nil);
            } else {
                if (!resolve) {
                    return;
                }
                resolve(@"init success");
            }
        }];
    }
    sharedLib.engine.streamProfile = streamProfile;
    sharedLib.engine.roomType = roomType;
    sharedLib.engine.delegate = sharedLib;
    sharedLib.engine.isAutoPublish = NO;
    sharedLib.engine.isAutoSubscribe = YES;
    //打印日志开关
    UCloudRtcLog *logger = [UCloudRtcLog new];
    LogLevel logLevel = isDebug?UCloudRtcLogLevel_DEBUG:UCloudRtcLogLevel_OFF;
    [logger setLogLevel:logLevel];
    sharedLib.engine.logger = logger;
}

/**
 @brief 加入房间
 @param roomid 即将加入的房间ID
 @param userid 当前用户的ID
 @param token  生成的token
*/
RCT_EXPORT_METHOD(joinRoomWithRoomid:(NSString *)roomid andUserid:(NSString *)userid andToken:(NSString *)token andResolve:(RCTPromiseResolveBlock)resolve
andReject:(RCTPromiseRejectBlock)reject){
    self.roomid = roomid;
    self.userid = userid;
    self.token = token;
    //设置本地预览模式：等比缩放填充整View，可能有部分被裁减
    [[RNMyLib sharedLib].engine setPreviewMode:UCloudRtcVideoViewModeScaleAspectFill];
    //设置远端预览模式：等比缩放填充整View，可能有部分被裁减
    [[RNMyLib sharedLib].engine setRemoteViewMode:UCloudRtcVideoViewModeScaleAspectFill];
    [[RNMyLib sharedLib].engine joinRoomWithRoomId:roomid userId:userid token:token completionHandler:^(NSDictionary * _Nonnull response, int errorCode) {
        NSLog(@"response:%@",response);
        NSLog(@"errorCode:%d",errorCode);
        if (errorCode && reject){ // 加入房间失败
            reject(@(errorCode).stringValue,@"joinRoom fail", nil);
        }else{ //加入房间成功
            if (!resolve){
                return;
            }
            resolve(@"joinRoom success");
        }
    }];
}

/**
 @brief 退出房间
*/
RCT_EXPORT_METHOD(leaveRoom){
    [[RNMyLib sharedLib].engine leaveRoom];
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
RCT_EXPORT_METHOD(publishLocalStreamWithCameraEnable:(BOOL)cameraEnable){
    [RNMyLib sharedLib].cameraEnable = cameraEnable;
    [[RNMyLib sharedLib].engine publish];
    [[RNMyLib sharedLib].engine setLocalPreview:self.localPreview];
}

/**
 @brief 取消发布本地流
*/
RCT_EXPORT_METHOD(unPublishLocalStream) {
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
        // 渲染到指定视图
        [self renderView:stream];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            // 渲染到指定视图
            [self renderView:stream];
        });
    }
}

/**非自动订阅模式下 可订阅流加入*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)channel newStreamHasJoinRoom:(UCloudRtcStream *_Nonnull)stream {
    // 渲染到指定视图
    [stream renderOnView:[RNMyVideoView sharedView]];
}

 /**流 状态回调*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager didReceiveStreamStatus:(NSArray<UCloudRtcStreamStatsInfo*> *_Nonnull)status {
    for (int i = 0 ; i < status.count; i ++) {
        UCloudRtcStreamStatsInfo *info = status[i];
        if ([info isKindOfClass:[UCloudRtcStreamStatsInfo class]]) {
            NSString *userid = info.userId;
            NSString *volume = [NSString stringWithFormat:@"%ld",(long)info.volume];
            if ([userid isKindOfClass:[NSString class]]) {
                [self libSendEventWithName:@"event_remoteVolumeChange" andParams:@{@"volume":volume,@"userid":info.userId}];
            }
            
        } else {
           NSLog(@"streamInfo: %@",info);
        }
    }
}
 
 /**发送事件*/
- (void)libSendEventWithName:(NSString *)name andParams:(NSDictionary *)params {
    [self sendEventWithName:name body:params];
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

/**发布状态的变化*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager didChangePublishState:(UCloudRtcEnginePublishState)publishState {
    NSLog(@"发布状态：%ld",(long)publishState);
    if (publishState == UCloudRtcEnginePublishStatePublishSucceed) {
        NSLog(@"设置是否禁用摄像头");
        [[RNMyLib sharedLib].engine openCamera:self.cameraEnable];
    }
}

#pragma mark - 懒加载view
- (UIView*)localPreview {
    if (!_localPreview) {
        UIView *localPreview = [UIView new];
        _localPreview = localPreview;
    }
    return _localPreview;
}

#pragma mark - 事件配置
- (NSArray<NSString *> *)supportedEvents {
    // 事件注册
    return @[
      @"event_memberDidJoinRoom",
      @"event_memberDidLeaveRoom",
      @"event_remoteVolumeChange"
    ];
}

/**渲染视图
 *[RNMyVideoView sharedView]  父视图，
 *view 子视图
 *渲染前移除父视图上所有子视图
 *初始化view加到父视图上，远端流渲染到子视图上
 */
-(void)renderView:(UCloudRtcStream *_Nonnull)stream{
    if ([[RNMyVideoView sharedView] subviews] != 0) {
        [[[RNMyVideoView sharedView] subviews]makeObjectsPerformSelector:@selector(removeFromSuperview)];
    }
    UIView *view = [[UIView alloc]init];
    view.frame = [RNMyVideoView sharedView].bounds;
    [[RNMyVideoView sharedView] addSubview:view];
    [stream renderOnView:view];
    
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

