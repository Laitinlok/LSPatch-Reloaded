package org.lsposed.lspatch.ui.page.logs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val LogsRoutePattern = "logs"

fun NavController.navigateToLogs(navOptions: NavOptions? = null) {
    navigate(LogsRoutePattern, navOptions)
}

fun NavGraphBuilder.logsScreen() {
    composable(
        route = LogsRoutePattern,
    ) {
        LogsScreen()
    }
}