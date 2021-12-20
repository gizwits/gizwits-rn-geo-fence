//
//  GizPickRegionViewController.m
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/22.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import "GizPickRegionViewController.h"

#import "GizPinView.h"
#import "GizGeofencePopupAddressView.h"
#import "GizAddressSearchView.h"

#import "GizCoordinateTransform.h"
#import "GizGeofenceArchiver.h"

#import <CoreLocation/CoreLocation.h>
#import <MapKit/MapKit.h>

const NSInteger canleAlertViewTag = 1011;

void GizLogDictionary(NSString *prefix, NSDictionary *dict) {
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:NULL];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSLog(@"%@%@", prefix, jsonString);
}

NSString *GizGetAddressFromDictionary(NSDictionary *addressDict, BOOL pickingRegion) {
    NSString *address;
    
    NSString *language = NSLocale.preferredLanguages.firstObject;
    
    if ([language containsString:@"zh-Hans"] || [language containsString:@"zh-Hant"]) {
        NSMutableString *mutableAddress = [[NSMutableString alloc] init];
        if (addressDict[@"City"]) {
            [mutableAddress appendString:addressDict[@"City"]];
        }
        
        if (addressDict[@"SubLocality"]) {
            [mutableAddress appendString:addressDict[@"SubLocality"]];
        }
        
        if (addressDict[@"Name"]) {
            [mutableAddress appendString:addressDict[@"Name"]];
        }
        
        address = mutableAddress;
    } else {
        if (pickingRegion) {
            NSArray *addressLines = addressDict[@"FormattedAddressLines"];
            
            if (addressLines) {
                address = [addressLines componentsJoinedByString:@", "];
            } else {
                address = @"";
            }
        } else {
            address = addressDict[@"Name"];
        }
    }
    
    return address;
}

NSString *GizGetSubaddressFromDictionary(NSDictionary *addressDict) {
    NSString *address;
    
    NSString *language = NSLocale.preferredLanguages.firstObject;
    
    if ([language containsString:@"zh-Hans"] || [language containsString:@"zh-Hant"]) {
        address = nil;
    } else {
        NSMutableArray<NSString *> *array = [[NSMutableArray alloc] init];
        
        if (addressDict[@"SubLocality"]) {
            [array addObject:addressDict[@"SubLocality"]];
        }
        
        if (addressDict[@"City"]) {
            [array addObject:addressDict[@"City"]];
        }
        
        address = [array componentsJoinedByString:@", "];
    }
    
    return address;
}


@interface GizPinAnnotation : NSObject <MKAnnotation>

@property (nonatomic, assign) CLLocationCoordinate2D coordinate;

@property (nonatomic, copy, nullable) NSString *title;
@property (nonatomic, copy, nullable) NSString *subtitle;

@end

@implementation GizPinAnnotation

@end


@interface GizPinAnnotationView : MKAnnotationView

@property (nonatomic, strong) UITapGestureRecognizer *tapGesture;

@end

@implementation GizPinAnnotationView

- (instancetype)initWithAnnotation:(id<MKAnnotation>)annotation reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithAnnotation:annotation reuseIdentifier:reuseIdentifier];
    
    if (self) {
        self.tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(actionTapAnnotationView:)];
        [self addGestureRecognizer:self.tapGesture];
    }
    
    return self;
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    // 禁用 MKAnnotationView 本身的手势事件
    // MKAnnotationView 有个单击手势会和 self.tapGesture 冲突
    return [gestureRecognizer isEqual:self.tapGesture];
}

- (void)actionTapAnnotationView:(UIGestureRecognizer *)sender {
    [self setSelected:!self.selected animated:YES];
}

@end


@implementation GizPickRegionNavigationController

// 禁止转屏
- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

@end



@interface GizPickRegionViewController () <CLLocationManagerDelegate, MKMapViewDelegate, GizPinViewDelegate, GizAddressSearchViewDelegate, MKLocalSearchCompleterDelegate, UITableViewDelegate, UITableViewDataSource,UIAlertViewDelegate>

@property (nonatomic, strong) IBOutlet MKMapView *mapView;

// 定位到当前位置
@property (nonatomic, strong) IBOutlet UIButton *locateButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *locateButtonWidth;

// 定制搜索
@property (weak, nonatomic) IBOutlet GizAddressSearchView *addressSearchView;
@property (weak, nonatomic) IBOutlet UIView *addressTableBackgroundView;
@property (weak, nonatomic) IBOutlet UITableView *addressTableView;

