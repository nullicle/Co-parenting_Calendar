package nz.co.chrisstevens.coparenting.feature.children.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.children.domain.Child

private sealed class ChildDialogState {
    object Adding : ChildDialogState()
    data class Editing(val child: Child) : ChildDialogState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenScreen(
    childRepository: ChildRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogState by remember { mutableStateOf<ChildDialogState?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Children") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialogState = ChildDialogState.Adding }) {
                Icon(Icons.Filled.Add, contentDescription = "Add child")
            }
        }
    ) { innerPadding ->
        if (childRepository.children.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Text(
                    text = "No children yet. Tap + to add one.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                items(childRepository.children, key = { it.id }) { child ->
                    ListItem(
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(child.colorArgb), CircleShape)
                            )
                        },
                        headlineContent = { Text(child.name) },
                        modifier = Modifier.clickable { dialogState = ChildDialogState.Editing(child) }
                    )
                }
            }
        }
    }

    when (val state = dialogState) {
        is ChildDialogState.Adding -> {
            ChildDialog(
                initialChild = null,
                onDismiss = { dialogState = null },
                onSave = { child ->
                    childRepository.addChild(child)
                    dialogState = null
                }
            )
        }
        is ChildDialogState.Editing -> {
            ChildDialog(
                initialChild = state.child,
                onDismiss = { dialogState = null },
                onSave = { child ->
                    childRepository.updateChild(child)
                    dialogState = null
                },
                onDelete = {
                    childRepository.deleteChild(state.child.id)
                    dialogState = null
                }
            )
        }
        null -> Unit
    }
}
