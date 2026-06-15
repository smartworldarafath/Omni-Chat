/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import org.signal.core.ui.CoreUiDependencies

/**
 * A collection of [TransitionSpecs] for setting up nav3 navigation.
 */
object TransitionSpecs {
  private fun duration(defaultDuration: Int, smoothDuration: Int): Int {
    return if (CoreUiDependencies.smoothTransitionsEnabled) smoothDuration else defaultDuration
  }

  /**
   * Screens slide in from the right and slide out from the left.
   */
  object HorizontalSlide {
    private const val DURATION = 200
    private val motionDuration: Int get() = duration(DURATION, 320)

    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      (
        slideInHorizontally(
          initialOffsetX = { it },
          animationSpec = tween(motionDuration)
        ) + fadeIn(animationSpec = tween(motionDuration))
        ) togetherWith
        (
          slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(motionDuration)
          ) + fadeOut(animationSpec = tween(motionDuration))
          )
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      (
        slideInHorizontally(
          initialOffsetX = { -it },
          animationSpec = tween(motionDuration)
        ) + fadeIn(animationSpec = tween(motionDuration))
        ) togetherWith
        (
          slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(motionDuration)
          ) + fadeOut(animationSpec = tween(motionDuration))
          )
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
      (
        slideInHorizontally(
          initialOffsetX = { -it },
          animationSpec = tween(motionDuration)
        ) + fadeIn(animationSpec = tween(motionDuration))
        ) togetherWith
        (
          slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(motionDuration)
          ) + fadeOut(animationSpec = tween(motionDuration))
          )
    }
  }

  /**
   * Screens slide in from the bottom and slide out to the bottom, like a sheet.
   */
  object VerticalSlide {
    private const val DURATION = 300
    private val motionDuration: Int get() = duration(DURATION, 420)

    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(motionDuration)
      ) + fadeIn(animationSpec = tween(motionDuration)) togetherWith
        fadeOut(animationSpec = tween(motionDuration))
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      fadeIn(animationSpec = tween(motionDuration)) togetherWith
        slideOutVertically(
          targetOffsetY = { it },
          animationSpec = tween(motionDuration)
        ) + fadeOut(animationSpec = tween(motionDuration))
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
      fadeIn(animationSpec = tween(motionDuration)) togetherWith
        slideOutVertically(
          targetOffsetY = { it },
          animationSpec = tween(motionDuration)
        ) + fadeOut(animationSpec = tween(motionDuration))
    }
  }
}
