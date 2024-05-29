package org.lsposed.lspatch.ui.page.manage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.CenterTopBar
import org.lsposed.lspatch.ui.page.BottomBarDestination

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageHomeScreen(
    onNavigateToNewPatch: () -> Unit,
) {
    val pages = listOf(
        R.string.apps,
        R.string.modules
    )

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = pages::size)
    Scaffold(
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Manage.label)) },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                ManageAppsFab(
                    navigateToManageAppsSelect = onNavigateToNewPatch,
                )
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            TabRow(
                contentColor = MaterialTheme.colorScheme.secondary,
                selectedTabIndex = pagerState.currentPage
            ) {
                pages.forEachIndexed { index, page ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 16.dp),
                            text = stringResource(page)
                        )
                    }
                }
            }

            HorizontalPager(pagerState) { page ->
                when (page) {
                    0 -> ManageAppsPage()
                    1 -> ManageModulesPage()
                }
            }
        }
    }
}
