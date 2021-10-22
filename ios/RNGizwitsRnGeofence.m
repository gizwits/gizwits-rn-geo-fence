
#import "RNGizwitsRnGeofence.h"
#import "GizCoordinateTransform.h"
#import "GizGeofenceArchiver.h"
#import "GizGeofenceLogging.h"
#import "GizGeofenceNetworking.h"

#import "GizPickRegionViewController.h"

#import <CoreLocation/CoreLocation.h>
#import <MapKit/MapKit.h>

#define GizGeoResultSuccess @"success"
#define GizGeoResultErrorCode @"GizGeofenceErrorCode"

NSDictionary *NSDictionaryFromCLLocationCoordinate2D(CLLocationCoordinate2D coordinate) {
  return @{@"latitude": @(coordinate.latitude), @"longitude": @(coordinate.longitude)};
}

NSString *NSStringFromCLLocationCoordinate2D(CLLocationCoordinate2D coordinate) {
  NSDictionary *dict = NSDictionaryFromCLLocationCoordinate2D(coordinate);
  NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:kNilOptions error:NULL];
  return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

CLLocationCoordinate2D CLLocationCoordinate2DFromNSDictionary(NSDictionary *dict) {
  CLLocationCoordinate2D coordinate;
  coordinate.latitude = [dict[@"latitude"] floatValue];
  coordinate.longitude = [dict[@"longitude"] floatValue];
  return coordinate;
}

CLLocationCoordinate2D CLLocationCoordinate2DFromNSString(NSString *string) {
  NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
  NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:NULL];
  return CLLocationCoordinate2DFromNSDictionary(dict);
}


// 颜色
@interface UIColor (GizGeofence)

@end

@implementation UIColor (GizGeofence)

/**
 用十六进制RGB值来创建颜色 (只支持RGB格式)
 
 @param hexValue 十六进制RGB值，如：0xFFFFFF
 @return UIColor
 */
+ (UIColor *)colorWithHex:(NSUInteger)hexValue {
  return [UIColor colorWithRed:((hexValue >> 16) & 0x000000FF) / 255.0f
                         green:((hexValue >> 8) & 0x000000FF) / 255.0f
                          blue:(hexValue & 0x000000FF) / 255.0f
                         alpha:1.0f];
}

/**
 用十六进制RGB值来创建颜色 (只支持RGB格式)
 
 @param hexString 十六进制RGB字符串。如：FFFFFF
 @return UIColor
 */
+ (UIColor *)colorWithHexString:(NSString *)hexString {
  
  if (!hexString || hexString.length == 0) {
    return nil;
  }
  
  if ([hexString hasPrefix:@"#"]) {
    hexString = [hexString substringFromIndex:1];
  }
  
  if (hexString.length != 6) {
    return nil;
  }
  
  unsigned int hexValue;
  
  [[NSScanner scannerWithString:hexString] scanHexInt:&hexValue];
  
  return [self colorWithHex:hexValue];
}

@end


// 监听区域
@interface CLCircularRegion (GizGeofence)

@end

@implementation CLCircularRegion (GizGeofence)

+ (instancetype)regionWithDictionary:(NSDictionary *)dict {
  CGFloat latitude = [dict[@"latitude"] floatValue];
  CGFloat longitude = [dict[@"longitude"] floatValue];
  CGFloat radius = [dict[@"radius"] floatValue];
  
  BOOL entry = [dict[@"status"] isEqualToString:@"enter"];
  
  NSString *identifier = dict[@"conditionId"];
  
  CLLocationCoordinate2D coordinate = {latitude, longitude};
  
  CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coordinate radius:radius identifier:identifier];
  region.notifyOnExit = !entry;
  region.notifyOnEntry = entry;
  
  return region;
}

