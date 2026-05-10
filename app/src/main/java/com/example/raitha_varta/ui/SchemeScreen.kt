package com.example.raitha_varta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raitha_varta.data.Scheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemeScreen(viewModel: SchemeViewModel) {
    val schemes by viewModel.schemes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Govt Schemes") },
                actions = {
                    IconButton(onClick = { viewModel.refreshSchemes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (schemes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schemes) { scheme ->
                    SchemeItem(scheme)
                }
            }
        }
    }
}

@Composable
fun SchemeItem(scheme: Scheme) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scheme.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scheme.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show less" else "Show more"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = scheme.description, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Eligibility", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = scheme.eligibility, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Benefits", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = scheme.benefits, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "How to Apply", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = scheme.howToApply, fontSize = 14.sp)
                }
            }
        }
    }
}
