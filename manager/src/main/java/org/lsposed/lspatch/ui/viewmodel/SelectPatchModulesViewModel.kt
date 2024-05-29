package org.lsposed.lspatch.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.ui.page.manage.patch.PatchAppProcessData
import org.lsposed.lspatch.util.FileUtils
import org.lsposed.lspatch.util.LSPPackageManager
import java.io.File
import javax.inject.Inject

private const val TAG = "NewPatchViewModel"

@HiltViewModel
class SelectPatchModulesViewModel @Inject constructor(
    private val patchAppProcessData: PatchAppProcessData,
    @ApplicationContext private val context: Context,
    private val lspPackageManager: LSPPackageManager
) : ViewModel() {
    val refreshing = lspPackageManager.loadingInstalledApplications

    //appInfo to XposedInfo(
    //                metaData.getInt("xposedminversion", -1).also { if (it == -1) return@mapNotNull null },
    //                metaData.getString("xposeddescription") ?: "",
    //                emptyList() // TODO: scope
    //            )

    val installedApplications = lspPackageManager.installedApplications.map { apps ->
        apps.filter {
            it.info.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    && it.info.metaData != null
                    && it.info.metaData.containsKey("xposedminversion")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    private val _loadedModules = MutableStateFlow(emptyList<ApplicationInfo>())
    val loadedModules = _loadedModules.asStateFlow()


    private val _modules = MutableStateFlow(patchAppProcessData.modules)
    val modules = _modules.asStateFlow()

    private fun createTmpFile(uri: Uri): File? {
        return try {
            context.contentResolver.openInputStream(uri).use {
                if (it == null) {
                    Log.w(TAG, "Failed to open input stream for $uri")
                    return null
                }
                FileUtils.copyToTmpFile(context, it)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadApk(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val tmpFile = createTmpFile(uri) ?: return@withContext false

        val packageInfo = context.packageManager.getPackageArchiveInfo(
            tmpFile.absolutePath, PackageManager.GET_META_DATA
        )

        if (packageInfo == null) {
            Log.w(TAG, "Invalid apk file: $tmpFile")
            return@withContext false
        }

        val applicationInfo = packageInfo.applicationInfo
        applicationInfo.sourceDir = tmpFile.absolutePath
        applicationInfo.publicSourceDir = tmpFile.absolutePath
        setModules(modules.value.toMutableList().apply {
            add(applicationInfo)
        })
        return@withContext true
    }

    fun refresh() {
        viewModelScope.launch {
            lspPackageManager.fetchAppList()
        }
    }

    fun loadIcon(applicationInfo: ApplicationInfo): Bitmap {
        return lspPackageManager.loadIcon(applicationInfo)
    }

    fun loadLabel(applicationInfo: ApplicationInfo): CharSequence {
        return applicationInfo.loadLabel(context.packageManager)
    }

    fun setModules(modules: List<ApplicationInfo>) {
        _modules.value = modules
        patchAppProcessData.modules = modules
    }
}
