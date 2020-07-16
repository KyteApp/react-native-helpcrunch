#import "RNHelpCrunch.h"
@implementation RNHelpCrunch

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(init:(NSString *)organization appId:(NSString *)applicationId appSecret:(NSString *)applicationSecret registerForNotification:(BOOL *)registerForNotifications preChatForm:(NSDictionary *)preChatFields resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    HCSConfiguration *configuration = [HCSConfiguration configurationForOrganization:organization
                                                        applicationId:applicationId
                                                        applicationSecret:applicationSecret];
    if (preChatFields != nil) {
        NSMutableArray *preChatConfig = [[NSMutableArray alloc] init];
        for (NSString *key in preChatFields) {
            BOOL isRequired = preChatFields[key][@"isRequired"];
            if ([key isEqualToString:@"name"]) {
                [preChatConfig addObject:[HCSUserAttribute nameAttributeAsRequired:isRequired]];
            }else if ([key isEqualToString:@"email"]) {
                [preChatConfig addObject:[HCSUserAttribute emailAttributeAsRequired:isRequired]];
            } else if ([key isEqualToString:@"company"]) {
                [preChatConfig addObject:[HCSUserAttribute companyAttributeAsRequired:isRequired]];
            } else if ([key isEqualToString:@"phone"]) {
                [preChatConfig addObject:[HCSUserAttribute phoneAttributeAsRequired:isRequired]];
            } else {
                [preChatConfig addObject:[[HCSUserAttribute alloc] initWithAttributeName:key placeholder:preChatFields[key][@"placeholder"] required:isRequired]];
            }
        }
        configuration.userAttributes = preChatConfig;
    }


    if (registerForNotifications) {
        configuration.shouldUsePushNotificationDelegate = true;
    }

    [HelpCrunch initWithConfiguration:configuration user:nil completion:^(NSError * _Nullable error) {
        if (error != nil) {
            reject([@(error.code) stringValue], @"Error while trying to initialise HelpCrunch.", error);
        }

        if (registerForNotifications) {
            [HelpCrunch registerForRemoteNotifications];
        }

        resolve(nil);
    }];
}

RCT_EXPORT_METHOD(updateUser:(NSDictionary *)user resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *userId = [RCTConvert NSString:user[@"userId"]];
    NSString *userName = [RCTConvert NSString:user[@"userName"]];
    NSString *userEmail = [RCTConvert NSString:user[@"userEmail"]];
    HCSUser *helpcrunchUser = [HCSUser new];
    helpcrunchUser.userId = userId;
    helpcrunchUser.name = userName;
    helpcrunchUser.email = userEmail;

    if ([user objectForKey:@"customData"] != [NSNull null] && [[user objectForKey:@"customData"] count] > 0) { // checking if there's any value inside customData object
        helpcrunchUser.customData = [user objectForKey:@"customData"];
    }
    [HelpCrunch updateUser:helpcrunchUser completion:^(NSError * _Nullable error) {
        resolve(nil);
    }];
}

RCT_EXPORT_METHOD(showChat:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    RCTExecuteOnMainQueue(^{
        [HelpCrunch showFromController:RCTPresentedViewController() completion:^(NSError * _Nullable error) {
            resolve(nil);
        }];
    });
}

RCT_EXPORT_METHOD(setThemeConfiguration:(NSDictionary *)themeConfiguration)
{
    HCSTheme *theme = [HelpCrunch lightTheme];
    if (themeConfiguration[@"themeConfig"] != nil) {
        for (NSString* themeKey in themeConfiguration[@"themeConfig"]) {
            if ([themeKey isEqualToString:@"preChatThemeConfig"]) {
                theme.prechatForm = [self setPreChatTheme:themeConfiguration[@"themeConfig"][themeKey]];
            }

            if ([themeKey isEqualToString:@"chatThemeConfig"]) {
                theme.chatArea = [self setChatTheme:themeConfiguration[@"themeConfig"][themeKey]];
            }

            if ([themeKey isEqualToString:@"navigationBarThemeConfig"]) {
                theme.navigationBar = [self setNavigationBarTheme:themeConfiguration[@"themeConfig"][themeKey]];
            }

            if ([themeKey isEqualToString:@"sendMessageThemeConfig"]) {
                theme.sendMessageArea = [self setSendMessageAreaTheme:themeConfiguration[@"themeConfig"][themeKey]];
            }
        }
    }

    [HelpCrunch bindTheme:theme];
}

