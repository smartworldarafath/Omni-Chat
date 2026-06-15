package org.signal.core.ui.compose.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.signal.core.ui.CoreUiDependencies
import org.signal.core.ui.R
import org.signal.core.ui.compose.ProvideIncognitoKeyboard

private val typography = Typography().run {
  copy(
    headlineLarge = headlineLarge.copy(
      fontSize = 32.sp,
      lineHeight = 40.sp,
      letterSpacing = 0.sp
    ),
    headlineMedium = headlineMedium.copy(
      fontSize = 28.sp,
      lineHeight = 36.sp,
      letterSpacing = 0.sp
    ),
    titleLarge = titleLarge.copy(
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = 0.sp
    ),
    titleMedium = titleMedium.copy(
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.0125.sp,
      fontFamily = FontFamily.SansSerif,
      fontStyle = FontStyle.Normal
    ),
    titleSmall = titleSmall.copy(
      fontSize = 16.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.0125.sp
    ),
    bodyLarge = bodyLarge.copy(
      fontSize = 16.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.0125.sp
    ),
    bodyMedium = bodyMedium.copy(
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.0107.sp
    ),
    bodySmall = bodySmall.copy(
      fontSize = 13.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.0192.sp
    ),
    labelLarge = labelLarge.copy(
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.0107.sp
    ),
    labelMedium = labelMedium.copy(
      fontSize = 13.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.0192.sp
    ),
    labelSmall = labelSmall.copy(
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.025.sp
    )
  )
}

private val lightColorScheme = lightColorScheme(
  primary = Color(0xFF2C58C3),
  primaryContainer = Color(0xFFD2DFFB),
  secondary = Color(0xFF586071),
  secondaryContainer = Color(0xFFDCE5F9),
  surface = Color(0xFFFBFCFF),
  surfaceContainerLow = Color(0xFFF2F5F9),
  surfaceVariant = Color(0xFFE7EBF3),
  background = Color(0xFFFBFCFF),
  error = Color(0xFFBA1B1B),
  errorContainer = Color(0xFFFFDAD4),
  onPrimary = Color(0xFFFFFFFF),
  onPrimaryContainer = Color(0xFF051845),
  onSecondary = Color(0xFFFFFFFF),
  onSecondaryContainer = Color(0xFF151D2C),
  onSurface = Color(0xFF1B1B1D),
  onSurfaceVariant = Color(0xFF545863),
  onBackground = Color(0xFF1B1D1D),
  outline = Color(0xFF808389)
)

private val lightExtendedColors = ExtendedColors(
  neutralSurface = Color(0x99FFFFFF),
  colorOnCustom = Color(0xFFFFFFFF),
  colorOnCustomVariant = Color(0xB3FFFFFF),
  colorSurface1 = Color(0xFFF2F5F9),
  colorSurface2 = Color(0xFFEDF0F6),
  colorSurface3 = Color(0xFFE8ECF4),
  colorSurface4 = Color(0xFFE6EAF3),
  colorSurface5 = Color(0xFFE3E7F1),
  colorTransparent1 = Color(0x14FFFFFF),
  colorTransparent2 = Color(0x29FFFFFF),
  colorTransparent3 = Color(0x8FFFFFFF),
  colorTransparent4 = Color(0xB8FFFFFF),
  colorTransparent5 = Color(0xF5FFFFFF),
  colorNeutral = Color(0xFFFFFFFF),
  colorNeutralVariant = Color(0xB8FFFFFF),
  colorTransparentInverse1 = Color(0x0A000000),
  colorTransparentInverse2 = Color(0x14000000),
  colorTransparentInverse3 = Color(0x66000000),
  colorTransparentInverse4 = Color(0xB8000000),
  colorTransparentInverse5 = Color(0xE0000000),
  colorNeutralInverse = Color(0xFF121212),
  colorNeutralVariantInverse = Color(0xFF5C5C5C),
  colorWarning = Color(0x1FB44828),
  colorOnWarning = Color(0xFFB44828)
)

