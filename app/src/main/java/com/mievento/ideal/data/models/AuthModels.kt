package com.mievento.ideal.data.models

// Modelos de Request
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val phone: String
)

data class VerifyPhoneRequest(
    val code: String
)

data class UpdateProfileRequest(
    val full_name: String,
    val phone: String
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

// Modelos de Response
data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val user: User? = null,
    val message: String? = null,
    val error: String? = null,
    val verification_code: String? = null // Para testing/MVP
)

// Modelo de Usuario
data class User(
    val id: Int,
    val email: String,
    val full_name: String,
    val phone: String? = null,
    val phone_verified: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)