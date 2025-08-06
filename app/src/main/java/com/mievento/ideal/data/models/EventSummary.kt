package com.mievento.ideal.data.models

data class EventSummary(
    val event: Event,
    val tasks: TaskStats,
    val guests: GuestStats,
    val providers: ProviderStats,
    val budget: BudgetStats,
    val budget_alert: BudgetAlert?
)

data class TaskStats(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val in_progress: Int,
    val completion_percentage: Int
)

data class GuestStats(
    val total: Int,
    val confirmed: Int,
    val pending: Int,
    val rejected: Int
)

data class ProviderStats(
    val total: Int,
    val hired: Int,
    val estimated_budget: Double
)

data class BudgetStats(
    val allocated: Double,
    val spent: Double,
    val remaining: Double,
    val percentage_used: Int
)

data class BudgetAlert(
    val alert: Boolean,
    val percentage: Int,
    val budget: Double,
    val spent: Double,
    val remaining: Double
)

data class EventSummaryResponse(
    val success: Boolean,
    val summary: EventSummary? = null,
    val error: String? = null
)