package com.kelompok4.resepin.ui.screens.allrecipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.CircleIconButton
import com.kelompok4.resepin.ui.components.PillChip
import com.kelompok4.resepin.ui.components.RecipeGridCard
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.TextSecondary

@Composable
fun AllRecipesScreen(
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    viewModel: AllRecipesViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AllRecipesContent(
        state = state,
        onBack = onBack,
        onSearchClick = onSearchClick,
        onSortChange = viewModel::setSort,
        onRecipeClick = onRecipeClick,
        onFavoriteClick = { viewModel.toggleFavorite(it.id) },
        onLoadMore = viewModel::loadNextPage,
    )
}

@Composable
fun AllRecipesContent(
    state: AllRecipesUiState,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onSortChange: (Boolean) -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Recipe) -> Unit,
    onLoadMore: () -> Unit,
) {
    AppScaffold(
        topBar = {
            ResepinTopBar("Semua Resep", onBack = onBack) {
                CircleIconButton(Icons.Outlined.Search, "Cari", onSearchClick, container = PrimaryContainer, iconSize = 22)
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillChip("Terbaru", state.newestFirst, { onSortChange(true) }, height = 34)
                    PillChip("Terlama", !state.newestFirst, { onSortChange(false) }, height = 34)
                }
            }
            itemsIndexed(state.recipes, key = { _, r -> r.id }) { index, recipe ->
                // Infinite scroll: muat halaman berikutnya ketika item terakhir tampil
                if (index == state.recipes.lastIndex) {
                    LaunchedEffect(state.recipes.size) { onLoadMore() }
                }
                RecipeGridCard(
                    recipe = recipe,
                    isFavorite = recipe.id in state.favoriteIds,
                    onClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoriteClick(recipe) },
                )
            }
            if (state.isLoadingMore || state.errorMessage != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            state.errorMessage ?: "Memuat ${AllRecipesViewModel.PAGE_SIZE} resep berikutnya...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}
