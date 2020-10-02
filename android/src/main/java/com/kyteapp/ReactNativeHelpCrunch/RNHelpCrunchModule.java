package com.kyteapp.ReactNativeHelpCrunch;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.helpcrunch.library.core.HelpCrunch;
import com.helpcrunch.library.core.Callback;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.helpcrunch.library.core.models.user.HCUser;
import com.helpcrunch.library.core.options.HCOptions;
import com.helpcrunch.library.core.options.HCPreChatForm;
import com.helpcrunch.library.core.options.design.HCAvatarTheme;
import com.helpcrunch.library.core.options.design.HCChatAreaTheme;
import com.helpcrunch.library.core.options.design.HCNotificationsTheme;
import com.helpcrunch.library.core.options.design.HCPreChatTheme;
import com.helpcrunch.library.core.options.design.HCSystemAlertsTheme;
import com.helpcrunch.library.core.options.design.HCTheme;
import com.helpcrunch.library.core.options.design.HCToolbarAreaTheme;
import com.helpcrunch.library.core.options.design.HCMessageAreaTheme;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RNHelpCrunchModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private HCTheme.Builder themeBuilder;
    private HCPreChatForm.Builder preChatBuilder;
    private String hostAppPackageName;
    private boolean eventsAlreadyRegistered = false;

    public RNHelpCrunchModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNHelpCrunch";
    }

    @ReactMethod
    public void init(String organization, String applicationId, String applicationSecret, ReadableMap preChatFields, String hostAppPackageName, final Promise promise) {
        this.hostAppPackageName = hostAppPackageName;
        HCOptions.Builder optionsBuilder = new HCOptions.Builder();
        if (preChatFields != null) {
            HCPreChatForm.Builder preChatForm = new HCPreChatForm.Builder();
            HashMap preChat = preChatFields.toHashMap();
            Iterator iterator = preChat.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry field = (Map.Entry)iterator.next();
                HashMap fieldData = (HashMap) field.getValue();

                String fieldName = (String) field.getKey();
                boolean isRequired = fieldData.containsKey("isRequired") && (boolean)fieldData.get("isRequired");

                if (fieldName.equals("email")) {
                    preChatForm.withEmail(isRequired, null);
                }else if (fieldName.equals("company")) {
                    preChatForm.withCompany(isRequired, null);
                }else if (fieldName.equals("phone")) {
                    preChatForm.withPhone(isRequired, null);
                } else {
                    String placeholder = fieldData.containsKey("placeholder") ? (String)fieldData.get("placeholder") : "";
                    preChatForm.withField((String)field.getKey(), placeholder, isRequired);
                }
                iterator.remove();
            }
            this.preChatBuilder = preChatForm;
            optionsBuilder.setPreChatForm(preChatForm.build());
        }

        HelpCrunch.initialize(organization, Integer.parseInt(applicationId), applicationSecret, null, optionsBuilder.build(), new Callback<Object>() {
            @Override
            public void onSuccess(Object result) {
                promise.resolve(null);
            }

            @Override
            public void onError(@NotNull String message) {
                promise.reject("InitializeError", message);
            }
        });

        promise.resolve(null);
    }

    @ReactMethod
    public void updateUser(ReadableMap user, final Promise promise) {
        HCUser.Builder helpcrunchUserBuilder = new HCUser.Builder();
        if (user.hasKey("userId")) {
            helpcrunchUserBuilder.withUserId(user.getString("userId"));
        }

        if (user.hasKey("userName")) {
            helpcrunchUserBuilder.withName(user.getString("userName"));
        }

        if (user.hasKey("userEmail")) {
            helpcrunchUserBuilder.withEmail(user.getString("userEmail"));
        }

        if (user.hasKey("userPhone")) {
            helpcrunchUserBuilder.withPhone(user.getString("userPhone"));
        }

        if (user.hasKey("userCompany")) {
            helpcrunchUserBuilder.withCompany(user.getString("userCompany"));
        }

        if (user.hasKey("customData")) {
            helpcrunchUserBuilder.withCustomData(user.getMap("customData").toHashMap());
        }

        HelpCrunch.updateUser(helpcrunchUserBuilder.build(), new Callback<HCUser>() {
            @Override
            public void onSuccess(HCUser result) {
                promise.resolve(null);
            }
            @Override
            public void onError(@NotNull String message) {
                promise.reject("UpdateUserError", message);
            }
        });
    }

    @ReactMethod
    public void showChat() {
        HCOptions.Builder optionsBuilder = new HCOptions.Builder();
        if (this.themeBuilder != null) {
            HCTheme theme = this.themeBuilder.build();
            optionsBuilder.setTheme(theme);
        }

        if (this.preChatBuilder != null) {
            HCPreChatForm preChat = this.preChatBuilder.build();
            optionsBuilder.setPreChatForm(preChat);
        }

        HCOptions options = optionsBuilder.build();
        Activity currentActivity = this.getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        HelpCrunch.showChatScreen(options);
    }

    @ReactMethod
    public void setThemeConfiguration(Promise promise) {
        Activity currentActivity = this.getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("RNActivityNotFound", "RN Activity wasn't found. :(");
            return;
        }

        PackageManager packageManager = this.getCurrentActivity().getPackageManager();
        Resources resources = null;
        try {
            resources = packageManager.getResourcesForApplication(this.hostAppPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            promise.reject("HelpCrunchThemeConfigError", e.getMessage());
        }

        Integer helpCrunchMainColor = resources.getIdentifier("helpCrunchMainColor", "color", this.hostAppPackageName);
        HCTheme.Builder themeBuilder = new HCTheme.Builder(helpCrunchMainColor, true);

        // PreChat theme
        HashMap<String, Integer> preChatThemeList = new HashMap<>();
        preChatThemeList.put("helpCrunchInputFieldTextColor", resources.getIdentifier("helpCrunchInputFieldTextColor", "color", this.hostAppPackageName));
        preChatThemeList.put("helpCrunchInputFieldTextHintColor", resources.getIdentifier("helpCrunchInputFieldTextHintColor", "color", this.hostAppPackageName));
        preChatThemeList.put("helpCrunchBackgroundColor", resources.getIdentifier("helpCrunchBackgroundColor", "color", this.hostAppPackageName));
        preChatThemeList.put("helpCrunchMessageBackgroundColor", resources.getIdentifier("helpCrunchMessageBackgroundColor", "color", this.hostAppPackageName));
        preChatThemeList.put("helpCrunchMessageTextColor", resources.getIdentifier("helpCrunchMessageTextColor", "color", this.hostAppPackageName));

        HCPreChatTheme.Builder preChatThemeBuilder = new HCPreChatTheme.Builder();
        for (Object o : preChatThemeList.entrySet()) {
            Map.Entry preChatEntry = (Map.Entry) o;
            if (!preChatEntry.getValue().equals(0)) {
                switch ((String)preChatEntry.getKey()) {
                    case "helpCrunchInputFieldTextColor": preChatThemeBuilder.setInputFieldTextColor((Integer)preChatEntry.getValue()); break;
                    case "helpCrunchInputFieldTextHintColor": preChatThemeBuilder.setInputFieldTextHintColor((Integer)preChatEntry.getValue()); break;
                    case "helpCrunchBackgroundColor": preChatThemeBuilder.setBackgroundColor((Integer)preChatEntry.getValue()); break;
                    case "helpCrunchMessageBackgroundColor": preChatThemeBuilder.setMessageBackgroundColor((Integer)preChatEntry.getValue()); break;
                    case "helpCrunchMessageTextColor": preChatThemeBuilder.setMessageTextColor((Integer)preChatEntry.getValue()); break;
                }
            }
        }
        HCPreChatTheme preChatTheme = preChatThemeBuilder.build();
        themeBuilder.setPreChatTheme(preChatTheme);

        // Chat Area theme
        HashMap<String, Integer> chatAreaThemeList = new HashMap<>();
        chatAreaThemeList.put("helpCrunchIncomingBubbleTextColor", resources.getIdentifier("helpCrunchIncomingBubbleTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingBubbleTextColor", resources.getIdentifier("helpCrunchOutcomingBubbleTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingBubbleColor", resources.getIdentifier("helpCrunchIncomingBubbleColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingBubbleColor", resources.getIdentifier("helpCrunchOutcomingBubbleColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingBubbleLinkColor", resources.getIdentifier("helpCrunchIncomingBubbleLinkColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingBubbleLinkColor", resources.getIdentifier("helpCrunchOutcomingBubbleLinkColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingCodeBackgroundColor", resources.getIdentifier("helpCrunchIncomingCodeBackgroundColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingCodeBackgroundColor", resources.getIdentifier("helpCrunchOutcomingCodeBackgroundColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingCodeTextColor", resources.getIdentifier("helpCrunchIncomingCodeTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingCodeTextColor", resources.getIdentifier("helpCrunchOutcomingCodeTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingBlockQuoteColor", resources.getIdentifier("helpCrunchIncomingBlockQuoteColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingBlockQuoteColor", resources.getIdentifier("helpCrunchOutcomingBlockQuoteColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingFileTextColor", resources.getIdentifier("helpCrunchIncomingFileTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingFileTextColor", resources.getIdentifier("helpCrunchOutcomingFileTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchIncomingFileIconBackgroundColor", resources.getIdentifier("helpCrunchIncomingFileIconBackgroundColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchOutcomingFileIconBackgroundColor", resources.getIdentifier("helpCrunchOutcomingFileIconBackgroundColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchAuthorNameColor", resources.getIdentifier("helpCrunchAuthorNameColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchSystemMessageColor", resources.getIdentifier("helpCrunchSystemMessageColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchTimeTextColor", resources.getIdentifier("helpCrunchTimeTextColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchProgressViewsColor", resources.getIdentifier("helpCrunchProgressViewsColor", "color", this.hostAppPackageName));
        chatAreaThemeList.put("helpCrunchChatBackgroundColor", resources.getIdentifier("helpCrunchChatBackgroundColor", "color", this.hostAppPackageName));

        HCChatAreaTheme.Builder chatAreaThemeBuilder = new HCChatAreaTheme.Builder();
        for (Object o : chatAreaThemeList.entrySet()) {
            Map.Entry chatThemeEntry = (Map.Entry) o;
            if (!chatThemeEntry.getValue().equals(0)) {
                switch ((String)chatThemeEntry.getKey()) {
                    case "helpCrunchIncomingBubbleTextColor": chatAreaThemeBuilder.setIncomingBubbleTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingBubbleTextColor": chatAreaThemeBuilder.setOutcomingBubbleTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingBubbleColor": chatAreaThemeBuilder.setIncomingBubbleColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingBubbleColor": chatAreaThemeBuilder.setOutcomingBubbleColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingBubbleLinkColor": chatAreaThemeBuilder.setIncomingBubbleLinkColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingBubbleLinkColor": chatAreaThemeBuilder.setOutcomingBubbleLinkColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingCodeBackgroundColor": chatAreaThemeBuilder.setIncomingCodeBackgroundColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingCodeBackgroundColor": chatAreaThemeBuilder.setOutcomingCodeBackgroundColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingCodeTextColor": chatAreaThemeBuilder.setIncomingCodeTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingCodeTextColor": chatAreaThemeBuilder.setOutcomingCodeTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingBlockQuoteColor": chatAreaThemeBuilder.setIncomingBlockQuoteColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingBlockQuoteColor": chatAreaThemeBuilder.setOutcomingBlockQuoteColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingFileTextColor": chatAreaThemeBuilder.setIncomingFileTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingFileTextColor": chatAreaThemeBuilder.setOutcomingFileTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchIncomingFileIconBackgroundColor": chatAreaThemeBuilder.setIncomingFileIconBackgroundColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchOutcomingFileIconBackgroundColor": chatAreaThemeBuilder.setOutcomingFileIconBackgroundColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchAuthorNameColor": chatAreaThemeBuilder.setAuthorNameColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchSystemMessageColor": chatAreaThemeBuilder.setSystemMessageColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchTimeTextColor": chatAreaThemeBuilder.setTimeTextColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchProgressViewsColor": chatAreaThemeBuilder.setProgressViewsColor((Integer)chatThemeEntry.getValue()); break;
                    case "helpCrunchChatBackgroundColor": chatAreaThemeBuilder.setBackgroundColor((Integer)chatThemeEntry.getValue()); break;
                }
            }
        }

        // Avatar theme
        HashMap<String, Integer> avatarThemeList = new HashMap<>();
        avatarThemeList.put("helpCrunchAvatarPlaceholderBackgroundColor", resources.getIdentifier("helpCrunchAvatarPlaceholderBackgroundColor", "color", this.hostAppPackageName));
        avatarThemeList.put("helpCrunchAvatarPlaceholderTextColor", resources.getIdentifier("helpCrunchAvatarPlaceholderTextColor", "color", this.hostAppPackageName));

        HCAvatarTheme.Builder avatarThemeBuilder = new HCAvatarTheme.Builder();
        for (Object o : avatarThemeList.entrySet()) {
            Map.Entry avatarThemeEntry = (Map.Entry) o;
            if (!avatarThemeEntry.getValue().equals(0)) {
                switch ((String) avatarThemeEntry.getKey()) {
                    case "helpCrunchAvatarPlaceholderBackgroundColor": avatarThemeBuilder.setPlaceholderBackgroundColor((Integer) avatarThemeEntry.getValue()); break;
                    case "helpCrunchAvatarPlaceholderTextColor": avatarThemeBuilder.setPlaceholderTextColor((Integer) avatarThemeEntry.getValue()); break;
                }
            }
        }
        HCAvatarTheme avatarTheme = avatarThemeBuilder.build();
        chatAreaThemeBuilder.setAvatarTheme(avatarTheme);

        HCChatAreaTheme chatAreaTheme = chatAreaThemeBuilder.build();
        themeBuilder.setChatAreaTheme(chatAreaTheme);

        // Navigation Bar theme
        HashMap<String, Integer> navigationBarList = new HashMap<>();
        navigationBarList.put("helpCrunchNavigationBarBackgroundColor", resources.getIdentifier("helpCrunchNavigationBarBackgroundColor", "color", this.hostAppPackageName));
        navigationBarList.put("helpCrunchStatusBarColor", resources.getIdentifier("helpCrunchStatusBarColor", "color", this.hostAppPackageName));
        navigationBarList.put("helpCrunchOutlineColor", resources.getIdentifier("helpCrunchOutlineColor", "color", this.hostAppPackageName));
        navigationBarList.put("helpCrunchAgentsTextColor", resources.getIdentifier("helpCrunchAgentsTextColor", "color", this.hostAppPackageName));


        HCToolbarAreaTheme.Builder navigationBarThemeBuilder = new HCToolbarAreaTheme.Builder();
        navigationBarThemeBuilder.setAvatarTheme(avatarTheme);
        for (Object o : navigationBarList.entrySet()) {
            Map.Entry navigationBarThemeEntry = (Map.Entry) o;
            if (!navigationBarThemeEntry.getValue().equals(0)) {
                switch ((String)navigationBarThemeEntry.getKey()) {
                    case "helpCrunchNavigationBarBackgroundColor": navigationBarThemeBuilder.setBackgroundColor((Integer)navigationBarThemeEntry.getValue()); break;
                    case "helpCrunchStatusBarColor": navigationBarThemeBuilder.setStatusBarColor((Integer)navigationBarThemeEntry.getValue()); break;
                    case "helpCrunchOutlineColor": navigationBarThemeBuilder.setOutlineColor((Integer)navigationBarThemeEntry.getValue()); break;
                    case "helpCrunchAgentsTextColor": navigationBarThemeBuilder.setAgentsTextColor((Integer)navigationBarThemeEntry.getValue()); break;
                }
            }
        }
        HCToolbarAreaTheme toolbarAreaTheme = navigationBarThemeBuilder.build();
        themeBuilder.setToolbarAreaTheme(toolbarAreaTheme);

        // Message Area theme
        HashMap<String, Integer> messageAreaList = new HashMap<>();
        messageAreaList.put("helpCrunchMessageAreaBackground", resources.getIdentifier("helpCrunchMessageAreaBackground", "color", this.hostAppPackageName));
        messageAreaList.put("helpCrunchMessageAreaInputFieldTextColor", resources.getIdentifier("helpCrunchMessageAreaInputFieldTextColor", "color", this.hostAppPackageName));
        messageAreaList.put("helpCrunchMessageMenuBackgroundColor", resources.getIdentifier("helpCrunchMessageMenuBackgroundColor", "color", this.hostAppPackageName));
        messageAreaList.put("helpCrunchMessageMenuSummaryTextColor", resources.getIdentifier("helpCrunchMessageMenuSummaryTextColor", "color", this.hostAppPackageName));
        messageAreaList.put("helpCrunchMessageMenuIconColor", resources.getIdentifier("helpCrunchMessageMenuIconColor", "color", this.hostAppPackageName));
        messageAreaList.put("helpCrunchMessageMenuTextColor", resources.getIdentifier("helpCrunchMessageMenuTextColor", "color", this.hostAppPackageName));

        HCMessageAreaTheme.Builder messageAreaThemeBuilder = new HCMessageAreaTheme.Builder();
        for (Object o : messageAreaList.entrySet()) {
            Map.Entry messageAreaThemeEntry = (Map.Entry) o;
            if (!messageAreaThemeEntry.getValue().equals(0)) {
                switch ((String)messageAreaThemeEntry.getKey()) {
                    case "helpCrunchMessageAreaBackground": messageAreaThemeBuilder.setBackgroundColor((Integer) messageAreaThemeEntry.getValue()); break;
                    case "helpCrunchMessageAreaInputFieldTextColor": messageAreaThemeBuilder.setInputFieldTextColor((Integer) messageAreaThemeEntry.getValue()); break;
                    case "helpCrunchMessageMenuBackgroundColor": messageAreaThemeBuilder.setMessageMenuBackgroundColor((Integer) messageAreaThemeEntry.getValue()); break;
                    case "helpCrunchMessageMenuSummaryTextColor": messageAreaThemeBuilder.setMessageMenuSummaryTextColor((Integer) messageAreaThemeEntry.getValue()); break;
                    case "helpCrunchMessageMenuIconColor": messageAreaThemeBuilder.setMessageMenuIconColor((Integer) messageAreaThemeEntry.getValue()); break;
                    case "helpCrunchMessageMenuTextColor": messageAreaThemeBuilder.setMessageMenuTextColor((Integer) messageAreaThemeEntry.getValue()); break;
                }
            }
        }
        HCMessageAreaTheme messageAreaTheme = messageAreaThemeBuilder.build();
        themeBuilder.setMessageAreaTheme(messageAreaTheme);

        // System Alerts
        HashMap<String, Integer> systemAlertsThemeList = new HashMap<>();
        systemAlertsThemeList.put("helpCrunchSystemAlertsHeaderColor", resources.getIdentifier("helpCrunchSystemAlertsHeaderColor", "color", this.hostAppPackageName));
        systemAlertsThemeList.put("helpCrunchSystemAlertsToastsTextColor", resources.getIdentifier("helpCrunchSystemAlertsToastsTextColor", "color", this.hostAppPackageName));
        systemAlertsThemeList.put("helpCrunchSystemAlertsWelcomeScreenBackgroundColor", resources.getIdentifier("helpCrunchSystemAlertsWelcomeScreenBackgroundColor", "color", this.hostAppPackageName));
        systemAlertsThemeList.put("helpCrunchSystemAlertsWelcomeScreenTextColor", resources.getIdentifier("helpCrunchSystemAlertsWelcomeScreenTextColor", "color", this.hostAppPackageName));
        systemAlertsThemeList.put("helpCrunchSystemAlertsWarningDialogsHeaderColor", resources.getIdentifier("helpCrunchSystemAlertsWarningDialogsHeaderColor", "color", this.hostAppPackageName));
        systemAlertsThemeList.put("helpCrunchSystemAlertsDialogMessageTextColor", resources.getIdentifier("helpCrunchSystemAlertsDialogMessageTextColor", "color", this.hostAppPackageName));

        HCSystemAlertsTheme.Builder systemAlertsThemeBuilder = new HCSystemAlertsTheme.Builder();
        for (Object o : systemAlertsThemeList.entrySet()) {
            Map.Entry systemAlertsThemeEntry = (Map.Entry) o;
            if (!systemAlertsThemeEntry.getValue().equals(0)) {
                switch ((String)systemAlertsThemeEntry.getKey()) {
                    case "helpCrunchSystemAlertsHeaderColor": systemAlertsThemeBuilder.setDialogsHeaderColor((Integer) systemAlertsThemeEntry.getValue()); break;
                    case "helpCrunchSystemAlertsToastsTextColor": systemAlertsThemeBuilder.setToastsTextColor((Integer) systemAlertsThemeEntry.getValue()); break;
                    case "helpCrunchSystemAlertsWelcomeScreenBackgroundColor": systemAlertsThemeBuilder.setWelcomeMessageBackgroundColor((Integer) systemAlertsThemeEntry.getValue()); break;
                    case "helpCrunchSystemAlertsWelcomeScreenTextColor": systemAlertsThemeBuilder.setWelcomeMessageTextColor((Integer) systemAlertsThemeEntry.getValue()); break;
                    case "helpCrunchSystemAlertsWarningDialogsHeaderColor": systemAlertsThemeBuilder.setWarningDialogsHeaderColor((Integer) systemAlertsThemeEntry.getValue()); break;
                    case "helpCrunchSystemAlertsDialogMessageTextColor": systemAlertsThemeBuilder.setDialogMessageTextColor((Integer) systemAlertsThemeEntry.getValue()); break;
                }
            }
        }
        HCSystemAlertsTheme systemAlertsTheme = systemAlertsThemeBuilder.build();
        themeBuilder.setSystemAlertsTheme(systemAlertsTheme);

        // Notifications Theme
        Integer appIcon = resources.getIdentifier("ic_notification", "drawable", this.hostAppPackageName);
        HCNotificationsTheme notificationsDesign = new HCNotificationsTheme.Builder()
                .setSmallIconRes(appIcon)
                .setMessagesCounterEnabled(true)
                .setAvatarTheme(avatarTheme)
                .setColor(ContextCompat.getColor(this.reactContext.getApplicationContext(), helpCrunchMainColor))
                .build();
        themeBuilder.setNotificationsTheme(notificationsDesign);

        this.themeBuilder = themeBuilder;
        promise.resolve(null);
    }

    @ReactMethod
    public void trackEvent(String eventName) {
        HelpCrunch.trackEvent(eventName);
    }

    @ReactMethod
    public void logout(final Promise promise) {
        HelpCrunch.logout(new Callback<Object>() {
            @Override
            public void onSuccess(Object result) {
                promise.resolve(null);
            }

            @Override
            public void onError(@NotNull String message) {
                promise.reject("LogoutUserError", message);
            }
        });
    }

    @ReactMethod
    public void getNumberOfUnreadMessages(final Promise promise) {
        HelpCrunch.getUnreadMessagesCount(new Callback<Integer>() {
            @Override
            public void onError(@NotNull String message) {
                promise.reject("GetUnreadMessagesCountError", message);
            }

            @Override
            public void onSuccess(Integer result) {
                WritableMap map = Arguments.createMap();
                map.putInt("numberOfUnreadMessages", result);
                promise.resolve(map);
            }
        });
    }

    @ReactMethod
    public void registerEvents() {
        if (!this.eventsAlreadyRegistered) {
            HelpCrunchEventManager eventManager = new HelpCrunchEventManager(reactContext);
            this.reactContext.getApplicationContext().registerReceiver(eventManager, new IntentFilter(HelpCrunch.EVENTS));
            this.eventsAlreadyRegistered = true;
        }
    }

}
