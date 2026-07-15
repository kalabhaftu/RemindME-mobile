package com.remindme.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.BottomSheetPicker
import com.remindme.app.ui.components.liquid.*
import com.remindme.app.ui.components.liquid.LiquidIcon
import com.remindme.app.ui.components.liquid.LiquidSnackbarHost
import com.remindme.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateHome: () -> Unit,
    onNavigateToThemeSelector: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LiquidScaffold(
        snackbarHost = { LiquidSnackbarHost(snackbarHostState) },
        appBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircledBackButton(onClick = onNavigateHome)
                Spacer(modifier = Modifier.width(12.dp))
                LiquidAppBar(
                    title = "Settings",
                    statusBarsPadding = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LiquidSpinner()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        TelegramSection(uiState, viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        TimezoneSection(uiState, viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        NotificationDefaultsSection(uiState, viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        TestNotificationsSection(viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        DeliveryLogSection(uiState)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        AppearanceSection(onNavigateToThemeSelector)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        AccountSection(viewModel, onNavigateHome)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        DangerZoneSection(viewModel, onNavigateHome)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    FloatingGlassContainer(
        borderRadius = 24.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun AppearanceSection(onNavigateToThemeSelector: () -> Unit) {
    val currentStyle = LocalLiquidGlassStyle.current
    val label = when (currentStyle) {
        LiquidGlassStyle.Frosted -> "Colored Glass"
        LiquidGlassStyle.Clear -> "Clear Glass"
    }

    SettingsSection(title = "Appearance") {
        FloatingGlassContainer(
            borderRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToThemeSelector() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Liquid Glass Style", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Choose between transparent and colored glass", color = TextSecondary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = Accent500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidIcon(Icons.Rounded.ChevronRight, color = TextTertiary, size = 18.dp)
                }
            }
        }
    }
}

@Composable
fun TelegramSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    SettingsSection("Telegram Bot Setup") {
        Text(
            "1. Message @BotFather on Telegram.\n2. Send /newbot and follow instructions.\n3. Copy the HTTP API Token and paste it below.",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoadingTelegram) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                LiquidSpinner()
            }
        } else if (uiState.hasTelegramToken) {
            FloatingGlassContainer(borderRadius = 16.dp) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Saved Token", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(uiState.maskedTelegramToken, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TextPrimary)
                        if (uiState.botUsername != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("@${uiState.botUsername}", color = Accent500, fontSize = 13.sp)
                        }
                    }
                    IconButton(onClick = { viewModel.deleteTelegramToken() }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("To update your token, delete the existing one first.", color = TextTertiary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = GlassBorder)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chat ID", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Send /start to your bot on Telegram, then tap Detect — or paste your Chat ID manually.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.hasChatId) {
                FloatingGlassContainer(borderRadius = 16.dp) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Saved Chat ID", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(uiState.maskedChatId, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TextPrimary)
                        }
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Checked", tint = StateSuccess)
                    }
                }
            } else {
                var chatId by remember { mutableStateOf("") }
                LiquidTextField(
                    value = chatId,
                    onValueChange = { chatId = it },
                    placeholder = "e.g. 123456789",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Button(
                        onClick = { viewModel.detectChatId() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BgSurface3, contentColor = TextPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.WifiTethering, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detect Chat ID")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.saveChatId(chatId) },
                        enabled = chatId.isNotEmpty(),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent500, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        } else {
            var token by remember { mutableStateOf("") }
            LiquidTextField(
                value = token,
                onValueChange = { token = it },
                placeholder = "123456789:ABCdefGHIjklmNOPqrstUVwxyZ",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.saveTelegramToken(token) },
                enabled = token.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent500, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Token")
            }
        }
    }
}

@Composable
fun TimezoneSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    SettingsSection("Timezone") {
        Text("Used to resolve reminder times.", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))

        var showPicker by remember { mutableStateOf(false) }
        val timezones = listOf(
            "UTC", "America/New_York", "America/Chicago", "America/Denver",
            "America/Los_Angeles", "Europe/London", "Europe/Paris", "Europe/Berlin",
            "Europe/Moscow", "Asia/Tokyo", "Asia/Shanghai", "Asia/Kolkata",
            "Australia/Sydney", "Pacific/Auckland"
        )
        
        FloatingGlassContainer(
            borderRadius = 16.dp,
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Timezone", color = TextPrimary)
                Text(uiState.timezone, color = Accent500, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showPicker) {
            BottomSheetPicker(
                title = "Select timezone",
                items = timezones,
                initialSelection = uiState.timezone,
                onDismiss = { showPicker = false },
                onSelect = { viewModel.updatePreference("timezone", it) },
                itemLabel = { it }
            )
        }
    }
}

