package com.mievento.ideal.data.repositories

import android.util.Log
import com.mievento.ideal.data.api.ApiService
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.models.CreateEventRequest
import com.mievento.ideal.data.models.EventListResponse
import com.mievento.ideal.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class EventRepository(
    private val tokenManager: TokenManager
) {
    private val apiService = ApiService.create(tokenManager)

    companion object {
        private const val TAG = "EventRepository"
    }

    private fun getAuthHeader(): String {
        val token = tokenManager.getToken()
        return "Bearer $token"
    }

    /**
     * Obtener todos los eventos del usuario autenticado
     */
    suspend fun getEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📋 Obteniendo eventos del usuario...")

            val response = apiService.getEvents(getAuthHeader())

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "📡 Respuesta recibida - Success: ${body?.success}")

                if (body?.success == true && body.events != null) {
                    Log.d(TAG, "✅ ${body.events.size} eventos obtenidos exitosamente")

                    // Log detallado de eventos
                    body.events.forEachIndexed { index, event ->
                        Log.d(TAG, "   Evento $index: ${event.name} (${event.status}) - Imagen: ${event.main_image}")
                    }

                    Result.success(body.events)
                } else {
                    val error = body?.message ?: "Error desconocido al obtener eventos"
                    Log.e(TAG, "❌ Error en respuesta: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ HTTP Error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = handleHttpException(e, "obtener eventos")
            Log.e(TAG, "❌ HTTP Error obteniendo eventos: $errorMessage")
            Result.failure(Exception(errorMessage))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "❌ Timeout obteniendo eventos")
            Result.failure(Exception("Tiempo de espera agotado. Verifica tu conexión."))
        } catch (e: IOException) {
            Log.e(TAG, "❌ IO Error obteniendo eventos", e)
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inesperado obteniendo eventos", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Obtener eventos con filtros específicos
     */
    suspend fun getEventsWithFilters(
        status: String? = null,
        type: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📋 Obteniendo eventos con filtros - Status: $status, Type: $type")

            val response = apiService.getEventsWithFilters(getAuthHeader(), status, type, dateFrom, dateTo)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.events != null) {
                    Log.d(TAG, "✅ ${body.events.size} eventos filtrados obtenidos")
                    Result.success(body.events)
                } else {
                    val error = body?.message ?: "Error al aplicar filtros"
                    Log.e(TAG, "❌ Error en filtros: $error")
                    Result.failure(Exception(error))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error con filtros", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener un evento específico por ID
     */
    suspend fun getEventById(eventId: Int): Result<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Obteniendo evento por ID: $eventId")

            if (eventId <= 0) {
                return@withContext Result.failure(Exception("ID de evento inválido"))
            }

            val response = apiService.getEvent(getAuthHeader(), eventId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.event != null) {
                    Log.d(TAG, "✅ Evento obtenido: ${body.event.name}")
                    Result.success(body.event)
                } else {
                    val error = body?.message ?: "Evento no encontrado"
                    Log.e(TAG, "❌ Error obteniendo evento: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Evento no encontrado"
                    403 -> "No tienes permiso para ver este evento"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Log.e(TAG, "❌ HTTP Error obteniendo evento: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo evento", e)
            Result.failure(e)
        }
    }

    /**
     * Crear un nuevo evento
     */
    suspend fun createEvent(eventRequest: CreateEventRequest): Result<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "➕ Creando evento: ${eventRequest.name}")
            Log.d(TAG, "   Tipo: ${eventRequest.type}")
            Log.d(TAG, "   Fecha: ${eventRequest.event_date}")
            Log.d(TAG, "   Imagen: ${eventRequest.main_image}")

            val response = apiService.createEvent(getAuthHeader(), eventRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.event != null) {
                    Log.d(TAG, "✅ Evento creado exitosamente: ${body.event.name}")
                    Log.d(TAG, "   ID: ${body.event.id}")
                    Log.d(TAG, "   Estado: ${body.event.status}")
                    Log.d(TAG, "   Imagen guardada: ${body.event.main_image}")
                    Result.success(body.event)
                } else {
                    val error = body?.message ?: "Error al crear evento"
                    Log.e(TAG, "❌ Error creando evento: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ HTTP Error creando evento: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando evento", e)
            Result.failure(e)
        }
    }

    /**
     * 🔥 NUEVO: Actualizar estado de un evento (planeacion → activo, etc.)
     */
    suspend fun updateEventStatus(eventId: Int, status: String): Result<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Actualizando estado del evento $eventId a '$status'")

            if (eventId <= 0) {
                return@withContext Result.failure(Exception("ID de evento inválido"))
            }

            val validStatuses = listOf("planeacion", "activo", "finalizado", "cancelado")
            if (!validStatuses.contains(status)) {
                return@withContext Result.failure(Exception("Estado inválido: $status"))
            }

            val statusData = mapOf("status" to status)
            val response = apiService.updateEventStatus(getAuthHeader(), eventId, statusData)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.event != null) {
                    Log.d(TAG, "✅ Estado actualizado exitosamente para evento $eventId")
                    Log.d(TAG, "   Estado nuevo: ${body.event.status}")
                    Log.d(TAG, "   Evento: ${body.event.name}")

                    val statusMessage = when (status) {
                        "activo" -> "🚀 Evento publicado y visible para usuarios"
                        "planeacion" -> "⏸️ Evento pausado y oculto de usuarios"
                        "finalizado" -> "✅ Evento marcado como finalizado"
                        "cancelado" -> "❌ Evento cancelado"
                        else -> "🔄 Estado actualizado"
                    }
                    Log.d(TAG, "   $statusMessage")

                    Result.success(body.event)
                } else {
                    val error = body?.message ?: "Error al actualizar estado del evento"
                    Log.e(TAG, "❌ Error actualizando estado: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Evento no encontrado"
                    403 -> "No tienes permiso para modificar este evento"
                    400 -> "Estado inválido o datos incorrectos"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Log.e(TAG, "❌ HTTP Error actualizando estado: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando estado", e)
            Result.failure(e)
        }
    }

    /**
     * Actualizar un evento completo
     */
    suspend fun updateEvent(eventId: Int, eventRequest: CreateEventRequest): Result<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "✏️ Actualizando evento $eventId: ${eventRequest.name}")

            if (eventId <= 0) {
                return@withContext Result.failure(Exception("ID de evento inválido"))
            }

            val response = apiService.updateEvent(getAuthHeader(), eventId, eventRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.event != null) {
                    Log.d(TAG, "✅ Evento actualizado exitosamente: ${body.event.name}")
                    Result.success(body.event)
                } else {
                    val error = body?.message ?: "Error al actualizar evento"
                    Log.e(TAG, "❌ Error actualizando evento: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ HTTP Error actualizando evento: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando evento", e)
            Result.failure(e)
        }
    }

    /**
     * Eliminar un evento
     */
    suspend fun deleteEvent(eventId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Eliminando evento $eventId")

            if (eventId <= 0) {
                return@withContext Result.failure(Exception("ID de evento inválido"))
            }

            val response = apiService.deleteEvent(getAuthHeader(), eventId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Evento $eventId eliminado exitosamente")
                    Result.success(true)
                } else {
                    val error = body?.message ?: "Error al eliminar evento"
                    Log.e(TAG, "❌ Error eliminando evento: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Evento no encontrado"
                    403 -> "No tienes permiso para eliminar este evento"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Log.e(TAG, "❌ HTTP Error eliminando evento: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando evento", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener resumen/estadísticas de un evento
     */
    suspend fun getEventSummary(eventId: Int): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📊 Obteniendo resumen del evento $eventId")

            if (eventId <= 0) {
                return@withContext Result.failure(Exception("ID de evento inválido"))
            }

            val response = apiService.getEventSummary(getAuthHeader(), eventId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "✅ Resumen obtenido para evento $eventId")
                    val summaryMap = mapOf(
                        "success" to (body.success ?: false),
                        "summary" to body
                    )
                    Result.success(summaryMap)
                } else {
                    val error = "Error al obtener resumen"
                    Log.e(TAG, "❌ Error obteniendo resumen: $error")
                    Result.failure(Exception(error))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo resumen", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener alertas de presupuesto de un evento
     */
    suspend fun getBudgetAlert(eventId: Int): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "💰 Obteniendo alertas de presupuesto para evento $eventId")

            val response = apiService.getBudgetAlert(getAuthHeader(), eventId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "✅ Alertas de presupuesto obtenidas")
                    Result.success(body)
                } else {
                    Result.failure(Exception("Error al obtener alertas"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo alertas de presupuesto", e)
            Result.failure(e)
        }
    }

    /**
     * Buscar eventos por texto
     */
    suspend fun searchEvents(query: String): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Buscando eventos con query: '$query'")

            val allEventsResult = getEvents()

            if (allEventsResult.isSuccess) {
                val allEvents = allEventsResult.getOrNull() ?: emptyList()
                val filteredEvents = allEvents.filter { event ->
                    event.name.contains(query, ignoreCase = true) ||
                            event.location.contains(query, ignoreCase = true) ||
                            event.type.contains(query, ignoreCase = true) ||
                            event.description?.contains(query, ignoreCase = true) == true ||
                            event.notes?.contains(query, ignoreCase = true) == true
                }

                Log.d(TAG, "🔍 Búsqueda completada: ${filteredEvents.size} resultados para '$query'")
                Result.success(filteredEvents)
            } else {
                allEventsResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en búsqueda", e)
            Result.failure(e)
        }
    }

    /**
     * Manejar errores HTTP específicos
     */
    private fun handleHttpException(e: HttpException, operation: String): String {
        return when (e.code()) {
            400 -> "Datos inválidos para $operation"
            401 -> "No autenticado. Inicia sesión nuevamente"
            403 -> "No tienes permisos para $operation"
            404 -> "Recurso no encontrado para $operation"
            409 -> "Conflicto al $operation"
            422 -> "Datos incorretos para $operation"
            429 -> "Demasiadas solicitudes. Intenta más tarde"
            500 -> "Error del servidor al $operation"
            502, 503 -> "Servidor no disponible. Intenta más tarde"
            else -> "Error de conexión (${e.code()}) al $operation"
        }
    }

    /**
     * Obtener estadísticas de todos los eventos
     */
    suspend fun getEventsStatistics(): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📊 Calculando estadísticas de eventos")

            val eventsResult = getEvents()
            if (eventsResult.isSuccess) {
                val events = eventsResult.getOrNull() ?: emptyList()

                val stats = mapOf(
                    "total" to events.size,
                    "planeacion" to events.count { it.status == "planeacion" },
                    "activos" to events.count { it.status == "activo" },
                    "finalizados" to events.count { it.status == "finalizado" },
                    "cancelados" to events.count { it.status == "cancelado" },
                    "total_budget" to events.sumOf { it.budget },
                    "events_by_type" to events.groupBy { it.type }.mapValues { it.value.size }
                )

                Log.d(TAG, "📊 Estadísticas calculadas: $stats")
                Result.success(stats)
            } else {
                eventsResult.exceptionOrNull()?.let { Result.failure<Map<String, Any>>(it) }
                    ?: Result.failure(Exception("Error obteniendo eventos para estadísticas"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calculando estadísticas", e)
            Result.failure(e)
        }
    }
}