- (NSDictionary *)convertToDictionary {
  NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
  
  [dict setObject:@(self.center.latitude) forKey:@"latitude"];
  [dict setObject:@(self.center.longitude) forKey:@"longitude"];
  [dict setObject:@(self.radius) forKey:@"radius"];
  
  NSString *when = self.notifyOnEntry ? @"enter" : @"leave";
  
  [dict setObject:when forKey:@"status"];
  [dict setObject:self.identifier forKey:@"conditionId"];
  
  return dict;
}

@end


// 地理围栏工具
@interface RNGizwitsRnGeofence () <CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager *locationManager;

@property (nonatomic, assign) BOOL isPickingRegion;

// 选取位置 callback
@property (nonatomic, strong) RCTResponseSenderBlock pickCallbackResult;
@property (nonatomic, strong) RCTResponseSenderBlock requestAuthorizationCallbackResult;
@property (nonatomic, strong) RCTResponseSenderBlock currentLocationCallbackResult;
@property (nonatomic, strong) NSString *getAddressCallbackId;

@property (nonatomic, strong) NSDictionary *pickedDict;

@property (nonatomic, strong) NSDictionary *themeDict;

@property (nonatomic, strong) UINavigationController* pickerAddressNavigation;

@end

@implementation RNGizwitsRnGeofence
RCT_EXPORT_MODULE();
- (dispatch_queue_t)methodQueue{
  return dispatch_get_main_queue();
}

- (CLLocationManager *)locationManager{
  if (_locationManager == nil) {
    _locationManager = [[CLLocationManager alloc] init];
    _locationManager.delegate = self;
  }
  return _locationManager;
}

#pragma mark - Public methods
/*
 * 获取位置功能授权状态
 * getAuthorizationStatus
 RNGizwitsRnGeofence.getAuthorizationStatus((error, status) => {});
 */
RCT_EXPORT_METHOD(getAuthorizationStatus:(RCTResponseSenderBlock)result){
  CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
  result(@[[NSNull null], @(status)]);
}

/*
 * 获取定位服务是否可用
 * getLocationServerEnable
 RNGizwitsRnGeofence.getLocationServerEnable((error, status) => {});
 */
RCT_EXPORT_METHOD(getLocationServerEnable:(RCTResponseSenderBlock)result){
  BOOL status = [CLLocationManager locationServicesEnabled];
  result(@[[NSNull null], @(status)]);
}

/*
 * 请求总是使用位置信息的权限
 * requestAlwaysAuthorization
 RNGizwitsRnGeofence.requestAlwaysAuthorization((error, result) => {});
 */
RCT_EXPORT_METHOD(requestAlwaysAuthorization:(RCTResponseSenderBlock)result){
  CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
  self.requestAuthorizationCallbackResult = result;
  // 未授权
  if (status == kCLAuthorizationStatusNotDetermined) {
    // 请求总是允许的权限
    GeofenceLog(@"Location service authorization not determined, requesting authorization...");
    [self.locationManager requestAlwaysAuthorization];
    return;
  }
  
  [self handleAuthorizationStatus:status];
}

/*
 * 设置主题
 * setThemeInfo
 RNGizwitsRnGeofence.setThemeInfo({"themeColor": "010101", "navi_bg": "ffffff", "titleColor": "000000", "backgroundColor":"ffffff","title": "标题", "right_title": "完成"}, (error, result) => {});
 */
RCT_EXPORT_METHOD(setThemeInfo:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  self.themeDict = info;
  GeofenceLog(@"setThemeInfo => %@", self.themeDict);
  [self updateTheme];
  result(@[[NSNull null], GizGeoResultSuccess]);
}

/*
 - (void)setToken:(CDVInvokedUrlCommand *)command {
 
 if (command.arguments && command.arguments.firstObject) {
 NSString *token = command.arguments.firstObject;
 [GizGeofenceArchiver archiveToken:token];
 } else {
 [GizGeofenceArchiver archiveToken:nil];
 }
 
 CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
 [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
 }
 */

/*
 * 设置服务器信息
 * setServerInfo
 RNGizwitsRnGeofence.setServerInfo({"url": "", "version": "", "token": "", "appKey": "", "type": ""}, (error, result) => {});
 */
