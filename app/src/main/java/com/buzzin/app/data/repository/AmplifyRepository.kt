package com.buzzin.app.data.repository

import com.buzzin.app.data.api.ApiResult
import com.buzzin.app.data.api.BuzzInApiClient
import com.buzzin.app.data.model.*

/**
 * Repository for AWS Amplify backend operations
 * Provides clean API for ViewModels to access backend
 */
class AmplifyRepository(
    private val apiClient: BuzzInApiClient = BuzzInApiClient.getInstance()
) {

    // User Operations
    suspend fun createUser(name: String, age: Int, photoRes: String, bio: String? = null): Result<User> {
        return when (val result = apiClient.createUser(name, age, photoRes, bio)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    // Location Operations
    suspend fun getNearbyLocations(lat: Double, lng: Double, radiusKm: Double = 1.0): Result<List<Location>> {
        return when (val result = apiClient.getNearbyLocations(lat, lng, radiusKm)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    // Check-in Operations
    suspend fun checkIn(userId: String, locationId: String, lat: Double, lng: Double): Result<CheckIn> {
        return when (val result = apiClient.checkIn(userId, locationId, lat, lng)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    suspend fun checkOut(checkInId: String): Result<CheckIn> {
        return when (val result = apiClient.checkOut(checkInId)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    // Swipe Operations
    suspend fun getActiveUsersAtLocation(locationId: String): Result<List<User>> {
        return when (val result = apiClient.getActiveUsersAtLocation(locationId)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    suspend fun performSwipe(
        userId: String,
        targetUserId: String,
        locationId: String,
        action: SwipeAction
    ): Result<SwipeResult> {
        return when (val result = apiClient.performSwipe(userId, targetUserId, locationId, action)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    // Match Operations
    suspend fun getUserMatches(userId: String): Result<List<Match>> {
        return when (val result = apiClient.getUserMatches(userId)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    // Message Operations
    suspend fun sendMessage(matchId: String, senderId: String, text: String, photoRes: String? = null): Result<Message> {
        return when (val result = apiClient.sendMessage(matchId, senderId, text, photoRes)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    companion object {
        @Volatile
        private var instance: AmplifyRepository? = null

        fun getInstance(): AmplifyRepository {
            return instance ?: synchronized(this) {
                instance ?: AmplifyRepository().also { instance = it }
            }
        }
    }
}
