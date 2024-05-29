package org.lsposed.lspatch.manager

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREFS_STORAGE_DIRECTORY = "storage_directory"

class PatchManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("patch_manager", Context.MODE_PRIVATE)

    var storageDirectory: Uri? = prefs.getString(PREFS_STORAGE_DIRECTORY, null)?.let { Uri.parse(it) }
        set(value) {
            field = value
            prefs.edit().putString(PREFS_STORAGE_DIRECTORY, value?.toString()).apply()
        }

    fun hasStorageDirectory(): Boolean {
        val storageDirectory = this.storageDirectory
        return storageDirectory != null && DocumentFile.fromTreeUri(context, storageDirectory) != null
    }
}