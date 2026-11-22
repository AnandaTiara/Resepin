package com.kelompok4.resepin.ui.screens.favorite

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.User
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.RecipeRepository
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.ConfirmDialog
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.RecipeGridCard
import com.kelompok4.resepin.ui.components.UserAvatar
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.GradientEnd
import com.kelompok4.resepin.ui.theme.GradientStart
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// ============================== VIEWMODEL ==============================
data class FavoriteUiState(
    val user: User? = null,
    val recipes: List<Recipe> = emptyList(),
    val newestFirst: Boolean = true,
    val pendingRemove: Recipe? = null,
)

class FavoriteViewModel(
    private val recipeRepository: RecipeRepository = AppContainer.recipeRepository,
    authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private data class Local(val newestFirst: Boolean = true, val pendingRemove: Recipe? = null)

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<FavoriteUiState> = combine(
        authRepository.currentUser,
        recipeRepository.recipes,
        recipeRepository.favoriteIds,
        local,
    ) { user, recipes, favIds, l ->
        val byId = recipes.associateBy { it.id }
        val ordered = favIds.mapNotNull { byId[it] } // favIds sudah urut terbaru disimpan
        FavoriteUiState(user, if (l.newestFirst) ordered else ordered.reversed(), l.newestFirst, l.pendingRemove)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoriteUiState())

    fun toggleSort() = local.update { it.copy(newestFirst = !it.newestFirst) }

    fun askRemove(recipe: Recipe) = local.update { it.copy(pendingRemove = recipe) }

    fun dismissRemove() = local.update { it.copy(pendingRemove = null) }

    fun confirmRemove() {
        local.value.pendingRemove?.let { recipeRepository.removeFavorite(it.id) }
        dismissRemove()
    }
}

// ============================== SCREEN ==============================
@Composable
fun FavoriteScreen(
    onRecipeClick: (Recipe) -> Unit,
    onExplore: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: FavoriteViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoriteContent(state, onRecipeClick, viewModel::askRemove, viewModel::toggleSort, onExplore, onProfileClick)
    state.pendingRemove?.let { recipe ->
        ConfirmDialog(
            title = "Hapus dari favorit?",
            message = "Resep “${recipe.title}” akan dihapus dari daftar favoritmu.",
            confirmText = "Hapus",
            icon = Icons.Outlined.FavoriteBorder,
            onConfirm = viewModel::confirmRemove,
            onDismiss = viewModel::dismissRemove,
        )
    }
}

@Composable
fun FavoriteContent(
    state: FavoriteUiState,
    onRecipeClick: (Recipe) -> Unit,
    onRemoveClick: (Recipe) -> Unit,
    onToggleSort: () -> Unit,
    onExplore: () -> Unit,
    onProfileClick: () -> Unit,
) {
    AppScaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Favorit", style = MaterialTheme.typography.headlineSmall)
                    Text("${state.recipes.size} resep tersimpan", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                UserAvatar(state.user, Modifier.clip(CircleShape).clickable(onClick = onProfileClick), size = 40.dp, showPhoto = false)
            }
        },
    ) { padding ->
        if (state.recipes.isEmpty()) {
            EmptyFavorite(Modifier.fillMaxSize().padding(padding), onExplore)
            return@AppScaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row {
                    val shape = RoundedCornerShape(50)
                    Row(
                        Modifier
                            .clip(shape)
                            .background(Surface)
                            .border(1.dp, Border, shape)
                            .clickable(onClick = onToggleSort)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, null, tint = Primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (state.newestFirst) "Terbaru disimpan" else "Terlama disimpan",
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            items(state.recipes, key = { it.id }) { recipe ->
                RecipeGridCard(
                    recipe = recipe,
                    isFavorite = true,
                    onClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onRemoveClick(recipe) },
                )
            }
        }
    }
}

@Composable
private fun EmptyFavorite(modifier: Modifier, onExplore: () -> Unit) {
    Box(modifier.padding(horizontal = 40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 60.dp)) {
            Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                // Lingkaran berlapis + titik dekorasi, sesuai ilustrasi empty state di desain
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(PrimaryContainer, radius = size.minDimension / 2.6f)
                    drawCircle(GradientStart.copy(alpha = 0.45f), radius = size.minDimension / 3.6f)
                    drawCircle(GradientEnd, radius = 5.dp.toPx(), center = Offset(size.width * 0.86f, size.height * 0.16f))
                    drawCircle(GradientEnd.copy(alpha = 0.7f), radius = 3.dp.toPx(), center = Offset(size.width * 0.08f, size.height * 0.64f))
                    drawCircle(GradientEnd.copy(alpha = 0.7f), radius = 2.5.dp.toPx(), center = Offset(size.width * 0.78f, size.height * 0.80f))
                }
                Icon(Icons.Outlined.FavoriteBorder, null, tint = Primary, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Belum ada resep favorit", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Ketuk ikon hati pada resep untuk menyimpannya di sini.",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Jelajahi Resep", onExplore, Modifier.width(200.dp))
        }
    }
}
