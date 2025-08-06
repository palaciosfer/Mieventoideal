package com.mievento.ideal.presentation.presenters

import android.util.Log
import com.mievento.ideal.data.repositories.ParticipationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserPresenter(
    private val view: UserView,
    private val participationRepository: ParticipationRepository
) {

    companion object {
        private const val TAG = "UserPresenter"
    }

    fun loadAvailableEvents() {
        Log.d(TAG, "📋 Cargando eventos disponibles...")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = participationRepository.getAvailableEvents()

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val events = result.getOrNull() ?: emptyList()
                        Log.d(TAG, "✅ ${events.size} eventos disponibles cargados")
                        view.showAvailableEvents(events)
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error al cargar eventos"
                        Log.e(TAG, "❌ Error cargando eventos: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción cargando eventos", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    fun requestParticipation(eventId: Int) {
        Log.d(TAG, "🎯 Solicitando participación en evento $eventId")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = participationRepository.requestParticipation(eventId, "")

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Participación solicitada exitosamente")
                        view.onParticipationRequested()
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error al solicitar participación"
                        Log.e(TAG, "❌ Error solicitando participación: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción solicitando participación", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }
}