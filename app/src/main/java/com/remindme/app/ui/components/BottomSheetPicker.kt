package com.remindme.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.components.liquid.FloatingGlassContainer
import com.remindme.app.ui.components.liquid.LiquidIcon
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.TextPrimary

@Composable
fun <T> BottomSheetPicker(
    title: String,
    items: List<T>,
    initialSelection: T,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    itemLabel: (T) -> String = { it.toString() }
) {
    val isVisible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible.value = true
    }

    fun dismiss() {
        isVisible.value = false
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable { dismiss() }
    ) {
        AnimatedVisibility(
            visible = isVisible.value,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 200)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingGlassContainer(
                borderRadius = 28.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp)
                    .clickable(enabled = false) {} // Prevent click-through
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        items(items) { item ->
                            val isSelected = item == initialSelection
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(item)
                                        dismiss()
                                    }
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = itemLabel(item),
                                    fontSize = 16.sp,
                                    color = if (isSelected) Accent500 else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    LiquidIcon(
                                        imageVector = Icons.Rounded.Check,
                                        color = Accent500,
                                        size = 20.dp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
