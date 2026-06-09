package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoVintoDao {
    @Query("SELECT * FROM user_session WHERE id = 0")
    fun getUserSession(): Flow<UserSession?>

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getListingsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(session: UserSession)

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllListings(listings: List<Listing>)

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getListingById(id: Int): Flow<Listing?>

    @Query("SELECT * FROM chat_messages WHERE listingId = :listingId ORDER BY timestamp ASC")
    fun getChatMessages(listingId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)
}
