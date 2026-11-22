package com.kelompok4.resepin.data.model

import androidx.annotation.DrawableRes

// ==================== MODEL ====================
// Lapisan Model hanya berisi data class murni: tidak mengenal Compose / UI.

enum class DishType(val label: String) {
    MAKANAN("Makanan"),
    MINUMAN("Minuman"),
}

enum class Difficulty(val label: String) {
    MUDAH("Mudah"),
    SEDANG("Sedang"),
    SULIT("Sulit"),
}

data class Ingredient(
    val name: String,
    val amount: String,
)

data class Author(
    val id: String,
    val fullName: String,
    val shortName: String,
    @param:DrawableRes val avatarRes: Int? = null,
    /** Foto profil dari Firebase (data URI base64). */
    val avatarUri: String? = null,
)

data class Recipe(
    val id: String,
    val title: String,
    val type: DishType,
    val category: String,
    val author: Author,
    @param:DrawableRes val imageRes: Int? = null,
    val imageUri: String? = null,
    val description: String = "",
    val timeMinutes: Int,
    val servings: Int,
    val difficulty: Difficulty,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    /** Ringkasan langkah singkat (dipakai di Detail Resep Ringkas). */
    val summarySteps: List<String> = steps.take(3),
    val createdAt: Long,
)

data class Comment(
    val id: String,
    val recipeId: String,
    val author: Author,
    val text: String,
    val createdAt: Long,
    val edited: Boolean = false,
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    @param:DrawableRes val coverRes: Int? = null,
    val recipeIds: List<String>,
    val createdAt: Long,
    /** id pengguna pemilik playlist (playlist bersifat pribadi). */
    val ownerId: String = "",
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    @param:DrawableRes val avatarRes: Int? = null,
    val avatarUri: String? = null,
) {
    val firstName: String get() = name.trim().substringBefore(" ")
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

/** Filter waktu memasak pada layar Cari. */
enum class TimeFilter(val label: String, val maxMinutes: Int?) {
    SEMUA("Semua", null),
    MAKS_15("Maks 15 mnt", 15),
    MAKS_30("Maks 30 mnt", 30),
    MAKS_45("Maks 45 mnt", 45),
    MAKS_60("Maks 60 mnt", 60),
}

data class SearchFilter(
    val type: DishType? = null,
    val time: TimeFilter = TimeFilter.SEMUA,
)
