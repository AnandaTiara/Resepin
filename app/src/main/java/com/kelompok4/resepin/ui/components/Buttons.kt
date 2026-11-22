package com.kelompok4.resepin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kelompok4.resepin.ui.theme.Danger
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.TextPrimary

/** Ukuran tombol sesuai komponen Figma: Large = 48dp, Medium = 40dp. */
enum class ButtonSize(val height: Dp) { Large(48.dp), Medium(40.dp) }

private val PillShape = RoundedCornerShape(50)

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    size: ButtonSize = ButtonSize.Large,
    containerColor: Color = Primary,
    contentColor: Color = Surface,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled && !loading,
        shape = PillShape,
        contentPadding = PaddingValues(horizontal = 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.9f),
        ),
    ) {
        ButtonContent(text, loading, contentColor, leadingIcon, size)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    color: Color = Primary,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = PillShape,
        border = BorderStroke(1.5.dp, color),
        contentPadding = PaddingValues(horizontal = 18.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = color),
    ) {
        ButtonContent(text, false, color, leadingIcon, size)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    size: ButtonSize = ButtonSize.Large,
) = PrimaryButton(text, onClick, modifier, loading = loading, size = size, containerColor = Danger)

/** Tombol peach lebar, contoh: "+ Buat Playlist" dan "+ Tambah Resep". */
@Composable
fun TonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) = PrimaryButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    containerColor = PrimaryContainer,
    contentColor = TextPrimary,
    leadingIcon = leadingIcon,
)

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    color: Color,
    leadingIcon: ImageVector?,
    size: ButtonSize,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = color, strokeWidth = 2.dp)
        } else {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = if (size == ButtonSize.Large) MaterialTheme.typography.labelLarge
                else MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}
