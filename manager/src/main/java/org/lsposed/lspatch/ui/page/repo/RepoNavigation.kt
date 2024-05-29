package org.lsposed.lspatch.ui.page.repo

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val RepoRoutePattern = "repo"

fun NavController.navigateToRepo(navOptions: NavOptions? = null) {
    navigate(RepoRoutePattern, navOptions)
}

fun NavGraphBuilder.repoScreen() {
    composable(
        route = RepoRoutePattern,
    ) {
        RepoScreen()
    }
}