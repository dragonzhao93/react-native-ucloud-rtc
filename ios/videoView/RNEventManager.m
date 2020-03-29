//
//  RNEventManager.m
//  RNMyLibrary
//
//  Created by developer on 2020/3/28.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RNEventManager.h"

@implementation RNEventManager

RCT_EXPORT_MODULE();

+(instancetype)sharedEvent {
    static id instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}


/// 向RN发送事件
/// @param name 事件名
/// @param body 参数
+(void)sendEventWithName:(NSString *)name andBody:(id)body {
    [[RNEventManager sharedEvent] sendEventWithName:name body:body];
}
@end
