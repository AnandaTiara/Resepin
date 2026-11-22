package com.kelompok4.resepin.ui.screens.recipeform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.DummyData
import com.kelompok4.resepin.data.model.Difficulty
import com.kelompok4.resepin.data.model.DishType
import com.kelompok4.resepin.data.model.Ingredient
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class IngredientInput(val key: String = UUID.randomUUID().toString(), val name: String = "", val amount: String = "")
data class StepInput(val key: String = UUID.randomUUID().toString(), val text: String = "")

data class RecipeFormErrors(
    val photo: String? = null,
    val name: String? = null,
    val category: String? = null,
    val ingredients: String? = null,
    val steps: String? = null,
    val time: String? = null,
    val servings: String? = null,
) {
    val hasError get() = listOf(photo, name, category, ingredients, steps, time, servings).any { it != null }
}

data class RecipeFormUiState(
    val isEdit: Boolean = false,
    /** null = tidak sedang upload; 0f..1f = progres upload foto. */
    val uploadProgress: Float? = null,
    val errors: RecipeFormErrors = RecipeFormErrors(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

class RecipeFormViewModel(
    private val editRecipeId: String?,
    private val recipeRepository: RecipeRepository = AppContainer.recipeRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    val categories = DummyData.categories

    // ---------- Isian form (mutableStateOf karena divalidasi & diedit pengguna) ----------
    var name by mutableStateOf("")
        private set
    var category by mutableStateOf("")
        private set
    var type by mutableStateOf(DishType.MAKANAN)
        private set
    var description by mutableStateOf("")
        private set
    var time by mutableStateOf("")
        private set
    var servings by mutableStateOf("")
        private set
    var difficulty by mutableStateOf(Difficulty.MUDAH)
        private set
    var photoUri by mutableStateOf<String?>(null)
        private set
    var photoRes by mutableStateOf<Int?>(null)
        private set
    val ingredients = mutableStateListOf(IngredientInput(), IngredientInput())
    val steps = mutableStateListOf(StepInput(), StepInput())

    private val _uiState = MutableStateFlow(RecipeFormUiState(isEdit = editRecipeId != null))
    val uiState: StateFlow<RecipeFormUiState> = _uiState.asStateFlow()

    private var uploadJob: Job? = null
    private var original: Recipe? = null

    init {
        editRecipeId?.let { recipeRepository.getRecipe(it) }?.let { r ->
            original = r
            name = r.title
            category = r.category
            type = r.type
            description = r.description
            time = r.timeMinutes.toString()
            servings = r.servings.toString()
            difficulty = r.difficulty
            photoUri = r.imageUri
            photoRes = r.imageRes
            ingredients.clear()
            ingredients.addAll(r.ingredients.map { IngredientInput(name = it.name, amount = it.amount) })
            steps.clear()
            steps.addAll(r.steps.map { StepInput(text = it) })
        }
    }

    private fun clearError(transform: (RecipeFormErrors) -> RecipeFormErrors) =
        _uiState.update { it.copy(errors = transform(it.errors)) }

    fun onNameChange(v: String) { name = v.take(60); clearError { it.copy(name = null) } }
    fun onCategoryChange(v: String) { category = v; clearError { it.copy(category = null) } }
    fun onTypeChange(v: DishType) { type = v }
    fun onDescriptionChange(v: String) { description = v.take(300) }
    fun onTimeChange(v: String) { time = v.filter(Char::isDigit).take(3); clearError { it.copy(time = null) } }
    fun onServingsChange(v: String) { servings = v.filter(Char::isDigit).take(2); clearError { it.copy(servings = null) } }
    fun onDifficultyChange(v: Difficulty) { difficulty = v }

    fun onIngredientChange(index: Int, name: String? = null, amount: String? = null) {
        val item = ingredients[index]
        ingredients[index] = item.copy(name = name ?: item.name, amount = amount ?: item.amount)
        clearError { it.copy(ingredients = null) }
    }

    fun addIngredient() { ingredients.add(IngredientInput()) }
    fun removeIngredient(index: Int) { if (ingredients.size > 1) ingredients.removeAt(index) else ingredients[0] = IngredientInput() }

    fun onStepChange(index: Int, text: String) {
        steps[index] = steps[index].copy(text = text)
        clearError { it.copy(steps = null) }
    }

    fun addStep() { steps.add(StepInput()) }
    fun removeStep(index: Int) { if (steps.size > 1) steps.removeAt(index) else steps[0] = StepInput() }

    /** Simulasi upload foto ke server sambil menampilkan progres "Mengunggah foto... xx%". */
    fun onPhotoPicked(uri: String) {
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            clearError { it.copy(photo = null) }
            for (step in 0..20) {
                _uiState.update { it.copy(uploadProgress = step / 20f) }
                delay(70)
            }
            photoUri = uri
            photoRes = null
            _uiState.update { it.copy(uploadProgress = null) }
        }
    }

    fun save() {
        if (_uiState.value.uploadProgress != null || _uiState.value.isSaving) return
        val filledIngredients = ingredients.filter { it.name.isNotBlank() }
        val filledSteps = steps.filter { it.text.isNotBlank() }
        val errors = RecipeFormErrors(
            name = if (name.trim().length < 3) "Nama resep minimal 3 karakter" else null,
            category = if (category.isBlank()) "Pilih kategori resep" else null,
            ingredients = when {
                filledIngredients.isEmpty() -> "Isi minimal 1 bahan"
                filledIngredients.any { it.amount.isBlank() } -> "Takaran bahan belum diisi"
                else -> null
            },
            steps = if (filledSteps.isEmpty()) "Isi minimal 1 langkah" else null,
            time = if ((time.toIntOrNull() ?: 0) <= 0) "Wajib diisi" else null,
            servings = if ((servings.toIntOrNull() ?: 0) <= 0) "Wajib diisi" else null,
        )
        if (errors.hasError) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val recipe = Recipe(
                id = original?.id ?: "u_${UUID.randomUUID()}",
                title = name.trim(),
                type = type,
                category = category,
                author = original?.author ?: authRepository.currentAuthor(),
                imageRes = photoRes,
                imageUri = photoUri,
                description = description.trim(),
                timeMinutes = time.toInt(),
                servings = servings.toInt(),
                difficulty = difficulty,
                ingredients = filledIngredients.map { Ingredient(it.name.trim(), it.amount.trim()) },
                steps = filledSteps.map { it.text.trim() },
                createdAt = original?.createdAt ?: DummyData.now(),
            )
            recipeRepository.saveRecipe(recipe)
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
