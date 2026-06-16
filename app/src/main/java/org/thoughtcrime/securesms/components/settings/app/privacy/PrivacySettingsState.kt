package org.thoughtcrime.securesms.components.settings.app.privacy

import org.thoughtcrime.securesms.keyvalue.SettingsValues

data class PrivacySettingsState(
  val blockedCount: Int,
  val readReceipts: Boolean,
  val typingIndicators: Boolean,
  val lastSeenPrivacy: SettingsValues.PrivacyAudience,
  val onlinePrivacy: SettingsValues.PrivacyAudience,
  val birthdayPrivacy: SettingsValues.PrivacyAudience,
  val bioPrivacy: SettingsValues.PrivacyAudience,
  val storyPrivacy: SettingsValues.PrivacyAudience,
  val screenLock: Boolean,
  val screenLockActivityTimeout: Long,
  val screenSecurity: Boolean,
  val incognitoKeyboard: Boolean,
  val paymentLock: Boolean,
  val isObsoletePasswordEnabled: Boolean,
  val isObsoletePasswordTimeoutEnabled: Boolean,
  val obsoletePasswordTimeout: Int,
  val universalExpireTimer: Int
)
