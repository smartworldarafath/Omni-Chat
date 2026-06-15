package org.thoughtcrime.securesms.components.settings.app.account

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.InputFilter
import android.text.InputType
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.app.DialogCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.signal.core.ui.compose.ComposeFragment
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Dialogs
import org.signal.core.ui.compose.Dividers
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Rows
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.Texts
import org.signal.core.util.ServiceUtil
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.compose.rememberStatusBarColorNestedScrollModifier
import org.thoughtcrime.securesms.contactshare.SimpleTextWatcher
import org.thoughtcrime.securesms.dependencies.AppDependencies
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.lock.v2.CreateSvrPinActivity
import org.thoughtcrime.securesms.lock.v2.PinKeyboardType
import org.thoughtcrime.securesms.lock.v2.SvrConstants
import org.thoughtcrime.securesms.pin.RegistrationLockV2Dialog
import org.thoughtcrime.securesms.registration.ui.RegistrationActivity
import org.thoughtcrime.securesms.util.PlayStoreUtil
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.whispersystems.signalservice.api.kbs.PinHashUtil

class AccountSettingsFragment : ComposeFragment() {

  private val viewModel: AccountSettingsViewModel by viewModels()

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == CreateSvrPinActivity.REQUEST_NEW_PIN && resultCode == CreateSvrPinActivity.RESULT_OK) {
      Snackbar.make(requireView(), R.string.ConfirmKbsPinFragment__pin_created, Snackbar.LENGTH_LONG).show()
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshState()
  }

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val callbacks = remember { Callbacks() }

    AccountSettingsScreen(
      state = state,
      callbacks = callbacks
    )
  }

  private fun setRegistrationLockEnabled(enabled: Boolean) {
    if (enabled) {
      RegistrationLockV2Dialog.showEnableDialog(requireContext()) { viewModel.refreshState() }
    } else {
      RegistrationLockV2Dialog.showDisableDialog(requireContext()) { viewModel.refreshState() }
    }
  }

  private fun setPinRemindersEnabled(enabled: Boolean) {
    if (!enabled) {
      val context: Context = requireContext()
      val metrics: DisplayMetrics = resources.displayMetrics

      val dialog: AlertDialog = MaterialAlertDialogBuilder(context)
        .setView(R.layout.pin_disable_reminders_dialog)
        .setOnDismissListener { viewModel.refreshState() }
        .create()

      dialog.show()
      dialog.window!!.setLayout((metrics.widthPixels * .80).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

      val pinEditText = DialogCompat.requireViewById(dialog, R.id.reminder_disable_pin) as EditText
      val statusText = DialogCompat.requireViewById(dialog, R.id.reminder_disable_status) as TextView
      val cancelButton = DialogCompat.requireViewById(dialog, R.id.reminder_disable_cancel)
      val turnOffButton = DialogCompat.requireViewById(dialog, R.id.reminder_disable_turn_off)
      val changeKeyboard = DialogCompat.requireViewById(dialog, R.id.reminder_change_keyboard) as MaterialButton

      dialog.lifecycleScope.launch {
        viewModel.state.collect { state ->
          state.pinKeyboardType.applyTo(
            pinEditText = pinEditText,
            toggleTypeButton = changeKeyboard
          )
        }
      }

      changeKeyboard.setOnClickListener { viewModel.togglePinKeyboardType() }

      pinEditText.post {
        ViewUtil.focusAndShowKeyboard(pinEditText)
      }

      pinEditText.addTextChangedListener(object : SimpleTextWatcher() {
        override fun onTextChanged(text: String) {
          turnOffButton.isEnabled = text.length >= SvrConstants.MINIMUM_PIN_LENGTH
        }
      })

      pinEditText.typeface = Typeface.DEFAULT
      turnOffButton.setOnClickListener {
        val pin = pinEditText.text.toString()
        val correct = PinHashUtil.verifyLocalPinHash(SignalStore.svr.localPinHash!!, pin)
        if (correct) {
          SignalStore.pin.setPinRemindersEnabled(false)
          viewModel.refreshState()
          dialog.dismiss()
        } else {
          statusText.setText(R.string.preferences_app_protection__incorrect_pin_try_again)
        }
      }

      cancelButton.setOnClickListener { dialog.dismiss() }
    } else {
      SignalStore.pin.setPinRemindersEnabled(true)
      viewModel.refreshState()
    }
  }

  private fun openTwoStepVerification() {
    val state = viewModel.state.value
    if (state.twoStepVerificationEnabled) {
      showTwoStepVerificationManageDialog(state)
    } else {
      showTwoStepVerificationSetupDialog()
    }
  }

  private fun showTwoStepVerificationSetupDialog() {
    val pinEditText = createTwoStepPinEditText()
    val emailEditText = createTwoStepEmailEditText()

    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.AccountSettingsFragment__two_step_verification_setup_title)
      .setMessage(R.string.AccountSettingsFragment__two_step_verification_setup_body)
      .setView(createTwoStepInputLayout(pinEditText, emailEditText))
      .setPositiveButton(R.string.AccountSettingsFragment__turn_on, null)
      .setNegativeButton(android.R.string.cancel, null)
      .create()

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val pin = pinEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        when {
          !isValidTwoStepPin(pin) -> pinEditText.error = getString(R.string.AccountSettingsFragment__enter_a_6_digit_pin)
          !isValidRecoveryEmail(email) -> emailEditText.error = getString(R.string.AccountSettingsFragment__enter_a_valid_email)
          else -> {
            viewModel.enableTwoStepVerification(pin, email)
            Toast.makeText(requireContext(), R.string.AccountSettingsFragment__two_step_verification_enabled, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
          }
        }
      }
    }

    dialog.setOnDismissListener { viewModel.refreshState() }
    dialog.show()
    pinEditText.post { ViewUtil.focusAndShowKeyboard(pinEditText) }
  }

  private fun showTwoStepVerificationManageDialog(state: AccountSettingsState) {
    val email = state.twoStepVerificationEmail?.takeIf { it.isNotBlank() } ?: getString(R.string.AccountSettingsFragment__two_step_verification_enabled)

    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.AccountSettingsFragment__two_step_verification)
      .setMessage(getString(R.string.AccountSettingsFragment__two_step_verification_manage_body, email))
      .setItems(
        arrayOf(
          getString(R.string.AccountSettingsFragment__change_two_step_pin),
          getString(R.string.AccountSettingsFragment__change_recovery_email),
          getString(R.string.AccountSettingsFragment__disable_two_step_verification)
        )
      ) { _, which ->
        when (which) {
          0 -> showTwoStepPinVerificationDialog { showTwoStepPinChangeDialog() }
          1 -> showTwoStepPinVerificationDialog { showTwoStepEmailChangeDialog() }
          2 -> showTwoStepPinVerificationDialog {
            viewModel.disableTwoStepVerification()
            Toast.makeText(requireContext(), R.string.AccountSettingsFragment__two_step_verification_disabled, Toast.LENGTH_SHORT).show()
          }
        }
      }
      .setNegativeButton(android.R.string.cancel, null)
      .show()
  }

  private fun showTwoStepPinVerificationDialog(onVerified: () -> Unit) {
    val pinEditText = createTwoStepPinEditText()

    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.AccountSettingsFragment__verify_two_step_pin)
      .setView(createTwoStepInputLayout(pinEditText))
      .setPositiveButton(R.string.AccountSettingsFragment__continue, null)
      .setNegativeButton(android.R.string.cancel, null)
      .create()

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val pin = pinEditText.text.toString().trim()
        when {
          !isValidTwoStepPin(pin) -> pinEditText.error = getString(R.string.AccountSettingsFragment__enter_a_6_digit_pin)
          !viewModel.isTwoStepVerificationPin(pin) -> pinEditText.error = getString(R.string.AccountSettingsFragment__incorrect_two_step_pin)
          else -> {
            dialog.dismiss()
            onVerified()
          }
        }
      }
    }

    dialog.show()
    pinEditText.post { ViewUtil.focusAndShowKeyboard(pinEditText) }
  }

  private fun showTwoStepPinChangeDialog() {
    val pinEditText = createTwoStepPinEditText()

    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.AccountSettingsFragment__change_two_step_pin)
      .setView(createTwoStepInputLayout(pinEditText))
      .setPositiveButton(R.string.AccountSettingsFragment__save, null)
      .setNegativeButton(android.R.string.cancel, null)
      .create()

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val pin = pinEditText.text.toString().trim()
        if (isValidTwoStepPin(pin)) {
          viewModel.updateTwoStepVerificationPin(pin)
          Toast.makeText(requireContext(), R.string.AccountSettingsFragment__two_step_verification_updated, Toast.LENGTH_SHORT).show()
          dialog.dismiss()
        } else {
          pinEditText.error = getString(R.string.AccountSettingsFragment__enter_a_6_digit_pin)
        }
      }
    }

    dialog.show()
    pinEditText.post { ViewUtil.focusAndShowKeyboard(pinEditText) }
  }

  private fun showTwoStepEmailChangeDialog() {
    val emailEditText = createTwoStepEmailEditText().apply {
      setText(viewModel.state.value.twoStepVerificationEmail.orEmpty())
      setSelection(text?.length ?: 0)
    }

    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.AccountSettingsFragment__change_recovery_email)
      .setView(createTwoStepInputLayout(emailEditText))
      .setPositiveButton(R.string.AccountSettingsFragment__save, null)
      .setNegativeButton(android.R.string.cancel, null)
      .create()

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val email = emailEditText.text.toString().trim()
        if (isValidRecoveryEmail(email)) {
          viewModel.updateTwoStepVerificationEmail(email)
          Toast.makeText(requireContext(), R.string.AccountSettingsFragment__two_step_verification_updated, Toast.LENGTH_SHORT).show()
          dialog.dismiss()
        } else {
          emailEditText.error = getString(R.string.AccountSettingsFragment__enter_a_valid_email)
        }
      }
    }

    dialog.show()
    emailEditText.post { ViewUtil.focusAndShowKeyboard(emailEditText) }
  }

  private fun createTwoStepPinEditText(): EditText {
    return EditText(requireContext()).apply {
      hint = getString(R.string.AccountSettingsFragment__six_digit_pin)
      inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
      filters = arrayOf(InputFilter.LengthFilter(6))
      setSingleLine(true)
      typeface = Typeface.DEFAULT
    }
  }

  private fun createTwoStepEmailEditText(): EditText {
    return EditText(requireContext()).apply {
      hint = getString(R.string.AccountSettingsFragment__recovery_email)
      inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
      setSingleLine(true)
    }
  }

  private fun createTwoStepInputLayout(vararg editTexts: EditText): LinearLayout {
    val horizontalPadding = (24 * resources.displayMetrics.density).toInt()
    val topMargin = (12 * resources.displayMetrics.density).toInt()

    return LinearLayout(requireContext()).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(horizontalPadding, 0, horizontalPadding, 0)

      editTexts.forEachIndexed { index, editText ->
        val params = LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT
        )

        if (index > 0) {
          params.topMargin = topMargin
        }

        addView(editText, params)
      }
    }
  }

  private fun isValidTwoStepPin(pin: String): Boolean {
    return pin.length == 6 && pin.all { it.isDigit() }
  }

  private fun isValidRecoveryEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }

  private inner class Callbacks : AccountSettingsScreenCallbacks {
    override fun onNavigationClick() {
      activity?.onBackPressedDispatcher?.onBackPressed()
    }

    @Suppress("DEPRECATION")
    override fun onChangePinClick() {
      startActivityForResult(CreateSvrPinActivity.getIntentForPinChangeFromSettings(requireContext()), CreateSvrPinActivity.REQUEST_NEW_PIN)
    }

    @Suppress("DEPRECATION")
    override fun onCreatePinClick() {
      startActivityForResult(CreateSvrPinActivity.getIntentForPinCreate(requireContext()), CreateSvrPinActivity.REQUEST_NEW_PIN)
    }

    override fun setPinRemindersEnabled(enabled: Boolean) {
      this@AccountSettingsFragment.setPinRemindersEnabled(enabled)
    }

    override fun setRegistrationLockEnabled(enabled: Boolean) {
      this@AccountSettingsFragment.setRegistrationLockEnabled(enabled)
    }

    override fun openAdvancedPinSettings() {
      findNavController().safeNavigate(R.id.action_accountSettingsFragment_to_advancedPinSettingsActivity)
    }

    override fun openTwoStepVerification() {
      this@AccountSettingsFragment.openTwoStepVerification()
    }

    override fun openChangeNumberFlow() {
      findNavController().safeNavigate(R.id.action_accountSettingsFragment_to_changePhoneNumberFragment)
    }

    override fun openDeviceTransferFlow() {
      findNavController().safeNavigate(R.id.action_accountSettingsFragment_to_oldDeviceTransferActivity)
    }

    override fun openExportAccountDataFlow() {
      findNavController().safeNavigate(R.id.action_accountSettingsFragment_to_exportAccountFragment)
    }

    override fun openUpdateAppFlow() {
      PlayStoreUtil.openPlayStoreOrOurApkDownloadPage(requireContext())
    }

    override fun openReRegistrationFlow() {
      startActivity(RegistrationActivity.newIntentForReRegistration(requireContext()))
    }

    override fun openDeleteAccountFlow() {
      findNavController().safeNavigate(R.id.action_accountSettingsFragment_to_deleteAccountFragment)
    }

    override fun deleteAllData() {
      if (!ServiceUtil.getActivityManager(AppDependencies.application).clearApplicationUserData()) {
        Toast.makeText(requireContext(), R.string.preferences_account_delete_all_data_failed, Toast.LENGTH_LONG).show()
      }
    }
  }
}

