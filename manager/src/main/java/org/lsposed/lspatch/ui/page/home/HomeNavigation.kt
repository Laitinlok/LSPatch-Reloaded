package org.lsposed.lspatch.ui.page.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val HomeRoutePattern = "home"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeRoutePattern, navOptions)
}

fun NavGraphBuilder.homeScreen() {
    composable(
        route = HomeRoutePattern,
    ) {
        HomeScreen()
    }
}