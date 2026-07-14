package com.example.remindme_mobile.ui.components.liquid

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindme_mobile.ui.theme.AppColors
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import com.kyant.shapes.Squircle

@Composable
fun LiquidAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleWidget: @Composable (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottom: @Composable (() -> Unit)? = null,
    backdrop: Backdrop = LocalBackdrop.current
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8f.dp, start = 16f.dp, end = 16f.dp, bottom = 8f.dp)
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Squircle(1f) }, // Squircle(1f) is a super-ellipse/rounded rect style, or we can use Capsule() for fully rounded
                effects = {
                    vibrancy()
                    blur(2f.dp.toPx())
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                }
            )
            .padding(horizontal = 16f.dp, vertical = 12f.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(12f.dp))
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
                        fontSize = 20f.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
            }

            if (actions != null) {
                Spacer(modifier = Modifier.width(8f.dp))
                actions()
            }
        }

        if (bottom != null) {
            Spacer(modifier = Modifier.height(8f.dp))
            bottom()
        }
    }
}
