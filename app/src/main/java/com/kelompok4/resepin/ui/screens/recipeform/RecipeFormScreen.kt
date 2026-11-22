package com.kelompok4.resepin.ui.screens.recipeform

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.Difficulty
import com.kelompok4.resepin.data.model.DishType
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.FieldError
import com.kelompok4.resepin.ui.components.FieldLabel
import com.kelompok4.resepin.ui.components.InputBox
import com.kelompok4.resepin.ui.components.LabeledTextField
import com.kelompok4.resepin.ui.components.OutlineButton
import com.kelompok4.resepin.ui.components.PillChip
import com.kelompok4.resepin.ui.components.PrimaryButton
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.components.SectionHeader
import com.kelompok4.resepin.ui.components.rememberUriBitmap
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Danger
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.SurfaceVariant
import com.kelompok4.resepin.ui.theme.TextMuted
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private const val MAX_PHOTO_BYTES = 2L * 1024 * 1024

@Composable
fun RecipeFormScreen(
    recipeId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: RecipeFormViewModel = viewModel(key = "form_${recipeId ?: "new"}") { RecipeFormViewModel(recipeId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved(if (state.isEdit) "Perubahan resep disimpan" else "Resep berhasil ditambahkan")
    }

    // Photo Picker bawaan Android (tidak butuh izin penyimpanan)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            if (fileSize(context, uri) > MAX_PHOTO_BYTES) {
                scope.launch { snackbar.showSnackbar("Ukuran foto melebihi 2 MB") }
            } else {
                viewModel.onPhotoPicked(uri.toString())
            }
        }
    }

    AppScaffold(
        snackbarHostState = snackbar,
        topBar = { ResepinTopBar(if (state.isEdit) "Edit Resep" else "Tambah Resep", onBack = onBack) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            PhotoPickerBox(
                photoUri = viewModel.photoUri,
                photoRes = viewModel.photoRes,
                onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            )
            state.uploadProgress?.let { progress ->
                Spacer(Modifier.height(12.dp))
                Row {
                    Text("Mengunggah foto...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                    color = Primary, trackColor = SurfaceVariant, strokeCap = StrokeCap.Round,
                    gapSize = 0.dp, drawStopIndicator = {},
                )
            }
            Spacer(Modifier.height(20.dp))
            LabeledTextField(
                value = viewModel.name, onValueChange = viewModel::onNameChange,
                label = "Nama resep", placeholder = "Contoh: Nasi Goreng Kampung",
                error = state.errors.name, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            CategoryDropdown(viewModel.category, viewModel.categories, viewModel::onCategoryChange, state.errors.category)
            Spacer(Modifier.height(16.dp))
            FieldLabel("Jenis")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DishType.entries.forEach { t -> PillChip(t.label, viewModel.type == t, { viewModel.onTypeChange(t) }, height = 34) }
            }
            Spacer(Modifier.height(16.dp))
            LabeledTextField(
                value = viewModel.description, onValueChange = viewModel::onDescriptionChange,
                label = "Deskripsi", placeholder = "Ceritakan singkat resepmu",
                singleLine = false, minHeight = 96.dp, modifier = Modifier.fillMaxWidth(),
            )

            // ---------------- Bahan ----------------
            Spacer(Modifier.height(28.dp))
            SectionHeader("Bahan", trailingInfo = "${viewModel.ingredients.size} bahan")
            Spacer(Modifier.height(12.dp))
            viewModel.ingredients.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InputBox(
                        value = item.name, onValueChange = { viewModel.onIngredientChange(index, name = it) },
                        placeholder = "Nama bahan", modifier = Modifier.weight(1.6f), minHeight = 46.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    InputBox(
                        value = item.amount, onValueChange = { viewModel.onIngredientChange(index, amount = it) },
                        placeholder = "Takaran", modifier = Modifier.weight(1f), minHeight = 46.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    RemoveButton { viewModel.removeIngredient(index) }
                }
                Spacer(Modifier.height(10.dp))
            }
            FieldError(state.errors.ingredients)
            DashedAddButton("Tambah Bahan", viewModel::addIngredient)

            // ---------------- Langkah ----------------
            Spacer(Modifier.height(28.dp))
            SectionHeader("Langkah", trailingInfo = "${viewModel.steps.size} langkah")
            Spacer(Modifier.height(12.dp))
            viewModel.steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.padding(top = 12.dp).size(22.dp).clip(CircleShape).background(PrimaryContainer),
                        contentAlignment = Alignment.Center,
                    ) { Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary) }
                    Spacer(Modifier.width(10.dp))
                    InputBox(
                        value = step.text, onValueChange = { viewModel.onStepChange(index, it) },
                        placeholder = "Tulis langkah ${index + 1}", singleLine = false, minHeight = 64.dp,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    RemoveButton(Modifier.padding(top = 8.dp)) { viewModel.removeStep(index) }
                }
                Spacer(Modifier.height(10.dp))
            }
            FieldError(state.errors.steps)
            DashedAddButton("Tambah Langkah", viewModel::addStep)

            // ---------------- Waktu, porsi, kesulitan ----------------
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledTextField(
                    value = viewModel.time, onValueChange = viewModel::onTimeChange, label = "Waktu (menit)",
                    placeholder = "25", keyboardType = KeyboardType.Number, error = state.errors.time,
                    modifier = Modifier.weight(1f),
                )
                LabeledTextField(
                    value = viewModel.servings, onValueChange = viewModel::onServingsChange, label = "Porsi",
                    placeholder = "2", keyboardType = KeyboardType.Number, error = state.errors.servings,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            FieldLabel("Tingkat kesulitan")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { d -> PillChip(d.label, viewModel.difficulty == d, { viewModel.onDifficultyChange(d) }, height = 34) }
            }
            Spacer(Modifier.height(32.dp))
            PrimaryButton(
                if (state.isEdit) "Simpan Perubahan" else "Simpan Resep",
                viewModel::save,
                Modifier.fillMaxWidth(),
                loading = state.isSaving,
                enabled = state.uploadProgress == null,
            )
            Spacer(Modifier.height(12.dp))
            OutlineButton("Batal", onBack, Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun fileSize(context: Context, uri: Uri): Long = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getLong(0) else 0L
    } ?: 0L
}.getOrDefault(0L)

