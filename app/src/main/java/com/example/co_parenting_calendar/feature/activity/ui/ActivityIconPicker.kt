package com.example.co_parenting_calendar.feature.activity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.feature.activity.domain.ActivityIconType

@Composable
fun ActivityIconPicker(
    selectedIcon: ActivityIconType,
    onIconSelected: (ActivityIconType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(activityIconTypes) { iconType ->
            val isSelected = iconType == selectedIcon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onIconSelected(iconType) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconType.imageVector(),
                    contentDescription = iconType.displayName(),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
