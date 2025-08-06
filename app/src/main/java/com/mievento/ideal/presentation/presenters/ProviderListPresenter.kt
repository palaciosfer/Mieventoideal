package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.CreateProviderRequest
import com.mievento.ideal.data.models.Provider
import com.mievento.ideal.data.repositories.ProviderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ProviderListView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showProviders(providers: List<Provider>)
    fun onProviderCreated()
    fun onProviderUpdated()
    fun onProviderDeleted()
    fun showCreateProviderDialog()
    fun showEditProviderDialog(provider: Provider)
}

class ProviderListPresenter(
    private val view: ProviderListView,
    private val providerRepository: ProviderRepository
) {

    fun loadProviders(eventId: Int) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = providerRepository.getProviders(eventId)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showProviders(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar proveedores")
                }
            }
        }
    }

    fun createProvider(
        eventId: Int,
        name: String,
        service: String,
        contactName: String,
        phone: String,
        email: String,
        estimatedBudget: String,
        notes: String
    ) {
        if (name.isBlank()) {
            view.showError("El nombre es requerido")
            return
        }
        if (service.isBlank()) {
            view.showError("El servicio es requerido")
            return
        }

        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val budget = estimatedBudget.toDoubleOrNull()
            val request = CreateProviderRequest(
                name = name,
                service = service,
                contact_name = contactName.ifBlank { null },
                phone = phone.ifBlank { null },
                email = email.ifBlank { null },
                estimated_budget = budget,
                notes = notes.ifBlank { null }
            )

            val result = providerRepository.createProvider(eventId, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onProviderCreated()
                    loadProviders(eventId)
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al crear proveedor")
                }
            }
        }
    }

    fun updateProvider(
        id: Int,
        name: String,
        service: String,
        contactName: String,
        phone: String,
        email: String,
        estimatedBudget: String,
        notes: String
    ) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val budget = estimatedBudget.toDoubleOrNull()
            val request = CreateProviderRequest(
                name = name,
                service = service,
                contact_name = contactName.ifBlank { null },
                phone = phone.ifBlank { null },
                email = email.ifBlank { null },
                estimated_budget = budget,
                notes = notes.ifBlank { null }
            )

            val result = providerRepository.updateProvider(id, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onProviderUpdated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al actualizar proveedor")
                }
            }
        }
    }

    fun deleteProvider(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = providerRepository.deleteProvider(id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onProviderDeleted()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al eliminar proveedor")
                }
            }
        }
    }
}