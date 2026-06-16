package org.thoughtcrime.securesms.util;

import android.app.Activity;
import android.os.Build;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import org.signal.core.util.logging.Log;

public final class DisplayPerformanceController {

  private static final String TAG = Log.tag(DisplayPerformanceController.class);

  private DisplayPerformanceController() {
  }

  @SuppressWarnings("deprecation")
  public static void apply(@NonNull Activity activity) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return;
    }

    Window window = activity.getWindow();
    if (window == null) {
      return;
    }

    Display display = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? activity.getDisplay() : window.getWindowManager().getDefaultDisplay();
    if (display == null) {
      return;
    }

    Display.Mode bestMode = getBestRefreshMode(display);
    if (bestMode == null) {
      return;
    }

    WindowManager.LayoutParams params = window.getAttributes();
    boolean changed = false;

    if (params.preferredDisplayModeId != bestMode.getModeId()) {
      params.preferredDisplayModeId = bestMode.getModeId();
      changed = true;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && params.preferredRefreshRate != bestMode.getRefreshRate()) {
      params.preferredRefreshRate = bestMode.getRefreshRate();
      changed = true;
    }

    if (changed) {
      window.setAttributes(params);
      Log.d(TAG, "Applied preferred display refresh rate: " + bestMode.getRefreshRate() + "hz");
    }
  }

  private static Display.Mode getBestRefreshMode(@NonNull Display display) {
    Display.Mode current = display.getMode();
    Display.Mode best = current;

    for (Display.Mode mode : display.getSupportedModes()) {
      boolean sameResolution = mode.getPhysicalWidth() == current.getPhysicalWidth() &&
                               mode.getPhysicalHeight() == current.getPhysicalHeight();

      if (sameResolution && mode.getRefreshRate() > best.getRefreshRate()) {
        best = mode;
      }
    }

    return best;
  }
}
