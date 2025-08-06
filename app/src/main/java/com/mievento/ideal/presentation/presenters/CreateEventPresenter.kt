package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.CreateEventRequest
import com.mievento.ideal.data.repositories.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface CreateEventView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun onEventCreated()
}

class CreateEventPresenter(
    private val view: CreateEventView,
    private val eventRepository: EventRepository
) {

    fun createEvent(
        name: String,
        type: String,
        date: String,
        time: String,
        location: String,
        description: String,
        notes: String,
        budget: String,
        imageUrl: String? = null // ✅ AGREGAR ESTE PARÁMETRO
    ) {
        // Validaciones
        if (name.isBlank()) {
            view.showError("El nombre del evento es requerido")
            return
        }

        if (date.isBlank()) {
            view.showError("La fecha del evento es requerida")
            return
        }

        if (time.isBlank()) {
            view.showError("La hora del evento es requerida")
            return
        }

        if (location.isBlank()) {
            view.showError("La ubicación del evento es requerida")
            return
        }

        val budgetValue = try {
            if (budget.isBlank()) 0.0 else budget.toDouble()
        } catch (e: NumberFormatException) {
            view.showError("El presupuesto debe ser un número válido")
            return
        }

        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            val eventRequest = CreateEventRequest(
                name = name,
                type = type,
                event_date = date,
                event_time = time,
                location = location,
                description = description.ifBlank { null },
                notes = notes.ifBlank { null },
                budget = budgetValue
                // TODO: Agregar main_image = imageUrl cuando actualices el modelo
            )

            val result = eventRepository.createEvent(eventRequest)

            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onEventCreated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al crear evento")
                }
            }
        }
    }
}