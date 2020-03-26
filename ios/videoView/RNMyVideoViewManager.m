//
//  RNMyVideoViewManager.m
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RNMyVideoViewManager.h"
#import "RNMyVideoView.h"
#import <UIKit/UIKit.h>

@interface RNMyVideoViewManager ()
@property (nonatomic, strong) RNMyVideoView *videoView;
@end

@implementation RNMyVideoViewManager


- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(RNMyVideoView)

//
//RCT_CUSTOM_VIEW_PROPERTY(userid, NSString, RNMyVideoView) {
//   view.userid = [RCTConvert NSString:json];
//}
//
//RCT_CUSTOM_VIEW_PROPERTY(roomid, NSString, RNMyVideoView) {
//   view.roomid = [RCTConvert NSString:json];
//}
//
//RCT_CUSTOM_VIEW_PROPERTY(token, NSString, RNMyVideoView) {
//   view.token = [RCTConvert NSString:json];
//}
//
//RCT_CUSTOM_VIEW_PROPERTY(appkey, NSString, RNMyVideoView) {
//   view.appkey = [RCTConvert NSString:json];
//}
//
//RCT_CUSTOM_VIEW_PROPERTY(appid, NSString, RNMyVideoView) {
//   view.appid = [RCTConvert NSString:json];
//}

- (UIView *)view {
    RNMyVideoView *videoView = [RNMyVideoView sharedView];
    NSLog(@"view初始化");
    return videoView;
}






@end
