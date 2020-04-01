//
//  RNMyVideoView.m
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RNMyVideoView.h"

@implementation RNMyVideoView


static RNMyVideoView *instance;
static dispatch_once_t onceToken;
+ (instancetype)sharedView {
    dispatch_once(&onceToken, ^{
        BOOL isMainThread = [NSThread isMainThread];
        if (isMainThread) {
          instance = [[self alloc] init];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
               instance = [[self alloc] init];
            });
        }
    });
    return instance;
}


#pragma mark - willMoveToSuperview
-(void) willMoveToSuperview:(UIView *)newSuperview {
  [super willMoveToSuperview:newSuperview];
}

// 释放View
+ (void)releaseView {
    onceToken = 0;
    instance = nil;
}

@end
