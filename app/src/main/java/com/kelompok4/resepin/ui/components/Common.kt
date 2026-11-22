package com.kelompok4.resepin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kelompok4.resepin.data.model.Comment
import com.kelompok4.resepin.data.model.Ingredient
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Danger
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.SurfaceVariant
import com.kelompok4.resepin.ui.theme.TextMuted
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary

// ------------------------------------------------------------------ Top bar
@Composable
fun ResepinTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = if (onBack != null) 4.dp else 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            CircleIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali", onBack)
            Spacer(Modifier.width(4.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        actions()
    }
}

/** Tombol ikon bulat 40dp. Bila [container] diisi, lingkaran diberi warna (misal tombol favorit peach). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
    container: Color = Color.Transparent,
    size: Int = 40,
    iconSize: Int = 24,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize.dp))
    }
}

// ------------------------------------------------------------------ Section header
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    trailingInfo: String? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (actionText != null) {
            Text(
                actionText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAction?.invoke() }
                    .padding(4.dp),
            )
        }
        if (trailingInfo != null) {
            Text(trailingInfo, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ------------------------------------------------------------------ Chips
/** Chip filter berbentuk pill: aktif = oranye, tidak aktif = putih bergaris. */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    height: Int = 40,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(height.dp)
            .clip(shape)
            .background(if (selected) Primary else Surface)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, Border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = if (selected) Surface else Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) Surface else TextPrimary,
        )
    }
}

/** Chip kategori kecil di detail resep (Makanan / Nasi). */
@Composable
fun CategoryChip(text: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (highlighted) PrimaryContainer else SurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = if (highlighted) Primary else TextSecondary,
    )
}

// ------------------------------------------------------------------ Info resep (waktu, porsi, level)
@Composable
fun RecipeInfoRow(recipe: Recipe, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoTile(Icons.Outlined.AccessTime, "${recipe.timeMinutes} mnt", "Waktu", Modifier.weight(1f))
        InfoTile(Icons.Outlined.Group, "${recipe.servings} orang", "Porsi", Modifier.weight(1f))
        InfoTile(Icons.Outlined.SoupKitchen, recipe.difficulty.label, "Level", Modifier.weight(1f))
    }
}

@Composable
private fun InfoTile(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

/** Komponen "IngredientRow". */
@Composable
fun IngredientRow(ingredient: Ingredient, modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (compact) 8.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(ingredient.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(ingredient.amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        }
        HorizontalDivider(color = Border)
    }
}

/** Nomor langkah dalam lingkaran peach kecil. */
@Composable
fun NumberedStep(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

// ------------------------------------------------------------------ Komentar
@Composable
fun CommentItem(
    comment: Comment,
    modifier: Modifier = Modifier,
    isMine: Boolean = false,
    myAvatarUri: String? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth()) {
        AuthorAvatar(comment.author, size = 36.dp, overrideUri = if (isMine) myAvatarUri else null)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    comment.author.fullName.let { if (isMine) it else it.substringBefore(" ") },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(8.dp))
                Text(formatRelativeTime(comment.createdAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                if (comment.edited) {
                    Text(" · diedit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
            if (isMine && (onEdit != null || onDelete != null)) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (onEdit != null) TextAction("Edit", Primary, onEdit)
                    if (onDelete != null) TextAction("Hapus", Danger, onDelete)
                }
            }
        }
    }
}

@Composable
private fun TextAction(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
    )
}

// ------------------------------------------------------------------ Dialog konfirmasi
/**
 * Dialog konfirmasi hapus. Bila [icon] diisi, tampil gaya terpusat dengan ikon
 * (Hapus resep / Hapus favorit); bila null, gaya rata kiri (Hapus komentar / playlist).
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    loading: Boolean = false,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(296.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .padding(24.dp),
            horizontalAlignment = if (icon != null) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            if (icon != null) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, null, tint = if (confirmText == "Hapus") Danger else Primary, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.height(14.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = if (icon != null) TextAlign.Center else TextAlign.Start,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = if (icon != null) TextAlign.Center else TextAlign.Start,
            )
            Spacer(Modifier.height(20.dp))
            if (icon != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlineButton("Batal", onDismiss, Modifier.weight(1f), size = ButtonSize.Medium)
                    DangerButton(confirmText, onConfirm, Modifier.weight(1f), loading = loading, size = ButtonSize.Medium)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    OutlineButton("Batal", onDismiss, size = ButtonSize.Medium)
                    DangerButton(confirmText, onConfirm, loading = loading, size = ButtonSize.Medium)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Empty state
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(com.kelompok4.resepin.ui.theme.PlaceholderBrush),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(44.dp)) }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(24.dp))
            action()
        }
    }
}

// ------------------------------------------------------------------ Util waktu relatif
fun formatRelativeTime(timeMillis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - timeMillis).coerceAtLeast(0)
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "baru saja"
        minutes < 60 -> "$minutes menit lalu"
        hours < 24 -> "$hours jam lalu"
        days < 7 -> "$days hari lalu"
        else -> "${days / 7} minggu lalu"
    }
}
