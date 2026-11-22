package com.kelompok4.resepin.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.resepin.data.model.Author
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.User
import com.kelompok4.resepin.ui.theme.AvatarContainer
import com.kelompok4.resepin.ui.theme.PlaceholderBrush
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryDark
import com.kelompok4.resepin.ui.theme.PlusJakartaSans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Memuat gambar dari Uri (hasil Photo Picker) di background thread. */
@Composable
fun rememberUriBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri == null) null else withContext(Dispatchers.IO) { decodeUri(context, uri) }
    }
    return bitmap
}

private fun decodeUri(context: Context, uri: String): ImageBitmap? = runCatching {
    // Foto dari Firebase disimpan sebagai data URI base64 ("data:image/jpeg;base64,...").
    if (uri.startsWith("data:")) {
        val bytes = Base64.decode(uri.substringAfter("base64,"), Base64.DEFAULT)
        return@runCatching BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }
    val parsed = Uri.parse(uri)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > 1280 || bounds.outHeight / sample > 1280) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, options) }?.asImageBitmap()
}.getOrNull()

/** Placeholder gradien oranye + ikon gambar (komponen RecipeCard tanpa foto). */
@Composable
fun GradientPlaceholder(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Image,
    iconSize: Dp = 28.dp,
) {
    Box(modifier = modifier.background(PlaceholderBrush), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Primary.copy(alpha = 0.55f), modifier = Modifier.size(iconSize))
    }
}

/** Foto resep: dari drawable, dari Uri (resep buatan pengguna), atau placeholder gradien. */
@Composable
fun RecipeImage(recipe: Recipe, modifier: Modifier = Modifier, iconSize: Dp = 28.dp) {
    val uriBitmap = rememberUriBitmap(recipe.imageUri)
    when {
        recipe.imageRes != null -> Image(
            painter = painterResource(recipe.imageRes),
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
        uriBitmap != null -> Image(
            bitmap = uriBitmap,
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
        else -> GradientPlaceholder(modifier, iconSize = iconSize)
    }
}

@Composable
fun PlaylistCover(@DrawableRes coverRes: Int?, modifier: Modifier = Modifier, iconSize: Dp = 32.dp) {
    if (coverRes != null) {
        Image(
            painter = painterResource(coverRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        GradientPlaceholder(modifier, icon = Icons.AutoMirrored.Outlined.QueueMusic, iconSize = iconSize)
    }
}

/** Avatar bulat: foto jika ada, kalau tidak huruf inisial di latar peach. */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    @DrawableRes imageRes: Int? = null,
    imageUri: String? = null,
) {
    val uriBitmap = rememberUriBitmap(imageUri)
    val base = modifier
        .size(size)
        .clip(CircleShape)
    when {
        uriBitmap != null -> Image(uriBitmap, name, base, contentScale = ContentScale.Crop)
        imageRes != null -> Image(painterResource(imageRes), name, base, contentScale = ContentScale.Crop)
        else -> Box(base.background(AvatarContainer), contentAlignment = Alignment.Center) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "?",
                color = PrimaryDark,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp,
            )
        }
    }
}

@Composable
fun UserAvatar(user: User?, modifier: Modifier = Modifier, size: Dp = 40.dp, showPhoto: Boolean = true) {
    Avatar(
        name = user?.name ?: "?",
        modifier = modifier,
        size = size,
        imageRes = if (showPhoto && user?.avatarUri == null) user?.avatarRes else null,
        imageUri = if (showPhoto) user?.avatarUri else null,
    )
}

@Composable
fun AuthorAvatar(author: Author, modifier: Modifier = Modifier, size: Dp = 36.dp, overrideUri: String? = null) {
    Avatar(author.fullName, modifier, size, author.avatarRes, overrideUri ?: author.avatarUri)
}
