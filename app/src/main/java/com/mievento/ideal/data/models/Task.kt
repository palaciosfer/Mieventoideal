package com.mievento.ideal.data.models

data class Task(
    val id: Int,
    val event_id: Int,
    val title: String,
    val description: String?,
    val due_date: String?,
    val status: String = "pendiente", // "pendiente", "en_proceso", "finalizada"
    val priority: String = "media", // "baja", "media", "alta"
    val created_at: String,
    val updated_at: String
)

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val due_date: String? = null,
    val priority: String = "media"
)

data class TaskResponse(
    val success: Boolean,
    val message: String? = null,
    val task: Task? = null,
    val tasks: List<Task>? = null,
    val error: String? = null
)