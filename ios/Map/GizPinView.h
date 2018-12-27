//
//  GizPinView.h
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/23.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN


@class GizPinView;

@protocol GizPinViewDelegate <NSObject>

@optional

- (void)didTapPinView:(GizPinView *)pinView;

@end


@interface GizPinView : UIView

/// 主题色
@property (nonatomic, strong) UIColor *themeColor;

@property (nonatomic, weak) id<GizPinViewDelegate> delegate;

+ (instancetype)pinView;

@end


// 需要将 RegionView 和 pinView 拆分开来，因为 pinView 需要点击事件，
// 而 RegionView 的 userInteractionEnabled 需要设置为 NO，不然就会截获手势，
// 导致拖动不了地图。


@interface GizRegionBackgroundView : UIView

/// 主题色
@property (nonatomic, strong) UIColor *themeColor;

/// 半径，单位: 米
@property (nonatomic, assign) NSInteger radius;

+ (instancetype)regionBackgroundView;

@end

NS_ASSUME_NONNULL_END
