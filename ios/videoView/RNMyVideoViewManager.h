//
//  RNMyVideoViewManager.h
//  RNMyLibrary
//
//  Created by developer on 2020/3/26.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <React/RCTViewManager.h>

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

NS_ASSUME_NONNULL_BEGIN

@interface RNMyVideoViewManager : RCTViewManager <RCTBridgeModule>

@end

NS_ASSUME_NONNULL_END
