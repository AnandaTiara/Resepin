package com.kelompok4.resepin.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok4.resepin.data.repository.AuthRepository
import com.kelompok4.resepin.data.repository.CommentRepository
import com.kelompok4.resepin.data.repository.PlaylistRepository
import com.kelompok4.resepin.data.repository.RecipeRepository
import com.kelompok4.resepin.data.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Wadah dependency sederhana (pengganti Hilt). Setiap Repository hanya dibuat satu kali
 * supaya semua ViewModel berbagi sumber data yang sama.
 * Sumber data kini Firebase (Authentication + Cloud Firestore), bukan data dummy di memori.
 * [init] dipanggil dari [com.kelompok4.resepin.ResepinApp].
 */
object AppContainer {
    private lateinit var appContext: Context

    /** Scope level aplikasi: dipakai Repository untuk menjaga listener Firestore tetap hidup. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    val authRepository by lazy { AuthRepository(appContext, firebaseAuth, firestore, appScope) }
    val recipeRepository by lazy { RecipeRepository(appContext, firestore, authRepository, appScope) }
    val commentRepository by lazy { CommentRepository(firestore, authRepository, appScope) }
    val playlistRepository by lazy { PlaylistRepository(firestore, authRepository, appScope) }
    val searchRepository by lazy { SearchRepository(firestore, authRepository, appScope) }
}
