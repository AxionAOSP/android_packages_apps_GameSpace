package com.android.axion.gamespace.utils.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.android.axion.gamespace.data.AppSettings
import com.android.axion.gamespace.data.SystemSettings
import com.android.axion.gamespace.utils.GameModeUtils
import com.android.axion.gamespace.utils.ScreenUtils


@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceViewEntryPoint {
    fun appSettings(): AppSettings
    fun systemSettings(): SystemSettings
    fun screenUtils(): ScreenUtils
    fun gameModeUtils(): GameModeUtils
}
