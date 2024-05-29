package org.lsposed.lspatch.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.lsposed.lspatch.ui.page.manage.patch.PatchAppProcessData

@Module
@InstallIn(ActivityRetainedComponent::class)
class ProcessModule {

    @Provides
    @ActivityRetainedScoped
    fun providePatchAppProcessData(): PatchAppProcessData {
        return PatchAppProcessData()
    }
}