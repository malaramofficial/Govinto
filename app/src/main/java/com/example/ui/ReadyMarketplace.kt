package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.GoVintoRepository
import com.example.data.Listing
import com.example.ui.theme.TrustBlue100
import com.example.ui.theme.TrustBlue500
import com.example.ui.theme.TrustBlue700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TrueWhite

@Composable
fun ReadyAppContent(application: android.app.Application) {
    val db = remember { AppDatabase.getDatabase(application) }
    val repository = remember { GoVintoRepository(db.goVintoDao()) }
    val vm: ReadyMarketplaceViewModel = viewModel(factory = ReadyMarketplaceViewModelFactory(application, repository))
    val session by vm.userSession.collectAsState()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            session.selectedLanguage.isBlank() -> ReadyLanguageScreen(vm)
            !session.isLoggedIn -> ReadyLoginScreen(vm)
            else -> ReadyMainScreen(vm, session)
        }
    }
}

@Composable
private fun ReadyLanguageScreen(vm: ReadyMarketplaceViewModel) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(TrustBlue700), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Storefront, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("GoVinto", fontSize = 34.sp, fontWeight = FontWeight.Black, color = TrustBlue700)
        Text("खरीदें • बेचें • दोबारा इस्तेमाल करें", color = Slate600)
        Spacer(Modifier.height(40.dp))
        Text("भाषा चुनें / Choose language", fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.chooseLanguage("hi") }, Modifier.fillMaxWidth().height(54.dp)) { Text("हिंदी") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { vm.chooseLanguage("en") }, Modifier.fillMaxWidth().height(54.dp)) { Text("English") }
    }
}

@Composable
private fun ReadyLoginScreen(vm: ReadyMarketplaceViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val hi = vm.userSession.collectAsState().value.selectedLanguage == "hi"
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("GoVinto", fontSize = 32.sp, fontWeight = FontWeight.Black, color = TrustBlue700)
        Text(if (hi) "मार्केट में शामिल हों" else "Join the marketplace", color = Slate600)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(if (hi) "आपका नाम" else "Your name") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email (optional)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(Modifier.height(18.dp))
        Button(onClick = { vm.signIn(name, email) }, enabled = name.trim().length >= 2, Modifier.fillMaxWidth().height(54.dp)) {
            Text(if (hi) "मार्केट शुरू करें" else "Enter GoVinto")
        }
        Spacer(Modifier.height(12.dp))
        Text(if (hi) "आपकी प्रोफ़ाइल इस डिवाइस पर सुरक्षित रूप से सेव होगी।" else "Your profile is saved locally on this device.", fontSize = 12.sp, color = Slate600)
    }
}

@Composable
private fun ReadyMainScreen(vm: ReadyMarketplaceViewModel, session: com.example.data.UserSession) {
    val screen by vm.screen.collectAsState()
    val detailId by vm.detailId.collectAsState()
    val detail by vm.detail.collectAsState()
    val hi = session.selectedLanguage == "hi"
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = TrueWhite) {
                NavItem(Icons.Default.Home, if (hi) "होम" else "Home", screen == "home") { vm.setScreen("home") }
                NavItem(Icons.Default.AddCircle, if (hi) "बेचें" else "Sell", screen == "sell") { vm.setScreen("sell") }
                NavItem(Icons.Default.Chat, if (hi) "चैट" else "Chats", screen == "messages") { vm.setScreen("messages") }
                NavItem(Icons.Default.Person, if (hi) "प्रोफाइल" else "Profile", screen == "profile") { vm.setScreen("profile") }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (screen) {
                "home" -> ReadyHome(vm, hi)
                "sell" -> ReadySell(vm, hi)
                "messages" -> ReadyMessages(vm, hi)
                "profile" -> ReadyProfile(vm, session, hi)
            }
            if (detailId != null && detail != null) ReadyDetail(vm, detail!!, hi)
        }
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = onClick, icon = { Icon(icon, null) }, label = { Text(label, fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = TrustBlue700, selectedTextColor = TrustBlue700, indicatorColor = TrustBlue100))
}

