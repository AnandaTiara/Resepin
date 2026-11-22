package com.kelompok4.resepin.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok4.resepin.data.firebase.defaultFilter
import com.kelompok4.resepin.data.firebase.fireAndForget
import com.kelompok4.resepin.data.firebase.searchHistory
import com.kelompok4.resepin.data.firebase.toMap
import com.kelompok4.resepin.data.model.SearchFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Riwayat pencarian & filter default disimpan per pengguna di dokumen `users/{uid}`. */
class SearchRepository(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository,
    scope: CoroutineScope,
) {
    val history: StateFlow<List<String>> = authRepository.userDoc
        .map { it.searchHistory() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Filter default yang disimpan bila toggle "Simpan sebagai filter default" aktif. */
    val defaultFilter: StateFlow<SearchFilter?> = authRepository.userDoc
        .map { it.defaultFilter() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private fun write(data: Map<String, Any?>, what: String) {
        val uid = authRepository.userId.value ?: return
        db.collection("users").document(uid).set(data, SetOptions.merge()).fireAndForget(what)
    }

    fun addHistory(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val updated = (listOf(q) + history.value.filterNot { it.equals(q, ignoreCase = true) }).take(10)
        write(mapOf("searchHistory" to updated), "menyimpan riwayat pencarian")
    }

    fun removeHistory(query: String) =
        write(mapOf("searchHistory" to history.value - query), "menghapus riwayat pencarian")

    fun clearHistory() = write(mapOf("searchHistory" to emptyList<String>()), "mengosongkan riwayat pencarian")

    fun saveDefaultFilter(filter: SearchFilter?) =
        write(mapOf("defaultFilter" to (filter?.toMap() ?: FieldValue.delete())), "menyimpan filter default")
}
