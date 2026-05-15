package com.example.raitha_varta.ui.expert

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import coil.compose.AsyncImage

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.raitha_varta.data.ChatMessage
import com.example.raitha_varta.data.Consultation
import com.example.raitha_varta.ui.appStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpertAskScreen(
    languageCode: String = "en",
    onToggleLanguage: () -> Unit,
    onNavigateToWeather: (() -> Unit)? = null,
    onNavigateToExpert: (() -> Unit)? = null,
    onNavigateToCrops: (() -> Unit)? = null,
    onNavigateToRecommend: (() -> Unit)? = null,
    onNavigateToPrediction: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    viewModel: ExpertViewModel = hiltViewModel()
) {
    var isChatActive by remember { mutableStateOf(false) }
    val consultations by viewModel.consultations.collectAsState(initial = emptyList<com.example.raitha_varta.data.Consultation>())

    AnimatedContent(targetState = isChatActive, label = "screen_transition") { active ->
        if (active) {
            ChatInterface(
                viewModel = viewModel,
                languageCode = languageCode,
                onToggleLanguage = onToggleLanguage,
                onNavigateToWeather = onNavigateToWeather,
                onNavigateToExpert = onNavigateToExpert,
                onNavigateToCrops = onNavigateToCrops,
                onNavigateToRecommend = onNavigateToRecommend,
                onNavigateToPrediction = onNavigateToPrediction,
                onBack = { isChatActive = false }
            )
        } else {
            ExpertLandingPage(
                languageCode = languageCode,
                onToggleLanguage = onToggleLanguage,
                onBack = onBack,
                consultations = consultations,
                onStartNew = { image ->
                    viewModel.startNewConsultation(image, languageCode)
                    isChatActive = true
                },
                onDelete = { id -> viewModel.deleteConsultation(id) },
                onRename = { id, newTitle -> viewModel.renameConsultation(id, newTitle) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertLandingPage(
    languageCode: String,
    onToggleLanguage: () -> Unit,
    onBack: (() -> Unit)? = null,
    consultations: List<Consultation>,
    onStartNew: (Bitmap?) -> Unit,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit
) {
    val context = LocalContext.current
    val appText = remember(languageCode) { appStrings(languageCode) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) onStartNew(bitmap)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to analyze crops.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(appText.expertTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = "Language")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color(0xFF1A1A1A),
                    actionIconContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDFBF6))
        ) {
            // Background Watermark Logo
            AsyncImage(
                model = com.example.raitha_varta.R.drawable.logo,
                contentDescription = null,
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center)
                    .graphicsLayer(alpha = 0.04f),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = appText.expertLandingTitle,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF004D40)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = appText.expertLandingSubtitle,
                        color = Color(0xFF00695C),
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tip Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF176).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFFFB300), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = appText.expertTip,
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // History Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = appText.expertPrevQueries, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            if (consultations.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(appText.expertNoHistory, color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(consultations) { consultation ->
                        ConsultationItem(
                            consultation = consultation,
                            onDelete = { onDelete(consultation.id) },
                            onRename = { newTitle -> onRename(consultation.id, newTitle) }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun ConsultationItem(consultation: Consultation, onDelete: () -> Unit, onRename: (String) -> Unit) {
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(consultation.timestamp))
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(consultation.lastMessage) { mutableStateOf(consultation.lastMessage) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Consultation", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Consultation Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        onRename(renameText)
                        showRenameDialog = false
                    }
                }) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = consultation.lastMessage,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
                Text(text = date, color = Color.Gray, fontSize = 12.sp)
            }
            IconButton(onClick = { showRenameDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
            }
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = consultation.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInterface(
    viewModel: ExpertViewModel,
    languageCode: String,
    onToggleLanguage: () -> Unit,
    onNavigateToWeather: (() -> Unit)? = null,
    onNavigateToExpert: (() -> Unit)? = null,
    onNavigateToCrops: (() -> Unit)? = null,
    onNavigateToRecommend: (() -> Unit)? = null,
    onNavigateToPrediction: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.sendMessage(inputText, bitmap, languageCode)
            inputText = ""
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to analyze crops.", Toast.LENGTH_LONG).show()
        }
    }
    
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Expert Ask", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = "Language")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(Icons.Default.Add, "Add photo", tint = Color(0xFF2E7D32))
                    }
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(if (languageCode == "kn") "ಪ್ರಶ್ನೆ ಕೇಳಿ..." else "Ask a question...") },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText, null, languageCode)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = if (inputText.isNotBlank()) Color(0xFF2E7D32) else Color.Gray)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (message.isFromUser) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            message.image?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            val clipboardManager = LocalClipboardManager.current
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (message.isFromUser) message.text else localizedChatText(message.text, languageCode == "kn"),
                                    color = if (message.isFromUser) Color.White else Color(0xFF1A1A1A),
                                    modifier = Modifier.weight(1f)
                                )
                                if (!message.isFromUser) {
                                    val textToCopy = localizedChatText(message.text, languageCode == "kn")
                                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(textToCopy)) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.ContentCopy, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

private fun localizedChatText(text: String, isKn: Boolean): String {
    val parts = text.split("|||")
    if (parts.size < 2) return text
    return if (isKn) parts[1].trim() else parts[0].trim()
}
