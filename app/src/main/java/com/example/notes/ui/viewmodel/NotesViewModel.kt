package com.example.notes.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.notes.data.local.entities.Note
import com.example.notes.data.repository.NotesRepository
import com.example.notes.ui.navigation.Screen
import com.example.notes.util.isSameDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val noteId: Long = try {
        savedStateHandle.toRoute<Screen.NoteDetail>().noteId
    } catch (e: Exception) {
        -1L
    }
    
    val existingNote: StateFlow<Note?> = if (noteId != -1L) {
        repository.getNoteByIdFlow(noteId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null).asStateFlow()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = combine(
        searchQuery
            .debounce(300L)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.allNotes
                } else {
                    repository.searchNotes(query)
                }
            },
        selectedDateMillis
    ) { notes, selectedDateMillis ->
        if (selectedDateMillis == null) {
            notes
        } else {
            notes.filter { it.createdAt.isSameDay(selectedDateMillis) }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun setSelectedDate(dateMillis: Long?) {
        _selectedDateMillis.value = dateMillis
    }

    fun addNote(content: String) {
        viewModelScope.launch {
            repository.insertNote(Note(content = content))
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    suspend fun getNoteById(id: Long): Note? {
        return repository.getNoteById(id)
    }
}
