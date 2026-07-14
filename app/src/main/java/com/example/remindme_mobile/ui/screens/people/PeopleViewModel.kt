package com.example.remindme_mobile.ui.screens.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme_mobile.data.remote.SupabaseManager
import com.example.remindme_mobile.data.repository.ReminderRepository
import com.example.remindme_mobile.domain.models.CategoryType
import com.example.remindme_mobile.domain.models.ReminderItem
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PeopleSort {
    DAYS_ASC, NAME_ASC, AGE_DESC, RECENT
}

data class PeopleUiState(
    val people: List<ReminderItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val sort: PeopleSort = PeopleSort.DAYS_ASC,
    val searchQuery: String = ""
)

class PeopleViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    init {
        fetchPeople()
        setupRealtime()
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            realtimeChannel = SupabaseManager.client.channel("mobile_people")
            val tables = listOf("reminder_items", "person_details")
            
            tables.forEach { table ->
                realtimeChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                    this.table = table
                }?.collect {
                    fetchPeople(showLoading = false)
                }
            }
            realtimeChannel?.subscribe()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            realtimeChannel?.unsubscribe()
        }
    }

    fun fetchPeople(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val all = repository.getReminders()
                val people = all.filter { it.category == CategoryType.PERSON }
                _uiState.update { it.copy(people = people, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSort(sort: PeopleSort) {
        _uiState.update { it.copy(sort = sort) }
    }

    fun deletePerson(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(id)
                fetchPeople(showLoading = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
