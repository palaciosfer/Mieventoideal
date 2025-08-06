package com.mievento.ideal.presentation.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.EventRepository
import com.mievento.ideal.presentation.adapters.EventAdapter
import com.mievento.ideal.presentation.presenters.MainPresenter
import com.mievento.ideal.presentation.presenters.MainView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: MainPresenter
    private lateinit var eventAdapter: EventAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var fabCreateEvent: FloatingActionButton

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_EVENT_REQUEST = 100
        private const val EDIT_EVENT_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 Iniciando MainActivity - Panel Administrador")

        val tokenManager = TokenManager(this)
        val eventRepository = EventRepository(tokenManager)
        presenter = MainPresenter(this, eventRepository)

        setupUI()
        setupEventAdapter()

        presenter.loadEvents()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📱 MainActivity onResume - Recargando eventos")
        presenter.loadEvents()
    }

    private fun setupUI() {
        // Crear el layout raíz principal
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Toolbar del administrador
        val toolbar = createToolbar()

        // Información para administrador
        val adminInfo = createAdminInfo()

        // RecyclerView para eventos
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(0, 16, 0, 100) // Espacio para el FAB
        }

        // Vista cuando no hay eventos
        emptyView = TextView(this).apply {
            text = "📋 No hay eventos creados\n\nPresiona el botón + para crear tu primer evento"
            textSize = 16f
            setTextColor(Color.parseColor("#757575"))
            gravity = Gravity.CENTER
            setPadding(32, 64, 32, 64)
            gone()
        }

        // ProgressBar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // Botón flotante para crear evento
        fabCreateEvent = createFloatingActionButton()

        // 🔥 CORREGIDO: Agregar vistas en el orden correcto
        mainLayout.addView(toolbar)
        mainLayout.addView(adminInfo)
        mainLayout.addView(progressBar)
        mainLayout.addView(recyclerView)
        mainLayout.addView(emptyView)

        // Agregar mainLayout al scrollView
        scrollView.addView(mainLayout)

        // Agregar scrollView al rootLayout
        rootLayout.addView(scrollView)

        // Agregar FAB al rootLayout (encima del scrollView)
        rootLayout.addView(fabCreateEvent)

        // 🔥 IMPORTANTE: Solo llamar setContentView UNA vez
        setContentView(rootLayout)
    }

    private fun createToolbar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 48, 32, 24)
            setBackgroundColor(Color.parseColor("#2196F3"))
            gravity = Gravity.CENTER_VERTICAL

            val icon = TextView(this@MainActivity).apply {
                text = "⚙️"
                textSize = 24f
                setPadding(0, 0, 16, 0)
            }

            val title = TextView(this@MainActivity).apply {
                text = "Panel Administrador"
                textSize = 20f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnProfile = Button(this@MainActivity).apply {
                text = "👤"
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.WHITE)
                textSize = 18f
                setPadding(16, 8, 16, 8)
                setOnClickListener { showProfileMenu() }
            }

            addView(icon)
            addView(title)
            addView(btnProfile)
        }
    }

    private fun createAdminInfo(): TextView {
        return TextView(this).apply {
            text = "Gestiona tus eventos y participaciones desde aquí\n" +
                    "• Crea eventos y publícalos para que los usuarios los vean\n" +
                    "• Administra participantes y solicitudes\n" +
                    "• Controla el estado de cada evento"
            textSize = 14f
            setTextColor(Color.parseColor("#4CAF50"))
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.parseColor("#E8F5E8"))
        }
    }

    private fun createFloatingActionButton(): FloatingActionButton {
        return FloatingActionButton(this).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setBackgroundColor(Color.parseColor("#4CAF50"))

            // 🔥 CORREGIDO: Configurar layoutParams correctamente
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, 32, 32)
            }

            setOnClickListener { createNewEvent() }
        }
    }

    private fun setupEventAdapter() {
        eventAdapter = EventAdapter(
            events = emptyList(),
            onEventClick = { event ->
                Log.d(TAG, "📱 Click en evento: ${event.id} - ${event.name}")
                showEventDetails(event)
            },
            onEventLongClick = { event ->
                Log.d(TAG, "📱 Long click en evento: ${event.id} - ${event.name}")
                showEventContextMenu(event)
            },
            onEventEdit = { event ->
                Log.d(TAG, "✏️ Editar evento: ${event.id} - ${event.name}")
                editEvent(event)
            },
            onEventParticipants = { event ->
                Log.d(TAG, "👥 Ver participantes de evento: ${event.id} - ${event.name}")
                viewEventParticipants(event)
            },
            onEventStatusChange = { event, newStatus ->
                Log.d(TAG, "🔄 Cambiar estado de evento: ${event.id} de '${event.status}' a '$newStatus'")
                changeEventStatus(event, newStatus)
            }
        )

        recyclerView.adapter = eventAdapter
    }

    // 🔥 NUEVO: Cambiar estado del evento con confirmación
    private fun changeEventStatus(event: Event, newStatus: String) {
        val (actionText, description, confirmText) = when (newStatus) {
            "activo" -> Triple(
                "Publicar Evento",
                "El evento '${event.name}' será visible para todos los usuarios y podrán solicitar participar.",
                "🚀 Sí, Publicar"
            )
            "planeacion" -> Triple(
                "Pausar Evento",
                "El evento '${event.name}' se ocultará de los usuarios y no podrán solicitar participar.",
                "⏸️ Sí, Pausar"
            )
            "finalizado" -> Triple(
                "Finalizar Evento",
                "El evento '${event.name}' se marcará como finalizado.",
                "✅ Sí, Finalizar"
            )
            "cancelado" -> Triple(
                "Cancelar Evento",
                "El evento '${event.name}' se marcará como cancelado.",
                "❌ Sí, Cancelar"
            )
            else -> Triple(
                "Cambiar Estado",
                "¿Deseas cambiar el estado del evento '${event.name}'?",
                "Sí, Cambiar"
            )
        }

        AlertDialog.Builder(this)
            .setTitle(actionText)
            .setMessage(description)
            .setPositiveButton(confirmText) { _, _ ->
                Log.d(TAG, "✅ Confirmado cambio de estado para evento ${event.id} a '$newStatus'")
                presenter.updateEventStatus(event.id, newStatus)
            }
            .setNegativeButton("❌ Cancelar") { dialog, _ ->
                Log.d(TAG, "❌ Cancelado cambio de estado para evento ${event.id}")
                dialog.dismiss()
            }
            .setIcon(when (newStatus) {
                "activo" -> android.R.drawable.ic_media_play
                "planeacion" -> android.R.drawable.ic_media_pause
                "finalizado" -> android.R.drawable.ic_menu_save
                "cancelado" -> android.R.drawable.ic_delete
                else -> android.R.drawable.ic_dialog_info
            })
            .show()
    }

    private fun editEvent(event: Event) {
        val intent = Intent(this, CreateEventActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("event_id", event.id)
            putExtra("event_name", event.name)
            putExtra("event_type", event.type)
            putExtra("event_date", event.event_date)
            putExtra("event_time", event.event_time)
            putExtra("event_location", event.location)
            putExtra("event_description", event.description)
            putExtra("event_notes", event.notes)
            putExtra("event_budget", event.budget)
            putExtra("event_status", event.status)
            putExtra("event_image", event.main_image)
        }
        startActivityForResult(intent, EDIT_EVENT_REQUEST)
    }

    private fun viewEventParticipants(event: Event) {
        val intent = Intent(this, EventParticipantsActivity::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("event_name", event.name)
            putExtra("event_status", event.status)
        }
        startActivity(intent)
    }

    private fun showEventDetails(event: Event) {
        val statusText = when (event.status) {
            "planeacion" -> "📋 En Planeación (No visible para usuarios)"
            "activo" -> "🎯 Activo (Visible para usuarios)"
            "finalizado" -> "✅ Finalizado"
            "cancelado" -> "❌ Cancelado"
            else -> event.status
        }

        val message = """
            📅 Fecha: ${event.event_date} a las ${event.event_time}
            📍 Ubicación: ${event.location}
            🎭 Tipo: ${event.type.capitalize()}
            💰 Presupuesto: $${event.budget}
            📊 Estado: $statusText
            
            ${if (!event.description.isNullOrBlank()) "📝 Descripción:\n${event.description}\n\n" else ""}
            ${if (!event.notes.isNullOrBlank()) "📌 Notas:\n${event.notes}" else ""}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📋 ${event.name}")
            .setMessage(message)
            .setPositiveButton("✏️ Editar") { _, _ -> editEvent(event) }
            .setNeutralButton("👥 Participantes") { _, _ -> viewEventParticipants(event) }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun showEventContextMenu(event: Event) {
        val options = arrayOf(
            "✏️ Editar Evento",
            "👥 Ver Participantes",
            "🔄 Cambiar Estado",
            "🗑️ Eliminar Evento"
        )

        AlertDialog.Builder(this)
            .setTitle(" ${event.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editEvent(event)
                    1 -> viewEventParticipants(event)
                    2 -> showStatusChangeMenu(event)
                    3 -> confirmDeleteEvent(event)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showStatusChangeMenu(event: Event) {
        val statusOptions = arrayOf(
            "📋 En Planeación",
            "🎯 Activo (Visible)",
            "✅ Finalizado",
            "❌ Cancelado"
        )

        val statusValues = arrayOf("planeacion", "activo", "finalizado", "cancelado")

        AlertDialog.Builder(this)
            .setTitle("Cambiar Estado del Evento")
            .setItems(statusOptions) { _, which ->
                changeEventStatus(event, statusValues[which])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun confirmDeleteEvent(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Eliminar Evento")
            .setMessage("¿Estás seguro de eliminar el evento '${event.name}'?\n\n⚠️ Esta acción no se puede deshacer.")
            .setPositiveButton("🗑️ Sí, Eliminar") { _, _ ->
                presenter.deleteEvent(event.id)
            }
            .setNegativeButton("❌ Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun createNewEvent() {
        Log.d(TAG, "➕ Crear nuevo evento")
        val intent = Intent(this, CreateEventActivity::class.java)
        startActivityForResult(intent, CREATE_EVENT_REQUEST)
    }

    private fun showProfileMenu() {
        val options = arrayOf(
            " Ver Perfil",
            "🚪 Cerrar Sesión"
        )

        AlertDialog.Builder(this)
            .setTitle("👤 Perfil de Administrador")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showToast("👤 Perfil - Próximamente")
                    1 -> confirmLogout()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("🚪 Cerrar Sesión")
            .setMessage("¿Estás seguro de cerrar la sesión de administrador?")
            .setPositiveButton("🚪 Sí, Cerrar") { _, _ ->
                logout()
            }
            .setNegativeButton("❌ Cancelar", null)
            .show()
    }

    private fun logout() {
        val tokenManager = TokenManager(this)
        tokenManager.clearToken()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CREATE_EVENT_REQUEST, EDIT_EVENT_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "✅ Evento guardado exitosamente - Recargando lista")
                    presenter.loadEvents()
                }
            }
        }
    }

    // Implementación de MainView
    override fun showLoading() {
        Log.d(TAG, "⏳ Mostrando loading")
        progressBar.visible()
        recyclerView.gone()
        emptyView.gone()
    }

    override fun hideLoading() {
        Log.d(TAG, "✅ Ocultando loading")
        progressBar.gone()
    }

    override fun showEvents(events: List<Event>) {
        Log.d(TAG, "📋 Mostrando ${events.size} eventos")

        hideLoading()

        if (events.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()

            // Log detallado de cada evento
            events.forEachIndexed { index, event ->
                Log.d(TAG, "   📄 Evento $index:")
                Log.d(TAG, "      ID: ${event.id}")
                Log.d(TAG, "      Nombre: ${event.name}")
                Log.d(TAG, "      Estado: ${event.status}")
                Log.d(TAG, "      Imagen: ${event.main_image}")
            }

            eventAdapter.updateEvents(events)
        }
    }

    override fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        hideLoading()
        showToast("❌ $message")
    }

    override fun showMessage(message: String) {
        Log.d(TAG, "💬 Mensaje: $message")
        showToast("✅ $message")
    }

    override fun onEventDeleted() {
        Log.d(TAG, "🗑️ Evento eliminado - Recargando lista")
        showMessage("Evento eliminado exitosamente")
        presenter.loadEvents()
    }

    override fun onEventStatusUpdated() {
        Log.d(TAG, "🔄 Estado de evento actualizado - Recargando lista")
        showMessage("Estado del evento actualizado exitosamente")
        presenter.loadEvents()
    }
}