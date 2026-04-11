package com.webforge.studio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.webforge.studio.ui.blockeditor.BlockEditorScreen
import com.webforge.studio.ui.canvas.CanvasScreen
import com.webforge.studio.ui.home.HomeScreen
import com.webforge.studio.ui.newproject.NewProjectScreen

/** Navigation route constants used throughout the app. */
object Routes {
    const val HOME = "home"
    const val NEW_PROJECT = "new_project"
    const val CANVAS = "canvas/{projectId}"
    const val BLOCK_EDITOR = "block_editor/{projectId}?elementId={elementId}"

    fun canvas(projectId: String) = "canvas/$projectId"
    fun blockEditor(projectId: String, elementId: String?) = "block_editor/$projectId?elementId=${elementId ?: "null"}"
}

/**
 * Root navigation graph for WebForge Studio.
 *
 * Each destination obtains its ViewModel via [androidx.hilt.navigation.compose.hiltViewModel]
 * — no manual factory or repository wiring required here. The `projectId`
 * argument for the canvas destination is read directly from [SavedStateHandle]
 * inside [CanvasViewModel].
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNewProject = { navController.navigate(Routes.NEW_PROJECT) },
                onOpenProject = { projectId -> navController.navigate(Routes.canvas(projectId)) },
            )
        }

        composable(Routes.NEW_PROJECT) {
            NewProjectScreen(
                onProjectCreated = { projectId ->
                    navController.navigate(Routes.canvas(projectId)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.CANVAS,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
            ),
        ) {
            CanvasScreen(
                onBack = { navController.popBackStack() },
                onOpenInteractions = { elementId ->
                    val projectId = requireNotNull(it.arguments?.getString("projectId"))
                    navController.navigate(Routes.blockEditor(projectId, elementId))
                },
            )
        }

        composable(
            route = Routes.BLOCK_EDITOR,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("elementId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "null"
                },
            ),
        ) {
            BlockEditorScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
