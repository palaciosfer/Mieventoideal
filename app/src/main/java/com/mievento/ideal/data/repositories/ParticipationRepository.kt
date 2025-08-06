package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class ParticipationRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun getAvailableEvents(): Result<List<Event>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getAvailableEvents(authHeader)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.events ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener eventos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestParticipation(eventId: Int, notes: String?): Result<EventParticipation> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val request = RequestParticipationRequest(notes)
            val response = RetrofitClient.apiService.requestParticipation(authHeader, eventId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.participation!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al solicitar participación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyParticipations(): Result<List<EventParticipation>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getMyParticipations(authHeader)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.participations ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener participaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventParticipants(eventId: Int): Result<List<EventParticipation>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getEventParticipants(authHeader, eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.participants ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener participantes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToParticipation(eventId: Int, userId: Int, status: String, message: String?): Result<Boolean> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val request = RespondParticipationRequest(status, message)
            val response = RetrofitClient.apiService.respondToParticipation(authHeader, eventId, userId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al responder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}