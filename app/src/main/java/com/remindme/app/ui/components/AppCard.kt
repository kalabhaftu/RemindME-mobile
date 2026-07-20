package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.BgElevated

/**
 * @param elevated  When true, uses a higher-opacity glass background for popup/sheet surfaces
 *                  (SelectedDaySheet, QuickAddSheet, FAB, PopupMenu). Standard cards stay subtle.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 28.dp,
    padding: Dp = 0.dp,
    tintColor: Color? = null,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    val bgColor = tintColor ?: appSurfaceColor(elevated)

    val shape = RoundedCornerShape(borderRadius)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor)
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
