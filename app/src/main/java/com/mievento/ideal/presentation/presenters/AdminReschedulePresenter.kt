package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.RescheduleRequest
import com.mievento.ideal.data.repositories.RescheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface AdminRescheduleView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showRequests(requests: List<RescheduleRequest>)
    fun onRequestResponded()
}

class AdminReschedulePresenter(
    private val view: AdminRescheduleView,
    private val rescheduleRepository: RescheduleRepository
) {

    fun loadRequests() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = rescheduleRepository.getAdminRequests()
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showRequests(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar solicitudes")
                }
            }
        }
    }

    fun respondToRequest(id: Int, status: String, adminResponse: String) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = rescheduleRepository.respondToRequest(id, status, adminResponse)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onRequestResponded()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al responder")
                }
            }
        }
    }
}