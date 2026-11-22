package com.kelompok4.resepin.ui.screens.cooking

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.repository.RecipeRepository
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.theme.Background
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Success
import com.kelompok4.resepin.ui.theme.SuccessContainer
import com.kelompok4.resepin.ui.theme.SuccessText
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.SurfaceVariant
import com.kelompok4.resepin.ui.theme.TextMuted
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ============================== VIEWMODEL ==============================
data class CookingUiState(
    val recipeTitle: String = "",
    val steps: List<String> = emptyList(),
    val doneSteps: Set<Int> = emptySet(),
) {
    val doneCount get() = doneSteps.size
    val progress get() = if (steps.isEmpty()) 0f else doneCount.toFloat() / steps.size
    val percent get() = (progress * 100).toInt()
}

class CookingStepsViewModel(
    recipeId: String,
    repository: RecipeRepository = AppContainer.recipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        repository.getRecipe(recipeId).let { CookingUiState(it?.title.orEmpty(), it?.steps.orEmpty()) },
    )
    val uiState: StateFlow<CookingUiState> = _uiState.asStateFlow()

    fun toggleStep(index: Int) {
        _uiState.update { s -> s.copy(doneSteps = if (index in s.doneSteps) s.doneSteps - index else s.doneSteps + index) }
    }
}

// ============================== SCREEN ==============================
@Composable
fun CookingStepsScreen(
    recipeId: String,
    onBack: () -> Unit,
    onFinish: (String) -> Unit,
    viewModel: CookingStepsViewModel = viewModel(key = "cook_$recipeId") { CookingStepsViewModel(recipeId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CookingStepsContent(
        state = state,
        onBack = onBack,
        onToggle = viewModel::toggleStep,
        onFinish = {
            val msg = if (state.doneCount == state.steps.size) "Selamat, ${state.recipeTitle} siap disajikan!"
            else "Selesai memasak ${state.recipeTitle}"
            onFinish(msg)
        },
    )
}

@Composable
fun CookingStepsContent(
    state: CookingUiState,
    onBack: () -> Unit,
    onToggle: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(state.progress, label = "progress")
    AppScaffold(
        topBar = { ResepinTopBar("Langkah Memasak", subtitle = state.recipeTitle, onBack = onBack) },
        bottomBar = {
            Column(Modifier.background(Background)) {
                HorizontalDivider(color = Border)
                PrimaryButton(
                    "Selesai Memasak",
                    onFinish,
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.doneCount} dari ${state.steps.size} langkah selesai",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text("${state.percent}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Success)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Success,
                    trackColor = SurfaceVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                Spacer(Modifier.height(4.dp))
            }
            itemsIndexed(state.steps) { index, step ->
                StepItem(index + 1, step, done = index in state.doneSteps, onClick = { onToggle(index) })
            }
        }
    }
}

/** Komponen "StepItem" (State=Done / State=Todo). */
@Composable
fun StepItem(number: Int, text: String, done: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val bg by animateColorAsState(if (done) SuccessContainer else Surface, label = "stepBg")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .then(if (done) Modifier else Modifier.border(1.dp, Border, shape))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (done) Success else Surface)
                .then(if (done) Modifier else Modifier.border(2.dp, TextSecondary, RoundedCornerShape(6.dp))),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Filled.Check, null, tint = Surface, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Langkah $number",
                style = MaterialTheme.typography.labelSmall,
                color = if (done) SuccessText else TextMuted,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (done) TextSecondary else TextPrimary,
            )
        }
    }
}
