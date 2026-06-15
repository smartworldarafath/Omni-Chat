package org.thoughtcrime.securesms.components.settings.app.appearance

import org.thoughtcrime.securesms.keyvalue.SettingsValues

data class AppearanceSettingsState(
  val theme: SettingsValues.Theme,
  val appUiMode: SettingsValues.AppUiMode,
  val textFont: SettingsValues.TextFont,
  val messageFontSize: Int,
  val chatListView: SettingsValues.ChatListView,
  val smoothTransitionsEnabled: Boolean,
  val language: String,
  val isCompactNavigationBar: Boolean
)
