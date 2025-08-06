package com.mievento.ideal.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TokenManager(context: Context) {

    companion object {
        private const val PREF_NAME = "MiEventoPrefs"
        private const val TOKEN_KEY = "auth_token"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_ID_KEY = "user_id" // 🔥 NUEVO
        private const val USER_ROLE_KEY = "user_role" // 🔥 NUEVO
        private const val TAG = "TokenManager"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        Log.d(TAG, "💾 Guardando token: ${token.take(20)}...")
        sharedPreferences.edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(TOKEN_KEY, null)
        Log.d(TAG, "🔑 Token obtenido: ${token?.take(20) ?: "null"}...")
        return token
    }

    fun hasValidToken(): Boolean {
        val token = getToken()
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "❌ No hay token guardado")
            return false
        }

        // Aquí podrías agregar validación de expiración si tienes esa info
        // Por ahora, solo verificamos que existe
        Log.d(TAG, "✅ Token encontrado: ${token.take(20)}...")
        return true
    }

    fun saveUserInfo(email: String, name: String) {
        Log.d(TAG, "👤 Guardando info usuario: $email")
        sharedPreferences.edit()
            .putString(USER_EMAIL_KEY, email)
            .putString(USER_NAME_KEY, name)
            .apply()
    }

    // 🔥 NUEVO: Método para guardar datos completos del usuario
    fun saveUserData(userId: Int, email: String, name: String) {
        Log.d(TAG, "👤 Guardando datos completos usuario: ID=$userId, Email=$email")
        sharedPreferences.edit()
            .putInt(USER_ID_KEY, userId)
            .putString(USER_EMAIL_KEY, email)
            .putString(USER_NAME_KEY, name)
            .apply()
    }

    // 🔥 NUEVO: Métodos para rol de usuario
    fun saveUserRole(role: String) {
        Log.d(TAG, "🎭 Guardando rol de usuario: $role")
        sharedPreferences.edit()
            .putString(USER_ROLE_KEY, role)
            .apply()
    }

    fun getUserRole(): String? {
        val role = sharedPreferences.getString(USER_ROLE_KEY, null)
        Log.d(TAG, "🎭 Rol obtenido: $role")
        return role
    }

    // 🔥 NUEVO: Método para obtener ID de usuario
    fun getUserId(): Int {
        val userId = sharedPreferences.getInt(USER_ID_KEY, -1)
        Log.d(TAG, "🆔 ID de usuario obtenido: $userId")
        return userId
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(USER_EMAIL_KEY, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(USER_NAME_KEY, null)
    }

    // 🔥 NUEVO: Método para verificar si es administrador
    fun isAdmin(): Boolean {
        val role = getUserRole()
        val isAdmin = role == "admin" || role == "administrator"
        Log.d(TAG, "👑 Es administrador: $isAdmin (rol: $role)")
        return isAdmin
    }

    // 🔥 NUEVO: Método para verificar si es usuario regular
    fun isUser(): Boolean {
        val role = getUserRole()
        val isUser = role == "user" || role == "participant"
        Log.d(TAG, "👤 Es usuario regular: $isUser (rol: $role)")
        return isUser
    }

    fun clearToken() {
        Log.d(TAG, "🗑️ Limpiando token y datos de usuario")
        sharedPreferences.edit()
            .remove(TOKEN_KEY)
            .remove(USER_EMAIL_KEY)
            .remove(USER_NAME_KEY)
            .remove(USER_ID_KEY) // 🔥 NUEVO
            .remove(USER_ROLE_KEY) // 🔥 NUEVO
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return hasValidToken()
    }

    // 🔥 NUEVO: Método para obtener información completa del usuario
    fun getUserInfo(): UserInfo? {
        val token = getToken()
        val userId = getUserId()
        val email = getUserEmail()
        val name = getUserName()
        val role = getUserRole()

        return if (token != null && userId != -1 && email != null && name != null) {
            UserInfo(
                id = userId,
                email = email,
                name = name,
                role = role,
                token = token
            )
        } else {
            Log.d(TAG, "❌ Información de usuario incompleta")
            null
        }
    }

    // 🔥 NUEVO: Método para debug - mostrar toda la info guardada
    fun logStoredData() {
        Log.d(TAG, "📋 === DATOS ALMACENADOS ===")
        Log.d(TAG, "🔑 Token: ${getToken()?.take(20)}...")
        Log.d(TAG, "🆔 User ID: ${getUserId()}")
        Log.d(TAG, "📧 Email: ${getUserEmail()}")
        Log.d(TAG, "👤 Name: ${getUserName()}")
        Log.d(TAG, "🎭 Role: ${getUserRole()}")
        Log.d(TAG, "✅ Logged in: ${isLoggedIn()}")
        Log.d(TAG, "👑 Is admin: ${isAdmin()}")
        Log.d(TAG, "========================")
    }
}

// 🔥 NUEVA: Data class para información del usuario
data class UserInfo(
    val id: Int,
    val email: String,
    val name: String,
    val role: String?,
    val token: String
)