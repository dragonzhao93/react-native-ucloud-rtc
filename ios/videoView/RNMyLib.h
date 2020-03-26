//
//  RNMyLib.h
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import <UCloudRtcSdk_ios/UCloudRtcSdk_ios.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNMyLib : NSObject<RCTBridgeModule>
/// 音视频实例
@property (nonatomic, strong) UCloudRtcEngine *engine;

+(instancetype)sharedLib;


#pragma mark - 公共接口

@end

NS_ASSUME_NONNULL_END
