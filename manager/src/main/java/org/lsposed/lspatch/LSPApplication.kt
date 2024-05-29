package org.lsposed.lspatch

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.lsposed.lspatch.manager.AppBroadcastReceiver
import org.lsposed.lspatch.util.HiddenApi
import org.lsposed.lspatch.util.ShizukuApi

@HiltAndroidApp
class LSPApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        HiddenApi.unseal()
    }

    override fun onCreate() {
        super.onCreate()
        cacheDir.resolve("apk").mkdir()
        ShizukuApi.init()
        AppBroadcastReceiver.register(this)
    }
}
