package com.kelompok4.resepin.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.RecipeGridCard
import com.kelompok4.resepin.ui.components.SectionHeader
import com.kelompok4.resepin.ui.components.UserAvatar
import com.kelompok4.resepin.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit,
    onSeeAllClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onRecipeClick = onRecipeClick,
        onFavoriteClick = { viewModel.toggleFavorite(it.id) },
        onClearHistory = viewModel::clearHistory,
        onSeeAllClick = onSeeAllClick,
        onProfileClick = onProfileClick,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Recipe) -> Unit,
    onClearHistory: () -> Unit,
    onSeeAllClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    AppScaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Halo, ${state.user?.firstName ?: ""}", style = MaterialTheme.typography.headlineSmall)
                    Text("Mau masak apa hari ini?", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                UserAvatar(
                    state.user,
                    size = 40.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick),
                )
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
            // ---------- Terakhir Dilihat ----------
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    "Terakhir Dilihat",
                    actionText = if (state.recentlyViewed.isNotEmpty()) "Hapus Riwayat" else null,
                    onAction = onClearHistory,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                if (state.recentlyViewed.isEmpty()) {
                    Text(
                        "Belum ada resep yang dilihat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(state.recentlyViewed, key = { it.id }) { recipe ->
                            RecipeGridCard(
                                recipe = recipe,
                                isFavorite = recipe.id in state.favoriteIds,
                                onClick = { onRecipeClick(recipe) },
                                onFavoriteClick = { onFavoriteClick(recipe) },
                                modifier = Modifier.width(158.dp),
                            )
                        }
                    }
                }
            }
            // ---------- Resep Terbaru ----------
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader("Resep Terbaru", actionText = "Lihat Semua", onAction = onSeeAllClick)
                }
            }
            items(state.latestRecipes, key = { "latest_${it.id}" }) { recipe ->
                RecipeGridCard(
                    recipe = recipe,
                    isFavorite = recipe.id in state.favoriteIds,
                    onClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoriteClick(recipe) },
                )
            }
        }
    }
}
