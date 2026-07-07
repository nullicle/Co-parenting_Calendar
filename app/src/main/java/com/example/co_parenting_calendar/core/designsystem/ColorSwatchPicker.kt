package com.example.co_parenting_calendar.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A small fixed palette so picking a colour for a child or parent is a single tap. */
val colorPalette: List<Long> = listOf(
    0xFFE57373, // red
    0xFFFFB74D, // orange
    0xFFFFF176, // yellow
    0xFF81C784, // green
    0xFF4FC3F7, // light blue
    0xFF9575CD, // purple
    0xFFF06292, // pink
    0xFFA1887F // brown
)

@Composable
fun ColorSwatchPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Long> = colorPalette
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { colorArgb ->
            val borderModifier = if (colorArgb == selectedColor) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            } else {
                Modifier
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(colorArgb), CircleShape)
                    .then(borderModifier)
                    .clickable { onColorSelected(colorArgb) }
            )
        }
    }
}
