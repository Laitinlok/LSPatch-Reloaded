package org.lsposed.lspatch.manager

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import org.lsposed.lspatch.config.ConfigManager
import javax.inject.Inject

private const val TAG = "ModuleService"

@AndroidEntryPoint
class ModuleService : Service() {
    @Inject lateinit var configManager: ConfigManager

    override fun onBind(intent: Intent): IBinder? {
        val packageName = intent.getStringExtra("packageName") ?: return null
        // TODO: Authentication
        Log.i(TAG, "$packageName requests binder")
        return ManagerService(this, configManager).asBinder()
    }
}
