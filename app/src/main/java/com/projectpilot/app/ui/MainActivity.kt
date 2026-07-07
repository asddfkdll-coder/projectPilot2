package com.projectpilot.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.projectpilot.app.ui.screens.add.AddProjectScreen
import com.projectpilot.app.ui.screens.analysis.AiAnalysisScreen
import com.projectpilot.app.ui.screens.dashboard.ProjectDashboardScreen
import com.projectpilot.app.ui.screens.detail.ProjectDetailScreen
import com.projectpilot.app.ui.screens.git.GitScreen
import com.projectpilot.app.ui.screens.home.HomeScreen
import com.projectpilot.app.ui.screens.recipes.RecipesScreen
import com.projectpilot.app.ui.screens.knowledge.OfflineKnowledgeScreen
import com.projectpilot.app.ui.screens.settings.AiProviderSettingsScreen
import com.projectpilot.app.ui.screens.settings.EnhancedSettingsScreen
import com.projectpilot.app.ui.theme.ProjectPilotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectPilotTheme {
                Surface(modifier = Modifier.fillMaxSize()) { AppNav() }
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val AI_PROVIDERS = "ai_providers"
    const val DETAIL = "detail/{id}"
    const val GIT = "git/{id}"
    const val RECIPES = "recipes/{id}"
    const val AI_ANALYSIS = "ai_analysis/{id}/{name}"
    const val DASHBOARD = "dashboard"
    const val KNOWLEDGE = "knowledge"
    fun detail(id: Long) = "detail/$id"
    fun git(id: Long) = "git/$id"
    fun recipes(id: Long) = "recipes/$id"
    fun aiAnalysis(id: Long, name: String) = "ai_analysis/$id/$name"
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAdd = { nav.navigate(Routes.ADD) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onOpen = { id -> nav.navigate(Routes.detail(id)) },
                onDashboard = { nav.navigate(Routes.DASHBOARD) },
                onKnowledge = { nav.navigate(Routes.KNOWLEDGE) }
            )
        }
        composable(Routes.ADD) { AddProjectScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) {
            EnhancedSettingsScreen(
                onBack = { nav.popBackStack() },
                onNavigateToAiProviders = { nav.navigate(Routes.AI_PROVIDERS) }
            )
        }
        composable(Routes.AI_PROVIDERS) { AiProviderSettingsScreen() }
        composable(Routes.DETAIL) { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            ProjectDetailScreen(
                projectId = id,
                onBack = { nav.popBackStack() },
                onGit = { nav.navigate(Routes.git(it)) },
                onRecipes = { nav.navigate(Routes.recipes(it)) },
                onAiAnalysis = { nav.navigate("ai_analysis/$id/Project") }
            )
        }
        composable(Routes.GIT) { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            GitScreen(projectId = id, onBack = { nav.popBackStack() })
        }
        composable(Routes.RECIPES) { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            RecipesScreen(projectId = id, onBack = { nav.popBackStack() })
        }
        composable(
            Routes.AI_ANALYSIS,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val name = entry.arguments?.getString("name") ?: ""
            AiAnalysisScreen(
                projectId = id,
                projectName = name,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.DASHBOARD) {
            ProjectDashboardScreen(
                onBack = { nav.popBackStack() },
                onNavigateToProject = { id -> nav.navigate(Routes.detail(id)) },
                onNavigateToAnalysis = { id, name -> nav.navigate(Routes.aiAnalysis(id, name)) }
            )
        }
        composable(Routes.KNOWLEDGE) {
            OfflineKnowledgeScreen(
                onBack = { nav.popBackStack() },
                onOpenAnalysis = { id -> /* Navigate to analysis detail */ },
                onOpenProjectAnalyses = { projectId -> nav.navigate(Routes.aiAnalysis(projectId, "")) }
            )
        }
    }
}
