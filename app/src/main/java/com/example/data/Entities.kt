package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSession(
    @PrimaryKey val id: Int = 0,
    val selectedLanguage: String = "", // "en" or "hi"
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val titleEn: String,
    val titleHi: String,
    val price: String,
    val descEn: String,
    val descHi: String,
    val sellerNameEn: String,
    val sellerNameHi: String,
    val sellerRating: Float = 4.5f,
    val isSold: Boolean = false,
    val isUploadedByUser: Boolean = false,
    val imageResName: String = "",
    val customImageUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val cloudId: String = "",
    val ownerUid: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val sender: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
