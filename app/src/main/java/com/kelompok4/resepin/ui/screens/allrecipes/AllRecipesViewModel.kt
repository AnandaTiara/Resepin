package com.kelompok4.resepin.ui.screens.allrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.repository.RecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllRecipesUiState(
    val newestFirst: Boolean = true,
    val recipes: List<Recipe> = emptyList(),
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
)

class AllRecipesViewModel(
    private val repository: RecipeRepository = AppContainer.recipeRepository,
) : ViewModel() {

    companion object {
        const val PAGE_SIZE = 6
    }

    private data class PagingState(
        val newestFirst: Boolean = true,
        val recipes: List<Recipe> = emptyList(),
        val page: Int = 0,
        val isLoadingMore: Boolean = false,
        val endReached: Boolean = false,
        val errorMessage: String? = null,
    )

    private val paging = MutableStateFlow(PagingState())
    private var loadJob: Job? = null

    val uiState: StateFlow<AllRecipesUiState> = combine(paging, repository.favoriteIds) { p, fav ->
        AllRecipesUiState(p.newestFirst, p.recipes, p.isLoadingMore, p.endReached, fav.toSet(), p.errorMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AllRecipesUiState(isLoadingMore = true))

    init {
        loadNextPage()
    }

    fun setSort(newestFirst: Boolean) {
        if (paging.value.newestFirst == newestFirst) return
        loadJob?.cancel()
        paging.value = PagingState(newestFirst = newestFirst)
        loadNextPage()
    }

    /** Dipanggil saat pengguna menggulir sampai item terakhir. */
    fun loadNextPage() {
        val current = paging.value
        if (current.isLoadingMore && loadJob?.isActive == true) return
        if (current.endReached) return
        loadJob = viewModelScope.launch {
            paging.update { it.copy(isLoadingMore = true, errorMessage = null) }
            try {
                val next = repository.loadPage(current.page, PAGE_SIZE, current.newestFirst)
                paging.update {
                    it.copy(
                        recipes = it.recipes + next,
                        page = it.page + 1,
                        isLoadingMore = false,
                        endReached = next.size < PAGE_SIZE,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                paging.update { it.copy(isLoadingMore = false, errorMessage = "Gagal memuat resep") }
            }
        }
    }

    fun toggleFavorite(id: String) = repository.toggleFavorite(id)
}
