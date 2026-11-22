package com.kelompok4.resepin.ui.screens.playlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Playlist
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.User
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.PlaylistRepository
import com.kelompok4.resepin.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================== DAFTAR PLAYLIST ==============================
data class PlaylistListUiState(
    val user: User? = null,
    val playlists: List<Playlist> = emptyList(),
)

class PlaylistListViewModel(
    playlistRepository: PlaylistRepository = AppContainer.playlistRepository,
    authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {
    val uiState: StateFlow<PlaylistListUiState> =
        combine(authRepository.currentUser, playlistRepository.playlists) { user, playlists ->
            PlaylistListUiState(user, playlists)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaylistListUiState())
}

// ============================== FORM BUAT / EDIT PLAYLIST ==============================
data class PlaylistFormUiState(
    val isEdit: Boolean = false,
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

class PlaylistFormViewModel(
    private val playlistId: String?,
    private val repository: PlaylistRepository = AppContainer.playlistRepository,
) : ViewModel() {

    companion object {
        const val MIN_NAME = 3
        const val MAX_NAME = 50
    }

    var name by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(PlaylistFormUiState(isEdit = playlistId != null))
    val uiState: StateFlow<PlaylistFormUiState> = _uiState.asStateFlow()

    init {
        playlistId?.let { repository.getPlaylist(it) }?.let {
            name = it.name
            description = it.description
        }
    }

    fun onNameChange(value: String) {
        name = value.take(MAX_NAME)
        _uiState.update { it.copy(nameError = null) }
    }

    fun onDescriptionChange(value: String) {
        description = value.take(200)
    }

    fun save() {
        val trimmed = name.trim()
        val error = when {
            trimmed.length < MIN_NAME -> "Nama playlist minimal $MIN_NAME karakter"
            repository.isNameUsed(trimmed, playlistId) -> "Nama sudah dipakai playlist lain"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(nameError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (playlistId == null) repository.createPlaylist(trimmed, description)
            else repository.updatePlaylist(playlistId, trimmed, description)
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}

// ============================== ISI PLAYLIST ==============================
data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val recipes: List<Recipe> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
)

class PlaylistDetailViewModel(
    private val playlistId: String,
    private val playlistRepository: PlaylistRepository = AppContainer.playlistRepository,
    recipeRepository: RecipeRepository = AppContainer.recipeRepository,
) : ViewModel() {

    private data class Local(val showDeleteDialog: Boolean = false, val isDeleting: Boolean = false, val isDeleted: Boolean = false)

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        playlistRepository.playlists,
        recipeRepository.recipes,
        local,
    ) { playlists, recipes, l ->
        val playlist = playlists.firstOrNull { it.id == playlistId }
        val byId = recipes.associateBy { it.id }
        PlaylistDetailUiState(
            playlist = playlist,
            recipes = playlist?.recipeIds?.mapNotNull { byId[it] }.orEmpty(),
            showDeleteDialog = l.showDeleteDialog,
            isDeleting = l.isDeleting,
            isDeleted = l.isDeleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaylistDetailUiState())

    fun removeRecipe(recipeId: String) = playlistRepository.removeRecipe(playlistId, recipeId)

    fun undoRemove(recipeId: String) = playlistRepository.addRecipe(playlistId, recipeId)

    fun askDelete() = local.update { it.copy(showDeleteDialog = true) }

    fun dismissDelete() = local.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        viewModelScope.launch {
            local.update { it.copy(isDeleting = true) }
            playlistRepository.deletePlaylist(playlistId)
            local.update { Local(isDeleted = true) }
        }
    }
}

// ============================== PILIH RESEP ==============================
data class PickRecipeUiState(
    val playlistName: String = "",
    val recipes: List<Recipe> = emptyList(),
    val addedIds: Set<String> = emptySet(),
)

class PickRecipeViewModel(
    private val playlistId: String,
    private val playlistRepository: PlaylistRepository = AppContainer.playlistRepository,
    recipeRepository: RecipeRepository = AppContainer.recipeRepository,
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    private val queryFlow = MutableStateFlow("")

    val uiState: StateFlow<PickRecipeUiState> = combine(
        playlistRepository.playlists.map { list -> list.firstOrNull { it.id == playlistId } },
        recipeRepository.recipes,
        queryFlow,
    ) { playlist, recipes, q ->
        PickRecipeUiState(
            playlistName = playlist?.name.orEmpty(),
            recipes = recipes
                .filter { q.isBlank() || it.title.contains(q.trim(), ignoreCase = true) }
                .sortedByDescending { it.createdAt },
            addedIds = playlist?.recipeIds?.toSet().orEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PickRecipeUiState())

    fun onQueryChange(value: String) {
        query = value
        queryFlow.value = value
    }

    fun toggle(recipeId: String) {
        if (recipeId in uiState.value.addedIds) playlistRepository.removeRecipe(playlistId, recipeId)
        else playlistRepository.addRecipe(playlistId, recipeId)
    }
}
