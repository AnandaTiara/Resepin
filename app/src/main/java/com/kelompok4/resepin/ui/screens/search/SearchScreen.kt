package com.kelompok4.resepin.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.DishType
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.TimeFilter
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.CircleIconButton
import com.kelompok4.resepin.ui.components.EmptyState
import com.kelompok4.resepin.ui.components.OutlineButton
import com.kelompok4.resepin.ui.components.PillChip
import com.kelompok4.resepin.ui.components.RecipeListRow
import com.kelompok4.resepin.ui.components.SearchField
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    onRecipeClick: (Recipe) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Tombol back sistem di halaman hasil → kembali ke halaman filter
    BackHandler(enabled = state.showResults) { viewModel.backToFilters() }

    SearchContent(
        state = state,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::submit,
        onClear = viewModel::clearQuery,
        onBack = viewModel::backToFilters,
        onTypeChange = viewModel::setType,
        onTimeChange = viewModel::setTime,
        onSaveDefaultChange = viewModel::setSaveAsDefault,
        onHistoryClick = viewModel::searchFromHistory,
        onRemoveHistory = viewModel::removeHistory,
        onClearHistory = viewModel::clearHistory,
        onResetFilter = viewModel::resetFilter,
        onRecipeClick = onRecipeClick,
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onTypeChange: (DishType?) -> Unit,
    onTimeChange: (TimeFilter) -> Unit,
    onSaveDefaultChange: (Boolean) -> Unit,
    onHistoryClick: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onResetFilter: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
) {
    AppScaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = if (state.showResults) 4.dp else 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.showResults) {
                    CircleIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali", onBack)
                    Spacer(Modifier.width(4.dp))
                }
                SearchField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = "Cari nama, bahan, atau kategori",
                    onSearch = onSubmit,
                    onClear = if (state.showResults) onClear else null,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { padding ->
        if (state.showResults) {
            ResultsContent(state, Modifier.padding(padding), onTypeChange, onTimeChange, onResetFilter, onRecipeClick)
        } else {
            FilterContent(
                state, Modifier.padding(padding), onTypeChange, onTimeChange, onSaveDefaultChange,
                onHistoryClick, onRemoveHistory, onClearHistory,
            )
        }
    }
}

@Composable
private fun FilterContent(
    state: SearchUiState,
    modifier: Modifier,
    onTypeChange: (DishType?) -> Unit,
    onTimeChange: (TimeFilter) -> Unit,
    onSaveDefaultChange: (Boolean) -> Unit,
    onHistoryClick: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(4.dp))
                Text("Jenis hidangan", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillChip("Semua", state.filter.type == null, { onTypeChange(null) })
                    DishType.entries.forEach { type ->
                        PillChip(type.label, state.filter.type == type, { onTypeChange(type) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Waktu memasak", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
            }
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TimeFilter.entries.forEach { t -> PillChip(t.label, state.filter.time == t, { onTimeChange(t) }) }
            }
            Spacer(Modifier.height(16.dp))
            val shape = RoundedCornerShape(16.dp)
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .border(1.dp, Border, shape)
                    .clickable { onSaveDefaultChange(!state.saveAsDefault) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Simpan sebagai filter default", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.saveAsDefault,
                    onCheckedChange = onSaveDefaultChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Primary,
                        checkedThumbColor = Surface,
                        uncheckedTrackColor = Border,
                        uncheckedThumbColor = Surface,
                        uncheckedBorderColor = Border,
                    ),
                )
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Riwayat Pencarian", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (state.history.isNotEmpty()) {
                    Text(
                        "Hapus Semua", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Primary,
                        modifier = Modifier.clickable(onClick = onClearHistory),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (state.history.isEmpty()) {
                Text(
                    "Belum ada riwayat pencarian.",
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
        items(state.history, key = { it }) { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick(text) }
                    .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.AccessTime, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                CircleIconButton(Icons.Outlined.Close, "Hapus riwayat", { onRemoveHistory(text) }, tint = TextSecondary, iconSize = 18)
            }
        }
    }
}

@Composable
private fun ResultsContent(
    state: SearchUiState,
    modifier: Modifier,
    onTypeChange: (DishType?) -> Unit,
    onTimeChange: (TimeFilter) -> Unit,
    onResetFilter: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        val hasFilter = state.filter.type != null || state.filter.time != TimeFilter.SEMUA
        if (hasFilter) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Ketuk chip filter aktif untuk melepasnya
                state.filter.type?.let { PillChip(it.label, true, { onTypeChange(null) }, height = 42) }
                if (state.filter.time != TimeFilter.SEMUA) {
                    PillChip(state.filter.time.label, true, { onTimeChange(TimeFilter.SEMUA) }, height = 42)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        if (state.results.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(horizontal = 32.dp), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "Resep tidak ditemukan",
                    message = "Coba ubah kata kunci atau filter yang dipakai.",
                    modifier = Modifier.padding(bottom = 80.dp),
                ) {
                    OutlineButton("Reset Filter", onResetFilter, Modifier.fillMaxWidth())
                }
            }
        } else {
            Text(
                "${state.results.size} resep ditemukan",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.results, key = { it.id }) { recipe ->
                    RecipeListRow(recipe, Modifier.fillMaxWidth(), onClick = { onRecipeClick(recipe) }, highlightAuthor = true, thumbSize = 72)
                }
            }
        }
    }
}
