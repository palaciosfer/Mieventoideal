package com.mievento.ideal.data.models

data class Event(
    val id: Int,
    val user_id: Int,
    val name: String,
    val type: String,
    val event_date: String,
    val event_time: String,
    val location: String,
    val description: String?,
    val notes: String?,
    val status: String = "planeacion",
    val budget: Double = 0.0,
    val main_image: String? = null,
    val admin_name: String? = null,
    val created_at: String,
    val updated_at: String
)

data class CreateEventRequest(
    val name: String,
    val type: String,
    val event_date: String,
    val event_time: String,
    val location: String,
    val description: String? = null,
    val notes: String? = null,
    val budget: Double = 0.0,
    val main_image: String? = null
)

// 🔥 ELIMINAR EventResponse de aquí - ya está en EventResponse.kt