package org.lsposed.lspatch.ui.page.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.lsposed.lspatch.config.Configs
import org.lsposed.lspatch.config.MyKeyStore
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val keyStore: MyKeyStore,
    val configs: Configs
) : ViewModel() {
    val useDefaultKeyStore: Boolean
        get() = keyStore.useDefault

    val tmpFileKeyStore: File
        get() = keyStore.tmpFile

    suspend fun resetKeyStore() {
        keyStore.reset()
    }

    suspend fun setCustomKeyStore(password: String, alias: String, aliasPassword: String) {
        keyStore.setCustom(password, alias, aliasPassword)
    }
}