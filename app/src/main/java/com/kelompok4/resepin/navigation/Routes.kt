package com.kelompok4.resepin.navigation

/** Semua rute layar (flow mengikuti desain Hi-Fi). */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Tab utama (punya bottom nav)
    const val HOME = "home"
    const val SEARCH = "search"
    const val COMMUNITY = "community"
    const val PLAYLISTS = "playlists"
    const val FAVORITES = "favorites"

    const val ALL_RECIPES = "all_recipes"
    const val PROFILE = "profile"

    /** mode = "ringkas" | "lengkap", from = "default" | "favorit" */
    const val DETAIL = "detail/{recipeId}?mode={mode}&from={from}"
    fun detail(recipeId: String, mode: String = DetailMode.RINGKAS, from: String = DetailFrom.DEFAULT) =
        "detail/$recipeId?mode=$mode&from=$from"

    const val COOKING = "cooking/{recipeId}"
    fun cooking(recipeId: String) = "cooking/$recipeId"

    const val COMMENTS = "comments/{recipeId}"
    fun comments(recipeId: String) = "comments/$recipeId"

    const val RECIPE_FORM = "recipe_form?recipeId={recipeId}"
    fun recipeForm(recipeId: String? = null) = if (recipeId == null) "recipe_form" else "recipe_form?recipeId=$recipeId"

    const val PLAYLIST_FORM = "playlist_form?playlistId={playlistId}"
    fun playlistForm(playlistId: String? = null) =
        if (playlistId == null) "playlist_form" else "playlist_form?playlistId=$playlistId"

    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    fun playlistDetail(playlistId: String) = "playlist/$playlistId"

    const val PICK_RECIPE = "pick_recipe/{playlistId}"
    fun pickRecipe(playlistId: String) = "pick_recipe/$playlistId"

    /** Key untuk mengirim pesan (Snackbar) ke layar sebelumnya lewat SavedStateHandle. */
    const val RESULT_MESSAGE = "result_message"
}

object DetailMode {
    const val RINGKAS = "ringkas"
    const val LENGKAP = "lengkap"
}

object DetailFrom {
    const val DEFAULT = "default"
    const val FAVORIT = "favorit"
}
