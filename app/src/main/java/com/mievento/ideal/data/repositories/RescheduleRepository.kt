package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class RescheduleRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun createRescheduleRequest(eventId: Int, request: CreateRescheduleRequest): Result<RescheduleRequest> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.createRescheduleRequest(authHeader, eventId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.request!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al crear solicitud"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRequests(): Result<List<RescheduleRequest>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getUserRescheduleRequests(authHeader)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.requests ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener solicitudes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminRequests(): Result<List<RescheduleRequest>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getAdminRescheduleRequests(authHeader)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.requests ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener solicitudes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToRequest(id: Int, status: String, adminResponse: String): Result<RescheduleRequest> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.respondToRescheduleRequest(
                authHeader, id, mapOf("status" to status, "admin_response" to adminResponse)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.request!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al responder solicitud"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}