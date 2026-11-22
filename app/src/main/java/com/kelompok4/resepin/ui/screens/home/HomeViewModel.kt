package com.kelompok4.resepin.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.User
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val user: User? = null,
    val recentlyViewed: List<Recipe> = emptyList(),
    val latestRecipes: List<Recipe> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
)

class HomeViewModel(
    private val recipeRepository: RecipeRepository = AppContainer.recipeRepository,
    authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    // UI state diturunkan dari beberapa sumber sekaligus dengan combine + stateIn
    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        recipeRepository.recipes,
        recipeRepository.recentlyViewedIds,
        recipeRepository.favoriteIds,
    ) { user, recipes, recentIds, favIds ->
        val byId = recipes.associateBy { it.id }
        HomeUiState(
            user = user,
            recentlyViewed = recentIds.mapNotNull { byId[it] },
            latestRecipes = recipes.sortedByDescending { it.createdAt }.take(6),
            favoriteIds = favIds.toSet(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun toggleFavorite(recipeId: String) = recipeRepository.toggleFavorite(recipeId)

    fun clearHistory() = recipeRepository.clearRecentlyViewed()
}
