package org.lsposed.lspatch.ui.page.manage.patch

import android.content.pm.ApplicationInfo
import org.lsposed.lspatch.ui.viewmodel.PatchMode

data class PatchAppProcessData(
    var application: ApplicationInfo? = null,
    var mode: PatchMode = PatchMode.LOCAL,
    var modules: List<ApplicationInfo> = emptyList(),
    var debuggable: Boolean = false,
    var overrideVersionCode: Boolean = false,
    var signatureBypassLevel: Int = 2,
)
