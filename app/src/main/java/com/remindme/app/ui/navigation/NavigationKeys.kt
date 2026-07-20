package com.remindme.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object AuthCheck : NavKey
@Serializable data object Login : NavKey
@Serializable data object Search : NavKey
@Serializable data object Settings : NavKey
@Serializable data object Templates : NavKey
@Serializable data object Notifications : NavKey
@Serializable data object ThemeSelector : NavKey
@Serializable data object NotificationHelp : NavKey
@Serializable data object MagicLink : NavKey

@Serializable data object AddPerson : NavKey
@Serializable data class EditPerson(val personId: String) : NavKey
@Serializable data object AddSubscription : NavKey
@Serializable data object AddTask : NavKey
@Serializable data class EditReminder(val reminderId: String) : NavKey

@Serializable data class PersonDetail(val personId: String) : NavKey