@property (nonatomic, strong) UIBarButtonItem *backBarButtonItem;
@property (nonatomic, strong) UIBarButtonItem *rightBarButtonItem;

@property (nonatomic, assign) BOOL hasInitializedMapView;

@property (nonatomic, strong) CLLocationManager *locationManager;

// 区域选择
@property (nonatomic, strong) GizRegionBackgroundView *regionBackgroundView;    // 区域
@property (nonatomic, strong) GizPinView *pinView;                              // 大头针
@property (nonatomic, strong) GizGeofencePopupAddressView *popupAddressView;    // 地址
@property (nonatomic, assign) CGFloat radius;                                   // 默认区域半径

// 家庭地址
@property (nonatomic, strong) GizPinAnnotation *pinAnnotation;

// 地址搜索
@property (nonatomic, strong) MKLocalSearchCompleter *localSearchCompleter;

// 是否显示地址搜素历史
@property (nonatomic, assign) BOOL shouldShowAddressSearchHistories;
@property (nonatomic, strong, nullable) NSMutableArray<NSDictionary *> *addressSearchHistories;

@end

@implementation GizPickRegionViewController

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    
    if (self) {
        _themeColor = [UIColor orangeColor];
        _barColor = [UIColor whiteColor];
        _textColor = [UIColor blackColor];
        _bgColor = [UIColor whiteColor];
        _rightButtonTitle = @"确定";
        self.title = @"选取位置";
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = _bgColor;
    self.addressTableBackgroundView.backgroundColor = _bgColor;
    // 返回
    self.backBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"GizRegionResource.bundle/navigation_btn_back_normal"] style:UIBarButtonItemStylePlain target:self action:@selector(actionBack:)];
    self.navigationItem.leftBarButtonItem = self.backBarButtonItem;
    
    // 确定
    self.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:self.rightButtonTitle style:UIBarButtonItemStylePlain target:self action:@selector(actionRightBarButtonClicked:)];
    self.navigationItem.rightBarButtonItem = self.rightBarButtonItem;
    
    // 地图
    [self initMapView];
    
    // 地址搜索
    self.addressSearchView.themeColor = self.themeColor;
    self.addressSearchView.placeholder = self.searchPlaceholder;
    self.addressSearchView.backgroundColor = [self.barColor colorWithAlphaComponent:0.9];;
    self.addressSearchView.delegate = self;
    
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    
    if([CLLocationManager locationServicesEnabled]){
        CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
        switch (status) {
            case kCLAuthorizationStatusNotDetermined: {
//                [self.locationManager requestAlwaysAuthorization];
            }
                break;
                
            case kCLAuthorizationStatusDenied:
                break;
                
            case kCLAuthorizationStatusAuthorizedWhenInUse:
                // self.mapView.showsUserLocation = YES;
                break;
                
            case kCLAuthorizationStatusAuthorizedAlways:
                // self.mapView.showsUserLocation = YES;
                break;
                
            default:
                break;
        }
    } else {
        // self.mapView.showsUserLocation = NO;
    }
}

