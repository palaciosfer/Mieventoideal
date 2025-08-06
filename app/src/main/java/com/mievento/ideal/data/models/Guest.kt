package com.mievento.ideal.data.models

data class Guest(
    val id: Int,
    val event_id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val relationship: String?,
    val confirmation_status: String = "pendiente", // "pendiente", "confirmado", "rechazado"
    val notes: String?,
    val created_at: String,
    val updated_at: String
)

data class CreateGuestRequest(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val relationship: String? = null,
    val notes: String? = null
)

data class GuestResponse(
    val success: Boolean,
    val message: String? = null,
    val guest: Guest? = null,
    val guests: List<Guest>? = null,
    val error: String? = null
)