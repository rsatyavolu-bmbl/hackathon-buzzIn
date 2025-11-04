package com.buzzin.app.data.repository

import android.content.Context
import com.buzzin.app.data.DeviceIdManager
import com.buzzin.app.data.api.*
import retrofit2.Response

/**
 * Repository for BuzzIn data operations.
 * Automatically includes device/application ID in all backend requests.
 */
class BuzzInRepository(
    private val apiService: BuzzInApiService,
    private val deviceIdManager: DeviceIdManager,
    private val context: Context
) {

    /**
     * Register this device with the backend.
     * Should be called on first app launch or after login.
     */
    suspend fun registerDevice(): Result<DeviceRegistrationResponse> {
        return try {
            val metadata = deviceIdManager.getDeviceMetadata(context)
            val request = DeviceRegistrationRequest(
                applicationId = metadata.applicationId,
                androidDeviceId = metadata.androidDeviceId,
                appVersion = metadata.appVersion,
                userId = metadata.userId
            )
            
            val response = apiService.registerDevice(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to register device: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save user context to backend.
     */
    suspend fun saveContext(
        lastLocation: LocationData?,
        preferences: Map<String, Any>
    ): Result<ContextResponse> {
        return try {
            val request = UserContextRequest(
                lastLocation = lastLocation,
                preferences = preferences
            )
            
            val response = apiService.saveContext(
                applicationId = deviceIdManager.applicationId,
                userId = deviceIdManager.userId,
                context = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to save context: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieve user context from backend.
     */
    suspend fun getContext(): Result<UserContextResponse> {
        return try {
            val response = apiService.getContext(
                applicationId = deviceIdManager.applicationId,
                userId = deviceIdManager.userId
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get context: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buzz in at a location.
     */
    suspend fun buzzIn(request: BuzzInRequest): Result<BuzzInResponse> {
        return try {
            val metadata = deviceIdManager.getDeviceMetadata(context)
            val response = apiService.buzzIn(
                applicationId = metadata.applicationId,
                sessionId = metadata.sessionId,
                buzzInRequest = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to buzz in: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get nearby buzz-ins.
     */
    suspend fun getNearbyBuzzIns(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 1000
    ): Result<NearbyBuzzInsResponse> {
        return try {
            val response = apiService.getNearbyBuzzIns(
                applicationId = deviceIdManager.applicationId,
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get nearby buzz-ins: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Set the user ID after authentication.
     */
    fun setUserId(userId: String) {
        deviceIdManager.userId = userId
    }

    /**
     * Clear user ID on logout.
     */
    fun clearUserId() {
        deviceIdManager.clearUserId()
    }

    /**
     * Get the current application ID.
     */
    fun getApplicationId(): String = deviceIdManager.applicationId

    /**
     * Get complete device metadata.
     */
    fun getDeviceMetadata() = deviceIdManager.getDeviceMetadata(context)
}

