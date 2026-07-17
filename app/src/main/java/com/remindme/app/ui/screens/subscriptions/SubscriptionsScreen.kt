package com.remindme.app.ui.screens.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.remindme.app.R
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.ui.components.EmptyState
import com.remindme.app.ui.components.liquid.FloatingGlassContainer
import com.remindme.app.ui.components.liquid.LiquidIcon
import com.remindme.app.ui.components.liquid.LiquidSpinner
import com.remindme.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionsViewModel = viewModel(),
    onNavigateToEdit: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val subscriptions by viewModel.sortedSubscriptions.collectAsState()
    val haptic = LocalHapticFeedback.current

    if (uiState.isLoading && subscriptions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LiquidSpinner()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 140.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
    ) {
        if (subscriptions.isEmpty() && !uiState.isLoading) {
            item {
                Box(modifier = Modifier.padding(top = 120.dp)) {
                    EmptyState(
                        iconRes = R.drawable.empty_subscriptions,
                        message = "No subscriptions yet. Tap + to add one."
                    )
                }
            }
        } else {
            items(subscriptions, key = { it.id }) { sub ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteSubscription(sub.id)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(StateDanger)
                                .padding(end = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    },
                    content = {
                        SubscriptionRow(item = sub, onClick = { onNavigateToEdit(sub.id) })
                    }
                )
            }
        }
    }
}

@Composable
fun SubscriptionRow(item: ReminderItem, onClick: () -> Unit) {
    val sub = item.subscription
    val renewalStr = sub?.renewalDate
    val logo = sub?.logoUrl
    val amount = sub?.billingAmount?.toString()
    val currency = sub?.billingCurrency ?: "USD"
    val cycle = sub?.cycle ?: "monthly"
    
    fun daysUntil(rdStr: String?): Int {
        if (rdStr.isNullOrBlank()) return 0
        return try {
            val rd = LocalDate.parse(rdStr)
            val today = LocalDate.now()
            var next = LocalDate.of(today.year, rd.monthValue, rd.dayOfMonth)
            if (next.isBefore(today)) {
                next = LocalDate.of(today.year + 1, rd.monthValue, rd.dayOfMonth)
            }
            ChronoUnit.DAYS.between(today, next).toInt()
        } catch (e: DateTimeParseException) {
            0
        }
    }

    val days = daysUntil(renewalStr)

    FloatingGlassContainer(
        borderRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (!logo.isNullOrBlank()) {
                    AsyncImage(
                        model = logo,
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.ic_launcher_foreground) // Placeholder
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgSurface3),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💳", fontSize = 22.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val subtitle = buildString {
                    if (!renewalStr.isNullOrBlank()) {
                        try {
                            val rd = LocalDate.parse(renewalStr)
                            val formatter = DateTimeFormatter.ofPattern("MMM d")
                            append("Renews ${rd.format(formatter)} · ")
                        } catch (e: Exception) {}
                    }
                    append(cycle)
                    if (amount != null) {
                        append(" · $currency $amount")
                    }
                }
                
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgSurface3)
            ) {
                Text(
                    text = "$days d",
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
