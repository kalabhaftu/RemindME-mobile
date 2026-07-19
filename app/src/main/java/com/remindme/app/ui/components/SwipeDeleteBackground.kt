package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.StateDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDeleteBackground(
    dismissState: SwipeToDismissBoxState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    bottomPadding: Dp = 8.dp,
    endPadding: Dp = 20.dp,
) {
    val isSettled = dismissState.currentValue == SwipeToDismissBoxValue.Settled &&
            dismissState.targetValue == SwipeToDismissBoxValue.Settled

    val bgColor = if (isSettled) Color.Transparent else StateDanger
    val iconColor = if (isSettled) Color.Transparent else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .padding(end = endPadding),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = iconColor
        )
    }
}