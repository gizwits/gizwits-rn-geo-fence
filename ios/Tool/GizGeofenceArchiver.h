//
//  GizGeofenceArchiver.h
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 归档工具
 */
@interface GizGeofenceArchiver : NSObject

+ (void)archiveToken:(nullable NSString *)token;
+ (nullable NSString *)getToken;

+ (void)archiveServerInfo:(nullable NSDictionary *)serverInfo;
+ (nullable NSDictionary *)getServerInfo;

+ (void)archiveAddressSearchHistory:(NSDictionary *)addressDict;
+ (nullable NSArray<NSDictionary *> *)getAddressSearchHistories;
+ (void)deleteAddressSearchHistoryAtIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
