package org.lsposed.lspatch.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.ui.page.manage.patch.PatchAppProcessData
import org.lsposed.lspatch.util.FileUtils
import org.lsposed.lspatch.util.LSPPackageManager
import java.io.File
import javax.inject.Inject

private const val TAG = "SelectAppViewModel"

@HiltViewModel
class SelectAppViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val patchAppProcessData: PatchAppProcessData,
    private val lspPackageManager: LSPPackageManager
) : ViewModel() {
    var deepLinkUri = savedStateHandle.get<Intent?>(NavController.KEY_DEEP_LINK_INTENT).let {
        if (it == null || it.type != "application/vnd.android.package-archive") null else it.data
    }
        private set

    val refreshing = lspPackageManager.loadingInstalledApplications

    val apps = lspPackageManager.installedApplications.map { apps ->
        apps.filter {
            it.info.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    && it.info.metaData != null
                    && !it.info.metaData.containsKey("xposedminversion")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

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
        setApplicationInfo(applicationInfo)
        return@withContext true
    }

    suspend fun loadApks(uris: List<Uri>): Boolean = withContext(Dispatchers.IO) {
        var baseApplicationInfo: ApplicationInfo? = null
        val splits = mutableListOf<String>()

        for (uri in uris) {
            val tmpFile = createTmpFile(uri) ?: continue

            val packageInfo = context.packageManager.getPackageArchiveInfo(
                tmpFile.absolutePath, PackageManager.GET_META_DATA
            )

            if (packageInfo == null) {
                splits.add(tmpFile.absolutePath)
                continue
            }

            if (baseApplicationInfo == null) {
                val applicationInfo = packageInfo.applicationInfo
                applicationInfo.sourceDir = tmpFile.absolutePath
                applicationInfo.publicSourceDir = tmpFile.absolutePath
                baseApplicationInfo = applicationInfo
            } else {
                Log.w(TAG, "Multiple base apks found")
            }
        }

        if (baseApplicationInfo == null) {
            Log.e(TAG, "No base apk found")
            return@withContext false
        }

        baseApplicationInfo.splitSourceDirs = splits.toTypedArray()
        baseApplicationInfo.splitPublicSourceDirs = splits.toTypedArray()
        setApplicationInfo(baseApplicationInfo)
        return@withContext true
    }

    fun consumeDeepLink() {
        deepLinkUri = null
    }

    fun refresh() {
        viewModelScope.launch {
            lspPackageManager.fetchAppList()
        }
    }

    fun loadIcon(applicationInfo: ApplicationInfo): Bitmap {
        return lspPackageManager.loadIcon(applicationInfo)
    }

    fun setApplicationInfo(applicationInfo: ApplicationInfo) {
        patchAppProcessData.application = applicationInfo
    }
}
