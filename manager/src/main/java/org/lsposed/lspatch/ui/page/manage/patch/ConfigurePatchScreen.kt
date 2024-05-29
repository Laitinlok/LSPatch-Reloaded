package org.lsposed.lspatch.ui.page.manage.patch

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.SelectionColumn
import org.lsposed.lspatch.ui.component.settings.SettingsCheckBox
import org.lsposed.lspatch.ui.component.settings.SettingsItem
import org.lsposed.lspatch.ui.viewmodel.ConfigurePatchViewModel
import org.lsposed.lspatch.ui.viewmodel.PatchMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurePatchScreen(
    viewModel: ConfigurePatchViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onStartPatch: () -> Unit,
    onSelectModules: () -> Unit
) {
    val context = LocalContext.current

    val patchMode = viewModel.mode.collectAsState().value
    val debuggable = viewModel.debuggable.collectAsState().value
    val overrideVersionCode = viewModel.overrideVersionCode.collectAsState().value
    val signatureBypassLevel = viewModel.signatureBypassLevel.collectAsState().value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Configure patch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Up")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    AnimatedContent(targetState = patchMode) { patchMode ->
                        when (patchMode) {
                            PatchMode.LOCAL -> ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.patch_start)) },
                                icon = { Icon(Icons.Outlined.AutoFixHigh, "Patch") },
                                onClick = { onStartPatch() }
                            )
                            PatchMode.EMBEDDED -> ExtendedFloatingActionButton(
                                text = { Text("Select modules") },
                                icon = { Icon(Icons.Outlined.LibraryAdd, "Select modules") },
                                onClick = { onSelectModules() }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    bitmap = viewModel.loadIcon(viewModel.application).asImageBitmap(),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = viewModel.application.loadLabel(context.packageManager).toString(),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = viewModel.application.packageName,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            SelectionColumn(Modifier.padding(24.dp)) {
                SelectionItem(
                    selected = patchMode == PatchMode.LOCAL,
                    onClick = { viewModel.setMode(PatchMode.LOCAL) },
                    icon = Icons.Outlined.Api,
                    title = stringResource(R.string.patch_local),
                    desc = stringResource(R.string.patch_local_desc)
                )
                SelectionItem(
                    selected = patchMode == PatchMode.EMBEDDED,
                    onClick = { viewModel.setMode(PatchMode.EMBEDDED) },
                    icon = Icons.Outlined.WorkOutline,
                    title = stringResource(R.string.patch_integrated),
                    desc = stringResource(R.string.patch_integrated_desc)
                )
            }

            SettingsCheckBox(
                modifier = Modifier.clickable {
                    viewModel.setDebuggable(!debuggable)
                },
                checked = debuggable,
                icon = Icons.Outlined.BugReport,
                title = stringResource(R.string.patch_debuggable)
            )

            SettingsCheckBox(
                modifier = Modifier.clickable {
                    viewModel.setOverrideVersionCode(!overrideVersionCode)
                },
                checked = overrideVersionCode,
                icon = Icons.Outlined.Layers,
                title = stringResource(R.string.patch_override_version_code),
                desc = stringResource(R.string.patch_override_version_code_desc)
            )

            var showSignatureBypassDialog by remember { mutableStateOf(false) }
            SettingsItem(
                modifier = Modifier.clickable { showSignatureBypassDialog = true },
                icon = Icons.Outlined.RemoveModerator,
                title = stringResource(R.string.patch_sigbypass),
                desc = when (signatureBypassLevel) {
                    0 -> stringResource(R.string.patch_sigbypasslv0)
                    1 -> stringResource(R.string.patch_sigbypasslv1)
                    2 -> stringResource(R.string.patch_sigbypasslv2)
                    else -> throw IllegalArgumentException("Invalid signatureBypassLevel: $signatureBypassLevel")
                }
            )

            if (showSignatureBypassDialog) {
                AlertDialog(
                    onDismissRequest = { showSignatureBypassDialog = false },
                    confirmButton = {
                        Button(onClick = { showSignatureBypassDialog = false }) {
                            Text("Close")
                        }
                    },
                    title = { Text(stringResource(R.string.patch_sigbypass)) },
                    text = {
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            arrayOf(
                                R.string.patch_sigbypasslv0,
                                R.string.patch_sigbypasslv1,
                                R.string.patch_sigbypasslv2
                            ).forEachIndexed { index, resId ->
                                val onClick = {
                                    viewModel.setSignatureBypassLevel(index)
                                    showSignatureBypassDialog = false
                                }
                                Row(
                                    modifier = Modifier.clickable(onClick = onClick),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = signatureBypassLevel == index,
                                        onClick = onClick
                                    )
                                    Text(
                                        text = stringResource(resId),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
