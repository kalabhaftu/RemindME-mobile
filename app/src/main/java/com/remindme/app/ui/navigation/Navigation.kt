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
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.ui.components.liquid.LiquidGlassPrefs
import com.remindme.app.ui.components.liquid.LocalLiquidGlassStyle
import com.remindme.app.ui.screens.main.MainScreen
import com.remindme.app.ui.screens.add.AddPersonScreen
import com.remindme.app.ui.screens.add.AddSubscriptionScreen
import com.remindme.app.ui.screens.add.AddTaskScreen
import com.remindme.app.ui.screens.login.LoginScreen
import com.remindme.app.ui.screens.peopledetail.PersonDetailScreen
import com.remindme.app.ui.screens.search.SearchScreen
import com.remindme.app.ui.screens.settings.SettingsScreen
import com.remindme.app.ui.screens.templates.TemplatesScreen
import io.github.jan.supabase.auth.auth
import com.remindme.app.domain.models.CategoryType
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    var sessionStatus by remember { mutableStateOf<io.github.jan.supabase.auth.status.SessionStatus?>(null) }
    var glassStyle by remember { mutableStateOf(LiquidGlassPrefs.getStyle(context)) }

    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "glass_style") {
                glassStyle = LiquidGlassPrefs.getStyle(context)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(context) {
        val prefs = context.getSharedPreferences("liquid_glass_prefs", android.content.Context.MODE_PRIVATE)
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

    LaunchedEffect(sessionStatus) {
        val status = sessionStatus ?: return@LaunchedEffect
        when (status) {
            is io.github.jan.supabase.auth.status.SessionStatus.Initializing -> {
                // Stay on AuthCheck / splash
            }
            is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                if (backStack.lastOrNull() == AuthCheck || backStack.lastOrNull() == Login) {
                    backStack.clear()
                    backStack.add(Main)
                }
            }
            else -> {
                if (backStack.lastOrNull() == AuthCheck || backStack.lastOrNull() == Main) {
                    backStack.clear()
                    backStack.add(Login)
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalLiquidGlassStyle provides glassStyle
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
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
                            backStack.clear()
                            backStack.add(Main)
                        }
                    )
                }
            entry<Main> {
                MainScreen(onItemClick = { navKey -> backStack.add(navKey) }, modifier = Modifier.safeDrawingPadding().padding(16.dp))
            }
            entry<AddPerson> {
                AddPersonScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<AddSubscription> {
                AddSubscriptionScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<AddTask> {
                AddTaskScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<PersonDetail> { key ->
                PersonDetailScreen(
                    personId = key.personId,
                    onBack = { backStack.removeLastOrNull() },
                    onEdit = { backStack.add(EditPerson(key.personId)) }
                )
            }
            entry<EditPerson> { key ->
                AddPersonScreen(
                    personId = key.personId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Search> {
                val context = LocalContext.current
                SearchScreen(
                    onItemClick = { id, category ->
                        when (category) {
                            CategoryType.PERSON -> backStack.add(PersonDetail(id))
                            else -> {
                                android.widget.Toast.makeText(
                                    context,
                                    "Viewing/editing for ${category.name.lowercase()} is coming soon!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Settings> {
                SettingsScreen(
                    onNavigateHome = { 
                        backStack.clear()
                        backStack.add(Main) 
                    },
                    onNavigateToThemeSelector = {
                        backStack.add(ThemeSelector)
                    }
                )
            }
            entry<ThemeSelector> {
                com.remindme.app.ui.screens.settings.ThemeSelectorScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Templates> {
                TemplatesScreen(
                    onApplyTemplate = { /* handle */ },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Notifications> {
                com.remindme.app.ui.screens.notifications.NotificationsScreen(
                    onOpenReminder = { id -> backStack.add(PersonDetail(id)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        },
    )
    }
}
