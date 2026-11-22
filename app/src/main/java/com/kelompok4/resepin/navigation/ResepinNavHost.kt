package com.kelompok4.resepin.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kelompok4.resepin.data.AppContainer
import com.kelompok4.resepin.data.model.Recipe
import com.kelompok4.resepin.ui.components.BottomNavBar
import com.kelompok4.resepin.ui.components.MainTab
import com.kelompok4.resepin.ui.screens.allrecipes.AllRecipesScreen
import com.kelompok4.resepin.ui.screens.auth.LoginScreen
import com.kelompok4.resepin.ui.screens.auth.RegisterScreen
import com.kelompok4.resepin.ui.screens.comment.CommentScreen
import com.kelompok4.resepin.ui.screens.community.CommunityScreen
import com.kelompok4.resepin.ui.screens.cooking.CookingStepsScreen
import com.kelompok4.resepin.ui.screens.detail.RecipeDetailActions
import com.kelompok4.resepin.ui.screens.detail.RecipeDetailScreen
import com.kelompok4.resepin.ui.screens.favorite.FavoriteScreen
import com.kelompok4.resepin.ui.screens.home.HomeScreen
import com.kelompok4.resepin.ui.screens.playlist.PickRecipeScreen
import com.kelompok4.resepin.ui.screens.playlist.PlaylistDetailScreen
import com.kelompok4.resepin.ui.screens.playlist.PlaylistFormScreen
import com.kelompok4.resepin.ui.screens.playlist.PlaylistListScreen
import com.kelompok4.resepin.ui.screens.profile.ProfileScreen
import com.kelompok4.resepin.ui.screens.recipeform.RecipeFormScreen
import com.kelompok4.resepin.ui.screens.search.SearchScreen
import com.kelompok4.resepin.ui.theme.Background

/** Rute yang menampilkan bottom navigation, beserta tab yang aktif. */
private fun tabFor(route: String?): MainTab? = when (route) {
    Routes.HOME, Routes.ALL_RECIPES -> MainTab.Beranda
    Routes.SEARCH -> MainTab.Cari
    Routes.COMMUNITY -> MainTab.Komunitas
    Routes.PLAYLISTS -> MainTab.Playlist
    Routes.FAVORITES -> MainTab.Favorit
    else -> null
}

private fun MainTab.route() = when (this) {
    MainTab.Beranda -> Routes.HOME
    MainTab.Cari -> Routes.SEARCH
    MainTab.Komunitas -> Routes.COMMUNITY
    MainTab.Playlist -> Routes.PLAYLISTS
    MainTab.Favorit -> Routes.FAVORITES
}