private val darkExtendedColors = ExtendedColors(
  neutralSurface = Color(0x14FFFFFF),
  colorOnCustom = Color(0xFFFFFFFF),
  colorOnCustomVariant = Color(0xB3FFFFFF),
  colorSurface1 = Color(0xFF23242A),
  colorSurface2 = Color(0xFF272A31),
  colorSurface3 = Color(0xFF2C2F37),
  colorSurface4 = Color(0xFF2E3039),
  colorSurface5 = Color(0xFF31343E),
  colorTransparent1 = Color(0x0AFFFFFF),
  colorTransparent2 = Color(0x1FFFFFFF),
  colorTransparent3 = Color(0x29FFFFFF),
  colorTransparent4 = Color(0x7AFFFFFF),
  colorTransparent5 = Color(0xB8FFFFFF),
  colorNeutral = Color(0xFF121212),
  colorNeutralVariant = Color(0xFF5C5C5C),
  colorTransparentInverse1 = Color(0x0A000000),
  colorTransparentInverse2 = Color(0x14000000),
  colorTransparentInverse3 = Color(0x29000000),
  colorTransparentInverse4 = Color(0xB8000000),
  colorTransparentInverse5 = Color(0xF5000000),
  colorNeutralInverse = Color(0xE0FFFFFF),
  colorNeutralVariantInverse = Color(0xA3FFFFFF),
  colorWarning = Color(0x1FEB977D),
  colorOnWarning = Color(0xFFEB977D)
)

private val darkColorScheme = darkColorScheme(
  primary = Color(0xFFB6C5FA),
  primaryContainer = Color(0xFF464B5C),
  secondary = Color(0xFFC1C6DD),
  secondaryContainer = Color(0xFF414659),
  surface = Color(0xFF1B1C1F),
  surfaceContainerLow = Color(0xFF23242A),
  surfaceVariant = Color(0xFF303133),
  background = Color(0xFF1B1C1F),
  error = Color(0xFFFFB4A9),
  errorContainer = Color(0xFF930006),
  onPrimary = Color(0xFF1E2438),
  onPrimaryContainer = Color(0xFFDBE1FC),
  onSecondary = Color(0xFF2A3042),
  onSecondaryContainer = Color(0xFFDCE1F9),
  onSurface = Color(0xFFE2E1E5),
  onSurfaceVariant = Color(0xFFBEBFC5),
  onBackground = Color(0xFFE2E1E5),
  outline = Color(0xFF5C5E65)
)

private val lightGlassColorScheme = lightColorScheme.copy(
  primary = Color(0xFF0B6FD3),
  primaryContainer = Color(0xCCD8EBFF),
  secondary = Color(0xFF4B637A),
  secondaryContainer = Color(0xB8D9ECFF),
  surface = Color(0xEAF8FBFF),
  surfaceContainerLow = Color(0xB8FFFFFF),
  surfaceVariant = Color(0xB8E4F2FF),
  background = Color(0xFFF0F8FF),
  onPrimary = Color(0xFFFFFFFF),
  onPrimaryContainer = Color(0xFF042747),
  onSecondary = Color(0xFFFFFFFF),
  onSecondaryContainer = Color(0xFF0D2537),
  onSurface = Color(0xFF17202A),
  onSurfaceVariant = Color(0xFF3F5365),
  onBackground = Color(0xFF17202A),
  outline = Color(0x660B6FD3)
)

private val darkGlassColorScheme = darkColorScheme.copy(
  primary = Color(0xFF8EC9FF),
  primaryContainer = Color(0x66325782),
  secondary = Color(0xFFB5CCE1),
  secondaryContainer = Color(0x66334B61),
  surface = Color(0xCC141B24),
  surfaceContainerLow = Color(0x991B2733),
  surfaceVariant = Color(0x99314A60),
  background = Color(0xFF101821),
  onPrimary = Color(0xFF06243F),
  onPrimaryContainer = Color(0xFFDCEEFF),
  onSecondary = Color(0xFF132B3D),
  onSecondaryContainer = Color(0xFFE0F0FF),
  onSurface = Color(0xFFEAF4FF),
  onSurfaceVariant = Color(0xFFC4D5E4),
  onBackground = Color(0xFFEAF4FF),
  outline = Color(0x668EC9FF)
)

