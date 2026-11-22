package com.kelompok4.resepin.ui.screens.community

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kelompok4.resepin.ui.components.EmptyState
import com.kelompok4.resepin.ui.components.RecipeGridCard
import com.kelompok4.resepin.ui.components.UserAvatar
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.SurfaceVariant
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// ============================== VIEWMODEL ==============================
enum class CommunityTab(val label: String) { SEMUA("Semua Resep"), SAYA("Resep Saya") }

data class CommunityUiState(
    val user: User? = null,
    val tab: CommunityTab = CommunityTab.SEMUA,
    val recipes: List<Recipe> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
)

class CommunityViewModel(
    private val recipeRepository: RecipeRepository = AppContainer.recipeRepository,
    authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(CommunityTab.SEMUA)

    val uiState: StateFlow<CommunityUiState> = combine(
        authRepository.currentUser,
        recipeRepository.recipes,
        recipeRepository.favoriteIds,
        selectedTab,
    ) { user, recipes, fav, tab ->
        val sorted = recipes.sortedByDescending { it.createdAt }
        CommunityUiState(
            user = user,
            tab = tab,
            recipes = when (tab) {
                CommunityTab.SEMUA -> sorted.filter { it.author.id != user?.id }
                CommunityTab.SAYA -> sorted.filter { it.author.id == user?.id }
            },
            favoriteIds = fav.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommunityUiState())

    fun selectTab(tab: CommunityTab) {
        selectedTab.value = tab
    }

    fun toggleFavorite(id: String) = recipeRepository.toggleFavorite(id)
}

// ============================== SCREEN ==============================
@Composable
fun CommunityScreen(
    onRecipeClick: (Recipe) -> Unit,
    onAddRecipe: () -> Unit,
    onProfileClick: () -> Unit,
    resultMessage: String? = null,
    onResultMessageShown: () -> Unit = {},
    viewModel: CommunityViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(resultMessage) {
        if (resultMessage != null) {
            // Setelah menambah resep, langsung tampilkan tab "Resep Saya"
            viewModel.selectTab(CommunityTab.SAYA)
            onResultMessageShown()
            snackbar.showSnackbar(resultMessage)
        }
    }
    CommunityContent(
        state = state,
        snackbar = snackbar,
        onTabChange = viewModel::selectTab,
        onRecipeClick = onRecipeClick,
        onFavoriteClick = { viewModel.toggleFavorite(it.id) },
        onAddRecipe = onAddRecipe,
        onProfileClick = onProfileClick,
    )
}

@Composable
fun CommunityContent(
    state: CommunityUiState,
    snackbar: SnackbarHostState,
    onTabChange: (CommunityTab) -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Recipe) -> Unit,
    onAddRecipe: () -> Unit,
    onProfileClick: () -> Unit,
) {
    AppScaffold(
        snackbarHostState = snackbar,
        topBar = {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Komunitas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    UserAvatar(state.user, Modifier.clip(CircleShape).clickable(onClick = onProfileClick), size = 40.dp)
                }
                SegmentedTabs(state.tab, onTabChange)
                Spacer(Modifier.height(8.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipe,
                containerColor = Primary,
                contentColor = Surface,
                shape = RoundedCornerShape(18.dp),
            ) { Icon(Icons.Filled.Add, "Tambah resep", modifier = Modifier.size(28.dp)) }
        },
    ) { padding ->
        if (state.recipes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                EmptyState(Icons.AutoMirrored.Outlined.MenuBook, "Belum ada resep", "Ketuk tombol + untuk membagikan resep pertamamu.")
            }
            return@AppScaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.tab == CommunityTab.SAYA) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("${state.recipes.size} resep milikmu", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            items(state.recipes, key = { it.id }) { recipe ->
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

@Composable
private fun SegmentedTabs(selected: CommunityTab, onSelect: (CommunityTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceVariant)
            .padding(4.dp),
    ) {
        CommunityTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Text(
                tab.label,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Primary else SurfaceVariant)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 9.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Surface else TextPrimary,
            )
        }
    }
}
