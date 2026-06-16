/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.components.settings.app.storage

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.backup.v2.MessageBackupTier
import org.thoughtcrime.securesms.backup.v2.ui.subscription.BackupUpgradeAvailabilityChecker
import org.thoughtcrime.securesms.components.settings.app.subscription.InAppPaymentsRepository
import org.thoughtcrime.securesms.database.InAppPaymentTable
import org.thoughtcrime.securesms.database.MediaTable
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.database.SignalDatabase.Companion.media
import org.thoughtcrime.securesms.database.ThreadTable
import org.thoughtcrime.securesms.dependencies.AppDependencies
import org.thoughtcrime.securesms.jobs.OptimizeMediaJob
import org.thoughtcrime.securesms.jobs.RestoreOptimizedMediaJob
import org.thoughtcrime.securesms.keyvalue.KeepMessagesDuration
import org.thoughtcrime.securesms.keyvalue.SignalStore
import java.io.File

class ManageStorageSettingsViewModel : ViewModel() {

  private val store = MutableStateFlow(
    ManageStorageState(
      keepMessagesDuration = SignalStore.settings.keepMessagesDuration,
      lengthLimit = if (SignalStore.settings.isTrimByLengthEnabled) SignalStore.settings.threadTrimLength else ManageStorageState.NO_LIMIT,
      syncTrimDeletes = SignalStore.settings.shouldSyncThreadTrimDeletes()
    )
  )
  val state = store.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      InAppPaymentsRepository.observeLatestBackupPayment()
        .collectLatest { payment ->
          store.update { it.copy(isPaidTierPending = payment.state == InAppPaymentTable.State.PENDING) }
        }
    }

    viewModelScope.launch {
      store.update {
        it.copy(onDeviceStorageOptimizationState = getOnDeviceStorageOptimizationState())
      }
    }
  }

  fun refresh() {
    viewModelScope.launch(Dispatchers.IO) {
      val breakdown: MediaTable.StorageBreakdown = media.getStorageBreakdown()
      store.update { it.copy(breakdown = breakdown, cacheBytes = cacheSize(AppDependencies.application.cacheDir)) }
    }
  }

  fun clearSelectedCache(documents: Boolean, videos: Boolean, photos: Boolean, others: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      clearCacheFiles(AppDependencies.application.cacheDir, documents, videos, photos, others)
      store.update { it.copy(cacheBytes = cacheSize(AppDependencies.application.cacheDir)) }
    }
  }

  fun deleteChatHistory() {
    SignalExecutors.BOUNDED_IO.execute {
      SignalDatabase.threads.deleteAllConversations()
      AppDependencies.messageNotifier.updateNotification(AppDependencies.application)
    }
  }

  private fun cacheSize(file: File?): Long {
    if (file == null || !file.exists()) {
      return 0L
    }

    if (file.isFile) {
      return file.length()
    }

    return file.listFiles()?.sumOf { cacheSize(it) } ?: 0L
  }

  private fun clearCacheFiles(file: File?, documents: Boolean, videos: Boolean, photos: Boolean, others: Boolean): Boolean {
    if (file == null || !file.exists()) {
      return true
    }

    if (file.isDirectory) {
      file.listFiles()?.forEach { clearCacheFiles(it, documents, videos, photos, others) }
      return true
    }

    val category = cacheCategory(file)
    val shouldDelete = when (category) {
      CacheCategory.DOCUMENT -> documents
      CacheCategory.VIDEO -> videos
      CacheCategory.PHOTO -> photos
      CacheCategory.OTHER -> others
    }

    return !shouldDelete || file.delete()
  }

  private fun cacheCategory(file: File): CacheCategory {
    return when (file.extension.lowercase()) {
      "jpg", "jpeg", "png", "webp", "gif", "heic", "avif" -> CacheCategory.PHOTO
      "mp4", "mkv", "webm", "mov", "3gp" -> CacheCategory.VIDEO
      "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar", "7z" -> CacheCategory.DOCUMENT
      else -> CacheCategory.OTHER
    }
  }

  private enum class CacheCategory {
    DOCUMENT,
    VIDEO,
    PHOTO,
    OTHER
  }

  fun setKeepMessagesDuration(newDuration: KeepMessagesDuration) {
    SignalStore.settings.setKeepMessagesForDuration(newDuration)
    AppDependencies.trimThreadsByDateManager.scheduleIfNecessary()

    store.update { it.copy(keepMessagesDuration = newDuration) }
  }

  fun showConfirmKeepDurationChange(newDuration: KeepMessagesDuration): Boolean {
    return newDuration.ordinal > state.value.keepMessagesDuration.ordinal
  }

  fun setChatLengthLimit(newLimit: Int) {
    val restrictingChange = isRestrictingLengthLimitChange(newLimit)

    SignalStore.settings.setThreadTrimByLengthEnabled(newLimit != ManageStorageState.NO_LIMIT)
    SignalStore.settings.threadTrimLength = newLimit
    store.update { it.copy(lengthLimit = newLimit) }

    if (SignalStore.settings.isTrimByLengthEnabled && restrictingChange) {
      SignalExecutors.BOUNDED.execute {
        val keepMessagesDuration = SignalStore.settings.keepMessagesDuration

        val trimBeforeDate = if (keepMessagesDuration != KeepMessagesDuration.FOREVER) {
          System.currentTimeMillis() - keepMessagesDuration.duration
        } else {
          ThreadTable.NO_TRIM_BEFORE_DATE_SET
        }

        SignalDatabase.threads.trimAllThreads(newLimit, trimBeforeDate)
      }
    }
  }

  fun showConfirmSetChatLengthLimit(newLimit: Int): Boolean {
    return isRestrictingLengthLimitChange(newLimit)
  }

  fun setSyncTrimDeletes(syncTrimDeletes: Boolean) {
    SignalStore.settings.setSyncThreadTrimDeletes(syncTrimDeletes)
    store.update { it.copy(syncTrimDeletes = syncTrimDeletes) }
  }

  fun setOptimizeStorage(enabled: Boolean) {
    viewModelScope.launch {
      val storageState = getOnDeviceStorageOptimizationState()
      if (storageState >= OnDeviceStorageOptimizationState.DISABLED) {
        SignalStore.backup.optimizeStorage = enabled
        store.update {
          it.copy(
            onDeviceStorageOptimizationState = if (enabled) OnDeviceStorageOptimizationState.ENABLED else OnDeviceStorageOptimizationState.DISABLED,
            storageOptimizationStateChanged = true
          )
        }
      }
    }
  }

  private fun isRestrictingLengthLimitChange(newLimit: Int): Boolean {
    return state.value.lengthLimit == ManageStorageState.NO_LIMIT || (newLimit != ManageStorageState.NO_LIMIT && newLimit < state.value.lengthLimit)
  }

  private suspend fun getOnDeviceStorageOptimizationState(): OnDeviceStorageOptimizationState {
    return when {
      !SignalStore.backup.areBackupsEnabled || !BackupUpgradeAvailabilityChecker.isUpgradeAvailable(AppDependencies.application) -> OnDeviceStorageOptimizationState.FEATURE_NOT_AVAILABLE
      SignalStore.backup.backupTier != MessageBackupTier.PAID -> OnDeviceStorageOptimizationState.REQUIRES_PAID_TIER
      SignalStore.backup.optimizeStorage -> OnDeviceStorageOptimizationState.ENABLED
      else -> OnDeviceStorageOptimizationState.DISABLED
    }
  }

  override fun onCleared() {
    if (state.value.storageOptimizationStateChanged) {
      when (state.value.onDeviceStorageOptimizationState) {
        OnDeviceStorageOptimizationState.DISABLED -> RestoreOptimizedMediaJob.enqueue()
        OnDeviceStorageOptimizationState.ENABLED -> OptimizeMediaJob.enqueue()
        else -> Unit
      }
    }
  }

  enum class OnDeviceStorageOptimizationState {
    /**
     * The entire feature is not available and the option should not be displayed to the user.
     */
    FEATURE_NOT_AVAILABLE,

    /**
     * The feature is available, but the user is not on the paid backups plan.
     */
    REQUIRES_PAID_TIER,

    /**
     * The user is on the paid backups plan but optimized storage is disabled.
     */
    DISABLED,

    /**
     * The user is on the paid backups plan and optimized storage is enabled.
     */
    ENABLED
  }

  @Immutable
  data class ManageStorageState(
    val keepMessagesDuration: KeepMessagesDuration,
    val lengthLimit: Int,
    val syncTrimDeletes: Boolean,
    val breakdown: MediaTable.StorageBreakdown? = null,
    val cacheBytes: Long = 0,
    val onDeviceStorageOptimizationState: OnDeviceStorageOptimizationState = OnDeviceStorageOptimizationState.FEATURE_NOT_AVAILABLE,
    val storageOptimizationStateChanged: Boolean = false,
    val isPaidTierPending: Boolean = false
  ) {
    companion object {
      const val NO_LIMIT = 0
    }
  }
}
