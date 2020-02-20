import { Platform } from 'react-native';

const themeConfiguration = {
  mainColor: null,
  preChatThemeConfig: Platform.select({
    ios: {
      prechatFormTopMessageBackgroundColor: null,
      prechatFormTopMessageFontColor: null,
      prechatFormContinueTextColor: null,

      prechatFormContinueButtonText: null,
      prechatFormTextFieldBackgroundColor: null,
      prechatFormTextFieldBorderColor: null,
      prechatFormTextFieldSelectedBorderColor: null,
      prechatFormTextFieldPlaceholderColor: null,
      prechatFormTextFieldFontColor: null,
    },
    android: {
      buttonContinueBackgroundSelector: null,
      inputFieldTextColor: null,
      inputFieldTextHintColor: null,
      backgroundColor: null,
      messageBackgroundColor: null,
      messageTextColor: null,
    }
  }),
  chatThemeConfig: Platform.select({
    ios: {
      chatWaitingMessageBackgroundColor: null,
      chatWaitingMessageFontColor: null,
      chatWaitingMessageActivityIndicatorColor: null,
      chatWaitingMessageIsVisible: null,
      messageTimeColor: null,
      chatDateColor: null,
      chatBackgroundColor: null
    },
    android: {
      incomingBubbleTextColor: null,
      outcomingBubbleTextColor: null,
      incomingBubbleColor: null,
      outcomingBubbleColor: null,
      incomingBubbleLinkColor: null,
      outcomingBubbleLinkColor: null,
      incomingCodeBackgroundColor: null,
      outcomingCodeBackgroundColor: null,
      incomingCodeTextColor: null,
      outcomingCodeTextColor: null,
      incomingBlockQuoteColor: null,
      outcomingBlockQuoteColor: null,
      incomingFileTextColor: null,
      outcomingFileTextColor: null,
      incomingFileIconBackgroundColor: null,
      outcomingFileIconBackgroundColor: null,
      authorNameColor: null,
      systemMessageColor: null,
      timeTextColor: null,
      progressViewsColor: null,
      backgroundColor: null,
    },
  }),
  agentMessagesThemeConfig: Platform.select({
    ios: {
      messageIncomingBubbleColor: null,
      messageIncomingFontColor: null,
      messageIncomingAgentNameFontColor: null,
      messageIncomingLinksColor: null,
      messageIncomingFileTypeBackgroundColor: null,
      messageIncomingFileTypeFontColor: null,
      messageIncomingQuoteLineColor: null,
      messageIncomingCodeBorderColor: null,
      messageIncomingCodeFontColor: null,
      messageIncomingCodeBackgroundColor: null
    }
  }),
  clientMessagesThemeConfig: Platform.select({
    ios: {
      messageOutgoingBubbleColor: null,
      messageOutgoingFontColor: null,
      messageOutgoingLinksColor: null,
      messageOutgoingFileTypeBackgroundColor: null,
      messageOutgoingFileTypeFontColor: null,
      messageOutgoingQuoteLineColor: null,
      messageOutgoingCodeBorderColor: null,
      messageOutgoingCodeFontColor: null,
      messageOutgoingCodeBackgroundColor: null
    },
  }),
  navigationBarThemeConfig: Platform.select({
    ios: {
      navigationBarTextColor: null,
      navigationBarBackgroundColor: null,
      navigationBarAgentsMoreBackgroundColor: null,
      navigationBarBottomLineColor: null,
      isNavigationBarBottomLineVisible: true,
    },
    android: {
      backgroundColor: null,
      statusBarColor: null,
      agentsTextColor: null,
    }
  }),
  sendMessageThemeConfig: Platform.select({
    ios: {
      sendMessageBackgroundColor: null,
      sendMessageAttachmentIconImage: null,
      sendMessageSendButtonIconImage: null,
      sendMessageTextFieldBackgroundColor: null,
      sendMessageTextFieldBorderColor: null,
      sendMessageTextFieldFontColor: null,
      sendMessageTextFieldPlaceholderColor: null,
      sendMessageSendButtonText: null,
      sendMessageSendButtonColor: null
    },
    android: {
      buttonTextColor: null,
      backgroundColor: null,
      inputFieldTextColor: null,
      inputFieldTextHintColor: null,
      messageMenuBackgroundColor: null,
      messageMenuSummaryTextColor: null,
      messageMenuIconColor: null,
      messageMenuTextColor: null,
    }
  }),
};

export { themeConfiguration };
