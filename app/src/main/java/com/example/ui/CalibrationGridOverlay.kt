package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CalibrationGridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw grid lines
        val strokeWidth = 1.dp.toPx()
        val gridColor = Color(0x803FE0FF) // Neon Cyan semi-transparent
        val axisColor = Color(0xE0FF3B30) // Neon Red axes

        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        // Draw horizontal & vertical axes
        drawLine(
            color = axisColor,
            start = androidx.compose.ui.geometry.Offset(width / 2f, 0f),
            end = androidx.compose.ui.geometry.Offset(width / 2f, height),
            strokeWidth = strokeWidth * 1.5f
        )
        drawLine(
            color = axisColor,
            start = androidx.compose.ui.geometry.Offset(0f, height / 2f),
            end = androidx.compose.ui.geometry.Offset(width, height / 2f),
            strokeWidth = strokeWidth * 1.5f
        )

        // Draw sub-grid lines
        val cols = 8
        val rows = 6
        for (i in 1 until cols) {
            val x = width * (i.toFloat() / cols)
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, height),
                strokeWidth = strokeWidth,
                pathEffect = dashPathEffect
            )
        }
        for (i in 1 until rows) {
            val y = height * (i.toFloat() / rows)
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(width, y),
                strokeWidth = strokeWidth,
                pathEffect = dashPathEffect
            )
        }

        // Draw concentric rectangles (representing active scaling bounds)
        val rectScales = listOf(0.9f, 0.8f, 0.7f, 0.6f, 0.5f)
        rectScales.forEach { rectScale ->
            val w = width * rectScale
            val h = height * rectScale
            val x = (width - w) / 2f
            val y = (height - h) / 2f
            drawRect(
                color = gridColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(w, h),
                style = Stroke(width = strokeWidth)
            )
        }

        // Corner check lines
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.15f), strokeWidth = strokeWidth)
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(width, 0f), end = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.15f), strokeWidth = strokeWidth)
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(0f, height), end = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.85f), strokeWidth = strokeWidth)
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(width, height), end = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.85f), strokeWidth = strokeWidth)
    }
}
