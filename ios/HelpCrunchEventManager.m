 #import "HelpCrunchEventManager.h"

 @interface HelpCrunchEventManager ()

 @end

 @implementation HelpCrunchEventManager

 RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
  return @[
      @"HCSURLNotification",
      @"HCSImageURLNotification",
      @"HCSFileURLNotification",
      @"HCSUserStartedChatNotification",
      @"HCSUserClosedChatNotification",
      @"HCSUnreadMessagesNotification"
  ];
}
- (void)startObserving {
    for (NSString *event in [self supportedEvents]) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                         selector:@selector(handleNotification:)
                                         name:event
                                         object:nil];
    }
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)handleNotification:(NSNotification *)notification {
    [self sendEventWithName:notification.name body:notification.userInfo];
}

 @end
