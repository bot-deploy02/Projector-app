package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM projector_presets ORDER BY timestamp DESC")
    fun getAllPresetsFlow(): Flow<List<PresetEntity>>

    @Query("SELECT * FROM projector_presets ORDER BY timestamp DESC")
    suspend fun getAllPresets(): List<PresetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity)

    @Query("DELETE FROM projector_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)
}
