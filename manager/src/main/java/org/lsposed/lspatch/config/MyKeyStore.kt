package org.lsposed.lspatch.config

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyKeyStore @Inject constructor(
    @ApplicationContext context: Context,
    private val configs: Configs
) {

    val file = File("${context.filesDir}/keystore.bks")
    val tmpFile = File("${context.filesDir}/keystore.bks.tmp")

    var useDefault by mutableStateOf(!file.exists())
        private set

    suspend fun reset() {
        withContext(Dispatchers.IO) {
            file.delete()
            configs.keyStorePassword = "123456"
            configs.keyStoreAlias = "key0"
            configs.keyStoreAliasPassword = "123456"
            useDefault = true
        }
    }

    suspend fun setCustom(password: String, alias: String, aliasPassword: String) {
        withContext(Dispatchers.IO) {
            tmpFile.renameTo(file)
            configs.keyStorePassword = password
            configs.keyStoreAlias = alias
            configs.keyStoreAliasPassword = aliasPassword
            useDefault = false
        }
    }
}
