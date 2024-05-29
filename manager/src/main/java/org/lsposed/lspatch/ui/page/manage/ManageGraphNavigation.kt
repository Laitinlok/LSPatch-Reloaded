package org.lsposed.lspatch.ui.page.manage

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink

const val ManageGraphRoutePattern = "manage"
const val ManageHomeRoutePattern = "home"
const val ManageAppsSelectRoutePattern = "apps/select"
const val ManageAppsPatchRoutePattern = "apps/patch"

fun NavController.navigateToManageGraph(navOptions: NavOptions? = null) {
    navigate(ManageGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.manageScreen(
    onNavigateToNewPatch: () -> Unit
) {
    composable(
        route = ManageGraphRoutePattern,
        deepLinks = listOf(
            navDeepLink {
                mimeType = "application/vnd.android.package-archive"
            }
        )
    ) {
        ManageHomeScreen(
            onNavigateToNewPatch = onNavigateToNewPatch
        )
    }
}