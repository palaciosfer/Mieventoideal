package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.CreateRescheduleRequest
import com.mievento.ideal.data.repositories.RescheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface RescheduleRequestView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun onRequestCreated()
}

class RescheduleRequestPresenter(
    private val view: RescheduleRequestView,
    private val rescheduleRepository: RescheduleRepository
) {

    fun createRescheduleRequest(
        eventId: Int,
        newDate: String,
        newTime: String,
        newDescription: String,
        newNotes: String,
        reason: String
    ) {
        if (!validateInput(newDate, newTime)) return

        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateRescheduleRequest(
                new_date = newDate,
                new_time = newTime,
                new_description = newDescription.ifBlank { null },
                new_notes = newNotes.ifBlank { null },
                reason = reason.ifBlank { null }
            )

            val result = rescheduleRepository.createRescheduleRequest(eventId, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onRequestCreated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al crear solicitud")
                }
            }
        }
    }

    private fun validateInput(newDate: String, newTime: String): Boolean {
        if (newDate.isBlank()) {
            view.showError("La nueva fecha es requerida")
            return false
        }
        if (newTime.isBlank()) {
            view.showError("La nueva hora es requerida")
            return false
        }
        return true
    }
}