//
//  GizCoordinateTransform.h
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/24.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 坐标系转换。
 WGS-84：是国际标准，GPS坐标（Google Earth使用、或者GPS模块）
 GCJ-02：中国坐标偏移标准，Google Map、高德、腾讯使用 (火星坐标系)
 BD-09 ：百度坐标偏移标准，Baidu Map使用
 */
@interface GizCoordinateTransform : NSObject

+ (CLLocationCoordinate2D)transformFromWGSToGCJ:(CLLocationCoordinate2D)wgsCoordinate;
+ (CLLocationCoordinate2D)transformFromGCJToBaidu:(CLLocationCoordinate2D)gcjCoordinate;
+ (CLLocationCoordinate2D)transformFromBaiduToGCJ:(CLLocationCoordinate2D)baiduCoordinate;
+ (CLLocationCoordinate2D)transformFromGCJToWGS:(CLLocationCoordinate2D)gcjCoordinate;

@end

NS_ASSUME_NONNULL_END