/** Garis putus-putus di sekeliling komponen. */
fun Modifier.dashedBorder(color: Color, radius: Dp, strokeWidth: Dp = 1.5.dp) = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
    )
}

@Composable
private fun PhotoPickerBox(photoUri: String?, photoRes: Int?, onClick: () -> Unit) {
    val bitmap = rememberUriBitmap(photoUri)
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(shape)
            .background(Color(0xFFFFF1E6))
            .dashedBorder(Primary.copy(alpha = 0.6f), 20.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            bitmap != null -> Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            photoRes != null -> Image(painterResource(photoRes), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.FileUpload, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text("Pilih foto resep", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("JPG atau PNG, maks. 2 MB", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        if (bitmap != null || photoRes != null) {
            Text(
                "Ganti foto",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary,
            )
        }
    }
}

@Composable
private fun CategoryDropdown(value: String, options: List<String>, onSelect: (String) -> Unit, error: String?) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Column {
        FieldLabel("Kategori")
        Spacer(Modifier.height(8.dp))
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(shape)
                    .background(Surface)
                    .then(Modifier.drawBehind {
                        drawRoundRect(
                            color = if (error != null) Danger else Border,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = Stroke(1.dp.toPx()),
                        )
                    })
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    value.ifEmpty { "Pilih kategori" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isEmpty()) TextMuted else TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = TextSecondary)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = Surface) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                        onClick = { onSelect(option); expanded = false },
                    )
                }
            }
        }
        FieldError(error)
    }
}

@Composable
private fun RemoveButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFDE4E1)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Outlined.Close, "Hapus", tint = Danger, modifier = Modifier.size(16.dp)) }
}

@Composable
private fun DashedAddButton(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .dashedBorder(Primary, 22.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Add, null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
    }
}