RCT_EXPORT_METHOD(setServerInfo:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  if (info && info.count) {
    [GizGeofenceArchiver archiveServerInfo:info];
    GeofenceLog(@"setServerInfo => %@", info);
  } else {
    [GizGeofenceArchiver archiveServerInfo:nil];
    GeofenceLog(@"setServerInfo => null");
  }
  result(@[[NSNull null], GizGeoResultSuccess]);
}


/*
 * 选择或编辑区域
 * pickRegion
 RNGizwitsRnGeofence.pickRegion({}, (error, selectedRegion) => {
 //region = {"latitude": 22.123456, "longitude": 113.123456, "radius": 200, "status": "enter/leave", "conditionId": ""}
 });
 */
RCT_EXPORT_METHOD(pickRegion:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  self.isPickingRegion = YES;
  [self pickRegionOrAddress:info result:result];
}

/*
 * 选择地址
 * pickAddress
 RNGizwitsRnGeofence.pickAddress({}, (error, selectedAddress) => {
 //address = {"latitude": 22.123456, "longitude": 113.123456};
 });
 */
RCT_EXPORT_METHOD(pickAddress:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  self.isPickingRegion = NO;
  [self pickRegionOrAddress:info result:result];
}

/*
 * 添加区域
 * addRegion
 RNGizwitsRnGeofence.addRegion({"latitude": 22.123456, "longitude": 113.123456, "radius": 200, "status": "enter/leave", "conditionId": ""}, (error, result) => {});
 */
RCT_EXPORT_METHOD(addRegion:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  GeofenceLog(@"addRegion => %@", info);
  CLCircularRegion *region = [CLCircularRegion regionWithDictionary:info];
  [self.locationManager startMonitoringForRegion:region];
  result(@[[NSNull null], [region convertToDictionary]]);
}

/*
 * 移除区域
 * removeRegion
 RNGizwitsRnGeofence.removeRegion({"latitude": 22.123456, "longitude": 113.123456, "radius": 200, "status": "enter", "conditionId": ""}, (error, result) => {});
 */
RCT_EXPORT_METHOD(removeRegion:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  GeofenceLog(@"removeRegion => %@", info);
  CLCircularRegion *region = [CLCircularRegion regionWithDictionary:info];
  [self.locationManager stopMonitoringForRegion:region];
  result(@[[NSNull null], GizGeoResultSuccess]);
}

/*
 * 移除所有区域
 * removeAllRegions
 RNGizwitsRnGeofence.removeAllRegions((error, result) => {});
 */
RCT_EXPORT_METHOD(removeAllRegions:(RCTResponseSenderBlock)result){
  GeofenceLog(@"removeAllRegions...");
  [self internalRemoveAllRegions];
  result(@[[NSNull null], GizGeoResultSuccess]);
}

/*
 * 获取区域列表
 * getRegionList
 RNGizwitsRnGeofence.getRegionList((error, regionList) => {});
 */
RCT_EXPORT_METHOD(getRegionList:(RCTResponseSenderBlock)result){
  NSMutableArray<NSDictionary *> *regionList = [[NSMutableArray alloc] init];
  for (CLRegion *region in self.locationManager.monitoredRegions) {
    if (![region isKindOfClass:[CLCircularRegion class]]) {
      continue;
    }
    CLCircularRegion *circularRegion = (CLCircularRegion *)region;
    NSDictionary *regionDict = [circularRegion convertToDictionary];
    [regionList addObject:regionDict];
  }
  
  GeofenceLog(@"getRegionList => %@", regionList);
  result(@[[NSNull null], regionList]);
}

/*
 * 设置区域列表
 * setRegionList
 RNGizwitsRnGeofence.setRegionList([], (error, result) => {});
 */
