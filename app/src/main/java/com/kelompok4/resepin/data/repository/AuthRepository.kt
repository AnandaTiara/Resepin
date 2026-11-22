package com.kelompok4.resepin.data.repository

import android.content.Context
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok4.resepin.data.firebase.ImageEncoder
import com.kelompok4.resepin.data.firebase.awaitWrite
import com.kelompok4.resepin.data.firebase.snapshotFlow
import com.kelompok4.resepin.data.firebase.toUser
import com.kelompok4.resepin.data.model.Author
import com.kelompok4.resepin.data.model.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Autentikasi memakai Firebase Authentication (email + kata sandi).
 * Profil (nama, foto, favorit, riwayat, dst.) disimpan di dokumen `users/{uid}` pada Firestore.
 * Sesi login tersimpan otomatis oleh Firebase, jadi pengguna tetap masuk setelah aplikasi ditutup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepository(
    private val appContext: Context,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    scope: CoroutineScope,
) {
    private fun userDocRef(uid: String) = db.collection("users").document(uid)

    private fun fallbackUser(firebaseUser: FirebaseUser): User = User(
        id = firebaseUser.uid,
        name = firebaseUser.displayName?.takeIf { it.isNotBlank() }
            ?: firebaseUser.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Pengguna",
        email = firebaseUser.email.orEmpty(),
    )

    private val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** True bila sudah ada sesi login tersimpan (dipakai untuk menentukan layar awal). */
    val isSignedIn: Boolean get() = auth.currentUser != null

    /** id pengguna yang sedang login (null jika belum login). */
    val userId: StateFlow<String?> = authState
        .map { it?.uid }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, auth.currentUser?.uid)

    /** Dokumen profil `users/{uid}` milik pengguna yang login (null jika logout). Dipakai Repository lain. */
    val userDoc: SharedFlow<DocumentSnapshot?> = userId
        .flatMapLatest { uid -> if (uid == null) flowOf(null) else userDocRef(uid).snapshotFlow() }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    val currentUser: StateFlow<User?> = combine(authState, userDoc) { firebaseUser, snapshot ->
        when {
            firebaseUser == null -> null
            snapshot != null && snapshot.id == firebaseUser.uid && snapshot.exists() -> snapshot.toUser(firebaseUser)
            else -> fallbackUser(firebaseUser)
        }
    }.stateIn(scope, SharingStarted.Eagerly, auth.currentUser?.let(::fallbackUser))

    /** Mengembalikan null jika berhasil, atau pesan error jika gagal. */
    suspend fun login(email: String, password: String): String? = try {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        withTimeoutOrNull(5_000) { currentUser.first { it != null } }
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.toMessage()
    }

    suspend fun register(name: String, email: String, password: String): String? = try {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = result.user ?: return "Gagal membuat akun"
        val cleanName = name.trim()
        userDocRef(firebaseUser.uid).set(
            mapOf(
                "name" to cleanName,
                "email" to email.trim(),
                "favoriteIds" to emptyList<String>(),
                "recentlyViewedIds" to emptyList<String>(),
                "searchHistory" to emptyList<String>(),
            ),
        ).awaitWrite()
        // Tunggu sampai nama dari Firestore terbaca agar Beranda langsung menyapa dengan nama yang benar.
        withTimeoutOrNull(5_000) { currentUser.first { it != null && it.name == cleanName } }
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.toMessage()
    }

    suspend fun updateProfile(name: String, email: String, avatarUri: String?) {
        val uid = auth.currentUser?.uid ?: return
        val data = mutableMapOf<String, Any?>("name" to name.trim(), "email" to email.trim())
        if (avatarUri != null) {
            ImageEncoder.encode(appContext, avatarUri, maxSide = 200, quality = 70)?.let { data["avatarData"] = it }
        }
        userDocRef(uid).set(data, SetOptions.merge()).awaitWrite()
    }

    fun logout() {
        auth.signOut()
    }

    /** Penulis (Author) untuk pengguna yang sedang login. */
    fun currentAuthor(): Author {
        val user = currentUser.value
        return Author(
            id = user?.id.orEmpty(),
            fullName = user?.name ?: "Pengguna",
            shortName = "Kamu",
            avatarRes = null,
            avatarUri = user?.avatarUri,
        )
    }

    private fun Throwable.toMessage(): String = when (this) {
        is FirebaseAuthWeakPasswordException -> "Kata sandi terlalu lemah (minimal 6 karakter)"
        is FirebaseAuthUserCollisionException -> "Email sudah dipakai akun lain"
        is FirebaseAuthInvalidUserException -> "Email belum terdaftar"
        is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah"
        is FirebaseNetworkException -> "Tidak ada koneksi internet"
        is FirebaseTooManyRequestsException -> "Terlalu banyak percobaan, coba lagi nanti"
        else -> "Terjadi kesalahan: ${localizedMessage ?: "tidak diketahui"}"
    }
}
