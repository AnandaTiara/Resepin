package com.kelompok4.resepin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kelompok4.resepin.R

// Font yang dipakai di desain: Plus Jakarta Sans
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.4f).toInt()) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    color = TextPrimary,
)

val Typography = Typography(
    displaySmall = style(26, FontWeight.ExtraBold),     // "Resepin"
    headlineMedium = style(24, FontWeight.Bold),        // judul resep di detail
    headlineSmall = style(22, FontWeight.ExtraBold),    // "Playlist", "Favorit", "Komunitas"
    titleLarge = style(18, FontWeight.Bold),            // judul top bar & section
    titleMedium = style(15, FontWeight.Bold),           // judul item list
    titleSmall = style(13, FontWeight.Bold),            // judul kartu resep
    bodyLarge = style(15, FontWeight.Normal),
    bodyMedium = style(14, FontWeight.Normal),
    bodySmall = style(12, FontWeight.Normal),
    labelLarge = style(15, FontWeight.SemiBold),        // teks tombol
    labelMedium = style(14, FontWeight.SemiBold),       // label input
    labelSmall = style(11, FontWeight.Medium),
)