@Stable
@VisibleForTesting
interface AccountSettingsScreenCallbacks {

  fun onNavigationClick() = Unit
  fun onChangePinClick() = Unit
  fun onCreatePinClick() = Unit
  fun setPinRemindersEnabled(enabled: Boolean) = Unit
  fun setRegistrationLockEnabled(enabled: Boolean) = Unit
  fun openAdvancedPinSettings() = Unit
  fun openTwoStepVerification() = Unit
  fun openChangeNumberFlow() = Unit
  fun openDeviceTransferFlow() = Unit
  fun openExportAccountDataFlow() = Unit
  fun openUpdateAppFlow() = Unit
  fun openReRegistrationFlow() = Unit
  fun openDeleteAccountFlow() = Unit
  fun deleteAllData() = Unit

  object Empty : AccountSettingsScreenCallbacks
}

@VisibleForTesting
object AccountSettingsTestTags {
  const val SCROLLER = "scroller"
  const val ROW_MODIFY_PIN = "row-modify-pin"
  const val ROW_PIN_REMINDER = "row-pin-reminder"
  const val ROW_REGISTRATION_LOCK = "row-registration-lock"
  const val ROW_ADVANCED_PIN_SETTINGS = "row-advanced-pin-settings"
  const val ROW_TWO_STEP_VERIFICATION = "row-two-step-verification"
  const val ROW_CHANGE_PHONE_NUMBER = "row-change-phone-number"
  const val ROW_TRANSFER_ACCOUNT = "row-transfer-account"
  const val ROW_REQUEST_ACCOUNT_DATA = "row-request-account-data"
  const val ROW_UPDATE_SIGNAL = "row-update-signal"
  const val ROW_RE_REGISTER = "row-re-register"
  const val ROW_DELETE_ALL_DATA = "row-delete-all-data"
  const val ROW_DELETE_ACCOUNT = "row-delete-account"
  const val DIALOG_CONFIRM_DELETE_ALL_DATA = "dialog-confirm-delete-all-data"
}

