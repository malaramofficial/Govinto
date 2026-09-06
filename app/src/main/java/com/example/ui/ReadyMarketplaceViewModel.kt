package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.GoVintoRepository
import com.example.data.Listing
import com.example.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadyMarketplaceViewModel(application: Application, private val repository: GoVintoRepository) : AndroidViewModel(application) {
    val userSession: StateFlow<UserSession> = repository.userSession.map { it ?: UserSession() }.stateIn(viewModelScope, SharingStarted.Eagerly, UserSession())
    val listings: StateFlow<List<Listing>> = repository.allListings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val search = MutableStateFlow("")
    val category = MutableStateFlow("All")
    private val _screen = MutableStateFlow("home")
    val screen: StateFlow<String> = _screen.asStateFlow()
    private val _detailId = MutableStateFlow<Int?>(null)
    val detailId: StateFlow<Int?> = _detailId.asStateFlow()
    val detail: StateFlow<Listing?> = _detailId.flatMapLatest { id -> if (id == null) flowOf(null) else repository.getListingById(id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _chatId = MutableStateFlow<Int?>(null)
    val chatId: StateFlow<Int?> = _chatId.asStateFlow()
    val chatListing: StateFlow<Listing?> = _chatId.flatMapLatest { id -> if (id == null) flowOf(null) else repository.getListingById(id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val messages: StateFlow<List<ChatMessage>> = _chatId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.getChatMessages(id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val typing: StateFlow<Boolean> = MutableStateFlow(false)
    val filteredListings: StateFlow<List<Listing>> = combine(listings, search, category) { all, q, cat -> all.filter { item -> (cat == "All" || item.category == cat) && (q.isBlank() || listOf(item.titleEn, item.titleHi, item.descEn, item.descHi).any { it.contains(q, true) }) } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sellTitle = MutableStateFlow("")
    val sellPrice = MutableStateFlow("")
    val sellDescription = MutableStateFlow("")
    val sellCategory = MutableStateFlow("Clothes")
    val sellImageUri = MutableStateFlow("")
    val sellError = MutableStateFlow("")

    init { viewModelScope.launch { repository.prepopulateIfEmpty() } }
    fun chooseLanguage(lang: String) = viewModelScope.launch { repository.saveUserSession(userSession.value.copy(selectedLanguage = lang)) }
    fun signIn(name: String, email: String) = viewModelScope.launch { repository.saveUserSession(userSession.value.copy(displayName = name.trim().ifBlank { "GoVinto User" }, email = email.trim(), isLoggedIn = true)) }
    fun signOut() = viewModelScope.launch { try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}; repository.saveUserSession(UserSession()); _screen.value = "home"; _detailId.value = null; _chatId.value = null }
    fun setScreen(value: String) { _screen.value = value; if (value != "home") _detailId.value = null; if (value != "messages") _chatId.value = null }
    fun openDetail(id: Int) { _detailId.value = id }
    fun closeDetail() { _detailId.value = null }
    fun openChat(id: Int) { _chatId.value = id; _screen.value = "messages"; _detailId.value = null }
    fun closeChat() { _chatId.value = null }
    fun sendMessage(text: String) { val id = _chatId.value ?: return; if (text.isBlank()) return; viewModelScope.launch { repository.insertChatMessage(ChatMessage(listingId = id, sender = "user", messageText = text.trim())) } }
    fun postListing() {
        val title = sellTitle.value.trim(); val price = sellPrice.value.trim(); val desc = sellDescription.value.trim()
        if (title.length < 3) { sellError.value = "कृपया सही उत्पाद नाम लिखें।"; return }
        if (price.toLongOrNull()?.let { it > 0 } != true) { sellError.value = "कृपया सही कीमत दर्ज करें।"; return }
        if (desc.length < 10) { sellError.value = "विवरण कम से कम 10 अक्षरों का रखें।"; return }
        viewModelScope.launch {
            val name = userSession.value.displayName.ifBlank { "GoVinto User" }
            repository.insertListing(Listing(category=sellCategory.value, titleEn=title, titleHi=title, price=price, descEn=desc, descHi=desc, sellerNameEn=name, sellerNameHi=name, sellerRating=5f, isUploadedByUser=true, customImageUri=sellImageUri.value))
            sellTitle.value=""; sellPrice.value=""; sellDescription.value=""; sellImageUri.value=""; sellError.value=""; _screen.value="home"
        }
    }
    fun deleteListing(item: Listing) = viewModelScope.launch { repository.deleteListing(item.id) }
    fun toggleSold(item: Listing) = viewModelScope.launch { repository.insertListing(item.copy(isSold = !item.isSold)) }
}

class ReadyMarketplaceViewModelFactory(private val application: Application, private val repository: GoVintoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadyMarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ReadyMarketplaceViewModel(application, repository) as T
        }
        error("Unknown ViewModel class")
    }
}
