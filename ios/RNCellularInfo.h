
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import "RCTLog.h"
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTLog.h>
#import <React/RCTEventEmitter.h>
#endif

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "Reachability.h"


@interface RNCellularInfo : RCTEventEmitter <RCTBridgeModule> {
    NSString* type;
    
    Reachability* internetReach;
    
}

@property (copy) NSString* connectionType;
@property (strong) Reachability* internetReach;

@end

