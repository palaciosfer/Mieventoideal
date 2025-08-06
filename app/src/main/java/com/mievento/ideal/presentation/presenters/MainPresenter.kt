package com.mievento.ideal.presentation.presenters

import android.util.Log
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPresenter(
    private val view: MainView,
    private val eventRepository: EventRepository
) {

    companion object {
        private const val TAG = "MainPresenter"
    }

    /**
     * Cargar todos los eventos del administrador
     */
    fun loadEvents() {
        Log.d(TAG, "📋 Iniciando carga de eventos...")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.getEvents()

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val events = result.getOrNull() ?: emptyList()
                        Log.d(TAG, "✅ Eventos cargados exitosamente: ${events.size}")

                        // Log detallado de eventos
                        events.forEachIndexed { index, event ->
                            Log.d(TAG, "   Evento $index: ${event.name} (${event.status}) - Imagen: ${event.main_image}")
                        }

                        view.showEvents(events)
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error desconocido al cargar eventos"
                        Log.e(TAG, "❌ Error cargando eventos: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al cargar eventos", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    /**
     * Cargar eventos con filtros específicos
     */
    fun loadEventsWithFilters(
        status: String? = null,
        type: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        Log.d(TAG, "📋 Cargando eventos con filtros - Status: $status, Type: $type")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.getEventsWithFilters(status, type, dateFrom, dateTo)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val events = result.getOrNull() ?: emptyList()
                        Log.d(TAG, "✅ Eventos filtrados cargados: ${events.size}")
                        view.showEvents(events)
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error al aplicar filtros"
                        Log.e(TAG, "❌ Error con filtros: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción con filtros", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error al aplicar filtros: ${e.message}")
                }
            }
        }
    }

    /**
     * 🔥 NUEVO: Actualizar estado de un evento (planeacion → activo, etc.)
     */
    fun updateEventStatus(eventId: Int, newStatus: String) {
        Log.d(TAG, "🔄 Actualizando estado del evento $eventId a '$newStatus'")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.updateEventStatus(eventId, newStatus)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val updatedEvent = result.getOrNull()
                        Log.d(TAG, "✅ Estado actualizado exitosamente para evento $eventId")
                        Log.d(TAG, "   Nuevo estado: ${updatedEvent?.status}")

                        val statusMessage = when (newStatus) {
                            "activo" -> "🚀 Evento publicado y visible para usuarios"
                            "planeacion" -> "⏸️ Evento pausado y oculto de usuarios"
                            "finalizado" -> "✅ Evento marcado como finalizado"
                            "cancelado" -> "❌ Evento cancelado"
                            else -> "🔄 Estado del evento actualizado"
                        }

                        view.showMessage(statusMessage)
                        view.onEventStatusUpdated()

                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error al actualizar estado"
                        Log.e(TAG, "❌ Error actualizando estado: $error")
                        view.showError("Error al actualizar estado: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción actualizando estado", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión al actualizar estado: ${e.message}")
                }
            }
        }
    }

    /**
     * Eliminar un evento
     */
    fun deleteEvent(eventId: Int) {
        Log.d(TAG, "🗑️ Eliminando evento $eventId")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.deleteEvent(eventId)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Evento $eventId eliminado exitosamente")
                        view.onEventDeleted()
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error al eliminar evento"
                        Log.e(TAG, "❌ Error eliminando evento: $error")
                        view.showError("Error al eliminar evento: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción eliminando evento", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión al eliminar evento: ${e.message}")
                }
            }
        }
    }

    /**
     * Obtener estadísticas generales de eventos
     */
    fun loadEventStats() {
        Log.d(TAG, "📊 Cargando estadísticas de eventos")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.getEvents()

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val events = result.getOrNull() ?: emptyList()

                        val stats = calculateEventStats(events)
                        Log.d(TAG, "📊 Estadísticas calculadas: $stats")

                        view.showMessage("📊 Total: ${stats.total} eventos")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error calculando estadísticas", e)
            }
        }
    }

    /**
     * Buscar eventos por nombre
     */
    fun searchEvents(query: String) {
        if (query.trim().isEmpty()) {
            loadEvents()
            return
        }

        Log.d(TAG, "🔍 Buscando eventos con query: '$query'")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.searchEvents(query)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val filteredEvents = result.getOrNull() ?: emptyList()
                        Log.d(TAG, "🔍 Encontrados ${filteredEvents.size} eventos con '$query'")
                        view.showEvents(filteredEvents)

                        if (filteredEvents.isEmpty()) {
                            view.showMessage("🔍 No se encontraron eventos con '$query'")
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error en búsqueda"
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en búsqueda", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error en búsqueda: ${e.message}")
                }
            }
        }
    }

    /**
     * Recargar eventos (pull to refresh)
     */
    fun refreshEvents() {
        Log.d(TAG, "🔄 Refrescando eventos...")
        loadEvents()
    }

    /**
     * Duplicar un evento
     */
    fun duplicateEvent(eventId: Int) {
        Log.d(TAG, "📋 Duplicando evento $eventId")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.getEventById(eventId)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val originalEvent = result.getOrNull()
                        if (originalEvent != null) {
                            Log.d(TAG, "✅ Evento obtenido para duplicar: ${originalEvent.name}")
                            view.hideLoading()
                            view.showMessage("📋 Función de duplicar - Próximamente")
                        } else {
                            view.hideLoading()
                            view.showError("No se pudo obtener el evento a duplicar")
                        }
                    } else {
                        view.hideLoading()
                        view.showError("Error al obtener evento para duplicar")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error duplicando evento", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error al duplicar evento: ${e.message}")
                }
            }
        }
    }

    /**
     * Calcular estadísticas de eventos
     */
    private fun calculateEventStats(events: List<Event>): EventStats {
        val total = events.size
        val planeacion = events.count { it.status == "planeacion" }
        val activos = events.count { it.status == "activo" }
        val finalizados = events.count { it.status == "finalizado" }
        val cancelados = events.count { it.status == "cancelado" }

        val totalBudget = events.sumOf { it.budget }

        val eventsByType = events.groupBy { it.type }.mapValues { it.value.size }

        return EventStats(
            total = total,
            planeacion = planeacion,
            activos = activos,
            finalizados = finalizados,
            cancelados = cancelados,
            totalBudget = totalBudget,
            eventsByType = eventsByType
        )
    }

    /**
     * Validar datos de evento antes de operaciones
     */
    private fun validateEventOperation(eventId: Int): Boolean {
        if (eventId <= 0) {
            Log.e(TAG, "❌ ID de evento inválido: $eventId")
            view.showError("ID de evento inválido")
            return false
        }
        return true
    }

    /**
     * Manejar errores de red específicos
     */
    private fun handleNetworkError(error: Throwable) {
        when {
            error.message?.contains("timeout", ignoreCase = true) == true -> {
                view.showError("⏱️ Tiempo de espera agotado. Verifica tu conexión.")
            }
            error.message?.contains("network", ignoreCase = true) == true -> {
                view.showError("🌐 Error de red. Verifica tu conexión a internet.")
            }
            error.message?.contains("server", ignoreCase = true) == true -> {
                view.showError("🔧 Error del servidor. Intenta más tarde.")
            }
            else -> {
                view.showError("❌ Error: ${error.message}")
            }
        }
    }
}

/**
 * Data class para estadísticas de eventos
 */
data class EventStats(
    val total: Int,
    val planeacion: Int,
    val activos: Int,
    val finalizados: Int,
    val cancelados: Int,
    val totalBudget: Double,
    val eventsByType: Map<String, Int>
) {
    override fun toString(): String {
        return "EventStats(total=$total, activos=$activos, planeacion=$planeacion, finalizados=$finalizados, cancelados=$cancelados)"
    }
}