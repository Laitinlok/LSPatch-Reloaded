package org.lsposed.lspatch.ui.page.manage.patch

import android.content.pm.ApplicationInfo
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.util.lastItemIndex
import org.lsposed.lspatch.ui.util.lastVisibleItemIndex
import org.lsposed.lspatch.ui.viewmodel.PatchLogEntry
import org.lsposed.lspatch.ui.viewmodel.PatchViewModel
import org.lsposed.lspatch.ui.viewmodel.SelectPatchModulesViewModel
import org.lsposed.lspatch.util.ShizukuApi

private const val TAG = "NewPatchPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchScreen(
    viewModel: PatchViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current

    val logs = viewModel.logs.collectAsState().value
    val isFinished = viewModel.isFinished.collectAsState().value

    BackHandler(!isFinished) {
        Toast.makeText(context, "Patching in progress, please wait...", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        viewModel.startPatch()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patching...") }
            )
        }
    ) { innerPadding ->
        val state = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state
        ) {
            items(logs) { log ->
                when (log.type) {
                    PatchLogEntry.Type.DEBUG,
                    PatchLogEntry.Type.INFO -> Text(log.message)
                    PatchLogEntry.Type.ERROR -> Text(log.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        LaunchedEffect(state.lastItemIndex) {
            if (state.lastVisibleItemIndex != state.lastItemIndex) {
                state.lastItemIndex?.let { state.animateScrollToItem(it) }
            }
        }
    }
}

//@Composable
//private fun DoPatchBody(modifier: Modifier, navigator: DestinationsNavigator) {
//    val viewModel = viewModel<NewPatchViewModel>()
//    val snackbarHost = LocalSnackbarHost.current
//    val scope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) {
//        if (viewModel.logs.isEmpty()) {
//            viewModel.dispatch(ViewAction.LaunchPatch)
//        }
//    }
//
//    BoxWithConstraints(modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
//        val shellBoxMaxHeight =
//            if (viewModel.patchState == PatchState.PATCHING) maxHeight
//            else maxHeight - ButtonDefaults.MinHeight - 12.dp
//        Column(
//            Modifier
//                .fillMaxSize()
//                .wrapContentHeight()
//                .animateContentSize(spring(stiffness = Spring.StiffnessLow))
//        ) {
//            ShimmerAnimation(enabled = viewModel.patchState == PatchState.PATCHING) {
//                ProvideTextStyle(MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)) {
//                    val scrollState = rememberLazyListState()
//                    LazyColumn(
//                        state = scrollState,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(max = shellBoxMaxHeight)
//                            .clip(RoundedCornerShape(32.dp))
//                            .background(brush)
//                            .padding(horizontal = 24.dp, vertical = 18.dp)
//                    ) {
//                        items(viewModel.logs) {
//                            when (it.first) {
//                                Log.DEBUG -> Text(text = it.second)
//                                Log.INFO -> Text(text = it.second)
//                                Log.ERROR -> Text(text = it.second, color = MaterialTheme.colorScheme.error)
//                            }
//                        }
//                    }
//
//                    LaunchedEffect(scrollState.lastItemIndex) {
//                        if (!scrollState.isScrolledToEnd) {
//                            scrollState.animateScrollToItem(scrollState.lastItemIndex!!)
//                        }
//                    }
//                }
//            }
//
//            when (viewModel.patchState) {
//                PatchState.PATCHING -> BackHandler {}
//                PatchState.FINISHED -> {
//                    val shizukuUnavailable = stringResource(R.string.shizuku_unavailable)
//                    val installSuccessfully = stringResource(R.string.patch_install_successfully)
//                    val installFailed = stringResource(R.string.patch_install_failed)
//                    val copyError = stringResource(R.string.copy_error)
//                    var installing by remember { mutableStateOf(false) }
//                    if (installing) InstallDialog(viewModel.patchApp) { status, message ->
//                        scope.launch {
//                            installing = false
//                            if (status == PackageInstaller.STATUS_SUCCESS) {
//                                lspApp.globalScope.launch { snackbarHost.showSnackbar(installSuccessfully) }
//                                navigator.navigateUp()
//                            } else if (status != LSPPackageManager.STATUS_USER_CANCELLED) {
//                                val result = snackbarHost.showSnackbar(installFailed, copyError)
//                                if (result == SnackbarResult.ActionPerformed) {
//                                    val cm = lspApp.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                                    cm.setPrimaryClip(ClipData.newPlainText("LSPatch", message))
//                                }
//                            }
//                        }
//                    }
//                    Row(Modifier.padding(top = 12.dp)) {
//                        Button(
//                            modifier = Modifier.weight(1f),
//                            onClick = { navigator.navigateUp() },
//                            content = { Text(stringResource(R.string.patch_return)) }
//                        )
//                        Spacer(Modifier.weight(0.2f))
//                        Button(
//                            modifier = Modifier.weight(1f),
//                            onClick = {
//                                if (!ShizukuApi.isPermissionGranted) {
//                                    scope.launch {
//                                        snackbarHost.showSnackbar(shizukuUnavailable)
//                                    }
//                                } else {
//                                    installing = true
//                                }
//                            },
//                            content = { Text(stringResource(R.string.install)) }
//                        )
//                    }
//                }
//                PatchState.ERROR -> {
//                    Row(Modifier.padding(top = 12.dp)) {
//                        Button(
//                            modifier = Modifier.weight(1f),
//                            onClick = { navigator.navigateUp() },
//                            content = { Text(stringResource(R.string.patch_return)) }
//                        )
//                        Spacer(Modifier.weight(0.2f))
//                        Button(
//                            modifier = Modifier.weight(1f),
//                            onClick = {
//                                val cm = lspApp.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                                cm.setPrimaryClip(ClipData.newPlainText("LSPatch", viewModel.logs.joinToString { it.second + "\n" }))
//                            },
//                            content = { Text(stringResource(R.string.copy_error)) }
//                        )
//                    }
//                }
//                else -> Unit
//            }
//        }
//    }
//}

@Composable
private fun InstallDialog(applicationInfo: ApplicationInfo, onFinish: (Int, String?) -> Unit) {
    val scope = rememberCoroutineScope()
    var uninstallFirst by remember {
        mutableStateOf(
            ShizukuApi.isPackageInstalledWithoutPatch(
                applicationInfo.packageName
            )
        )
    }
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
