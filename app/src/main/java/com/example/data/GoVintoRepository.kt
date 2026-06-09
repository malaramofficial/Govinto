package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GoVintoRepository(private val dao: GoVintoDao) {

    val userSession: Flow<UserSession?> = dao.getUserSession()
    val allListings: Flow<List<Listing>> = dao.getAllListings()

    suspend fun saveUserSession(session: UserSession) {
        dao.insertUserSession(session)
    }

    suspend fun insertListing(listing: Listing) {
        dao.insertListing(listing)
    }

    fun getListingById(id: Int): Flow<Listing?> {
        return dao.getListingById(id)
    }

    fun getChatMessages(listingId: Int): Flow<List<ChatMessage>> {
        return dao.getChatMessages(listingId)
    }

    suspend fun insertChatMessage(message: ChatMessage) {
        dao.insertChatMessage(message)
    }

    suspend fun prepopulateIfEmpty() {
        val count = dao.getListingsCount()
        if (count == 0) {
            val defaults = listOf(
                Listing(
                    category = "Clothes",
                    titleEn = "Vintage Denim Jacket",
                    titleHi = "क्लासिक डेनिम जैकेट",
                    price = "750",
                    descEn = "A premium blue denim jacket stored with care. Fits medium size. Perfect for a fresh style statement, looking for a new home!",
                    descHi = "एक प्रीमियम नीली डेनिम जैकेट जिसे सावधानी से रखा गया है। मध्यम आकार। एक नए स्टाइल स्टेटमेंट के लिए एकदम सही, नए घर की तलाश में!",
                    sellerNameEn = "Arjun Sharma",
                    sellerNameHi = "अर्जुन शर्मा",
                    sellerRating = 4.8f,
                    imageResName = "denim",
                    isSold = false
                ),
                Listing(
                    category = "Electronics",
                    titleEn = "Mechanical RGB Keyboard",
                    titleHi = "मैकेनिकल आरजीबी कीबोर्ड",
                    price = "1200",
                    descEn = "Tactile blue switch mechanical keyboard with stunning RGB backlights. Barely used, fully cleaned. Perfect for students and writers looking for a new journey.",
                    descHi = "आकर्षक आरजीबी बैकलाइट वाला शानदार मैकेनिकल कीबोर्ड। बहुत कम इस्तेमाल किया गया, पूरी तरह से साफ। नया सफर शुरू करने वाले छात्रों और लेखकों के लिए उत्तम।",
                    sellerNameEn = "Sofia Verma",
                    sellerNameHi = "सोफिया वर्मा",
                    sellerRating = 4.5f,
                    imageResName = "keyboard",
                    isSold = false
                ),
                Listing(
                    category = "Furniture",
                    titleEn = "Wooden Study Desk",
                    titleHi = "लकड़ी की स्टडी डेस्क",
                    price = "1800",
                    descEn = "Sturdy study desk made of seasoned solid wood. Served as my guide during college success. Now ready to empower another young learner!",
                    descHi = "मजबूत लकड़ी की बनी स्टडी टेबल। कॉलेज की परीक्षाओं में मेरी मार्गदर्शक रही। अब एक और युवा विद्यार्थी के सपनों को उड़ान देने के लिए तैयार!",
                    sellerNameEn = "Rajesh Kumar",
                    sellerNameHi = "राजेश कुमार",
                    sellerRating = 4.9f,
                    imageResName = "table",
                    isSold = false
                ),
                Listing(
                    category = "Books",
                    titleEn = "Inspirational Novel Collection",
                    titleHi = "प्रेरणादायक उपन्यासों का संग्रह",
                    price = "450",
                    descEn = "A set of three best-selling wisdom books on positivity, growth, and leadership. Crisp pages, ready to spark inspiration in your life.",
                    descHi = "सकारात्मकता, प्रगति और नेतृत्व पर तीन सबसे अधिक बिकने वाली किताबों का सेट। स्पष्ट पन्ने, आपके जीवन में प्रेरणा की नई लौ जगाने को तैयार।",
                    sellerNameEn = "Neha Patel",
                    sellerNameHi = "नेहा पटेल",
                    sellerRating = 4.7f,
                    imageResName = "books",
                    isSold = false
                )
            )
            dao.insertAllListings(defaults)
        }
    }
}
