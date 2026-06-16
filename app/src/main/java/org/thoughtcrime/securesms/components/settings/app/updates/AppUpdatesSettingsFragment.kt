/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.components.settings.app.updates

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.signal.core.ui.compose.ComposeFragment
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Rows
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalIcons
import org.signal.core.ui.compose.Texts
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.compose.rememberStatusBarColorNestedScrollModifier
import org.thoughtcrime.securesms.dependencies.AppDependencies
import org.thoughtcrime.securesms.jobs.ApkUpdateJob
import org.thoughtcrime.securesms.keyvalue.SignalStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Settings around app updates. Only shown for builds that manage their own app updates.
 */
class AppUpdatesSettingsFragment : ComposeFragment() {

  private val viewModel: AppUpdatesSettingsViewModel by viewModels()

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppUpdatesSettingsScreen(
      state = state,
      callbacks = remember { Callbacks() }
    )
  }

  override fun onResume() {
    super.onResume()
    viewModel.refresh()
  }

  private inner class Callbacks : AppUpdatesSettingsCallbacks {
    override fun onNavigationClick() {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onAutoUpdateChanged(enabled: Boolean) {
      SignalStore.apkUpdate.autoUpdate = enabled
      viewModel.refresh()
    }

    override fun onCheckForUpdatesClick() {
      AppDependencies.jobManager.add(ApkUpdateJob())
    }
  }
}

private interface AppUpdatesSettingsCallbacks {
  fun onNavigationClick() = Unit
  fun onAutoUpdateChanged(enabled: Boolean) = Unit
  fun onCheckForUpdatesClick() = Unit

  object Empty : AppUpdatesSettingsCallbacks
}

@Composable
private fun AppUpdatesSettingsScreen(
  state: AppUpdatesSettingsState,
  callbacks: AppUpdatesSettingsCallbacks
) {
  Scaffolds.Settings(
    title = stringResource(R.string.preferences_app_updates__title),
    onNavigationClick = callbacks::onNavigationClick,
    navigationIcon = SignalIcons.ArrowStart.imageVector
  ) { paddingValues ->

    LazyColumn(
      modifier = Modifier
        .padding(paddingValues)
        .then(rememberStatusBarColorNestedScrollModifier())
    ) {
      item {
        Box(
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .background(SignalTheme.colors.neutralSurface, RoundedCornerShape(24.dp))
            .padding(20.dp)
        ) {
          Column {
            Text(
              text = "Omni Chat updater",
              style = MaterialTheme.typography.titleLarge
            )
            Text(
              text = "Auto-update is not active yet. This feature is planned and will be enabled in a future release.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(top = 8.dp)
            )
            UpdatePulseChart(modifier = Modifier.padding(top = 20.dp))
          }
        }
      }

      item {
        Texts.SectionHeader(text = "Update status")
      }

      item {
        Rows.TextRow(
          text = "Check for updates",
          label = "Last checked on: ${rememberLastSuccessfulUpdateString(state.lastCheckedTime)}",
          onClick = callbacks::onCheckForUpdatesClick
        )
      }
    }
  }
}

@Composable
private fun UpdatePulseChart(modifier: Modifier = Modifier) {
  val primary = MaterialTheme.colorScheme.primary
  val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(74.dp)
  ) {
    val centerY = size.height / 2f
    drawLine(
      color = outline,
      start = Offset(0f, centerY),
      end = Offset(size.width, centerY),
      strokeWidth = 1.dp.toPx()
    )

    val step = size.width / 5f
    for (i in 0..5) {
      val x = i * step
      drawLine(
        color = primary.copy(alpha = 0.65f),
        start = Offset(x, centerY + if (i % 2 == 0) 20.dp.toPx() else 8.dp.toPx()),
        end = Offset(x + step * 0.72f, centerY - if (i % 2 == 0) 18.dp.toPx() else 28.dp.toPx()),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
      )
      drawCircle(
        color = primary,
        radius = 4.dp.toPx(),
        center = Offset(x, centerY)
      )
    }

    drawArc(
      color = primary.copy(alpha = 0.18f),
      startAngle = 180f,
      sweepAngle = 180f,
      useCenter = false,
      style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
    )
  }
}

@Composable
private fun rememberLastSuccessfulUpdateString(lastUpdateTime: Duration): String {
  return remember(lastUpdateTime) {
    if (lastUpdateTime > Duration.ZERO) {
      val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mma", Locale.US)
      dateFormat.format(Date(lastUpdateTime.inWholeMilliseconds))
    } else {
      "Never"
    }
  }
}

@DayNightPreviews
@Composable
private fun AppUpdatesSettingsScreenPreview() {
  Previews.Preview {
    AppUpdatesSettingsScreen(
      state = AppUpdatesSettingsState(
        lastCheckedTime = System.currentTimeMillis().milliseconds,
        autoUpdateEnabled = true
      ),
      callbacks = AppUpdatesSettingsCallbacks.Empty
    )
  }
}
