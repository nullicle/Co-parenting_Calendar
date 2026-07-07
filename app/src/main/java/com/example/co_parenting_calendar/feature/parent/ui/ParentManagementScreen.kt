package com.example.co_parenting_calendar.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository
import com.example.co_parenting_calendar.feature.parent.domain.Parent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentManagementScreen(
    parentRepository: ParentRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingParent by remember { mutableStateOf<Parent?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Parents") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            parentRepository.parents.forEach { parent ->
                ListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(parent.colorArgb), CircleShape)
                        )
                    },
                    headlineContent = { Text(parent.name) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { editingParent = parent }
                )
                HorizontalDivider()
            }
        }
    }

    editingParent?.let { parent ->
        ParentDialog(
            parent = parent,
            onDismiss = { editingParent = null },
            onSave = { updated ->
                parentRepository.updateParent(updated)
                editingParent = null
            }
        )
    }
}
