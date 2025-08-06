package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.RescheduleRequest
import com.mievento.ideal.data.repositories.RescheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface UserRescheduleView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showRequests(requests: List<RescheduleRequest>)
}

class UserReschedulePresenter(
    private val view: UserRescheduleView,
    private val rescheduleRepository: RescheduleRepository
) {

    fun loadRequests() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = rescheduleRepository.getUserRequests()
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
}