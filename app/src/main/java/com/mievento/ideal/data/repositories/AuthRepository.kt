package com.mievento.ideal.data.repositories

import android.util.Log
import com.mievento.ideal.data.api.ApiService
import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class AuthRepository(
    private val tokenManager: TokenManager
) {
    private val apiService = ApiService.create(tokenManager)

    companion object {
        private const val TAG = "AuthRepository"
    }

    private fun getAuthHeader(): String {
        val token = tokenManager.getToken()
        return "Bearer $token"
    }

    /**
     * Login de usuario
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔐 Iniciando login para: $email")

            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Login exitoso para: $email")

                    // Guardar datos básicos
                    body.token?.let { tokenManager.saveToken(it) }
                    body.user?.let { user ->
                        tokenManager.saveUserData(user.id, user.email, user.full_name)

                        // 🔥 DETERMINAR Y GUARDAR ROL AQUÍ
                        val userRole = determineUserRole(user.email)
                        tokenManager.saveUserRole(userRole)

                        Log.d(TAG, "🎭 Rol determinado: $userRole para ${user.email}")
                    }

                    Result.success(body)
                } else {
                    val error = body?.message ?: body?.error ?: "Error en login"
                    Log.e(TAG, "❌ Error en login: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ HTTP Error en login: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = handleHttpException(e, "login")
            Log.e(TAG, "❌ HTTP Exception en login: $errorMessage")
            Result.failure(Exception(errorMessage))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "❌ Timeout en login")
            Result.failure(Exception("Tiempo de espera agotado. Verifica tu conexión."))
        } catch (e: IOException) {
            Log.e(TAG, "❌ IO Error en login", e)
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inesperado en login", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * 🔥 NUEVO: Determinar rol del usuario basado en email
     */
    private fun determineUserRole(email: String): String {
        Log.d(TAG, "🔍 Determinando rol para email: $email")

        val role = when {
            // Emails específicos de administradores
            email.equals("admin@mievento.com", ignoreCase = true) -> "admin"
            email.equals("administrador@mievento.com", ignoreCase = true) -> "admin"

            // Dominios de administradores
            email.endsWith("@mievento.com", ignoreCase = true) -> "admin"
            email.endsWith("@admin.com", ignoreCase = true) -> "admin"

            // Patrones de email de administradores
            email.contains("admin", ignoreCase = true) -> "admin"
            email.contains("administrador", ignoreCase = true) -> "admin"
            email.contains("manager", ignoreCase = true) -> "admin"
            email.contains("organizador", ignoreCase = true) -> "admin"

            // Por defecto, todos los demás son usuarios regulares
            else -> "user"
        }

        Log.d(TAG, "🎭 Rol determinado: $role para $email")
        return role
    }

    /**
     * Registro de usuario
     */
    suspend fun register(registerRequest: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📝 Iniciando registro para: ${registerRequest.email}")

            val response = apiService.register(registerRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Registro exitoso para: ${registerRequest.email}")

                    // Guardar datos básicos si vienen en el registro
                    body.token?.let { tokenManager.saveToken(it) }
                    body.user?.let { user ->
                        tokenManager.saveUserData(user.id, user.email, user.full_name)

                        // Determinar y guardar rol
                        val userRole = determineUserRole(user.email)
                        tokenManager.saveUserRole(userRole)

                        Log.d(TAG, "🎭 Rol determinado en registro: $userRole para ${user.email}")
                    }

                    Result.success(body)
                } else {
                    val error = body?.message ?: body?.error ?: "Error en registro"
                    Log.e(TAG, "❌ Error en registro: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ HTTP Error en registro: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en registro", e)
            Result.failure(e)
        }
    }

    /**
     * Verificar teléfono
     */
    suspend fun verifyPhone(code: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📱 Verificando teléfono con código: $code")

            val verifyRequest = VerifyPhoneRequest(code)
            val response = apiService.verifyPhone(getAuthHeader(), verifyRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Teléfono verificado exitosamente")
                    Result.success(body)
                } else {
                    val error = body?.message ?: "Error en verificación"
                    Log.e(TAG, "❌ Error en verificación: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando teléfono", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener perfil del usuario
     */
    suspend fun getProfile(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👤 Obteniendo perfil del usuario")

            val response = apiService.getProfile(getAuthHeader())

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Perfil obtenido exitosamente")

                    // Actualizar datos locales si vienen
                    body.user?.let { user ->
                        tokenManager.saveUserData(user.id, user.email, user.full_name)

                        // Re-determinar rol si es necesario
                        if (tokenManager.getUserRole() == null) {
                            val userRole = determineUserRole(user.email)
                            tokenManager.saveUserRole(userRole)
                        }
                    }

                    Result.success(body)
                } else {
                    val error = body?.message ?: "Error obteniendo perfil"
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo perfil", e)
            Result.failure(e)
        }
    }

    /**
     * Actualizar perfil
     */
    suspend fun updateProfile(updateRequest: UpdateProfileRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "✏️ Actualizando perfil")

            val response = apiService.updateProfile(getAuthHeader(), updateRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Perfil actualizado exitosamente")

                    // Actualizar datos locales
                    body.user?.let { user ->
                        tokenManager.saveUserData(user.id, user.email, user.full_name)
                    }

                    Result.success(body)
                } else {
                    val error = body?.message ?: "Error actualizando perfil"
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando perfil", e)
            Result.failure(e)
        }
    }

    /**
     * Cambiar contraseña
     */
    suspend fun changePassword(changeRequest: ChangePasswordRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔒 Cambiando contraseña")

            val response = apiService.changePassword(getAuthHeader(), changeRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "✅ Contraseña cambiada exitosamente")
                    Result.success(body)
                } else {
                    val error = body?.message ?: "Error cambiando contraseña"
                    Result.failure(Exception(error))
                }
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cambiando contraseña", e)
            Result.failure(e)
        }
    }

    /**
     * Logout
     */
    fun logout() {
        Log.d(TAG, "🚪 Cerrando sesión")
        tokenManager.clearToken()
    }

    /**
     * Verificar si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Verificar si el usuario es admin
     */
    fun isAdmin(): Boolean {
        return tokenManager.isAdmin()
    }

    /**
     * Verificar si el usuario es regular
     */
    fun isUser(): Boolean {
        return tokenManager.isUser()
    }

    /**
     * Manejar errores HTTP específicos
     */
    private fun handleHttpException(e: HttpException, operation: String): String {
        return when (e.code()) {
            400 -> "Datos inválidos para $operation"
            401 -> "Credenciales inválidas para $operation"
            403 -> "No tienes permisos para $operation"
            404 -> "Recurso no encontrado para $operation"
            409 -> "Conflicto al $operation - Usuario ya existe"
            422 -> "Datos incorrectos para $operation"
            429 -> "Demasiadas solicitudes. Intenta más tarde"
            500 -> "Error del servidor al $operation"
            502, 503 -> "Servidor no disponible. Intenta más tarde"
            else -> "Error de conexión (${e.code()}) al $operation"
        }
    }
}