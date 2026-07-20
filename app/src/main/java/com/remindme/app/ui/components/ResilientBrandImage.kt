package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.remindme.app.ui.theme.Accent400
import com.remindme.app.ui.theme.Accent500

@Composable
fun ResilientBrandImage(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
) {
    var failed by remember(imageUrl) { mutableStateOf(false) }
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank() && !failed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { failed = true }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Accent500.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase().ifBlank { "R" },
                    color = Accent400,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
