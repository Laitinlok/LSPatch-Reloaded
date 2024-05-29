package org.lsposed.lspatch.ui.page.logs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.lsposed.lspatch.ui.component.CenterTopBar
import org.lsposed.lspatch.ui.page.BottomBarDestination

@Composable
fun LogsScreen() {
    Scaffold(
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Logs.label)) }
    ) { innerPadding ->
        Text(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            text = "This page is not yet implemented",
            textAlign = TextAlign.Center
        )
    }
}
