package com.mievento.ideal.presentation.presenters

import android.util.Log
import com.mievento.ideal.data.models.RegisterRequest
import com.mievento.ideal.data.repositories.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterPresenter(
    private val view: RegisterView,
    private val authRepository: AuthRepository
) {

    companion object {
        private const val TAG = "RegisterPresenter"
    }

    /**
     * 🔥 CORREGIDO: Crear RegisterRequest antes de llamar al repository
     */
    fun register(email: String, password: String, fullName: String, phone: String) {
        Log.d(TAG, "📝 Iniciando registro para: $email")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 🔥 CREAR EL OBJETO RegisterRequest
                val registerRequest = RegisterRequest(
                    email = email,
                    password = password,
                    full_name = fullName,
                    phone = phone
                )

                // 🔥 PASAR EL OBJETO COMPLETO AL REPOSITORY
                val result = authRepository.register(registerRequest)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response != null) {
                            Log.d(TAG, "✅ Registro exitoso para: $email")
                            view.onRegistrationSuccess(response)
                        } else {
                            Log.e(TAG, "❌ Respuesta nula en registro")
                            view.showError("Error: respuesta inválida del servidor")
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error en registro"
                        Log.e(TAG, "❌ Error en registro: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en registro", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    /**
     * Validar datos antes de registro
     */
    fun validateAndRegister(email: String, password: String, confirmPassword: String, fullName: String, phone: String) {
        Log.d(TAG, "🔍 Validando datos de registro...")

        // Validaciones básicas
        when {
            email.isBlank() -> {
                view.showError("El email es requerido")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                view.showError("Formato de email inválido")
                return
            }
            fullName.isBlank() -> {
                view.showError("El nombre completo es requerido")
                return
            }
            fullName.length < 2 -> {
                view.showError("El nombre debe tener al menos 2 caracteres")
                return
            }
            phone.isBlank() -> {
                view.showError("El teléfono es requerido")
                return
            }
            phone.length < 10 -> {
                view.showError("El teléfono debe tener al menos 10 dígitos")
                return
            }
            password.isBlank() -> {
                view.showError("La contraseña es requerida")
                return
            }
            password.length < 6 -> {
                view.showError("La contraseña debe tener al menos 6 caracteres")
                return
            }
            confirmPassword.isBlank() -> {
                view.showError("Confirma tu contraseña")
                return
            }
            password != confirmPassword -> {
                view.showError("Las contraseñas no coinciden")
                return
            }
        }

        // Si todas las validaciones pasan, proceder con el registro
        Log.d(TAG, "✅ Validaciones exitosas, procediendo con registro")
        register(email, password, fullName, phone)
    }

    /**
     * Verificar si el email ya está en uso (opcional)
     */
    fun checkEmailAvailability(email: String) {
        // TODO: Implementar verificación de email disponible
        // Por ahora solo validar formato
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Formato de email inválido")
        }
    }

    /**
     * Determinar rol del usuario basado en email (para mostrar info)
     */
    fun determineUserRole(email: String): String {
        return when {
            email.contains("admin", ignoreCase = true) -> "admin"
            email.contains("administrador", ignoreCase = true) -> "admin"
            email.contains("manager", ignoreCase = true) -> "admin"
            email.endsWith("@mievento.com", ignoreCase = true) -> "admin"
            email.endsWith("@admin.com", ignoreCase = true) -> "admin"
            else -> "user"
        }
    }

    /**
     * Mostrar información sobre roles al usuario
     */
    fun showRoleInfo(email: String) {
        val role = determineUserRole(email)
        val roleMessage = when (role) {
            "admin" -> "🔥 Este email será registrado como Administrador - Podrás crear y gestionar eventos"
            else -> "👤 Este email será registrado como Usuario - Podrás participar en eventos"
        }
        view.showMessage(roleMessage)
    }
}