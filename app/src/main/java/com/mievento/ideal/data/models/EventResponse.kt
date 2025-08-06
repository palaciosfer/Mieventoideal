package com.mievento.ideal.data.models

data class EventsResponse(
    val success: Boolean,
    val events: List<Event>?,
    val message: String?
)

data class EventResponse(
    val success: Boolean,
    val event: Event?,
    val message: String?
)

data class BaseResponse(
    val success: Boolean,
    val message: String?
)