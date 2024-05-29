package org.lsposed.lspatch.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lsposed.lspatch.util.LSPPackageManager
import javax.inject.Inject

@AndroidEntryPoint
class AppBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.Default)
    @Inject lateinit var lspPackageManager: LSPPackageManager

    companion object {
        private const val TAG = "AppBroadcastReceiver"

        private val actions = setOf(
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED
        )

        fun register(context: Context) {
            val filter = IntentFilter().apply {
                actions.forEach(::addAction)
                addDataScheme("package")
            }
            context.registerReceiver(AppBroadcastReceiver(), filter)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in actions) {
            scope.launch {
                Log.i(TAG, "Received intent: $intent")
                lspPackageManager.fetchAppList()
            }
        }
    }
}
