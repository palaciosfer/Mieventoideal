package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.AuthRepository
import com.mievento.ideal.data.repositories.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface EventListView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showEvents(events: List<Event>)
    fun onEventDeleted()
    fun navigateToCreateEvent()
    fun navigateToEventDetail(event: Event)
    fun navigateToLogin()
}

class EventListPresenter(
    private val view: EventListView,
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) {

    fun loadEvents() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = eventRepository.getEvents()
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showEvents(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar eventos")
                }
            }
        }
    }

    fun deleteEvent(eventId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = eventRepository.deleteEvent(eventId)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onEventDeleted()
                    loadEvents() // Recargar lista
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al eliminar evento")
                }
            }
        }
    }

    fun onCreateEventClicked() {
        view.navigateToCreateEvent()
    }

    fun onEventClicked(event: Event) {
        view.navigateToEventDetail(event)
    }

    fun logout() {
        authRepository.logout()
        view.navigateToLogin()
    }
}