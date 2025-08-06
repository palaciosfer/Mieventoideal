package com.mievento.ideal.data.models

data class EventListResponse(
    val success: Boolean,
    val events: List<Event>? = null,
    val error: String? = null,
    val message: String? = null
)