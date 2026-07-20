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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.remindme.app.ui.components.BottomSheetPicker
import com.remindme.app.ui.components.*
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.SnackbarHost
import com.remindme.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateHome: () -> Unit,
    onNavigateToThemeSelector: () -> Unit,
    onNavigateToNotificationHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    var glassStyle by remember { mutableStateOf(ThemePrefs.getStyle(context)) }

    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme_style") {
                glassStyle = ThemePrefs.getStyle(context)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(context) {
        val prefs = context.getSharedPreferences("remindme_prefs", android.content.Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    CompositionLocalProvider(LocalThemeStyle provides glassStyle) {
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
                    CircledBackButton(onClick = onNavigateHome)
                    Spacer(modifier = Modifier.width(12.dp))
                    TopBar(
                        title = "Settings",
                        statusBarsPadding = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 32.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isLoading) {
                        item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    }
                    item {
                        TelegramSection(uiState, viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        TimezoneSection(uiState, viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        NotificationDefaultsSection(uiState, viewModel, onNavigateToNotificationHelp)
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
                        CalendarSubscriptionSection(uiState, viewModel)
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
                        AboutSupportSection()
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
    AppCard(
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
    val currentStyle = LocalThemeStyle.current
    val label = when (currentStyle) {
        ThemeStyle.Glass -> "Default Glass"
        ThemeStyle.Solid -> "Flat Solid"
    }

    SettingsSection(title = "Appearance") {
        AppCard(
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
                    Text("Theme Appearance", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Glass: blurred backgrounds | Solid: flat colors", color = TextSecondary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = Accent500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    AppIcon(iconRes = PremiumIcons.ChevronRight, color = TextTertiary, size = 18.dp)
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
                Spinner()
            }
        } else if (uiState.hasTelegramToken) {
            AppCard(borderRadius = 16.dp) {
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
                        AppIcon(iconRes = PremiumIcons.Delete, contentDescription = "Delete", tint = StateDanger)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("To update your token, delete the existing one first.", color = TextTertiary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderSubtle)
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
                AppCard(borderRadius = 16.dp) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Saved Chat ID", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(uiState.maskedChatId, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TextPrimary)
                        }
                        AppIcon(iconRes = PremiumIcons.CheckCircle, contentDescription = "Checked", tint = StateSuccess)
                    }
                }
            } else {
                var chatId by remember { mutableStateOf("") }
                AppTextField(
                    value = chatId,
                    onValueChange = { chatId = it },
                    placeholder = "e.g. 123456789",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    AppButton(
                        onClick = { viewModel.detectChatId() },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        AppIcon(iconRes = PremiumIcons.WifiTethering, color = TextPrimary, size = 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detect Chat ID", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    AppButton(
                        onClick = { viewModel.saveChatId(chatId) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        tint = Accent500
                    ) {
                        AppIcon(iconRes = PremiumIcons.Save, color = Accent500, size = 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save", fontSize = 14.sp)
                    }
                }
            }
        } else {
            var token by remember { mutableStateOf("") }
            AppTextField(
                value = token,
                onValueChange = { token = it },
                placeholder = "123456789:ABCdefGHIjklmNOPqrstUVwxyZ",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppButton(
                onClick = { viewModel.saveTelegramToken(token) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                tint = Accent500
            ) {
                AppIcon(iconRes = PremiumIcons.Save, color = Accent500, size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Token", fontSize = 14.sp)
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
        
        AppCard(
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
fun NotificationDefaultsSection(uiState: SettingsUiState, viewModel: SettingsViewModel, onNavigateToNotificationHelp: () -> Unit = {}) {
    SettingsSection("Global Notification Defaults") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("These settings will be inherited by new reminders unless you override them per-item.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onNavigateToNotificationHelp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                AppIcon(iconRes = PremiumIcons.HelpOutline, modifier = Modifier.size(16.dp), color = Accent500)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Help", color = Accent500, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        GlassSwitchGroup(
            listOf(
                Triple("Email Notifications", uiState.emailEnabled) { checked: Boolean -> viewModel.updatePreference("default_channel_email", checked) },
                Triple("Push Notifications", uiState.pushEnabled) { checked: Boolean -> viewModel.updatePreference("default_channel_push", checked) },
                Triple("Telegram Notifications", uiState.telegramEnabled) { checked: Boolean -> viewModel.updatePreference("default_channel_telegram", checked) },
                Triple("In-App Notifications", uiState.inAppEnabled) { checked: Boolean -> viewModel.updatePreference("default_channel_in_app", checked) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Default Timing", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        var showTimingPicker by remember { mutableStateOf(false) }
        val timings = listOf("at_time" to "At time of event", "morning_of" to "Morning of", "noon_of" to "Noon of", "evening_of" to "Evening of", "custom" to "Custom Time")
        
        AppCard(
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
        AppTextField(
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
    AppCard(borderRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = TextPrimary)
            AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun GlassSwitchGroup(rows: List<Triple<String, Boolean, (Boolean) -> Unit>>) {
    AppCard(borderRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            rows.forEachIndexed { index, (title, checked, onCheckedChange) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontWeight = FontWeight.Medium, color = TextPrimary)
                    AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
                }
                if (index != rows.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = BorderSubtle
                    )
                }
            }
        }
    }
}

@Composable
fun TestNotificationsSection(viewModel: SettingsViewModel) {
    SettingsSection("Test Notifications") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TestButton("Email", PremiumIcons.Email, Modifier.weight(1f)) { viewModel.testChannel("email") }
            TestButton("Push", PremiumIcons.Notifications, Modifier.weight(1f)) { viewModel.testChannel("push") }
            TestButton("Telegram", PremiumIcons.Send, Modifier.weight(1f)) { viewModel.testChannel("telegram") }
        }
    }
}

@Composable
fun TestButton(label: String, icon: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AppButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp)
    ) {
        AppIcon(iconRes = icon, color = TextPrimary, size = 16.dp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, maxLines = 1)
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
                        "failed" -> StateDanger
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
                    if (!log.error_message.isNullOrBlank()) {
                        Text(
                            log.error_message,
                            color = StateDanger.copy(alpha = 0.82f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSubscriptionSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val webcalUrl = uiState.calendarWebcalUrl
    var showManualGuide by remember { mutableStateOf(false) }

    SettingsSection("Calendar subscription") {
        Text(
            "Subscribe once and birthdays you add in RemindME will appear in Google Calendar or Outlook.",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (webcalUrl == null) {
            Text(
                if (uiState.isLoadingCalendar) "Preparing your private calendar link…" else "Calendar link unavailable. Check your connection and retry.",
                color = TextTertiary,
                fontSize = 13.sp
            )
            if (!uiState.isLoadingCalendar) {
                Spacer(modifier = Modifier.height(12.dp))
                AppButton(
                    onClick = { viewModel.loadCalendarFeed() },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    AppIcon(iconRes = PremiumIcons.Refresh, color = TextPrimary, size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry", color = TextPrimary)
                }
            }
        } else {
            AppCard(borderRadius = 16.dp) {
                Text(
                    webcalUrl,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AppButton(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("RemindME calendar", webcalUrl))
                    viewModel.showMessage("Calendar link copied")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                tint = Accent500
            ) {
                AppIcon(iconRes = PremiumIcons.ContentCopy, color = Accent500, size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy webcal link", color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                onClick = {
                    val webcalIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(webcalUrl))
                    val calendarHandler = context.packageManager.queryIntentActivities(
                        webcalIntent,
                        android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                    ).firstOrNull { info ->
                        val packageName = info.activityInfo.packageName.lowercase()
                        packageName.contains("calendar") || packageName.contains("outlook")
                    }
                    if (calendarHandler == null) {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("RemindME calendar", webcalUrl))
                        viewModel.showMessage("No calendar app accepted webcal; link copied")
                        showManualGuide = true
                    } else {
                        webcalIntent.setPackage(calendarHandler.activityInfo.packageName)
                        runCatching { context.startActivity(webcalIntent) }.onFailure {
                            showManualGuide = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                AppIcon(iconRes = PremiumIcons.OpenInNew, color = TextPrimary, size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open calendar app", color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { viewModel.rotateCalendarFeed() },
                enabled = !uiState.isLoadingCalendar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Regenerate private link", color = StateDanger)
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Google Calendar", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                "1. Open Google Calendar. 2. Open Other calendars and choose Add by URL. 3. Paste the webcal link and add the calendar.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Outlook Calendar", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                "1. Open Outlook Calendar. 2. Choose Add calendar and Subscribe from web. 3. Paste the webcal link and subscribe.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }

    if (showManualGuide) {
        AlertDialog(
            onDismissRequest = { showManualGuide = false },
            containerColor = appSurfaceColor(elevated = true),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Connect your calendar", color = TextPrimary) },
            text = {
                Text(
                    "The installed calendar app does not accept webcal links automatically. The private link is already copied.\n\nGoogle Calendar\n1. Open Google Calendar.\n2. Open Other calendars and choose Add by URL.\n3. Paste the link and add the calendar.\n\nOutlook Calendar\n1. Open Outlook Calendar.\n2. Choose Add calendar and Subscribe from web.\n3. Paste the link and subscribe."
                )
            },
            confirmButton = {
                TextButton(onClick = { showManualGuide = false }) {
                    Text("Got it", color = Accent500)
                }
            }
        )
    }
}

@Composable
fun AccountSection(viewModel: SettingsViewModel, onNavigateHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportData(context, it) } }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importData(context, it) } }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.importAllContacts(context) }
    SettingsSection("Account") {
        Text("Export your data or sign out on all devices.", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))

        AppButton(
            onClick = { viewModel.checkForUpdate(context) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            AppIcon(iconRes = PremiumIcons.Update, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Check for Updates", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            onClick = { exportLauncher.launch("remindme-export-${java.time.LocalDate.now()}.json") },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            AppIcon(iconRes = PremiumIcons.Download, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Data (JSON)", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            AppIcon(iconRes = PremiumIcons.UploadFile, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import JSON", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            onClick = {
                if (!uiState.isImportingContacts) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.importAllContacts(context)
                    } else {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            },
            enabled = !uiState.isImportingContacts,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (uiState.isImportingContacts) {
                Spinner(size = 18.dp)
            } else {
                AppIcon(iconRes = PremiumIcons.People, modifier = Modifier.size(18.dp), color = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (uiState.isImportingContacts) "Importing contacts…" else "Import All Contacts", color = TextPrimary)
        }
        Text(
            "Imports every device contact in one run. Contacts without a birthday are added without a birthday notification; duplicates are skipped.",
            color = TextTertiary,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            onClick = { viewModel.signOut(onNavigateHome) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            AppIcon(iconRes = PremiumIcons.Logout, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out (This Device)", color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            onClick = { viewModel.signOutAllDevices(onNavigateHome) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            AppIcon(iconRes = PremiumIcons.PhonelinkErase, modifier = Modifier.size(18.dp), color = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out All Devices", color = TextPrimary)
        }
    }
}

@Composable
fun AboutSupportSection() {
    val context = LocalContext.current
    val webBase = com.remindme.app.BuildConfig.WEB_API_URL.trimEnd('/')

    fun openUrl(url: String) {
        runCatching {
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        }.onFailure {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    SettingsSection("About & support") {
        Text(
            "RemindME keeps important dates close without making them noisy.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Version ${com.remindme.app.BuildConfig.VERSION_NAME}", color = TextTertiary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AppButton(
                onClick = {
                    runCatching {
                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "RemindME — reminders that stay close. $webBase")
                        }.let { android.content.Intent.createChooser(it, "Share RemindME") })
                    }.onFailure {
                        Toast.makeText(context, "Unable to share RemindME", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f).height(46.dp)
            ) {
                AppIcon(iconRes = PremiumIcons.Share, color = TextPrimary, size = 17.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", color = TextPrimary)
            }
            AppButton(
                onClick = {
                    runCatching {
                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:kalabhaftu1@gmail.com?subject=RemindME%20feedback")
                        })
                    }.onFailure {
                        Toast.makeText(context, "No email app available", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f).height(46.dp)
            ) {
                AppIcon(iconRes = PremiumIcons.MailOutline, color = TextPrimary, size = 17.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Feedback", color = TextPrimary)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { openUrl("$webBase/terms") }, modifier = Modifier.weight(1f)) {
                Text("Terms of Service", color = Accent500, fontSize = 12.sp)
            }
            TextButton(onClick = { openUrl("$webBase/privacy") }, modifier = Modifier.weight(1f)) {
                Text("Privacy Policy", color = Accent500, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DangerZoneSection(viewModel: SettingsViewModel, onNavigateHome: () -> Unit) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(StateDanger.copy(alpha = 0.05f))
            .border(1.dp, StateDanger.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(iconRes = PremiumIcons.Security, contentDescription = null, tint = StateDanger, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = StateDanger)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Permanently delete your account, all reminders, and revoke all external tokens. This action cannot be undone.",
                color = StateDanger.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                surfaceColor = StateDanger.copy(alpha = 0.2f)
            ) {
                AppIcon(iconRes = PremiumIcons.Delete, modifier = Modifier.size(18.dp), color = StateDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account", color = StateDanger)
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = appSurfaceColor(elevated = true),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Delete account?", color = TextPrimary) },
            text = { Text("This permanently deletes your reminders, settings, delivery channels, and account. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    viewModel.deleteAccount(onNavigateHome)
                }) { Text("Delete", color = StateDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
fun ThemeSelectorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentStyle by remember { mutableStateOf(ThemePrefs.getStyle(context)) }

    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme_style") {
                currentStyle = ThemePrefs.getStyle(context)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(context) {
        val prefs = context.getSharedPreferences("remindme_prefs", android.content.Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    CompositionLocalProvider(LocalThemeStyle provides currentStyle) {
        AppScaffold(
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
                text = "Switch between Glass and Solid theme appearance.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            ThemeStyle.entries.forEach { style ->
                val isSelected = currentStyle == style
                val title = when (style) {
                    ThemeStyle.Glass -> "Default Glass"
                    ThemeStyle.Solid -> "Flat Solid"
                }
                val desc = when (style) {
                    ThemeStyle.Glass -> "Semi-transparent panels and subtle glass depth. The default RemindME look."
                    ThemeStyle.Solid -> "Solid opaque surfaces. Cleaner contrast with flat backgrounds."
                }

                AppCard(
                    borderRadius = 24.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clickable {
                            ThemePrefs.setStyle(context, style)
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
                                AppIcon(iconRes = PremiumIcons.CheckCircle, color = Accent500, size = 20.dp)
                            } else {
                                AppIcon(iconRes = PremiumIcons.Circle, color = TextTertiary, size = 20.dp)
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
                        CompositionLocalProvider(LocalThemeStyle provides style) {
                            AppCard(
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
                                        AppIcon(iconRes = PremiumIcons.Notifications, color = Accent500, size = 20.dp)
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
}