- (void)initMapView {
    
    [self.locateButton setImage:[UIImage imageNamed:@"GizRegionResource.bundle/location_icon"] forState:UIControlStateNormal];
    self.locateButton.layer.cornerRadius = self.locateButtonWidth.constant / 2.0;
    self.locateButton.layer.shadowOpacity = 0.3;
    self.locateButton.layer.shadowOffset = CGSizeMake(0, 0);
    self.locateButton.layer.shadowColor = UIColor.blackColor.CGColor;
    
    if (self.pickingRegion) {
        // 选择区域
        // 区域视图
        self.regionBackgroundView = [GizRegionBackgroundView regionBackgroundView];
        self.regionBackgroundView.themeColor = self.themeColor;
        self.regionBackgroundView.translatesAutoresizingMaskIntoConstraints = NO;
        [self.view insertSubview:self.regionBackgroundView belowSubview:self.addressTableBackgroundView];
        
        NSLayoutConstraint *regionCenterX = [NSLayoutConstraint constraintWithItem:self.regionBackgroundView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self.mapView attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0];
        NSLayoutConstraint *regionCenterY = [NSLayoutConstraint constraintWithItem:self.regionBackgroundView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.mapView attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0];
        [self.view addConstraints:@[regionCenterX, regionCenterY]];
        
        NSLayoutConstraint *regionViewWidth = [NSLayoutConstraint constraintWithItem:self.regionBackgroundView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:CGRectGetWidth(self.regionBackgroundView.frame)];
        NSLayoutConstraint *regionViewHeight = [NSLayoutConstraint constraintWithItem:self.regionBackgroundView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:CGRectGetWidth(self.regionBackgroundView.frame)];
        [self.regionBackgroundView addConstraints:@[regionViewWidth, regionViewHeight]];
        
        // 大头针
        self.pinView = [GizPinView pinView];
        self.pinView.themeColor = self.themeColor;
        self.pinView.delegate = self;
        self.pinView.translatesAutoresizingMaskIntoConstraints = NO;
        [self.view insertSubview:self.pinView belowSubview:self.addressTableBackgroundView];
        
        NSLayoutConstraint *pinCenterX = [NSLayoutConstraint constraintWithItem:self.pinView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self.regionBackgroundView attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0];
        NSLayoutConstraint *pinCenterY = [NSLayoutConstraint constraintWithItem:self.pinView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.regionBackgroundView attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0];
        [self.view addConstraints:@[pinCenterX, pinCenterY]];
        
        NSLayoutConstraint *pinViewWidth = [NSLayoutConstraint constraintWithItem:self.pinView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:CGRectGetWidth(self.pinView.frame)];
        NSLayoutConstraint *pinViewHeight = [NSLayoutConstraint constraintWithItem:self.pinView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:CGRectGetWidth(self.pinView.frame)];
        [self.pinView addConstraints:@[pinViewWidth, pinViewHeight]];
    } else {
        // 选择家庭地址
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(actionTapMapView:)];
        [self.mapView addGestureRecognizer:tap];
    }
    
    // 地址搜索
    self.localSearchCompleter = [[MKLocalSearchCompleter alloc] init];
    self.localSearchCompleter.delegate = self;
    [self updateTheme];
}

// 将地图移动到指定坐标
- (void)moveMapViewTo:(CLLocationCoordinate2D)coordinate radius:(CGFloat)radius animated:(BOOL)animated {
    
    CGFloat mapDistance;
    
    if (radius <= 0) {
        mapDistance = 300 / 320.0 * CGRectGetWidth(self.view.frame);;
    } else {
        if(self.regionBackgroundView){
            mapDistance = radius * 2 / CGRectGetWidth(self.regionBackgroundView.frame) * CGRectGetWidth(self.view.frame);
        }else {
            mapDistance = radius*(300 / 320.0 * CGRectGetWidth(self.view.frame));
            if(mapDistance > 5097656.25){
                // 太大数会导致闪退，需要约束下
                mapDistance = 5097656.25;
            }
        }
    }
    
    MKCoordinateRegion region = MKCoordinateRegionMakeWithDistance(coordinate, mapDistance, mapDistance);
    
    [self.mapView setRegion:region animated:animated];
}


-(void)showLocationServerAlert{
    NSString* title;
    if(self.openLocationSettingsText){
        title = self.openLocationSettingsText;
    } else {
        title = @"打开定位服务";
    }
    NSString* message;
    if(self.gpsNetworkNotEnabledText){
        message = self.gpsNetworkNotEnabledText;
    } else {
        message = @"当前服务需要打开位置信息服务";
    }
    NSString* cancelText = self.rightButtonTitle;
    if(!cancelText){
        cancelText = @"知道了";
    }
    UIAlertView* alert = [[UIAlertView alloc]initWithTitle:title message:message delegate:self cancelButtonTitle:cancelText otherButtonTitles:nil];
    [alert show];
}

-(void)showPermissionServerAlert{
    if(self.permissionNotEnabledTitle || self.permissionNotEnabledContent){
        NSString* title;
        if(self.permissionNotEnabledTitle){
            title = self.permissionNotEnabledTitle;
        } else {
            title = @"";
        }
        NSString* message;
        if(self.permissionNotEnabledContent){
            message = self.permissionNotEnabledContent;
        } else {
            message = @"";
        }
        
        NSString* cancelText = self.cancelText;
        if(!cancelText){
            cancelText = @"取消";
        }
        NSString* sureText = self.rightButtonTitle;
        if(!sureText){
            sureText = @"打开";
        }
        
        UIAlertView* alert = [[UIAlertView alloc]initWithTitle:title message:message delegate:self cancelButtonTitle:cancelText otherButtonTitles:sureText,nil];
        alert.tag = canleAlertViewTag;
        [alert show];
    }
}

#pragma mark 家庭地址

/**
 【选择家庭地址】将 mapView 移到指定地址

 @param addressDict 地址坐标。传 nil 则移到手机当前位置
 @param animated animated
 */
