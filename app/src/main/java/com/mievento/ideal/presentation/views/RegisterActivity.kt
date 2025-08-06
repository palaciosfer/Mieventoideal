package com.mievento.ideal.presentation.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mievento.ideal.data.models.AuthResponse
import com.mievento.ideal.data.repositories.AuthRepository
import com.mievento.ideal.presentation.presenters.RegisterPresenter
import com.mievento.ideal.presentation.presenters.RegisterView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class RegisterActivity : AppCompatActivity(), RegisterView {

    private lateinit var presenter: RegisterPresenter
    private lateinit var tokenManager: TokenManager

    // UI Components
    private lateinit var emailEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginButton: Button
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "📝 Iniciando RegisterActivity")

        initializeComponents()
        setupUI()
        setupPresenter()
    }

    private fun initializeComponents() {
        tokenManager = TokenManager(this)
    }

    private fun setupUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(32, 32, 32, 32)
        }

        // Título
        val titleText = TextView(this).apply {
            text = "📝 Crear Cuenta"
            textSize = 28f
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        // Formulario
        val formLayout = createRegistrationForm()

        // ProgressBar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // Botones
        val buttonLayout = createButtonSection()

        mainLayout.addView(titleText)
        mainLayout.addView(formLayout)
        mainLayout.addView(progressBar)
        mainLayout.addView(buttonLayout)

        setContentView(ScrollView(this).apply {
            addView(mainLayout)
        })
    }

    private fun createRegistrationForm(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)

            // Email
            addView(TextView(this@RegisterActivity).apply {
                text = "📧 Email"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 0, 0, 8)
            })

            emailEditText = EditText(this@RegisterActivity).apply {
                hint = "tu@email.com"
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }
            addView(emailEditText)

            // Nombre completo
            addView(TextView(this@RegisterActivity).apply {
                text = "👤 Nombre Completo"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 16, 0, 8)
            })

            fullNameEditText = EditText(this@RegisterActivity).apply {
                hint = "Tu nombre completo"
                inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }
            addView(fullNameEditText)

            // Teléfono
            addView(TextView(this@RegisterActivity).apply {
                text = "📱 Teléfono"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 16, 0, 8)
            })

            phoneEditText = EditText(this@RegisterActivity).apply {
                hint = "Tu número de teléfono"
                inputType = InputType.TYPE_CLASS_PHONE
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }
            addView(phoneEditText)

            // Contraseña
            addView(TextView(this@RegisterActivity).apply {
                text = "🔒 Contraseña"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 16, 0, 8)
            })

            passwordEditText = EditText(this@RegisterActivity).apply {
                hint = "Mínimo 6 caracteres"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }
            addView(passwordEditText)

            // Confirmar contraseña
            addView(TextView(this@RegisterActivity).apply {
                text = "🔒 Confirmar Contraseña"
                textSize = 16f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 16, 0, 8)
            })

            confirmPasswordEditText = EditText(this@RegisterActivity).apply {
                hint = "Repite tu contraseña"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setBackgroundColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 16f
            }
            addView(confirmPasswordEditText)
        }
    }

    private fun createButtonSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)

            registerButton = Button(this@RegisterActivity).apply {
                text = "🚀 Crear Cuenta"
                textSize = 18f
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
                setOnClickListener { attemptRegistration() }
            }

            backToLoginButton = Button(this@RegisterActivity).apply {
                text = "🔙 Volver al Login"
                textSize = 16f
                setBackgroundColor(Color.parseColor("#757575"))
                setTextColor(Color.WHITE)
                setPadding(24, 12, 24, 12)
                setOnClickListener { goBackToLogin() }
            }

            addView(registerButton)
            addView(backToLoginButton)
        }
    }

    private fun setupPresenter() {
        val authRepository = AuthRepository(tokenManager)
        presenter = RegisterPresenter(this, authRepository)
    }

    private fun attemptRegistration() {
        val email = emailEditText.text.toString().trim()
        val fullName = fullNameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        Log.d(TAG, "📝 Intentando registro con email: $email")
        presenter.validateAndRegister(email, password, confirmPassword, fullName, phone)
    }

    private fun goBackToLogin() {
        Log.d(TAG, "🔙 Volviendo al login")
        finish()
    }

    // Implementación de RegisterView
    override fun showLoading() {
        Log.d(TAG, "⏳ Mostrando loading...")
        progressBar.visible()
        registerButton.isEnabled = false
        registerButton.text = "Creando cuenta..."
    }

    override fun hideLoading() {
        Log.d(TAG, "✅ Ocultando loading...")
        progressBar.gone()
        registerButton.isEnabled = true
        registerButton.text = "🚀 Crear Cuenta"
    }

    override fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        hideLoading()
        showToast(message)
    }

    override fun showMessage(message: String) {
        Log.d(TAG, "💬 Mensaje: $message")
        showToast(message)
    }

    override fun onRegistrationSuccess(response: AuthResponse) {
        Log.d(TAG, "🎉 Registro exitoso")
        hideLoading()
        showToast("✅ Cuenta creada exitosamente")

        // Ir al login
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("clear_fields", true)
        startActivity(intent)
        finish()
    }
}