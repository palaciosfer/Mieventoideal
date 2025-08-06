package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.EventParticipation
import com.mievento.ideal.data.repositories.ParticipationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface MyParticipationsView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showParticipations(participations: List<EventParticipation>)
}

class MyParticipationsPresenter(
    private val view: MyParticipationsView,
    private val participationRepository: ParticipationRepository
) {

    fun loadMyParticipations() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = participationRepository.getMyParticipations()
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showParticipations(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar participaciones")
                }
            }
        }
    }
}