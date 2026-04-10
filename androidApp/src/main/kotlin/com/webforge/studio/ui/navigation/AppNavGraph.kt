package com.webforge.studio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.webforge.studio.repository.ProjectRepository
import com.webforge.studio.ui.canvas.CanvasScreen
import com.webforge.studio.ui.canvas.CanvasViewModelFactory
import com.webforge.studio.ui.home.HomeScreen
import com.webforge.studio.ui.home.HomeViewModelFactory
import com.webforge.studio.ui.newproject.NewProjectScreen
import com.webforge.studio.ui.newproject.NewProjectViewModelFactory

/** Navigation route constants used throughout the app. */
object Routes {
    const val HOME = "home"
    const val NEW_PROJECT = "new_project"
    const val CANVAS = "canvas/{projectId}"

    fun canvas(projectId: String) = "canvas/$projectId"
}

/**
 * Root navigation graph for WebForge Studio.
 *
 * @param repository    Shared [ProjectRepository] instance injected from [Application].
 * @param navController Optional [NavHostController]; a new one is created by default.
 */
@Composable
fun AppNavGraph(
    repository: ProjectRepository,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            val viewModel = viewModel(factory = HomeViewModelFactory(repository))
            HomeScreen(
                viewModel = viewModel,
                onNewProject = { navController.navigate(Routes.NEW_PROJECT) },
                onOpenProject = { projectId -> navController.navigate(Routes.canvas(projectId)) },
            )
        }

        composable(Routes.NEW_PROJECT) {
            val viewModel = viewModel(factory = NewProjectViewModelFactory(repository))
            NewProjectScreen(
                viewModel = viewModel,
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
        ) { backStackEntry ->
            val projectId = requireNotNull(backStackEntry.arguments?.getString("projectId"))
            val viewModel = viewModel(factory = CanvasViewModelFactory(projectId, repository))
            CanvasScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
