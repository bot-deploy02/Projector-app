package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresetEntity
import com.example.data.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SampleVideo(
    val title: String,
    val description: String,
    val url: String
)

class ProjectorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PresetRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PresetRepository(database.presetDao())
        seedDefaultPresets()
    }

    // List of reliable high-quality sample video clips for immediate testing
    val sampleVideos = listOf(
        SampleVideo(
            title = "Big Buck Bunny",
            description = "Bright cartoon with vibrant colors, excellent for testing projector lens sharpness.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        SampleVideo(
            title = "Sintel (Sci-Fi Movie)",
            description = "Detailed lighting, cinematic shadows, and contrast tests for wall projections.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        SampleVideo(
            title = "Elephants Dream",
            description = "High-saturation surreal artwork, great for testing projector color saturation and focus.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        SampleVideo(
            title = "Projector Grid Pattern",
            description = "A synthetic calibration grid clip (Subaru Driving) for fine tuning geometry.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
        )
    )

    // Reactive Flow of all presets stored in the DB
    val savedPresets: StateFlow<List<PresetEntity>> = repository.allPresetsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Video Playback and Adjustment States
    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri = _videoUri.asStateFlow()

    private val _videoTitle = MutableStateFlow("")
    val videoTitle = _videoTitle.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    // Calibration Parameters
    private val _scale = MutableStateFlow(1.0f) // zoom factor
    val scale = _scale.asStateFlow()

    private val _mirrorX = MutableStateFlow(false) // Left-to-Right Flip
    val mirrorX = _mirrorX.asStateFlow()

    private val _mirrorY = MutableStateFlow(false) // Up-to-Down Flip
    val mirrorY = _mirrorY.asStateFlow()

    private val _rotation = MutableStateFlow(0.0f) // Fine tilt adjustment (-15 to +15 deg)
    val rotation = _rotation.asStateFlow()

    private val _translationX = MutableStateFlow(0.0f) // Centering X offset
    val translationX = _translationX.asStateFlow()

    private val _translationY = MutableStateFlow(0.0f) // Centering Y offset
    val translationY = _translationY.asStateFlow()

    // Color/Power Correction
    private val _contrast = MutableStateFlow(1.0f) // 0.5 to 2.5
    val contrast = _contrast.asStateFlow()

    private val _saturation = MutableStateFlow(1.0f) // 0.0 to 3.0
    val saturation = _saturation.asStateFlow()

    private val _brightness = MutableStateFlow(1.0f) // Screen override (0.0 to 1.0)
    val brightness = _brightness.asStateFlow()

    // Interface visibility (crucial to completely hide UI during projection)
    private val _controlsVisible = MutableStateFlow(true)
    val controlsVisible = _controlsVisible.asStateFlow()

    private val _isCalibrating = MutableStateFlow(false) // Show grid overlay
    val isCalibrating = _isCalibrating.asStateFlow()

    // Preset seeding logic to give users awesome templates instantly
    private fun seedDefaultPresets() {
        viewModelScope.launch {
            val existing = repository.getAllPresets()
            if (existing.isEmpty()) {
                repository.insertPreset(
                    PresetEntity(
                        name = "🔍 Default (Full Screen)",
                        scale = 1.0f,
                        mirrorX = false,
                        mirrorY = false,
                        contrast = 1.0f,
                        saturation = 1.0f,
                        isSystemPreset = true
                    )
                )
                repository.insertPreset(
                    PresetEntity(
                        name = "📦 DIY Projector (Recommended)",
                        scale = 0.85f, // Zoom out to avoid rounded corner clipping
                        mirrorX = true, // Double flipped horizontally
                        mirrorY = true, // And vertically to correct magnifying lens physics
                        contrast = 1.3f, // Boost contrast for wall brightness loss
                        saturation = 1.2f, // Saturation boost
                        isSystemPreset = true
                    )
                )
                repository.insertPreset(
                    PresetEntity(
                        name = "🪞 Mirror Player (L-R Flip)",
                        scale = 1.0f,
                        mirrorX = true,
                        mirrorY = false,
                        contrast = 1.0f,
                        saturation = 1.0f,
                        isSystemPreset = true
                    )
                )
                repository.insertPreset(
                    PresetEntity(
                        name = "🤸 Upside-Down Flip (Lens)",
                        scale = 1.0f,
                        mirrorX = false,
                        mirrorY = true,
                        contrast = 1.0f,
                        saturation = 1.0f,
                        isSystemPreset = true
                    )
                )
                repository.insertPreset(
                    PresetEntity(
                        name = "⏹️ Zoomed Out (Square Corners)",
                        scale = 0.70f, // Heavy zoom-out to ensure sharp rectangular borders
                        mirrorX = false,
                        mirrorY = false,
                        contrast = 1.1f,
                        saturation = 1.0f,
                        isSystemPreset = true
                    )
                )
            }
        }
    }

    // Media Loaders
    fun loadVideo(uri: Uri, title: String) {
        _videoUri.value = uri
        _videoTitle.value = title
        _isPlaying.value = true
        _controlsVisible.value = true
    }

    fun loadSampleVideo(sample: SampleVideo) {
        loadVideo(Uri.parse(sample.url), sample.title)
    }

    fun clearVideo() {
        _videoUri.value = null
        _videoTitle.value = ""
        _isPlaying.value = false
    }

    // Controls
    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun updatePlaybackProgress(position: Long, totalDuration: Long) {
        _currentPosition.value = position
        _duration.value = totalDuration
    }

    fun setControlsVisible(visible: Boolean) {
        _controlsVisible.value = visible
    }

    fun toggleControls() {
        _controlsVisible.value = !_controlsVisible.value
    }

    fun setCalibrating(calibrating: Boolean) {
        _isCalibrating.value = calibrating
    }

    // Parameter setters
    fun setScale(value: Float) {
        _scale.value = value.coerceIn(0.2f, 1.5f)
    }

    fun setMirrorX(value: Boolean) {
        _mirrorX.value = value
    }

    fun setMirrorY(value: Boolean) {
        _mirrorY.value = value
    }

    fun setRotation(value: Float) {
        _rotation.value = value.coerceIn(-30.0f, 30.0f)
    }

    fun setTranslationX(value: Float) {
        _translationX.value = value
    }

    fun setTranslationY(value: Float) {
        _translationY.value = value
    }

    fun setContrast(value: Float) {
        _contrast.value = value.coerceIn(0.5f, 2.5f)
    }

    fun setSaturation(value: Float) {
        _saturation.value = value.coerceIn(0.0f, 3.0f)
    }

    fun setBrightness(value: Float) {
        _brightness.value = value.coerceIn(0.0f, 1.0f)
    }

    // Load active settings from a Preset
    fun applyPreset(preset: PresetEntity) {
        _scale.value = preset.scale
        _mirrorX.value = preset.mirrorX
        _mirrorY.value = preset.mirrorY
        _rotation.value = preset.rotation
        _translationX.value = preset.translationX
        _translationY.value = preset.translationY
        _contrast.value = preset.contrast
        _saturation.value = preset.saturation
        _brightness.value = preset.brightness
    }

    // Save current settings as a new custom Preset
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            val preset = PresetEntity(
                name = name,
                scale = _scale.value,
                mirrorX = _mirrorX.value,
                mirrorY = _mirrorY.value,
                rotation = _rotation.value,
                translationX = _translationX.value,
                translationY = _translationY.value,
                contrast = _contrast.value,
                saturation = _saturation.value,
                brightness = _brightness.value,
                isSystemPreset = false
            )
            repository.insertPreset(preset)
        }
    }

    // Delete custom preset
    fun deletePreset(presetId: Int) {
        viewModelScope.launch {
            repository.deletePresetById(presetId)
        }
    }

    // Reset parameters back to default
    fun resetCalibration() {
        _scale.value = 1.0f
        _mirrorX.value = false
        _mirrorY.value = false
        _rotation.value = 0.0f
        _translationX.value = 0.0f
        _translationY.value = 0.0f
        _contrast.value = 1.0f
        _saturation.value = 1.0f
        _brightness.value = 1.0f
    }
}
