package com.mindandmotion.app.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindandmotion.app.data.journal.Mood
import com.mindandmotion.app.ui.components.AppTopBar
import com.mindandmotion.app.ui.components.ConfirmDialog
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalEntryScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ro")) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.selectedDate.format(formatter),
                onBack = onBack,
                actions = {
                    if (state.selectedEntry != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Șterge intrarea")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MoodPicker(
                selected = state.draftMood,
                onMoodSelected = viewModel::onMoodChanged
            )

            OutlinedTextField(
                value = state.draftContent,
                onValueChange = viewModel::onContentChanged,
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = { Text("Cum a fost ziua ta?") }
            )

            Button(
                onClick = {
                    viewModel.onSaveEntry()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.draftContent.isNotBlank()
            ) {
                Text("Salvează")
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Ștergi intrarea?",
            message = "Acțiunea nu poate fi anulată.",
            onConfirm = {
                viewModel.onDeleteEntry()
                showDeleteConfirm = false
                onBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun MoodPicker(selected: Mood?, onMoodSelected: (Mood?) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Mood.entries.forEach { mood ->
            FilterChip(
                selected = selected == mood,
                onClick = { onMoodSelected(if (selected == mood) null else mood) },
                label = { Text(mood.label()) }
            )
        }
    }
}

private fun Mood.label(): String = when (this) {
    Mood.GREAT -> "Excelent"
    Mood.GOOD -> "Bine"
    Mood.OKAY -> "Așa și-așa"
    Mood.BAD -> "Greu"
}
