package com.kelompok4.resepin.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok4.resepin.data.DummyData
import com.kelompok4.resepin.data.firebase.awaitWrite
import com.kelompok4.resepin.data.firebase.snapshotFlow
import com.kelompok4.resepin.data.firebase.toComment
import com.kelompok4.resepin.data.firebase.toFirestore
import com.kelompok4.resepin.data.model.Author
import com.kelompok4.resepin.data.model.Comment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Komentar disimpan di koleksi `comments` dan terlihat oleh semua pengguna secara realtime. */
@OptIn(ExperimentalCoroutinesApi::class)
class CommentRepository(
    db: FirebaseFirestore,
    authRepository: AuthRepository,
    scope: CoroutineScope,
) {
    private val commentsCol = db.collection("comments")

    val comments: StateFlow<List<Comment>> = authRepository.userId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList<Comment>())
            else commentsCol.snapshotFlow().map { snapshot ->
                snapshot.documents.mapNotNull { it.toComment(uid) }.sortedByDescending { it.createdAt }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    suspend fun addComment(recipeId: String, author: Author, text: String) {
        val doc = commentsCol.document()
        val comment = Comment(
            id = doc.id,
            recipeId = recipeId,
            author = author,
            text = text.trim(),
            createdAt = DummyData.now(),
        )
        doc.set(comment.toFirestore(resourceName = { null }, seed = false)).awaitWrite()
    }

    suspend fun editComment(id: String, text: String) {
        commentsCol.document(id).update(mapOf("text" to text.trim(), "edited" to true)).awaitWrite()
    }

    suspend fun deleteComment(id: String) {
        commentsCol.document(id).delete().awaitWrite()
    }
}
