//
//  GizGeofenceLogging.h
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/9/4.
//

#import <Foundation/Foundation.h>


FOUNDATION_EXTERN void GeofenceLog(NSString *format, ...) NS_FORMAT_FUNCTION(1, 2) NS_NO_TAIL_CALL;


NS_ASSUME_NONNULL_BEGIN

@interface GizGeofenceLogging : NSObject

@end

NS_ASSUME_NONNULL_END
