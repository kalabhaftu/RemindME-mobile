package com.remindme.app.ui.screens.holidays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.PremiumIcons
import com.remindme.app.ui.components.Spinner
import com.remindme.app.ui.components.SwipeDeleteBackground
import com.remindme.app.ui.components.appScrimColor
import com.remindme.app.ui.components.appSurfaceColor
import com.remindme.app.ui.components.AppPullToRefresh
import com.remindme.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidaysScreen(
    viewModel: HolidaysViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showCountryPicker by remember { mutableStateOf(false) }

    AppPullToRefresh(
        isRefreshing = uiState.isLoadingCountries || uiState.isLoadingHolidays,
        onRefresh = { viewModel.refresh() }
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 140.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
    ) {

        if (uiState.subscribedKeys.isNotEmpty()) {
            items(uiState.subscribedKeys.toList(), key = { it }) { key ->
                val parts = key.split("-")
                val name = if (parts.size > 2) parts.subList(2, parts.size).joinToString("-") else key

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.removeSubscribed(key)
                            true
                        } else {
                            false
                        }
                    }
                )
                val isSwiping = dismissState.currentValue != SwipeToDismissBoxValue.Settled ||
                    dismissState.targetValue != SwipeToDismissBoxValue.Settled

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        SwipeDeleteBackground(
                            dismissState = dismissState,
                            cornerRadius = 8.dp,
                            bottomPadding = 8.dp,
                            endPadding = 16.dp
                        )
                    },
                    content = {
                        AppCard(
                            borderRadius = 12.dp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(iconRes = PremiumIcons.CardGiftcard, size = 16.dp, color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                AppIcon(iconRes = PremiumIcons.ArrowBack, size = 14.dp, color = TextPrimary)
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            val selectedName = uiState.countries.find { it.countryCode == uiState.selectedCountry }?.name ?: uiState.selectedCountry
            AppCard(
                borderRadius = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCountryPicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppCard(
                        borderRadius = 10.dp,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            AppIcon(iconRes = PremiumIcons.Public, size = 18.dp, color = Accent500)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Country", fontSize = 11.sp, color = TextTertiary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = selectedName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                    if (uiState.isLoadingCountries) {
                        Spinner(modifier = Modifier.size(18.dp))
                    }
                    AppIcon(iconRes = PremiumIcons.KeyboardArrowDown, size = 20.dp, color = TextTertiary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoadingHolidays) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Spinner()
                }
            }
        } else {
            items(uiState.holidays, key = { it.holidayKey }) { holiday ->
                val active = uiState.subscribedKeys.contains(holiday.holidayKey)
                AppCard(
                    borderRadius = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.toggleHoliday(holiday) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppCard(
                            borderRadius = 10.dp,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                AppIcon(
                                    iconRes = PremiumIcons.CardGiftcard,
                                    size = 18.dp,
                                    color = if (active) Accent500 else TextTertiary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = holiday.localName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            val date = try {
                                LocalDate.parse(holiday.date).format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
                            } catch (e: Exception) {
                                holiday.date
                            }
                            Text(text = date, fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (uiState.togglingKey == holiday.holidayKey) {
                            Spinner(modifier = Modifier.size(24.dp))
                        } else {
                            AppIcon(
                                iconRes = if (active) PremiumIcons.CheckCircle else PremiumIcons.AddCircle,
                                color = if (active) Accent500 else TextTertiary,
                                size = 24.dp
                            )
                        }
                    }
                }
            }
        }
    }
    }

    if (showCountryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false },
            containerColor = appSurfaceColor(elevated = true),
            scrimColor = appScrimColor(),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderSubtle)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Text(
                    text = "Select Country",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp, start = 20.dp, top = 20.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
                ) {
                    items(uiState.countries, key = { it.countryCode }) { c ->
                        val isSelected = c.countryCode == uiState.selectedCountry
                        AppCard(
                            borderRadius = 14.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    viewModel.selectCountry(c.countryCode)
                                    showCountryPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = c.name,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    AppIcon(iconRes = PremiumIcons.Check, size = 18.dp, color = Accent500)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
