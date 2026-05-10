package com.example.raitha_varta.ui.crop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raitha_varta.data.Crop

/**
 * Optimized Crop Card for Farmer Dashboard.
 * Uses local Resource Manager images and Clean Architecture data mapping.
 */
@Composable
fun CropCard(
    crop: Crop,
    isSelected: Boolean,
    onCropSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Senior UI Touch: Smooth elevation and color transitions
    val elevation by animateDpAsState(if (isSelected) 12.dp else 2.dp, label = "elev")
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF1F8E9) else Color.White,
        label = "bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clickable { onCropSelected() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF2E7D32)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Status Badge
                if (crop.isRecommended || isSelected) {
                    Surface(
                        color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFFFA000),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (isSelected) "SELECTED" else "RECOMMENDED",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Crop Image - Using local Resource Manager images only
                Image(
                    painter = painterResource(id = crop.imageRes),
                    contentDescription = crop.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = crop.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF1B5E20) else Color.Black
            )
            
            Text(
                text = "Season: ${crop.season}",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            // Minimalist Growth Stage Info
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { crop.growthProgress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF43A047),
                trackColor = Color(0xFFC8E6C9)
            )
        }
    }
}
