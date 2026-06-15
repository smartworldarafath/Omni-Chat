/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.core.ui

import android.app.Application

object CoreUiDependencies {
  const val APP_UI_MODE_DEFAULT = "default"
  const val APP_UI_MODE_GLASSMORPHISM = "glassmorphism"
  const val APP_UI_MODE_LIQUID_GLASS = "liquid_glass"
  const val TEXT_FONT_DEFAULT = "default"

  private lateinit var _application: Application
  private lateinit var _provider: Provider

  fun init(application: Application, provider: Provider) {
    if (this::_provider.isInitialized) {
      return
    }

    _application = application
    _provider = provider
  }

  val application: Application
    get() = _application

  val packageId: String
    get() = _provider.providePackageId()

  val isIncognitoKeyboardEnabled: Boolean
    get() = _provider.provideIsIncognitoKeyboardEnabled()

  val isScreenSecurityEnabled: Boolean
    get() = _provider.provideIsScreenSecurityEnabled()

  val forceSplitPane: Boolean
    get() = _provider.provideForceSplitPane()

  val appUiMode: String
    get() = _provider.provideAppUiMode()

  val textFont: String
    get() = _provider.provideTextFont()

  val smoothTransitionsEnabled: Boolean
    get() = _provider.provideSmoothTransitionsEnabled()

  interface Provider {
    fun providePackageId(): String
    fun provideIsIncognitoKeyboardEnabled(): Boolean
    fun provideIsScreenSecurityEnabled(): Boolean
    fun provideForceSplitPane(): Boolean
    fun provideAppUiMode(): String = CoreUiDependencies.APP_UI_MODE_DEFAULT
    fun provideTextFont(): String = CoreUiDependencies.TEXT_FONT_DEFAULT
    fun provideSmoothTransitionsEnabled(): Boolean = true
  }
}
