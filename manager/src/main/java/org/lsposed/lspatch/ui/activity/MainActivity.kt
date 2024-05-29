package org.lsposed.lspatch.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import dagger.hilt.android.AndroidEntryPoint
import org.lsposed.lspatch.ui.page.BottomBarDestination
import org.lsposed.lspatch.ui.page.home.homeScreen
import org.lsposed.lspatch.ui.page.logs.logsScreen
import org.lsposed.lspatch.ui.page.manage.patch.ConfigurePatchScreen
import org.lsposed.lspatch.ui.page.manage.patch.PatchScreen
import org.lsposed.lspatch.ui.page.manage.patch.SelectAppToPatchScreen
import org.lsposed.lspatch.ui.page.manage.patch.SelectPatchModulesScreen
import org.lsposed.lspatch.ui.page.manage.manageScreen
import org.lsposed.lspatch.ui.page.repo.repoScreen
import org.lsposed.lspatch.ui.page.settings.settingsScreen
import org.lsposed.lspatch.ui.theme.LSPTheme
import org.lsposed.lspatch.ui.util.LocalSnackbarHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LSPTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = "main",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("main") {
                                val bottomBarNavController = rememberNavController()
                                Scaffold(
                                    bottomBar = { BottomBar(bottomBarNavController) },
                                ) { innerPadding ->
                                    NavHost(
                                        navController = bottomBarNavController,
                                        startDestination = BottomBarDestination.Home.route,
                                        modifier = Modifier.padding(innerPadding)
                                    ) {
                                        repoScreen()
                                        manageScreen(
                                            onNavigateToNewPatch = {
                                                navController.navigate("manage/apps/new-patch/select-app")
                                            }
                                        )
                                        homeScreen()
                                        logsScreen()
                                        settingsScreen()
                                    }
                                }
                            }

                            /* Manage */
                            /* Manage -> Apps -> New patch -> Select app */
                            composable(
                                route = "manage/apps/new-patch/select-app",
                                deepLinks = listOf(
                                    navDeepLink {
                                        mimeType = "application/vnd.android.package-archive"
                                    }
                                )
                            ) {
                                SelectAppToPatchScreen(
                                    onNavigateUp = {
                                        navController.navigateUp()
                                    },
                                    onAppSelected = {
                                        navController.navigate("manage/apps/new-patch/configure")
                                    }
                                )
                            }

                            /* Manage -> Apps -> New patch -> Configure */
                            composable(
                                route = "manage/apps/new-patch/configure"
                            ) {
                                ConfigurePatchScreen(
                                    onNavigateUp = {
                                        navController.navigateUp()
                                    },
                                    onStartPatch = {
                                        navController.navigate("manage/apps/new-patch/patch")
                                    },
                                    onSelectModules = {
                                        navController.navigate("manage/apps/new-patch/select-modules")
                                    }
                                )
                            }

                            /* Manage -> Apps -> New patch -> Select modules */
                            composable(
                                route = "manage/apps/new-patch/select-modules"
                            ) {
                                SelectPatchModulesScreen(
                                    onNavigateUp = {
                                        navController.navigateUp()
                                    },
                                    onStartPatch = {
                                        navController.navigate("manage/apps/new-patch/patch")
                                    },
                                )
                            }

                            /* Manage -> Apps -> New patch -> Patch */
                            composable(
                                route = "manage/apps/new-patch/patch"
                            ) {
                                PatchScreen(
                                    onNavigateUp = {
                                        navController.navigateUp()
                                    },
                                )
                            }

                            // modules

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val currentEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = currentEntry?.destination
    val rootRoute = currentDestination?.let {
        var destination = it
        var route = destination.route
        while (true) {
            val parent = destination.parent ?: break
            val parentRoute = parent.route ?: break
            destination = parent
            route = parentRoute
        }
        route
    }

    NavigationBar(tonalElevation = 8.dp) {
        BottomBarDestination.values().forEach { destination ->
            val selected = rootRoute?.startsWith(destination.route) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    destination.navigate(
                        navController,
                        navOptions {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                },
                icon = {
                    Icon(
                        if (selected) destination.iconSelected else destination.iconNotSelected,
                        stringResource(destination.label)
                    )
                },
                label = { Text(stringResource(destination.label)) },
                alwaysShowLabel = false
            )
        }
    }
}
