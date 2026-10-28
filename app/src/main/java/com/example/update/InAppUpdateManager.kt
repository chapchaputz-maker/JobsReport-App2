package com.example.update

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * State representing Google Play In-App Update flow status.
 */
sealed class InAppUpdateUIState {
    object Idle : InAppUpdateUIState()

    data class UpdateAvailable(
        val appUpdateInfo: AppUpdateInfo,
        val isImmediate: Boolean,
        val availableVersionCode: Int,
        val clientVersionStalenessDays: Int?
    ) : InAppUpdateUIState()

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytesToDownload: Long
    ) : InAppUpdateUIState() {
        val progress: Float
            get() = if (totalBytesToDownload > 0) {
                (bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()).coerceIn(0f, 1f)
            } else 0f
    }

    object Downloaded : InAppUpdateUIState()

    data class Error(val message: String) : InAppUpdateUIState()
}

/**
 * Manages Google Play In-App Updates in accordance with official Android documentation:
 * - Checks for update availability on app start and when returning to foreground.
 * - Supports Flexible update flow for regular improvements and Immediate for critical updates.
 * - Handles download progress and completion.
 * - Respects session-level dismissal so users are not annoyed.
 */
class InAppUpdateManager private constructor(context: Context) {

    companion object {
        private const val TAG = "InAppUpdateManager"
        const val REQUEST_CODE_IMMEDIATE_UPDATE = 8192

        // Configurable thresholds: update priority (0-5, where 4-5 is critical)
        private const val CRITICAL_UPDATE_PRIORITY = 4
        // If client is older than 30 days without updating
        private const val STALENESS_CRITICAL_DAYS = 30

        @Volatile
        private var instance: InAppUpdateManager? = null

        fun getInstance(context: Context): InAppUpdateManager {
            return instance ?: synchronized(this) {
                instance ?: InAppUpdateManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var isListenerRegistered = false
    private var userDismissedThisSession = false

    var uiState by mutableStateOf<InAppUpdateUIState>(InAppUpdateUIState.Idle)
        private set

    private val installStateListener = InstallStateUpdatedListener { state: InstallState ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                uiState = InAppUpdateUIState.Downloading(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytesToDownload = state.totalBytesToDownload()
                )
            }
            InstallStatus.DOWNLOADED -> {
                uiState = InAppUpdateUIState.Downloaded
            }
            InstallStatus.FAILED -> {
                Log.w(TAG, "In-app update download failed with code: ${state.installErrorCode()}")
                uiState = InAppUpdateUIState.Error("Update download could not complete.")
            }
            InstallStatus.CANCELED -> {
                Log.d(TAG, "In-app update download was cancelled.")
                uiState = InAppUpdateUIState.Idle
            }
            else -> {}
        }
    }

    init {
        registerListener()
    }

    private fun registerListener() {
        if (!isListenerRegistered) {
            appUpdateManager.registerListener(installStateListener)
            isListenerRegistered = true
        }
    }

    fun unregisterListener() {
        if (isListenerRegistered) {
            appUpdateManager.unregisterListener(installStateListener)
            isListenerRegistered = false
        }
    }

    /**
     * Checks whether an update is available on Google Play.
     */
    fun checkForUpdate() {
        if (userDismissedThisSession) {
            Log.d(TAG, "User already dismissed update prompt for this session. Skipping check.")
            return
        }

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when (info.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    val priority = info.updatePriority()
                    val staleness = info.clientVersionStalenessDays() ?: 0

                    val isImmediate = (priority >= CRITICAL_UPDATE_PRIORITY) ||
                            (staleness >= STALENESS_CRITICAL_DAYS)

                    val isAllowed = if (isImmediate) {
                        info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    } else {
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    }

                    if (isAllowed) {
                        uiState = InAppUpdateUIState.UpdateAvailable(
                            appUpdateInfo = info,
                            isImmediate = isImmediate,
                            availableVersionCode = info.availableVersionCode(),
                            clientVersionStalenessDays = info.clientVersionStalenessDays()
                        )
                        Log.d(TAG, "Update available: code=${info.availableVersionCode()}, isImmediate=$isImmediate")
                    } else {
                        Log.d(TAG, "Update available but requested update type is not allowed.")
                    }
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    // Resume an immediate update that was interrupted
                    uiState = InAppUpdateUIState.UpdateAvailable(
                        appUpdateInfo = info,
                        isImmediate = true,
                        availableVersionCode = info.availableVersionCode(),
                        clientVersionStalenessDays = info.clientVersionStalenessDays()
                    )
                }
                else -> {
                    if (info.installStatus() == InstallStatus.DOWNLOADED) {
                        uiState = InAppUpdateUIState.Downloaded
                    } else {
                        Log.d(TAG, "No update available on Google Play (status=${info.updateAvailability()}).")
                    }
                }
            }
        }.addOnFailureListener { e ->
            // Non-Play environments or offline device; silently handle without disturbing the user
            Log.d(TAG, "Google Play In-App Update check failed: ${e.message}")
        }
    }

    /**
     * Triggers the genuine Google Play in-app update flow.
     */
    fun startUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        isImmediate: Boolean = false,
        launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    ): Boolean {
        val updateType = if (isImmediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        val options = AppUpdateOptions.newBuilder(updateType).build()

        return try {
            if (launcher != null) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
            } else {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    options,
                    REQUEST_CODE_IMMEDIATE_UPDATE
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start in-app update flow", e)
            uiState = InAppUpdateUIState.Error("Unable to open Google Play update flow.")
            false
        }
    }

    /**
     * For Flexible updates: installs the downloaded update and restarts the app.
     */
    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete update", e)
        }
    }

    /**
     * Dismisses the prompt for the current session.
     */
    fun dismissPrompt() {
        userDismissedThisSession = true
        uiState = InAppUpdateUIState.Idle
    }

    /**
     * Must be called in Activity onResume to handle interrupted updates and downloaded state.
     */
    fun onResume(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                uiState = InAppUpdateUIState.Downloaded
            } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume in-progress immediate update
                val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        activity,
                        options,
                        REQUEST_CODE_IMMEDIATE_UPDATE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to resume immediate update in onResume", e)
                }
            }
        }.addOnFailureListener { e ->
            Log.d(TAG, "Failed to check update info in onResume: ${e.message}")
        }
    }
}