- (void)updateMapViewWithAddressDict:(NSDictionary *)addressDict animated:(BOOL)animated {
    
    CLLocationCoordinate2D coordinate;
    CLLocation *location;
    
    if (!addressDict || addressDict.count == 0) {
        CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
        if(status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse){
            location = self.mapView.userLocation.location;
            coordinate = location.coordinate;
        } else {
            if(self.pinAnnotation){
                [self.mapView removeAnnotation:self.pinAnnotation];
            }
            // 默认位置，显示故宫为中心点
            CLLocationCoordinate2D defaultCoordinate = CLLocationCoordinate2DMake(39.917305169773726, 116.39707945810558);
            [self moveMapViewTo:defaultCoordinate radius:1.0 animated:animated];
        }
    } else {
        coordinate.latitude = [addressDict[@"latitude"] floatValue];
        coordinate.longitude = [addressDict[@"longitude"] floatValue];
        // 将 GPS 坐标转换为 高德地图 坐标
        coordinate = [GizCoordinateTransform transformFromWGSToGCJ:coordinate];
        location = [[CLLocation alloc] initWithLatitude:coordinate.latitude longitude:coordinate.longitude];
    }
    
    if(location){
        [self moveMapViewTo:coordinate radius:-1 animated:animated];
        
        __weak __typeof(self) weakSelf = self;
        [self getAddressInfoWithLocation:location completion:^(NSDictionary *addressDict) {
            __strong __typeof(weakSelf) strongSelf = weakSelf;
            [strongSelf updateAnnotationViewWithAddressDict:addressDict coordinate:coordinate shouldReadd:NO];
        }];
    }
}

/**
 【选择家庭地址】更新大头针

 @param addressDict 地址逆向解析结果
 @param coordinate 地址坐标
 @param readd 是否移掉大头针重新添加
 */
- (void)updateAnnotationViewWithAddressDict:(NSDictionary *)addressDict coordinate:(CLLocationCoordinate2D)coordinate shouldReadd:(BOOL)readd {
    
    // 更新大头针
    void (^updateAnnotation)(GizPinAnnotation *, NSDictionary *, CLLocationCoordinate2D) = ^(GizPinAnnotation *pinAnnotation, NSDictionary *addressDict, CLLocationCoordinate2D coordinate) {
        
        pinAnnotation.coordinate = coordinate;
        pinAnnotation.title = GizGetAddressFromDictionary(addressDict, NO);
        pinAnnotation.subtitle = GizGetSubaddressFromDictionary(addressDict);
    };
    
    if (!self.pinAnnotation) {
        self.pinAnnotation = [[GizPinAnnotation alloc] init];
        updateAnnotation(self.pinAnnotation, addressDict, coordinate);
        [self.mapView addAnnotation:self.pinAnnotation];
    } else {
        if (readd) {
            // 移掉大头针重新添加
            [self.mapView removeAnnotation:self.pinAnnotation];
            
            updateAnnotation(self.pinAnnotation, addressDict, coordinate);
            [self.mapView addAnnotation:self.pinAnnotation];
        } else {
            // 不需移掉大头针，只更新大头针
            updateAnnotation(self.pinAnnotation, addressDict, coordinate);
        }
    }
}

#pragma mark 区域

/**
 【选择区域】将 mapView 移到指定区域

 @param regionDict 区域中心坐标及半径。传 nil 时则移到手机当前位置。
 @param animated animated
 */
- (void)updateMapViewWithRegionDict:(NSDictionary *)regionDict animated:(BOOL)animated {
    
    CLLocationCoordinate2D coordinate;
    
    if (!regionDict || regionDict.count == 0) {
        CLLocation *location = self.mapView.userLocation.location;
        coordinate = location.coordinate;
    } else {
        coordinate.latitude = [regionDict[@"latitude"] floatValue];
        coordinate.longitude = [regionDict[@"longitude"] floatValue];
        // 将 GPS 坐标转换为 高德地图 坐标
        coordinate = [GizCoordinateTransform transformFromWGSToGCJ:coordinate];
    }
    
    if (regionDict && regionDict[@"radius"]) {
        self.radius = [regionDict[@"radius"] floatValue];
    } else {
        self.radius = 200.0;
    }
    
    [self moveMapViewTo:coordinate radius:self.radius animated:animated];
}

/**
 【选择区域】更新区域弹出地址

 @param addressDict 地址逆向解析结果
 */
