//
//  RCTOpenInstall.m
//  RCTOpenInstall
//
//  Created by cooper on 2018/11/26.
//  Copyright © 2018年 openinstall. All rights reserved.
//

#import "RCTOpenInstall.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTEventDispatcher.h>
#import <React/RCTRootView.h>
#import <React/RCTBridge.h>
#import <React/RCTLog.h>

#elif __has_include("RCTBridge.h")
#import "RCTEventDispatcher.h"
#import "RCTRootView.h"
#import "RCTBridge.h"
#import "RCTLog.h"

#elif __has_include("React/RCTBridge.h")
#import "React/RCTEventDispatcher.h"
#import "React/RCTRootView.h"
#import "React/RCTBridge.h"
#import "React/RCTLog.h"
#endif

#define OpeninstallWakeupCallBack @"OpeninstallWakeupCallBack"

@interface RCTOpenInstall ()
@property (nonatomic, strong)NSDictionary *wakeUpParams;
@property (nonatomic, assign)BOOL wakeupStat;
@property (nonatomic, assign)BOOL initStat;
@property (nonatomic, strong)NSURL *handleURL;
@property (nonatomic, strong)NSUserActivity *userActivity;
@end

@implementation RCTOpenInstall

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(OpeninstallModule);

/*
static RCTOpenInstall *sharedInstance = nil;
+ (instancetype)shareInstance{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (sharedInstance == nil) {
            sharedInstance = [[RCTOpenInstall alloc]init];
            sharedInstance.wakeUpParams = [[NSDictionary alloc] init];
        }
    });
    return sharedInstance;
}
*/
- (instancetype)init{
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (void)initOpenInstall:(NSDictionary *)params{
    //RCTOpenInstall *shared = [RCTOpenInstall shareInstance];
    if (!self.initStat){
        self.initStat = YES;
        [OpenInstallSDK initWithDelegate:self];
    }
    [self check];
}

RCT_EXPORT_METHOD(initSDK:(NSDictionary *)params)
{
    [self initOpenInstall:params];
}

RCT_EXPORT_METHOD(getInstall:(int)s completion:(RCTResponseSenderBlock)callback)
{
    NSTimeInterval time = 10.0f;
    if (s>0) {
        time = s;
    }
    [[OpenInstallSDK defaultManager] getInstallParmsWithTimeoutInterval:time completed:^(OpenInstallData * _Nullable appData) {
        
        if (!appData.data&&!appData.channelCode) {
            NSArray *params = @[[NSNull null]];
            callback(params);
            return;
        }
        BOOL shouldRetry = NO;
        if (appData.opCode==OPCode_timeout) {
            shouldRetry = YES;
        }
        NSDictionary *dic = @{@"channel":appData.channelCode?:@"",@"data":appData.data?:@"",@"shouldRetry":@(shouldRetry)};
        NSArray *params = @[dic];
        callback(params);
    }];
}

- (void)getWakeUpParams:(OpenInstallData *)appData{
    //RCTBridge *bri = [[RCTBridge alloc]initWithDelegate:self launchOptions:nil];
    if (!appData.data&&!appData.channelCode) {
        
        if (self.bridge) {
//            [self.bridge.eventDispatcher sendAppEventWithName:OpeninstallWakeupCallBack body:nil];
            [self.bridge enqueueJSCall:@"RCTDeviceEventEmitter" method:@"emit" args:@[OpeninstallWakeupCallBack] completion:NULL];
        }
        return;
    }
    NSDictionary *params = @{@"channel":appData.channelCode?:@"",@"data":appData.data?:@""};
    if (self.bridge) {
//        [self.bridge.eventDispatcher sendAppEventWithName:OpeninstallWakeupCallBack body:params];
        [self.bridge enqueueJSCall:@"RCTDeviceEventEmitter" method:@"emit" args:@[OpeninstallWakeupCallBack,params] completion:NULL];
    }
    @synchronized(self){
        self.wakeUpParams = params;
    }
}

RCT_EXPORT_METHOD(getWakeUp:(RCTResponseSenderBlock)callback)
{
    //[RCTOpenInstall shareInstance];
//    if (!self.wakeupStat) {
        if (self.wakeUpParams.count != 0) {
            NSArray *params = @[self.wakeUpParams];
            callback(params);
        }else{
            callback(@[[NSNull null]]);
        }
//        self.wakeupStat = YES;
//    }
}

RCT_EXPORT_METHOD(reportRegister)
{
    [OpenInstallSDK reportRegister];
}

RCT_EXPORT_METHOD(reportEffectPoint:(NSString *)effectID effectValue:(NSInteger)effectValue)
{
    [[OpenInstallSDK defaultManager] reportEffectPoint:effectID effectValue:effectValue];
}

RCT_EXPORT_METHOD(reportEffectPoint:(NSString *)effectID effectValue:(NSInteger)effectValue effectDictionary:(NSDictionary *)params)
{
    [[OpenInstallSDK defaultManager] reportEffectPoint:effectID effectValue:effectValue effectDictionary:params];
}

RCT_EXPORT_METHOD(reportShare:(NSString *)shareCode reportPlatform:(NSString *)platform completion:(RCTResponseSenderBlock)callback)
{
    [[OpenInstallSDK defaultManager] reportShareParametersWithShareCode:shareCode
                                                          sharePlatform:platform
                                                              completed:^(NSInteger code, NSString * _Nullable msg)
    {
        BOOL shouldRetry = NO;
        if (code==-1){
            shouldRetry = YES;
        }
        NSDictionary *dic = @{@"shouldRetry":@(shouldRetry),@"message":msg};
        NSArray *params = @[dic];
        callback(params);
    }];
    
}


- (void)handLinkURL:(NSURL *)url{
    [self wakeupParamStored:url];
}

- (void)continueUserActivity:(NSUserActivity *)userActivity{
    [self wakeupParamStored:userActivity];
}

- (void)wakeupParamStored:(id)handle{
    //[RCTOpenInstall shareInstance];
    if (self.initStat) {
        if ([handle isKindOfClass:[NSURL class]]) {
            [OpenInstallSDK handLinkURL:(NSURL *)handle];
        }else{
            [OpenInstallSDK continueUserActivity:(NSUserActivity *)handle];
        }
    }else{
        if ([handle isKindOfClass:[NSURL class]]) {
            self.handleURL = (NSURL *)handle;
        }else{
            self.userActivity = (NSUserActivity *)handle;
        }
    }
}

- (void)check{
    if (self.handleURL) {
        [OpenInstallSDK handLinkURL:self.handleURL];
        self.handleURL = nil;
    }
    if (self.userActivity) {
        [OpenInstallSDK continueUserActivity:self.userActivity];
        self.userActivity = nil;
    }
}
 
@end
