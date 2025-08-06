package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class TaskRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun createTask(eventId: Int, request: CreateTaskRequest): Result<Task> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.createTask(authHeader, eventId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.task!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al crear tarea"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTasks(eventId: Int): Result<List<Task>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getTasks(authHeader, eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.tasks ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener tareas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(id: Int, request: CreateTaskRequest): Result<Task> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.updateTask(authHeader, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.task!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al actualizar tarea"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(id: Int, status: String): Result<Task> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.updateTaskStatus(
                authHeader, id, mapOf("status" to status)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.task!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al actualizar estado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(id: Int): Result<Boolean> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.deleteTask(authHeader, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al eliminar tarea"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}