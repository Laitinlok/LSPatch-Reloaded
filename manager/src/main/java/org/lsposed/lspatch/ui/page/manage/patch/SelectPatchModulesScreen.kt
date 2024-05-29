package org.lsposed.lspatch.ui.page.manage.patch

import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.AppItem
import org.lsposed.lspatch.ui.component.PullRefreshBox
import org.lsposed.lspatch.ui.util.LocalSnackbarHost
import org.lsposed.lspatch.ui.viewmodel.SelectPatchModulesViewModel
import org.lsposed.lspatch.util.ShizukuApi

private const val TAG = "NewPatchPage"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SelectPatchModulesScreen(
    viewModel: SelectPatchModulesViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onStartPatch: () -> Unit
) {
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    val refreshing = viewModel.refreshing.collectAsState().value
    val installedApplications = viewModel.installedApplications.collectAsState().value
    val loadedModules = viewModel.loadedModules.collectAsState().value
    val selectedModules = viewModel.modules.collectAsState().value

    var loading by remember { mutableStateOf(false) }

    val openMultipleDocumentsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        scope.launch {
            loading = true
            for (uri in uris) {
                if (!viewModel.loadApk(uri)) {
                    scope.launch { snackbarHost.showSnackbar("Failed to add module apk") }
                }
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select modules to embed") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Up")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = {
                        openMultipleDocumentsLauncher.launch(arrayOf(
                            "application/vnd.android.package-archive"
                        ))
                    }) {
                        Icon(Icons.Outlined.SdCard, "From storage")
                    }
                },
                floatingActionButton = {
                    var showNoModulesSelectedDialog by remember { mutableStateOf(false) }
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.patch_start)) },
                        icon = { Icon(Icons.Outlined.AutoFixHigh, "Patch") },
                        onClick = {
                            if (selectedModules.isEmpty()) {
                                showNoModulesSelectedDialog = true
                            } else {
                                onStartPatch()
                            }
                        }
                    )

                    if (showNoModulesSelectedDialog) {
                        AlertDialog(
                            onDismissRequest = { showNoModulesSelectedDialog = false },
                            confirmButton = {
                                TextButton(onClick = { onStartPatch() }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showNoModulesSelectedDialog = false }) {
                                    Text("No")
                                }
                            },
                            title = { Text("No modules selected") },
                            text = { Text("No modules are selected. Are you sure you want to continue?") }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        PullRefreshBox(
            refreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = loadedModules,
                    key = { it.packageName }
                ) {
                    AppItem(
                        modifier = Modifier
                            .animateItemPlacement(spring(stiffness = Spring.StiffnessLow))
                            .clickable {
                                viewModel.setModules(
                                    selectedModules
                                        .toMutableList()
                                        .apply {
                                            if (!selectedModules.contains(it)) {
                                                add(it)
                                            } else {
                                                remove(it)
                                            }
                                        })
                            },
                        icon = viewModel.loadIcon(it).asImageBitmap(),
                        label = viewModel.loadLabel(it).toString(),
                        packageName = it.packageName,
                        checked = selectedModules.contains(it),
                    )
                }

                items(
                    items = installedApplications,
                    key = { it.info.packageName }
                ) {
                    AppItem(
                        modifier = Modifier
                            .animateItemPlacement(spring(stiffness = Spring.StiffnessLow))
                            .clickable {
                                viewModel.setModules(
                                    selectedModules
                                        .toMutableList()
                                        .apply {
                                            if (!selectedModules.contains(it.info)) {
                                                add(it.info)
                                            } else {
                                                remove(it.info)
                                            }
                                        })
                            },
                        icon = viewModel.loadIcon(it.info).asImageBitmap(),
                        label = it.label,
                        packageName = it.info.packageName,
                        checked = selectedModules.contains(it.info),
                    )
                }
            }

            if (installedApplications.isEmpty() && loadedModules.isEmpty()) {
                Text(
                    text = stringResource(R.string.patch_no_xposed_module),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun InstallDialog(applicationInfo: ApplicationInfo, onFinish: (Int, String?) -> Unit) {
    val scope = rememberCoroutineScope()
    var uninstallFirst by remember { mutableStateOf(ShizukuApi.isPackageInstalledWithoutPatch(applicationInfo.packageName)) }
    var installing by remember { mutableStateOf(0) }
    suspend fun doInstall() {
        Log.i(TAG, "Installing app ${applicationInfo.packageName}")
//        installing = 1
//        val (status, message) = lspPackageManager.install()
//        installing = 0
//        Log.i(TAG, "Installation end: $status, $message")
//        onFinish(status, message)
    }

    LaunchedEffect(Unit) {
        if (!uninstallFirst) {
            doInstall()
        }
    }

    if (uninstallFirst) {
        AlertDialog(
            onDismissRequest = { /*onFinish(LSPPackageManager.STATUS_USER_CANCELLED, "User cancelled")*/ },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
//                            Log.i(TAG, "Uninstalling app ${patchApp.info.packageName}")
//                            uninstallFirst = false
//                            installing = 2
//                            val (status, message) = lspPackageManageruninstall(patchApp.info.packageName)
//                            installing = 0
//                            Log.i(TAG, "Uninstallation end: $status, $message")
//                            if (status == PackageInstaller.STATUS_SUCCESS) {
//                                doInstall()
//                            } else {
//                                onFinish(status, message)
//                            }
                        }
                    },
                    content = { Text(stringResource(android.R.string.ok)) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { /*onFinish(LSPPackageManager.STATUS_USER_CANCELLED, "User cancelled")*/ },
                    content = { Text(stringResource(android.R.string.cancel)) }
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.uninstall),
                    textAlign = TextAlign.Center
                )
            },
            text = { Text(stringResource(R.string.patch_uninstall_text)) }
        )
    }

    if (installing != 0) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(if (installing == 1) R.string.installing else R.string.uninstalling),
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}
