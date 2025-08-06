package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class ProviderRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun createProvider(eventId: Int, request: CreateProviderRequest): Result<Provider> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.createProvider(authHeader, eventId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.provider!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al crear proveedor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProviders(eventId: Int): Result<List<Provider>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getProviders(authHeader, eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.providers ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener proveedores"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProvider(id: Int, request: CreateProviderRequest): Result<Provider> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.updateProvider(authHeader, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.provider!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al actualizar proveedor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProvider(id: Int): Result<Boolean> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.deleteProvider(authHeader, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al eliminar proveedor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}