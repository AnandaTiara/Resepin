package com.kelompok4.resepin.data.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "ResepinFirebase"

/** Mengamati sebuah query Firestore secara realtime. Error (mis. saat logout) diabaikan, nilai terakhir dipertahankan. */
internal fun Query.snapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w(TAG, "Listener query gagal: ${error.message}")
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

/** Mengamati satu dokumen secara realtime. */
internal fun DocumentReference.snapshotFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w(TAG, "Listener dokumen gagal: ${error.message}")
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

/**
 * Menunggu penulisan ke server selesai, maksimal [timeoutMs].
 * Jika sedang offline, Firestore menyimpan penulisan di antrean lokal dan mengirimnya saat online,
 * jadi timeout di sini tidak dianggap error (UI tidak menggantung). Error sungguhan (mis. aturan keamanan) tetap dilempar.
 */
internal suspend fun <T> Task<T>.awaitWrite(timeoutMs: Long = 10_000) {
    withTimeoutOrNull(timeoutMs) { await() }
}

/** Penulisan tanpa menunggu (tidak suspend): perubahan lokal langsung terlihat lewat snapshot listener. */
internal fun <T> Task<T>.fireAndForget(what: String) {
    addOnFailureListener { Log.w(TAG, "Gagal $what: ${it.message}") }
}
