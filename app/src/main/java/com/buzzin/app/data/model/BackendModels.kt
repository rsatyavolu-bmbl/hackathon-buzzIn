package com.buzzin.app.data.model

/**
 * Backend data models matching AWS Amplify GraphQL schema
 */

data class User(
    val id: String,
    val name: String,
    val age: Int,
    val bio: String? = null,
    val photoRes: String,
    val interests: List<String>? = null
)

data class Location(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType
)

enum class LocationType {
    COFFEE,
    RESTAURANT,
    BAR,
    PARK,
    GYM,
    OTHER
}

data class CheckIn(
    val id: String,
    val userId: String,
    val locationId: String,
    val checkInTime: String,
    val checkOutTime: String? = null,
    val isActive: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class Swipe(
    val id: String,
    val userId: String,
    val targetUserId: String,
    val locationId: String,
    val action: SwipeAction,
    val timestamp: String
)

enum class SwipeAction {
    LIKE,
    IGNORE
}

data class Match(
    val id: String,
    val user1: User?,
    val user2: User?,
    val location: LocationInfo?,
    val matchTime: String,
    val messageSent: Boolean = false
)

data class LocationInfo(
    val id: String? = null,
    val name: String
)

data class Message(
    val id: String,
    val matchId: String,
    val senderId: String,
    val text: String,
    val photoRes: String? = null,
    val timestamp: String,
    val isRead: Boolean = false
)

data class SwipeResult(
    val matched: Boolean,
    val match: Match? = null
)
