package com.kelompok4.resepin.data

import com.kelompok4.resepin.R
import com.kelompok4.resepin.data.model.Author
import com.kelompok4.resepin.data.model.Comment
import com.kelompok4.resepin.data.model.Difficulty
import com.kelompok4.resepin.data.model.DishType
import com.kelompok4.resepin.data.model.Ingredient
import com.kelompok4.resepin.data.model.Playlist
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.data.model.User

/**
 * Data dummy (pengganti API/database) yang isinya mengikuti desain Hi-Fi.
 * Hanya Repository yang boleh membaca objek ini.
 */
object DummyData {

    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    fun now() = System.currentTimeMillis()

    // ---------- Pengguna ----------
    const val CURRENT_USER_ID = "u_me"

    val currentUser = User(
        id = CURRENT_USER_ID,
        name = "Muhammad Alhafiz",
        email = "user@email.com",
        avatarRes = R.drawable.avatar_user,
    )

    /** Akun demo yang bisa langsung dipakai login. */
    const val DEMO_PASSWORD = "123456"

    val me = Author(CURRENT_USER_ID, "Muhammad Alhafiz", "Kamu", R.drawable.avatar_user)
    val dina = Author("u_dina", "Dina Prameswari", "Dina", R.drawable.avatar_dina)
    val raka = Author("u_raka", "Raka Pratama", "Raka", R.drawable.avatar_raka)
    val sinta = Author("u_sinta", "Sinta Maharani", "Sinta", R.drawable.avatar_sinta)
    val bima = Author("u_bima", "Bima Saputra", "Bima", R.drawable.avatar_bima)
    val arya = Author("u_arya", "Arya Wibowo", "Arya", null)