- (void)updatePopupAddressViewWithAddressDict:(NSDictionary *)addressDict {
    
    if (!addressDict) {
        return;
    }
    
    NSString *address = GizGetAddressFromDictionary(addressDict, YES);
    
    if (!self.popupAddressView) {
        self.popupAddressView = [[GizGeofencePopupAddressView alloc] initWithAddress:address];
        self.popupAddressView.hidden = YES;
        [self.view insertSubview:self.popupAddressView belowSubview:self.addressTableBackgroundView];
        
        CGPoint center = self.regionBackgroundView.center;
        center.y -= 15;
        
        [self.popupAddressView layoutAtPoint:center animated:NO];
        
        [self.popupAddressView showWithAnimated:YES];
    } else {
        self.popupAddressView.address = address;
    }
}

// 计算 mapView 中两个点之间的距离
- (CLLocationDistance)distanceBetweenPoint1:(CGPoint)point1 point2:(CGPoint)point2 {
    CLLocationCoordinate2D leftCoordinate = [self.mapView convertPoint:point1 toCoordinateFromView:nil];
    CLLocationCoordinate2D rightCoordinate = [self.mapView convertPoint:point2 toCoordinateFromView:nil];
    
    MKMapPoint leftMapPoint = MKMapPointForCoordinate(leftCoordinate);
    MKMapPoint rightMapPoint = MKMapPointForCoordinate(rightCoordinate);
    return MKMetersBetweenMapPoints(leftMapPoint, rightMapPoint);
}

// 根据坐标逆向解析地址
- (void)getAddressInfoWithCoordinate:(CLLocationCoordinate2D)coordinate completion:(void (^)(NSDictionary *addressDict))completionHandler {
    
    CLLocation *location = [[CLLocation alloc] initWithLatitude:coordinate.latitude longitude:coordinate.longitude];
    
    [self getAddressInfoWithLocation:location completion:completionHandler];
}

// 根据位置逆向解析地址
- (void)getAddressInfoWithLocation:(CLLocation *)location completion:(void (^)(NSDictionary *addressDict))completionHandler {
    
    CLGeocoder *geocoder = [[CLGeocoder alloc] init];
    
    [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray<CLPlacemark *> * _Nullable placemarks, NSError * _Nullable error) {
        
        if (error) {
            completionHandler(nil);
        } else {
            CLPlacemark *placemark = placemarks.firstObject;
            NSDictionary *addressDictionary = placemark.addressDictionary;
            
            GizLogDictionary(@"逆向解析地址 => ", addressDictionary);
            
            completionHandler(placemark.addressDictionary);
        }
    }];
}

#pragma mark - Setters

- (void)setThemeColor:(UIColor *)themeColor {
    _themeColor = themeColor;
    
    if (self.regionBackgroundView) {
        self.regionBackgroundView.themeColor = themeColor;
    }
    
    if (self.pinView) {
        self.pinView.themeColor = themeColor;
    }
    
    if (self.addressSearchView) {
        self.addressSearchView.themeColor = themeColor;
    }
}

- (void)setRightButtonTitle:(NSString *)rightButtonTitle {
    _rightButtonTitle = rightButtonTitle;
    
    if (self.rightBarButtonItem) {
        self.rightBarButtonItem.title = rightButtonTitle;
    }
}

- (void)setSearchPlaceholder:(NSString *)searchPlaceholder {
    _searchPlaceholder = searchPlaceholder;
    
    if (self.addressSearchView) {
        self.addressSearchView.placeholder = searchPlaceholder;
    }
}

-(void)setBgColor:(UIColor *)bgColor{
    _bgColor = bgColor;
    self.addressTableBackgroundView.backgroundColor = self.bgColor;
}

-(void)updateTheme{
    if(self.addressSearchView){
           self.addressSearchView.themeColor = self.themeColor;
           self.addressSearchView.textColor = self.textColor;
       }
    self.addressTableBackgroundView.backgroundColor = self.bgColor;
       if(!self.addressTableBackgroundView.hidden){
           self.addressSearchView.backgroundColor = self.barColor;
           [self.addressTableView reloadData];
       } else{
           self.addressSearchView.backgroundColor = [self.barColor colorWithAlphaComponent:0.9];
       }
}

#pragma mark - Actions

// 返回
- (void)actionBack:(id)sender {
    
    if (!self.addressTableBackgroundView.hidden) {
        [self.addressSearchView endEditing:YES];
        self.addressSearchView.text = @"";
        [self hideAddressTableView];
        return;
    }
    
    if (self.completionHandler) {
        self.completionHandler(nil, YES);
    }
    
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}

