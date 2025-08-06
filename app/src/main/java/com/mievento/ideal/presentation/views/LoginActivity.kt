package com.mievento.ideal.presentation.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mievento.ideal.data.models.AuthResponse
import com.mievento.ideal.data.repositories.AuthRepository
import com.mievento.ideal.presentation.presenters.LoginPresenter
import com.mievento.ideal.presentation.presenters.LoginView

import com.mievento.ideal.data.models.LoginRequest
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class LoginActivity : AppCompatActivity(), LoginView {

    private lateinit var presenter: LoginPresenter
    private lateinit var tokenManager: TokenManager

    // UI Components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var forgotPasswordText: TextView

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🔐 Iniciando LoginActivity")

        initializeComponents()
        checkIfAlreadyLoggedIn()
        setupUI()
        setupPresenter()
    }

    private fun initializeComponents() {
        tokenManager = TokenManager(this)
    }

    private fun checkIfAlreadyLoggedIn() {
        if (tokenManager.isLoggedIn()) {
            Log.d(TAG, "✅ Usuario ya autenticado, redirigiendo...")
            redirectBasedOnRole()
            return
        }
        Log.d(TAG, "❌ Usuario no autenticado, mostrando login")
    }

    private fun setupUI() {
        // Layout principal
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(32, 0, 32, 0)
            gravity = Gravity.CENTER
        }

        // Logo y título
        val logoLayout = createLogoSection()

        // Formulario de login
        val formLayout = createLoginForm()

        // Botones
        val buttonLayout = createButtonSection()

        // Opciones adicionales
        val optionsLayout = createOptionsSection()

        // ProgressBar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // Agregar todo al layout principal
        mainLayout.addView(logoLayout)
        mainLayout.addView(formLayout)
        mainLayout.addView(progressBar)
        mainLayout.addView(buttonLayout)
        mainLayout.addView(optionsLayout)

        setContentView(ScrollView(this).apply {
            addView(mainLayout)
        })
    }

    private fun createLogoSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 64, 0, 48)

            // Logo placeholder
            val logoText = TextView(this@LoginActivity).apply {
                text = "🎉"
                textSize = 72f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 16)
            }

            // Título de la app
            val appTitle = TextView(this@LoginActivity).apply {
                text = "Mi Evento Ideal"
                textSize = 28f
                setTextColor(Color.parseColor("#1976D2"))
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 8)
            }

            // Subtítulo
            val subtitle = TextView(this@LoginActivity).apply {
                text = "Organiza y participa en eventos increíbles"
                textSize = 16f
                setTextColor(Color.parseColor("#757575"))
                gravity = Gravity.CENTER
            }

            addView(logoText)
            addView(appTitle)
            addView(subtitle)
        }
    }

    private fun createLoginForm(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 32)

            // Campo de email
            val emailLabel = TextView(this@LoginActivity).apply {
                text = "📧 Email"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 0, 0, 8)
            }

            emailEditText = EditText(this@LoginActivity).apply {
                hint = "Ingresa tu email"
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }

            // Campo de contraseña
            val passwordLabel = TextView(this@LoginActivity).apply {
                text = "🔒 Contraseña"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 16, 0, 8)
            }

            passwordEditText = EditText(this@LoginActivity).apply {
                hint = "Ingresa tu contraseña"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }

            addView(emailLabel)
            addView(emailEditText)
            addView(passwordLabel)
            addView(passwordEditText)
        }
    }

    private fun createButtonSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)

            // Botón de login
            loginButton = Button(this@LoginActivity).apply {
                text = "🚀 Iniciar Sesión"
                textSize = 18f
                setBackgroundColor(Color.parseColor("#2196F3"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
                setOnClickListener { attemptLogin() }
            }

            // Espacio
            val spacer = View(this@LoginActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    16
                )
            }

            // Botón de registro
            registerButton = Button(this@LoginActivity).apply {
                text = "📝 Crear Cuenta"
                textSize = 16f
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setPadding(24, 12, 24, 12)
                setOnClickListener { goToRegister() }
            }

            addView(loginButton)
            addView(spacer)
            addView(registerButton)
        }
    }

    private fun createOptionsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 32)

            // Olvidé mi contraseña
            forgotPasswordText = TextView(this@LoginActivity).apply {
                text = "¿Olvidaste tu contraseña?"
                textSize = 14f
                setTextColor(Color.parseColor("#1976D2"))
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 0)
                setOnClickListener {
                    showToast("Funcionalidad próximamente disponible")
                }
            }

            // Información de roles
            val roleInfo = TextView(this@LoginActivity).apply {
                text = "💡 Tip: Los administradores pueden crear eventos\nLos usuarios pueden participar en eventos"
                textSize = 12f
                setTextColor(Color.parseColor("#757575"))
                gravity = Gravity.CENTER
                setPadding(16, 24, 16, 0)
            }

            addView(forgotPasswordText)
            addView(roleInfo)
        }
    }

    private fun setupPresenter() {
        val authRepository = AuthRepository(tokenManager)
        presenter = LoginPresenter(this, authRepository)
    }

    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        Log.d(TAG, "🔐 Intentando login con email: $email")

        // Validaciones básicas
        if (email.isEmpty()) {
            emailEditText.error = "Email requerido"
            emailEditText.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Email inválido"
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Contraseña requerida"
            passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Contraseña debe tener al menos 6 caracteres"
            passwordEditText.requestFocus()
            return
        }

        // Realizar login
        presenter.login(email, password)
    }

    private fun goToRegister() {
        Log.d(TAG, "📝 Navegando a registro")
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    // Redirección basada en rol
    private fun redirectBasedOnRole() {
        val userRole = tokenManager.getUserRole()
        Log.d(TAG, "🎭 Rol del usuario: $userRole")

        val targetActivity = when (userRole) {
            "admin" -> {
                Log.d(TAG, "👑 Redirigiendo a vista de administrador")
                MainActivity::class.java
            }
            "user" -> {
                Log.d(TAG, "👤 Redirigiendo a vista de usuario")
                UserActivity::class.java
            }
            else -> {
                Log.d(TAG, "❓ Rol desconocido, determinando por email...")
                val email = tokenManager.getUserEmail() ?: ""
                val determinedRole = determineUserRole(email)
                tokenManager.saveUserRole(determinedRole)

                if (determinedRole == "admin") MainActivity::class.java
                else UserActivity::class.java
            }
        }

        startActivity(Intent(this, targetActivity))
        finish()
    }

    // Determinar rol del usuario
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

    // Manejo exitoso de login con redirección
    private fun handleLoginSuccess(response: AuthResponse) {
        Log.d(TAG, "✅ Login exitoso: ${response.user?.full_name}")

        val user = response.user
        if (user == null) {
            showError("Error: datos de usuario no válidos")
            return
        }

        // Guardar datos del usuario
        tokenManager.saveToken(response.token ?: "")
        tokenManager.saveUserData(user.id, user.email, user.full_name)

        // Determinar rol y guardarlo
        val userRole = determineUserRole(user.email)
        tokenManager.saveUserRole(userRole)

        // Log información completa del usuario
        tokenManager.logStoredData()

        hideLoading()

        // Mensaje personalizado según el rol
        val welcomeMessage = when (userRole) {
            "admin" -> "👑 Bienvenido Administrador ${user.full_name}"
            else -> "👤 Bienvenido ${user.full_name}"
        }
        showToast(welcomeMessage)

        // Redireccionar según el rol
        val targetActivity = if (userRole == "admin") {
            MainActivity::class.java  // Vista del administrador
        } else {
            UserActivity::class.java  // Vista del usuario regular
        }

        Log.d(TAG, "🔄 Redirigiendo a: ${targetActivity.simpleName} (rol: $userRole)")
        startActivity(Intent(this, targetActivity))
        finish()
    }

    // Implementación de LoginView
    override fun showLoading() {
        Log.d(TAG, "⏳ Mostrando loading...")
        progressBar.visible()
        loginButton.isEnabled = false
        registerButton.isEnabled = false
        loginButton.text = "Iniciando sesión..."
    }

    override fun hideLoading() {
        Log.d(TAG, "✅ Ocultando loading...")
        progressBar.gone()
        loginButton.isEnabled = true
        registerButton.isEnabled = true
        loginButton.text = "🚀 Iniciar Sesión"
    }

    override fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        hideLoading()

        val userFriendlyMessage = when {
            message.contains("Invalid credentials", ignoreCase = true) ->
                "❌ Email o contraseña incorrectos"
            message.contains("User not found", ignoreCase = true) ->
                "❌ Usuario no encontrado. ¿Ya tienes cuenta?"
            message.contains("network", ignoreCase = true) ->
                "🌐 Error de conexión. Verifica tu internet"
            message.contains("timeout", ignoreCase = true) ->
                "⏱️ Tiempo de espera agotado. Intenta nuevamente"
            else -> "❌ Error: $message"
        }

        showToast(userFriendlyMessage)
    }

    override fun onLoginSuccess(response: AuthResponse) {
        Log.d(TAG, "🎉 Login exitoso recibido del presenter")
        handleLoginSuccess(response)
    }

    override fun showMessage(message: String) {
        Log.d(TAG, "💬 Mensaje: $message")
        showToast(message)
    }

    override fun onBackPressed() {
        Log.d(TAG, "🔙 Saliendo de la aplicación")
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("clear_fields", false)) {
            emailEditText.setText("")
            passwordEditText.setText("")
        }
    }
}