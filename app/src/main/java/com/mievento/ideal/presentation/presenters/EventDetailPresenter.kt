package com.mievento.ideal.presentation.presenters

import android.util.Log
import com.mievento.ideal.data.repositories.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDetailPresenter(
    private val view: EventDetailView,
    private val eventRepository: EventRepository
) {

    companion object {
        private const val TAG = "EventDetailPresenter"
    }

    fun loadEventDetails(eventId: Int) {
        Log.d(TAG, "📋 Cargando detalles del evento: $eventId")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.getEventById(eventId)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val event = result.getOrNull()
                        if (event != null) {
                            Log.d(TAG, "✅ Detalles del evento cargados: ${event.name}")
                            Log.d(TAG, "📷 Imagen del evento: ${event.main_image}")
                            view.showEventDetails(event)
                        } else {
                            Log.e(TAG, "❌ Evento no encontrado")
                            view.showError("Evento no encontrado")
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                        Log.e(TAG, "❌ Error cargando evento: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción cargando evento", e)
                withContext(Dispatchers.Main) {
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    fun deleteEvent(eventId: Int) {
        Log.d(TAG, "🗑️ Eliminando evento: $eventId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.deleteEvent(eventId)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Evento eliminado exitosamente")
                        view.onEventDeleted()
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error al eliminar"
                        Log.e(TAG, "❌ Error eliminando evento: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción eliminando evento", e)
                withContext(Dispatchers.Main) {
                    view.showError("Error eliminando evento: ${e.message}")
                }
            }
        }
    }
}