    // ---------- Resep ----------
    fun recipes(): List<Recipe> {
        val t = now()
        return listOf(
            Recipe(
                id = "r1", title = "Nasi Goreng Kampung", type = DishType.MAKANAN, category = "Nasi",
                author = dina, imageRes = R.drawable.img_nasi_goreng_kampung,
                description = "Nasi goreng rumahan dengan bumbu ulek dan teri.",
                timeMinutes = 25, servings = 2, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Nasi putih", "2 piring"),
                    Ingredient("Bawang merah", "3 siung"),
                    Ingredient("Bawang putih", "2 siung"),
                    Ingredient("Cabai rawit", "5 buah"),
                    Ingredient("Kecap manis", "2 sdm"),
                ),
                steps = listOf(
                    "Haluskan bawang merah, bawang putih, dan cabai.",
                    "Panaskan minyak, tumis bumbu halus hingga harum.",
                    "Masukkan nasi putih, aduk rata dengan api sedang.",
                    "Tambahkan kecap manis dan garam, koreksi rasa.",
                    "Sajikan hangat dengan telur mata sapi dan kerupuk.",
                ),
                summarySteps = listOf(
                    "Tumis bumbu halus hingga harum.",
                    "Masukkan nasi dan kecap, aduk rata.",
                    "Sajikan dengan telur dan kerupuk.",
                ),
                createdAt = t - 1 * HOUR,
            ),
            Recipe(
                id = "r2", title = "Es Kopi Susu Gula Aren", type = DishType.MINUMAN, category = "Kopi",
                author = raka, imageRes = R.drawable.img_es_kopi_susu,
                description = "Kopi susu kekinian dengan manis legit gula aren.",
                timeMinutes = 10, servings = 1, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Espresso", "2 shot"),
                    Ingredient("Susu segar", "150 ml"),
                    Ingredient("Gula aren cair", "2 sdm"),
                    Ingredient("Es batu", "1 gelas"),
                ),
                steps = listOf(
                    "Seduh espresso lalu dinginkan sebentar.",
                    "Tuang gula aren cair ke dasar gelas.",
                    "Tambahkan es batu dan susu segar.",
                    "Tuang espresso perlahan di atasnya, aduk sebelum diminum.",
                ),
                createdAt = t - 2 * HOUR,
            ),
            Recipe(
                id = "r3", title = "Soto Ayam Lamongan", type = DishType.MAKANAN, category = "Ayam",
                author = sinta, imageRes = R.drawable.img_soto_ayam,
                description = "Soto kuah kuning dengan taburan koya gurih.",
                timeMinutes = 60, servings = 4, difficulty = Difficulty.SEDANG,
                ingredients = listOf(
                    Ingredient("Ayam kampung", "1/2 ekor"),
                    Ingredient("Kunyit", "3 cm"),
                    Ingredient("Bawang putih", "5 siung"),
                    Ingredient("Bihun", "100 gr"),
                    Ingredient("Koya", "3 sdm"),
                ),
                steps = listOf(
                    "Rebus ayam hingga empuk, ambil kaldunya.",
                    "Haluskan bumbu kuning lalu tumis hingga harum.",
                    "Masukkan bumbu ke kaldu, masak hingga mendidih.",
                    "Suwir ayam, sajikan dengan bihun, telur, dan koya.",
                ),
                createdAt = t - 3 * HOUR,
            ),
            Recipe(
                id = "r4", title = "Pisang Goreng Crispy", type = DishType.MAKANAN, category = "Camilan",
                author = arya, imageRes = R.drawable.img_pisang_goreng,
                description = "Pisang goreng renyah tahan lama.",
                timeMinutes = 20, servings = 4, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Pisang kepok", "6 buah"),
                    Ingredient("Tepung terigu", "100 gr"),
                    Ingredient("Tepung beras", "50 gr"),
                    Ingredient("Air es", "150 ml"),
                ),
                steps = listOf(
                    "Campur tepung terigu, tepung beras, dan air es.",
                    "Belah pisang lalu celupkan ke adonan.",
                    "Goreng dalam minyak panas hingga kuning keemasan.",
                ),
                createdAt = t - 4 * HOUR,
            ),
            Recipe(
                id = "r5", title = "Wedang Jahe Serai", type = DishType.MINUMAN, category = "Teh",
                author = bima, imageRes = R.drawable.img_wedang_jahe,
                description = "Minuman hangat penghangat badan.",
                timeMinutes = 15, servings = 2, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Jahe", "2 ruas"),
                    Ingredient("Serai", "2 batang"),
                    Ingredient("Gula merah", "50 gr"),
                    Ingredient("Air", "500 ml"),
                ),
                steps = listOf(
                    "Bakar jahe lalu geprek bersama serai.",
                    "Rebus air bersama jahe, serai, dan gula merah.",
                    "Saring dan sajikan selagi hangat.",
                ),
                createdAt = t - 5 * HOUR,
            ),
            Recipe(
                id = "r6", title = "Sayur Asem Betawi", type = DishType.MAKANAN, category = "Sayur",
                author = arya, imageRes = R.drawable.img_sayur_asem,
                description = "Sayur asem segar khas Betawi.",
                timeMinutes = 40, servings = 4, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Labu siam", "1 buah"),
                    Ingredient("Jagung manis", "1 buah"),
                    Ingredient("Kacang panjang", "1 ikat"),
                    Ingredient("Asam jawa", "2 sdm"),
                ),
                steps = listOf(
                    "Didihkan air bersama bumbu halus dan asam jawa.",
                    "Masukkan jagung dan labu siam, masak hingga empuk.",
                    "Tambahkan kacang panjang, koreksi rasa, lalu sajikan.",
                ),
                createdAt = t - 6 * HOUR,
            ),
            Recipe(
                id = "r7", title = "Mie Goreng Jawa", type = DishType.MAKANAN, category = "Mie",
                author = bima, imageRes = R.drawable.img_mie_goreng_jawa,
                description = "Mie goreng manis gurih ala angkringan.",
                timeMinutes = 20, servings = 2, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Mie telur", "200 gr"),
                    Ingredient("Kol", "100 gr"),
                    Ingredient("Bawang putih", "3 siung"),
                    Ingredient("Kecap manis", "3 sdm"),
                ),
                steps = listOf(
                    "Rebus mie hingga setengah matang, tiriskan.",
                    "Tumis bawang putih, masukkan sayuran.",
                    "Masukkan mie dan kecap, aduk hingga rata.",
                ),
                createdAt = t - 1 * DAY,
            ),
            Recipe(
                id = "r8", title = "Nasi Goreng Seafood", type = DishType.MAKANAN, category = "Seafood",
                author = sinta, imageRes = R.drawable.img_nasi_goreng_seafood,
                description = "Nasi goreng dengan udang dan cumi.",
                timeMinutes = 30, servings = 2, difficulty = Difficulty.SEDANG,
                ingredients = listOf(
                    Ingredient("Nasi putih", "2 piring"),
                    Ingredient("Udang", "100 gr"),
                    Ingredient("Cumi", "100 gr"),
                    Ingredient("Saus tiram", "1 sdm"),
                ),
                steps = listOf(
                    "Tumis bawang hingga harum.",
                    "Masukkan udang dan cumi, masak hingga berubah warna.",
                    "Masukkan nasi dan saus tiram, aduk rata.",
                ),
                createdAt = t - 2 * DAY,
            ),
            Recipe(
                id = "r9", title = "Ayam Geprek Sambal Bawang", type = DishType.MAKANAN, category = "Ayam",
                author = dina, imageRes = R.drawable.img_ayam_geprek,
                description = "Ayam goreng tepung dengan sambal bawang pedas.",
                timeMinutes = 30, servings = 2, difficulty = Difficulty.SEDANG,
                ingredients = listOf(
                    Ingredient("Ayam fillet", "300 gr"),
                    Ingredient("Tepung bumbu", "100 gr"),
                    Ingredient("Cabai rawit", "15 buah"),
                    Ingredient("Bawang putih", "4 siung"),
                ),
                steps = listOf(
                    "Balur ayam dengan tepung bumbu lalu goreng.",
                    "Ulek cabai dan bawang putih, siram minyak panas.",
                    "Geprek ayam di atas sambal, sajikan.",
                ),
                createdAt = t - 3 * DAY,
            ),
            Recipe(
                id = "r10", title = "Ayam Goreng Kremes", type = DishType.MAKANAN, category = "Ayam",
                author = sinta, imageRes = R.drawable.img_ayam_kremes,
                description = "Ayam ungkep goreng dengan kremesan renyah.",
                timeMinutes = 25, servings = 4, difficulty = Difficulty.SEDANG,
                ingredients = listOf(
                    Ingredient("Ayam potong", "1/2 ekor"),
                    Ingredient("Tepung tapioka", "3 sdm"),
                    Ingredient("Air ungkepan", "300 ml"),
                ),
                steps = listOf(
                    "Ungkep ayam dengan bumbu kuning hingga meresap.",
                    "Goreng ayam hingga kecokelatan.",
                    "Campur air ungkepan dan tapioka, goreng jadi kremesan.",
                ),
                createdAt = t - 4 * DAY,
            ),
            Recipe(
                id = "r11", title = "Tumis Ayam Kecap", type = DishType.MAKANAN, category = "Ayam",
                author = raka, imageRes = R.drawable.img_tumis_ayam_kecap,
                description = "Ayam kecap manis gurih untuk lauk sehari-hari.",
                timeMinutes = 20, servings = 3, difficulty = Difficulty.MUDAH,
                ingredients = listOf(
                    Ingredient("Ayam potong", "500 gr"),
                    Ingredient("Bawang bombay", "1 buah"),
                    Ingredient("Kecap manis", "4 sdm"),
                ),
                steps = listOf(
                    "Goreng ayam sebentar hingga setengah matang.",
                    "Tumis bawang bombay, masukkan ayam.",
                    "Tambahkan kecap dan sedikit air, masak hingga meresap.",
                ),
                createdAt = t - 5 * DAY,
            ),
            Recipe(
                id = "r12", title = "Sate Ayam Madura", type = DishType.MAKANAN, category = "Ayam",
                author = bima, imageRes = R.drawable.img_sate_ayam,
                description = "Sate ayam bumbu kacang khas Madura.",
                timeMinutes = 30, servings = 4, difficulty = Difficulty.SEDANG,
                ingredients = listOf(
                    Ingredient("Daging ayam", "500 gr"),
                    Ingredient("Kacang tanah", "150 gr"),
                    Ingredient("Kecap manis", "5 sdm"),
                    Ingredient("Tusuk sate", "20 buah"),
                ),
                steps = listOf(
                    "Potong dadu ayam lalu tusuk dengan tusukan sate.",
                    "Haluskan kacang goreng dengan bumbu untuk saus.",
                    "Bakar sate sambil dioles kecap, sajikan dengan saus kacang.",
                ),
                createdAt = t - 6 * DAY,
            ),
            // ---------- Resep milik pengguna ("Resep Saya") ----------
            Recipe(
                id = "o1", title = "Pisang Goreng Crispy", type = DishType.MAKANAN, category = "Camilan",
                author = me, imageRes = R.drawable.img_pisang_goreng,
                timeMinutes = 20, servings = 4, difficulty = Difficulty.MUDAH,
                ingredients = listOf(Ingredient("Pisang kepok", "6 buah"), Ingredient("Tepung terigu", "100 gr")),
                steps = listOf("Buat adonan tepung.", "Celup pisang ke adonan.", "Goreng hingga renyah."),
                createdAt = t - 7 * DAY,
            ),
            Recipe(
                id = "o2", title = "Sayur Asem Betawi", type = DishType.MAKANAN, category = "Sayur",
                author = me, imageRes = R.drawable.img_sayur_asem,
                timeMinutes = 40, servings = 4, difficulty = Difficulty.MUDAH,
                ingredients = listOf(Ingredient("Labu siam", "1 buah"), Ingredient("Asam jawa", "2 sdm")),
                steps = listOf("Didihkan air dan bumbu.", "Masukkan sayuran.", "Koreksi rasa lalu sajikan."),
                createdAt = t - 8 * DAY,
            ),
            Recipe(
                id = "o3", title = "Es Teh Lemon", type = DishType.MINUMAN, category = "Teh",
                author = me, imageRes = null,
                timeMinutes = 5, servings = 1, difficulty = Difficulty.MUDAH,
                ingredients = listOf(Ingredient("Teh celup", "1 buah"), Ingredient("Lemon", "1/2 buah")),
                steps = listOf("Seduh teh.", "Peras lemon.", "Tambahkan es batu."),
                createdAt = t - 9 * DAY,
            ),
            Recipe(
                id = "o4", title = "Sambal Matah", type = DishType.MAKANAN, category = "Sambal",
                author = me, imageRes = null,
                timeMinutes = 10, servings = 4, difficulty = Difficulty.MUDAH,
                ingredients = listOf(Ingredient("Bawang merah", "8 siung"), Ingredient("Serai", "2 batang")),
                steps = listOf("Iris tipis bawang dan serai.", "Siram minyak panas.", "Beri perasan jeruk limau."),
                createdAt = t - 10 * DAY,
            ),
        )
    }

    // ---------- Komentar (resep Nasi Goreng Kampung punya 12 komentar) ----------
    fun comments(): List<Comment> {
        val t = now()
        val others = listOf(
            Comment("c5", "r1", arya, "Mantap, cocok buat sarapan.", t - 3 * DAY),
            Comment("c6", "r1", dina, "Terima kasih sudah mencoba resepnya!", t - 3 * DAY - HOUR),
            Comment("c7", "r1", raka, "Kalau pakai terasi makin sedap.", t - 4 * DAY),
            Comment("c8", "r1", sinta, "Anak-anak suka banget.", t - 5 * DAY),
            Comment("c9", "r1", bima, "Teri-nya digoreng dulu ya?", t - 6 * DAY),
            Comment("c10", "r1", arya, "Resep andalan tanggal tua.", t - 8 * DAY),
            Comment("c11", "r1", raka, "Sudah coba, hasilnya enak.", t - 10 * DAY),
            Comment("c12", "r1", sinta, "Bisa pakai nasi merah juga.", t - 12 * DAY),
        )
        return listOf(
            Comment("c1", "r1", me, "Aku coba pakai nasi dingin, hasilnya lebih pulen.", t - 5 * MINUTE, edited = true),
            Comment("c2", "r1", raka, "Enak banget, bumbunya pas. Aku tambah telur dadar.", t - 2 * HOUR),
            Comment("c3", "r1", sinta, "Boleh diganti cabai rawit dengan cabai merah?", t - 1 * DAY),
            Comment("c4", "r1", bima, "Porsinya pas untuk dua orang, terima kasih resepnya.", t - 2 * DAY),
        ) + others
    }

    // ---------- Playlist ----------
    fun playlists(): List<Playlist> {
        val t = now()
        return listOf(
            Playlist(
                id = "p1", name = "Menu Sahur",
                description = "Kumpulan menu praktis untuk sahur selama Ramadan.",
                coverRes = R.drawable.img_playlist_menu_sahur,
                recipeIds = listOf("r1", "r2", "r3", "r4"),
                createdAt = t - 1 * DAY,
            ),
            Playlist(
                id = "p2", name = "Kopi Akhir Pekan",
                description = "Racikan kopi santai untuk akhir pekan.",
                coverRes = R.drawable.img_playlist_kopi,
                recipeIds = listOf("r2", "r5", "o3"),
                createdAt = t - 2 * DAY,
            ),
            Playlist(
                id = "p3", name = "Masak Hemat",
                description = "Menu harian hemat, budget 20 ribu.",
                coverRes = R.drawable.img_playlist_masak_hemat,
                recipeIds = listOf("r1", "r3", "r4", "r6", "r7", "r10", "r11", "r12"),
                createdAt = t - 3 * DAY,
            ),
        )
    }

    /** Resep favorit awal (urutan = terbaru disimpan lebih dulu). */
    val initialFavorites = listOf("r1", "r2", "r3", "r4", "r5", "r6")

    /** Riwayat "Terakhir Dilihat" awal. */
    val initialRecentlyViewed = listOf("r1", "r2", "r3")

    val initialSearchHistory = listOf("nasi goreng", "es kopi", "ayam geprek", "sambal matah")

    val categories = listOf(
        "Nasi", "Mie", "Ayam", "Daging", "Seafood", "Sayur", "Camilan", "Sambal", "Kopi", "Teh", "Jus", "Lainnya",
    )
}
