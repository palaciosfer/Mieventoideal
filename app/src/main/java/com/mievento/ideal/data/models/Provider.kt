package com.mievento.ideal.data.models

data class Provider(
    val id: Int,
    val event_id: Int,
    val name: String,
    val service: String,
    val contact_name: String?,
    val phone: String?,
    val email: String?,
    val estimated_budget: Double?,
    val actual_cost: Double?,
    val status: String = "cotizacion",
    val notes: String?,
    val contract_file: String?,
    val created_at: String,
    val updated_at: String
)

data class CreateProviderRequest(
    val name: String,
    val service: String,
    val contact_name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val estimated_budget: Double? = null,
    val notes: String? = null
)

data class ProviderResponse(
    val success: Boolean,
    val message: String? = null,
    val provider: Provider? = null,
    val providers: List<Provider>? = null,
    val error: String? = null
)