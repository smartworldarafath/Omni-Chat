package org.thoughtcrime.securesms.components.settings.app.account

import android.util.Base64
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.thoughtcrime.securesms.dependencies.AppDependencies
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.TextSecurePreferences
import java.security.MessageDigest
import java.security.SecureRandom

class AccountSettingsViewModel : ViewModel() {
  private val store: MutableStateFlow<AccountSettingsState> = MutableStateFlow(getCurrentState())
  private val secureRandom = SecureRandom()

  val state: StateFlow<AccountSettingsState> = store

  fun refreshState() {
    store.update { getCurrentState() }
  }

  fun togglePinKeyboardType() {
    store.update {
      it.copy(pinKeyboardType = it.pinKeyboardType.other)
    }
  }

  fun enableTwoStepVerification(pin: String, email: String) {
    val salt = createSalt()
    val hash = hashPin(pin, salt)
    SignalStore.settings.setTwoStepVerification(email, salt, hash)
    refreshState()
  }

  fun updateTwoStepVerificationPin(pin: String) {
    val salt = createSalt()
    val hash = hashPin(pin, salt)
    SignalStore.settings.setTwoStepVerificationPin(salt, hash)
    refreshState()
  }

  fun updateTwoStepVerificationEmail(email: String) {
    SignalStore.settings.setTwoStepVerificationEmail(email)
    refreshState()
  }

  fun disableTwoStepVerification() {
    SignalStore.settings.clearTwoStepVerification()
    refreshState()
  }

  fun isTwoStepVerificationPin(pin: String): Boolean {
    val salt = SignalStore.settings.twoStepVerificationPinSalt ?: return false
    val storedHash = SignalStore.settings.twoStepVerificationPinHash ?: return false

    return runCatching {
      MessageDigest.isEqual(
        Base64.decode(storedHash, Base64.NO_WRAP),
        hashPinBytes(pin, salt)
      )
    }.getOrDefault(false)
  }

  private fun getCurrentState(): AccountSettingsState {
    return AccountSettingsState(
      hasPin = SignalStore.svr.hasPin() && !SignalStore.svr.hasOptedOut(),
      pinKeyboardType = SignalStore.pin.keyboardType,
      hasRestoredAep = SignalStore.account.restoredAccountEntropyPool,
      pinRemindersEnabled = SignalStore.pin.arePinRemindersEnabled() && SignalStore.svr.hasPin(),
      registrationLockEnabled = SignalStore.svr.isRegistrationLockEnabled,
      twoStepVerificationEnabled = SignalStore.settings.isTwoStepVerificationEnabled,
      twoStepVerificationEmail = SignalStore.settings.twoStepVerificationEmail,
      userUnregistered = TextSecurePreferences.isUnauthorizedReceived(AppDependencies.application),
      clientDeprecated = SignalStore.misc.isClientDeprecated,
      canTransferWhileUnregistered = true
    )
  }

  private fun createSalt(): String {
    val bytes = ByteArray(16)
    secureRandom.nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
  }

  private fun hashPin(pin: String, salt: String): String {
    return Base64.encodeToString(hashPinBytes(pin, salt), Base64.NO_WRAP)
  }

  private fun hashPinBytes(pin: String, salt: String): ByteArray {
    return MessageDigest.getInstance("SHA-256").apply {
      update(Base64.decode(salt, Base64.NO_WRAP))
      update(pin.toByteArray(Charsets.UTF_8))
    }.digest()
  }
}
