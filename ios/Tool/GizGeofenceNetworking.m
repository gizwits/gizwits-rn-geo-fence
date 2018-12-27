//
//  GizGeofenceNetworking.m
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/28.
//  Copyright © 2018年 Gizwits. All rights reserved.
//

#import "GizGeofenceNetworking.h"
#import "GizGeofenceArchiver.h"
#import "GizGeofenceLogging.h"

@implementation GizGeofenceNetworking

+ (instancetype)sharedInstance {
    static GizGeofenceNetworking *networking = nil;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        networking = [[GizGeofenceNetworking alloc] init];
    });
    
    return networking;
}

- (void)enterRegion:(CLCircularRegion *)region completionHandler:(void (^)(NSError * _Nullable))completionHandler {
    [self enterOrLeaveRegion:region status:@"enter" completionHandler:completionHandler];
}

- (void)leaveRegion:(CLCircularRegion *)region completionHandler:(void (^)(NSError * _Nullable))completionHandler {
    [self enterOrLeaveRegion:region status:@"leave" completionHandler:completionHandler];
}

- (void)enterOrLeaveRegion:(CLCircularRegion *)region status:(NSString *)status completionHandler:(void (^)(NSError * _Nullable))completionHandler {
    
    NSDictionary *serverInfo = [GizGeofenceArchiver getServerInfo];
    
    if (!serverInfo) {
        NSError *error = [NSError errorWithDomain:@"com.gizwits.geofencing" code:9999 userInfo:@{NSLocalizedDescriptionKey: @"没有服务器配置信息"}];
        if (completionHandler) {
            completionHandler(error);
        }
        return;
    }
    
    NSString *url = serverInfo[@"url"];
    
    if (!url || url.length == 0) {
        NSError *error = [NSError errorWithDomain:@"com.gizwits.geofencing" code:9999 userInfo:@{NSLocalizedDescriptionKey: @"服务器地址为空"}];
        if (completionHandler) {
            completionHandler(error);
        }
        return;
    }
    
    NSString *version = serverInfo[@"version"] ?: @"";
    NSString *token = serverInfo[@"token"] ?: @"";
    NSString *appKey = serverInfo[@"appKey"] ?: @"";
    NSString *type = serverInfo[@"type"] ?: @"";
    NSString *conditionId = region.identifier;
    
    NSMutableDictionary *bodyDict = [[NSMutableDictionary alloc] init];
    [bodyDict setObject:appKey forKey:@"appKey"];
    [bodyDict setObject:type forKey:@"type"];
    [bodyDict setObject:version forKey:@"version"];
    
    NSArray *dataArray = @[@{@"conditionId": conditionId, @"status": status}];
    [bodyDict setObject:dataArray forKey:@"data"];
    
    NSURLSessionConfiguration *sessionConfiguration = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:sessionConfiguration];
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]];
    request.HTTPMethod = @"POST";
    [request setValue:version forHTTPHeaderField:@"Version"];
    [request setValue:token forHTTPHeaderField:@"Authorization"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    NSData *bodyData = [NSJSONSerialization dataWithJSONObject:bodyDict options:kNilOptions error:NULL];
    request.HTTPBody = bodyData;
    
    GeofenceLog(@"上报区域事件 request => %@", request);
    GeofenceLog(@"上报区域事件 serverInfo => %@, body => %@", serverInfo, bodyDict);
    
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        
        if (data) {
            id json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:NULL];
            GeofenceLog(@"上报区域事件 response data => %@", json);
        }
        
        GeofenceLog(@"上报区域事件 response => %@", response);
        GeofenceLog(@"上报区域事件 error => %@", error);

        if (completionHandler) {
            completionHandler(error);
        }
    }];
    
    [dataTask resume];
}

@end