private val lightGlassExtendedColors = lightExtendedColors.copy(
  neutralSurface = Color(0xB8FFFFFF),
  colorSurface1 = Color(0xCCFFFFFF),
  colorSurface2 = Color(0xB8EAF5FF),
  colorSurface3 = Color(0xA3DCEEFF),
  colorSurface4 = Color(0x99D1E8FF),
  colorSurface5 = Color(0x8FC5E0FA),
  colorTransparent3 = Color(0x99FFFFFF),
  colorTransparent4 = Color(0xCCFFFFFF),
  colorTransparent5 = Color(0xEFFFFFFF),
  colorTransparentInverse2 = Color(0x1F0B6FD3),
  colorTransparentInverse3 = Color(0x520B6FD3)
)

private val darkGlassExtendedColors = darkExtendedColors.copy(
  neutralSurface = Color(0xCC263744),
  colorSurface1 = Color(0xF0203445),
  colorSurface2 = Color(0xE6315268),
  colorSurface3 = Color(0xD9425F78),
  colorSurface4 = Color(0xCC4F6F8B),
  colorSurface5 = Color(0xBF5D7F9F),
  colorTransparent3 = Color(0x40FFFFFF),
  colorTransparent4 = Color(0x66FFFFFF),
  colorTransparent5 = Color(0x99FFFFFF),
  colorTransparentInverse2 = Color(0x24000000),
  colorTransparentInverse3 = Color(0x66000000)
)

private val lightLiquidGlassColorScheme = lightColorScheme.copy(
  primary = Color(0xFF1166D8),
  primaryContainer = Color(0xDDE8F3FF),
  secondary = Color(0xFF50627A),
  secondaryContainer = Color(0xD8ECF5FF),
  surface = Color(0xF2FFFFFF),
  surfaceContainerLow = Color(0xDFFFFFFF),
  surfaceVariant = Color(0xD8EEF6FF),
  background = Color(0xFFF6FAFF),
  onPrimary = Color.White,
  onPrimaryContainer = Color(0xFF071D3C),
  onSecondary = Color.White,
  onSecondaryContainer = Color(0xFF142236),
  onSurface = Color(0xFF151B24),
  onSurfaceVariant = Color(0xFF445265),
  onBackground = Color(0xFF151B24),
  outline = Color(0x6687A7C8)
)

private val darkLiquidGlassColorScheme = darkColorScheme.copy(
  primary = Color(0xFFA6CEFF),
  primaryContainer = Color(0x99557694),
  secondary = Color(0xFFC9D8E7),
  secondaryContainer = Color(0x99506374),
  surface = Color(0xE6121720),
  surfaceContainerLow = Color(0xCC1B222E),
  surfaceVariant = Color(0xCC2D3B4B),
  background = Color(0xFF10141B),
  onPrimary = Color(0xFF061F3B),
  onPrimaryContainer = Color(0xFFEAF4FF),
  onSecondary = Color(0xFF172838),
  onSecondaryContainer = Color(0xFFF0F7FF),
  onSurface = Color(0xFFF1F6FF),
  onSurfaceVariant = Color(0xFFD1DCE8),
  onBackground = Color(0xFFF1F6FF),
  outline = Color(0x66BFD8F6)
)

private val lightLiquidGlassExtendedColors = lightExtendedColors.copy(
  neutralSurface = Color(0xCCFFFFFF),
  colorSurface1 = Color(0xEFFFFFFF),
  colorSurface2 = Color(0xDDECF6FF),
  colorSurface3 = Color(0xD4E4F2FF),
  colorSurface4 = Color(0xC9D6E8FF),
  colorSurface5 = Color(0xBFC9DBF2),
  colorTransparent3 = Color(0xA3FFFFFF),
  colorTransparent4 = Color(0xD9FFFFFF),
  colorTransparent5 = Color(0xF7FFFFFF),
  colorTransparentInverse2 = Color(0x1A6B8CB0),
  colorTransparentInverse3 = Color(0x4D5D7EA4)
)

