package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.CreateTaskRequest
import com.mievento.ideal.data.models.Task
import com.mievento.ideal.data.repositories.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface TaskListView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showTasks(tasks: List<Task>)
    fun onTaskCreated()
    fun onTaskUpdated()
    fun onTaskDeleted()
    fun showCreateTaskDialog()
    fun showEditTaskDialog(task: Task)
}

class TaskListPresenter(
    private val view: TaskListView,
    private val taskRepository: TaskRepository
) {

    fun loadTasks(eventId: Int) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = taskRepository.getTasks(eventId)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showTasks(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar tareas")
                }
            }
        }
    }

    fun createTask(eventId: Int, title: String, description: String, dueDate: String, priority: String) {
        if (title.isBlank()) {
            view.showError("El título es requerido")
            return
        }

        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateTaskRequest(
                title = title,
                description = description.ifBlank { null },
                due_date = dueDate.ifBlank { null },
                priority = priority
            )

            val result = taskRepository.createTask(eventId, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onTaskCreated()
                    loadTasks(eventId)
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al crear tarea")
                }
            }
        }
    }

    fun updateTask(id: Int, title: String, description: String, dueDate: String, priority: String) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateTaskRequest(
                title = title,
                description = description.ifBlank { null },
                due_date = dueDate.ifBlank { null },
                priority = priority
            )

            val result = taskRepository.updateTask(id, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onTaskUpdated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al actualizar tarea")
                }
            }
        }
    }

    fun updateTaskStatus(id: Int, status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = taskRepository.updateTaskStatus(id, status)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onTaskUpdated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al actualizar estado")
                }
            }
        }
    }

    fun deleteTask(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = taskRepository.deleteTask(id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onTaskDeleted()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al eliminar tarea")
                }
            }
        }
    }
}