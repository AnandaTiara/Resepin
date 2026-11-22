package com.kelompok4.resepin.ui.screens.comment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Comment
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommentUiState(
    val comments: List<Comment> = emptyList(),
    val myUserId: String? = null,
    val myAvatarUri: String? = null,
    val editingId: String? = null,
    val pendingDelete: Comment? = null,
    val isSending: Boolean = false,
)

class CommentViewModel(
    private val recipeId: String,
    private val commentRepository: CommentRepository = AppContainer.commentRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    companion object {
        const val MAX_LENGTH = 500
    }

    /** Teks input komentar (divalidasi panjangnya), disimpan dengan mutableStateOf. */
    var input by mutableStateOf("")
        private set

    private data class Local(val editingId: String? = null, val pendingDelete: Comment? = null, val isSending: Boolean = false)

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<CommentUiState> = combine(
        commentRepository.comments,
        authRepository.currentUser,
        local,
    ) { comments, user, l ->
        CommentUiState(
            comments = comments.filter { it.recipeId == recipeId }.sortedByDescending { it.createdAt },
            myUserId = user?.id,
            myAvatarUri = user?.avatarUri,
            editingId = l.editingId,
            pendingDelete = l.pendingDelete,
            isSending = l.isSending,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommentUiState())

    val canSend: Boolean get() = input.isNotBlank() && input.length <= MAX_LENGTH

    fun onInputChange(value: String) {
        input = value.take(MAX_LENGTH)
    }

    fun startEdit(comment: Comment) {
        input = comment.text
        local.update { it.copy(editingId = comment.id) }
    }

    fun cancelEdit() {
        input = ""
        local.update { it.copy(editingId = null) }
    }

    fun send() {
        if (!canSend) return
        val text = input
        val editingId = local.value.editingId
        viewModelScope.launch {
            local.update { it.copy(isSending = true) }
            if (editingId != null) commentRepository.editComment(editingId, text)
            else commentRepository.addComment(recipeId, authRepository.currentAuthor(), text)
            input = ""
            local.update { it.copy(isSending = false, editingId = null) }
        }
    }

    fun askDelete(comment: Comment) = local.update { it.copy(pendingDelete = comment) }

    fun dismissDelete() = local.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val target = local.value.pendingDelete ?: return
        viewModelScope.launch {
            commentRepository.deleteComment(target.id)
            if (local.value.editingId == target.id) cancelEdit()
            local.update { it.copy(pendingDelete = null) }
        }
    }
}
