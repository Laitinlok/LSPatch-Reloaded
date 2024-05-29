package org.lsposed.lspatch.ui.page.manage

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.AnywhereDropdown
import org.lsposed.lspatch.ui.component.AppItem
import org.lsposed.lspatch.ui.component.PullRefreshBox
import org.lsposed.lspatch.ui.viewmodel.manage.ManageModulesViewModel
import org.lsposed.lspatch.util.LSPPackageManager

@Composable
fun ManageModulesPage(
    viewModel: ManageModulesViewModel = hiltViewModel()
) {
    val refreshing = viewModel.refreshing.collectAsState().value
    val modules = viewModel.modules.collectAsState().value

    PullRefreshBox(
        refreshing = refreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = modules,
                key = { it.first.info.packageName }
            ) { app ->
                AppItem(
                    modifier = Modifier,
                    icon = viewModel.loadIcon(app.first.info).asImageBitmap(),
                    label = app.first.label,
                    packageName = app.first.info.packageName,
                    additionalContent = {
                        app.second.description?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = buildAnnotatedString {
                                append(AnnotatedString("API", SpanStyle(color = MaterialTheme.colorScheme.secondary)))
                                append(" ")
                                append(app.second.api.toString())
                            },
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }

        if (modules.isEmpty()) {
            Text(
                text = stringResource(R.string.manage_no_modules),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

//        LazyColumn(Modifier.fillMaxHeight()) {
//            items(
//                items = viewModel.appList,
//                key = { it.first.info.packageName }
//            ) {
//                var expanded by remember { mutableStateOf(false) }
//                val settingsIntent = remember { lspPackageManager.getSettingsIntent(it.first.info.packageName) }
//                AnywhereDropdown(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false },
//                    onClick = { settingsIntent?.let { context.startActivity(it) } },
//                    onLongClick = { expanded = true },
//                    surface = {
//                        AppItem(
//                            icon = lspPackageManager.getIcon(it.first),
//                            label = it.first.label,
//                            packageName = it.first.info.packageName,
//                            additionalContent = {
//                                Text(
//                                    text = it.second.description,
//                                    style = MaterialTheme.typography.bodySmall
//                                )
//                                Text(
//                                    text = buildAnnotatedString {
//                                        append(AnnotatedString("API", SpanStyle(color = MaterialTheme.colorScheme.secondary)))
//                                        append("  ")
//                                        append(it.second.api.toString())
//                                    },
//                                    fontWeight = FontWeight.SemiBold,
//                                    style = MaterialTheme.typography.bodySmall
//                                )
//                            }
//                        )
//                    }
//                ) {
//                    DropdownMenuItem(
//                        text = { Text(text = it.first.label, color = MaterialTheme.colorScheme.primary) },
//                        onClick = {}, enabled = false
//                    )
//                    if (settingsIntent != null) {
//                        DropdownMenuItem(
//                            text = { Text(stringResource(R.string.manage_module_settings)) },
//                            onClick = { context.startActivity(settingsIntent) }
//                        )
//                    }
//                    DropdownMenuItem(
//                        text = { Text(stringResource(R.string.manage_app_info)) },
//                        onClick = {
//                            val intent = Intent(
//                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                                Uri.fromParts("package", it.first.info.packageName, null)
//                            )
//                            context.startActivity(intent)
//                        }
//                    )
//                }
//            }
//        }
}
