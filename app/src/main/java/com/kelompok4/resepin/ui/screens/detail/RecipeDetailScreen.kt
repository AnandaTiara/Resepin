package com.kelompok4.resepin.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.navigation.DetailFrom
import com.kelompok4.resepin.navigation.DetailMode
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.AuthorAvatar
import com.kelompok4.resepin.ui.components.CategoryChip
import com.kelompok4.resepin.ui.components.CircleIconButton
import com.kelompok4.resepin.ui.components.CommentItem
import com.kelompok4.resepin.ui.components.ConfirmDialog
import com.kelompok4.resepin.ui.components.EmptyState
import com.kelompok4.resepin.ui.components.IngredientRow
import com.kelompok4.resepin.ui.components.NumberedStep
import com.kelompok4.resepin.ui.components.OutlineButton
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.RecipeGridCard
import com.kelompok4.resepin.ui.components.RecipeImage
import com.kelompok4.resepin.ui.components.RecipeInfoRow
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.components.SectionHeader
import com.kelompok4.resepin.ui.theme.Background
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Danger
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary

/** Event navigasi dari layar detail (dikirim naik ke NavHost). */
data class RecipeDetailActions(
    val onBack: () -> Unit,
    val onOpenFull: (String) -> Unit,
    val onStartCooking: (String) -> Unit,
    val onSeeComments: (String) -> Unit,
    val onRecipeClick: (Recipe) -> Unit,
    val onEdit: (String) -> Unit,
)

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    mode: String,
    from: String,
    actions: RecipeDetailActions,
    resultMessage: String? = null,
    onResultMessageShown: () -> Unit = {},
    viewModel: RecipeDetailViewModel = viewModel(key = "detail_$recipeId") { RecipeDetailViewModel(recipeId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.isDeleted, state.isRemovedFromFavorite) {
        if (state.isDeleted || state.isRemovedFromFavorite) actions.onBack()
    }
    LaunchedEffect(resultMessage) {
        if (resultMessage != null) {
            snackbar.showSnackbar(resultMessage)
            onResultMessageShown()
        }
    }

    RecipeDetailContent(
        state = state,
        mode = mode,
        from = from,
        snackbar = snackbar,
        actions = actions,
        onToggleFavorite = viewModel::toggleFavorite,
        onAskRemoveFavorite = viewModel::askRemoveFavorite,
        onAskDelete = viewModel::askDelete,
    )

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus resep?",
            message = "Resep beserta bahan, langkah, dan foto akan dihapus permanen.",
            confirmText = "Hapus",
            icon = Icons.Outlined.DeleteOutline,
            loading = state.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDialogs,
        )
    }
    if (state.showRemoveFavoriteDialog) {
        ConfirmDialog(
            title = "Hapus dari favorit?",
            message = "Resep “${state.recipe?.title}” akan dihapus dari daftar favoritmu.",
            confirmText = "Hapus",
            icon = Icons.Outlined.FavoriteBorder,
            onConfirm = viewModel::confirmRemoveFavorite,
            onDismiss = viewModel::dismissDialogs,
        )
    }
}

