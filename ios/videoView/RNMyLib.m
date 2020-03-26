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
RCT_EXPORT_METHOD(initWithAppid:(NSString *)appid andAppkey:(NSString *)appkey){
    
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    sharedLib.engine = [[UCloudRtcEngine alloc]initWithAppID:appid appKey:appkey completionBlock:^(int errorCode) {
        
    }];
    sharedLib.engine.delegate = sharedLib;
    //[[sharedLib.engine] setPreviewMode:UCloudRtcVideoViewModeScaleAspectFit];
}

/// 加入房间
RCT_EXPORT_METHOD(joinRoomWithRoomid:(NSString *)roomid andUserid:(NSString *)userid andToken:(NSString *)token){
    
    [[RNMyLib sharedLib].engine joinRoomWithRoomId:roomid userId:userid token:@"" completionHandler:^(NSDictionary * _Nonnull response, int errorCode) {
        
        [[RNMyLib sharedLib].engine setLocalPreview:[RNMyVideoView sharedView]];
        
        [[RNMyLib sharedLib].engine.localStream renderOnView:[RNMyVideoView sharedView]];
        NSLog(@"response:%@",response);
        NSLog(@"errorCode:%d",errorCode);
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



#pragma mark - UCloudRtcEngineDelegate

/**收到远程流*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager receiveRemoteStream:(UCloudRtcStream *_Nonnull)stream{
    [RNMyLib sharedLib].targetStream = stream;
    // 渲染到指定视图
    [[RNMyLib sharedLib].targetStream renderOnView:[RNMyVideoView sharedView]];
}

/**非自动订阅模式下 可订阅流加入*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)channel newStreamHasJoinRoom:(UCloudRtcStream *_Nonnull)stream {
    [RNMyLib sharedLib].targetStream = stream;
    // 渲染到指定视图
    RNMyLib *sharedLib = [RNMyLib sharedLib];
    [sharedLib.targetStream renderOnView:[RNMyVideoView sharedView]];
}


/**流 状态回调*/
- (void)uCloudRtcEngine:(UCloudRtcEngine *_Nonnull)manager didReceiveStreamStatus:(NSArray<UCloudRtcStreamStatsInfo*> *_Nonnull)status; {
    for (int i = 0 ; i < status.count; i ++) {
        UCloudRtcStreamStatsInfo *info = status[i];
        NSLog(@"streamInfo:  streamId = %@   userId = %@",info.streamId,info.userId);
    }
}

@end
