package com.example.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseListingService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val listingsCollection = firestore.collection("listings")
    private val cloudinary = CloudinaryImageService()

    suspend fun publishListing(listing: Listing): Listing {
        val user = auth.currentUser ?: error("Google login required")
        val doc = listingsCollection.document()
        var imageUrl = ""

        if (listing.customImageUri.isNotBlank()) {
            imageUrl = cloudinary.uploadImage(Uri.parse(listing.customImageUri), doc.id)
        }

        val data = hashMapOf<String, Any>(
            "ownerUid" to user.uid,
            "category" to listing.category,
            "titleEn" to listing.titleEn,
            "titleHi" to listing.titleHi,
            "price" to listing.price,
            "descEn" to listing.descEn,
            "descHi" to listing.descHi,
            "sellerNameEn" to listing.sellerNameEn,
            "sellerNameHi" to listing.sellerNameHi,
            "sellerRating" to listing.sellerRating.toDouble(),
            "isSold" to listing.isSold,
            "imageUrl" to imageUrl,
            "createdAt" to listing.createdAt
        )
        doc.set(data).await()

        return listing.copy(
            id = stableLocalId(doc.id),
            cloudId = doc.id,
            ownerUid = user.uid,
            customImageUri = imageUrl,
            isUploadedByUser = true
        )
    }

    suspend fun fetchListings(): List<Listing> {
        val currentUid = auth.currentUser?.uid.orEmpty()
        val snapshot = listingsCollection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        return snapshot.documents.map { documentToListing(it, currentUid) }
    }

    suspend fun deleteListing(cloudId: String) {
        if (cloudId.isBlank()) return
        listingsCollection.document(cloudId).delete().await()
    }

    suspend fun updateSold(cloudId: String, sold: Boolean) {
        if (cloudId.isBlank()) return
        listingsCollection.document(cloudId).update("isSold", sold).await()
    }

    private fun documentToListing(doc: DocumentSnapshot, currentUid: String): Listing {
        val ownerUid = doc.getString("ownerUid").orEmpty()
        return Listing(
            id = stableLocalId(doc.id),
            cloudId = doc.id,
            ownerUid = ownerUid,
            category = doc.getString("category").orEmpty(),
            titleEn = doc.getString("titleEn").orEmpty(),
            titleHi = doc.getString("titleHi").orEmpty(),
            price = doc.getString("price").orEmpty(),
            descEn = doc.getString("descEn").orEmpty(),
            descHi = doc.getString("descHi").orEmpty(),
            sellerNameEn = doc.getString("sellerNameEn").orEmpty(),
            sellerNameHi = doc.getString("sellerNameHi").orEmpty(),
            sellerRating = (doc.getDouble("sellerRating") ?: 5.0).toFloat(),
            isSold = doc.getBoolean("isSold") ?: false,
            isUploadedByUser = ownerUid == currentUid,
            customImageUri = doc.getString("imageUrl").orEmpty(),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    private fun stableLocalId(cloudId: String): Int {
        val value = cloudId.hashCode() and Int.MAX_VALUE
        return if (value == 0) 1 else value
    }
}
