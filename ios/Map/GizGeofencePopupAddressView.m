//
//  GizGeofencePopupAddressView.m
//  TestGeofencing
//
//  Created by Minus🍀 on 2018/11/23.
//

#import "GizGeofencePopupAddressView.h"

@interface GizGeofencePopupAddressView () {
    CGFloat leftPadding;
    CGFloat topPadding;
    CGFloat bottomPadding;
    CGFloat cornerRadius;
    CGFloat arrowWidth;
    CGFloat arrowHeight;
}

@property (nonatomic, strong) UILabel *addressLabel;

@property (nonatomic, assign) CGPoint showPoint;

@property (nonatomic, strong) UIBezierPath *bezierPath;
@property (nonatomic, strong) CAShapeLayer *backgroundLayer;

@end

@implementation GizGeofencePopupAddressView

- (instancetype)initWithAddress:(NSString *)address {
    self = [super init];
    
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        
        arrowWidth = 10;
        arrowHeight = 5;
        leftPadding = 15;
        topPadding = 8;
        bottomPadding = topPadding + arrowHeight;
        cornerRadius = 10;
        
        self.backgroundLayer = [[CAShapeLayer alloc] init];
        self.backgroundLayer.strokeColor = [UIColor whiteColor].CGColor;
        self.backgroundLayer.fillColor = [UIColor whiteColor].CGColor;
        self.backgroundLayer.shadowColor = [UIColor lightGrayColor].CGColor;
        self.backgroundLayer.shadowOpacity = 0.3;
        self.backgroundLayer.shadowRadius = cornerRadius;
        [self.layer addSublayer:self.backgroundLayer];
        
        self.addressLabel = [[UILabel alloc] init];
        self.addressLabel.textColor = [UIColor blackColor];
        self.addressLabel.font = [UIFont systemFontOfSize:16];
        self.addressLabel.numberOfLines = 2;
        [self addSubview:self.addressLabel];
        
        self.address = address;
    }
    
    return self;
}

- (void)setAddress:(NSString *)address {
    _address = address;
    
    self.addressLabel.text = address;
    [self updateFramesWithAnimated:YES];
}

- (void)updateFramesWithAnimated:(BOOL)animated {
    
    CGFloat maxWidth = CGRectGetWidth(UIScreen.mainScreen.bounds) - 60;
    CGFloat maxHeight = 44;
    CGSize size;
    
    if (self.address && self.address.length > 0) {
        size = [self.address boundingRectWithSize:CGSizeMake(maxWidth, maxHeight) options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading|NSStringDrawingTruncatesLastVisibleLine attributes:@{NSFontAttributeName: self.addressLabel.font} context:nil].size;
    } else {
        size = CGSizeMake(22, 22);
    }
    
    size.width = MAX(50, size.width);
    size.height = MAX(22, size.height);
    
    CGRect labelFrame;
    labelFrame.size = size;
    labelFrame.origin = CGPointMake(leftPadding, topPadding);
    
    CGRect viewFrame;
    viewFrame.size = CGSizeMake(size.width + leftPadding * 2, size.height + topPadding + bottomPadding);
    viewFrame.origin = CGPointMake(self.showPoint.x-viewFrame.size.width/2, self.showPoint.y-viewFrame.size.height);
    
    self.bezierPath = [UIBezierPath bezierPath];
    [self.bezierPath moveToPoint:CGPointMake(cornerRadius, 0)];
    [self.bezierPath addLineToPoint:CGPointMake(viewFrame.size.width-cornerRadius, 0)];
    [self.bezierPath addArcWithCenter:CGPointMake(viewFrame.size.width-cornerRadius, cornerRadius) radius:cornerRadius startAngle:M_PI*1.5 endAngle:0 clockwise:YES];
    [self.bezierPath addLineToPoint:CGPointMake(viewFrame.size.width, viewFrame.size.height-cornerRadius-arrowHeight)];
    [self.bezierPath addArcWithCenter:CGPointMake(viewFrame.size.width-cornerRadius, viewFrame.size.height-cornerRadius-arrowHeight) radius:cornerRadius startAngle:0 endAngle:M_PI_2 clockwise:YES];
    
    [self.bezierPath addLineToPoint:CGPointMake(viewFrame.size.width/2+arrowWidth/2, viewFrame.size.height-arrowHeight)];
    [self.bezierPath addLineToPoint:CGPointMake(viewFrame.size.width/2, viewFrame.size.height)];
    [self.bezierPath addLineToPoint:CGPointMake(viewFrame.size.width/2-arrowWidth/2, viewFrame.size.height-arrowHeight)];
    [self.bezierPath addLineToPoint:CGPointMake(cornerRadius, viewFrame.size.height-arrowHeight)];
    
    [self.bezierPath addArcWithCenter:CGPointMake(cornerRadius, viewFrame.size.height-cornerRadius-arrowHeight) radius:cornerRadius startAngle:M_PI_2 endAngle:M_PI clockwise:YES];
    [self.bezierPath addLineToPoint:CGPointMake(0, cornerRadius)];
    [self.bezierPath addArcWithCenter:CGPointMake(cornerRadius, cornerRadius) radius:cornerRadius startAngle:M_PI endAngle:M_PI*1.5 clockwise:YES];
    
    self.backgroundLayer.path = self.bezierPath.CGPath;
    self.backgroundLayer.shadowPath = self.bezierPath.CGPath;
    
    self.addressLabel.frame = labelFrame;
    self.frame = viewFrame;
    self.backgroundLayer.frame = self.bounds;
}

- (void)layoutAtPoint:(CGPoint)point animated:(BOOL)animated {
    self.showPoint = point;
    
    [self updateFramesWithAnimated:animated];
}

- (void)showWithAnimated:(BOOL)animated {
    
    self.layer.transform = CATransform3DMakeScale(0.9, 0.9, 1.0);
    self.hidden = NO;
    self.alpha = 0.0;
    
    [UIView animateWithDuration:0.4 delay:0 usingSpringWithDamping:0.3 initialSpringVelocity:12 options:UIViewAnimationOptionCurveLinear animations:^{
        self.layer.transform = CATransform3DMakeScale(1.0, 1.0, 1.0);
    } completion:nil];
    
    [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionCurveLinear animations:^{
        self.alpha = 1.0;
    } completion:nil];
}

- (void)hideWithAnimated:(BOOL)animated {
    [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionCurveLinear animations:^{
        self.alpha = 0.0;
    } completion:^(BOOL finished) {
        self.hidden = YES;
        self.alpha = 1.0;
    }];
}

@end
