package org.thoughtcrime.securesms.components.settings.app.appearance

import android.app.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.signal.core.util.AppUtil
import org.thoughtcrime.securesms.dependencies.AppDependencies
import org.thoughtcrime.securesms.jobs.EmojiSearchIndexDownloadJob
import org.thoughtcrime.securesms.keyvalue.SettingsValues.AppUiMode
import org.thoughtcrime.securesms.keyvalue.SettingsValues.ChatListView
import org.thoughtcrime.securesms.keyvalue.SettingsValues.TextFont
import org.thoughtcrime.securesms.keyvalue.SettingsValues.Theme
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.SplashScreenUtil

class AppearanceSettingsViewModel : ViewModel() {
  private val store = MutableStateFlow(getState())
  val state: StateFlow<AppearanceSettingsState> = store

  fun refreshState() {
    store.update { getState() }
  }

  fun setTheme(activity: Activity?, theme: Theme) {
    store.update { it.copy(theme = theme) }
    SignalStore.settings.theme = theme
    SplashScreenUtil.setSplashScreenThemeIfNecessary(activity, theme)
  }

  fun setAppUiMode(appUiMode: AppUiMode) {
    store.update { it.copy(appUiMode = appUiMode) }
    SignalStore.settings.appUiMode = appUiMode
  }

  fun resetAppUiMode() {
    setAppUiMode(AppUiMode.DEFAULT)
  }

  fun setTextFont(textFont: TextFont) {
    store.update { it.copy(textFont = textFont) }
    SignalStore.settings.textFont = textFont
  }

  fun setLanguage(language: String) {
    store.update { it.copy(language = language) }
    SignalStore.settings.language = language
    EmojiSearchIndexDownloadJob.scheduleImmediately()
    AppUtil.restart(AppDependencies.application)
  }

  fun setMessageFontSize(size: Int) {
    store.update { it.copy(messageFontSize = size) }
    SignalStore.settings.messageFontSize = size
  }

  fun setChatListView(chatListView: ChatListView) {
    store.update { it.copy(chatListView = chatListView) }
    SignalStore.settings.chatListView = chatListView
  }

  fun setSmoothTransitionsEnabled(enabled: Boolean) {
    store.update { it.copy(smoothTransitionsEnabled = enabled) }
    SignalStore.settings.isSmoothTransitionsEnabled = enabled
  }

  private fun getState(): AppearanceSettingsState {
    return AppearanceSettingsState(
      SignalStore.settings.theme,
      SignalStore.settings.appUiMode,
      SignalStore.settings.textFont,
      SignalStore.settings.messageFontSize,
      SignalStore.settings.chatListView,
      SignalStore.settings.isSmoothTransitionsEnabled,
      SignalStore.settings.language,
      SignalStore.settings.useCompactNavigationBar
    )
  }
}
