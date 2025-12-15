package com.foliolib.app.presentation.screen.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.Note
import com.foliolib.app.domain.usecase.book.GetBookDetailsUseCase
import com.foliolib.app.domain.usecase.reading.AddNoteUseCase
import com.foliolib.app.domain.usecase.reading.GetNotesForBookUseCase
import com.foliolib.app.domain.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class NotesUiState(
    val bookTitle: String = "",
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val newNoteContent: String = "",
    val newNotePage: String = "",
    val newNoteChapter: String = ""
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val getNotesForBookUseCase: GetNotesForBookUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val readingRepository: ReadingRepository
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadBook()
        loadNotes()
    }

    private fun loadBook() {
        viewModelScope.launch {
            getBookDetailsUseCase(bookId).collect { book ->
                _uiState.update { state ->
                    state.copy(
                        bookTitle = book?.title ?: "",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            getNotesForBookUseCase(bookId).collect { notes ->
                _uiState.update { it.copy(notes = notes.sortedByDescending { note -> note.createdAt }) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                newNoteContent = "",
                newNotePage = "",
                newNoteChapter = ""
            )
        }
    }

    fun updateNoteContent(content: String) {
        _uiState.update { it.copy(newNoteContent = content) }
    }

    fun updateNotePage(page: String) {
        _uiState.update { it.copy(newNotePage = page) }
    }

    fun updateNoteChapter(chapter: String) {
        _uiState.update { it.copy(newNoteChapter = chapter) }
    }

    fun addNote() {
        viewModelScope.launch {
            val content = _uiState.value.newNoteContent
            val page = _uiState.value.newNotePage.toIntOrNull()
            val chapter = _uiState.value.newNoteChapter.takeIf { it.isNotBlank() }

            if (content.isNotBlank()) {
                addNoteUseCase(
                    bookId = bookId,
                    content = content,
                    page = page,
                    chapter = chapter
                ).onSuccess {
                    Timber.d("Note added successfully")
                    hideAddDialog()
                }.onFailure { error ->
                    Timber.e(error, "Failed to add note")
                    _uiState.update { it.copy(error = error.message) }
                }
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            readingRepository.deleteNote(noteId)
                .onSuccess {
                    Timber.d("Note deleted successfully")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to delete note")
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
