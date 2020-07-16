import { NativeModules, NativeEventEmitter } from 'react-native';

const { RNHelpCrunch, HelpCrunchEventManager } = NativeModules;
import { themeConfiguration } from './themeConfiguration';

export default class HelpCrunch {
  constructor() {
    this.URLNotification = 'HCSURLNotification';
    this.ImageURLNotification = 'HCSImageURLNotification';
    this.FileURLNotification = 'HCSFileURLNotification';
    this.UserStartedChatNotification = 'HCSUserStartedChatNotification';
    this.UserClosedChatNotification = 'HCSUserClosedChatNotification';
    this.UnreadChatsNotification = 'HCSUnreadChatsNotification';
    this.eventEmitterObject = {
      'HCSURLNotification': null,
      'HCSImageURLNotification': null,
      'HCSFileURLNotification': null,
      'HCSUserStartedChatNotification': null,
      'HCSUserClosedChatNotification': null,
      'HCSUnreadChatsNotification': null,
    };
  }

  static init(organization, applicationId, applicationSecret, registerForNotifications = false, preChatFields = null, packageName = null) {
    return Platform.OS === 'ios' ? RNHelpCrunch.init(organization, applicationId, applicationSecret, registerForNotifications, preChatFields)
      : RNHelpCrunch.init(organization, applicationId, applicationSecret, preChatFields, packageName);
  }

  static updateUser({ userId, userName, userEmail, customData }) {
    return RNHelpCrunch.updateUser({ userId, userName, userEmail, customData });
  }

  static showChat() {
    return RNHelpCrunch.showChat();
  }

  static setThemeConfiguration(themeConfig = themeConfiguration) {
    Platform.OS === 'ios' ? RNHelpCrunch.setThemeConfiguration(themeConfig) : RNHelpCrunch.setThemeConfiguration();
  }

  static trackEvent(eventName, eventProps = null) {
    if (!eventName) {
      throw new Error('You must pass an eventName.');
    }

    Platform.OS === 'ios' ? RNHelpCrunch.trackEvent(eventName, eventProps) : RNHelpCrunch.trackEvent(eventName);
  }

  static logout() {
    return RNHelpCrunch.logout();
  }

  async addListener(eventType, callback = null) {
    if (!eventType) {
      throw new Error('You must pass an eventType.');
    }

    if (!!this.eventEmitterObject[eventType]) {
      throw new Error('This event is already started. Please, remove it first and then add it again.');
    }

    if (Platform.OS === 'android') {
      await RNHelpCrunch.registerEvents();
    }

    this.eventEmitterObject[eventType] = new NativeEventEmitter(HelpCrunchEventManager);
    this.eventEmitterObject[eventType].addListener(eventType, callback ? (data) => callback(data) : null);
  }

  removeListener(eventType) {
    if (!eventType) {
      throw new Error('You must pass an eventType.');
    }

    if (!!this.eventEmitterObject[eventType]) {
      this.eventEmitterObject[eventType].remove();
      this.eventEmitterObject[eventType] = null;
    }
  }

  static getNumberOfUnreadChats() {
    return RNHelpCrunch.getNumberOfUnreadChats();
  }
}
