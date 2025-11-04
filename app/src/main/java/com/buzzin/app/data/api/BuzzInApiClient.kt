package com.buzzin.app.data.api

import android.util.Log
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify
import com.buzzin.app.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * API client for BuzzIn backend (AWS Amplify GraphQL)
 */
class BuzzInApiClient {

    private val gson = Gson()

    /**
 * Create a new user
     */
    suspend fun createUser(name: String, age: Int, photoRes: String, bio: String? = null): ApiResult<User> {
        val mutation = """
            mutation CreateUser {
                createUser(input: {
                    name: "$name"
                    age: $age
                    photoRes: "$photoRes"
                    bio: "${bio ?: ""}"
                }) {
                    id
                    name
                    age
                    photoRes
                    bio
                }
            }
        """.trimIndent()

        return executeMutation(mutation)
    }

    /**
     * Get nearby locations
     */
    suspend fun getNearbyLocations(lat: Double, lng: Double, radiusKm: Double = 1.0): ApiResult<List<Location>> {
        val query = """
            query ListNearbyLocations {
                listNearbyLocations(
                    latitude: $lat
                    longitude: $lng
                    radiusKm: $radiusKm
                ) {
                    id
                    name
                    address
                    latitude
                    longitude
                    type
                }
            }
        """.trimIndent()

        return executeQuery(query)
    }

    /**
     * Check in to a location
     */
    suspend fun checkIn(userId: String, locationId: String, lat: Double, lng: Double): ApiResult<CheckIn> {
        val now = java.time.Instant.now().toString()

        val mutation = """
            mutation CheckIn {
                createCheckIn(input: {
                    userId: "$userId"
                    locationId: "$locationId"
                    checkInTime: "$now"
                    isActive: true
                    latitude: $lat
                    longitude: $lng
                }) {
                    id
                    userId
                    locationId
                    checkInTime
                    isActive
                }
            }
        """.trimIndent()

        return executeMutation(mutation)
    }

    /**
     * Check out (buzz out)
     */
    suspend fun checkOut(checkInId: String): ApiResult<CheckIn> {
        val now = java.time.Instant.now().toString()

        val mutation = """
            mutation CheckOut {
                updateCheckIn(input: {
                    id: "$checkInId"
                    checkOutTime: "$now"
                    isActive: false
                }) {
                    id
                    checkOutTime
                    isActive
                }
            }
        """.trimIndent()

        return executeMutation(mutation)
    }

    /**
     * Get active users at a location (for swiping)
     */
    suspend fun getActiveUsersAtLocation(locationId: String): ApiResult<List<User>> {
        val query = """
            query GetActiveUsers {
                getActiveUsersAtLocation(locationId: "$locationId") {
                    id
                    name
                    age
                    bio
                    photoRes
                    interests
                }
            }
        """.trimIndent()

        return executeQuery(query)
    }

    /**
     * Perform swipe (like or ignore)
     */
    suspend fun performSwipe(
        userId: String,
        targetUserId: String,
        locationId: String,
        action: SwipeAction
    ): ApiResult<SwipeResult> {
        val mutation = """
            mutation PerformSwipe {
                performSwipe(
                    userId: "$userId"
                    targetUserId: "$targetUserId"
                    locationId: "$locationId"
                    action: ${action.name}
                ) {
                    matched
                    match {
                        id
                        matchTime
                        user1 { id name photoRes }
                        user2 { id name photoRes }
                        location { name }
                    }
                }
            }
        """.trimIndent()

        return executeMutation(mutation)
    }

    /**
     * Get user's matches
     */
    suspend fun getUserMatches(userId: String): ApiResult<List<Match>> {
        val query = """
            query GetMatches {
                listMatches(filter: {
                    or: [
                        { user1Id: { eq: "$userId" } }
                        { user2Id: { eq: "$userId" } }
                    ]
                }) {
                    items {
                        id
                        matchTime
                        messageSent
                        user1 { id name photoRes }
                        user2 { id name photoRes }
                        location { name }
                    }
                }
            }
        """.trimIndent()

        return executeQuery(query)
    }

    /**
     * Send message
     */
    suspend fun sendMessage(matchId: String, senderId: String, text: String, photoRes: String? = null): ApiResult<Message> {
        val now = java.time.Instant.now().toString()
        val photoField = photoRes?.let { """photoRes: "$it"""" } ?: ""

        val mutation = """
            mutation SendMessage {
                createMessage(input: {
                    matchId: "$matchId"
                    senderId: "$senderId"
                    text: "$text"
                    $photoField
                    timestamp: "$now"
                    isRead: false
                }) {
                    id
                    text
                    photoRes
                    timestamp
                }
            }
        """.trimIndent()

        return executeMutation(mutation)
    }

    // Helper functions
    private suspend fun <T> executeQuery(query: String): ApiResult<T> = suspendCancellableCoroutine { continuation ->
        Amplify.API.query(
            SimpleGraphQLRequest<String>(
                query,
                String::class.java,
                GraphQLRequest.VariablesSerializer { "{}" }
            ),
            { response ->
                Log.d(TAG, "Query success: ${response.data}")
                try {
                    continuation.resume(ApiResult.Success(response.data as T))
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            },
            { error ->
                Log.e(TAG, "Query error", error)
                continuation.resume(ApiResult.Error(error.message ?: "Unknown error"))
            }
        )
    }

    private suspend fun <T> executeMutation(mutation: String): ApiResult<T> = suspendCancellableCoroutine { continuation ->
        Amplify.API.mutate(
            SimpleGraphQLRequest<String>(
                mutation,
                String::class.java,
                GraphQLRequest.VariablesSerializer { "{}" }
            ),
            { response ->
                Log.d(TAG, "Mutation success: ${response.data}")
                try {
                    continuation.resume(ApiResult.Success(response.data as T))
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            },
            { error ->
                Log.e(TAG, "Mutation error", error)
                continuation.resume(ApiResult.Error(error.message ?: "Unknown error"))
            }
        )
    }

    companion object {
        private const val TAG = "BuzzInApiClient"

        @Volatile
        private var instance: BuzzInApiClient? = null

        fun getInstance(): BuzzInApiClient {
            return instance ?: synchronized(this) {
                instance ?: BuzzInApiClient().also { instance = it }
            }
        }
    }
}

/**
 * API Result wrapper
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}
