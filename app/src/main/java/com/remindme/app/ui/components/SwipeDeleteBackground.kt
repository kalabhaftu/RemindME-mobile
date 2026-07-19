package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.StateDanger

@Composable
fun SwipeDeleteBackground(
    visible: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    bottomPadding: Dp = 8.dp,
    endPadding: Dp = 20.dp,
) {
    if (!visible) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
            .clip(RoundedCornerShape(cornerRadius))
            .background(StateDanger)
            .padding(end = endPadding),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White
        )
    }
}