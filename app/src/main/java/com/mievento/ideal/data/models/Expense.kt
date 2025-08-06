package com.mievento.ideal.data.models

data class Expense(
    val id: Int,
    val event_id: Int,
    val provider_id: Int?,
    val category: String,
    val description: String,
    val amount: Double,
    val expense_date: String,
    val receipt_file: String?,
    val notes: String?,
    val provider_name: String?,
    val created_at: String,
    val updated_at: String
)

data class CreateExpenseRequest(
    val provider_id: Int? = null,
    val category: String,
    val description: String,
    val amount: Double,
    val expense_date: String,
    val receipt_file: String? = null,
    val notes: String? = null
)

data class ExpenseResponse(
    val success: Boolean,
    val message: String? = null,
    val expense: Expense? = null,
    val expenses: List<Expense>? = null,
    val error: String? = null
)