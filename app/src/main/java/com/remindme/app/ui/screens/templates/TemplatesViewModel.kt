package com.remindme.app.ui.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.time.Instant

@Serializable
data class ReminderTemplate(
    val id: String,
    val user_id: String,
    val name: String,
    val category: String,
    val notes_template: String? = null,
    val recurrence_frequency: String? = null,
    val created_at: String
)

data class TemplatesUiState(
    val templates: List<ReminderTemplate> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TemplatesViewModel : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    private val supabase = SupabaseManager.client

    init {
        loadTemplates()
    }

    fun loadTemplates() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            val list = supabase.postgrest["reminder_templates"]
                .select {
                    filter { eq("user_id", user.id) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ReminderTemplate>()
            _uiState.update { it.copy(templates = list, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    fun createTemplate(name: String, category: String, notes: String?) = viewModelScope.launch(Dispatchers.IO) {
        if (name.isBlank()) return@launch
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            val obj = buildJsonObject {
                put("user_id", user.id)
                put("name", name.trim())
                put("category", category)
                if (!notes.isNullOrBlank()) {
                    put("notes_template", notes.trim())
                }
                putJsonArray("default_channels") { add(kotlinx.serialization.json.JsonPrimitive("in_app")) }
            }
            supabase.postgrest["reminder_templates"].insert(obj)
            loadTemplates()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun deleteTemplate(id: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            supabase.postgrest["reminder_templates"].delete {
                filter { eq("id", id) }
            }
            loadTemplates()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }
}