// 确定
- (void)actionRightBarButtonClicked:(id)sender {
    
    NSMutableDictionary *regionDict;
    
    if (self.regionDict) {
        regionDict = [[NSMutableDictionary alloc] initWithDictionary:self.regionDict];
    } else {
        regionDict = [[NSMutableDictionary alloc] init];
    }
    
    CLLocationCoordinate2D coordinate;
    
    if (self.pickingRegion) {
        // 【选择区域】坐标取地图中心点
        coordinate = self.mapView.centerCoordinate;
        
        [regionDict setObject:@(self.regionBackgroundView.radius) forKey:@"radius"];
    } else {
        // 【选择家庭地址】坐标取大头针坐标
        coordinate = self.pinAnnotation.coordinate;
    }
    
    // 将 高德地图 坐标转换为 GPS 坐标
    CLLocationCoordinate2D gpsCoordinate = [GizCoordinateTransform transformFromGCJToWGS:coordinate];
    
    [regionDict setObject:@(gpsCoordinate.latitude) forKey:@"latitude"];
    [regionDict setObject:@(gpsCoordinate.longitude) forKey:@"longitude"];
    
    if (self.completionHandler) {
        self.completionHandler(regionDict, NO);
    }
    
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}

// 跳转到手机当前的位置
- (IBAction)actionLocate:(id)sender {
    if([CLLocationManager locationServicesEnabled]){
        CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
        if(status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways){
            if (self.pickingRegion) {
                [self updateMapViewWithRegionDict:nil animated:YES];
            } else {
                [self updateMapViewWithAddressDict:nil animated:YES];
            }
        } else {
            // 没权限，暂时不做任何动作
            [self showPermissionServerAlert];
        }
       
    } else {
        // self.mapView.showsUserLocation = NO;
        [self showLocationServerAlert];
    }
}

// 【家庭地址】点击地图时，更新大头针的位置及地址信息
- (void)actionTapMapView:(UIGestureRecognizer *)sender {
    
    CGPoint point = [sender locationInView:self.mapView];
    CLLocationCoordinate2D coordinate = [self.mapView convertPoint:point toCoordinateFromView:self.mapView];
    
    __weak __typeof(self) weakSelf = self;
    [self getAddressInfoWithCoordinate:coordinate completion:^(NSDictionary *addressDict) {
        __strong __typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf updateAnnotationViewWithAddressDict:addressDict coordinate:coordinate shouldReadd:YES];
    }];
}


#pragma mark - CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    if([CLLocationManager locationServicesEnabled]){
        switch (status) {
            case kCLAuthorizationStatusAuthorizedWhenInUse:
            case kCLAuthorizationStatusAuthorizedAlways:
                // self.mapView.showsUserLocation = YES;
                break;
            case kCLAuthorizationStatusNotDetermined:
            case kCLAuthorizationStatusDenied:
                if(!self.regionDict){
                    // 没有位置信息的话，才提示没权限
                    [self showPermissionServerAlert];
                }
                break;
                
            default:
                break;
        }
    } else {
        // 定位服务不可用，直接弹窗提示
        // self.mapView.showsUserLocation = NO;
        if(!self.regionDict){
            [self showLocationServerAlert];
        }
    }
}

#pragma mark - MKMapViewDelegate
- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView{
    if (self.hasInitializedMapView) {
        return;
    }
    
    self.hasInitializedMapView = YES;
    
    if (self.pickingRegion) {
        [self updateMapViewWithRegionDict:self.regionDict animated:NO];
    } else {
        [self updateMapViewWithAddressDict:self.regionDict animated:NO];
    }
}

