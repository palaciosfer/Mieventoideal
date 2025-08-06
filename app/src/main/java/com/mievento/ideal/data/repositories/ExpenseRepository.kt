package com.mievento.ideal.data.repositories

import com.mievento.ideal.data.api.RetrofitClient
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager

class ExpenseRepository(private val tokenManager: TokenManager) {

    private fun getAuthHeader(): String? {
        val token = tokenManager.getToken()
        return if (token != null) "Bearer $token" else null
    }

    suspend fun createExpense(eventId: Int, request: CreateExpenseRequest): Result<Expense> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.createExpense(authHeader, eventId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.expense!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al crear gasto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenses(eventId: Int): Result<List<Expense>> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.getExpenses(authHeader, eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.expenses ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al obtener gastos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpense(id: Int, request: CreateExpenseRequest): Result<Expense> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.updateExpense(authHeader, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.expense!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al actualizar gasto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(id: Int): Result<Boolean> {
        return try {
            val authHeader = getAuthHeader() ?: return Result.failure(Exception("No autenticado"))
            val response = RetrofitClient.apiService.deleteExpense(authHeader, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error al eliminar gasto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}