package org.lsposed.lspatch.ui.page.manage.patch

import android.service.credentials.CreateEntry
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.lsposed.lspatch.ui.component.AppItem
import org.lsposed.lspatch.ui.component.PullRefreshBox
import org.lsposed.lspatch.ui.component.SearchAppBar
import org.lsposed.lspatch.ui.viewmodel.SelectAppViewModel

private const val TAG = "SelectAppScreen"

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SelectAppToPatchScreen(
    viewModel: SelectAppViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onAppSelected: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val deepLinkUri = viewModel.deepLinkUri
    val refreshing = viewModel.refreshing.collectAsState().value
    val apps = viewModel.apps.collectAsState().value

    var loading by remember { mutableStateOf(false) }

    val openMultipleDocumentsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        scope.launch {
            loading = true
            if (uris.size == 1) {
                if (viewModel.loadApk(uris.first())) {
                    onAppSelected()
                }
            } else if (uris.size > 1) {
                if (viewModel.loadApks(uris)) {
                    onAppSelected()
                }
            }
            loading = false
        }
    }

    LaunchedEffect(deepLinkUri) {
        if (deepLinkUri != null) {
            loading = true
            if (viewModel.loadApk(deepLinkUri)) {
                onAppSelected()
            }
            viewModel.consumeDeepLink()
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select app to patch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Up")
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
                        Icon(
                            imageVector = Icons.Outlined.SdCard,
                            contentDescription = "From storage"
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
                    items = apps,
                    key = { it.info.packageName }
                ) {
                    AppItem(
                        modifier = Modifier
                            .animateItemPlacement(spring(stiffness = Spring.StiffnessLow))
                            .clickable {
                                viewModel.setApplicationInfo(it.info)
                                onAppSelected()
                            },
                        icon = viewModel.loadIcon(it.info).asImageBitmap(),
                        label = it.label,
                        packageName = it.info.packageName
                    )
                }
            }

            if (apps.isEmpty()) {
                Text(
                    text = "No apps found",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (loading) {
        Dialog(
            onDismissRequest = { /* do nothing */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            CircularProgressIndicator()
        }
    }
}
