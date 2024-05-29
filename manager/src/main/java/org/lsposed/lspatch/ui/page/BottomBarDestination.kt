package org.lsposed.lspatch.ui.page

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.page.home.HomeRoutePattern
import org.lsposed.lspatch.ui.page.home.navigateToHome
import org.lsposed.lspatch.ui.page.logs.LogsRoutePattern
import org.lsposed.lspatch.ui.page.logs.navigateToLogs
import org.lsposed.lspatch.ui.page.manage.ManageGraphRoutePattern
import org.lsposed.lspatch.ui.page.manage.navigateToManageGraph
import org.lsposed.lspatch.ui.page.repo.RepoRoutePattern
import org.lsposed.lspatch.ui.page.repo.navigateToRepo
import org.lsposed.lspatch.ui.page.settings.SettingsRoutePattern
import org.lsposed.lspatch.ui.page.settings.navigateToSettings

enum class BottomBarDestination(
    val route: String,
    val navigate: NavController.(NavOptions?) -> Unit,
    @StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector
) {
    Repo(RepoRoutePattern, NavController::navigateToRepo, R.string.screen_repo, Icons.Filled.GetApp, Icons.Outlined.GetApp),
    Manage(ManageGraphRoutePattern, NavController::navigateToManageGraph, R.string.screen_manage, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    Home(HomeRoutePattern, NavController::navigateToHome, R.string.screen_home, Icons.Filled.Home, Icons.Outlined.Home),
    Logs(LogsRoutePattern, NavController::navigateToLogs, R.string.screen_logs, Icons.Filled.Assignment, Icons.Outlined.Assignment),
    Settings(SettingsRoutePattern, NavController::navigateToSettings, R.string.screen_settings, Icons.Filled.Settings, Icons.Outlined.Settings);
}
