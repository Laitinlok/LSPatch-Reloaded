package org.lsposed.lspatch.ui.viewmodel.manage

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lsposed.lspatch.util.LSPPackageManager
import javax.inject.Inject

private const val TAG = "ManageModulesViewModel"

@HiltViewModel
class ManageModulesViewModel @Inject constructor(
    private val lspPackageManager: LSPPackageManager
) : ViewModel() {
    val refreshing = lspPackageManager.loadingInstalledApplications
    val modules = lspPackageManager.installedApplications.map { applications ->
        applications.mapNotNull { appInfo ->
            val metaData = appInfo.info.metaData ?: return@mapNotNull null
            val xposedminversion = metaData.getInt("xposedminversion", -1)
            if (xposedminversion == -1) return@mapNotNull null
            val xposeddescription = metaData.getString("xposeddescription")

            appInfo to XposedInfo(
                xposedminversion,
                xposeddescription,
                emptyList() // TODO: scope
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun refresh() {
        viewModelScope.launch {
            lspPackageManager.fetchAppList()
        }
    }

    fun loadIcon(applicationInfo: ApplicationInfo): Bitmap {
        return lspPackageManager.loadIcon(applicationInfo)
    }

    class XposedInfo(
        val api: Int,
        val description: String?,
        val scope: List<String>
    )
}
