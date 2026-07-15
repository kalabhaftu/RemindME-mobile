package com.remindme.app.ui.screens.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.domain.models.ReminderItem
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeParseException
import com.remindme.app.utils.ComputedFields

enum class PeopleSort {
    DAYS_ASC, NAME_ASC, AGE_DESC, RECENT
}

data class PeopleUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val sort: PeopleSort = PeopleSort.DAYS_ASC,
    val searchQuery: String = ""
)

class PeopleViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    private val _allPeople = MutableStateFlow<List<ReminderItem>>(emptyList())
    
    val filteredPeople: StateFlow<List<ReminderItem>> = combine(_allPeople, _uiState) { people, state ->
        var list = people
        if (state.searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }

        fun getBirthdate(item: ReminderItem): LocalDate? {
            val bd = item.person?.birthdate?.takeIf { it.isNotBlank() } ?: return null
            return try { LocalDate.parse(bd.substring(0, 10)) } catch (e: Exception) { null }
        }

        list.sortedWith(Comparator { a, b ->
            when (state.sort) {
                PeopleSort.DAYS_ASC -> {
                    val bdA = getBirthdate(a)
                    val bdB = getBirthdate(b)
                    val daysA = if (bdA != null) ComputedFields.calculateDaysToBirthday(bdA) else 9999
                    val daysB = if (bdB != null) ComputedFields.calculateDaysToBirthday(bdB) else 9999
                    daysA.compareTo(daysB)
                }
                PeopleSort.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                PeopleSort.AGE_DESC -> {
                    val bdA = getBirthdate(a)
                    val bdB = getBirthdate(b)
                    if (bdA == null || bdB == null) 0 else {
                        val ageA = ComputedFields.calculateAge(bdA)
                        val ageB = ComputedFields.calculateAge(bdB)
                        ageB.compareTo(ageA)
                    }
                }
                PeopleSort.RECENT -> b.createdAt.compareTo(a.createdAt)
            }
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                _allPeople.value = people
                _uiState.update { it.copy(isLoading = false) }
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
                // Optimistically remove from list
                _allPeople.update { list -> list.filter { it.id != id } }
                repository.deleteReminder(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                // Refetch on error
                fetchPeople(showLoading = false)
            }
        }
    }
}
