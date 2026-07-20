package com.remindme.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.ui.components.ThemePrefs
import com.remindme.app.ui.components.LocalThemeStyle
import com.remindme.app.ui.screens.main.MainScreen
import com.remindme.app.ui.screens.add.AddPersonScreen
import com.remindme.app.ui.screens.add.AddSubscriptionScreen
import com.remindme.app.ui.screens.add.AddTaskScreen
import com.remindme.app.ui.screens.login.LoginScreen
import com.remindme.app.ui.screens.login.MagicLinkScreen
import com.remindme.app.ui.screens.peopledetail.PersonDetailScreen
import com.remindme.app.ui.screens.search.SearchScreen
import com.remindme.app.ui.screens.settings.SettingsScreen
import com.remindme.app.ui.screens.edit.EditReminderScreen
import com.remindme.app.ui.screens.notifications.NotificationHelpScreen
import com.remindme.app.ui.screens.templates.TemplatesScreen
import io.github.jan.supabase.auth.auth
import com.remindme.app.domain.models.CategoryType
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import com.remindme.app.services.OfflineSyncScheduler
import com.remindme.app.services.PushTokenRegistrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(
    openReminderId: String? = null,
    onReminderOpened: () -> Unit = {}
) {
    val context = LocalContext.current
    var sessionStatus by remember { mutableStateOf<io.github.jan.supabase.auth.status.SessionStatus?>(null) }
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

    LaunchedEffect(Unit) {
        SupabaseManager.client.auth.sessionStatus.collectLatest { status ->
            sessionStatus = status
        }
    }

    val backStack = rememberNavBackStack(AuthCheck)

    fun replaceRoot(destination: NavKey) {
        if (backStack.isEmpty()) {
            backStack.add(destination)
            return
        }
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
        while (backStack.size > 1) {
            backStack.removeAt(0)
        }
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    LaunchedEffect(sessionStatus) {
        val status = sessionStatus ?: return@LaunchedEffect
        when (status) {
            is io.github.jan.supabase.auth.status.SessionStatus.Initializing -> {
                // Stay on AuthCheck / splash
            }
            is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                OfflineSyncScheduler.runNow(context)
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    runCatching { PushTokenRegistrar.registerCurrentToken() }
                }
                if (backStack.lastOrNull() == AuthCheck || backStack.lastOrNull() == Login) {
                    replaceRoot(Main)
                }
            }
            else -> {
                if (backStack.lastOrNull() != Login) {
                    replaceRoot(Login)
                }
            }
        }
    }

    LaunchedEffect(openReminderId, sessionStatus) {
        if (openReminderId != null && sessionStatus is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
            if (backStack.lastOrNull() != EditReminder(openReminderId)) {
                replaceRoot(EditReminder(openReminderId))
            }
            onReminderOpened()
        }
    }

    CompositionLocalProvider(
        LocalThemeStyle provides glassStyle
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { popBack() },
            entryProvider =
            entryProvider {
                entry<AuthCheck> {
                    val bgBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF1A1A2E),
                            androidx.compose.ui.graphics.Color(0xFF16213E),
                            androidx.compose.ui.graphics.Color(0xFF0F3460)
                        ),
                        start = androidx.compose.ui.geometry.Offset.Zero,
                        end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "RemindME",
                            fontSize = 32.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                entry<Login> {
                    LoginScreen(
                        onNavigateHome = {
                            replaceRoot(Main)
                        },
                        onNavigateToMagicLink = {
                            backStack.add(MagicLink)
                        }
                    )
                }
            entry<Main> {
                MainScreen(onItemClick = { navKey -> backStack.add(navKey) }, modifier = Modifier.safeDrawingPadding().padding(16.dp))
            }
            entry<AddPerson> {
                AddPersonScreen(onBack = { popBack() })
            }
            entry<AddSubscription> {
                AddSubscriptionScreen(onBack = { popBack() })
            }
            entry<AddTask> {
                AddTaskScreen(onBack = { popBack() })
            }
            entry<EditReminder> { key ->
                EditReminderScreen(
                    reminderId = key.reminderId,
                    onBack = { popBack() }
                )
            }
            entry<PersonDetail> { key ->
                PersonDetailScreen(
                    personId = key.personId,
                    onBack = { popBack() },
                    onEdit = { backStack.add(EditPerson(key.personId)) }
                )
            }
            entry<EditPerson> { key ->
                AddPersonScreen(
                    personId = key.personId,
                    onBack = { popBack() }
                )
            }
            entry<Search> {
                val context = LocalContext.current
                SearchScreen(
                    onItemClick = { id, category ->
                        backStack.add(EditReminder(id))
                    },
                    onBack = { popBack() }
                )
            }
            entry<Settings> {
                    SettingsScreen(
                    onNavigateHome = { 
                        replaceRoot(Main)
                    },
                    onNavigateToThemeSelector = {
                        backStack.add(ThemeSelector)
                    },
                    onNavigateToNotificationHelp = {
                        backStack.add(NotificationHelp)
                    }
                )
            }
            entry<ThemeSelector> {
                com.remindme.app.ui.screens.settings.ThemeSelectorScreen(
                    onBack = { popBack() }
                )
            }
            entry<MagicLink> {
                MagicLinkScreen(
                    onNavigateHome = {
                        replaceRoot(Main)
                    },
                    onBack = { popBack() }
                )
            }
            entry<NotificationHelp> {
                NotificationHelpScreen(
                    onBack = { popBack() }
                )
            }
            entry<Templates> {
                TemplatesScreen(
                    onApplyTemplate = { /* handle */ },
                    onBack = { popBack() }
                )
            }
            entry<Notifications> {
                com.remindme.app.ui.screens.notifications.NotificationsScreen(
                    onOpenReminder = { id -> backStack.add(EditReminder(id)) },
                    onBack = { popBack() }
                )
            }
        },
    )
    }
}
