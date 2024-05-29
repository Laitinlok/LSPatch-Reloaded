package org.lsposed.lspatch.ui.viewmodel.manage

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.Patcher
import org.lsposed.lspatch.manager.PatchManager
import org.lsposed.lspatch.share.Constants
import org.lsposed.lspatch.share.PatchConfig
import org.lsposed.lspatch.ui.viewstate.ProcessingState
import org.lsposed.lspatch.util.AppInfo
import org.lsposed.lspatch.util.LSPPackageManager
import org.lsposed.lspatch.util.ShizukuApi
import org.lsposed.patch.util.Logger
import java.io.FileNotFoundException
import java.util.zip.ZipFile
import javax.inject.Inject

private const val TAG = "ManageViewModel"

@HiltViewModel
class ManageAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lspPackageManager: LSPPackageManager,
    private val patcher: Patcher,
    private val patchManager: PatchManager
) : ViewModel() {
    val refreshing = lspPackageManager.loadingInstalledApplications
    val installedApplications = lspPackageManager.installedApplications.map { applications ->
        applications.mapNotNull { appInfo ->
            appInfo.info.metaData?.getString("lspatch")?.let { lspatchData ->
                val json = Base64.decode(lspatchData, Base64.DEFAULT).toString(Charsets.UTF_8)
                Log.d(TAG, "Read patched config: $json")
                appInfo to Gson().fromJson(json, PatchConfig::class.java)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val hasStorageDirectory: Boolean
        get() = patchManager.hasStorageDirectory()

    fun refresh() {
        viewModelScope.launch {
            lspPackageManager.fetchAppList()
        }
    }

    fun loadIcon(applicationInfo: ApplicationInfo): Bitmap {
        return lspPackageManager.loadIcon(applicationInfo)
    }

    fun setStorageDirectory(uri: Uri) {
        patchManager.storageDirectory = uri
    }

    sealed class ViewAction {
        data class UpdateLoader(val appInfo: AppInfo, val config: PatchConfig) : ViewAction()
        object ClearUpdateLoaderResult : ViewAction()
        data class PerformOptimize(val appInfo: AppInfo) : ViewAction()
        object ClearOptimizeResult : ViewAction()
    }

//    val appList: List<Pair<AppInfo, PatchConfig>> by derivedStateOf {
//        lspPackageManager.installedApplications.mapNotNull { appInfo ->
//            appInfo.info.metaData?.getString("lspatch")?.let {
//                val json = Base64.decode(it, Base64.DEFAULT).toString(Charsets.UTF_8)
//                Log.d(TAG, "Read patched config: $json")
//                appInfo to Gson().fromJson(json, PatchConfig::class.java)
//            }
//        }.also {
//            Log.d(TAG, "Loaded ${it.size} patched apps")
//        }
//    }

    var updateLoaderState: ProcessingState<Result<Unit>> by mutableStateOf(ProcessingState.Idle)
        private set

    var optimizeState: ProcessingState<Boolean> by mutableStateOf(ProcessingState.Idle)
        private set

    private val logger = object : Logger() {
        override fun d(msg: String) {
            if (verbose) Log.d(TAG, msg)
        }

        override fun i(msg: String) {
            Log.i(TAG, msg)
        }

        override fun e(msg: String) {
            Log.e(TAG, msg)
        }
    }

    fun dispatch(action: ViewAction) {
        viewModelScope.launch {
            when (action) {
                is ViewAction.UpdateLoader -> updateLoader(action.appInfo, action.config)
                is ViewAction.ClearUpdateLoaderResult -> updateLoaderState = ProcessingState.Idle
                is ViewAction.PerformOptimize -> performOptimize(action.appInfo)
                is ViewAction.ClearOptimizeResult -> optimizeState = ProcessingState.Idle
            }
        }
    }

    private suspend fun updateLoader(appInfo: AppInfo, config: PatchConfig) {
        Log.i(TAG, "Update loader for ${appInfo.info.packageName}")
        updateLoaderState = ProcessingState.Processing
        val result = runCatching {
            withContext(Dispatchers.IO) {
                lspPackageManager.cleanTmpApkDir()
                val apkPaths = listOf(appInfo.info.sourceDir) + (appInfo.info.splitSourceDirs ?: emptyArray())
                val patchPaths = mutableListOf<String>()
                val embeddedModulePaths = mutableListOf<String>()
                for (apk in apkPaths) {
                    ZipFile(apk).use { zip ->
                        var entry = zip.getEntry(Constants.ORIGINAL_APK_ASSET_PATH)
                        if (entry == null) entry = zip.getEntry("assets/lspatch/origin_apk.bin")
                        if (entry == null) throw FileNotFoundException("Original apk entry not found for $apk")
                        zip.getInputStream(entry).use { input ->
                            val dst = context.cacheDir.resolve("apk").resolve(apk.substringAfterLast('/'))
                            patchPaths.add(dst.absolutePath)
                            dst.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
                ZipFile(appInfo.info.sourceDir).use { zip ->
                    zip.entries().iterator().forEach { entry ->
                        if (entry.name.startsWith(Constants.EMBEDDED_MODULES_ASSET_PATH)) {
                            val dst = context.cacheDir.resolve("apk").resolve(entry.name.substringAfterLast('/'))
                            embeddedModulePaths.add(dst.absolutePath)
                            zip.getInputStream(entry).use { input ->
                                dst.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                }
                patcher.patch(logger, Patcher.Options(config, patchPaths, embeddedModulePaths))
                val (status, message) = lspPackageManager.install()
                if (status != PackageInstaller.STATUS_SUCCESS) throw RuntimeException(message)
            }
        }
        updateLoaderState = ProcessingState.Done(result)
    }

    private suspend fun performOptimize(appInfo: AppInfo) {
        Log.i(TAG, "Perform optimize for ${appInfo.info.packageName}")
        optimizeState = ProcessingState.Processing
        val result = withContext(Dispatchers.IO) {
            ShizukuApi.performDexOptMode(appInfo.info.packageName)
        }
        optimizeState = ProcessingState.Done(result)
    }
}
