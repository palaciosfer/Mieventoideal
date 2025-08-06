package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.CreateExpenseRequest
import com.mievento.ideal.data.models.Expense
import com.mievento.ideal.data.repositories.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ExpenseListView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showExpenses(expenses: List<Expense>)
    fun onExpenseCreated()
    fun onExpenseUpdated()
    fun onExpenseDeleted()
    fun showCreateExpenseDialog()
    fun showEditExpenseDialog(expense: Expense)
}

class ExpenseListPresenter(
    private val view: ExpenseListView,
    private val expenseRepository: ExpenseRepository
) {

    fun loadExpenses(eventId: Int) {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val result = expenseRepository.getExpenses(eventId)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.showExpenses(result.getOrNull() ?: emptyList())
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al cargar gastos")
                }
            }
        }
    }

    fun createExpense(
        eventId: Int,
        category: String,
        description: String,
        amount: String,
        expenseDate: String,
        notes: String
    ) {
        if (category.isBlank()) {
            view.showError("La categoría es requerida")
            return
        }
        if (description.isBlank()) {
            view.showError("La descripción es requerida")
            return
        }
        if (amount.isBlank()) {
            view.showError("El monto es requerido")
            return
        }
        if (expenseDate.isBlank()) {
            view.showError("La fecha es requerida")
            return
        }

        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            view.showError("El monto debe ser un número mayor a 0")
            return
        }

        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateExpenseRequest(
                category = category,
                description = description,
                amount = amountValue,
                expense_date = expenseDate,
                notes = notes.ifBlank { null }
            )

            val result = expenseRepository.createExpense(eventId, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onExpenseCreated()
                    loadExpenses(eventId)
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al crear gasto")
                }
            }
        }
    }

    fun updateExpense(
        id: Int,
        category: String,
        description: String,
        amount: String,
        expenseDate: String,
        notes: String
    ) {
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            view.showError("El monto debe ser un número mayor a 0")
            return
        }

        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = CreateExpenseRequest(
                category = category,
                description = description,
                amount = amountValue,
                expense_date = expenseDate,
                notes = notes.ifBlank { null }
            )

            val result = expenseRepository.updateExpense(id, request)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                if (result.isSuccess) {
                    view.onExpenseUpdated()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al actualizar gasto")
                }
            }
        }
    }

    fun deleteExpense(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = expenseRepository.deleteExpense(id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    view.onExpenseDeleted()
                } else {
                    view.showError(result.exceptionOrNull()?.message ?: "Error al eliminar gasto")
                }
            }
        }
    }
}