@Composable
private fun ReadyHome(vm: ReadyMarketplaceViewModel, hi: Boolean) {
    val search by vm.search.collectAsState()
    val category by vm.category.collectAsState()
    val items by vm.filteredListings.collectAsState()
    val cats = listOf("All", "Clothes", "Electronics", "Furniture", "Books")
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("GoVinto", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TrustBlue700); Text(if (hi) "आपके आसपास की चीज़ें" else "Things worth a second life", fontSize = 12.sp, color = Slate600) }
            Text(if (hi) "EN" else "हिन्दी", Modifier.clip(RoundedCornerShape(14.dp)).background(TrustBlue100).clickable { vm.chooseLanguage(if (hi) "en" else "hi") }.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, color = TrustBlue700)
        }
        OutlinedTextField(search, { vm.search.value = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp), placeholder = { Text(if (hi) "क्या ढूंढ रहे हैं?" else "Search items...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { if (search.isNotEmpty()) IconButton({ vm.search.value = "" }) { Icon(Icons.Default.Clear, null) } }, singleLine = true, shape = RoundedCornerShape(18.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats) { cat -> FilterChip(selected = category == cat, onClick = { vm.category.value = cat }, label = { Text(categoryLabel(cat, hi)) }) }
        }
        if (items.isEmpty()) EmptyState(hi) else LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item -> ReadyListingCard(item, hi, { vm.openDetail(item.id) }, { vm.openChat(item.id) }) }
        }
    }
}

private fun categoryLabel(c: String, hi: Boolean) = when (c) {
    "All" -> if (hi) "सभी" else "All"
    "Clothes" -> if (hi) "कपड़े" else "Clothes"
    "Electronics" -> if (hi) "इलेक्ट्रॉनिक्स" else "Electronics"
    "Furniture" -> if (hi) "फर्नीचर" else "Furniture"
    "Books" -> if (hi) "किताबें" else "Books"
    else -> c
}

@Composable
private fun EmptyState(hi: Boolean) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.SearchOff, null, Modifier.size(54.dp), tint = Slate600); Spacer(Modifier.height(10.dp)); Text(if (hi) "कोई सामान नहीं मिला" else "No items found", fontWeight = FontWeight.Bold); Text(if (hi) "दूसरा शब्द या श्रेणी आज़माएँ" else "Try another search or category", color = Slate600, fontSize = 13.sp) } }

