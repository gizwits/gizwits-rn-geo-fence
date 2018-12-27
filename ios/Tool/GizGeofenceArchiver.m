//
//  GizGeofenceArchiver.m
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/8/27.
//

#import "GizGeofenceArchiver.h"

@implementation GizGeofenceArchiver

+ (void)archiveToken:(NSString *)token {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:token forKey:@"GizGeofenceToken"];
    [userDefaults synchronize];
}

+ (NSString *)getToken {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    return [userDefaults objectForKey:@"GizGeofenceToken"];
}

+ (void)archiveServerInfo:(NSDictionary *)serverInfo {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:serverInfo forKey:@"GizGeofenceServerInfo"];
    [userDefaults synchronize];
}

+ (NSDictionary *)getServerInfo {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    return [userDefaults objectForKey:@"GizGeofenceServerInfo"];
}

+ (NSString *)addressSearchHistoriesArchivePath {
    NSString *path = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).lastObject;
    path = [path stringByAppendingPathComponent:@"cordova-gizwits-geofencing"];
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:NULL];
    }
    
    path = [path stringByAppendingPathComponent:@"AddressSearchHistoriestian"];
    
    return path;
}

+ (void)archiveAddressSearchHistory:(NSDictionary *)addressDict {
    
    if (!addressDict || addressDict.count == 0) {
        return;
    }
    
    NSMutableArray<NSDictionary *> *searchHistories;
    
    NSArray<NSDictionary *> *archiveArray = [self getAddressSearchHistories];
    
    if (archiveArray) {
        searchHistories = [archiveArray mutableCopy];
        [searchHistories insertObject:addressDict atIndex:0];
    } else {
        searchHistories = [[NSMutableArray alloc] init];
        [searchHistories addObject:addressDict];
    }
    
    if (![NSKeyedArchiver archiveRootObject:searchHistories toFile:[self addressSearchHistoriesArchivePath]]) {
        NSLog(@"归档地址搜索历史失败 => %@", searchHistories);
    }
}

+ (NSArray<NSDictionary *> *)getAddressSearchHistories {
    NSArray<NSDictionary *> *searchHistories = (NSArray<NSDictionary *> *)[NSKeyedUnarchiver unarchiveObjectWithFile:[self addressSearchHistoriesArchivePath]];
    return searchHistories;
}

+ (void)deleteAddressSearchHistoryAtIndex:(NSInteger)index {
    
    if (index < 0) {
        return;
    }
    
    NSMutableArray<NSDictionary *> *searchHistories;
    
    NSArray<NSDictionary *> *archiveArray = [self getAddressSearchHistories];
    
    if (!archiveArray || archiveArray.count <= index) {
        return;
    }
    
    searchHistories = [archiveArray mutableCopy];
    [searchHistories removeObjectAtIndex:index];
    
    if (![NSKeyedArchiver archiveRootObject:searchHistories toFile:[self addressSearchHistoriesArchivePath]]) {
        NSLog(@"归档地址搜索历史失败 => %@", searchHistories);
    }
}

@end
