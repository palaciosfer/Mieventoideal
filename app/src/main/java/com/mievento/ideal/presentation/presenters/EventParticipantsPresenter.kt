package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.EventParticipation
import com.mievento.ideal.data.repositories.ParticipationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface EventParticipantsView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showParticipants(participants: List<EventParticipation>)
    fun onParticipantResponseSent()
}

class EventParticipantsPresenter(
    private val view: EventParticipantsView,
    private val participationRepository: ParticipationRepository
) {

    fun loadEventParticipants(eventId: Int) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = participationRepository.getEventParticipants(eventId)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showParticipants(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar participantes")
                }
            }
        }
    }

    fun respondToParticipant(eventId: Int, userId: Int, status: String, message: String?) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = participationRepository.respondToParticipation(eventId, userId, status, message)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onParticipantResponseSent()
                    loadEventParticipants(eventId) // Recargar lista
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al responder")
                }
            }
        }
    }
}