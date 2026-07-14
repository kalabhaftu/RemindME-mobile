package com.example.remindme_mobile.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object AddPerson : NavKey
@Serializable data object AddSubscription : NavKey
@Serializable data object AddTask : NavKey