@Composable
@VisibleForTesting
fun AccountSettingsScreen(
  state: AccountSettingsState,
  callbacks: AccountSettingsScreenCallbacks
) {
  Scaffolds.Settings(
    title = stringResource(R.string.AccountSettingsFragment__account),
    onNavigationClick = callbacks::onNavigationClick,
    navigationIcon = ImageVector.vectorResource(R.drawable.ic_arrow_left_24)
  ) { contentPadding ->
    LazyColumn(
      modifier = Modifier
        .padding(contentPadding)
        .then(rememberStatusBarColorNestedScrollModifier())
        .testTag(AccountSettingsTestTags.SCROLLER)
    ) {
      item {
        Texts.SectionHeader(
          text = stringResource(R.string.preferences_app_protection__signal_pin)
        )
      }

      item {
        @StringRes val textId = if (state.hasPin || state.hasRestoredAep) {
          R.string.preferences_app_protection__change_your_pin
        } else {
          R.string.preferences_app_protection__create_a_pin
        }

        Rows.TextRow(
          text = stringResource(textId),
          enabled = state.isNotDeprecatedOrUnregistered(),
          onClick = {
            if (state.hasPin) {
              callbacks.onChangePinClick()
            } else {
              callbacks.onCreatePinClick()
            }
          },
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_MODIFY_PIN)
        )
      }

      item {
        Rows.ToggleRow(
          text = stringResource(R.string.preferences_app_protection__pin_reminders),
          label = stringResource(R.string.AccountSettingsFragment__youll_be_asked_less_frequently),
          checked = state.hasPin && state.pinRemindersEnabled,
          enabled = state.hasPin && state.isNotDeprecatedOrUnregistered(),
          onCheckChanged = callbacks::setPinRemindersEnabled,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_PIN_REMINDER)
        )
      }

      item {
        Rows.ToggleRow(
          text = stringResource(R.string.preferences_app_protection__registration_lock),
          label = stringResource(R.string.AccountSettingsFragment__require_your_signal_pin),
          checked = state.registrationLockEnabled,
          enabled = state.hasPin && state.isNotDeprecatedOrUnregistered(),
          onCheckChanged = callbacks::setRegistrationLockEnabled,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_REGISTRATION_LOCK)
        )
      }

      item {
        Rows.TextRow(
          text = stringResource(R.string.preferences__advanced_pin_settings),
          enabled = state.isNotDeprecatedOrUnregistered(),
          onClick = callbacks::openAdvancedPinSettings,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_ADVANCED_PIN_SETTINGS)
        )
      }

      item {
        Dividers.Default()
      }

      item {
        Texts.SectionHeader(
          text = stringResource(R.string.AccountSettingsFragment__account)
        )
      }

      item {
        val twoStepLabel = when {
          state.twoStepVerificationEnabled && !state.twoStepVerificationEmail.isNullOrBlank() -> {
            stringResource(R.string.AccountSettingsFragment__two_step_verification_enabled_with_email, state.twoStepVerificationEmail.orEmpty())
          }
          state.twoStepVerificationEnabled -> {
            stringResource(R.string.AccountSettingsFragment__two_step_verification_enabled)
          }
          else -> {
            stringResource(R.string.AccountSettingsFragment__two_step_verification_summary)
          }
        }

        Rows.TextRow(
          text = stringResource(R.string.AccountSettingsFragment__two_step_verification),
          label = twoStepLabel,
          enabled = state.isNotDeprecatedOrUnregistered(),
          onClick = callbacks::openTwoStepVerification,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_TWO_STEP_VERIFICATION)
        )
      }

      if (!state.userUnregistered) {
        item {
          Rows.TextRow(
            text = stringResource(R.string.AccountSettingsFragment__change_phone_number),
            enabled = state.isNotDeprecatedOrUnregistered(),
            onClick = callbacks::openChangeNumberFlow,
            modifier = Modifier.testTag(AccountSettingsTestTags.ROW_CHANGE_PHONE_NUMBER)
          )
        }
      }

      item {
        Rows.TextRow(
          text = stringResource(R.string.preferences_chats__transfer_account),
          label = stringResource(R.string.preferences_chats__transfer_account_to_a_new_android_device),
          enabled = state.canTransferWhileUnregistered || state.isNotDeprecatedOrUnregistered(),
          onClick = callbacks::openDeviceTransferFlow,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_TRANSFER_ACCOUNT)
        )
      }

      item {
        Rows.TextRow(
          text = stringResource(R.string.AccountSettingsFragment__request_account_data),
          enabled = state.isNotDeprecatedOrUnregistered(),
          onClick = callbacks::openExportAccountDataFlow,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_REQUEST_ACCOUNT_DATA)
        )
      }

      if (!state.isNotDeprecatedOrUnregistered()) {
        if (state.clientDeprecated) {
          item {
            Rows.TextRow(
              text = stringResource(R.string.preferences_account_update_signal),
              onClick = callbacks::openUpdateAppFlow,
              modifier = Modifier.testTag(AccountSettingsTestTags.ROW_UPDATE_SIGNAL)
            )
          }
        } else if (state.userUnregistered) {
          item {
            Rows.TextRow(
              text = stringResource(R.string.preferences_account_reregister),
              onClick = callbacks::openReRegistrationFlow,
              modifier = Modifier.testTag(AccountSettingsTestTags.ROW_RE_REGISTER)
            )
          }
        }

        item {
          var displayDialog by remember { mutableStateOf(false) }

          Rows.TextRow(
            text = {
              Text(
                text = stringResource(R.string.preferences_account_delete_all_data),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.signal_alert_primary)
              )
            },
            onClick = {
              displayDialog = true
            },
            modifier = Modifier.testTag(AccountSettingsTestTags.ROW_DELETE_ALL_DATA)
          )

          if (displayDialog) {
            DeleteAllDataConfirmationDialog(
              onDismissRequest = { displayDialog = false },
              onConfirm = callbacks::deleteAllData
            )
          }
        }
      }

      item {
        @ColorRes val textColor = if (state.isNotDeprecatedOrUnregistered()) {
          R.color.signal_alert_primary
        } else {
          R.color.signal_alert_primary_50
        }

        Rows.TextRow(
          text = {
            Text(
              text = stringResource(R.string.preferences__delete_account),
              color = colorResource(textColor)
            )
          },
          enabled = state.isNotDeprecatedOrUnregistered(),
          onClick = callbacks::openDeleteAccountFlow,
          modifier = Modifier.testTag(AccountSettingsTestTags.ROW_DELETE_ACCOUNT)
        )
      }
    }
  }
}

