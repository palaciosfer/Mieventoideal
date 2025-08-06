package com.mievento.ideal.data.models

data class RescheduleRequest(
    val id: Int,
    val event_id: Int,
    val user_id: Int,
    val new_date: String,
    val new_time: String,
    val new_description: String?,
    val new_notes: String?,
    val reason: String?,
    val status: String, // "pendiente", "aceptada", "rechazada"
    val admin_response: String?,
    val created_at: String,
    val updated_at: String,
    // Datos adicionales del evento
    val event_name: String? = null,
    val user_name: String? = null
)

data class CreateRescheduleRequest(
    val new_date: String,
    val new_time: String,
    val new_description: String? = null,
    val new_notes: String? = null,
    val reason: String? = null
)

data class RescheduleResponse(
    val success: Boolean,
    val message: String? = null,
    val request: RescheduleRequest? = null,
    val requests: List<RescheduleRequest>? = null,
    val error: String? = null
)