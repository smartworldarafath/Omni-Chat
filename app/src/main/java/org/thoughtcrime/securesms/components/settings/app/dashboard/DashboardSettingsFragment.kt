package org.thoughtcrime.securesms.components.settings.app.dashboard

import android.os.SystemClock
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.signal.core.ui.compose.ComposeFragment
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Rows
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalIcons
import org.signal.core.ui.compose.theme.SignalTheme
import java.util.Locale

class DashboardSettingsFragment : ComposeFragment() {

  @Composable
  override fun FragmentContent() {
    DashboardSettingsScreen(
      onNavigationClick = {
        requireActivity().onBackPressedDispatcher.onBackPressed()
      }
    )
  }
}

@Composable
private fun DashboardSettingsScreen(
  onNavigationClick: () -> Unit = {}
) {
  var elapsedSeconds by remember { mutableLongStateOf(0L) }
  val samples = remember { mutableStateListOf<Float>() }

  LaunchedEffect(Unit) {
    val start = SystemClock.elapsedRealtime()
    while (true) {
      elapsedSeconds = (SystemClock.elapsedRealtime() - start) / 1000
      val pulse = 0.42f + ((elapsedSeconds % 18) / 18f) * 0.42f
      samples.add(pulse.coerceIn(0.15f, 0.95f))
      while (samples.size > 36) {
        samples.removeAt(0)
      }
      delay(1000)
    }
  }

  Scaffolds.Settings(
    title = "Dashboard",
    onNavigationClick = onNavigationClick,
    navigationIcon = SignalIcons.ArrowStart.imageVector
  ) { contentPadding ->
    LazyColumn(
      modifier = Modifier
        .padding(contentPadding)
    ) {
      item {
        Rows.TextRow(
          text = {
            Column {
              Text(
                text = "Screen-on time",
                style = MaterialTheme.typography.titleMedium
              )
              Text(
                text = formatDuration(elapsedSeconds),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
              )
              Text(
                text = "Live app usage session monitor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
              )
            }
          }
        )
      }

      item {
        Box(
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
            .height(220.dp)
            .background(SignalTheme.colors.neutralSurface, RoundedCornerShape(22.dp))
            .padding(18.dp)
        ) {
          UsageMonitorChart(samples = samples.toList())
        }
      }
    }
  }
}

@Composable
private fun UsageMonitorChart(samples: List<Float>) {
  val lineColor = MaterialTheme.colorScheme.primary
  val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
  val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)

  Canvas(modifier = Modifier.fillMaxWidth().height(184.dp)) {
    val h = size.height
    val w = size.width
    repeat(5) { index ->
      val y = h * index / 4f
      drawLine(
        color = gridColor,
        start = Offset(0f, y),
        end = Offset(w, y),
        strokeWidth = 1.dp.toPx()
      )
    }

    if (samples.isEmpty()) {
      return@Canvas
    }

    val step = if (samples.size == 1) w else w / (samples.size - 1)
    val path = Path()
    val fillPath = Path()

    samples.forEachIndexed { index, value ->
      val x = index * step
      val y = h - (h * value)
      if (index == 0) {
        path.moveTo(x, y)
        fillPath.moveTo(x, h)
        fillPath.lineTo(x, y)
      } else {
        path.lineTo(x, y)
        fillPath.lineTo(x, y)
      }
    }

    fillPath.lineTo(w, h)
    fillPath.close()
    drawPath(path = fillPath, color = fillColor)
    drawPath(
      path = path,
      color = lineColor,
      style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
  }
}

private fun formatDuration(seconds: Long): String {
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val secs = seconds % 60
  return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
}

@DayNightPreviews
@Composable
private fun DashboardSettingsScreenPreview() {
  Previews.Preview {
    DashboardSettingsScreen()
  }
}
