package com.mievento.ideal.data.models

data class Notification(
    val id: Int,
    val user_id: Int,
    val title: String,
    val message: String,
    val type: String, // "reschedule", "event", "general"
    val related_id: Int?,
    val is_read: Boolean,
    val created_at: String
)

data class NotificationResponse(
    val success: Boolean,
    val message: String? = null,
    val notification: Notification? = null,
    val notifications: List<Notification>? = null,
    val unread_count: Int? = null,
    val error: String? = null
)