package org.lsposed.lspatch.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isSearching by remember { mutableStateOf(searchText.isNotEmpty()) }

    if (isSearching) {
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    }

    DisposableEffect(keyboardController) {
        onDispose {
            keyboardController?.hide()
        }
    }

    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterStart),
                    visible = !isSearching,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    content = { title() }
                )

                AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        placeholder = { Text("Search") },
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )
                }
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            AnimatedVisibility(
                visible = !isSearching,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { isSearching = true },
                ) { Icon(Icons.Filled.Search, "Search") }
            }

            AnimatedVisibility(
                visible = isSearching,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {
                        isSearching = false
                        keyboardController?.hide()
                        onSearchTextChange("")
                    },
                    content = { Icon(Icons.Filled.Close, null) }
                )
            }
        }
    )
}

@Preview
@Composable
private fun SearchAppBarPreview() {
    var searchText by remember { mutableStateOf("") }
    SearchAppBar(
        title = { Text("Search text") },
        navigationIcon = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Up")
            }
        },
        searchText = searchText,
        onSearchTextChange = { searchText = it },
    )
}

@Preview
@Composable
private fun SearchAppBarWithTextPreview() {
    var searchText by remember { mutableStateOf("LSPatch") }
    SearchAppBar(
        title = { Text("Search text") },
        navigationIcon = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Up")
            }
        },
        searchText = searchText,
        onSearchTextChange = { searchText = it },
    )
}
