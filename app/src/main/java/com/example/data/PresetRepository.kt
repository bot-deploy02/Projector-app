package com.example.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresetsFlow: Flow<List<PresetEntity>> = presetDao.getAllPresetsFlow()

    suspend fun getAllPresets(): List<PresetEntity> = presetDao.getAllPresets()

    suspend fun insertPreset(preset: PresetEntity) {
        presetDao.insertPreset(preset)
    }

    suspend fun deletePresetById(id: Int) {
        presetDao.deletePresetById(id)
    }
}
