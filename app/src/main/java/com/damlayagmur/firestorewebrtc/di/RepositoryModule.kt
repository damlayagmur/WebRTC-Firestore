package com.damlayagmur.firestorewebrtc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import org.webrtc.EglBase
import android.content.Context
import com.damlayagmur.firestorewebrtc.data.repository.CallRepositoryImpl
import com.damlayagmur.firestorewebrtc.data.repository.MainRepositoryImpl
import com.damlayagmur.firestorewebrtc.domain.CallRepository
import com.damlayagmur.firestorewebrtc.domain.MainRepository

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideCallRepository(
        @ApplicationContext context: Context,
        eglBase: EglBase
    ): CallRepository =
        CallRepositoryImpl(context, eglBase)

    @Provides
    @ViewModelScoped
    fun provideMainRepository(@ApplicationContext context: Context): MainRepository =
        MainRepositoryImpl(context)
}