package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PresetEntity
import com.example.ui.CalibrationGridOverlay
import com.example.ui.ProjectorViewModel
import com.example.ui.SampleVideo
import com.example.ui.VideoTexturePlayer
import java.util.Locale
import kotlin.math.roundToInt

// AMOLED Black & Projection-optimized Theme
private val ProjectionColorScheme = darkColorScheme(
    primary = Color(0xFF3FE0FF),       // Neon Projector Cyan
    secondary = Color(0xFFFF3B30),     // Alignment Red
    tertiary = Color(0xFFFFCC00),      // Calibration Yellow
    background = Color(0xFF000000),    // AMOLED Pure Black
    surface = Color(0xFF0C101B),       // Space Blue-Black
    surfaceVariant = Color(0xFF161B29),// Contrast Deck Surface
    onBackground = Color(0xFFECEFF5),
    onSurface = Color(0xFFECEFF5),
    onSurfaceVariant = Color(0xFFC0C7D6)
)

class MainActivity : ComponentActivity() {
    private val viewModel: ProjectorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Prevent screen from turning off during video projections
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme(colorScheme = ProjectionColorScheme) {
                ProjectorPlayerApp(viewModel = viewModel)
            }
        }
    }
}

// Extension to safely query the Activity from Compose Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ProjectorPlayerApp(viewModel: ProjectorViewModel) {
    val context = LocalContext.current
    val videoUri by viewModel.videoUri.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val brightness by viewModel.brightness.collectAsState()

    // Activity Brightness Override Sync
    LaunchedEffect(brightness) {
        context.findActivity()?.let { activity ->
            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = brightness
            activity.window.attributes = layoutParams
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black // True black background for projector isolates light leak
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (videoUri == null) {
                // Landing Dashboard Screen
                DashboardScreen(viewModel = viewModel)
            } else {
                // Immersive Calibration Video Player Screen
                ProjectorPlayerScreen(
                    viewModel = viewModel,
                    videoUri = videoUri!!,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ProjectorViewModel) {
    val context = LocalContext.current
    val savedPresets by viewModel.savedPresets.collectAsState()

    // Activity File Picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Retrieve actual file name or label
            viewModel.loadVideo(uri, "My Local Video")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Projector Player",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3FE0FF),
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "DIY Cardboard Projector Assistant",
                        fontSize = 11.sp,
                        color = Color(0xFFC0C7D6),
                        fontWeight = FontWeight.Light
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF000000),
                titleContentColor = Color(0xFF3FE0FF)
            ),
            actions = {
                IconButton(
                    onClick = { viewModel.resetCalibration() },
                    modifier = Modifier.testTag("reset_all_calibration")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Active Calibration",
                        tint = Color(0xFF3FE0FF)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner Illustration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101B))
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.projector_onboarding),
                            contentDescription = "Cardboard Projector Illustration",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "How DIY Box Projectors Work",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3FE0FF)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "A magnifying glass lens double-flips light (upside down and left-to-right). This app lets you mirror the source, scale (zoom out) the bounds to fit inside the box aperture, and boost contrast to preserve clarity on raw walls.",
                                fontSize = 12.sp,
                                color = Color(0xFFC0C7D6),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Quick Play Actions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF161B29))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Video Source",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("select_video_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3FE0FF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Open video file icon",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pick Local Video File",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Projector Presets List (Room integration)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Projector Profiles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Projector profiles section",
                            tint = Color(0xFF3FE0FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (savedPresets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFF0C101B), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading presets...",
                                color = Color(0xFFC0C7D6),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedPresets.forEach { preset ->
                                PresetItemRow(
                                    preset = preset,
                                    onSelect = { viewModel.applyPreset(preset) },
                                    onDelete = if (!preset.isSystemPreset) {
                                        { viewModel.deletePreset(preset.id) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // Streaming Test Clips Carousel (Ensures offline/immediate testability)
            item {
                Column {
                    Text(
                        text = "Instant Alignment Test Clips",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(viewModel.sampleVideos) { sample ->
                            SampleVideoCard(
                                sample = sample,
                                onClick = { viewModel.loadSampleVideo(sample) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PresetItemRow(
    preset: PresetEntity,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var isSelected by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
                isSelected = true
            }
            .testTag("preset_item_card_${preset.name.replace(" ", "_")}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C101B)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (preset.isSystemPreset) Color(0xFF161B29) else Color(0x603FE0FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (preset.isSystemPreset) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3FE0FF).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "SYSTEM",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3FE0FF)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scale: ${(preset.scale * 100).roundToInt()}% | Mirror X: ${if (preset.mirrorX) "Yes" else "No"} | Mirror Y: ${if (preset.mirrorY) "Yes" else "No"} | Contrast: ${preset.contrast}x",
                    fontSize = 11.sp,
                    color = Color(0xFFC0C7D6)
                )
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Custom Profile",
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Preset Loaded Indicator",
                    tint = Color(0xFF3FE0FF).copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SampleVideoCard(
    sample: SampleVideo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() }
            .testTag("sample_video_card_${sample.title.replace(" ", "_")}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF161B29))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF3FE0FF).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Test Stream icon",
                        tint = Color(0xFF3FE0FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sample.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sample.description,
                fontSize = 11.sp,
                color = Color(0xFFC0C7D6),
                maxLines = 3,
                minLines = 3,
                lineHeight = 14.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProjectorPlayerScreen(
    viewModel: ProjectorViewModel,
    videoUri: Uri,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long
) {
    var seekPosition by remember { mutableStateOf<Long?>(null) }
    var isTuningOpen by remember { mutableStateOf(false) }
    var isSavePresetDialogOpen by remember { mutableStateOf(false) }

    val scale by viewModel.scale.collectAsState()
    val mirrorX by viewModel.mirrorX.collectAsState()
    val mirrorY by viewModel.mirrorY.collectAsState()
    val rotation by viewModel.rotation.collectAsState()
    val translationX by viewModel.translationX.collectAsState()
    val translationY by viewModel.translationY.collectAsState()
    val contrast by viewModel.contrast.collectAsState()
    val saturation by viewModel.saturation.collectAsState()
    val controlsVisible by viewModel.controlsVisible.collectAsState()
    val isCalibrating by viewModel.isCalibrating.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Keeps background fully black during zoom-out
            .clickable { viewModel.toggleControls() },
        contentAlignment = Alignment.Center
    ) {
        // Video Render Container transformed relative to scale and mirror states
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Left-to-Right Flip & Up-to-Down Flip via Scale modifiers
                    this.scaleX = if (mirrorX) -scale else scale
                    this.scaleY = if (mirrorY) -scale else scale
                    this.rotationZ = rotation
                    this.translationX = translationX
                    this.translationY = translationY
                },
            contentAlignment = Alignment.Center
        ) {
            VideoTexturePlayer(
                videoUri = videoUri,
                isPlaying = isPlaying,
                seekPosition = seekPosition,
                contrast = contrast,
                saturation = saturation,
                onSeekConsumed = { seekPosition = null },
                onProgressUpdate = { pos, dur ->
                    viewModel.updatePlaybackProgress(pos, dur)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Grid calibration layout
            if (isCalibrating) {
                CalibrationGridOverlay(modifier = Modifier.fillMaxSize())
            }
        }

        // Float Dialog Overlay for custom name presets
        if (isSavePresetDialogOpen) {
            SavePresetDialog(
                onDismiss = { isSavePresetDialogOpen = false },
                onSave = { name ->
                    viewModel.saveCurrentAsPreset(name)
                    isSavePresetDialogOpen = false
                }
            )
        }

        // Glassmorphism Overlays
        // TOP CONTROLS BAR
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE005080F))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable(enabled = false) {}, // Eat clicks to prevent video pause
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.clearVideo() },
                    modifier = Modifier.testTag("exit_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Player",
                        tint = Color.White
                    )
                }

                Text(
                    text = viewModel.videoTitle.collectAsState().value,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )

                // Calibration Grid Toggle
                IconButton(
                    onClick = { viewModel.setCalibrating(!isCalibrating) },
                    modifier = Modifier.testTag("toggle_grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Grid3x3,
                        contentDescription = "Toggle Grid Calibration Overlay",
                        tint = if (isCalibrating) Color(0xFF3FE0FF) else Color.White
                    )
                }
            }
        }

        // BOTTOM CONTROLS BOARD (Collapsible Calibration Deck)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = slideInVertically(tween(250)) { it } + fadeIn(),
            exit = slideOutVertically(tween(250)) { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFA080B13))
                    .padding(16.dp)
                    .clickable(enabled = false) {} // Eat click
            ) {
                // Seekbar Block
                val currentSec = currentPosition / 1000
                val totalSec = duration / 1000
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        fontSize = 11.sp,
                        color = Color(0xFFC0C7D6),
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { percent ->
                            seekPosition = (percent * duration).toLong()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("video_seek_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF3FE0FF),
                            activeTrackColor = Color(0xFF3FE0FF),
                            inactiveTrackColor = Color(0xFF1C2436)
                        )
                    )

                    Text(
                        text = formatTime(duration),
                        fontSize = 11.sp,
                        color = Color(0xFFC0C7D6),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Core Playback Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left actions: Tuning Drawer Toggle
                    IconButton(
                        onClick = { isTuningOpen = !isTuningOpen },
                        modifier = Modifier
                            .background(
                                if (isTuningOpen) Color(0xFF3FE0FF).copy(alpha = 0.2f) else Color.Transparent,
                                CircleShape
                            )
                            .testTag("toggle_deck_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Calibration Settings Deck",
                            tint = if (isTuningOpen) Color(0xFF3FE0FF) else Color.White
                        )
                    }

                    // Centered playback keys
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Rewind 10s
                        IconButton(
                            onClick = { seekPosition = (currentPosition - 10000).coerceAtLeast(0) }
                        ) {
                            Text(text = "⏮", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Play/Pause
                        IconButton(
                            onClick = { viewModel.setPlaying(!isPlaying) },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color(0xFF3FE0FF), CircleShape)
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = "Play/Pause Video",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Forward 10s
                        IconButton(
                            onClick = { seekPosition = (currentPosition + 10000).coerceAtMost(duration) }
                        ) {
                            Text(text = "⏭", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Right actions: Full Screen mode instructions
                    Button(
                        onClick = { viewModel.setControlsVisible(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2235)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("project_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Maximize,
                            contentDescription = "Full Screen Projection mode",
                            tint = Color(0xFF3FE0FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PROJECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3FE0FF)
                        )
                    }
                }

                // Calibration parameters drawer
                AnimatedVisibility(visible = isTuningOpen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Divider(color = Color(0xFF1F283D))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Mirroring Flip Switches
                        Text(
                            text = "LENS INVERSION CORRECTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3FE0FF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF0F1524), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setMirrorX(!mirrorX) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("mirror_horizontal_card"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Horizontal Mirror", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Left-to-Right flip", fontSize = 10.sp, color = Color(0xFFC0C7D6))
                                }
                                Switch(
                                    checked = mirrorX,
                                    onCheckedChange = { viewModel.setMirrorX(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = Color(0xFF3FE0FF),
                                        uncheckedThumbColor = Color(0xFFC0C7D6),
                                        uncheckedTrackColor = Color(0xFF1F283D)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF0F1524), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setMirrorY(!mirrorY) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("mirror_vertical_card"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Vertical Flip", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Up-to-Down lens fix", fontSize = 10.sp, color = Color(0xFFC0C7D6))
                                }
                                Switch(
                                    checked = mirrorY,
                                    onCheckedChange = { viewModel.setMirrorY(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = Color(0xFF3FE0FF),
                                        uncheckedThumbColor = Color(0xFFC0C7D6),
                                        uncheckedTrackColor = Color(0xFF1F283D)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Zoom & Alignment Sliders
                        Text(
                            text = "DIMENSIONS & FOCUS (ZOOM)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3FE0FF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Zoom Out (Scale) slider
                        TuningSliderRow(
                            label = "Zoom Scale (Anti-Corner Clip)",
                            value = scale,
                            valueRange = 0.3f..1.3f,
                            onValueChange = { viewModel.setScale(it) },
                            displayValue = "${(scale * 100).roundToInt()}%"
                        )

                        // Fine Rotation slider
                        TuningSliderRow(
                            label = "Fine Tilt Correction",
                            value = rotation,
                            valueRange = -15f..15f,
                            onValueChange = { viewModel.setRotation(it) },
                            displayValue = String.format(Locale.US, "%.1f°", rotation)
                        )

                        // Translation X (Horizontal shift)
                        TuningSliderRow(
                            label = "Center Offset X",
                            value = translationX,
                            valueRange = -300f..300f,
                            onValueChange = { viewModel.setTranslationX(it) },
                            displayValue = "${translationX.roundToInt()}px"
                        )

                        // Translation Y (Vertical shift)
                        TuningSliderRow(
                            label = "Center Offset Y",
                            value = translationY,
                            valueRange = -300f..300f,
                            onValueChange = { viewModel.setTranslationY(it) },
                            displayValue = "${translationY.roundToInt()}px"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Color Wall Correctors
                        Text(
                            text = "POWER & WALL COLOR CORRECTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3FE0FF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Screen Brightness (Overrides system)
                        val brightnessState by viewModel.brightness.collectAsState()
                        TuningSliderRow(
                            label = "Projector Power (Brightness)",
                            value = brightnessState,
                            valueRange = 0.1f..1.0f,
                            onValueChange = { viewModel.setBrightness(it) },
                            displayValue = "${(brightnessState * 100).roundToInt()}%"
                        )

                        // Contrast slider
                        TuningSliderRow(
                            label = "Wall Contrast Boost",
                            value = contrast,
                            valueRange = 0.5f..2.5f,
                            onValueChange = { viewModel.setContrast(it) },
                            displayValue = String.format(Locale.US, "%.2fx", contrast)
                        )

                        // Saturation slider
                        TuningSliderRow(
                            label = "Wall Color Saturation",
                            value = saturation,
                            valueRange = 0.0f..3.0f,
                            onValueChange = { viewModel.setSaturation(it) },
                            displayValue = String.format(Locale.US, "%.2fx", saturation)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Deck (Preset save & quick reset)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resetCalibration() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset setup icon", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Reset Calibration", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { isSavePresetDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3FE0FF)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("save_preset_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Save Preset Profile icon", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Save Profile", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TuningSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    displayValue: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, color = Color(0xFFC0C7D6))
            Text(
                text = displayValue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3FE0FF),
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            modifier = Modifier.height(32.dp).testTag("slider_${label.replace(" ", "_")}"),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF3FE0FF),
                activeTrackColor = Color(0xFF3FE0FF),
                inactiveTrackColor = Color(0xFF141A29)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePresetDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F1524),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3FE0FF).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = "Save Calibration Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3FE0FF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Save current scale, mirroring, rotation, translation, and color configurations under a descriptive name.",
                    fontSize = 11.sp,
                    color = Color(0xFFC0C7D6)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Profile Name (e.g. Living Room Box)") },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3FE0FF),
                        unfocusedBorderColor = Color(0xFF1C2436),
                        focusedLabelColor = Color(0xFF3FE0FF),
                        unfocusedLabelColor = Color(0xFFC0C7D6),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_preset_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = Color(0xFFC0C7D6))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (text.isNotBlank()) onSave(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3FE0FF)),
                        shape = RoundedCornerShape(6.dp),
                        enabled = text.isNotBlank(),
                        modifier = Modifier.testTag("dialog_preset_save_confirm")
                    ) {
                        Text(text = "Save", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Simple playback duration format helper
fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}


