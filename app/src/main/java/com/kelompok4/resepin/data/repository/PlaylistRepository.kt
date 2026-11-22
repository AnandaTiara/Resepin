package com.kelompok4.resepin.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok4.resepin.data.DummyData
import com.kelompok4.resepin.data.firebase.awaitWrite
import com.kelompok4.resepin.data.firebase.fireAndForget
import com.kelompok4.resepin.data.firebase.snapshotFlow
import com.kelompok4.resepin.data.firebase.toPlaylist
import com.kelompok4.resepin.data.model.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Playlist disimpan di koleksi `playlists` dan bersifat pribadi (hanya pemiliknya yang melihat). */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistRepository(
    db: FirebaseFirestore,
    private val authRepository: AuthRepository,
    scope: CoroutineScope,
) {
    private val playlistsCol = db.collection("playlists")

    val playlists: StateFlow<List<Playlist>> = authRepository.userId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList<Playlist>())
            else playlistsCol.whereEqualTo("ownerId", uid).snapshotFlow().map { snapshot ->
                snapshot.documents.mapNotNull { it.toPlaylist() }.sortedByDescending { it.createdAt }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getPlaylist(id: String): Playlist? = playlists.value.firstOrNull { it.id == id }

    fun isNameUsed(name: String, exceptId: String?): Boolean =
        playlists.value.any { it.id != exceptId && it.name.trim().equals(name.trim(), ignoreCase = true) }

    suspend fun createPlaylist(name: String, description: String) {
        val uid = authRepository.userId.value ?: return
        val doc = playlistsCol.document()
        doc.set(
            mapOf(
                "id" to doc.id,
                "ownerId" to uid,
                "name" to name.trim(),
                "description" to description.trim(),
                "recipeIds" to emptyList<String>(),
                "createdAt" to DummyData.now(),
            ),
        ).awaitWrite()
    }

    suspend fun updatePlaylist(id: String, name: String, description: String) {
        playlistsCol.document(id)
            .update(mapOf("name" to name.trim(), "description" to description.trim()))
            .awaitWrite()
    }

    suspend fun deletePlaylist(id: String) {
        playlistsCol.document(id).delete().awaitWrite()
    }

    fun addRecipe(playlistId: String, recipeId: String) {
        playlistsCol.document(playlistId)
            .update("recipeIds", FieldValue.arrayUnion(recipeId))
            .fireAndForget("menambah resep ke playlist")
    }

    fun removeRecipe(playlistId: String, recipeId: String) {
        playlistsCol.document(playlistId)
            .update("recipeIds", FieldValue.arrayRemove(recipeId))
            .fireAndForget("mengeluarkan resep dari playlist")
    }

    /** Dipanggil saat sebuah resep dihapus agar tidak tersisa di playlist milik pengguna ini. */
    fun removeRecipeEverywhere(recipeId: String) {
        playlists.value.filter { recipeId in it.recipeIds }.forEach { removeRecipe(it.id, recipeId) }
    }
}
