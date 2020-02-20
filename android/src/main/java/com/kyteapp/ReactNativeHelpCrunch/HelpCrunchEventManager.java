package com.kyteapp.ReactNativeHelpCrunch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.helpcrunch.library.core.Callback;
import com.helpcrunch.library.core.HelpCrunch;

import org.jetbrains.annotations.NotNull;

public class HelpCrunchEventManager extends BroadcastReceiver {
    private final ReactApplicationContext reactContext;
    public HelpCrunchEventManager(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        HelpCrunch.Event parcelableExtra = (HelpCrunch.Event) intent.getSerializableExtra(HelpCrunch.EVENT_TYPE);
        HelpCrunch.Screen screen = (HelpCrunch.Screen) intent.getSerializableExtra(HelpCrunch.SCREEN_TYPE);

        if (parcelableExtra == null) {
            return;
        }
        final DeviceEventManagerModule.RCTDeviceEventEmitter eventEmmiter = this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        switch (parcelableExtra) {
            case SCREEN_CLOSED:
                eventEmmiter.emit("HCSUserClosedChatWindow", null);
                break;
            case SCREEN_OPENED:
                eventEmmiter.emit("HCSUserOpenedChatWindow", null);
                break;
            case ON_IMAGE_URL:
                eventEmmiter.emit("HCSImageURLNotification", null);
                break;
            case ON_FILE_URL:
                eventEmmiter.emit("HCSFileURLNotification", null);
                break;
            case ON_ANY_OTHER_URL:
                eventEmmiter.emit("HCSURLNotification", null);
                break;
            case ON_UNREAD_COUNT_CHANGED:
                HelpCrunch.getUnreadMessagesCount(new Callback<Integer>() {
                    @Override
                    public void onError(@NotNull String message) {
                    }

                    @Override
                    public void onSuccess(Integer result) {
                        WritableMap data = Arguments.createMap();
                        data.putInt("data", result);
                        eventEmmiter.emit("HCSUnreadMessagesNotification", data);
                    }
                });
                break;
        }
    }
}
