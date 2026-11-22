package com.kelompok4.resepin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.resepin.data.model.Playlist
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.TextPrimary
import com.kelompok4.resepin.ui.theme.TextSecondary

fun Recipe.metaByline(): String = "oleh ${author.shortName} · $timeMinutes mnt"
fun Recipe.metaTypeTime(): String = "${type.label} · $timeMinutes mnt"

/** Tombol hati bulat putih di pojok kartu. */
@Composable
fun FavoriteBadge(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, size: Int = 28) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Hapus dari favorit" else "Simpan ke favorit",
            tint = if (isFavorite) Primary else TextPrimary,
            modifier = Modifier.size((size * 0.6f).dp),
        )
    }
}

/** Chip putih kecil di atas foto (Makanan / Minuman). */
@Composable
fun TypeBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Surface)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = Primary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall,
    )
}

/** Komponen "RecipeCard" untuk grid & carousel. */
@Composable
fun RecipeGridCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTypeBadge: Boolean = true,
    bylineOverride: String? = null,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            RecipeImage(recipe, Modifier.matchParentSize())
            FavoriteBadge(
                isFavorite = isFavorite,
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
            if (showTypeBadge) {
                TypeBadge(
                    recipe.type.label,
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 10.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            recipe.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            bylineOverride ?: recipe.metaByline(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Komponen "RecipeRow": baris resep dengan thumbnail di kiri. */
@Composable
fun RecipeListRow(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlightAuthor: Boolean = false,
    thumbSize: Int = 64,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Surface)
            .border(1.dp, Border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            recipe,
            Modifier
                .size(thumbSize.dp)
                .clip(RoundedCornerShape(12.dp)),
            iconSize = 22.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(recipe.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(recipe.metaTypeTime(), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
                "oleh ${recipe.author.shortName}",
                style = MaterialTheme.typography.bodySmall,
                color = if (highlightAuthor) Primary else TextSecondary,
                fontWeight = if (highlightAuthor) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

/** Komponen "PlaylistCard" untuk carousel "Playlist Saya". */
@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .width(140.dp)
        .clickable(onClick = onClick)) {
        PlaylistCover(
            playlist.coverRes,
            Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp)),
        )
        Spacer(Modifier.height(12.dp))
        Text(playlist.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text("${playlist.recipeIds.size} resep", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

/** Komponen "PlaylistItem" untuk daftar "Semua Playlist". */
@Composable
fun PlaylistListItem(playlist: Playlist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface)
            .border(1.dp, Border, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistCover(
            playlist.coverRes,
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            iconSize = 20.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(playlist.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text("${playlist.recipeIds.size} resep", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
        )
    }
}
