package org.lsposed.lspatch.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.lsposed.lspatch.Patcher
import org.lsposed.lspatch.share.PatchConfig
import org.lsposed.lspatch.ui.page.manage.patch.PatchAppProcessData
import org.lsposed.lspatch.util.LSPPackageManager
import org.lsposed.patch.util.Logger
import javax.inject.Inject

data class PatchLogEntry(
    val type: Type,
    val message: String
) {
    enum class Type {
        DEBUG,
        INFO,
        ERROR
    }
}

private const val TAG = "PatchViewModel"

@HiltViewModel
class PatchViewModel @Inject constructor(
    private val patchAppProcessData: PatchAppProcessData,
    @ApplicationContext private val context: Context,
    private val patcher: Patcher
) : ViewModel() {
    private val _logs = MutableStateFlow(emptyList<PatchLogEntry>())
    val logs = _logs.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val logger = object : Logger() {
        override fun d(msg: String) {
            Log.d(TAG, msg)
            _logs.value += PatchLogEntry(PatchLogEntry.Type.DEBUG, msg)
        }

        override fun i(msg: String) {
            Log.i(TAG, msg)
            _logs.value += PatchLogEntry(PatchLogEntry.Type.INFO, msg)
        }

        override fun e(msg: String) {
            Log.e(TAG, msg)
            _logs.value += PatchLogEntry(PatchLogEntry.Type.ERROR, msg)
        }
    }

    fun startPatch() {
        viewModelScope.launch(Dispatchers.IO) {
            val application = requireNotNull(patchAppProcessData.application)
            try {
                patcher.patch(
                    logger = logger,
                    options = Patcher.Options(
                        config = PatchConfig(
                            patchAppProcessData.mode == PatchMode.LOCAL,
                            patchAppProcessData.debuggable,
                            patchAppProcessData.overrideVersionCode,
                            patchAppProcessData.signatureBypassLevel,
                            null, null
                        ),
                        apkPaths = listOf(application.publicSourceDir) + (application.splitPublicSourceDirs?.toList() ?: emptyList()),
                        embeddedModules = patchAppProcessData.modules.flatMap {
                            listOf(it.publicSourceDir) + (it.splitPublicSourceDirs?.toList() ?: emptyList())
                        }
                    )
                )
                _isFinished.value = true
            } catch (e: Exception) {
                logger.e("Failed to patch: ${e.message}")
            }
        }
    }

}