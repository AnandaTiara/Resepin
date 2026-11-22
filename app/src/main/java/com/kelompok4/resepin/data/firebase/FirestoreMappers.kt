package com.kelompok4.resepin.data.firebase

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.kelompok4.resepin.R
import com.kelompok4.resepin.data.model.Author
import com.kelompok4.resepin.data.model.Comment
import com.kelompok4.resepin.data.model.Difficulty
import com.kelompok4.resepin.data.model.DishType
import com.kelompok4.resepin.data.model.Ingredient
import com.kelompok4.resepin.data.model.Playlist
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.SearchFilter
import com.kelompok4.resepin.data.model.TimeFilter
import com.kelompok4.resepin.data.model.User

// Pemetaan manual Model <-> dokumen Firestore (tanpa refleksi, aman untuk data class Kotlin).

/** Nama drawable (mis. "img_soto_ayam") -> id resource. Dipakai untuk gambar bawaan (data contoh). */
internal fun drawableIdByName(name: String?): Int? {
    if (name.isNullOrBlank()) return null
    return runCatching { R.drawable::class.java.getField(name).getInt(null) }.getOrNull()
}

private fun enumOrNull(value: String?): String? = value?.takeIf { it.isNotBlank() }

internal fun makeAuthor(
    id: String,
    fullName: String,
    avatarData: String?,
    avatarName: String?,
    currentUid: String?,
) = Author(
    id = id,
    fullName = fullName,
    shortName = if (id == currentUid) "Kamu" else fullName.trim().substringBefore(" "),
    avatarRes = drawableIdByName(avatarName),
    avatarUri = avatarData,
)

private fun DocumentSnapshot.stringList(field: String): List<String> =
    (get(field) as? List<*>)?.filterIsInstance<String>().orEmpty()

// ---------------------------------------------------------------- Pengguna
internal fun DocumentSnapshot.toUser(fallback: FirebaseUser): User {
    val fallbackName = fallback.displayName?.takeIf { it.isNotBlank() }
        ?: fallback.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "Pengguna"
    return User(
        id = fallback.uid,
        name = getString("name")?.takeIf { it.isNotBlank() } ?: fallbackName,
        email = getString("email") ?: fallback.email.orEmpty(),
        avatarUri = getString("avatarData"),
    )
}

internal fun DocumentSnapshot?.favoriteIds(): List<String> = this?.stringList("favoriteIds").orEmpty()
internal fun DocumentSnapshot?.recentlyViewedIds(): List<String> = this?.stringList("recentlyViewedIds").orEmpty()
internal fun DocumentSnapshot?.searchHistory(): List<String> = this?.stringList("searchHistory").orEmpty()

internal fun DocumentSnapshot?.defaultFilter(): SearchFilter? {
    val map = this?.get("defaultFilter") as? Map<*, *> ?: return null
    val type = (map["type"] as? String)?.let { runCatching { DishType.valueOf(it) }.getOrNull() }
    val time = (map["time"] as? String)?.let { runCatching { TimeFilter.valueOf(it) }.getOrNull() } ?: TimeFilter.SEMUA
    return SearchFilter(type, time)
}

internal fun SearchFilter.toMap(): Map<String, Any?> = mapOf("type" to type?.name, "time" to time.name)

// ---------------------------------------------------------------- Resep
internal fun Recipe.toFirestore(
    resourceName: (Int?) -> String?,
    imageData: String?,
    seed: Boolean,
): Map<String, Any?> = hashMapOf(
    "id" to id,
    "title" to title,
    "type" to type.name,
    "category" to category,
    "authorId" to author.id,
    "authorName" to author.fullName,
    "authorAvatar" to author.avatarUri?.takeIf { it.startsWith("data:") },
    "authorAvatarName" to resourceName(author.avatarRes),
    "imageName" to resourceName(imageRes),
    "imageData" to imageData,
    "description" to description,
    "timeMinutes" to timeMinutes,
    "servings" to servings,
    "difficulty" to difficulty.name,
    "ingredients" to ingredients.map { mapOf("name" to it.name, "amount" to it.amount) },
    "steps" to steps,
    "summarySteps" to summarySteps,
    "createdAt" to createdAt,
    "seed" to seed,
)

internal fun DocumentSnapshot.toRecipe(currentUid: String?): Recipe? {
    val title = getString("title") ?: return null
    val authorId = getString("authorId") ?: return null
    val steps = stringList("steps")
    val ingredients = (get("ingredients") as? List<*>).orEmpty().mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        Ingredient(map["name"] as? String ?: return@mapNotNull null, map["amount"] as? String ?: "")
    }
    val summary = stringList("summarySteps").ifEmpty { steps.take(3) }
    return Recipe(
        id = id,
        title = title,
        type = runCatching { DishType.valueOf(enumOrNull(getString("type")) ?: "MAKANAN") }.getOrDefault(DishType.MAKANAN),
        category = getString("category").orEmpty(),
        author = makeAuthor(
            id = authorId,
            fullName = getString("authorName") ?: "Pengguna",
            avatarData = getString("authorAvatar"),
            avatarName = getString("authorAvatarName"),
            currentUid = currentUid,
        ),
        imageRes = drawableIdByName(getString("imageName")),
        imageUri = getString("imageData"),
        description = getString("description").orEmpty(),
        timeMinutes = (getLong("timeMinutes") ?: 0L).toInt(),
        servings = (getLong("servings") ?: 1L).toInt(),
        difficulty = runCatching { Difficulty.valueOf(enumOrNull(getString("difficulty")) ?: "MUDAH") }.getOrDefault(Difficulty.MUDAH),
        ingredients = ingredients,
        steps = steps,
        summarySteps = summary,
        createdAt = getLong("createdAt") ?: 0L,
    )
}

// ---------------------------------------------------------------- Komentar
internal fun Comment.toFirestore(resourceName: (Int?) -> String?, seed: Boolean): Map<String, Any?> = hashMapOf(
    "id" to id,
    "recipeId" to recipeId,
    "authorId" to author.id,
    "authorName" to author.fullName,
    "authorAvatar" to author.avatarUri?.takeIf { it.startsWith("data:") },
    "authorAvatarName" to resourceName(author.avatarRes),
    "text" to text,
    "createdAt" to createdAt,
    "edited" to edited,
    "seed" to seed,
)

internal fun DocumentSnapshot.toComment(currentUid: String?): Comment? {
    val recipeId = getString("recipeId") ?: return null
    val authorId = getString("authorId") ?: return null
    return Comment(
        id = id,
        recipeId = recipeId,
        author = makeAuthor(
            id = authorId,
            fullName = getString("authorName") ?: "Pengguna",
            avatarData = getString("authorAvatar"),
            avatarName = getString("authorAvatarName"),
            currentUid = currentUid,
        ),
        text = getString("text").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
        edited = getBoolean("edited") ?: false,
    )
}

// ---------------------------------------------------------------- Playlist
internal fun DocumentSnapshot.toPlaylist(): Playlist? {
    val name = getString("name") ?: return null
    return Playlist(
        id = id,
        name = name,
        description = getString("description").orEmpty(),
        coverRes = null,
        recipeIds = stringList("recipeIds"),
        createdAt = getLong("createdAt") ?: 0L,
        ownerId = getString("ownerId").orEmpty(),
    )
}
