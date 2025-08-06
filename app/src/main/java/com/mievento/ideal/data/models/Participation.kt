package com.mievento.ideal.data.models

data class EventParticipation(
    val id: Int,
    val event_id: Int,
    val user_id: Int,
    val status: String, // "pendiente", "aceptado", "rechazado"
    val participation_date: String,
    val notes: String?,
    // Información del evento
    val event_name: String? = null,
    val event_date: String? = null,
    val event_time: String? = null,
    val location: String? = null,
    val description: String? = null,
    val main_image: String? = null,
    // Información del usuario - TODAS ESTAS PROPIEDADES SON NECESARIAS
    val user_name: String? = null,
    val full_name: String? = null,
    val email: String? = null,
    val phone: String? = null
)

data class RequestParticipationRequest(
    val notes: String? = null
)

data class RespondParticipationRequest(
    val status: String,
    val response_message: String? = null
)

data class ParticipationResponse(
    val success: Boolean,
    val message: String? = null,
    val participation: EventParticipation? = null,
    val participations: List<EventParticipation>? = null,
    val participants: List<EventParticipation>? = null,
    val events: List<Event>? = null,
    val error: String? = null
)