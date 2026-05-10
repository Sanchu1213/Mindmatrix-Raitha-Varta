package com.example.raitha_varta.ui.community

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerCommunityScreen(
    languageCode: String = "en",
    onToggleLanguage: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()
    val isKn = languageCode == "kn"
    var showNewPost by remember { mutableStateOf(false) }

    if (selectedPost != null) {
        PostDetailScreen(post = selectedPost!!, isKn = isKn,
            onBack = { viewModel.clearSelectedPost() },
            onReply = { text, author -> viewModel.addReply(selectedPost!!.id, text, author) },
            onToggleLanguage = onToggleLanguage)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(if (isKn) "ರೈತ ಸಮುದಾಯ" else "Farmer Community",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4E342E), titleContentColor = Color.White,
                    navigationIconContentColor = Color.White),
                actions = {
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = if (isKn) "ಭಾಷೆ" else "Language", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewPost = true },
                containerColor = Color(0xFF4E342E),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(if (isKn) "ಪ್ರಶ್ನೆ ಕೇಳಿ" else "Ask Question") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFFBF5F0)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Banner
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color(0xFF4E342E))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint = Color(0xFFFFCC80), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(if (isKn) "ರೈತ ಸಮುದಾಯ" else "Farmer Community",
                                fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(if (isKn) "ಪ್ರಶ್ನೆ ಕೇಳಿ, ಫೋಟೋ ಹಂಚಿಕೊಳ್ಳಿ, ತಜ್ಞರ ಸಲಹೆ ಪಡೆಯಿರಿ"
                                else "Ask questions, share photos, get expert advice from fellow farmers",
                                color = Color.White.copy(0.8f), fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(Icons.Default.People, "${posts.size * 47 + 120}", if (isKn) "ರೈತರು" else "Farmers")
                    StatPill(Icons.Default.Forum, "${posts.size}", if (isKn) "ಪ್ರಶ್ನೆಗಳು" else "Questions")
                    StatPill(Icons.Default.Star, "${posts.sumOf { it.likes }}", if (isKn) "ಸಹಾಯ" else "Helped")
                }
            }

            items(posts, key = { it.id }) { post ->
                PostCard(post = post, isKn = isKn,
                    onLike = { viewModel.toggleLike(post.id) },
                    onClick = { viewModel.selectPost(post) })
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showNewPost) {
        NewPostSheet(isKn = isKn, onDismiss = { showNewPost = false },
            onPost = { q, name, loc, tag, img ->
                viewModel.addPost(q, name, loc, tag, img)
                showNewPost = false
            })
    }
}

@Composable
private fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFEFEBE9)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = Color(0xFF6D4C41), modifier = Modifier.size(13.dp))
            Text("$value $label", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostCard(post: CommunityPost, isKn: Boolean, onLike: () -> Unit, onClick: () -> Unit) {
    val tagColor = when (post.tag) {
        "Disease" -> Color(0xFFD32F2F); "Seeds" -> Color(0xFF388E3C); "Soil" -> Color(0xFF795548)
        "Fertilizer" -> Color(0xFF1976D2); else -> Color(0xFF7B1FA2)
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(tagColor.copy(0.15f), CircleShape), Alignment.Center) {
                    Text(post.authorName.take(1).uppercase(), fontWeight = FontWeight.Black,
                        color = tagColor, fontSize = 15.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A1A1A))
                    Text(localizedCommunityText(post.authorLocation, isKn), fontSize = 11.sp, color = Color(0xFF455A64))
                }
                Surface(color = tagColor.copy(0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(localizedTag(post.tag, isKn), Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = tagColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(localizedCommunityText(post.question, isKn), fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, color = Color(0xFF1A1A1A))

            // Show user-uploaded Bitmap image
            if (post.image != null) {
                Spacer(Modifier.height(8.dp))
                Image(
                    bitmap = post.image.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            // Show URL image with error handling (no blank space on failure)
            val imageUrl = post.imageUrl?.takeIf { it.isNotBlank() }
            if (imageUrl != null) {
                var loadSuccess by remember(imageUrl) { mutableStateOf(true) }
                if (loadSuccess) {
                    Spacer(Modifier.height(8.dp))
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEEEEE)), Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp),
                                    color = Color(0xFF4E342E), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            LaunchedEffect(Unit) { loadSuccess = false }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Like button
                IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                    Icon(if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null, tint = if (post.isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp))
                }
                Text("${post.likes}", fontSize = 12.sp, color = Color(0xFF455A64))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.Forum, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.replies.size} ${if (isKn) "ಉತ್ತರ" else "replies"}",
                    fontSize = 12.sp, color = Color(0xFF1976D2))
                Spacer(Modifier.weight(1f))
                Text(SimpleDateFormat(if (isKn) "dd MMM" else "dd MMM", Locale(if (isKn) "kn" else "en", "IN")).format(Date(post.timestamp)),
                    fontSize = 11.sp, color = Color(0xFF90A4AE))
            }
        }
    }
}

private fun localizedCommunityText(text: String, isKn: Boolean): String {
    val parts = text.split("|||")
    if (parts.size < 2) return text
    return if (isKn) parts[1].trim() else parts[0].trim()
}

