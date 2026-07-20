package com.remindme.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.ui.components.AppScaffold
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.Spinner
import com.remindme.app.ui.components.AppTextField
import com.remindme.app.ui.components.CircledBackButton
import com.remindme.app.ui.components.AppPullToRefresh
import com.remindme.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onItemClick: (String, CategoryType) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AppScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircledBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                AppTextField(
                    value = uiState.query,
                    onValueChange = viewModel::updateQuery,
                    placeholder = "Search reminders, notes, people…",
                    modifier = Modifier.weight(1f)
                )
            }

            AppPullToRefresh(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Accent500)
            }
            if (!uiState.isLoading && uiState.results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp, start = 32.dp, end = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = if (uiState.query.isEmpty()) "Search birthdays, tasks, subscriptions…" else "No results for \"${uiState.query}\"",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.results, key = { it.id }) { item ->
                        SearchResultItem(item = item, onClick = { onItemClick(item.id, item.category) })
                    }
                }
            }
            }
        }
    }
}

@Composable
fun SearchResultItem(item: ReminderItem, onClick: () -> Unit) {
    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCard(
                borderRadius = 12.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    val icon = when (item.category) {
                        CategoryType.PERSON -> Icons.Outlined.Person
                        CategoryType.SUBSCRIPTION -> Icons.Outlined.CreditCard
                        CategoryType.CUSTOM_HOLIDAY -> Icons.Outlined.CardGiftcard
                        CategoryType.TASK -> Icons.Outlined.Checklist
                    }
                    AppIcon(imageVector = icon, color = Accent500, size = 22.dp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                val catText = item.category.name.lowercase().replace("_", " ")
                Text(
                    text = catText,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (!item.notes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.notes,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }
            AppIcon(imageVector = Icons.Outlined.ChevronRight, size = 18.dp, color = TextTertiary)
        }
    }
}
