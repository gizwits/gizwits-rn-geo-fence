//
//  GizPickRegionViewController.h
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/22.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN


typedef void (^GizPickRegionCompletionHandler)(NSDictionary * _Nullable dict, BOOL canceled);


@interface GizPickRegionNavigationController : UINavigationController

@end


@interface GizPickRegionViewController : UIViewController

@property (nonatomic, strong, nullable) UIColor *themeColor;
@property (nonatomic, strong, nullable) NSString *rightButtonTitle;
@property (nonatomic, strong, nullable) NSString *searchPlaceholder;

/**
 选择区域或地址。YES = 选择区域，NO = 选择地址。
 */
@property (nonatomic, assign) BOOL pickingRegion;

/**
 1. 选取新区域时，传 nil；编辑区域则传入对应的区域信息。格式：{"radius": 200, "latitude": 32.123456, "longitude": 122.123456}
 2. 选取新地址，传 nil；编辑地址则传入对应的地址。格式：{"latitude": 32.123456, "longitude": 122.123456}
 */
@property (nonatomic, strong, nullable) NSDictionary *regionDict;

@property (nonatomic, copy, nullable) GizPickRegionCompletionHandler completionHandler;

@end

NS_ASSUME_NONNULL_END
