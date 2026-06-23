package com.mindandmotion.app.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindandmotion.app.data.task.Priority
import com.mindandmotion.app.ui.components.AppTopBar
import com.mindandmotion.app.ui.components.ConfirmDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    viewModel: TaskViewModel,
    taskId: Long?,
    onBack: () -> Unit
) {
    // Încarcă draft-ul o singură dată, când intrăm pe ecran pentru acest id.
    LaunchedEffect(taskId) { viewModel.loadForEdit(taskId) }

    val state by viewModel.editState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (state.isEditing) "Editează task" else "Task nou",
                onBack = onBack,
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Șterge task")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Titlu") },
                singleLine = true,
                isError = state.title.isBlank()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descriere (opțional)") }
            )

            Text("Prioritate", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            PrioritySelector(
                selected = state.priority,
                onSelected = viewModel::onPriorityChange
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
                Text(
                    text = state.dueDate?.format(dateFormatter)?.let { "Scadent: $it" }
                        ?: "Adaugă deadline (opțional)",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (state.dueDate != null) {
                TextButton(onClick = { viewModel.onDueDateChange(null) }) {
                    Text("Șterge deadline")
                }
            }

            Button(
                onClick = {
                    viewModel.onSave()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave
            ) {
                Text("Salvează")
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueDate?.toUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueDateChange(pickerState.selectedDateMillis?.toLocalDate())
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anulează") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Ștergi task-ul?",
            message = "Acțiunea nu poate fi anulată.",
            onConfirm = {
                viewModel.deleteEditing()
                showDeleteConfirm = false
                onBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioritySelector(selected: Priority, onSelected: (Priority) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.entries.forEach { priority ->
            FilterChip(
                selected = selected == priority,
                onClick = { onSelected(priority) },
                label = { Text(priority.shortLabel()) }
            )
        }
    }
}

private fun Priority.shortLabel(): String = when (this) {
    Priority.HIGH -> "Mare"
    Priority.MEDIUM -> "Medie"
    Priority.LOW -> "Mică"
}

private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ro"))

// Date picker-ul lucrează în millis UTC; convertim explicit ca să nu apară off-by-one din fus orar.
private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
