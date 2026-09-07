package com.example.data

import kotlinx.coroutines.flow.Flow

class GoVintoRepository(private val dao: GoVintoDao, private val firebase: FirebaseListingService = FirebaseListingService()) {
    val userSession: Flow<UserSession?> = dao.getUserSession()
    val allListings: Flow<List<Listing>> = dao.getAllListings()

    suspend fun saveUserSession(session: UserSession) = dao.insertUserSession(session)

    suspend fun insertListing(listing: Listing) = dao.insertListing(listing)

    suspend fun publishListing(listing: Listing): Listing {
        val cloudListing = firebase.publishListing(listing)
        dao.insertListing(cloudListing)
        return cloudListing
    }

    suspend fun deleteListing(listing: Listing) {
        if (listing.cloudId.isNotBlank()) firebase.deleteListing(listing.cloudId)
        dao.deleteListing(listing.id)
    }

    suspend fun toggleSold(listing: Listing): Listing {
        val newSold = !listing.isSold
        if (listing.cloudId.isNotBlank()) firebase.updateSold(listing.cloudId, newSold)
        val updated = listing.copy(isSold = newSold)
        dao.insertListing(updated)
        return updated
    }

    fun getListingById(id: Int): Flow<Listing?> = dao.getListingById(id)
    fun getChatMessages(listingId: Int): Flow<List<ChatMessage>> = dao.getChatMessages(listingId)
    suspend fun insertChatMessage(message: ChatMessage) = dao.insertChatMessage(message)

    suspend fun syncListingsFromCloud() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) return
        val cloudListings = firebase.fetchListings()
        cloudListings.forEach { dao.insertListing(it) }
    }

    suspend fun prepopulateIfEmpty() {
        if (dao.getListingsCount() != 0) return
        dao.insertAllListings(listOf(
            Listing(category="Clothes", titleEn="Vintage Denim Jacket", titleHi="क्लासिक डेनिम जैकेट", price="750", descEn="Premium blue denim jacket, carefully stored and lightly used.", descHi="प्रीमियम नीली डेनिम जैकेट, अच्छी तरह रखी हुई और कम इस्तेमाल की गई।", sellerNameEn="Arjun Sharma", sellerNameHi="अर्जुन शर्मा", sellerRating=4.8f, imageResName="denim"),
            Listing(category="Electronics", titleEn="Mechanical RGB Keyboard", titleHi="मैकेनिकल RGB कीबोर्ड", price="1200", descEn="Mechanical keyboard with RGB backlight, lightly used and cleaned.", descHi="RGB बैकलाइट वाला मैकेनिकल कीबोर्ड, कम इस्तेमाल किया गया और साफ है।", sellerNameEn="Sofia Verma", sellerNameHi="सोफिया वर्मा", sellerRating=4.5f, imageResName="keyboard"),
            Listing(category="Furniture", titleEn="Wooden Study Desk", titleHi="लकड़ी की स्टडी डेस्क", price="1800", descEn="Sturdy seasoned-wood study desk in good condition.", descHi="अच्छी स्थिति में मजबूत और टिकाऊ लकड़ी की स्टडी डेस्क।", sellerNameEn="Rajesh Kumar", sellerNameHi="राजेश कुमार", sellerRating=4.9f, imageResName="table"),
            Listing(category="Books", titleEn="Inspirational Book Collection", titleHi="प्रेरणादायक किताबों का संग्रह", price="450", descEn="Three books on positivity, growth and leadership.", descHi="सकारात्मकता, प्रगति और नेतृत्व पर तीन किताबों का संग्रह।", sellerNameEn="Neha Patel", sellerNameHi="नेहा पटेल", sellerRating=4.7f, imageResName="books")
        ))
    }
}
