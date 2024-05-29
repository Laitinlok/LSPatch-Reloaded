package org.lsposed.lspatch

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.config.Configs
import org.lsposed.lspatch.config.MyKeyStore
import org.lsposed.lspatch.share.Constants
import org.lsposed.lspatch.share.PatchConfig
import org.lsposed.patch.LSPatch
import org.lsposed.patch.util.Logger
import java.io.IOException
import javax.inject.Inject

class Patcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStore: MyKeyStore,
    private val configs: Configs,
) {

    data class Options(
        val config: PatchConfig,
        val apkPaths: List<String>,
        val embeddedModules: List<String>?
    )

    private fun optionsToStringArray(options: Options): Array<String> {
        return with(options) {
            buildList {
                addAll(apkPaths)
                add("-o"); add(context.cacheDir.resolve("apk").absolutePath)
                if (config.debuggable) add("-d")
                add("-l"); add(config.sigBypassLevel.toString())
                if (config.useManager) add("--manager")
                if (config.overrideVersionCode) add("-r")
                if (configs.detailPatchLogs) add("-v")
                embeddedModules?.forEach {
                    add("-m"); add(it)
                }
                if (!keyStore.useDefault) {
                    addAll(arrayOf("-k", keyStore.file.path, configs.keyStorePassword, configs.keyStoreAlias, configs.keyStoreAliasPassword))
                }
            }.toTypedArray()
        }
    }

    suspend fun patch(logger: Logger, options: Options) {
        withContext(Dispatchers.IO) {
            LSPatch(logger, *optionsToStringArray(options)).doCommandLine()

            val uri = configs.storageDirectory?.toUri()
                ?: throw IOException("Uri is null")
            val root = DocumentFile.fromTreeUri(context, uri)
                ?: throw IOException("DocumentFile is null")
            root.listFiles().forEach {
                if (it.name?.endsWith(Constants.PATCH_FILE_SUFFIX) == true) it.delete()
            }
            context.cacheDir.resolve("apk").walk()
                .filter { it.name.endsWith(Constants.PATCH_FILE_SUFFIX) }
                .forEach { apk ->
                    val file = root.createFile("application/vnd.android.package-archive", apk.name)
                        ?: throw IOException("Failed to create output file")
                    val output = context.contentResolver.openOutputStream(file.uri)
                        ?: throw IOException("Failed to open output stream")
                    output.use {
                        apk.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                }
            logger.i("Patched files are saved to ${root.uri.lastPathSegment}")
        }
    }
}
