package org.thoughtcrime.securesms.components.settings.app.appearance

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.signal.core.ui.compose.ComposeFragment
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Rows
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalIcons
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.app.appearance.navbar.ChooseNavigationBarStyleFragment
import org.thoughtcrime.securesms.compose.rememberStatusBarColorNestedScrollModifier
import org.thoughtcrime.securesms.keyvalue.SettingsValues
import org.thoughtcrime.securesms.util.navigation.safeNavigate

/**
 * Allows the user to change language, theme, etc. from application settings.
 */
class AppearanceSettingsFragment : ComposeFragment() {

  private val viewModel: AppearanceSettingsViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    childFragmentManager.setFragmentResultListener(ChooseNavigationBarStyleFragment.REQUEST_KEY, viewLifecycleOwner) { key, bundle ->
      if (bundle.getBoolean(key, false)) {
        viewModel.refreshState()
      }
    }
  }

  @Composable
  override fun FragmentContent() {
    val callbacks = remember { Callbacks() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearanceSettingsScreen(
      state = state,
      callbacks = callbacks
    )
  }

  private inner class Callbacks : AppearanceSettingsCallbacks {
    override fun onNavigationClick() {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onLanguageSelected(selection: String) {
      MaterialAlertDialogBuilder(requireContext())
        .setMessage(R.string.preferences_language_change_confirmation_message)
        .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.setLanguage(selection) }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    }

    override fun onThemeSelected(selection: String) {
      viewModel.setTheme(activity, SettingsValues.Theme.deserialize(selection))
    }

    override fun onSwitchDayNightMode(theme: SettingsValues.Theme) {
      viewModel.setTheme(activity, theme)
    }

    override fun onAppUiModeSelected(selection: String) {
      viewModel.setAppUiMode(SettingsValues.AppUiMode.deserialize(selection))
    }

    override fun onResetAppUiModeClick() {
      viewModel.resetAppUiMode()
    }

    override fun onTextFontSelected(selection: String) {
      viewModel.setTextFont(SettingsValues.TextFont.deserialize(selection))
    }

    override fun onChatColorAndWallpaperClick() {
      findNavController().safeNavigate(R.id.action_appearanceSettings_to_wallpaperActivity)
    }

    override fun onAppIconClick() {
      findNavController().safeNavigate(R.id.action_appearanceSettings_to_appIconActivity)
    }

    override fun onMessageFontSizeSelected(selection: String) {
      viewModel.setMessageFontSize(selection.toInt())
    }

    override fun onMessageFontSizeChanged(size: Int) {
      viewModel.setMessageFontSize(size)
    }

    override fun onChatListViewSelected(selection: String) {
      viewModel.setChatListView(SettingsValues.ChatListView.deserialize(selection))
    }

    override fun onSmoothTransitionsChanged(enabled: Boolean) {
      viewModel.setSmoothTransitionsEnabled(enabled)
    }

    override fun onNavigationBarSizeClick() {
      ChooseNavigationBarStyleFragment().show(childFragmentManager, null)
    }
  }
}

interface AppearanceSettingsCallbacks {
  fun onNavigationClick() = Unit
  fun onLanguageSelected(selection: String) = Unit
  fun onThemeSelected(selection: String) = Unit
  fun onSwitchDayNightMode(theme: SettingsValues.Theme) = Unit
  fun onAppUiModeSelected(selection: String) = Unit
  fun onResetAppUiModeClick() = Unit
  fun onTextFontSelected(selection: String) = Unit
  fun onChatColorAndWallpaperClick() = Unit
  fun onAppIconClick() = Unit
  fun onMessageFontSizeSelected(selection: String) = Unit
  fun onMessageFontSizeChanged(size: Int) = Unit
  fun onChatListViewSelected(selection: String) = Unit
  fun onSmoothTransitionsChanged(enabled: Boolean) = Unit
  fun onNavigationBarSizeClick() = Unit

  object Empty : AppearanceSettingsCallbacks
}

