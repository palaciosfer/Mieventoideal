package com.mievento.ideal.presentation.presenters

import android.util.Log
import com.mievento.ideal.data.repositories.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val view: LoginView,
    private val authRepository: AuthRepository
) {

    companion object {
        private const val TAG = "LoginPresenter"
    }

    fun login(email: String, password: String) {
        Log.d(TAG, "🔐 Iniciando login para: $email")
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.login(email, password)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response != null) {
                            Log.d(TAG, "✅ Login exitoso")
                            view.onLoginSuccess(response)
                        } else {
                            Log.e(TAG, "❌ Respuesta nula")
                            view.showError("Error: respuesta inválida del servidor")
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error de login"
                        Log.e(TAG, "❌ Error en login: $error")
                        view.showError(error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en login", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }
}