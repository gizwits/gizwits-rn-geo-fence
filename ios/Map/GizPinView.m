//
//  GizPinView.m
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/23.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import "GizPinView.h"

#define GizRegionViewWidth 250

CGRect CGRectMakeWithCenter(CGPoint center, CGFloat radius) {
    return CGRectMake(center.x - radius, center.y - radius, radius * 2, radius * 2);;
}


@interface GizRadiusLabel : UILabel

@end

@implementation GizRadiusLabel

- (CGSize)intrinsicContentSize {
    CGSize size = [super intrinsicContentSize];
    size.width += 14;
    size.height += 4;
    return size;
}

@end


@interface GizPinView ()

@property (nonatomic, strong) CAShapeLayer *pinLayer;

- (void)initSubViews;
- (void)createPinLayer;
- (void)updateView;

@end

@implementation GizPinView

+ (instancetype)pinView {
    GizPinView *view = [[GizPinView alloc] initWithFrame:CGRectMake(0, 0, 50, 50)];
    return view;
}

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        self.userInteractionEnabled = YES;
        _themeColor = [UIColor orangeColor];
        
        [self initSubViews];
        
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(actionTapPinView:)];
        [self addGestureRecognizer:tap];
    }
    
    return self;
}

- (void)setThemeColor:(UIColor *)themeColor {
    _themeColor = themeColor;
    
    [self updateView];
}

- (void)initSubViews {
    [self createPinLayer];
}

- (void)createPinLayer {
    CGFloat radius = CGRectGetWidth(self.frame) / 2.0;
    
    CGPoint center = CGPointMake(radius, radius);
    
    // 标点背景
    CAShapeLayer *pinBgLayer = [CAShapeLayer layer];
    pinBgLayer.frame = self.layer.bounds;
    pinBgLayer.fillColor = [UIColor whiteColor].CGColor;
    pinBgLayer.strokeColor = [UIColor whiteColor].CGColor;
    pinBgLayer.shadowColor = [UIColor grayColor].CGColor;
    pinBgLayer.shadowOpacity = 1.0;
    pinBgLayer.shadowOffset = CGSizeMake(0, 0);
    
    UIBezierPath *whiteCirclePath = [UIBezierPath bezierPathWithOvalInRect:CGRectMakeWithCenter(center, 13)];
    pinBgLayer.path = whiteCirclePath.CGPath;
    [self.layer addSublayer:pinBgLayer];
    
    // 标点
    self.pinLayer = [CAShapeLayer layer];
    self.pinLayer.frame = self.layer.bounds;
    self.pinLayer.fillColor = [UIColor orangeColor].CGColor;
    self.pinLayer.strokeColor = [UIColor orangeColor].CGColor;
    
    UIBezierPath *pinPath = [UIBezierPath bezierPathWithOvalInRect:CGRectMakeWithCenter(center, 9)];
    self.pinLayer.path = pinPath.CGPath;
    [self.layer addSublayer:self.pinLayer];
}

- (void)updateView {
    self.pinLayer.fillColor = self.themeColor.CGColor;
    self.pinLayer.strokeColor = self.themeColor.CGColor;
}

- (void)actionTapPinView:(id)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(didTapPinView:)]) {
        [self.delegate didTapPinView:self];
    }
}

@end


@interface GizRegionBackgroundView ()

@property (nonatomic, strong) CAShapeLayer *radiusLayer;

@property (nonatomic, strong) GizRadiusLabel *radiusLabel;

@end

@implementation GizRegionBackgroundView

+ (instancetype)regionBackgroundView {
    GizRegionBackgroundView *view = [[GizRegionBackgroundView alloc] initWithFrame:CGRectMake(0, 0, GizRegionViewWidth, GizRegionViewWidth)];
    return view;
}

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        self.userInteractionEnabled = NO;
        _themeColor = [UIColor orangeColor];
        
        [self initSubViews];
    }
    
    return self;
}

- (void)initSubViews {
    
    CGFloat radius = CGRectGetWidth(self.frame) / 2.0;
    
    self.backgroundColor = [[UIColor blueColor] colorWithAlphaComponent:0.1];
    self.layer.cornerRadius = radius;
    self.layer.borderWidth = 1.0;
    
    [self createShapeLayers];
    [self createRadiusLabel];
    [self updateView];
}

- (void)createShapeLayers {
    CGFloat radius = CGRectGetWidth(self.frame) / 2.0;
    
    CGPoint center = CGPointMake(radius, radius);
    CGPoint rightPoint = CGPointMake(CGRectGetWidth(self.frame), center.y);
    
    // 半径
    self.radiusLayer = [CAShapeLayer layer];
    self.radiusLayer.frame = self.layer.bounds;
    self.radiusLayer.fillColor = [UIColor clearColor].CGColor;
    self.radiusLayer.strokeColor = [UIColor orangeColor].CGColor;
    self.radiusLayer.lineDashPattern = @[@(4)];
    
    UIBezierPath *radiusPath = [UIBezierPath bezierPath];
    [radiusPath moveToPoint:center];
    [radiusPath addLineToPoint:rightPoint];
    
    self.radiusLayer.path = radiusPath.CGPath;
    [self.layer addSublayer:self.radiusLayer];
}

- (void)createRadiusLabel {
    
    // 当前半径值
    self.radiusLabel = [[GizRadiusLabel alloc] init];
    self.radiusLabel.translatesAutoresizingMaskIntoConstraints = NO;
    self.radiusLabel.backgroundColor = [UIColor orangeColor];
    self.radiusLabel.layer.masksToBounds = YES;
    self.radiusLabel.layer.cornerRadius = 5;
    self.radiusLabel.font = [UIFont systemFontOfSize:14];
    self.radiusLabel.textColor = [UIColor whiteColor];
    self.radiusLabel.textAlignment = NSTextAlignmentCenter;
    [self addSubview:self.radiusLabel];
    
    self.radiusLabel.text = @"200m";
    
    NSLayoutConstraint *centerX = [NSLayoutConstraint constraintWithItem:self.radiusLabel attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1.5 constant:0];
    NSLayoutConstraint *top = [NSLayoutConstraint constraintWithItem:self.radiusLabel attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:5];
    [self addConstraints:@[centerX, top]];
}

- (void)updateView {
    self.layer.borderColor = self.themeColor.CGColor;
    self.radiusLayer.strokeColor = self.themeColor.CGColor;
    self.radiusLabel.backgroundColor = self.themeColor;
}

- (void)setThemeColor:(UIColor *)themeColor {
    _themeColor = themeColor;
    
    [self updateView];
}

- (void)setRadius:(NSInteger)radius {
    _radius = radius;
    
    self.radiusLabel.text = [NSString stringWithFormat:@"%@m", @(radius)];
}

@end
