package org.lsposed.lspatch.config

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.lsposed.lspatch.ui.util.delegateStateOf
import org.lsposed.lspatch.ui.util.getValue
import org.lsposed.lspatch.ui.util.setValue
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_KEYSTORE_PASSWORD = "keystore_password"
private const val PREFS_KEYSTORE_ALIAS = "keystore_alias"
private const val PREFS_KEYSTORE_ALIAS_PASSWORD = "keystore_alias_password"
private const val PREFS_STORAGE_DIRECTORY = "storage_directory"
private const val PREFS_DETAIL_PATCH_LOGS = "detail_patch_logs"

@Singleton
class Configs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var keyStorePassword by delegateStateOf(prefs.getString(PREFS_KEYSTORE_PASSWORD, "123456")!!) {
        prefs.edit().putString(PREFS_KEYSTORE_PASSWORD, it).apply()
    }

    var keyStoreAlias by delegateStateOf(prefs.getString(PREFS_KEYSTORE_ALIAS, "key0")!!) {
        prefs.edit().putString(PREFS_KEYSTORE_ALIAS, it).apply()
    }

    var keyStoreAliasPassword by delegateStateOf(prefs.getString(PREFS_KEYSTORE_ALIAS_PASSWORD, "123456")!!) {
        prefs.edit().putString(PREFS_KEYSTORE_ALIAS_PASSWORD, it).apply()
    }

    var storageDirectory by delegateStateOf(prefs.getString(PREFS_STORAGE_DIRECTORY, null)) {
        prefs.edit().putString(PREFS_STORAGE_DIRECTORY, it).apply()
    }

    var detailPatchLogs by delegateStateOf(prefs.getBoolean(PREFS_DETAIL_PATCH_LOGS, true)) {
        prefs.edit().putBoolean(PREFS_DETAIL_PATCH_LOGS, it).apply()
    }
}