@Composable
fun NotificationDefaultsSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    SettingsSection("Global Notification Defaults") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("These settings will be inherited by new reminders unless you override them per-item.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = { /* NotificationHelpScreen is shown as overlay via NavKey */ },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                LiquidIcon(Icons.AutoMirrored.Rounded.HelpOutline, modifier = Modifier.size(16.dp), color = Accent500)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Help", color = Accent500, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        GlassSwitch("Email Notifications", uiState.emailEnabled) { viewModel.updatePreference("default_channel_email", it) }
        Spacer(modifier = Modifier.height(8.dp))
        GlassSwitch("Push Notifications", uiState.pushEnabled) { viewModel.updatePreference("default_channel_push", it) }
        Spacer(modifier = Modifier.height(8.dp))
        GlassSwitch("Telegram Notifications", uiState.telegramEnabled) { viewModel.updatePreference("default_channel_telegram", it) }
        Spacer(modifier = Modifier.height(8.dp))
        GlassSwitch("In-App Notifications", uiState.inAppEnabled) { viewModel.updatePreference("default_channel_in_app", it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Default Timing", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        var showTimingPicker by remember { mutableStateOf(false) }
        val timings = listOf("at_time" to "At time of event", "morning_of" to "Morning of", "noon_of" to "Noon of", "evening_of" to "Evening of", "custom" to "Custom Time")
        
        FloatingGlassContainer(
            borderRadius = 16.dp,
            modifier = Modifier.fillMaxWidth().clickable { showTimingPicker = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Default Timing", color = TextPrimary)
                Text(timings.find { it.first == uiState.defaultLeadTime }?.second ?: uiState.defaultLeadTime, color = Accent500, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showTimingPicker) {
            BottomSheetPicker(
                title = "Select default timing",
                items = timings.map { it.first },
                initialSelection = uiState.defaultLeadTime,
                onDismiss = { showTimingPicker = false },
                onSelect = { viewModel.updatePreference("default_lead_time", it) },
                itemLabel = { id -> timings.find { it.first == id }?.second ?: id }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Escalation Nudge Delay (Hours)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        var nudgeVal by remember(uiState.nudgeDelayHours) { mutableStateOf(uiState.nudgeDelayHours.toString()) }
        LiquidTextField(
            value = nudgeVal,
            onValueChange = { 
                nudgeVal = it
                it.toIntOrNull()?.let { num ->
                    if (num > 0) viewModel.updatePreference("nudge_delay_hours", num)
                }
            },
            placeholder = "e.g. 4",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Time to wait before sending a follow-up nudge if a task is not marked done.", color = TextTertiary, fontSize = 12.sp)
    }
}

@Composable
fun GlassSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    FloatingGlassContainer(borderRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = TextPrimary)
            LiquidToggle(
                selected = { checked },
                onSelect = onCheckedChange
            )
        }
    }
}

@Composable
fun TestNotificationsSection(viewModel: SettingsViewModel) {
    SettingsSection("Test Notifications") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TestButton("Email", Icons.Rounded.Email, Modifier.weight(1f)) { viewModel.testChannel("email") }
            TestButton("Push", Icons.Rounded.Notifications, Modifier.weight(1f)) { viewModel.testChannel("push") }
            TestButton("Telegram", Icons.AutoMirrored.Rounded.Send, Modifier.weight(1f)) { viewModel.testChannel("telegram") }
        }
    }
}

@Composable
fun TestButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BgSurface3, contentColor = TextPrimary),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp)
        }
    }
}