private fun localizedTag(tag: String, isKn: Boolean): String {
    if (!isKn) return tag
    return when (tag) {
        "General" -> "ಸಾಮಾನ್ಯ"
        "Disease" -> "ರೋಗ"
        "Seeds" -> "ಬೀಜ"
        "Soil" -> "ಮಣ್ಣು"
        "Fertilizer" -> "ಗೊಬ್ಬರ"
        "Sowing" -> "ಬಿತ್ತನೆ"
        "Market" -> "ಮಾರುಕಟ್ಟೆ"
        else -> tag
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetailScreen(
    post: CommunityPost, isKn: Boolean, onBack: () -> Unit,
    onReply: (String, String) -> Unit,
    onToggleLanguage: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val authorName = if (isKn) "ನೀವು" else "You"

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0),
                title = { Text(if (isKn) "ವಿವರ" else "Post Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = if (isKn) "ಭಾಷೆ" else "Language", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4E342E),
                    titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = replyText, onValueChange = { replyText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(if (isKn) "ಉತ್ತರ ಬರೆಯಿರಿ..." else "Write a reply...") },
                        maxLines = 3, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (replyText.isNotBlank()) { onReply(replyText, authorName); replyText = "" }
                    }, modifier = Modifier.size(44.dp).background(Color(0xFF4E342E), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFFBF5F0)),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(Color(0xFF4E342E).copy(0.15f), CircleShape), Alignment.Center) {
                                Text(post.authorName.take(1), fontWeight = FontWeight.Black,
                                    color = Color(0xFF4E342E), fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                                Text(localizedCommunityText(post.authorLocation, isKn), fontSize = 12.sp, color = Color(0xFF455A64))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(localizedCommunityText(post.question, isKn), fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF1A1A1A))
                        // Show Bitmap image (user-uploaded)
                        if (post.image != null) {
                            Spacer(Modifier.height(10.dp))
                            Image(
                                bitmap = post.image.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Show URL image with error handling
                        val imageUrl = post.imageUrl?.takeIf { it.isNotBlank() }
                        if (imageUrl != null) {
                            var loadSuccess by remember(imageUrl) { mutableStateOf(true) }
                            if (loadSuccess) {
                                Spacer(Modifier.height(10.dp))
                                SubcomposeAsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFEEEEEE)), Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(28.dp),
                                                color = Color(0xFF4E342E), strokeWidth = 2.dp)
                                        }
                                    },
                                    error = {
                                        LaunchedEffect(Unit) { loadSuccess = false }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (post.replies.isNotEmpty()) {
                item { Text(if (isKn) "ಉತ್ತರಗಳು (${post.replies.size})" else "Replies (${post.replies.size})",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4E342E)) }

                items(post.replies) { reply ->
                    val expertColor = if (reply.isExpert) Color(0xFF1B5E20) else Color(0xFF37474F)
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(if (reply.isExpert) Color(0xFFE8F5E9) else Color.White),
                        border = if (reply.isExpert) BorderStroke(1.dp, Color(0xFF81C784)) else null) {
                        Row(Modifier.padding(12.dp)) {
                            Box(Modifier.size(34.dp).background(expertColor.copy(0.15f), CircleShape), Alignment.Center) {
                                Icon(if (reply.isExpert) Icons.Default.Verified else Icons.Default.Person,
                                    null, tint = expertColor, modifier = Modifier.size(17.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(reply.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = expertColor)
                                    if (reply.isExpert) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(4.dp)) {
                                            Text(if (isKn) "ತಜ್ಞರು" else "EXPERT", Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(localizedCommunityText(reply.text, isKn), fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF1A1A1A))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewPostSheet(isKn: Boolean, onDismiss: () -> Unit, onPost: (String, String, String, String, Bitmap?) -> Unit) {
    var question by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("General") }
    var image by remember { mutableStateOf<Bitmap?>(null) }
    var tagExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tags = listOf("General", "Disease", "Seeds", "Soil", "Fertilizer", "Sowing", "Market")

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            image = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val src = ImageDecoder.createSource(context.contentResolver, it); ImageDecoder.decodeBitmap(src)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isKn) "ಪ್ರಶ್ನೆ ಕೇಳಿ" else "Ask the Community",
            fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(if (isKn) "ನಿಮ್ಮ ಹೆಸರು" else "Your Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = location, onValueChange = { location = it },
                    label = { Text(if (isKn) "ಜಿಲ್ಲೆ" else "District") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
                ExposedDropdownMenuBox(expanded = tagExpanded, onExpandedChange = { tagExpanded = !tagExpanded }) {
                    OutlinedTextField(value = tag, onValueChange = {}, readOnly = true,
                        label = { Text(if (isKn) "ವಿಷಯ" else "Topic") },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tagExpanded) },
                        shape = RoundedCornerShape(10.dp))
                    ExposedDropdownMenu(tagExpanded, { tagExpanded = false }) {
                        tags.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { tag = t; tagExpanded = false }) }
                    }
                }
                OutlinedTextField(value = question, onValueChange = { question = it },
                    label = { Text(if (isKn) "ನಿಮ್ಮ ಪ್ರಶ್ನೆ *" else "Your Question *") },
                    modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(10.dp))
                if (image != null) {
                    Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))) {
                        Image(image!!.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        IconButton(onClick = { image = null }, Modifier.align(Alignment.TopEnd).size(28.dp)
                            .background(Color.Black.copy(0.5f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isKn) "ಫೋಟೋ ಸೇರಿಸಿ" else "Add Photo (optional)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (question.isNotBlank() && name.isNotBlank()) onPost(question, name, location, tag, image) },
                colors = ButtonDefaults.buttonColors(Color(0xFF4E342E))) {
                Text(if (isKn) "ಪೋಸ್ಟ್ ಮಾಡಿ" else "Post")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(if (isKn) "ರದ್ದು" else "Cancel") } }
    )
}