@Composable
private fun ReadyListingCard(item: Listing, hi: Boolean, onOpen: () -> Unit, onChat: () -> Unit) {
    Card(onClick = onOpen, Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = TrueWhite), border = BorderStroke(1.dp, Slate100)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ListingImage(item, Modifier.size(104.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(categoryLabel(item.category, hi), color = TrustBlue500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(if (hi) item.titleHi else item.titleEn, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("₹${item.price}", color = TrustBlue700, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("★ ${"%.1f".format(item.sellerRating)} • ${item.sellerNameEn}", color = Slate600, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = onChat, Modifier.fillMaxWidth().height(34.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.Chat, null, Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text(if (hi) "संदेश" else "Chat", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun ListingImage(item: Listing, modifier: Modifier) {
    if (item.customImageUri.isNotBlank()) AsyncImage(model = item.customImageUri, contentDescription = item.titleEn, modifier = modifier.clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
    else Box(modifier.clip(RoundedCornerShape(16.dp)).background(categoryBg(item.category)), contentAlignment = Alignment.Center) { Text(categoryEmoji(item.category), fontSize = 40.sp) }
}

private fun categoryEmoji(c: String) = when (c) { "Clothes" -> "👕"; "Electronics" -> "💻"; "Furniture" -> "🪑"; "Books" -> "📚"; else -> "🎁" }
private fun categoryBg(c: String) = when (c) { "Clothes" -> Color(0xFFEAF2FF); "Electronics" -> Color(0xFFEFFAF5); "Furniture" -> Color(0xFFFFF4E5); "Books" -> Color(0xFFF5F0FF); else -> Slate100 }

@Composable
private fun ReadyDetail(vm: ReadyMarketplaceViewModel, item: Listing, hi: Boolean) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .45f)).clickable { vm.closeDetail() }, contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.fillMaxWidth().fillMaxHeight(.88f).clickable(enabled = false) {}, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), color = TrueWhite) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(categoryLabel(item.category, hi), color = TrustBlue700, fontWeight = FontWeight.Bold); IconButton({ vm.closeDetail() }) { Icon(Icons.Default.Close, null) } }
                ListingImage(item, Modifier.fillMaxWidth().height(210.dp))
                Spacer(Modifier.height(14.dp))
                Text(if (hi) item.titleHi else item.titleEn, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("₹${item.price}", fontSize = 27.sp, fontWeight = FontWeight.Black, color = TrustBlue700)
                Spacer(Modifier.height(12.dp))
                Text(if (hi) item.descHi else item.descEn, color = Slate600, lineHeight = 21.sp)
                Spacer(Modifier.height(12.dp))
                Text("${if (hi) "विक्रेता" else "Seller"}: ${item.sellerNameEn}  •  ★ ${"%.1f".format(item.sellerRating)}", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Button({ vm.openChat(item.id) }, Modifier.fillMaxWidth().height(54.dp)) { Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text(if (hi) "विक्रेता को संदेश भेजें" else "Message seller") }
            }
        }
    }
}

@Composable
private fun ReadySell(vm: ReadyMarketplaceViewModel, hi: Boolean) {
    var pickerOpen by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) vm.sellImageUri.value = uri.toString() }
    val uri by vm.sellImageUri.collectAsState(); val title by vm.sellTitle.collectAsState(); val price by vm.sellPrice.collectAsState(); val desc by vm.sellDescription.collectAsState(); val cat by vm.sellCategory.collectAsState(); val error by vm.sellError.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(20.dp).statusBarsPadding()) {
        Text(if (hi) "अपना सामान बेचें" else "Sell your item", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TrustBlue700)
        Text(if (hi) "साफ फोटो और सही जानकारी से जल्दी खरीदार मिलेगा।" else "Clear photos and honest details help you sell faster.", color = Slate600)
        Spacer(Modifier.height(18.dp))
        Card(onClick = { picker.launch("image/*") }, Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = TrustBlue100)) {
            if (uri.isNotBlank()) AsyncImage(uri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.AddAPhoto, null, Modifier.size(42.dp), tint = TrustBlue700); Text(if (hi) "फोटो चुनें" else "Choose photo", color = TrustBlue700, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(title, { vm.sellTitle.value = it }, Modifier.fillMaxWidth(), label = { Text(if (hi) "सामान का नाम" else "Item title") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(listOf("Clothes","Electronics","Furniture","Books")) { c -> FilterChip(cat == c, { vm.sellCategory.value = c }, label = { Text(categoryLabel(c, hi)) }) } }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(price, { vm.sellPrice.value = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(if (hi) "कीमत (₹)" else "Price (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(desc, { vm.sellDescription.value = it }, Modifier.fillMaxWidth().height(130.dp), label = { Text(if (hi) "विवरण" else "Description") }, maxLines = 5)
        if (error.isNotBlank()) { Spacer(Modifier.height(6.dp)); Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
        Button({ vm.postListing() }, Modifier.fillMaxWidth().height(54.dp), enabled = title.trim().length >= 3 && price.isNotBlank() && desc.trim().length >= 10) { Icon(Icons.Default.Publish, null); Spacer(Modifier.width(8.dp)); Text(if (hi) "लिस्टिंग प्रकाशित करें" else "Publish listing") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReadyMessages(vm: ReadyMarketplaceViewModel, hi: Boolean) {
    val id by vm.chatId.collectAsState(); val item by vm.chatListing.collectAsState(); val msgs by vm.messages.collectAsState(); var text by remember { mutableStateOf("") }
    if (id == null || item == null) {
        val all by vm.listings.collectAsState(); val conversations = all.filter { it.isUploadedByUser }
        Column(Modifier.fillMaxSize().padding(20.dp).statusBarsPadding()) { Text(if (hi) "संदेश" else "Messages", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TrustBlue700); Spacer(Modifier.height(12.dp)); Text(if (hi) "किसी सामान पर Chat दबाकर बातचीत शुरू करें।" else "Open a listing and tap Chat to start a conversation.", color = Slate600); Spacer(Modifier.height(20.dp)); if (conversations.isEmpty()) EmptyChat(hi) else conversations.forEach { ReadyListingCard(it, hi, { vm.openDetail(it.id) }, { vm.openChat(it.id) }) } }
    } else Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton({ vm.closeChat() }) { Icon(Icons.Default.ArrowBack, null) }; Text(if (hi) item!!.titleHi else item!!.titleEn, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(msgs) { m -> Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.sender == "user") Arrangement.End else Arrangement.Start) { Surface(shape = RoundedCornerShape(16.dp), color = if (m.sender == "user") TrustBlue100 else Slate100) { Text(m.messageText, Modifier.padding(12.dp), color = Slate900) } } } }
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(text, { text = it }, Modifier.weight(1f), placeholder = { Text(if (hi) "संदेश लिखें" else "Message seller") }, singleLine = true, shape = RoundedCornerShape(20.dp)); IconButton({ vm.sendMessage(text); text = "" }, enabled = text.isNotBlank()) { Icon(Icons.Default.Send, null, tint = TrustBlue700) } }
    }
}

@Composable private fun EmptyChat(hi: Boolean) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.ChatBubbleOutline, null, Modifier.size(56.dp), tint = Slate600); Text(if (hi) "अभी कोई चैट नहीं" else "No chats yet", fontWeight = FontWeight.Bold) } }

@Composable
private fun ReadyProfile(vm: ReadyMarketplaceViewModel, session: com.example.data.UserSession, hi: Boolean) {
    val all by vm.listings.collectAsState(); val mine = all.filter { it.isUploadedByUser && it.sellerNameEn == session.displayName }
    Column(Modifier.fillMaxSize().padding(20.dp).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(64.dp).clip(CircleShape).background(TrustBlue100), contentAlignment = Alignment.Center) { Text(session.displayName.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TrustBlue700) }; Spacer(Modifier.width(14.dp)); Column { Text(session.displayName, fontSize = 21.sp, fontWeight = FontWeight.Bold); if (session.email.isNotBlank()) Text(session.email, color = Slate600, fontSize = 12.sp) } }
        Spacer(Modifier.height(24.dp)); Text(if (hi) "मेरी लिस्टिंग" else "My listings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (mine.isEmpty()) Text(if (hi) "आपने अभी कुछ नहीं बेचा है।" else "You have no listings yet.", color = Slate600) else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(mine) { item -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { ListingImage(item, Modifier.size(70.dp)); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(item.titleEn, fontWeight = FontWeight.Bold); Text("₹${item.price} • ${if (item.isSold) if (hi) "बिक गया" else "Sold" else if (hi) "उपलब्ध" else "Available"}", color = Slate600, fontSize = 12.sp) } IconButton({ vm.toggleSold(item) }) { Icon(if (item.isSold) Icons.Default.Refresh else Icons.Default.Check, null) }; IconButton({ vm.deleteListing(item) }) { Icon(Icons.Default.Delete, null) } } } } }
        Spacer(Modifier.height(18.dp)); OutlinedButton({ vm.signOut() }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Logout, null); Spacer(Modifier.width(6.dp)); Text(if (hi) "लॉग आउट" else "Sign out") }
    }
}
