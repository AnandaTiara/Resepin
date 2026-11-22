package com.kelompok4.resepin.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok4.resepin.data.DummyData
import com.kelompok4.resepin.data.firebase.ImageEncoder
import com.kelompok4.resepin.data.firebase.awaitWrite
import com.kelompok4.resepin.data.firebase.favoriteIds
import com.kelompok4.resepin.data.firebase.fireAndForget
import com.kelompok4.resepin.data.firebase.recentlyViewedIds
import com.kelompok4.resepin.data.firebase.snapshotFlow
import com.kelompok4.resepin.data.firebase.toFirestore
import com.kelompok4.resepin.data.firebase.toRecipe
import com.kelompok4.resepin.data.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Repository = satu-satunya pintu data resep, favorit, dan riwayat dilihat.
 * - Resep disimpan di koleksi `recipes` (dibagi ke semua pengguna, realtime).
 * - Favorit & riwayat dilihat disimpan per pengguna di dokumen `users/{uid}`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepository(
    private val appContext: Context,
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope,
) {
    private val recipesCol = db.collection("recipes")
    private val commentsCol = db.collection("comments")
    private fun userDoc(uid: String) = db.collection("users").document(uid)

    private val seeding = AtomicBoolean(false)

    val recipes: StateFlow<List<Recipe>> = authRepository.userId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList<Recipe>())
            else recipesCol.snapshotFlow()
                .onEach { snapshot ->
                    // Koleksi benar-benar kosong di server -> isi sekali dengan resep contoh.
                    if (snapshot.isEmpty && !snapshot.metadata.isFromCache) seedSampleData()
                }
                .map { snapshot -> snapshot.documents.mapNotNull { it.toRecipe(uid) } }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** id resep favorit, urut dari yang terbaru disimpan. */
    val favoriteIds: StateFlow<List<String>> = authRepository.userDoc
        .map { it.favoriteIds() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** id resep terakhir dilihat, urut dari yang terbaru. */
    val recentlyViewedIds: StateFlow<List<String>> = authRepository.userDoc
        .map { it.recentlyViewedIds() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getRecipe(id: String): Recipe? = recipes.value.firstOrNull { it.id == id }

    /** Paging dari data yang sudah tersinkron (Firestore sudah realtime & di-cache lokal). */
    suspend fun loadPage(page: Int, pageSize: Int, newestFirst: Boolean): List<Recipe> {
        val sorted = recipes.value.sortedBy { it.createdAt }.let { if (newestFirst) it.reversed() else it }
        return sorted.drop(page * pageSize).take(pageSize)
    }

    private fun writeUserList(field: String, value: List<String>) {
        val uid = authRepository.userId.value ?: return
        userDoc(uid).set(mapOf(field to value), SetOptions.merge()).fireAndForget("menyimpan $field")
    }

    fun toggleFavorite(id: String) {
        val ids = favoriteIds.value
        writeUserList("favoriteIds", if (id in ids) ids - id else listOf(id) + ids)
    }

    fun removeFavorite(id: String) = writeUserList("favoriteIds", favoriteIds.value - id)

    fun markViewed(id: String) =
        writeUserList("recentlyViewedIds", (listOf(id) + (recentlyViewedIds.value - id)).take(10))

    fun clearRecentlyViewed() = writeUserList("recentlyViewedIds", emptyList())

    /** Menyimpan resep baru atau memperbarui resep lama (dipakai form Tambah/Edit Resep). */
    suspend fun saveRecipe(recipe: Recipe) {
        val imageData = recipe.imageUri?.let { ImageEncoder.encode(appContext, it, maxSide = 960, quality = 72) }
        recipesCol.document(recipe.id)
            .set(recipe.toFirestore(this::resourceName, imageData, seed = false))
            .awaitWrite()
    }

    suspend fun deleteRecipe(id: String) {
        recipesCol.document(id).delete().awaitWrite()
        writeUserList("favoriteIds", favoriteIds.value - id)
        writeUserList("recentlyViewedIds", recentlyViewedIds.value - id)
    }

    private fun resourceName(res: Int?): String? =
        res?.let { runCatching { appContext.resources.getResourceEntryName(it) }.getOrNull() }

    /** Mengisi resep & komentar contoh sekali saja (id tetap, sehingga aman bila terpanggil dua kali). */


    private fun seedSampleData() {
        if (!seeding.compareAndSet(false, true)) return
        scope.launch {
            runCatching {
                val batch = db.batch()
                DummyData.recipes()
                    .filter { it.author.id != DummyData.CURRENT_USER_ID }
                    .forEach { r ->
                        batch.set(recipesCol.document(r.id), r.toFirestore(this@RecipeRepository::resourceName, imageData = null, seed = true))
                    }
                DummyData.comments()
                    .filter { it.author.id != DummyData.CURRENT_USER_ID }
                    .forEach { c ->
                        batch.set(commentsCol.document(c.id), c.toFirestore(this@RecipeRepository::resourceName, seed = true))
                    }
                batch.commit().awaitWrite()
            }
            seeding.set(false)
        }
    }
}