RCT_EXPORT_METHOD(setRegionList:(NSArray *)list result:(RCTResponseSenderBlock)result){
  NSMutableArray<NSDictionary *> *regionList = [NSMutableArray arrayWithArray:list];
  GeofenceLog(@"setRegionList => %@", regionList);
  [self internalRemoveAllRegions];
  for (NSDictionary *regionDict in regionList) {
    CLCircularRegion *region = [CLCircularRegion regionWithDictionary:regionDict];
    [self.locationManager startMonitoringForRegion:region];
  }
  result(@[[NSNull null], GizGeoResultSuccess]);
}

/*
 * 获取当前位置
 * getCurrentLocation
 RNGizwitsRnGeofence.getCurrentLocation((error, result) => {});
 */
RCT_EXPORT_METHOD(getCurrentLocation:(RCTResponseSenderBlock)result){
  CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
  
  switch (status) {
    case kCLAuthorizationStatusNotDetermined:
    case kCLAuthorizationStatusRestricted:
    case kCLAuthorizationStatusDenied: {
      result(@[@{GizGeoResultErrorCode: @(GizGeofenceErrorLocateDenied)}]);
      return;
    }
      
    default:
      break;
  }
  
  self.currentLocationCallbackResult = result;
  [self.locationManager startUpdatingLocation];
}

/*
 * 解析地址
 * getAddressInfo
 RNGizwitsRnGeofence.getAddressInfo({"latitude": 23.123456, "longitude": 123.123456}, (error, address) => {});
 */
RCT_EXPORT_METHOD(getAddressInfo:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  if (info.count == 0) {
    result(@[@{GizGeoResultErrorCode: @(GizGeofenceErrorIncorrectArgument)}]);
    return;
  }
  NSDictionary *dict = info;
  CLLocationCoordinate2D coordinate = CLLocationCoordinate2DFromNSDictionary(dict);
  coordinate = [GizCoordinateTransform transformFromWGSToGCJ:coordinate];
  CLLocation *location = [[CLLocation alloc] initWithLatitude:coordinate.latitude longitude:coordinate.longitude];
  CLGeocoder *geocoder = [[CLGeocoder alloc] init];
  [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray<CLPlacemark *> * _Nullable placemarks, NSError * _Nullable error) {
    if (error) {
      result(@[@{GizGeoResultErrorCode: @(GizGeofenceErrorLocationReverseFailed)}]);
    } else {
      CLPlacemark *placemark = placemarks.firstObject;
      result(@[[NSNull null], placemark.addressDictionary]);
    }
  }];
}

#pragma mark 坐标转换
/*
 * WGS-84 转换为 GCJ-02 transformFromWGSToGCJ
 RNGizwitsRnGeofence.transformFromWGSToGCJ({"latitude": "23.168181", "longitude": "113.327278"}, (error, targetCoordinate) => {});
 */
RCT_EXPORT_METHOD(transformFromWGSToGCJ:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  [self transformCoordinate:info result:result type:1];
}

/*
 * GCJ-02 转换为 WGS-84 transformFromGCJToWGS
 RNGizwitsRnGeofence.transformFromGCJToWGS({"latitude": "23.168181", "longitude": "113.327278"}, (error, targetCoordinate) => {});
 */
RCT_EXPORT_METHOD(transformFromGCJToWGS:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  [self transformCoordinate:info result:result type:2];
}

/*
 * GCJ-02 转换为 BD-09 transformFromGCJToBaidu
 RNGizwitsRnGeofence.transformFromGCJToBaidu({"latitude": "23.168181", "longitude": "113.327278"}, (error, targetCoordinate) => {});
 */
RCT_EXPORT_METHOD(transformFromGCJToBaidu:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  [self transformCoordinate:info result:result type:3];
}

/*
 * BD-09 转换为 GCJ-02 transformFromBaiduToGCJ
 RNGizwitsRnGeofence.transformFromBaiduToGCJ({"latitude": "23.168181", "longitude": "113.327278"}, (error, targetCoordinate) => {});
 */
RCT_EXPORT_METHOD(transformFromBaiduToGCJ:(NSDictionary *)info result:(RCTResponseSenderBlock)result){
  [self transformCoordinate:info result:result type:4];
}

