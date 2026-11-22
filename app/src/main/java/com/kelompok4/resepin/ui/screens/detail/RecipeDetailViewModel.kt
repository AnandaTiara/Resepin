package com.kelompok4.resepin.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Comment
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.CommentRepository
import com.kelompok4.resepin.data.repository.PlaylistRepository
import com.kelompok4.resepin.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    val isFavorite: Boolean = false,
    val isOwner: Boolean = false,
    val latestComments: List<Comment> = emptyList(),
    val commentCount: Int = 0,
    val suggestions: List<Recipe> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val myUserId: String? = null,
    val myAvatarUri: String? = null,
    val showDeleteDialog: Boolean = false,
    val showRemoveFavoriteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val isRemovedFromFavorite: Boolean = false,
)

class RecipeDetailViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository = AppContainer.recipeRepository,
    commentRepository: CommentRepository = AppContainer.commentRepository,
    private val playlistRepository: PlaylistRepository = AppContainer.playlistRepository,
    authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    /** State lokal milik layar ini (dialog & proses hapus). */
    private data class LocalState(
        val showDeleteDialog: Boolean = false,
        val showRemoveFavoriteDialog: Boolean = false,
        val isDeleting: Boolean = false,
        val isDeleted: Boolean = false,
        val isRemovedFromFavorite: Boolean = false,
    )

    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        recipeRepository.recipes,
        recipeRepository.favoriteIds,
        commentRepository.comments,
        authRepository.currentUser,
        local,
    ) { recipes, favIds, comments, user, l ->
        val recipe = recipes.firstOrNull { it.id == recipeId }
        val recipeComments = comments.filter { it.recipeId == recipeId }.sortedByDescending { it.createdAt }
        RecipeDetailUiState(
            isLoading = false,
            recipe = recipe,
            isFavorite = recipeId in favIds,
            isOwner = recipe != null && user != null && recipe.author.id == user.id,
            latestComments = recipeComments.take(2),
            commentCount = recipeComments.size,
            suggestions = recipes
                .filter { it.id != recipeId && it.author.id != user?.id && it.type == recipe?.type }
                .take(5),
            favoriteIds = favIds.toSet(),
            myUserId = user?.id,
            myAvatarUri = user?.avatarUri,
            showDeleteDialog = l.showDeleteDialog,
            showRemoveFavoriteDialog = l.showRemoveFavoriteDialog,
            isDeleting = l.isDeleting,
            isDeleted = l.isDeleted,
            isRemovedFromFavorite = l.isRemovedFromFavorite,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailUiState())

    init {
        recipeRepository.markViewed(recipeId)
    }

    fun toggleFavorite(id: String = recipeId) = recipeRepository.toggleFavorite(id)

    fun askRemoveFavorite() = local.update { it.copy(showRemoveFavoriteDialog = true) }

    fun confirmRemoveFavorite() {
        recipeRepository.removeFavorite(recipeId)
        local.update { it.copy(showRemoveFavoriteDialog = false, isRemovedFromFavorite = true) }
    }

    fun askDelete() = local.update { it.copy(showDeleteDialog = true) }

    fun dismissDialogs() = local.update { it.copy(showDeleteDialog = false, showRemoveFavoriteDialog = false) }

    fun confirmDelete() {
        viewModelScope.launch {
            local.update { it.copy(isDeleting = true) }
            recipeRepository.deleteRecipe(recipeId)
            playlistRepository.removeRecipeEverywhere(recipeId)
            local.update { it.copy(isDeleting = false, showDeleteDialog = false, isDeleted = true) }
        }
    }
}