RCT_EXPORT_METHOD(trackEvent:(NSString *)eventName params:(NSDictionary *)eventParams)
{
    [HelpCrunch trackEvent:eventName data:eventParams];
}

RCT_EXPORT_METHOD(logout:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [HelpCrunch logoutWithCompletion:^(NSError * _Nullable error) {
        resolve(nil);
    }];
}

RCT_EXPORT_METHOD(getNumberOfUnreadChats:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@{ @"numberOfUnreadChats": [NSString stringWithFormat:@"%lu", (unsigned long)[HelpCrunch numberOfUnreadChats]] });
}

- (HCSThemePrechatForm *) setPreChatTheme:(NSDictionary *)themeProperties {
    HCSThemePrechatForm *prechatTheme = [[HCSThemePrechatForm alloc] init];
    for (NSString* configKey in themeProperties) {
        if ([themeProperties[configKey] isKindOfClass:[NSString class]] && [themeProperties[configKey] hasPrefix:@"#"]) { // this is supposed to be a color
            [prechatTheme setValue:[self colorFromHexString:[RCTConvert NSString:themeProperties[configKey]]] forKey:configKey];
        } else {
            [prechatTheme setValue:themeProperties[configKey] forKey:configKey];
        }
    }
    return prechatTheme;
}

- (HCSThemeChatArea *) setChatTheme:(NSDictionary *)themeProperties {
    HCSThemeChatArea *chatTheme = [[HCSThemeChatArea alloc] init];
    for (NSString* configKey in themeProperties) {
        if ([themeProperties[configKey] isKindOfClass:[NSString class]] && [themeProperties[configKey] hasPrefix:@"#"]) { // this is supposed to be a color
            [chatTheme setValue:[self colorFromHexString:[RCTConvert NSString:themeProperties[configKey]]] forKey:configKey];
        } else {
            [chatTheme setValue:themeProperties[configKey] forKey:configKey];
        }
    }
    return chatTheme;
}

- (HCSThemeNavigationBar *) setNavigationBarTheme:(NSDictionary *)themeProperties {
    HCSThemeNavigationBar *navigationBarTheme = [[HCSThemeNavigationBar alloc] init];
    for (NSString* configKey in themeProperties) {
        if ([themeProperties[configKey] isKindOfClass:[NSString class]] && [themeProperties[configKey] hasPrefix:@"#"]) { // this is supposed to be a color
            [navigationBarTheme setValue:[self colorFromHexString:[RCTConvert NSString:themeProperties[configKey]]] forKey:configKey];
        } else {
            [navigationBarTheme setValue:themeProperties[configKey] forKey:configKey];
        }
    }
    return navigationBarTheme;
}

- (HCSThemeSendMessageArea *) setSendMessageAreaTheme:(NSDictionary *)themeProperties {
    HCSThemeSendMessageArea *sendMessageAreaTheme = [[HCSThemeSendMessageArea alloc] init];
    for (NSString* configKey in themeProperties) {
        if ([themeProperties[configKey] isKindOfClass:[NSString class]] && [themeProperties[configKey] hasPrefix:@"#"]) { // this is supposed to be a color
            [sendMessageAreaTheme setValue:[self colorFromHexString:[RCTConvert NSString:themeProperties[configKey]]] forKey:configKey];
        } else {
            [sendMessageAreaTheme setValue:themeProperties[configKey] forKey:configKey];
        }
    }
    return sendMessageAreaTheme;
}

- (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1];
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

@end