@Composable
private fun DeleteAllDataConfirmationDialog(
  onConfirm: () -> Unit,
  onDismissRequest: () -> Unit
) {
  Dialogs.SimpleAlertDialog(
    title = stringResource(R.string.preferences_account_delete_all_data_confirmation_title),
    body = stringResource(R.string.preferences_account_delete_all_data_confirmation_message),
    confirm = stringResource(R.string.preferences_account_delete_all_data_confirmation_proceed),
    onConfirm = onConfirm,
    dismiss = stringResource(R.string.preferences_account_delete_all_data_confirmation_cancel),
    onDismissRequest = onDismissRequest,
    modifier = Modifier.testTag(AccountSettingsTestTags.DIALOG_CONFIRM_DELETE_ALL_DATA)
  )
}

@DayNightPreviews
@Composable
private fun AccountSettingsScreenPreview() {
  Previews.Preview {
    AccountSettingsScreen(
      state = AccountSettingsState(
        hasPin = true,
        pinKeyboardType = PinKeyboardType.NUMERIC,
        hasRestoredAep = true,
        pinRemindersEnabled = true,
        registrationLockEnabled = true,
        twoStepVerificationEnabled = true,
        twoStepVerificationEmail = "signal@example.com",
        userUnregistered = false,
        clientDeprecated = false,
        canTransferWhileUnregistered = true
      ),
      callbacks = AccountSettingsScreenCallbacks.Empty
    )
  }
}

@DayNightPreviews
@Composable
private fun DeleteAllDataConfirmationDialogPreview() {
  Previews.Preview {
    DeleteAllDataConfirmationDialog({}, {})
  }
}
