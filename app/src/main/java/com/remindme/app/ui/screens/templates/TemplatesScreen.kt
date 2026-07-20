package com.remindme.app.ui.screens.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.BottomSheetPicker
import com.remindme.app.ui.components.*
import com.remindme.app.ui.components.SnackbarHost
import com.remindme.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel = viewModel(),
    onApplyTemplate: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        appBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircledBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                TopBar(
                    title = "Templates",
                    statusBarsPadding = false,
                    modifier = Modifier.weight(1f),
                    actions = {
                        IconButton(onClick = { showCreate = true }) {
                            AppIcon(imageVector = Icons.Outlined.Add, color = TextPrimary)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AppPullToRefresh(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.loadTemplates() }
            ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                if (!uiState.isLoading && uiState.templates.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.ViewList, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No templates yet", color = TextSecondary, fontSize = 16.sp)
                        }
                    }
                }
                items(uiState.templates, key = { it.id }) { template ->
                    TemplateItem(
                        template = template,
                        onApply = { onApplyTemplate(template.category) },
                        onDelete = { viewModel.deleteTemplate(template.id) }
                    )
                }
            }
            }
        }
    }

        if (showCreate) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        appScrimColor()
                    )
                    .clickable { showCreate = false }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {}
                ) {
                    CreateTemplateSheet(
                        onClose = { showCreate = false },
                        onCreate = { name, cat, notes ->
                            viewModel.createTemplate(name, cat, notes)
                            showCreate = false
                        }
                    )
                }
            }
        }
    }

@Composable
fun TemplateItem(template: ReminderTemplate, onApply: () -> Unit, onDelete: () -> Unit) {
    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable(onClick = onApply)
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
                    val icon = when (template.category) {
                        "person" -> Icons.Outlined.Person
                        "subscription" -> Icons.Outlined.CreditCard
                        "custom_holiday" -> Icons.Outlined.CardGiftcard
                        else -> Icons.Outlined.Checklist
                    }
                    AppIcon(imageVector = icon, color = Accent500, size = 22.dp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                val freq = template.recurrence_frequency ?: "none"
                Text(
                    text = "${template.category} · $freq",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onDelete) {
                AppIcon(imageVector = Icons.Outlined.Delete, color = StateDanger, size = 20.dp)
            }
        }
    }
}

@Composable
fun CreateTemplateSheet(onClose: () -> Unit, onCreate: (String, String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("task") }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val catLabels = mapOf("task" to "Task", "person" to "Person", "subscription" to "Subscription", "custom_holiday" to "Event")

    AppCard(
        borderRadius = 24.dp,
        elevated = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding() + 20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text("New Template", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            
            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Monthly Bills",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Category", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
            AppCard(
                borderRadius = 16.dp,
                modifier = Modifier.fillMaxWidth().clickable { showCategoryPicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select category", color = TextPrimary)
                    Text(catLabels[category] ?: category, color = Accent500, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Add a note...",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Row {
                AppButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                AppButton(
                    onClick = { onCreate(name, category, notes.takeIf { it.isNotBlank() }) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    tint = Accent500
                ) {
                    AppIcon(Icons.Outlined.Add, color = Accent500, size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showCategoryPicker) {
        BottomSheetPicker(
            title = "Select category",
            items = catLabels.keys.toList(),
            initialSelection = category,
            onDismiss = { showCategoryPicker = false },
            onSelect = { category = it },
            itemLabel = { catLabels[it] ?: it }
        )
    }
}
