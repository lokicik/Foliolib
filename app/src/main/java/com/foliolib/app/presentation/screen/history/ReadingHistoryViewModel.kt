package com.foliolib.app.presentation.screen.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.Note
import com.foliolib.app.domain.model.ReadingSession
import com.foliolib.app.domain.usecase.reading.GetNotesForBookUseCase
import com.foliolib.app.domain.usecase.reading.GetReadingSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReadingSessionWithNotes(
    val session: ReadingSession,
    val notes: List<Note>
)

data class ReadingHistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<ReadingSessionWithNotes> = emptyList()
)

@HiltViewModel
class ReadingHistoryViewModel @Inject constructor(
    private val getReadingSessionsUseCase: GetReadingSessionsUseCase,
    private val getNotesForBookUseCase: GetNotesForBookUseCase,
    private val readingRepository: com.foliolib.app.domain.repository.ReadingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])
    private val _uiState = MutableStateFlow(ReadingHistoryUiState())
    val uiState: StateFlow<ReadingHistoryUiState> = _uiState.asStateFlow()

    init {
        cleanupAndLoadSessions()
    }

    private fun cleanupAndLoadSessions() {
        viewModelScope.launch {
            readingRepository.deleteEmptySessions()
            
            combine(
                getReadingSessionsUseCase(bookId),
                getNotesForBookUseCase(bookId)
            ) { sessions, notes ->
                sessions.map { session ->
                    val sessionNotes = notes.filter { note ->
                        note.createdAt >= session.startTime && note.createdAt <= session.endTime
                    }
                    ReadingSessionWithNotes(session, sessionNotes)
                }
            }.collect { sessionsWithNotes ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sessions = sessionsWithNotes
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            readingRepository.deleteSessionsForBook(bookId)
        }
    }
}
