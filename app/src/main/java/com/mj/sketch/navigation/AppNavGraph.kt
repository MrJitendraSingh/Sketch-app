package com.mj.sketch.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mj.sketch.ui.screens.grid.GridSetupScreen
import com.mj.sketch.ui.screens.picker.ImagePickerScreen
import com.mj.sketch.ui.screens.preview.PreviewScreen
import com.mj.sketch.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object ImagePicker : Screen("image_picker")
    data object GridSetup : Screen("grid_setup/{imageUri}") {
        fun createRoute(imageUri: Uri): String {
            return "grid_setup/${Uri.encode(imageUri.toString())}"
        }
    }
    data object Preview : Screen("preview/{imageUri}?rows={rows}&cols={cols}&section={section}") {
        fun createRoute(
            imageUri: Uri,
            rows: Int = 1,
            cols: Int = 1,
            sectionIndex: Int = 0,
        ): String {
            return "preview/${Uri.encode(imageUri.toString())}?rows=$rows&cols=$cols&section=$sectionIndex"
        }
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.ImagePicker.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(route = Screen.ImagePicker.route) {
            ImagePickerScreen(
                onImageSelected = { uri ->
                    navController.navigate(Screen.GridSetup.createRoute(uri))
                },
            )
        }

        composable(
            route = Screen.GridSetup.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val decodedUri = Uri.decode(encodedUri).toUri()

            GridSetupScreen(
                imageUri = decodedUri,
                onStartTracing = { uri, rows, cols, section ->
                    navController.navigate(Screen.Preview.createRoute(uri, rows, cols, section))
                },
                onBackToPicker = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Screen.Preview.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("rows") { type = NavType.IntType; defaultValue = 1 },
                navArgument("cols") { type = NavType.IntType; defaultValue = 1 },
                navArgument("section") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val decodedUri = Uri.decode(encodedUri).toUri()
            val rows = backStackEntry.arguments?.getInt("rows") ?: 1
            val cols = backStackEntry.arguments?.getInt("cols") ?: 1
            val section = backStackEntry.arguments?.getInt("section") ?: 0

            PreviewScreen(
                imageUri = decodedUri,
                rows = rows,
                cols = cols,
                sectionIndex = section,
                onBackToPicker = {
                    navController.popBackStack()
                },
            )
        }
    }
}
