package com.example.raitha_varta.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.raitha_varta.data.NewsArticle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    articleId: String?,
    viewModel: NewsViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val article = (uiState as? NewsUiState.Success)?.articles?.find { it.id == articleId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    article?.let { item ->
                        IconButton(onClick = { shareArticle(context, item) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { viewModel.toggleBookmark(item) }) {
                            Icon(
                                imageVector = if (item.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (item.isBookmarked) "Remove Bookmark" else "Bookmark",
                                tint = if (item.isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        article?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                item.imageUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Source: ${item.source}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = item.category,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.content,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { openBrowser(context, item.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Read Full Article")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Article not found")
            }
        }
    }
}

private fun shareArticle(context: Context, article: NewsArticle) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.id}")
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun openBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error
    }
}
