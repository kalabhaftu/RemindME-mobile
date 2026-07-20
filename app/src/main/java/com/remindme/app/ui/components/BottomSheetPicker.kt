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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.LocalThemeStyle
import com.remindme.app.ui.components.ThemeStyle
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary
import com.remindme.app.ui.theme.TextTertiary

data class BottomSheetPickerItem(val key: String, val label: String)

@Composable
fun PickerField(
    label: String,
    value: String,
    displayValue: (String) -> String,
    title: String,
    items: List<BottomSheetPickerItem>,
    onChanged: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = displayValue(value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
        AppIcon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            color = TextTertiary,
            size = 20.dp
        )
    }

    if (showPicker) {
        BottomSheetPicker(
            title = title,
            items = items,
            initialSelection = items.find { it.key == value.toString() } ?: items.first(),
            onDismiss = { showPicker = false },
            onSelect = { selected ->
                onChanged(selected.key)
                showPicker = false
            },
            itemLabel = { it.label }
        )
    }
}

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

    Popup(
        onDismissRequest = { dismiss() },
        properties = PopupProperties(
            focusable = true,
            excludeFromSystemGesture = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appScrimColor())
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
                AppCard(
                    borderRadius = 28.dp,
                    elevated = true,
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
                                        AppIcon(
                                            imageVector = Icons.Outlined.Check,
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
}
