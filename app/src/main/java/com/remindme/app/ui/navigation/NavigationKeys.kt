package com.remindme.app.ui.navigation

import com.remindme.app.domain.models.CategoryType
import com.remindme.app.domain.models.ReminderItem
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
@Serializable data class EditSubscription(val subscriptionId: String) : NavKey
@Serializable data object AddTask : NavKey
@Serializable data class EditTask(val taskId: String) : NavKey
@Serializable data class EditReminder(val reminderId: String) : NavKey
@Serializable data class ReminderPreview(val reminderId: String) : NavKey

@Serializable data class PersonDetail(val personId: String) : NavKey

fun editDestinationFor(item: ReminderItem): NavKey = when (item.category) {
    CategoryType.PERSON -> EditPerson(item.id)
    CategoryType.SUBSCRIPTION -> EditSubscription(item.id)
    CategoryType.TASK -> EditTask(item.id)
    CategoryType.CUSTOM_HOLIDAY -> EditReminder(item.id)
}