- (void)transformCoordinate:(NSDictionary *)coordinateDict result:(RCTResponseSenderBlock)result type:(NSInteger)type {
  
  if (coordinateDict.count == 0) {
    result(@[@{GizGeoResultErrorCode: @(GizGeofenceErrorIncorrectArgument)}]);
    return;
  }
  
  NSDictionary *dict = coordinateDict;
  CLLocationCoordinate2D coordinate = CLLocationCoordinate2DFromNSDictionary(dict);
  
  CLLocationCoordinate2D targetCoordinate;
  
  switch (type) {
    case 1:
      targetCoordinate = [GizCoordinateTransform transformFromWGSToGCJ:coordinate];
      break;
      
    case 2:
      targetCoordinate = [GizCoordinateTransform transformFromGCJToWGS:coordinate];
      break;
      
    case 3:
      targetCoordinate = [GizCoordinateTransform transformFromGCJToBaidu:coordinate];
      break;
      
    case 4:
    default:
      targetCoordinate = [GizCoordinateTransform transformFromBaiduToGCJ:coordinate];
      break;
  }
  
  result(@[[NSNull null], NSDictionaryFromCLLocationCoordinate2D(targetCoordinate)]);
}

#pragma mark - Private methods

- (void)pickRegionOrAddress:(NSDictionary *)info result:(RCTResponseSenderBlock)result{
  
  self.pickCallbackResult = result;
  
  // 如果传入 dict，则编辑 区域/地址；否则添加新的
  if (info && info.count) {
    self.pickedDict = info;
  } else {
    self.pickedDict = @{};
  }
  
  if (self.isPickingRegion) {
    GeofenceLog(@"start pick region => %@", self.pickedDict);
  } else {
    GeofenceLog(@"start pick address => %@", self.pickedDict);
  }
  
  [self presentPickRegionViewController];
}

- (void)internalRemoveAllRegions {
  NSSet<CLRegion *> *monitoredRegions = [[NSSet alloc] initWithSet:self.locationManager.monitoredRegions];
  
  for (CLRegion *region in monitoredRegions) {
    [self.locationManager stopMonitoringForRegion:region];
  }
}

- (void)handleAuthorizationStatus:(CLAuthorizationStatus)status {
  RCTResponseSenderBlock requestAuthorizationCallbackResult;
  
  if (!self.requestAuthorizationCallbackResult) {
    return;
  }
  
  requestAuthorizationCallbackResult = self.requestAuthorizationCallbackResult;
  self.requestAuthorizationCallbackResult = nil;
  
  switch (status) {
    case kCLAuthorizationStatusAuthorizedAlways:
      GeofenceLog(@"Location service authorization always...");
      break;
      
    case kCLAuthorizationStatusDenied:
      GeofenceLog(@"Location service authorization denied...");
      break;
      
    case kCLAuthorizationStatusAuthorizedWhenInUse:
      GeofenceLog(@"Location service authorization only when in use...");
      break;
      
    case kCLAuthorizationStatusRestricted:
      GeofenceLog(@"Location service restricted...");
      break;
      
    default:
      break;
  }
  
  if (requestAuthorizationCallbackResult) {
    requestAuthorizationCallbackResult(@[[NSNull null], @(status)]);
  }
}

