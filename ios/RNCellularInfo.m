
#import "RNCellularInfo.h"
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTCarrier.h>

@implementation RNCellularInfo

@synthesize connectionType, internetReach;

// https://github.com/apache/cordova-plugin-network-information/blob/master/src/ios/CDVConnection.m

CTTelephonyNetworkInfo *telephonyInfo;

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"connectionTypeUpdated"];
}


- (id)init
{
    self = [super init];
    
    if (self) {
        
        self.connectionType = @"none";
        self.internetReach = [Reachability reachabilityForInternetConnection];
        self.connectionType = [self getConnectionTypeFor:self.internetReach];
        [self.internetReach startNotifier];
        
        // Initialization code here.
        telephonyInfo = [CTTelephonyNetworkInfo new];
        
        
         [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateConnectionType:)
         name:kReachabilityChangedNotification object:nil];
         
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateConnectionType:)
                                                     name:CTRadioAccessTechnologyDidChangeNotification object:nil];
        
        if (UIApplicationDidEnterBackgroundNotification && UIApplicationWillEnterForegroundNotification) {
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onPause) name:UIApplicationDidEnterBackgroundNotification object:nil];
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onResume) name:UIApplicationWillEnterForegroundNotification object:nil];
        }
    }
    
    return self;
}

- (NSString*)getConnectionTypeFor:(Reachability*)reachability
{
    NetworkStatus networkStatus = [reachability currentReachabilityStatus];
    
    switch (networkStatus) {
        case NotReachable:
            return @"none";
            
        case ReachableViaWWAN:
        {
            BOOL isConnectionRequired = [reachability connectionRequired];
            if (isConnectionRequired) {
                return @"none";
            } else {
                if ([[[UIDevice currentDevice] systemVersion] compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending) {
                    CTTelephonyNetworkInfo *telephonyInfo = [CTTelephonyNetworkInfo new];
                    if ([telephonyInfo.currentRadioAccessTechnology isEqualToString:CTRadioAccessTechnologyGPRS]) {
                        return @"2g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyEdge]) {
                        return @"2g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyWCDMA]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyHSDPA]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyHSUPA]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyCDMA1x]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyCDMAEVDORev0]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyCDMAEVDORevA]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyCDMAEVDORevB]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyeHRPD]) {
                        return @"3g";
                    } else if ([telephonyInfo.currentRadioAccessTechnology  isEqualToString:CTRadioAccessTechnologyLTE]) {
                        return @"4g";
                    }
                }
                return @"cellular";
            }
        }
        case ReachableViaWiFi:
            return @"wifi";
            
        default:
            return @"unknown";
    }
}

- (void)updateConnectionType
{
    NSLog(@"New Radio Access Technology: %@", telephonyInfo.currentRadioAccessTechnology);
    
    //[self.internetReach stopNotifier];
}

- (BOOL)isCellularConnection:(NSString*)theConnectionType
{
    return [theConnectionType isEqualToString:@"2g"] ||
    [theConnectionType isEqualToString:@"3g"] ||
    [theConnectionType isEqualToString:@"4g"] ||
    [theConnectionType isEqualToString:@"cellular"];
}

- (void)onPause
{
    [self.internetReach stopNotifier];
}

- (void)onResume
{
    [self.internetReach startNotifier];
    [self updateReachability:self.internetReach];
}

- (void)updateReachability:(Reachability*)reachability
{
    if (reachability) {
        // check whether the connection type has changed
        NSString* newConnectionType = [self getConnectionTypeFor:reachability];
        if ([newConnectionType isEqualToString:self.connectionType]) { // the same as before, remove dupes
            return;
        } else {
            self.connectionType = [self getConnectionTypeFor:reachability];
        }
    }
    [self sendEventWithName:@"connectionTypeUpdated" body:@{@"type": self.connectionType}];
}

- (void)updateConnectionType:(NSNotification*)note
{
    Reachability* curReach = [note object];
    
    if ((curReach != nil) && [curReach isKindOfClass:[Reachability class]]) {
        [self updateReachability:curReach];
    }
}


RCT_REMAP_METHOD(getConnectionType,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@{@"type": self.connectionType});
}

RCT_EXPORT_METHOD(carrierName:(RCTResponseSenderBlock)callback)
{
    CTTelephonyNetworkInfo *nInfo = [[CTTelephonyNetworkInfo alloc] init];
    NSString *carrierName = [[nInfo subscriberCellularProvider] carrierName];
    if(carrierName)
    {
        callback(@[carrierName]);
    }
    else
    {
        callback(@[@"nil"]);
    }
}

RCT_EXPORT_METHOD(isoCountryCode:(RCTResponseSenderBlock)callback)
{
    CTTelephonyNetworkInfo *nInfo = [[CTTelephonyNetworkInfo alloc] init];
    NSString *iso = [[nInfo subscriberCellularProvider] isoCountryCode];
    if(iso)
    {
        callback(@[iso]);
    }
    else
    {
        callback(@[@"nil"]);
    }
}

RCT_EXPORT_METHOD(mobileCountryCode:(RCTResponseSenderBlock)callback)
{
    CTTelephonyNetworkInfo *nInfo = [[CTTelephonyNetworkInfo alloc] init];
    NSString *mcc = [[nInfo subscriberCellularProvider] mobileCountryCode];
    if(mcc)
    {
        callback(@[mcc]);
    }
    else
    {
        callback(@[@"nil"]);
    }
}

RCT_EXPORT_METHOD(mobileNetworkCode:(RCTResponseSenderBlock)callback)
{
    CTTelephonyNetworkInfo *nInfo = [[CTTelephonyNetworkInfo alloc] init];
    NSString *mnc = [[nInfo subscriberCellularProvider] mobileNetworkCode];
    if(mnc)
    {
        callback(@[mnc]);
    }
    else
    {
        callback(@[@"nil"]);
    }
}


@end
  
