package com.kelompok4.resepin.ui.screens.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(trim()).matches()

// ============================== LOGIN ==============================
data class LoginUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    // Teks TextField yang divalidasi disimpan di ViewModel dengan mutableStateOf (bukan StateFlow)
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = null, errorMessage = null) }
    }

    fun login() {
        val emailError = when {
            email.isBlank() -> "Email wajib diisi"
            !email.isValidEmail() -> "Format email tidak valid"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Kata sandi wajib diisi"
            password.length < 6 -> "Minimal 6 karakter"
            else -> null
        }
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val error = authRepository.login(email, password)
            _uiState.update { it.copy(isLoading = false, errorMessage = error, isLoggedIn = error == null) }
        }
    }
}

// ============================== REGISTER ==============================
data class RegisterUiState(
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        name = value
        _uiState.update { it.copy(nameError = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = null, errorMessage = null) }
    }

    fun register() {
        val nameError = if (name.trim().length < 3) "Nama minimal 3 karakter" else null
        val emailError = when {
            email.isBlank() -> "Email wajib diisi"
            !email.isValidEmail() -> "Format email tidak valid"
            else -> null
        }
        val passwordError = if (password.length < 6) "Kata sandi minimal 6 karakter" else null
        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val error = authRepository.register(name, email, password)
            _uiState.update { it.copy(isLoading = false, errorMessage = error, isRegistered = error == null) }
        }
    }
}
