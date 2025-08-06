package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class GuestRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun createGuest(eventId: Int, request: CreateGuestRequest): Result<Guest> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.createGuest(authHeader, eventId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.guest!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al crear invitado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGuests(eventId: Int): Result<List<Guest>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getGuests(authHeader, eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.guests ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener invitados"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGuest(id: Int, request: CreateGuestRequest): Result<Guest> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.updateGuest(authHeader, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.guest!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al actualizar invitado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGuest(id: Int): Result<Boolean> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.deleteGuest(authHeader, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al eliminar invitado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agregar este método a la clase GuestRepository:

    suspend fun updateGuestConfirmation(id: Int, confirmationStatus: String): Result<Guest> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.updateGuestConfirmation(
                authHeader,
                id,
                mapOf("confirmation_status" to confirmationStatus)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.guest!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al actualizar confirmación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}