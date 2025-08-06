package com.mievento.ideal.data.models

data class EventImage(
    val id: Int,
    val event_id: Int,
    val image_url: String,
    val image_type: String, // "main" o "gallery"
    val created_at: String
)

data class EventImageResponse(
    val success: Boolean,
    val message: String? = null,
    val image: EventImage? = null,
    val images: List<EventImage>? = null,
    val error: String? = null
)