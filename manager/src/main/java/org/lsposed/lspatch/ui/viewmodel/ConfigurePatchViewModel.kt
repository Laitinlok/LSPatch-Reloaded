package org.lsposed.lspatch.ui.viewmodel

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lsposed.lspatch.ui.page.manage.patch.PatchAppProcessData
import org.lsposed.lspatch.util.LSPPackageManager
import javax.inject.Inject

@HiltViewModel
class ConfigurePatchViewModel @Inject constructor(
    private val patchAppProcessData: PatchAppProcessData,
    private val lspPackageManager: LSPPackageManager
) : ViewModel() {
    val application = patchAppProcessData.application ?: error("application not set")

    private val _mode = MutableStateFlow(patchAppProcessData.mode)
    val mode = _mode.asStateFlow()

    private val _debuggable = MutableStateFlow(patchAppProcessData.debuggable)
    val debuggable = _debuggable.asStateFlow()

    private val _overrideVersionCode = MutableStateFlow(patchAppProcessData.overrideVersionCode)
    val overrideVersionCode = _overrideVersionCode.asStateFlow()

    private val _signatureBypassLevel = MutableStateFlow(patchAppProcessData.signatureBypassLevel)
    val signatureBypassLevel = _signatureBypassLevel.asStateFlow()

    fun loadIcon(applicationInfo: ApplicationInfo): Bitmap {
        return lspPackageManager.loadIcon(applicationInfo)
    }

    fun setMode(mode: PatchMode) {
        _mode.value = mode
        patchAppProcessData.mode = mode
    }

    fun setDebuggable(debuggable: Boolean) {
        _debuggable.value = debuggable
        patchAppProcessData.debuggable = debuggable
    }

    fun setOverrideVersionCode(overrideVersionCode: Boolean) {
        _overrideVersionCode.value = overrideVersionCode
        patchAppProcessData.overrideVersionCode = overrideVersionCode
    }

    fun setSignatureBypassLevel(level: Int) {
        _signatureBypassLevel.value = level
        patchAppProcessData.signatureBypassLevel = level
    }
}
