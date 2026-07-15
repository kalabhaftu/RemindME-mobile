package com.remindme.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.remindme.app.data.remote.SupabaseManager
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainNavigation() {
    var isAuthenticated by remember { mutableStateOf(SupabaseManager.client.auth.currentUserOrNull() != null) }

    LaunchedEffect(Unit) {
        SupabaseManager.client.auth.sessionStatus.collectLatest { status ->
            isAuthenticated = status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated
        }
    }

    val backStack = rememberNavBackStack(if (isAuthenticated) Main else Login)

    // Ensure we redirect if auth state changes
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            backStack.clear()
            backStack.add(Login)
        } else if (backStack.lastOrNull() == Login) {
            backStack.clear()
            backStack.add(Main)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
        entryProvider {
            entry<Login> {
                LoginScreen()
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
            entry<PersonDetail> {
                PersonDetailScreen(
                    personId = it.personId,
                    onBack = { backStack.removeLastOrNull() },
                    onEdit = { /* TODO */ }
                )
            }
            entry<Search> {
                SearchScreen(
                    onItemClick = { id -> backStack.add(PersonDetail(id)) }
                )
            }
            entry<Settings> {
                SettingsScreen(
                    onNavigateHome = { 
                        backStack.clear()
                        backStack.add(Main) 
                    }
                )
            }
            entry<Templates> {
                TemplatesScreen(
                    onApplyTemplate = { /* handle */ }
                )
            }
            entry<Notifications> {
                com.remindme.app.ui.screens.notifications.NotificationsScreen(
                    onOpenReminder = { id -> backStack.add(PersonDetail(id)) }
                )
            }
        },
    )
}
