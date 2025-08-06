package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class NotificationRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun getNotifications(): Result<List<Notification>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getNotifications(authHeader)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.notifications ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener notificaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: Int): Result<Boolean> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.markNotificationAsRead(authHeader, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al marcar como leída"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getUnreadNotificationCount(authHeader)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.unread_count ?: 0)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener contador"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}