@Composable
private fun AppearanceSettingsScreen(
  state: AppearanceSettingsState,
  callbacks: AppearanceSettingsCallbacks
) {
  val themeRevealProgress = remember { Animatable(0f) }
  var targetTheme by remember { mutableStateOf<SettingsValues.Theme?>(null) }
  var revealOrigin by remember { mutableStateOf(Offset.Zero) }

  LaunchedEffect(targetTheme) {
    val pendingTheme = targetTheme ?: return@LaunchedEffect
    themeRevealProgress.snapTo(0f)
    themeRevealProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing)
    )
    callbacks.onSwitchDayNightMode(pendingTheme)
    targetTheme = null
  }

  Box {
    Scaffolds.Settings(
      title = stringResource(R.string.preferences__appearance),
      onNavigationClick = callbacks::onNavigationClick,
      navigationIcon = SignalIcons.ArrowStart.imageVector
    ) { paddingValues ->
      LazyColumn(
        modifier = Modifier
          .padding(paddingValues)
          .then(rememberStatusBarColorNestedScrollModifier())
      ) {
        item {
          Rows.RadioListRow(
            text = stringResource(R.string.preferences__language),
            labels = stringArrayResource(R.array.language_entries),
            values = stringArrayResource(R.array.language_values),
            selectedValue = state.language,
            onSelected = callbacks::onLanguageSelected
          )
        }

        item {
          Rows.RadioListRow(
            text = stringResource(R.string.preferences__theme),
            labels = stringArrayResource(R.array.pref_theme_entries),
            values = stringArrayResource(R.array.pref_theme_values),
            selectedValue = state.theme.serialize(),
            onSelected = callbacks::onThemeSelected
          )
        }

        item {
          Rows.RadioListRow(
            text = stringResource(R.string.preferences__app_ui),
            labels = stringArrayResource(R.array.pref_app_ui_entries),
            values = stringArrayResource(R.array.pref_app_ui_values),
            selectedValue = state.appUiMode.serialize(),
            onSelected = callbacks::onAppUiModeSelected
          )
        }

        item {
          Rows.TextRow(
            text = stringResource(R.string.preferences__app_ui_reset_to_default),
            label = stringResource(R.string.preferences__app_ui_reset_to_default_summary),
            enabled = state.appUiMode != SettingsValues.AppUiMode.DEFAULT,
            onClick = callbacks::onResetAppUiModeClick
          )
        }

        item {
          Rows.RadioListRow(
            text = stringResource(R.string.preferences__text_font),
            labels = stringArrayResource(R.array.pref_text_font_entries),
            values = stringArrayResource(R.array.pref_text_font_values),
            selectedValue = state.textFont.serialize(),
            onSelected = callbacks::onTextFontSelected
          )
        }

        item {
          SmoothTransitionsRow(
            enabled = state.smoothTransitionsEnabled,
            onCheckedChange = callbacks::onSmoothTransitionsChanged
          )
        }

        item {
          SectionLabel(text = stringResource(R.string.preferences__chat_section))
        }

        item {
          Rows.TextRow(
            text = stringResource(R.string.preferences__chat_color_and_wallpaper),
            onClick = callbacks::onChatColorAndWallpaperClick
          )
        }

        item {
          MessageTextSizeRow(
            value = state.messageFontSize.coerceIn(12, 30),
            onValueChange = callbacks::onMessageFontSizeChanged
          )
        }

        item {
          DayNightSwitchRow(
            isDark = state.theme == SettingsValues.Theme.DARK,
            onOriginChanged = { revealOrigin = it },
            onClick = {
              targetTheme = if (state.theme == SettingsValues.Theme.DARK) {
                SettingsValues.Theme.LIGHT
              } else {
                SettingsValues.Theme.DARK
              }
            }
          )
        }

        item {
          Rows.RadioListRow(
            text = stringResource(R.string.preferences__chat_list_view),
            labels = stringArrayResource(R.array.pref_chat_list_view_entries),
            values = stringArrayResource(R.array.pref_chat_list_view_values),
            selectedValue = state.chatListView.serialize(),
            onSelected = callbacks::onChatListViewSelected
          )
        }

        if (Build.VERSION.SDK_INT >= 26) {
          item {
            Rows.TextRow(
              text = stringResource(R.string.preferences__app_icon),
              onClick = callbacks::onAppIconClick
            )
          }
        }

        item {
          val label = if (state.isCompactNavigationBar) {
            R.string.preferences_compact
          } else {
            R.string.preferences_normal
          }

          Rows.TextRow(
            text = stringResource(R.string.preferences_navigation_bar_size),
            label = stringResource(label),
            onClick = callbacks::onNavigationBarSizeClick
          )
        }
      }
    }

    if (targetTheme != null) {
      val color = if (targetTheme == SettingsValues.Theme.DARK) Color(0xFF05070A) else Color(0xFFFFFFFF)
      Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = maxOf(size.width, size.height) * 1.25f
        drawCircle(
          color = color,
          radius = maxRadius * themeRevealProgress.value,
          center = revealOrigin
        )
      }
    }
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 6.dp)
  )
}

@Composable
private fun MessageTextSizeRow(value: Int, onValueChange: (Int) -> Unit) {
  var localValue by remember(value) { mutableStateOf(value.toFloat()) }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 12.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Text(
        text = stringResource(R.string.preferences__message_text_size),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1f)
      )
      Text(
        text = localValue.toInt().toString(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Slider(
      value = localValue,
      onValueChange = {
        localValue = it
        onValueChange(it.toInt().coerceIn(12, 30))
      },
      valueRange = 12f..30f,
      steps = 17,
      modifier = Modifier.fillMaxWidth()
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f), RoundedCornerShape(20.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = stringResource(R.string.preferences__message_text_preview_incoming),
        fontSize = localValue.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = stringResource(R.string.preferences__message_text_preview_outgoing),
        fontSize = localValue.sp,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}

@Composable
private fun DayNightSwitchRow(
  isDark: Boolean,
  onOriginChanged: (Offset) -> Unit,
  onClick: () -> Unit
) {
  Rows.TextRow(
    text = stringResource(if (isDark) R.string.preferences__switch_to_day_mode else R.string.preferences__switch_to_night_mode),
    label = stringResource(R.string.preferences__switch_day_night_summary),
    modifier = Modifier.onGloballyPositioned {
      val position = it.positionInRoot()
      onOriginChanged(Offset(position.x + 96f, position.y + (it.size.height / 2f)))
    },
    onClick = onClick
  )
}

@Composable
private fun SmoothTransitionsRow(enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.preferences__enable_smooth_transitions),
        style = MaterialTheme.typography.bodyLarge
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = stringResource(R.string.preferences__enable_smooth_transitions_summary),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(Modifier.size(16.dp))
    Switch(checked = enabled, onCheckedChange = onCheckedChange)
  }
}

@DayNightPreviews
@Composable
private fun AppearanceSettingsScreenPreview() {
  Previews.Preview {
    AppearanceSettingsScreen(
      state = AppearanceSettingsState(
        theme = SettingsValues.Theme.SYSTEM,
        appUiMode = SettingsValues.AppUiMode.DEFAULT,
        textFont = SettingsValues.TextFont.DEFAULT,
        messageFontSize = 0,
        chatListView = SettingsValues.ChatListView.COMFORTABLE,
        smoothTransitionsEnabled = true,
        language = "en-US",
        isCompactNavigationBar = false
      ),
      callbacks = AppearanceSettingsCallbacks.Empty
    )
  }
}
