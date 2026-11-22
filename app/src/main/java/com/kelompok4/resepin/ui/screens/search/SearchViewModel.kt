package com.kelompok4.resepin.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.DishType
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.SearchFilter
import com.kelompok4.resepin.data.model.TimeFilter
import com.kelompok4.resepin.data.repository.RecipeRepository
import com.kelompok4.resepin.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SearchUiState(
    val filter: SearchFilter = SearchFilter(),
    val saveAsDefault: Boolean = false,
    val history: List<String> = emptyList(),
    val showResults: Boolean = false,
    val results: List<Recipe> = emptyList(),
)

class SearchViewModel(
    private val recipeRepository: RecipeRepository = AppContainer.recipeRepository,
    private val searchRepository: SearchRepository = AppContainer.searchRepository,
) : ViewModel() {

    /** Teks kolom pencarian. */
    var query by mutableStateOf("")
        private set

    private data class Local(
        val filter: SearchFilter,
        val saveAsDefault: Boolean,
        val showResults: Boolean = false,
        val submittedQuery: String = "",
    )

    private val local = MutableStateFlow(
        searchRepository.defaultFilter.value.let { Local(filter = it ?: SearchFilter(), saveAsDefault = it != null) },
    )

    val uiState: StateFlow<SearchUiState> = combine(
        recipeRepository.recipes,
        searchRepository.history,
        local,
    ) { recipes, history, l ->
        SearchUiState(
            filter = l.filter,
            saveAsDefault = l.saveAsDefault,
            history = history,
            showResults = l.showResults,
            results = if (l.showResults) filterRecipes(recipes, l.submittedQuery, l.filter) else emptyList(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    private fun filterRecipes(recipes: List<Recipe>, q: String, filter: SearchFilter): List<Recipe> {
        val keyword = q.trim().lowercase()
        return recipes
            .filter { r ->
                keyword.isEmpty() ||
                    r.title.lowercase().contains(keyword) ||
                    r.category.lowercase().contains(keyword) ||
                    r.type.label.lowercase().contains(keyword) ||
                    r.ingredients.any { it.name.lowercase().contains(keyword) }
            }
            .filter { filter.type == null || it.type == filter.type }
            .filter { r -> filter.time.maxMinutes?.let { r.timeMinutes <= it } ?: true }
            .distinctBy { it.title + it.author.id }
    }

    fun onQueryChange(value: String) {
        query = value
        // Saat sudah di halaman hasil, hasil langsung ikut berubah ketika mengetik.
        local.update { if (it.showResults) it.copy(submittedQuery = value) else it }
    }

    fun submit() {
        searchRepository.addHistory(query)
        local.update { it.copy(showResults = true, submittedQuery = query) }
    }

    fun searchFromHistory(text: String) {
        query = text
        submit()
    }

    fun backToFilters() = local.update { it.copy(showResults = false) }

    fun clearQuery() {
        query = ""
        local.update { it.copy(showResults = false, submittedQuery = "") }
    }

    fun setType(type: DishType?) = updateFilter { it.copy(type = type) }

    fun setTime(time: TimeFilter) = updateFilter { it.copy(time = time) }

    fun resetFilter() = updateFilter { SearchFilter() }

    private fun updateFilter(transform: (SearchFilter) -> SearchFilter) {
        local.update { it.copy(filter = transform(it.filter)) }
        if (local.value.saveAsDefault) searchRepository.saveDefaultFilter(local.value.filter)
    }

    fun setSaveAsDefault(enabled: Boolean) {
        local.update { it.copy(saveAsDefault = enabled) }
        searchRepository.saveDefaultFilter(if (enabled) local.value.filter else null)
    }

    fun removeHistory(text: String) = searchRepository.removeHistory(text)

    fun clearHistory() = searchRepository.clearHistory()
}