@Composable
fun RecipeDetailContent(
    state: RecipeDetailUiState,
    mode: String,
    from: String,
    snackbar: SnackbarHostState,
    actions: RecipeDetailActions,
    onToggleFavorite: (String) -> Unit,
    onAskRemoveFavorite: () -> Unit,
    onAskDelete: () -> Unit,
) {
    val recipe = state.recipe
    val isFavoriteVariant = from == DetailFrom.FAVORIT && mode == DetailMode.RINGKAS && !state.isOwner

    AppScaffold(
        snackbarHostState = snackbar,
        topBar = {
            ResepinTopBar("Detail Resep", onBack = actions.onBack) {
                if (recipe != null && state.isOwner) {
                    CircleIconButton(Icons.Outlined.Edit, "Edit resep", { actions.onEdit(recipe.id) }, tint = Primary, container = PrimaryContainer, iconSize = 20)
                    Spacer(Modifier.width(8.dp))
                    CircleIconButton(Icons.Outlined.DeleteOutline, "Hapus resep", onAskDelete, tint = Danger, container = PrimaryContainer, iconSize = 20)
                } else if (recipe != null) {
                    CircleIconButton(
                        icon = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (state.isFavorite) "Hapus dari favorit" else "Simpan ke favorit",
                        onClick = { onToggleFavorite(recipe.id) },
                        tint = Primary,
                        container = PrimaryContainer,
                    )
                }
            }
        },
        bottomBar = if (isFavoriteVariant && recipe != null) {
            {
                Column(Modifier.background(Background)) {
                    HorizontalDivider(color = Border)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { actions.onOpenFull(recipe.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Lihat Selengkapnya", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = Primary, modifier = Modifier.size(16.dp))
                        }
                        OutlineButton(
                            "Hapus dari Favorit",
                            onAskRemoveFavorite,
                            leadingIcon = Icons.Outlined.FavoriteBorder,
                        )
                    }
                }
            }
        } else null,
    ) { padding ->
        if (recipe == null) {
            if (!state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    EmptyState(Icons.Outlined.SearchOff, "Resep tidak ditemukan", "Resep ini mungkin sudah dihapus.")
                }
            }
            return@AppScaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            when {
                state.isOwner -> OwnerBody(recipe)
                mode == DetailMode.LENGKAP -> FullBody(state, recipe, actions, onToggleFavorite)
                isFavoriteVariant -> SummaryBody(recipe, numberedSteps = true)
                else -> {
                    SummaryBody(recipe, numberedSteps = false)
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("Lihat Selengkapnya", { actions.onOpenFull(recipe.id) }, Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// --------------------------------------------------------------------- Bagian-bagian layar

@Composable
private fun Header(recipe: Recipe, photoHeight: Int, ownerBadge: Boolean = false) {
    Spacer(Modifier.height(4.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(photoHeight.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        RecipeImage(recipe, Modifier.matchParentSize(), iconSize = 40.dp)
        if (ownerBadge) {
            Text(
                "Resep milikmu",
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Surface)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(recipe.title, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        AuthorAvatar(recipe.author, size = 24.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            if (ownerBadge) "oleh Kamu" else "oleh ${recipe.author.fullName}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (ownerBadge) Primary else TextSecondary,
        )
    }
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryChip(recipe.type.label, highlighted = true)
        CategoryChip(recipe.category, highlighted = ownerBadge)
    }
    Spacer(Modifier.height(16.dp))
    RecipeInfoRow(recipe)
}

@Composable
private fun ColumnScope.SummaryBody(recipe: Recipe, numberedSteps: Boolean) {
    Header(recipe, photoHeight = 160)
    Spacer(Modifier.height(16.dp))
    SectionHeader("Bahan", trailingInfo = if (numberedSteps) "${recipe.ingredients.size} bahan" else null)
    Spacer(Modifier.height(4.dp))
    recipe.ingredients.take(3).forEach { IngredientRow(it, compact = numberedSteps) }
    Spacer(Modifier.height(16.dp))
    SectionHeader("Ringkasan Langkah")
    Spacer(Modifier.height(8.dp))
    if (numberedSteps) {
        recipe.summarySteps.forEachIndexed { i, step ->
            NumberedStep(i + 1, step)
            Spacer(Modifier.height(8.dp))
        }
    } else {
        Text(
            recipe.summarySteps.mapIndexed { i, s -> "${i + 1}. $s" }.joinToString(" "),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun ColumnScope.OwnerBody(recipe: Recipe) {
    Header(recipe, photoHeight = 160, ownerBadge = true)
    Spacer(Modifier.height(16.dp))
    SectionHeader("Bahan", trailingInfo = "${recipe.ingredients.size} bahan")
    recipe.ingredients.forEach { IngredientRow(it, compact = true) }
    Spacer(Modifier.height(20.dp))
    SectionHeader("Langkah", trailingInfo = "${recipe.steps.size} langkah")
    Spacer(Modifier.height(10.dp))
    recipe.steps.forEachIndexed { i, step ->
        NumberedStep(i + 1, step)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ColumnScope.FullBody(
    state: RecipeDetailUiState,
    recipe: Recipe,
    actions: RecipeDetailActions,
    onToggleFavorite: (String) -> Unit,
) {
    Header(recipe, photoHeight = 232)
    Spacer(Modifier.height(20.dp))
    SectionHeader("Bahan")
    Spacer(Modifier.height(4.dp))
    recipe.ingredients.forEach { IngredientRow(it) }
    Spacer(Modifier.height(24.dp))
    PrimaryButton("Mulai Memasak", { actions.onStartCooking(recipe.id) }, Modifier.fillMaxWidth())
    Spacer(Modifier.height(24.dp))
    SectionHeader(
        "Komentar",
        actionText = "Lihat Semua (${state.commentCount})",
        onAction = { actions.onSeeComments(recipe.id) },
    )
    Spacer(Modifier.height(16.dp))
    if (state.latestComments.isEmpty()) {
        Text("Belum ada komentar. Jadilah yang pertama!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
    state.latestComments.forEach { comment ->
        CommentItem(
            comment,
            isMine = comment.author.id == state.myUserId,
            myAvatarUri = state.myAvatarUri,
            modifier = Modifier.clickable { actions.onSeeComments(recipe.id) },
        )
        Spacer(Modifier.height(16.dp))
    }
    if (state.suggestions.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        SectionHeader("Saran Resep")
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp),
        ) {
            items(state.suggestions, key = { it.id }) { suggestion ->
                RecipeGridCard(
                    recipe = suggestion,
                    isFavorite = suggestion.id in state.favoriteIds,
                    onClick = { actions.onRecipeClick(suggestion) },
                    onFavoriteClick = { onToggleFavorite(suggestion.id) },
                    showTypeBadge = false,
                    modifier = Modifier.width(148.dp),
                )
            }
        }
    }
}