private val darkLiquidGlassExtendedColors = darkExtendedColors.copy(
  neutralSurface = Color(0xB8253342),
  colorSurface1 = Color(0xE61A2430),
  colorSurface2 = Color(0xD9293A4C),
  colorSurface3 = Color(0xCC354A60),
  colorSurface4 = Color(0xBF415A73),
  colorSurface5 = Color(0xB34F6A85),
  colorTransparent3 = Color(0x40FFFFFF),
  colorTransparent4 = Color(0x6EFFFFFF),
  colorTransparent5 = Color(0xA3FFFFFF),
  colorTransparentInverse2 = Color(0x33000000),
  colorTransparentInverse3 = Color(0x73000000)
)

private val birthstoneFont = FontFamily(Font(R.font.birthstone_regular))
private val emilysCandyFont = FontFamily(Font(R.font.emilys_candy_regular))
private val shadowsIntoLightFont = FontFamily(Font(R.font.shadows_into_light_regular))
private val walterTurncoatFont = FontFamily(Font(R.font.walter_turncoat_regular))
private val lifeSaversFont = FontFamily(
  Font(R.font.life_savers_regular, FontWeight.Normal),
  Font(R.font.life_savers_bold, FontWeight.Bold),
  Font(R.font.life_savers_extrabold, FontWeight.ExtraBold)
)
private val jollyLodgerFont = FontFamily(Font(R.font.jolly_lodger_regular))
private val kablammoFont = FontFamily(Font(R.font.kablammo_regular))
private val rubikPuddlesFont = FontFamily(Font(R.font.rubik_puddles_regular))
private val mooLahLahFont = FontFamily(Font(R.font.moo_lah_lah_regular))

private val lightSnackbarColors = SnackbarColors(
  color = darkColorScheme.surface,
  contentColor = darkColorScheme.onSurface,
  actionColor = darkColorScheme.primary,
  actionContentColor = darkColorScheme.primary,
  dismissActionContentColor = darkColorScheme.onSurface
)

private val darkSnackbarColors = SnackbarColors(
  color = darkColorScheme.surfaceVariant,
  contentColor = darkColorScheme.onSurfaceVariant,
  actionColor = darkColorScheme.primary,
  actionContentColor = darkColorScheme.primary,
  dismissActionContentColor = darkColorScheme.onSurfaceVariant
)

val LocalAppUiMode = staticCompositionLocalOf {
  CoreUiDependencies.APP_UI_MODE_DEFAULT
}

private fun Typography.withFont(fontFamily: FontFamily, scale: Float = 1f): Typography {
  fun TextStyle.adjust(): TextStyle {
    return copy(
      fontFamily = fontFamily,
      fontSize = (fontSize.value * scale).sp,
      letterSpacing = 0.sp
    )
  }

  return copy(
    headlineLarge = headlineLarge.adjust(),
    headlineMedium = headlineMedium.adjust(),
    headlineSmall = headlineSmall.adjust(),
    titleLarge = titleLarge.adjust(),
    titleMedium = titleMedium.adjust(),
    titleSmall = titleSmall.adjust(),
    bodyLarge = bodyLarge.adjust(),
    bodyMedium = bodyMedium.adjust(),
    bodySmall = bodySmall.adjust(),
    labelLarge = labelLarge.adjust(),
    labelMedium = labelMedium.adjust(),
    labelSmall = labelSmall.adjust()
  )
}

