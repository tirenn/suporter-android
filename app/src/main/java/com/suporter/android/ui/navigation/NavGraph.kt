package com.suporter.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.suporter.android.SuporterApp
import com.suporter.android.data.repository.AppRepository
import com.suporter.android.data.repository.AuthRepository
import com.suporter.android.data.repository.KeywordRepository
import com.suporter.android.data.repository.LogRepository
import com.suporter.android.ui.screens.apps.MonitoredAppsScreen
import com.suporter.android.ui.screens.auth.LoginScreen
import com.suporter.android.ui.screens.dashboard.DashboardScreen
import com.suporter.android.ui.screens.keywords.KeywordsScreen
import com.suporter.android.ui.screens.logs.LogsScreen
import com.suporter.android.ui.screens.playground.PlaygroundScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PLAYGROUND = "playground"
    const val APPS = "apps"
    const val KEYWORDS = "keywords"
    const val LOGS = "logs"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    app: SuporterApp,
    authRepository: AuthRepository,
    keywordRepository: KeywordRepository,
    appRepository: AppRepository,
    logRepository: LogRepository
) {
    val isLoggedIn by app.preferences.isLoggedInFlow.collectAsState()
    val startDestination = if (isLoggedIn) Routes.DASHBOARD else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authRepository = authRepository,
                initialServerUrl = app.preferences.getServerUrl(),
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                preferences = app.preferences,
                authRepository = authRepository,
                logRepository = logRepository,
                onNavigateToPlayground = { navController.navigate(Routes.PLAYGROUND) },
                onNavigateToApps = { navController.navigate(Routes.APPS) },
                onNavigateToKeywords = { navController.navigate(Routes.KEYWORDS) },
                onNavigateToLogs = { navController.navigate(Routes.LOGS) },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PLAYGROUND) {
            PlaygroundScreen(
                preferences = app.preferences,
                database = app.database,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.APPS) {
            MonitoredAppsScreen(
                appRepository = appRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.KEYWORDS) {
            KeywordsScreen(
                keywordRepository = keywordRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGS) {
            LogsScreen(
                logRepository = logRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
