package com.projectpilot.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.projectpilot.app.ui.screens.analysis.AiAnalysisScreen
import com.projectpilot.app.ui.screens.settings.AiProviderSettingsScreen
import com.projectpilot.app.ui.screens.settings.EnhancedSettingsScreen
import com.projectpilot.app.ui.theme.ProjectPilotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectPilotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "settings") {
                        composable("settings") {
                            EnhancedSettingsScreen(navController = navController)
                        }
                        composable("ai_providers") {
                            AiProviderSettingsScreen(navController = navController)
                        }
                        composable("ai_analysis/{projectId}/{projectName}") { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                            val projectName = backStackEntry.arguments?.getString("projectName") ?: ""
                            AiAnalysisScreen(
                                navController = navController,
                                projectId = projectId,
                                projectName = projectName
                            )
                        }
                    }
                }
            }
        }
    }
}
