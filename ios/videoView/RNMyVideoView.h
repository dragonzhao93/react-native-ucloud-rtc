//
//  RNMyVideoView.h
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <UIKit/UIKit.h>


NS_ASSUME_NONNULL_BEGIN

@interface RNMyVideoView : UIView


+ (instancetype)sharedView;

// 释放View
+ (void)releaseView;
@end

NS_ASSUME_NONNULL_END
