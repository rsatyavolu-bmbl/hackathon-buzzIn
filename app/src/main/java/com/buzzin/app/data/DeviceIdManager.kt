package com.buzzin.app.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.util.UUID

/**
 * Manages unique device/application identifiers for tracking user context across devices.
 * 
 * This class generates and persists a unique identifier that can be used to:
 * - Track user sessions across app restarts
 * - Save user context in backend database
 * - Distinguish between multiple devices for the same user
 * - Enable cross-device synchronization
 */
class DeviceIdManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Gets or creates a unique application instance ID.
     * This ID persists across app restarts but will be reset if app data is cleared.
     * 
     * Use this for:
     * - Tracking app installations
     * - Saving user preferences and context
     * - Backend database primary key
     */
    val applicationId: String
        get() {
            var id = prefs.getString(KEY_APPLICATION_ID, null)
            if (id == null) {
                id = generateUniqueId()
                prefs.edit().putString(KEY_APPLICATION_ID, id).apply()
            }
            return id
        }

    /**
     * Gets the Android device ID (Android ID).
     * This is unique per device and app installation.
     * 
     * Note: This ID can change if factory reset is performed.
     */
    fun getAndroidDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    /**
     * Gets a session ID that changes each time the app is launched.
     * Use this for tracking individual app sessions.
     */
    val sessionId: String
        get() = generateUniqueId()

    /**
     * Gets or creates a user-specific ID that can be linked to user account.
     * This should be set after user authentication.
     */
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) {
            prefs.edit().putString(KEY_USER_ID, value).apply()
        }

    /**
     * Clears the user ID (e.g., on logout).
     * Application ID is preserved.
     */
    fun clearUserId() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    /**
     * Clears all stored IDs (e.g., for testing or complete reset).
     */
    fun clearAllIds() {
        prefs.edit().clear().apply()
    }

    /**
     * Gets a composite identifier that includes both application and user IDs.
     * Format: "app_<applicationId>_user_<userId>"
     */
    fun getCompositeId(): String {
        val appId = applicationId
        val usrId = userId ?: "anonymous"
        return "app_${appId}_user_${usrId}"
    }

    /**
     * Gets metadata about the current device/app instance.
     */
    fun getDeviceMetadata(context: Context): DeviceMetadata {
        return DeviceMetadata(
            applicationId = applicationId,
            userId = userId,
            androidDeviceId = getAndroidDeviceId(context),
            sessionId = sessionId,
            appVersion = getAppVersion(context)
        )
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    companion object {
        private const val PREFS_NAME = "buzzin_device_prefs"
        private const val KEY_APPLICATION_ID = "application_id"
        private const val KEY_USER_ID = "user_id"

        @Volatile
        private var INSTANCE: DeviceIdManager? = null

        /**
         * Gets the singleton instance of DeviceIdManager.
         */
        fun getInstance(context: Context): DeviceIdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeviceIdManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

/**
 * Data class containing device and application metadata.
 */
data class DeviceMetadata(
    val applicationId: String,
    val userId: String?,
    val androidDeviceId: String,
    val sessionId: String,
    val appVersion: String
)

