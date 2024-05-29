package org.lsposed.lspatch.util

import android.content.Context
import java.io.File
import java.io.InputStream

object FileUtils {
    fun copyToTmpFile(context: Context, input: InputStream): File {
        val tmpFile = File.createTempFile(context.packageName, null, context.cacheDir)
        tmpFile.deleteOnExit()

        tmpFile.outputStream().use { output ->
            input.copyTo(output)
        }

        return tmpFile
    }
}