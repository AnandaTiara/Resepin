package com.kelompok4.resepin.ui.screens.playlist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.Playlist
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.ButtonSize
import com.kelompok4.resepin.ui.components.CircleIconButton
import com.kelompok4.resepin.ui.components.ConfirmDialog
import com.kelompok4.resepin.ui.components.EmptyState
import com.kelompok4.resepin.ui.components.LabeledTextField
import com.kelompok4.resepin.ui.components.OutlineButton
import com.kelompok4.resepin.ui.components.PlaylistCard
import com.kelompok4.resepin.ui.components.PlaylistListItem
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.RecipeListRow
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.components.SearchField
import com.kelompok4.resepin.ui.components.TonalButton
import com.kelompok4.resepin.ui.components.UserAvatar
import com.kelompok4.resepin.ui.theme.Success
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary
import kotlinx.coroutines.launch

// ============================== DAFTAR PLAYLIST ==============================
@Composable
fun PlaylistListScreen(
    onCreate: () -> Unit,
    onOpen: (Playlist) -> Unit,
    onProfileClick: () -> Unit,
    resultMessage: String? = null,
    onResultMessageShown: () -> Unit = {},
    viewModel: PlaylistListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(resultMessage) {
        if (resultMessage != null) {
            onResultMessageShown()
            snackbar.showSnackbar(resultMessage)
        }
    }
    PlaylistListContent(state, snackbar, onCreate, onOpen, onProfileClick)
}

@Composable
fun PlaylistListContent(
    state: PlaylistListUiState,
    snackbar: SnackbarHostState,
    onCreate: () -> Unit,
    onOpen: (Playlist) -> Unit,
    onProfileClick: () -> Unit,
) {
    AppScaffold(
        snackbarHostState = snackbar,
        topBar = {
            Row(
                Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Playlist", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                UserAvatar(state.user, Modifier.clip(CircleShape).clickable(onClick = onProfileClick), size = 40.dp, showPhoto = false)
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TonalButton("Buat Playlist", onCreate, Modifier.fillMaxWidth().padding(horizontal = 16.dp), leadingIcon = Icons.Outlined.Add)
            }
            if (state.playlists.isEmpty()) {
                item {
                    EmptyState(
                        Icons.AutoMirrored.Outlined.QueueMusic, "Belum ada playlist",
                        "Kumpulkan resep favoritmu ke dalam playlist.",
                        Modifier.padding(32.dp),
                    )
                }
                return@LazyColumn
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Playlist Saya", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.playlists, key = { it.id }) { p -> PlaylistCard(p, { onOpen(p) }) }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text("Semua Playlist", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            }
            items(state.playlists, key = { "all_${it.id}" }) { p ->
                PlaylistListItem(p, { onOpen(p) }, Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

// ============================== FORM BUAT / EDIT PLAYLIST ==============================
@Composable
fun PlaylistFormScreen(
    playlistId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: PlaylistFormViewModel = viewModel(key = "pl_form_${playlistId ?: "new"}") { PlaylistFormViewModel(playlistId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved(if (state.isEdit) "Playlist diperbarui" else "Playlist berhasil dibuat")
    }
    AppScaffold(topBar = { ResepinTopBar(if (state.isEdit) "Edit Playlist" else "Buat Playlist", onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            LabeledTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                label = "Nama playlist",
                placeholder = "Contoh: Menu Sahur",
                error = state.nameError,
                supportingText = "3 sampai 50 karakter, tidak boleh sama dengan playlist lain",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = viewModel.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Deskripsi (opsional)",
                placeholder = "Ceritakan isi playlist ini",
                singleLine = false,
                minHeight = 116.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(44.dp))
            PrimaryButton("Simpan", viewModel::save, Modifier.fillMaxWidth(), loading = state.isSaving)
            Spacer(Modifier.height(20.dp))
            OutlineButton("Batal", onBack, Modifier.fillMaxWidth())
        }
    }
}

// ============================== ISI PLAYLIST ==============================
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onAddRecipe: (String) -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    resultMessage: String? = null,
    onResultMessageShown: () -> Unit = {},
    viewModel: PlaylistDetailViewModel = viewModel(key = "pl_$playlistId") { PlaylistDetailViewModel(playlistId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }
    LaunchedEffect(resultMessage) {
        if (resultMessage != null) {
            snackbar.showSnackbar(resultMessage)
            onResultMessageShown()
        }
    }

    val playlist = state.playlist
    AppScaffold(
        snackbarHostState = snackbar,
        topBar = {
            ResepinTopBar(
                title = playlist?.name.orEmpty(),
                subtitle = "${state.recipes.size} resep",
                onBack = onBack,
            ) {
                CircleIconButton(Icons.Outlined.Edit, "Edit playlist", { onEdit(playlistId) }, iconSize = 22)
                CircleIconButton(Icons.Outlined.DeleteOutline, "Hapus playlist", viewModel::askDelete, iconSize = 22)
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            playlist?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                item { Text(desc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
            }
            item {
                TonalButton("Tambah Resep", { onAddRecipe(playlistId) }, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.Add)
            }
            if (state.recipes.isEmpty()) {
                item {
                    Text(
                        "Playlist ini masih kosong. Tambahkan resep pertamamu!",
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(state.recipes, key = { it.id }) { recipe ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RecipeListRow(recipe, Modifier.weight(1f), onClick = { onRecipeClick(recipe) })
                    Spacer(Modifier.width(4.dp))
                    CircleIconButton(
                        Icons.Outlined.DeleteOutline, "Keluarkan dari playlist",
                        onClick = {
                            viewModel.removeRecipe(recipe.id)
                            scope.launch {
                                val result = snackbar.showSnackbar(
                                    "${recipe.title} dikeluarkan", actionLabel = "Urungkan", duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) viewModel.undoRemove(recipe.id)
                            }
                        },
                        tint = TextPrimary, iconSize = 22,
                    )
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus playlist?",
            message = "Playlist \"${playlist?.name}\" beserta seluruh isinya akan dihapus.",
            confirmText = "Hapus",
            loading = state.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete,
        )
    }
}

// ============================== PILIH RESEP ==============================
@Composable
fun PickRecipeScreen(
    playlistId: String,
    onBack: () -> Unit,
    viewModel: PickRecipeViewModel = viewModel(key = "pick_$playlistId") { PickRecipeViewModel(playlistId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppScaffold(topBar = { ResepinTopBar("Tambah ke ${state.playlistName}", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "Cari resep",
                filled = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.recipes, key = { it.id }) { recipe ->
                    val added = recipe.id in state.addedIds
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RecipeListRow(recipe, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        if (added) {
                            Row(
                                Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.toggle(recipe.id) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.Check, null, tint = Success, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ada", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Success)
                            }
                        } else {
                            OutlineButton("Tambah", { viewModel.toggle(recipe.id) }, size = ButtonSize.Medium)
                        }
                    }
                }
                if (state.recipes.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Resep tidak ditemukan", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
