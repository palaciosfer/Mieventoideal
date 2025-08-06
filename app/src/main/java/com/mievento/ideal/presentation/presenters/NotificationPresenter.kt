package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.Notification
import com.mievento.ideal.data.repositories.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface NotificationView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showNotifications(notifications: List<Notification>)
    fun onNotificationMarkedAsRead()
}

class NotificationPresenter(
    private val view: NotificationView,
    private val notificationRepository: NotificationRepository
) {

    fun loadNotifications() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = notificationRepository.getNotifications()
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showNotifications(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar notificaciones")
                }
            }
        }
    }

    fun markAsRead(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = notificationRepository.markAsRead(id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onNotificationMarkedAsRead()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al marcar como leída")
                }
            }
        }
    }
}