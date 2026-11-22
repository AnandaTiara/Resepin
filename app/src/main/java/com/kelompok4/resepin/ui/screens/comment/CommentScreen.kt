package com.kelompok4.resepin.ui.screens.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.resepin.data.model.Comment
import com.kelompok4.resepin.ui.components.AppScaffold
import com.kelompok4.resepin.ui.components.CommentItem
import com.kelompok4.resepin.ui.components.ConfirmDialog
import com.kelompok4.resepin.ui.components.EmptyState
import com.kelompok4.resepin.ui.components.ResepinTopBar
import com.kelompok4.resepin.ui.theme.Background
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.SurfaceVariant
import com.kelompok4.resepin.ui.theme.TextMuted
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary

@Composable
fun CommentScreen(
    recipeId: String,
    onBack: () -> Unit,
    viewModel: CommentViewModel = viewModel(key = "comments_$recipeId") { CommentViewModel(recipeId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CommentContent(
        state = state,
        input = viewModel.input,
        canSend = viewModel.canSend,
        onBack = onBack,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::send,
        onEdit = viewModel::startEdit,
        onCancelEdit = viewModel::cancelEdit,
        onDelete = viewModel::askDelete,
    )
    state.pendingDelete?.let {
        ConfirmDialog(
            title = "Hapus komentar?",
            message = "Komentar kamu akan dihapus dari resep ini.",
            confirmText = "Hapus",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete,
        )
    }
}

@Composable
fun CommentContent(
    state: CommentUiState,
    input: String,
    canSend: Boolean,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onEdit: (Comment) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (Comment) -> Unit,
) {
    AppScaffold(
        topBar = { ResepinTopBar("Komentar", subtitle = "${state.comments.size} komentar", onBack = onBack) },
        bottomBar = {
            Column(Modifier.background(Background)) {
                HorizontalDivider(color = Border)
                if (state.editingId != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Mengedit komentar", style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Outlined.Close, "Batal edit", tint = TextSecondary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onCancelEdit)
                                .padding(4.dp)
                                .size(18.dp),
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = onInputChange,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        cursorBrush = SolidColor(Primary),
                        maxLines = 4,
                        decorationBox = { inner ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SurfaceVariant)
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.weight(1f)) {
                                    if (input.isEmpty()) Text("Tulis komentar...", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                                    inner()
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("${input.length}/${CommentViewModel.MAX_LENGTH}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        },
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Primary else Primary.copy(alpha = 0.5f))
                            .clickable(enabled = canSend && !state.isSending, onClick = onSend),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isSending) CircularProgressIndicator(Modifier.size(20.dp), color = Surface, strokeWidth = 2.dp)
                        else Icon(Icons.AutoMirrored.Outlined.Send, "Kirim", tint = Surface, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
    ) { padding ->
        if (state.comments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.ChatBubbleOutline, "Belum ada komentar", "Bagikan pendapatmu tentang resep ini.")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(state.comments, key = { it.id }) { comment ->
                    val mine = comment.author.id == state.myUserId
                    CommentItem(
                        comment = comment,
                        isMine = mine,
                        myAvatarUri = state.myAvatarUri,
                        onEdit = if (mine) ({ onEdit(comment) }) else null,
                        onDelete = if (mine) ({ onDelete(comment) }) else null,
                    )
                }
            }
        }
    }
}