@Composable
fun DeliveryLogSection(uiState: SettingsUiState) {
    SettingsSection("Recent Deliveries") {
        if (uiState.deliveryLogs.isEmpty()) {
            Text("No delivery history yet.", color = TextSecondary, fontSize = 13.sp)
        } else {
            Column {
                uiState.deliveryLogs.forEach { log ->
                    val color = when (log.status) {
                        "sent" -> StateSuccess
                        "failed" -> Color.Red
                        else -> TextTertiary
                    }
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${log.channel} · ${log.status}", modifier = Modifier.weight(1f), fontSize = 13.sp, color = TextPrimary)
                        if (log.scheduled_for != null) {
                            Text(log.scheduled_for.take(10), fontSize = 11.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSection(viewModel: SettingsViewModel, onNavigateHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    SettingsSection("Account") {
        Text("Export your data or sign out on all devices.", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))

        LiquidButton(
            onClick = { viewModel.checkForUpdate(context) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            LiquidIcon(Icons.Rounded.Update, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Check for Updates", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LiquidButton(
            onClick = { viewModel.exportData(context) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            LiquidIcon(Icons.Rounded.Download, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Data (JSON)", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LiquidButton(
            onClick = { viewModel.signOut(onNavigateHome) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            LiquidIcon(Icons.AutoMirrored.Rounded.Logout, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out (This Device)", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LiquidButton(
            onClick = { viewModel.signOutAllDevices(onNavigateHome) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            LiquidIcon(Icons.Rounded.PhonelinkErase, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out All Devices", color = TextPrimary)
        }
    }
}

@Composable
fun DangerZoneSection(viewModel: SettingsViewModel, onNavigateHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Red.copy(alpha = 0.05f))
            .border(1.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Security, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Permanently delete your account, all reminders, and revoke all external tokens. This action cannot be undone.",
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            LiquidButton(
                onClick = { viewModel.deleteAccount(onNavigateHome) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                surfaceColor = Color.Red.copy(alpha = 0.2f)
            ) {
                LiquidIcon(Icons.Rounded.Delete, modifier = Modifier.size(18.dp), color = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account", color = Color.Red)
            }
        }
    }
}

@Composable
fun ThemeSelectorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentStyle = LocalLiquidGlassStyle.current

    LiquidScaffold(
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
                LiquidAppBar(
                    title = "Theme Selector",
                    statusBarsPadding = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose your Liquid Glass style. The entire app's backdrop and card elements will update instantly to reflect your choice.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            LiquidGlassStyle.entries.forEach { style ->
                val isSelected = currentStyle == style
                val title = when (style) {
                    LiquidGlassStyle.Clear -> "Clear Glass (Reference Style)"
                    LiquidGlassStyle.Frosted -> "Colored Glass (Frosted Style)"
                }
                val desc = when (style) {
                    LiquidGlassStyle.Clear -> "Perfect transparency. Blends directly into the background using a glassmorphic shader overlay."
                    LiquidGlassStyle.Frosted -> "A beautiful frosted effect with solid color tints, enhancing text readability and UI depth."
                }

                FloatingGlassContainer(
                    borderRadius = 24.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clickable {
                            LiquidGlassPrefs.setStyle(context, style)
                        }
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Accent500 else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            if (isSelected) {
                                LiquidIcon(Icons.Rounded.CheckCircle, color = Accent500, size = 20.dp)
                            } else {
                                LiquidIcon(Icons.Rounded.Circle, color = TextTertiary, size = 20.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Render the mockup using the specific theme style override
                        CompositionLocalProvider(LocalLiquidGlassStyle provides style) {
                            FloatingGlassContainer(
                                borderRadius = 16.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Accent500.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LiquidIcon(Icons.Rounded.Notifications, color = Accent500, size = 20.dp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Sample Reminder Card", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                        Text("Mockup card showing the selected glass style", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