private fun typographyForSelectedFont(): Typography {
  return when (CoreUiDependencies.textFont) {
    "birthstone" -> typography.withFont(birthstoneFont, scale = 1.16f)
    "emilys_candy" -> typography.withFont(emilysCandyFont, scale = 0.98f)
    "shadows_into_light" -> typography.withFont(shadowsIntoLightFont, scale = 1.05f)
    "walter_turncoat" -> typography.withFont(walterTurncoatFont, scale = 0.96f)
    "life_savers" -> typography.withFont(lifeSaversFont, scale = 0.98f)
    "jolly_lodger" -> typography.withFont(jollyLodgerFont, scale = 1.03f)
    "kablammo" -> typography.withFont(kablammoFont, scale = 0.84f)
    "rubik_puddles" -> typography.withFont(rubikPuddlesFont, scale = 0.82f)
    "moo_lah_lah" -> typography.withFont(mooLahLahFont, scale = 0.82f)
    else -> typography
  }
}

@Composable
fun SignalTheme(
  isDarkMode: Boolean = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES,
  incognitoKeyboardEnabled: Boolean = CoreUiDependencies.isIncognitoKeyboardEnabled,
  content: @Composable () -> Unit
) {
  val selectedAppUiMode = CoreUiDependencies.appUiMode
  val isGlassmorphism = selectedAppUiMode == CoreUiDependencies.APP_UI_MODE_GLASSMORPHISM
  val isLiquidGlass = selectedAppUiMode == CoreUiDependencies.APP_UI_MODE_LIQUID_GLASS
  val extendedColors = when {
    isLiquidGlass && isDarkMode -> darkLiquidGlassExtendedColors
    isLiquidGlass -> lightLiquidGlassExtendedColors
    isGlassmorphism && isDarkMode -> darkGlassExtendedColors
    isGlassmorphism -> lightGlassExtendedColors
    isDarkMode -> darkExtendedColors
    else -> lightExtendedColors
  }
  val colorScheme = when {
    isLiquidGlass && isDarkMode -> darkLiquidGlassColorScheme
    isLiquidGlass -> lightLiquidGlassColorScheme
    isGlassmorphism && isDarkMode -> darkGlassColorScheme
    isGlassmorphism -> lightGlassColorScheme
    isDarkMode -> darkColorScheme
    else -> lightColorScheme
  }
  val snackbarColors = if (isDarkMode) darkSnackbarColors else lightSnackbarColors
  val appUiMode = when {
    isLiquidGlass -> CoreUiDependencies.APP_UI_MODE_LIQUID_GLASS
    isGlassmorphism -> CoreUiDependencies.APP_UI_MODE_GLASSMORPHISM
    else -> CoreUiDependencies.APP_UI_MODE_DEFAULT
  }

  ProvideIncognitoKeyboard(enabled = incognitoKeyboardEnabled) {
    CompositionLocalProvider(
      LocalExtendedColors provides extendedColors,
      LocalSnackbarColors provides snackbarColors,
      LocalAppUiMode provides appUiMode
    ) {
      MaterialTheme(
        colorScheme = colorScheme,
        typography = typographyForSelectedFont(),
        content = content
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TypographyPreview() {
  SignalTheme(
    isDarkMode = false,
    incognitoKeyboardEnabled = false
  ) {
    Column {
      Text(
        text = "Headline Small",
        style = MaterialTheme.typography.headlineLarge
      )
      Text(
        text = "Headline Small",
        style = MaterialTheme.typography.headlineMedium
      )
      Text(
        text = "Headline Small",
        style = MaterialTheme.typography.headlineSmall
      )
      Text(
        text = "Title Large",
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        text = "Title Medium",
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = "Title Small",
        style = MaterialTheme.typography.titleSmall
      )
      Text(
        text = "Body Large",
        style = MaterialTheme.typography.bodyLarge
      )
      Text(
        text = "Body Medium",
        style = MaterialTheme.typography.bodyMedium
      )
      Text(
        text = "Body Small",
        style = MaterialTheme.typography.bodySmall
      )
      Text(
        text = "Label Large",
        style = MaterialTheme.typography.labelLarge
      )
      Text(
        text = "Label Medium",
        style = MaterialTheme.typography.labelMedium
      )
      Text(
        text = "Label Small",
        style = MaterialTheme.typography.labelSmall
      )
    }
  }
}

object SignalTheme {
  val colors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
}
