package com.foliolib.app.presentation.screen.reading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.ReadingSession
import com.foliolib.app.domain.usecase.book.GetBookDetailsUseCase
import com.foliolib.app.domain.usecase.book.UpdateBookUseCase
import com.foliolib.app.domain.usecase.reading.AddNoteUseCase
import com.foliolib.app.domain.usecase.reading.EndReadingSessionUseCase
import com.foliolib.app.domain.usecase.reading.StartReadingSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ReadingUiState(
    val book: Book? = null,
    val session: ReadingSession? = null,
    val currentPage: Int = 0,
    val elapsedTimeMillis: Long = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showNoteDialog: Boolean = false,
    val noteContent: String = ""
)

@HiltViewModel
class ReadingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val endReadingSessionUseCase: EndReadingSessionUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val addNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadBook()
        startSession()
        startTimer()
    }

    private fun loadBook() {
        viewModelScope.launch {
            getBookDetailsUseCase(bookId).collect { book ->
                _uiState.update { state ->
                    state.copy(
                        book = book,
                        currentPage = book?.currentPage ?: 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            startReadingSessionUseCase(bookId)
                .onSuccess { session ->
                    _uiState.update { it.copy(session = session) }
                    Timber.d("Reading session started: ${session.id}")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to start reading session")
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                _uiState.update { state ->
                    state.copy(elapsedTimeMillis = state.elapsedTimeMillis + 1000)
                }
            }
        }
    }

    fun updateCurrentPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun endSession(onComplete: () -> Unit) {
        viewModelScope.launch {
            timerJob?.cancel()

            val session = _uiState.value.session
            val book = _uiState.value.book
            val currentPage = _uiState.value.currentPage

            if (session != null && book != null) {
                val pagesRead = currentPage - book.currentPage

                // End the session
                endReadingSessionUseCase(session.id, pagesRead)
                    .onSuccess {
                        // Update book's current page
                        updateBookUseCase(book.copy(currentPage = currentPage))
                        Timber.d("Reading session ended. Pages read: $pagesRead")
                        onComplete()
                    }
                    .onFailure { error ->
                        Timber.e(error, "Failed to end reading session")
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }

    fun showNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = true) }
    }

    fun hideNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, noteContent = "") }
    }

    fun updateNoteContent(content: String) {
        _uiState.update { it.copy(noteContent = content) }
    }

    fun saveNote() {
        viewModelScope.launch {
            val currentPage = _uiState.value.currentPage
            val noteContent = _uiState.value.noteContent

            if (noteContent.isNotBlank()) {
                addNoteUseCase(
                    bookId = bookId,
                    content = noteContent,
                    page = currentPage
                ).onSuccess {
                    Timber.d("Note saved successfully")
                    hideNoteDialog()
                }.onFailure { error ->
                    Timber.e(error, "Failed to save note")
                    _uiState.update { it.copy(error = error.message) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
