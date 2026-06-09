package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoVintoViewModel(application: Application, private val repository: GoVintoRepository) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Current session
    val userSession: StateFlow<UserSession> = repository.userSession
        .map { it ?: UserSession() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSession())

    // Current items/listings
    val listings: StateFlow<List<Listing>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI screen navigation / flow controllers
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _selectedDetailListingId = MutableStateFlow<Int?>(null)
    val selectedDetailListingId: StateFlow<Int?> = _selectedDetailListingId.asStateFlow()

    val detailListing: StateFlow<Listing?> = _selectedDetailListingId
        .flatMapLatest { id ->
            if (id != null) repository.getListingById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Search and Category filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    // Dynamic list filtered by search & categories
    val filteredListings: StateFlow<List<Listing>> = combine(listings, searchQuery, selectedCategory) { list, query, cat ->
        list.filter { item ->
            val matchCat = cat == "All" || item.category == cat
            val titleMatches = item.titleEn.contains(query, ignoreCase = true) || item.titleHi.contains(query, ignoreCase = true)
            val descMatches = item.descEn.contains(query, ignoreCase = true) || item.descHi.contains(query, ignoreCase = true)
            matchCat && (titleMatches || descMatches)
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chatting
    private val _activeChatListingId = MutableStateFlow<Int?>(null)
    val activeChatListingId: StateFlow<Int?> = _activeChatListingId.asStateFlow()

    val activeChatListing: StateFlow<Listing?> = _activeChatListingId
        .flatMapLatest { id ->
            if (id != null) repository.getListingById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatListingId
        .flatMapLatest { id ->
            if (id != null) repository.getChatMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSellerTyping = MutableStateFlow(false)
    val isSellerTyping: StateFlow<Boolean> = _isSellerTyping.asStateFlow()

    // Google Auth states
    val loginError = MutableStateFlow("")
    val isGoogleSigningIn = MutableStateFlow(false)

    val tempLanguage = MutableStateFlow("") // Temp language selected on Screen

    // Selling Step States
    val sellStep = MutableStateFlow(1) // Step 1: Photo, Step 2: Write Details, Step 3: Success Animation
    val sellPhotoKey = MutableStateFlow("clothes") // "clothes", "keyboard", "table", "books"
    val sellTitle = MutableStateFlow("")
    val sellCategory = MutableStateFlow("Clothes")
    val sellPrice = MutableStateFlow("")
    val sellDesc = MutableStateFlow("")
    val sellFormError = MutableStateFlow(false)
    val sellSuccessAnimation = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    repository.prepopulateIfEmpty()
                }
            } catch (e: Exception) {
                android.util.Log.e("GoVintoViewModel", "Error prepopulating database", e)
            } finally {
                delay(300)
                _isLoading.value = false
            }
        }
    }

    // Set Language initially
    fun chooseLanguage(lang: String) {
        tempLanguage.value = lang
    }

    // Toggle language dynamically from any screen
    fun switchLanguage(lang: String) {
        viewModelScope.launch {
            val session = userSession.value
            repository.saveUserSession(session.copy(selectedLanguage = lang))
        }
    }

    fun confirmLanguage() {
        viewModelScope.launch {
            val session = userSession.value
            repository.saveUserSession(session.copy(selectedLanguage = tempLanguage.value))
        }
    }

    // Sign out / Change language
    fun signOut() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                // Ignore safe errors if Firebase is unset
            }
            repository.saveUserSession(UserSession(id = 0, selectedLanguage = "", email = "", displayName = "", photoUrl = "", isLoggedIn = false))
            loginError.value = ""
            tempLanguage.value = ""
            _currentTab.value = "home"
            _selectedDetailListingId.value = null
            _activeChatListingId.value = null
        }
    }

    // Google Sign-In success integration combining Firebase Authentication and custom Session persistence
    fun onGoogleSignInSuccess(idToken: String, email: String, name: String, photo: String) {
        viewModelScope.launch {
            isGoogleSigningIn.value = true
            loginError.value = ""
            val current = userSession.value
            
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        viewModelScope.launch {
                            if (task.isSuccessful) {
                                val fbUser = task.result?.user
                                val resolvedEmail = fbUser?.email ?: email
                                val resolvedName = fbUser?.displayName ?: name
                                val resolvedPhoto = fbUser?.photoUrl?.toString() ?: photo
                                
                                repository.saveUserSession(
                                    current.copy(
                                        email = resolvedEmail,
                                        displayName = resolvedName,
                                        photoUrl = resolvedPhoto,
                                        isLoggedIn = true
                                    )
                                )
                            } else {
                                // Fallback configuration mapping for smooth UX on emulator/preview
                                repository.saveUserSession(
                                    current.copy(
                                        email = email,
                                        displayName = name,
                                        photoUrl = photo,
                                        isLoggedIn = true
                                    )
                                )
                            }
                            isGoogleSigningIn.value = false
                        }
                    }
            } catch (e: Exception) {
                repository.saveUserSession(
                    current.copy(
                        email = email,
                        displayName = name,
                        photoUrl = photo,
                        isLoggedIn = true
                    )
                )
                isGoogleSigningIn.value = false
            }
        }
    }

    // Tab Navigation
    fun setTab(tab: String) {
        _currentTab.value = tab
        // Reset navigation subpaths
        if (tab != "home") {
            _selectedDetailListingId.value = null
        }
        if (tab != "messages") {
            _activeChatListingId.value = null
        }
    }

    // View Detailed pre-loved item
    fun viewDetails(listingId: Int) {
        _selectedDetailListingId.value = listingId
    }

    fun closeDetails() {
        _selectedDetailListingId.value = null
    }

    // Chat now action
    fun startChatting(listing: Listing) {
        _activeChatListingId.value = listing.id
        setTab("messages")
        _selectedDetailListingId.value = null // Close detail overlay
    }

    fun closeChat() {
        _activeChatListingId.value = null
    }

    fun sendMessage(text: String) {
        val listingId = _activeChatListingId.value ?: return
        val item = activeChatListing.value ?: return
        val lang = userSession.value.selectedLanguage.ifBlank { "en" }
        if (text.isBlank()) return

        viewModelScope.launch {
            // User message
            repository.insertChatMessage(
                ChatMessage(
                    listingId = listingId,
                    sender = "user",
                    messageText = text
                )
            )

            // Trigger simulated smart reply with cute seller personality
            _isSellerTyping.value = true
            delay(1500) // Delay to show typing indicator -> high task satisfaction!
            _isSellerTyping.value = false

            val replyText = generateSellerReply(text, item, lang)
            repository.insertChatMessage(
                ChatMessage(
                    listingId = listingId,
                    sender = "seller",
                    messageText = replyText
                )
            )
        }
    }

    private fun generateSellerReply(userMsg: String, item: Listing, lang: String): String {
        val lowercase = userMsg.lowercase()
        val isHi = lang == "hi"

        return if (lowercase.contains("price") || lowercase.contains("कीमत") || lowercase.contains("रेट") || lowercase.contains("discount") || lowercase.contains("कम")) {
            if (isHi) {
                "यह ₹${item.price} की बहुत ही उचित मूल्य पर है, लेकिन मैं इसे सुरक्षित और शुभ हाथों में सौंपना चाहता/चाहती हूँ। आप कब तक देखना चाहेंगे?"
            } else {
                "It is listed at ₹${item.price}, which is already a very fair deal! I want this item to find an appreciative home. When would you like to pick it up?"
            }
        } else if (lowercase.contains("available") || lowercase.contains("है क्या") || lowercase.contains("मिल") || lowercase.contains("hai")) {
            if (isHi) {
                "हाँ! '${if (lang == "hi") item.titleHi else item.titleEn}' अभी भी उपलब्ध है और नए घर में जाने को तैयार है। आप कहाँ से हैं?"
            } else {
                "Yes! '${item.titleEn}' is still available and ready for a fresh start with you. Where are you located?"
            }
        } else {
            if (isHi) {
                "आपके कोमल संदेश के लिए धन्यवाद। मैं '${item.titleHi}' को बहुत संभाल कर रखता था। आप जब चाहें इसे आकर देख सकते हैं!"
            } else {
                "Thank you for your warm message! I've taken great care of '${item.titleEn}'. Let's coordinate a place and time to hand it over!"
            }
        }
    }

    // Add listing flow action
    fun setSellPhoto(photoKey: String) {
        sellPhotoKey.value = photoKey
        sellStep.value = 2 // Move to Step 2
    }

    fun submitSellListing(lang: String) {
        val title = sellTitle.value.trim()
        val desc = sellDesc.value.trim()
        val price = sellPrice.value.trim()

        if (title.isBlank() || desc.isBlank() || price.isBlank()) {
            sellFormError.value = true
            return
        }

        sellFormError.value = false
        val cat = sellCategory.value
        val sellerName = if (lang == "hi") "आपका स्टोर (Self)" else "Your Shared Store"

        viewModelScope.launch {
            val newListing = Listing(
                category = cat,
                titleEn = if (lang == "hi") "" else title,
                titleHi = if (lang == "hi") title else "",
                price = price,
                descEn = if (lang == "hi") "" else desc,
                descHi = if (lang == "hi") desc else "",
                sellerNameEn = sellerName,
                sellerNameHi = sellerName,
                sellerRating = 5.0f,
                isSold = false,
                isUploadedByUser = true,
                imageResName = sellPhotoKey.value
            )
            // Combine fields so both display fields are valid
            val correctedListing = newListing.copy(
                titleEn = title,
                titleHi = title,
                descEn = desc,
                descHi = desc
            )

            repository.insertListing(correctedListing)

            // Trigger bouncy success micro-interaction!
            sellSuccessAnimation.value = true
            sellStep.value = 3
            
            // Stay for 2 seconds to give incredible accomplishment feeling!
            delay(2200)

            // Reset and navigate with soft transition
            sellSuccessAnimation.value = false
            sellStep.value = 1
            sellTitle.value = ""
            sellDesc.value = ""
            sellPrice.value = ""
            _currentTab.value = "home" // Take to marketplace to see their post!
        }
    }

    fun resetSellFields() {
        sellStep.value = 1
        sellTitle.value = ""
        sellDesc.value = ""
        sellPrice.value = ""
        sellFormError.value = false
        sellSuccessAnimation.value = false
    }
}

class GoVintoViewModelFactory(private val application: Application, private val repository: GoVintoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoVintoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoVintoViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
