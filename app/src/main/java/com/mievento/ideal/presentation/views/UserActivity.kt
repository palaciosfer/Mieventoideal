package com.mievento.ideal.presentation.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.ParticipationRepository
import com.mievento.ideal.presentation.adapters.AvailableEventAdapter
import com.mievento.ideal.presentation.presenters.UserPresenter
import com.mievento.ideal.presentation.presenters.UserView // 🔥 IMPORT CORREGIDO
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class UserActivity : AppCompatActivity(), UserView {

    private lateinit var presenter: UserPresenter
    private lateinit var tokenManager: TokenManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: AvailableEventAdapter

    companion object {
        private const val TAG = "UserActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "👤 Iniciando UserActivity")

        initializeComponents()
        setupUI()
        setupPresenter()

        // Cargar eventos disponibles
        presenter.loadAvailableEvents()
    }

    private fun initializeComponents() {
        tokenManager = TokenManager(this)

        // Verificar que el usuario esté logueado
        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "❌ Usuario no autenticado, regresando a login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Verificar que sea usuario regular
        if (tokenManager.isAdmin()) {
            Log.d(TAG, "⚠️ Usuario administrador intentando acceder a vista de usuario")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
    }

    private fun setupUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
        }

        // Header del usuario
        val headerLayout = createUserHeader()

        // Título
        val titleText = TextView(this).apply {
            text = "🎉 Eventos Disponibles"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#1976D2"))
            setPadding(24, 24, 24, 16)
        }

        // RecyclerView para eventos disponibles
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@UserActivity)
            setPadding(16, 0, 16, 16)
        }

        // Vista vacía
        emptyView = TextView(this).apply {
            text = "📭 No hay eventos disponibles\n\nVuelve pronto para descubrir nuevos eventos"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#757575"))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
            gone()
        }

        // ProgressBar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // Botón para ver mis participaciones
        val myParticipationsButton = Button(this).apply {
            text = "📋 Mis Participaciones"
            setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(24, 12, 24, 12)
            setOnClickListener {
                // TODO: Ir a MyParticipationsActivity
                showToast("Próximamente: Ver mis participaciones")
            }
        }

        // Agregar todo al layout
        mainLayout.addView(headerLayout)
        mainLayout.addView(titleText)
        mainLayout.addView(progressBar)
        mainLayout.addView(recyclerView)
        mainLayout.addView(emptyView)
        mainLayout.addView(myParticipationsButton)

        setContentView(mainLayout)
    }

    private fun createUserHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
            setPadding(24, 16, 24, 16)

            // Info del usuario
            val userInfo = TextView(this@UserActivity).apply {
                text = "👤 ${tokenManager.getUserName() ?: "Usuario"}\n📧 ${tokenManager.getUserEmail() ?: ""}"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // Botón logout
            val logoutButton = Button(this@UserActivity).apply {
                text = "Cerrar Sesión"
                setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                setTextColor(android.graphics.Color.WHITE)
                setOnClickListener { logout() }
            }

            addView(userInfo)
            addView(logoutButton)
        }
    }

    private fun setupPresenter() {
        val participationRepository = ParticipationRepository(tokenManager)
        presenter = UserPresenter(this, participationRepository)
    }

    private fun logout() {
        Log.d(TAG, "🚪 Cerrando sesión de usuario")
        tokenManager.clearToken()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Implementación de UserView
    override fun showLoading() {
        progressBar.visible()
        recyclerView.gone()
        emptyView.gone()
    }

    override fun hideLoading() {
        progressBar.gone()
    }

    override fun showAvailableEvents(events: List<Event>) {
        Log.d(TAG, "📋 Mostrando ${events.size} eventos disponibles")

        if (events.isEmpty()) {
            emptyView.visible()
            recyclerView.gone()
        } else {
            emptyView.gone()
            recyclerView.visible()

            adapter = AvailableEventAdapter(events) { event ->
                presenter.requestParticipation(event.id)
            }
            recyclerView.adapter = adapter
        }
    }

    override fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        showToast("Error: $message")
        hideLoading()
    }

    override fun showMessage(message: String) {
        Log.d(TAG, "💬 Mensaje: $message")
        showToast(message)
    }

    override fun onParticipationRequested() {
        Log.d(TAG, "✅ Participación solicitada exitosamente")
        showMessage("🎉 Solicitud enviada exitosamente")
        // Recargar eventos para actualizar estados
        presenter.loadAvailableEvents()
    }
}