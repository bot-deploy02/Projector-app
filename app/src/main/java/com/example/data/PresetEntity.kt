package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projector_presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val scale: Float = 1.0f,
    val mirrorX: Boolean = false,
    val mirrorY: Boolean = false,
    val rotation: Float = 0.0f,
    val translationX: Float = 0.0f,
    val translationY: Float = 0.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val brightness: Float = 1.0f,
    val isSystemPreset: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
