package com.mindandmotion.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindandmotion.app.data.task.Priority
import com.mindandmotion.app.data.task.TaskEntity
import com.mindandmotion.app.data.task.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Starea listei de task-uri (MM-12). */
data class TaskListUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = true
)

/** Starea formularului de adăugare/editare (MM-13). */
data class TaskEditUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val isEditing: Boolean = false
) {
    val canSave: Boolean get() = title.isNotBlank()
}

/**
 * ViewModel pentru modulul Tasks (MM-11). Expune lista sortată (din DAO) și
 * starea formularului de editare. O singură instanță e partajată între
 * [com.mindandmotion.app.ui.tasks.TaskListScreen] și TaskEditScreen.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val listState: StateFlow<TaskListUiState> = repository.observeTasks()
        .map { TaskListUiState(tasks = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TaskListUiState())

    private val _editState = MutableStateFlow(TaskEditUiState())
    val editState: StateFlow<TaskEditUiState> = _editState.asStateFlow()

    // Task-ul aflat în editare, păstrat ca să nu pierdem id/isDone/createdAt la salvare.
    private var editingTask: TaskEntity? = null

    // ---- Listă ----

    fun onToggleDone(task: TaskEntity) {
        viewModelScope.launch { repository.setDone(task.id, !task.isDone) }
    }

    fun onDelete(task: TaskEntity) {
        viewModelScope.launch { repository.delete(task) }
    }

    // ---- Editare ----

    /** Încarcă formularul: `id == null` → task nou, altfel task existent. */
    fun loadForEdit(id: Long?) {
        if (id == null) {
            editingTask = null
            _editState.value = TaskEditUiState()
            return
        }
        viewModelScope.launch {
            val task = repository.getTask(id)
            editingTask = task
            _editState.value = if (task == null) {
                TaskEditUiState()
            } else {
                TaskEditUiState(
                    title = task.title,
                    description = task.description.orEmpty(),
                    priority = task.priority,
                    dueDate = task.dueDate,
                    isEditing = true
                )
            }
        }
    }

    fun onTitleChange(value: String) = _editState.update { it.copy(title = value) }

    fun onDescriptionChange(value: String) = _editState.update { it.copy(description = value) }

    fun onPriorityChange(priority: Priority) = _editState.update { it.copy(priority = priority) }

    fun onDueDateChange(date: LocalDate?) = _editState.update { it.copy(dueDate = date) }

    /** Șterge task-ul aflat în editare (acțiunea de ștergere din TaskEditScreen). */
    fun deleteEditing() {
        val task = editingTask ?: return
        viewModelScope.launch { repository.delete(task) }
    }

    fun onSave() {
        val draft = _editState.value
        if (!draft.canSave) return
        // Pornim de la task-ul existent (păstrează isDone/createdAt) sau de la unul nou.
        val base = editingTask ?: TaskEntity(title = "")
        viewModelScope.launch {
            repository.upsert(
                base.copy(
                    title = draft.title.trim(),
                    description = draft.description.trim().ifBlank { null },
                    priority = draft.priority,
                    dueDate = draft.dueDate
                )
            )
        }
    }
}

/**
 * Factory manuală (fără Hilt, conform ARCHITECTURE.md) — primește repository-ul
 * din AppContainer.
 */
class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
