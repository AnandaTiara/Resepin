package com.kelompok4.resepin.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Mengubah foto pilihan pengguna (content:// Uri) menjadi data URI JPEG berukuran kecil
 * ("data:image/jpeg;base64,...") agar bisa disimpan langsung di dokumen Firestore (batas 1 MiB per dokumen)
 * dan dilihat semua pengguna tanpa Firebase Storage (yang kini butuh paket Blaze).
 */
object ImageEncoder {

    private const val MAX_BYTES = 700_000

    /** Mengembalikan data URI, atau null jika gambar gagal dibaca. Data URI yang sudah ada dikembalikan apa adanya. */
    suspend fun encode(context: Context, uri: String, maxSide: Int, quality: Int): String? {
        if (uri.startsWith("data:")) return uri
        return withContext(Dispatchers.IO) {
            runCatching {
                val parsed = Uri.parse(uri)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / sample > maxSide * 2 || bounds.outHeight / sample > maxSide * 2) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                val decoded = context.contentResolver.openInputStream(parsed)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                } ?: return@runCatching null

                val scale = maxSide.toFloat() / maxOf(decoded.width, decoded.height)
                val bitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)
                } else decoded

                var q = quality
                var bytes: ByteArray
                do {
                    val out = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, q, out)
                    bytes = out.toByteArray()
                    q -= 10
                } while (bytes.size > MAX_BYTES && q >= 30)

                "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            }.getOrNull()
        }
    }
}
