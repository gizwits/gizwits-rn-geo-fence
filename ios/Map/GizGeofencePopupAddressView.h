//
//  GizGeofencePopupAddressView.h
//  TestGeofencing
//
//  Created by Minus🍀 on 2018/11/23.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface GizGeofencePopupAddressView : UIView

@property (nonatomic, strong) NSString *address;

- (instancetype)initWithAddress:(nullable NSString *)address;

- (void)layoutAtPoint:(CGPoint)point animated:(BOOL)animated;
- (void)showWithAnimated:(BOOL)animated;
- (void)hideWithAnimated:(BOOL)animated;

@end

NS_ASSUME_NONNULL_END
