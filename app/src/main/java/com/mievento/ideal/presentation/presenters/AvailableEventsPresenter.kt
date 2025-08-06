package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.ParticipationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface AvailableEventsView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showEvents(events: List<Event>)
    fun onParticipationRequested()
}

class AvailableEventsPresenter(
    private val view: AvailableEventsView,
    private val participationRepository: ParticipationRepository
) {

    fun loadAvailableEvents() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = participationRepository.getAvailableEvents()
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

    fun requestParticipation(eventId: Int, notes: String?) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = participationRepository.requestParticipation(eventId, notes)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onParticipationRequested()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al solicitar participación")
                }
            }
        }
    }
}