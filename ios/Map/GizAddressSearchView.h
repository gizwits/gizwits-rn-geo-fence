//
//  GizAddressSearchView.h
//  TestGeofencing
//
//  Created by Minus🍀 on 2018/11/27.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol GizAddressSearchViewDelegate;

@interface GizAddressSearchView : UIView

@property (nonatomic, strong, nullable) UIColor *themeColor;

@property (nonatomic, weak, nullable) id<GizAddressSearchViewDelegate> delegate;

@property (nonatomic, strong, nullable) NSString *placeholder;
@property (nonatomic, strong, nullable) NSString *text;
@property (nonatomic, strong, nullable) UIColor* textColor;

@end


@protocol GizAddressSearchViewDelegate <NSObject>

@optional

- (void)searchViewDidBeginEditing:(GizAddressSearchView *)searchView;
- (void)searchViewDidEndEditing:(GizAddressSearchView *)searchView;
- (void)searchView:(GizAddressSearchView *)searchView didUpdateText:(nullable NSString *)text;

@end


NS_ASSUME_NONNULL_END
