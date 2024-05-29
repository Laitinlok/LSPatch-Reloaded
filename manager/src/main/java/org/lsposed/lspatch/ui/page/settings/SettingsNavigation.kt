package org.lsposed.lspatch.ui.page.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val SettingsRoutePattern = "settings"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    navigate(SettingsRoutePattern, navOptions)
}

fun NavGraphBuilder.settingsScreen() {
    composable(
        route = SettingsRoutePattern,
    ) {
        SettingsScreen()
    }
}