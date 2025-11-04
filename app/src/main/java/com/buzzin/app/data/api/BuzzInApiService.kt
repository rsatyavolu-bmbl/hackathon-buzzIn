package com.buzzin.app.data.api

import com.buzzin.app.data.DeviceMetadata
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for BuzzIn backend.
 * All requests include device/application metadata for context tracking.
 */
interface BuzzInApiService {

    /**
     * Register or update device information in the backend.
     */
    @POST("api/v1/devices/register")
    suspend fun registerDevice(
        @Body deviceInfo: DeviceRegistrationRequest
    ): Response<DeviceRegistrationResponse>

    /**
     * Save user context/state to backend.
     */
    @POST("api/v1/context/save")
    suspend fun saveContext(
        @Header("X-Application-Id") applicationId: String,
        @Header("X-User-Id") userId: String?,
        @Body context: UserContextRequest
    ): Response<ContextResponse>

    /**
     * Retrieve user context from backend.
     */
    @GET("api/v1/context")
    suspend fun getContext(
        @Header("X-Application-Id") applicationId: String,
        @Header("X-User-Id") userId: String?
    ): Response<UserContextResponse>

    /**
     * Buzz in at a location.
     */
    @POST("api/v1/buzzin")
    suspend fun buzzIn(
        @Header("X-Application-Id") applicationId: String,
        @Header("X-Session-Id") sessionId: String,
        @Body buzzInRequest: BuzzInRequest
    ): Response<BuzzInResponse>

    /**
     * Get nearby buzz-ins.
     */
    @GET("api/v1/buzzin/nearby")
    suspend fun getNearbyBuzzIns(
        @Header("X-Application-Id") applicationId: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusMeters: Int = 1000
    ): Response<NearbyBuzzInsResponse>
}

// Request/Response Data Classes

data class DeviceRegistrationRequest(
    val applicationId: String,
    val androidDeviceId: String,
    val appVersion: String,
    val deviceModel: String = android.os.Build.MODEL,
    val osVersion: String = android.os.Build.VERSION.RELEASE,
    val userId: String? = null
)

data class DeviceRegistrationResponse(
    val success: Boolean,
    val deviceId: String,
    val message: String
)

data class UserContextRequest(
    val lastLocation: LocationData?,
    val preferences: Map<String, Any>,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

data class UserContextResponse(
    val applicationId: String,
    val userId: String?,
    val lastLocation: LocationData?,
    val preferences: Map<String, Any>,
    val lastActiveTimestamp: Long
)

data class ContextResponse(
    val success: Boolean,
    val message: String
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class BuzzInRequest(
    val placeId: String,
    val placeName: String,
    val location: LocationData,
    val duration: Int = 60, // minutes
    val visibility: String = "public" // public, friends, private
)

data class BuzzInResponse(
    val success: Boolean,
    val buzzInId: String,
    val expiresAt: Long,
    val nearbyUsers: Int
)

data class NearbyBuzzInsResponse(
    val buzzIns: List<BuzzInInfo>,
    val totalCount: Int
)

data class BuzzInInfo(
    val buzzInId: String,
    val userId: String,
    val userName: String,
    val placeId: String,
    val placeName: String,
    val location: LocationData,
    val createdAt: Long,
    val expiresAt: Long
)

