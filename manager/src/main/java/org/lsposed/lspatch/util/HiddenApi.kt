package org.lsposed.lspatch.util

import android.os.Build
import android.util.Log
import android.util.Property
import java.lang.reflect.Method

object HiddenApi {
    private const val TAG = "HiddenApi"

    fun unseal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        try {
            val methods = Property.of(Class::class.java, Array<Method>::class.java, "Methods")
                .get(Class.forName("dalvik.system.VMRuntime"))
            val runtime = methods.first { it.name == "getRuntime" }.invoke(null)
            methods.first { it.name == "setHiddenApiExemptions" }.invoke(runtime, arrayOf("L"))
            Log.d(TAG, "unseal: success")
        } catch (e: Exception) {
            Log.e(TAG, "unseal: failed", e)
        }
    }
}