//
//  RNEventManager.h
//  RNMyLibrary
//
//  Created by developer on 2020/3/28.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

NS_ASSUME_NONNULL_BEGIN

@interface RNEventManager : RCTEventEmitter <RCTBridgeModule>
//+(instancetype)sharedEvent;

/// 向RN发送事件
/// @param name 事件名
/// @param body 参数
+(void)sendEventWithName:(NSString *)name andBody:(id)body;
@end

NS_ASSUME_NONNULL_END
