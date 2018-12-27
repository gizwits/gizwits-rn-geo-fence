//
//  GizGeofenceNetworking.h
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/28.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

NS_ASSUME_NONNULL_BEGIN

@interface GizGeofenceNetworking : NSObject

+ (instancetype)sharedInstance;

- (void)enterRegion:(CLCircularRegion *)region completionHandler:(void (^)(NSError * _Nullable error))completionHandler;
- (void)leaveRegion:(CLCircularRegion *)region completionHandler:(void (^)(NSError * _Nullable error))completionHandler;
- (void)enterOrLeaveRegion:(CLCircularRegion *)region status:(NSString *)status completionHandler:(void (^)(NSError * _Nullable error))completionHandler;

@end

NS_ASSUME_NONNULL_END
