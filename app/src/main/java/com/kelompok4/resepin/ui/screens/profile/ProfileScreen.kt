package com.kelompok4.resepin.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.ui.components.Avatar
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.LabeledTextField
import com.kelompok4.resepin.ui.components.OutlineButton
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.screens.auth.isValidEmail
import com.kelompok4.resepin.ui.theme.Background
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================== VIEWMODEL ==============================
data class ProfileUiState(
    val avatarRes: Int? = null,
    val avatarUri: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val isLoggedOut: Boolean = false,
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val user = authRepository.currentUser.value

    var name by mutableStateOf(user?.name.orEmpty())
        private set
    var email by mutableStateOf(user?.email.orEmpty())
        private set

    private val _uiState = MutableStateFlow(ProfileUiState(avatarRes = user?.avatarRes, avatarUri = user?.avatarUri))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) {
        name = v.take(50)
        _uiState.update { it.copy(nameError = null) }
    }

    fun onEmailChange(v: String) {
        email = v
        _uiState.update { it.copy(emailError = null) }
    }

    fun onPhotoPicked(uri: String) = _uiState.update { it.copy(avatarUri = uri) }

    fun save() {
        val nameError = if (name.trim().length < 3) "Nama minimal 3 karakter" else null
        val emailError = if (!email.isValidEmail()) "Format email tidak valid" else null
        if (nameError != null || emailError != null) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            authRepository.updateProfile(name, email, _uiState.value.avatarUri)
            _uiState.update { it.copy(isSaving = false, savedMessage = "Perubahan profil disimpan") }
        }
    }

    fun messageShown() = _uiState.update { it.copy(savedMessage = null) }

    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}

// ============================== SCREEN ==============================
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.isLoggedOut) { if (state.isLoggedOut) onLoggedOut() }
    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.messageShown()
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoPicked(it.toString()) }
    }
    val pickPhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    AppScaffold(snackbarHostState = snackbar, topBar = { ResepinTopBar("Profil", onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.size(104.dp).clickable(onClick = pickPhoto)) {
                Avatar(viewModel.name, size = 104.dp, imageRes = state.avatarRes, imageUri = state.avatarUri)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(3.dp, Background, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.PhotoCamera, "Ubah foto", tint = Surface, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Ubah Foto Profil",
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Primary,
                modifier = Modifier.clickable(onClick = pickPhoto),
            )
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = viewModel.name, onValueChange = viewModel::onNameChange, label = "Nama",
                placeholder = "Nama kamu", error = state.nameError, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = viewModel.email, onValueChange = viewModel::onEmailChange, label = "Email",
                placeholder = "nama@email.com", keyboardType = KeyboardType.Email, error = state.emailError,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Simpan Perubahan", viewModel::save, Modifier.fillMaxWidth(), loading = state.isSaving)
            Spacer(Modifier.height(12.dp))
            OutlineButton("Keluar", viewModel::logout, Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
        }
    }
}