// 跳转 选择区域或家庭地址
- (void)presentPickRegionViewController {
  
  UINavigationController *navigationController = (UINavigationController *)[[UIStoryboard storyboardWithName:@"GizGeofence" bundle:nil] instantiateInitialViewController];
  GizPickRegionViewController *pickRegionViewController = (GizPickRegionViewController *)navigationController.topViewController;
  self.pickerAddressNavigation = navigationController;
  
  pickRegionViewController.pickingRegion = self.isPickingRegion;
  pickRegionViewController.regionDict = self.pickedDict;
  
  __weak __typeof(self) weakSelf = self;
  pickRegionViewController.completionHandler = ^(NSDictionary * _Nullable dict, BOOL canceled) {
    
    __strong __typeof(weakSelf) strongSelf = weakSelf;
    strongSelf.pickerAddressNavigation = NULL;
    
    if (canceled) {
      // 返回
      [strongSelf sendPickRegionWithError:GizGeofenceErrorCancel];
    } else {
      // 确定
      [strongSelf sendPickRegionWithRegionDict:dict];
    }
  };
    
  [self updateTheme];
  
//  // 设置主题
//  if (self.themeDict) {
//    NSString *themeColorStr = self.themeDict[@"themeColor"];
//
//    if (themeColorStr) {
//      UIColor *color = [UIColor colorWithHexString:themeColorStr];
//      navigationController.navigationBar.tintColor = color;
//      pickRegionViewController.themeColor = color;
//    }
//
//    NSString *naviBgColorStr = self.themeDict[@"navi_bg"];
//
//    if (naviBgColorStr) {
//      navigationController.navigationBar.barTintColor = [UIColor colorWithHexString:naviBgColorStr];
//    }
//
//    NSString *titleColorStr = self.themeDict[@"titleColor"];
//
//    if (titleColorStr) {
//      UIColor *color = [UIColor colorWithHexString:titleColorStr];
//
//      if (color) {
//        navigationController.navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName: color};
//      }
//    }
    
//    pickRegionViewController.title = self.themeDict[@"title"];
//    pickRegionViewController.rightButtonTitle = self.themeDict[@"right_title"];
//    pickRegionViewController.searchPlaceholder = self.themeDict[@"search_placeholder"];
//  }
  
  [[self getRootVC] presentViewController:navigationController animated:YES completion:nil];
}

- (void)updateTheme{
    // 设置主题
    if (self.themeDict && self.pickerAddressNavigation) {
      UINavigationController *navigationController = self.pickerAddressNavigation;
      GizPickRegionViewController *pickRegionViewController = (GizPickRegionViewController *)navigationController.topViewController;
      NSString *themeColorStr = self.themeDict[@"themeColor"];
      
      if (themeColorStr) {
        UIColor *color = [UIColor colorWithHexString:themeColorStr];
        navigationController.navigationBar.tintColor = color;
        pickRegionViewController.themeColor = color;
      }
      
      NSString *naviBgColorStr = self.themeDict[@"navi_bg"];
      
      if (naviBgColorStr) {
        UIColor * barColor =[UIColor colorWithHexString:naviBgColorStr];
          if(barColor){
              navigationController.navigationBar.barTintColor = barColor;
                      pickRegionViewController.barColor = barColor;
          }
      }
      
      NSString *titleColorStr = self.themeDict[@"titleColor"];
      
      if (titleColorStr) {
        UIColor *color = [UIColor colorWithHexString:titleColorStr];
        if (color) {
          navigationController.navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName: color};
            pickRegionViewController.textColor = color;
        }
      }
        
      NSString *bgColorStr = self.themeDict[@"backgroundColor"];
        
        if (bgColorStr) {
          UIColor *bgColor = [UIColor colorWithHexString:bgColorStr];
          if (bgColor) {
            pickRegionViewController.bgColor = bgColor;
          }
        }
        
        
      
      pickRegionViewController.title = self.themeDict[@"title"];
      pickRegionViewController.rightButtonTitle = self.themeDict[@"right_title"];
      pickRegionViewController.searchPlaceholder = self.themeDict[@"search_placeholder"];
    pickRegionViewController.gpsNetworkNotEnabledText = self.themeDict[@"gpsNetworkNotEnabledText"];
    pickRegionViewController.openLocationSettingsText = self.themeDict[@"openLocationSettingsText"];
        pickRegionViewController.cancelText = self.themeDict[@"cancelText"];
        
        pickRegionViewController.permissionNotEnabledTitle = self.themeDict[@"permissionNotEnabledTitle"];
        pickRegionViewController.permissionNotEnabledContent = self.themeDict[@"permissionNotEnabledContent"];
      [pickRegionViewController updateTheme];
    }
}

