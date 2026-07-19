package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.remindme.app.ui.theme.BgElevated
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.AppColors

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleWidget: @Composable (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottom: @Composable (() -> Unit)? = null,
    statusBarsPadding: Boolean = true
) {
    val isLight = !isSystemInDarkTheme()
    val glassStyle = LocalThemeStyle.current
    val bgColor = if (glassStyle == ThemeStyle.Solid) {
        if (isLight) Color(0xFFF2F2F7) else BgElevated
    } else if (isLight) {
        Color.White.copy(alpha = 0.62f)
    } else {
        Color.White.copy(alpha = 0.28f)
    }

    Column(
        modifier = modifier
            .run { if (statusBarsPadding) statusBarsPadding() else this }
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(12.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (titleWidget != null) {
                    titleWidget()
                } else if (title != null) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
            }

            if (actions != null) {
                Spacer(modifier = Modifier.width(8.dp))
                actions()
            }
        }

        if (bottom != null) {
            Spacer(modifier = Modifier.height(8.dp))
            bottom()
        }
    }
}
