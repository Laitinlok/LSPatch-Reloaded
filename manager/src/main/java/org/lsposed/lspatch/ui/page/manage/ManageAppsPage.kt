package org.lsposed.lspatch.ui.page.manage

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.AppItem
import org.lsposed.lspatch.ui.component.PullRefreshBox
import org.lsposed.lspatch.ui.viewmodel.manage.ManageAppsViewModel

private const val TAG = "ManageAppsPage"

@Composable
fun ManageAppsPage(
    viewModel: ManageAppsViewModel = hiltViewModel()
) {
    val refreshing = viewModel.refreshing.collectAsState().value
    val installedApplications = viewModel.installedApplications.collectAsState().value

    PullRefreshBox(
        refreshing = refreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = installedApplications,
                key = { it.first.info.packageName }
            ) { app ->
                AppItem(
                    icon = viewModel.loadIcon(app.first.info).asImageBitmap(),
                    label = app.first.label,
                    packageName = app.first.info.packageName
                )
            }
        }

        if (installedApplications.isEmpty()) {
            Text(
                text = stringResource(R.string.manage_no_apps),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

//    if (viewModel.appList.isEmpty()) {
//        Box(Modifier.fillMaxSize()) {
//            Text(
//                modifier = Modifier.align(Alignment.Center),
//                text = run {
//                    if (lspPackageManager.installedApplications.isEmpty()) stringResource(R.string.manage_loading)
//                    else stringResource(R.string.manage_no_apps)
//                },
//                style = MaterialTheme.typography.headlineSmall
//            )
//        }
//    } else {
//        var scopeApp by rememberSaveable { mutableStateOf("") }
////        resultRecipient.onNavResult {
////            if (it is NavResult.Value) {
////                scope.launch {
////                    val result = it.value as SelectAppsResult.MultipleApps
////                    ConfigManager.getModulesForApp(scopeApp).forEach {
////                        ConfigManager.deactivateModule(scopeApp, it)
////                    }
////                    result.selected.forEach {
////                        Log.d(TAG, "Activate ${it.app.packageName} for $scopeApp")
////                        ConfigManager.activateModule(scopeApp, Module(it.app.packageName, it.app.sourceDir))
////                    }
////                }
////            }
////        }
//
//        when (viewModel.updateLoaderState) {
//            is ProcessingState.Idle -> Unit
//            is ProcessingState.Processing -> LoadingDialog()
//            is ProcessingState.Done -> {
//                val it = viewModel.updateLoaderState as ProcessingState.Done
//                val updateSuccessfully = stringResource(R.string.manage_update_loader_successfully)
//                val updateFailed = stringResource(R.string.manage_update_loader_failed)
//                val copyError = stringResource(R.string.copy_error)
//                LaunchedEffect(Unit) {
//                    it.result.onSuccess {
//                        snackbarHost.showSnackbar(updateSuccessfully)
//                    }.onFailure {
//                        val result = snackbarHost.showSnackbar(updateFailed, copyError)
//                        if (result == SnackbarResult.ActionPerformed) {
//                            val cm = lspApp.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                            cm.setPrimaryClip(ClipData.newPlainText("LSPatch", it.toString()))
//                        }
//                    }
//                    viewModel.dispatch(AppManageViewModel.ViewAction.ClearUpdateLoaderResult)
//                }
//            }
//        }
//        when (viewModel.optimizeState) {
//            is ProcessingState.Idle -> Unit
//            is ProcessingState.Processing -> LoadingDialog()
//            is ProcessingState.Done -> {
//                val it = viewModel.optimizeState as ProcessingState.Done
//                val optimizeSucceed = stringResource(R.string.manage_optimize_successfully)
//                val optimizeFailed = stringResource(R.string.manage_optimize_failed)
//                LaunchedEffect(Unit) {
//                    snackbarHost.showSnackbar(if (it.result) optimizeSucceed else optimizeFailed)
//                    viewModel.dispatch(AppManageViewModel.ViewAction.ClearOptimizeResult)
//                }
//            }
//        }
//
//        LazyColumn(Modifier.fillMaxHeight()) {
//            items(
//                items = viewModel.appList,
//                key = { it.first.app.packageName }
//            ) {
//                val isRolling = it.second.useManager && it.second.lspConfig.VERSION_CODE >= Constants.MIN_ROLLING_VERSION_CODE
//                val canUpdateLoader = !isRolling && it.second.lspConfig.VERSION_CODE < LSPConfig.instance.VERSION_CODE
//                var expanded by remember { mutableStateOf(false) }
//                AnywhereDropdown(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false },
//                    onClick = { expanded = true },
//                    onLongClick = { expanded = true },
//                    surface = {
//                        AppItem(
//                            icon = lspPackageManager.getIcon(it.first),
//                            label = it.first.label,
//                            packageName = it.first.app.packageName,
//                            additionalContent = {
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Text(
//                                        text = buildAnnotatedString {
//                                            val (text, color) =
//                                                if (it.second.useManager) stringResource(R.string.patch_local) to MaterialTheme.colorScheme.secondary
//                                                else stringResource(R.string.patch_integrated) to MaterialTheme.colorScheme.tertiary
//                                            append(AnnotatedString(text, SpanStyle(color = color)))
//                                            append("  ")
//                                            if (isRolling) append(stringResource(R.string.manage_rolling))
//                                            else append(it.second.lspConfig.VERSION_CODE.toString())
//                                        },
//                                        fontWeight = FontWeight.SemiBold,
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                    if (canUpdateLoader) {
//                                        with(LocalDensity.current) {
//                                            val size = MaterialTheme.typography.bodySmall.fontSize * 1.2
//                                            Icon(Icons.Filled.KeyboardCapslock, null, Modifier.size(size.toDp()))
//                                        }
//                                    }
//                                }
//                            }
//                        )
//                    }
//                ) {
//                    DropdownMenuItem(
//                        text = { Text(text = it.first.label, color = MaterialTheme.colorScheme.primary) },
//                        onClick = {}, enabled = false
//                    )
//                    val shizukuUnavailable = stringResource(R.string.shizuku_unavailable)
//                    if (canUpdateLoader || BuildConfig.DEBUG) {
//                        DropdownMenuItem(
//                            text = { Text(stringResource(R.string.manage_update_loader)) },
//                            onClick = {
//                                expanded = false
//                                scope.launch {
//                                    if (!ShizukuApi.isPermissionGranted) {
//                                        snackbarHost.showSnackbar(shizukuUnavailable)
//                                    } else {
//                                        viewModel.dispatch(AppManageViewModel.ViewAction.UpdateLoader(it.first, it.second))
//                                    }
//                                }
//                            }
//                        )
//                    }
//                    if (it.second.useManager) {
//                        DropdownMenuItem(
//                            text = { Text(stringResource(R.string.manage_module_scope)) },
//                            onClick = {
//                                expanded = false
//                                scope.launch {
//                                    scopeApp = it.first.app.packageName
//                                    val activated = ConfigManager.getModulesForApp(scopeApp).map { it.pkgName }.toSet()
//                                    val initialSelected = lspPackageManager.installedApplications.mapNotNullTo(ArrayList()) {
//                                        if (activated.contains(it.app.packageName)) it.app.packageName else null
//                                    }
////                                    navigator.navigate(SelectAppsScreenDestination(true, initialSelected))
//                                }
//                            }
//                        )
//                    }
//                    DropdownMenuItem(
//                        text = { Text(stringResource(R.string.manage_optimize)) },
//                        onClick = {
//                            expanded = false
//                            scope.launch {
//                                if (!ShizukuApi.isPermissionGranted) {
//                                    snackbarHost.showSnackbar(shizukuUnavailable)
//                                } else {
//                                    viewModel.dispatch(AppManageViewModel.ViewAction.PerformOptimize(it.first))
//                                }
//                            }
//                        }
//                    )
//                    val uninstallSuccessfully = stringResource(R.string.manage_uninstall_successfully)
//                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                        if (result.resultCode == Activity.RESULT_OK) {
//                            scope.launch {
//                                snackbarHost.showSnackbar(uninstallSuccessfully)
//                            }
//                        }
//                    }
//                    DropdownMenuItem(
//                        text = { Text(stringResource(R.string.uninstall)) },
//                        onClick = {
//                            expanded = false
//                            val intent = Intent(Intent.ACTION_DELETE).apply {
//                                data = Uri.parse("package:${it.first.app.packageName}")
//                                putExtra(Intent.EXTRA_RETURN_RESULT, true)
//                            }
//                            launcher.launch(intent)
//                        }
//                    )
//                }
//            }
//        }
//    }
}

@Composable
fun ManageAppsFab(
    viewModel: ManageAppsViewModel = hiltViewModel(),
    navigateToManageAppsSelect: () -> Unit
) {
    var showSelectStorageDirectoryDialog by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        text = { Text("New patch") },
        icon = { Icon(Icons.Filled.Add, null) },
        onClick = {
            if (viewModel.hasStorageDirectory) {
                navigateToManageAppsSelect()
            } else {
                showSelectStorageDirectoryDialog = true
            }
        }
    )

    if (showSelectStorageDirectoryDialog) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                viewModel.setStorageDirectory(uri)

                Log.i(TAG, "Selected storage directory: ${uri.path}")

                navigateToManageAppsSelect()
                showSelectStorageDirectoryDialog = false
            }
        }

        AlertDialog(
            onDismissRequest = { showSelectStorageDirectoryDialog = false },
            confirmButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.ok)) },
                    onClick = {
                        launcher.launch(null)
                    }
                )
            },
            dismissButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.cancel)) },
                    onClick = { showSelectStorageDirectoryDialog = false }
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.patch_select_dir_title),
                    textAlign = TextAlign.Center
                )
            },
            text = { Text(stringResource(R.string.patch_select_dir_text)) }
        )
    }
}
