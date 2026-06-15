package org.thoughtcrime.securesms.util

import android.app.Activity
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.keyvalue.SignalStore
import java.util.WeakHashMap

object GlobalTextFontApplier {
  private val listeners = WeakHashMap<View, ViewTreeObserver.OnGlobalLayoutListener>()

  @JvmStatic
  fun install(activity: Activity) {
    val decorView = activity.window?.decorView ?: return
    listeners.remove(decorView)?.let {
      decorView.viewTreeObserver.removeOnGlobalLayoutListener(it)
    }

    if (SignalStore.settings.textFont.serialize() == "default") {
      return
    }

    val listener = ViewTreeObserver.OnGlobalLayoutListener {
      applyTo(decorView)
    }
    listeners[decorView] = listener
    decorView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    applyTo(decorView)
  }

  @JvmStatic
  fun uninstall(activity: Activity) {
    val decorView = activity.window?.decorView ?: return
    listeners.remove(decorView)?.let {
      decorView.viewTreeObserver.removeOnGlobalLayoutListener(it)
    }
  }

  @JvmStatic
  fun applyTo(root: View?) {
    if (root == null) {
      return
    }

    val typeface = selectedTypeface(root.context)
    applyTo(root, typeface)
  }

  private fun applyTo(view: View, selectedTypeface: Typeface?) {
    if (view is TextView) {
      val style = view.typeface?.style ?: Typeface.NORMAL
      view.typeface = selectedTypeface?.let { Typeface.create(it, style) } ?: Typeface.defaultFromStyle(style)
    }

    if (view is ViewGroup) {
      for (i in 0 until view.childCount) {
        applyTo(view.getChildAt(i), selectedTypeface)
      }
    }
  }

  private fun selectedTypeface(context: android.content.Context): Typeface? {
    val fontRes = when (SignalStore.settings.textFont.serialize()) {
      "birthstone" -> R.font.birthstone_regular
      "emilys_candy" -> R.font.emilys_candy_regular
      "shadows_into_light" -> R.font.shadows_into_light_regular
      "walter_turncoat" -> R.font.walter_turncoat_regular
      "life_savers" -> R.font.life_savers_regular
      "jolly_lodger" -> R.font.jolly_lodger_regular
      "kablammo" -> R.font.kablammo_regular
      "rubik_puddles" -> R.font.rubik_puddles_regular
      "moo_lah_lah" -> R.font.moo_lah_lah_regular
      else -> return null
    }

    return ResourcesCompat.getFont(context, fontRes)
  }
}
