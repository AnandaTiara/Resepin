package com.kelompok4.resepin.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palet warna diambil langsung dari desain Hi-Fi Figma "Resepin"
val Primary = Color(0xFFC2410C)            // oranye utama (tombol, link, ikon)
val PrimaryDark = Color(0xFF7C2D12)        // teks di atas peach (avatar inisial)
val PrimaryContainer = Color(0xFFFFE9DA)   // peach (pill bottom nav, tombol "Buat Playlist")
val AvatarContainer = Color(0xFFFFE0CC)
val Background = Color(0xFFFFFBF5)         // krem latar belakang
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF7EFE5)     // kotak info, chip kategori, input komentar
val Border = Color(0xFFEADFD2)
val DividerColor = Color(0xFFF1E7DB)
val TextPrimary = Color(0xFF2B1D14)
val TextSecondary = Color(0xFF6B5D52)
val TextMuted = Color(0xFF8F8277)
val Danger = Color(0xFFB3261E)
val Success = Color(0xFF2F7D4F)
val SuccessContainer = Color(0xFFE3F2E7)
val SuccessText = Color(0xFF3F8A5E)
val Scrim = Color(0x99000000)

val GradientStart = Color(0xFFFFC8A0)
val GradientEnd = Color(0xFFFB9F67)

/** Gradien oranye untuk placeholder foto resep / playlist (sesuai komponen RecipeCard). */
val PlaceholderBrush = Brush.linearGradient(listOf(GradientStart, GradientEnd))
