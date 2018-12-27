//
//  GizGeofenceLogging.m
//  GizwitsGeofence
//
//  Created by Gizwits on 2018/9/4.
//

#import "GizGeofenceLogging.h"

@implementation GizGeofenceLogging

+ (NSString *)logPath {
    NSString *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).lastObject;
    path = [path stringByAppendingPathComponent:@"GizGeofence"];
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    if (![fileManager fileExistsAtPath:path isDirectory:NULL]) {
        [fileManager createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:NULL];
    }
    
    return path;
}

+ (NSString *)logFileName {
    return [[self logPath] stringByAppendingPathComponent:@"log.txt"];
}

@end


const char *GeofenceLogFileName() {
    return [[GizGeofenceLogging logFileName] cStringUsingEncoding:NSUTF8StringEncoding];
}

void GeofenceLog(NSString *format, ...) {
    va_list args;
    va_start(args, format);
    
    NSString *formattedString = [[NSString alloc] initWithFormat:format arguments:args];
    formattedString = [@"[GizGeofenceLogging] " stringByAppendingString:formattedString];
    
    FILE *out_stream = fopen(GeofenceLogFileName(), "a+");
    
    fprintf(out_stream, "%s\n", [formattedString cStringUsingEncoding:NSUTF8StringEncoding]);
    printf("%s\n", [formattedString cStringUsingEncoding:NSUTF8StringEncoding]);
    
    fclose(out_stream);
    va_end(args);
}