/** Pindah tab tanpa menumpuk back stack (pola standar bottom navigation). */
private fun NavHostController.navigateToTab(tab: MainTab) {
    navigate(tab.route()) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Kirim pesan (Snackbar) ke layar sebelumnya lalu kembali. */
private fun NavHostController.popWithMessage(message: String) {
    previousBackStackEntry?.savedStateHandle?.set(Routes.RESULT_MESSAGE, message)
    popBackStack()
}

@Composable
private fun NavBackStackEntry.resultMessage(): Pair<String?, () -> Unit> {
    val handle = savedStateHandle
    val message by handle.getStateFlow<String?>(Routes.RESULT_MESSAGE, null).collectAsStateWithLifecycle()
    return message to { handle[Routes.RESULT_MESSAGE] = null }
}

@Composable
fun ResepinNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentTab = tabFor(backStack?.destination?.route)
    // Sesi login Firebase tersimpan otomatis: bila sudah login, langsung ke Beranda.
    val startDestination = remember { if (AppContainer.authRepository.isSignedIn) Routes.HOME else Routes.LOGIN }

    val openRecipe: (Recipe) -> Unit = { navController.navigate(Routes.detail(it.id)) }
    val openProfile: () -> Unit = { navController.navigate(Routes.PROFILE) }

    Scaffold(
        containerColor = Background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentTab != null) BottomNavBar(currentTab, navController::navigateToTab)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            // ------------------------------ Auth ------------------------------
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                )
            }

            // ------------------------------ Tab utama ------------------------------
            composable(Routes.HOME) {
                HomeScreen(
                    onRecipeClick = openRecipe,
                    onSeeAllClick = { navController.navigate(Routes.ALL_RECIPES) },
                    onProfileClick = openProfile,
                )
            }
            composable(Routes.SEARCH) { SearchScreen(onRecipeClick = openRecipe) }
            composable(Routes.COMMUNITY) { entry ->
                val (message, clearMessage) = entry.resultMessage()
                CommunityScreen(
                    onRecipeClick = openRecipe,
                    onAddRecipe = { navController.navigate(Routes.recipeForm()) },
                    onProfileClick = openProfile,
                    resultMessage = message,
                    onResultMessageShown = clearMessage,
                )
            }
            composable(Routes.PLAYLISTS) { entry ->
                val (message, clearMessage) = entry.resultMessage()
                PlaylistListScreen(
                    onCreate = { navController.navigate(Routes.playlistForm()) },
                    onOpen = { navController.navigate(Routes.playlistDetail(it.id)) },
                    onProfileClick = openProfile,
                    resultMessage = message,
                    onResultMessageShown = clearMessage,
                )
            }
            composable(Routes.FAVORITES) {
                FavoriteScreen(
                    onRecipeClick = { navController.navigate(Routes.detail(it.id, from = DetailFrom.FAVORIT)) },
                    onExplore = { navController.navigateToTab(MainTab.Beranda) },
                    onProfileClick = openProfile,
                )
            }

            // ------------------------------ Resep ------------------------------
            composable(Routes.ALL_RECIPES) {
                AllRecipesScreen(
                    onBack = { navController.popBackStack() },
                    onSearchClick = { navController.navigateToTab(MainTab.Cari) },
                    onRecipeClick = openRecipe,
                )
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.StringType },
                    navArgument("mode") { type = NavType.StringType; defaultValue = DetailMode.RINGKAS },
                    navArgument("from") { type = NavType.StringType; defaultValue = DetailFrom.DEFAULT },
                ),
            ) { entry ->
                val recipeId = entry.arguments?.getString("recipeId").orEmpty()
                val (message, clearMessage) = entry.resultMessage()
                RecipeDetailScreen(
                    recipeId = recipeId,
                    mode = entry.arguments?.getString("mode") ?: DetailMode.RINGKAS,
                    from = entry.arguments?.getString("from") ?: DetailFrom.DEFAULT,
                    resultMessage = message,
                    onResultMessageShown = clearMessage,
                    actions = RecipeDetailActions(
                        onBack = { navController.popBackStack() },
                        onOpenFull = { id -> navController.navigate(Routes.detail(id, mode = DetailMode.LENGKAP)) },
                        onStartCooking = { id -> navController.navigate(Routes.cooking(id)) },
                        onSeeComments = { id -> navController.navigate(Routes.comments(id)) },
                        onRecipeClick = openRecipe,
                        onEdit = { id -> navController.navigate(Routes.recipeForm(id)) },
                    ),
                )
            }
            composable(Routes.COOKING) { entry ->
                CookingStepsScreen(
                    recipeId = entry.arguments?.getString("recipeId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onFinish = { message -> navController.popWithMessage(message) },
                )
            }
            composable(Routes.COMMENTS) { entry ->
                CommentScreen(
                    recipeId = entry.arguments?.getString("recipeId").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.RECIPE_FORM,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { entry ->
                RecipeFormScreen(
                    recipeId = entry.arguments?.getString("recipeId"),
                    onBack = { navController.popBackStack() },
                    onSaved = { message -> navController.popWithMessage(message) },
                )
            }

            // ------------------------------ Playlist ------------------------------
            composable(
                Routes.PLAYLIST_FORM,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { entry ->
                PlaylistFormScreen(
                    playlistId = entry.arguments?.getString("playlistId"),
                    onBack = { navController.popBackStack() },
                    onSaved = { message -> navController.popWithMessage(message) },
                )
            }
            composable(Routes.PLAYLIST_DETAIL) { entry ->
                val playlistId = entry.arguments?.getString("playlistId").orEmpty()
                val (message, clearMessage) = entry.resultMessage()
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.playlistForm(it)) },
                    onAddRecipe = { navController.navigate(Routes.pickRecipe(it)) },
                    onRecipeClick = openRecipe,
                    resultMessage = message,
                    onResultMessageShown = clearMessage,
                )
            }
            composable(Routes.PICK_RECIPE) { entry ->
                PickRecipeScreen(
                    playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }

            // ------------------------------ Profil ------------------------------
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    },
                )
            }
        }
    }
}
