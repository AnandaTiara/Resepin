package com.kelompok4.resepin.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.R
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.LabeledTextField
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.theme.Danger
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.ResepinTheme
import com.kelompok4.resepin.ui.theme.TextSecondary

// ============================== LOGIN ==============================

/** SCREEN: menghubungkan ViewModel dengan Content (state turun, event naik). */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoggedIn) { if (state.isLoggedIn) onLoginSuccess() }

    LoginContent(
        email = viewModel.email,
        password = viewModel.password,
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onRegisterClick = onRegisterClick,
    )
}

/** CONTENT: stateless, mudah di-preview. */
@Composable
fun LoginContent(
    email: String,
    password: String,
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    AppScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(112.dp))
            Image(painterResource(R.drawable.ic_resepin_logo), contentDescription = "Logo Resepin", modifier = Modifier.size(88.dp))
            Spacer(Modifier.height(20.dp))
            Text("Resepin", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Temukan, simpan, dan bagikan resep favoritmu",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(32.dp))
            LabeledTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "nama@email.com",
                keyboardType = KeyboardType.Email,
                error = state.emailError,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Kata sandi",
                placeholder = "Masukkan kata sandi",
                isPassword = true,
                error = state.passwordError,
                imeAction = ImeAction.Done,
                onImeAction = onLogin,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(36.dp))
            PrimaryButton("Masuk", onLogin, Modifier.fillMaxWidth(), loading = state.isLoading)
            Spacer(Modifier.height(28.dp))
            SwitchAuthText("Belum punya akun?", "Daftar", onRegisterClick)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ============================== REGISTER ==============================

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isRegistered) { if (state.isRegistered) onRegisterSuccess() }

    RegisterContent(
        name = viewModel.name,
        email = viewModel.email,
        password = viewModel.password,
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRegister = viewModel::register,
        onBack = onBack,
    )
}

@Composable
fun RegisterContent(
    name: String,
    email: String,
    password: String,
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(topBar = { ResepinTopBar("Buat Akun", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Gabung dan mulai simpan resep favoritmu.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = name, onValueChange = onNameChange, label = "Nama lengkap", placeholder = "Nama kamu",
                error = state.nameError, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = email, onValueChange = onEmailChange, label = "Email", placeholder = "nama@email.com",
                keyboardType = KeyboardType.Email, error = state.emailError, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = password, onValueChange = onPasswordChange, label = "Kata sandi", placeholder = "Minimal 6 karakter",
                isPassword = true, error = state.passwordError, supportingText = "Minimal 6 karakter",
                imeAction = ImeAction.Done, onImeAction = onRegister, modifier = Modifier.fillMaxWidth(),
            )
            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Daftar", onRegister, Modifier.fillMaxWidth(), loading = state.isLoading)
            Spacer(Modifier.height(24.dp))
            SwitchAuthText("Sudah punya akun?", "Masuk", onBack)
        }
    }
}

@Composable
private fun SwitchAuthText(question: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(question, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            " $action",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreview() {
    ResepinTheme {
        LoginContent("", "", LoginUiState(), {}, {}, {}, {})
    }
}