- (void)mapView:(MKMapView *)mapView didUpdateUserLocation:(MKUserLocation *)userLocation {
    
//    if (self.hasInitializedMapView) {
//        return;
//    }
//
//    self.hasInitializedMapView = YES;
//
//    if (self.pickingRegion) {
//        [self updateMapViewWithRegionDict:self.regionDict animated:NO];
//    } else {
//        [self updateMapViewWithAddressDict:self.regionDict animated:NO];
//    }
}

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
    
    if (self.pickingRegion) {
        // 选取区域时，区域更新后，更新半径及地址信息
        GizRegionBackgroundView *regionView = (GizRegionBackgroundView *)self.regionBackgroundView;
        
        CGPoint mapCenter = mapView.center;
        CGFloat width = CGRectGetWidth(self.regionBackgroundView.frame);
        CGFloat half = width / 2.0;
        CGPoint leftPoint = CGPointMake(mapCenter.x - half, mapCenter.y);
        CGPoint rightPoint = CGPointMake(mapCenter.x + half, mapCenter.y);
        
        CLLocationDistance distance = [self distanceBetweenPoint1:leftPoint point2:rightPoint];
        
        // mapView setRegion 的时候，实际计算出来的半径 会比 self.radius 小一点，
        // 因此用线性拟合补上误差，使实际值看起来更接近 self.radius
        regionView.radius = distance / 2.0 + 0.001799669678803586 * self.radius + 0.0026730136303125818;
        
        __weak __typeof(self) weakSelf = self;
        [self getAddressInfoWithCoordinate:mapView.centerCoordinate completion:^(NSDictionary *addressDict) {
            __strong __typeof(weakSelf) strongSelf = weakSelf;
            [strongSelf updatePopupAddressViewWithAddressDict:addressDict];
        }];
    }
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation {
    
    if ([annotation isKindOfClass:[GizPinAnnotation class]]) {
        GizPinAnnotationView *view = (GizPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:@"AddressAnnotation"];
        
        if (!view) {
            view = [[GizPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"AddressAnnotation"];
            view.image = [UIImage imageNamed:@"GizRegionResource.bundle/home_icon"];
            view.canShowCallout = YES;
        } else {
            view.annotation = annotation;
        }
        
        return view;
    }
    
    MKAnnotationView *view = [[MKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"emptyAnnotation"];
    return view;
}

- (void)mapView:(MKMapView *)mapView didAddAnnotationViews:(NSArray<MKAnnotationView *> *)views {
    
    // 【选择家庭地址】添加大头针之后，显示标注视图
    for (MKAnnotationView *view in views) {
        if (![view.reuseIdentifier isEqualToString:@"AddressAnnotation"]) {
            continue;
        }
        
        view.selected = YES;
    }
}

#pragma mark - GizPinViewDelegate

- (void)didTapPinView:(GizPinView *)pinView {
    
    if (self.popupAddressView) {
        if (self.popupAddressView.hidden) {
            [self.popupAddressView showWithAnimated:YES];
        } else {
            [self.popupAddressView hideWithAnimated:YES];
        }
    }
}

#pragma mark - GizAddressSearchViewDelegate

- (void)searchViewDidBeginEditing:(GizAddressSearchView *)searchView {
    
    // 如果文本为空，则显示历史结果；否则显示搜索列表
    
    if (self.addressTableBackgroundView.hidden) {
        [self showAddressTableView];
    }
    
    self.localSearchCompleter.queryFragment = searchView.text;
    
    if (!searchView.text || searchView.text.length == 0) {
        self.shouldShowAddressSearchHistories = YES;
        self.addressSearchHistories = [[GizGeofenceArchiver getAddressSearchHistories] mutableCopy];
        [self.addressTableView reloadData];
        return;
    }
    
    self.shouldShowAddressSearchHistories = NO;
}

- (void)searchViewDidEndEditing:(GizAddressSearchView *)searchView {
    
}

- (void)searchView:(GizAddressSearchView *)searchView didUpdateText:(NSString *)text {
    
    // 如果文本为空，则显示历史结果；否则显示搜索列表
    if (!text || text.length == 0) {
        self.localSearchCompleter.queryFragment = text;
        self.shouldShowAddressSearchHistories = YES;
        self.addressSearchHistories = [[GizGeofenceArchiver getAddressSearchHistories] mutableCopy];
        [self.addressTableView reloadData];
        return;
    }
    
    self.shouldShowAddressSearchHistories = NO;
    
    MKCoordinateSpan span = MKCoordinateSpanMake(0.3, 0.3);
    
    self.localSearchCompleter.queryFragment = text;
    self.localSearchCompleter.region = MKCoordinateRegionMake(self.mapView.centerCoordinate, span);
}

- (void)showAddressTableView {
    self.addressTableBackgroundView.backgroundColor = self.bgColor;
    self.addressTableBackgroundView.hidden = NO;
    self.addressTableBackgroundView.alpha = 0;
    self.addressSearchView.backgroundColor = self.barColor;
    [UIView animateWithDuration:0.25 animations:^{
        self.addressTableBackgroundView.alpha = 1;
    }];
}

- (void)hideAddressTableView {
    [UIView animateWithDuration:0.25 animations:^{
        self.addressTableBackgroundView.alpha = 0;
    } completion:^(BOOL finished) {
        self.addressTableBackgroundView.backgroundColor = self.bgColor;
        self.addressTableBackgroundView.hidden = YES;
        self.addressTableBackgroundView.alpha = 1;
        self.addressSearchView.backgroundColor = [self.barColor colorWithAlphaComponent:0.9];
    }];
}

#pragma mark - UIAlertViewDelegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if(alertView.tag == canleAlertViewTag){
        NSLog(@"clickedButtonAtIndex:%d",buttonIndex);
        if(buttonIndex == 1){
            // 确定按钮
            CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
            if(status == kCLAuthorizationStatusNotDetermined){
                [self.locationManager requestAlwaysAuthorization];
            } else if(status == kCLAuthorizationStatusDenied){
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
                });
            }
        }
    }
}


