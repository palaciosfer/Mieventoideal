package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.CreateGuestRequest
import com.mievento.ideal.data.models.Guest
import com.mievento.ideal.data.repositories.GuestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface GuestListView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showGuests(guests: List<Guest>)
    fun onGuestCreated()
    fun onGuestUpdated()
    fun onGuestDeleted()
    fun showCreateGuestDialog()
    fun showEditGuestDialog(guest: Guest)
}

class GuestListPresenter(
    private val view: GuestListView,
    private val guestRepository: GuestRepository
) {

    fun loadGuests(eventId: Int) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = guestRepository.getGuests(eventId)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showGuests(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar invitados")
                }
            }
        }
    }

    fun createGuest(eventId: Int, name: String, email: String, phone: String, relationship: String, notes: String) {
        if (name.isBlank()) {
            view.showError("El nombre es requerido")
            return
        }

        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateGuestRequest(
                name = name,
                email = email.ifBlank { null },
                phone = phone.ifBlank { null },
                relationship = relationship.ifBlank { null },
                notes = notes.ifBlank { null }
            )

            val result = guestRepository.createGuest(eventId, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onGuestCreated()
                    loadGuests(eventId)
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al crear invitado")
                }
            }
        }
    }

    fun updateGuest(id: Int, name: String, email: String, phone: String, relationship: String, notes: String) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateGuestRequest(
                name = name,
                email = email.ifBlank { null },
                phone = phone.ifBlank { null },
                relationship = relationship.ifBlank { null },
                notes = notes.ifBlank { null }
            )

            val result = guestRepository.updateGuest(id, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onGuestUpdated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al actualizar invitado")
                }
            }
        }
    }

    fun deleteGuest(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = guestRepository.deleteGuest(id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onGuestDeleted()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al eliminar invitado")
                }
            }
        }
    }

    fun updateGuestConfirmation(id: Int, status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = guestRepository.updateGuestConfirmation(id, status)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onGuestUpdated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al actualizar confirmación")
                }
            }
        }
    }
}