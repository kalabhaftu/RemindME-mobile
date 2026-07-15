package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.GlassBorder
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary
import com.remindme.app.ui.theme.TextTertiary

data class BottomSheetPickerItem<T>(
    val value: T,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PickerField(
    label: String,
    value: T?,
    displayValue: (T) -> String,
    title: String,
    items: List<BottomSheetPickerItem<T>>,
    onChanged: (T) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    // FloatingGlassContainer representing the selected field
    FloatingGlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { showSheet = true },
        borderRadius = 16.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (value != null) displayValue(value) else "",
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }
            LiquidIcon(
                imageVector = Icons.Default.KeyboardArrowDown,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent, // We use Glass inside
            dragHandle = null
        ) {
            FloatingGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp), // Safe area bottom
                borderRadius = 16.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    HorizontalDivider(color = GlassBorder)
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        items(items) { item ->
                            val selected = item.value == value
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onChanged(item.value)
                                        showSheet = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    modifier = Modifier.weight(1f),
                                    color = if (selected) Accent500 else TextPrimary,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                if (selected) {
                                    LiquidIcon(
                                        imageVector = Icons.Default.Check,
                                        tint = Accent500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