- (UIViewController*) getRootVC {
  UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
  while (root.presentedViewController != nil) {
    root = root.presentedViewController;
  }
  
  return root;
}

#pragma mark - CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
  
  if (!self.requestAuthorizationCallbackResult) {
    return;
  }
  
  [self handleAuthorizationStatus:status];
}

- (void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region {
  
  if (![region isKindOfClass:[CLCircularRegion class]]) {
    return;
  }
  
  CLCircularRegion *circularRegion = (CLCircularRegion *)region;
  
  GeofenceLog(@"didStartMonitoringForRegion => %@", [circularRegion convertToDictionary]);
}

- (void)locationManager:(CLLocationManager *)manager monitoringDidFailForRegion:(CLRegion *)region withError:(NSError *)error {
  
  if (![region isKindOfClass:[CLCircularRegion class]]) {
    return;
  }
  
  CLCircularRegion *circularRegion = (CLCircularRegion *)region;
  
  GeofenceLog(@"monitoringDidFailForRegion => %@, error => %@", [circularRegion convertToDictionary], error);
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray<CLLocation *> *)locations {
  
  if (locations.count <= 0) {
    return;
  }
  
  [self.locationManager stopUpdatingLocation];
  
  CLLocation *location = locations.lastObject;
  
  if (!self.currentLocationCallbackResult) {
    return;
  }
  
  self.currentLocationCallbackResult(@[[NSNull null], NSDictionaryFromCLLocationCoordinate2D(location.coordinate)]);
  self.currentLocationCallbackResult = nil;
}

- (void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region {
  
  if (![region isKindOfClass:[CLCircularRegion class]]) {
    return;
  }
  
  CLCircularRegion *circularRegion = (CLCircularRegion *)region;
  
  GeofenceLog(@"进入区域 id = %@ {%.6f, %.6f} radius = %d", circularRegion.identifier, circularRegion.center.latitude, circularRegion.center.longitude, (int)circularRegion.radius);
  
  [[GizGeofenceNetworking sharedInstance] enterRegion:circularRegion completionHandler:^(NSError * _Nullable error) {
    
    if (error) {
      GeofenceLog(@"上报进入区域出错 => %@", error);
    } else {
      GeofenceLog(@"上报进入区域成功.");
    }
  }];
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region {
  
  if (![region isKindOfClass:[CLCircularRegion class]]) {
    return;
  }
  
  CLCircularRegion *circularRegion = (CLCircularRegion *)region;
  
  GeofenceLog(@"离开区域 id = %@ {%.6f, %.6f} radius = %d", circularRegion.identifier, circularRegion.center.latitude, circularRegion.center.longitude, (int)circularRegion.radius);
  
  [[GizGeofenceNetworking sharedInstance] leaveRegion:circularRegion completionHandler:^(NSError * _Nullable error) {
    
    if (error) {
      GeofenceLog(@"上报离开区域出错 => %@", error);
    } else {
      GeofenceLog(@"上报离开区域成功.");
    }
  }];
}

#pragma mark - Callback

- (NSArray<NSString *> *)supportedEvents{
  return @[];
}

- (void)sendPickRegionWithError:(GizGeofenceError)error {
  if (self.pickCallbackResult) {
    self.pickCallbackResult(@[@{GizGeoResultErrorCode: @(error)}]);
  }
  self.pickCallbackResult = nil;
}

- (void)sendPickRegionWithRegionDict:(NSDictionary *)regionDict {
  
  if (self.isPickingRegion) {
    GeofenceLog(@"picked region => %@", regionDict);
  } else {
    GeofenceLog(@"picked address => %@", regionDict);
  }
  
  if (self.pickCallbackResult) {
    self.pickCallbackResult(@[[NSNull null], regionDict]);
  }
  self.pickCallbackResult = nil;
}

@end