#pragma mark - MKLocalSearchCompleterDelegate

- (void)completerDidUpdateResults:(MKLocalSearchCompleter *)completer {
    [self.addressTableView reloadData];
}

- (void)completer:(MKLocalSearchCompleter *)completer didFailWithError:(NSError *)error {
    NSLog(@"地址搜索失败 => %@", error);
}

#pragma mark - UITableViewDelegate & UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.shouldShowAddressSearchHistories ? self.addressSearchHistories.count : self.localSearchCompleter.results.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 65;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.01;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.01;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *cellIdentifier = @"cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
        cell.backgroundColor = [UIColor clearColor];
//        cell.imageView.tintColor = self.themeColor;
        cell.imageView.image = [[UIImage imageNamed:@"GizRegionResource.bundle/location_icon_2"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
    }
    cell.textLabel.textColor = self.textColor;
    cell.detailTextLabel.textColor = self.textColor;
    cell.imageView.tintColor = self.themeColor;
    
    if (self.shouldShowAddressSearchHistories) {
        // 搜索历史
        NSDictionary *addressDict = self.addressSearchHistories[indexPath.row];
        cell.textLabel.text = addressDict[@"title"];
        cell.detailTextLabel.text = addressDict[@"subtitle"];
    } else {
        MKLocalSearchCompletion *searchCompletion = self.localSearchCompleter.results[indexPath.row];
        cell.textLabel.text = searchCompletion.title;
        cell.detailTextLabel.text = searchCompletion.subtitle;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    [self.addressSearchView endEditing:YES];
    [self hideAddressTableView];
    
    if (self.shouldShowAddressSearchHistories) {
        // 搜索历史
        NSDictionary *addressDict = self.addressSearchHistories[indexPath.row];
        
        if (self.pickingRegion) {
            [self updateMapViewWithRegionDict:addressDict animated:YES];
        } else {
            [self updateMapViewWithAddressDict:addressDict animated:YES];
        }
    } else {
        MKLocalSearchCompletion *searchCompletion = self.localSearchCompleter.results[indexPath.row];
        MKLocalSearchRequest *request = [[MKLocalSearchRequest alloc] initWithCompletion:searchCompletion];
        
        MKLocalSearch *search = [[MKLocalSearch alloc] initWithRequest:request];
        
        [search startWithCompletionHandler:^(MKLocalSearchResponse * _Nullable response, NSError * _Nullable error) {
            
            if (error) {
                NSLog(@"选择地址 搜索失败 => %@", error);
                return;
            }
            
            MKMapItem *firstItem = response.mapItems.firstObject;
            NSDictionary *addressDictionary = firstItem.placemark.addressDictionary;
            CLLocationCoordinate2D coordinate = firstItem.placemark.location.coordinate;
            CLLocationCoordinate2D gpsCoordinate = [GizCoordinateTransform transformFromGCJToWGS:coordinate];
            
            NSDictionary *archiveHistory = @{@"latitude": @(gpsCoordinate.latitude),
                                             @"longitude": @(gpsCoordinate.longitude),
                                             @"title": searchCompletion.title,
                                             @"subtitle": searchCompletion.subtitle
                                             };
            [GizGeofenceArchiver archiveAddressSearchHistory:archiveHistory];
            
            GizLogDictionary(@"选择地址 搜索结果 => ", addressDictionary);
            
            if (self.pickingRegion) {
                [self moveMapViewTo:coordinate radius:self.radius animated:YES];
            } else {
                [self moveMapViewTo:coordinate radius:-1 animated:YES];
                [self updateAnnotationViewWithAddressDict:addressDictionary coordinate:coordinate shouldReadd:YES];
            }
        }];
    }
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.shouldShowAddressSearchHistories;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle != UITableViewCellEditingStyleDelete) {
        return;
    }
    
    [self.addressSearchHistories removeObjectAtIndex:indexPath.row];
    [GizGeofenceArchiver deleteAddressSearchHistoryAtIndex:indexPath.row];
    
    [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
}

@end
