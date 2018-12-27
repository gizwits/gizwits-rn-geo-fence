
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, GizGeofenceError) {
    GizGeofenceErrorNone = 0,
    GizGeofenceErrorCancel = 1,             // 取消选取位置
    GizGeofenceErrorLocateDenied = 2,       // 定位权限没有开启
    GizGeofenceErrorLocateNotAlways = 3,    // 没有始终允许定位
    GizGeofenceErrorIncorrectArgument = 4,  // 参数不正确
    GizGeofenceErrorLocateRestricted = 5,   // 定位功能受限
    GizGeofenceErrorLocationReverseFailed = 6,   // 位置解析失败
};

/**
 地理围栏工具。(监听的区域上限为 20 个)
 */
@interface RNGizwitsRnGeofence : RCTEventEmitter <RCTBridgeModule>

///**
// 获取位置使用授权状态。
// 0: 未授权
// 1: 位置功能不可用
// 2: 拒绝授权
// 3: 位置功能总是可用
// 4: 位置功能在App运行时可用
// */
//- (void)authorizationStatus:(CDVInvokedUrlCommand *)command;
//
///// 请求总是使用位置信息的权限。
//- (void)requestAlwaysAuthorization:(CDVInvokedUrlCommand *)command;
//
///**
// 设置主题色，标题，导航栏背景等。
// {"themeColor": "FFFFFF", "titleColor": "FFFFFF", "navi_bg": "FFFFFF", "title": "标题", "right_title": "导航栏右侧按钮标题", "search_placeholder": "搜索框提示语"}
// */
//- (void)setThemeInfo:(CDVInvokedUrlCommand *)command;
//
///**
// 设置服务器信息，version/token/appKey/type/url 等
// {"url": "", "version": "", "token": "", "appKey": "", "type": ""}
// */
//- (void)setServerInfo:(CDVInvokedUrlCommand *)command;
//
///**
// 选取区域。添加区域时传入 {}；编辑区域时，传入 region
// 区域 region 结构:
// {"latitude": 22.123456, "longitude": 113.123456, "radius": 200, "status": "enter/leave", "conditionId": ""}
// */
//- (void)pickRegion:(CDVInvokedUrlCommand *)command;
//
///**
// 选取地址。新选择地址时传入 {}；编辑地址时，传入 address
// 地址 address 结构:
// {"latitude": 22.123456, "longitude": 113.123456}
// */
//- (void)pickAddress:(CDVInvokedUrlCommand *)command;
//
///// 添加区域
//- (void)addRegion:(CDVInvokedUrlCommand *)command;
///// 移除区域
//- (void)removeRegion:(CDVInvokedUrlCommand *)command;
///// 移除所有区域
//- (void)removeAllRegions:(CDVInvokedUrlCommand *)command;
///// 获取区域列表
//- (void)getRegionList:(CDVInvokedUrlCommand *)command;
///// 设置区域列表
//- (void)setRegionList:(CDVInvokedUrlCommand *)command;
//
///// 获取当前位置。success 回调坐标 {"latitude": 23.123456, "longitude": 123.123456}
//- (void)getCurrentLocation:(CDVInvokedUrlCommand *)command;
//
///**
// 解析地址。参数传入坐标 {"latitude": 23.123456, "longitude": 123.123456}
// success 回调地址信息 {
//     FormattedAddressLines = [
//     中国广东省广州市天河区沙东街道沙太路陶庄5号
//     ],
//     Street = 沙太路陶庄5号,
//     Thoroughfare = 沙太路陶庄5号,
//     Name = 沙东轻工业大厦,
//     City = 广州市,
//     Country = 中国,
//     State = 广东省,
//     SubLocality = 天河区,
//     CountryCode = CN
// }
// 地址语言随系统。
// */
//- (void)getAddressInfo:(CDVInvokedUrlCommand *)command;
//
//#pragma mark - 坐标转换
//
///*
// 坐标系转换。
// WGS-84：是国际标准，GPS坐标（Google Earth使用、或者GPS模块）
// GCJ-02：中国坐标偏移标准，Google Map、高德、腾讯使用 (火星坐标系)
// BD-09 ：百度坐标偏移标准，Baidu Map使用
// */
//
//- (void)transformFromWGSToGCJ:(CDVInvokedUrlCommand *)command;
//- (void)transformFromGCJToWGS:(CDVInvokedUrlCommand *)command;
//- (void)transformFromGCJToBaidu:(CDVInvokedUrlCommand *)command;
//- (void)transformFromBaiduToGCJ:(CDVInvokedUrlCommand *)command;

@end

NS_ASSUME_NONNULL_END
