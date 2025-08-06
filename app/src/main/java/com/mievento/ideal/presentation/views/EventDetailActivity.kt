package com.mievento.ideal.presentation.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.EventRepository
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EventDetailActivity : AppCompatActivity() {

    private lateinit var eventRepository: EventRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var ivEventImage: ImageView
    private lateinit var tvEventName: TextView
    private lateinit var tvEventType: TextView
    private lateinit var tvEventDate: TextView
    private lateinit var tvEventTime: TextView
    private lateinit var tvEventLocation: TextView
    private lateinit var tvEventDescription: TextView
    private lateinit var tvEventNotes: TextView
    private lateinit var tvEventBudget: TextView
    private lateinit var tvEventStatus: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var currentEvent: Event? = null
    private var eventId: Int = 0

    companion object {
        private const val TAG = "EventDetailActivity"
        private const val EDIT_EVENT_REQUEST = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        val eventName = intent.getStringExtra("event_name") ?: "Evento"

        Log.d(TAG, "🔍 Iniciando EventDetailActivity para evento: $eventId - $eventName")

        if (eventId == 0) {
            Log.e(TAG, "❌ ID de evento inválido")
            showToast("Error: ID de evento inválido")
            finish()
            return
        }

        val tokenManager = TokenManager(this)
        eventRepository = EventRepository(tokenManager)

        setupUI()
        loadEventDetails()
    }

    private fun setupUI() {
        Log.d(TAG, "🎨 Configurando UI")

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
        }

        // Toolbar
        val toolbar = createToolbar()

        // Progress bar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // Scroll view para el contenido
        scrollView = ScrollView(this).apply {
            gone()
        }

        // Contenido del evento
        val contentLayout = createContentLayout()
        scrollView.addView(contentLayout)

        // Layout principal
        val frameLayout = FrameLayout(this).apply {
            addView(scrollView)
            addView(progressBar, FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ))
        }

        mainLayout.addView(toolbar)
        mainLayout.addView(frameLayout, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        setContentView(mainLayout)

        Log.d(TAG, "✅ UI configurada")
    }

    private fun createToolbar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
            gravity = Gravity.CENTER_VERTICAL

            val btnBack = Button(this@EventDetailActivity).apply {
                text = "← Volver"
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(android.graphics.Color.WHITE)
                textSize = 16f
                setOnClickListener {
                    Log.d(TAG, "← Volver clickeado")
                    finish()
                }
            }

            val title = TextView(this@EventDetailActivity).apply {
                text = "📋 Detalles del Evento"
                textSize = 18f
                setTextColor(android.graphics.Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
            }

            addView(btnBack)
            addView(title)
        }
    }

    private fun createContentLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)

            // Imagen del evento
            ivEventImage = ImageView(this@EventDetailActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300
                ).apply {
                    setMargins(0, 0, 0, 24)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
            }

            // Nombre del evento
            tvEventName = TextView(this@EventDetailActivity).apply {
                textSize = 24f
                setTextColor(android.graphics.Color.parseColor("#212121"))
                setPadding(0, 0, 0, 16)
            }

            // Card de información básica
            val basicInfoCard = createInfoCard()

            // Botones de acción
            val actionButtonsLayout = createActionButtons()

            addView(ivEventImage)
            addView(tvEventName)
            addView(basicInfoCard)
            addView(actionButtonsLayout)
        }
    }

    private fun createInfoCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            val titleInfo = TextView(this@EventDetailActivity).apply {
                text = "📋 Información Básica"
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor("#2196F3"))
                setPadding(0, 0, 0, 16)
            }

            tvEventType = createDetailRow("🎭 Tipo:", "")
            tvEventDate = createDetailRow("📅 Fecha:", "")
            tvEventTime = createDetailRow("🕐 Hora:", "")
            tvEventLocation = createDetailRow("📍 Ubicación:", "")
            tvEventBudget = createDetailRow("💰 Presupuesto:", "")
            tvEventStatus = createDetailRow("📊 Estado:", "")
            tvEventDescription = createDetailRow("📝 Descripción:", "")
            tvEventNotes = createDetailRow("📌 Notas:", "")

            addView(titleInfo)
            addView(tvEventType)
            addView(tvEventDate)
            addView(tvEventTime)
            addView(tvEventLocation)
            addView(tvEventBudget)
            addView(tvEventStatus)
            addView(tvEventDescription)
            addView(tvEventNotes)
        }
    }

    private fun createActionButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            btnEdit = Button(this@EventDetailActivity).apply {
                text = "✏️ Editar"
                setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                setTextColor(android.graphics.Color.WHITE)
                setPadding(24, 16, 24, 16)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener { editEvent() }
            }

            btnDelete = Button(this@EventDetailActivity).apply {
                text = "🗑️ Eliminar"
                setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                setTextColor(android.graphics.Color.WHITE)
                setPadding(24, 16, 24, 16)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener { deleteEvent() }
            }

            addView(btnEdit)
            addView(btnDelete)
        }
    }

    private fun createDetailRow(label: String, value: String): TextView {
        return TextView(this).apply {
            text = "$label $value"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#424242"))
            setPadding(0, 4, 0, 4)
        }
    }

    private fun loadEventDetails() {
        Log.d(TAG, "📋 Cargando detalles del evento: $eventId")
        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = eventRepository.getEventById(eventId)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val event = result.getOrNull()
                        if (event != null) {
                            showEventDetails(event)
                        } else {
                            showError("Evento no encontrado")
                        }
                    } else {
                        showError(result.exceptionOrNull()?.message ?: "Error desconocido")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun showLoading() {
        progressBar.visible()
        scrollView.gone()
    }

    private fun hideLoading() {
        progressBar.gone()
        scrollView.visible()
    }

    private fun showEventDetails(event: Event) {
        Log.d(TAG, "📋 Mostrando detalles del evento: ${event.name}")
        Log.d(TAG, "📷 Imagen del evento: ${event.main_image}")

        currentEvent = event

        tvEventName.text = event.name
        tvEventType.text = "🎭 Tipo: ${event.type.uppercase()}"
        tvEventDate.text = "📅 Fecha: ${formatEventDate(event.event_date)}"
        tvEventTime.text = "🕐 Hora: ${event.event_time}"
        tvEventLocation.text = "📍 Ubicación: ${event.location}"
        tvEventBudget.text = "💰 Presupuesto: $${String.format("%.2f", event.budget)}"
        tvEventStatus.text = "📊 Estado: ${event.status.uppercase()}"
        tvEventDescription.text = "📝 Descripción: ${event.description ?: "Sin descripción"}"
        tvEventNotes.text = "📌 Notas: ${event.notes ?: "Sin notas"}"

        loadEventImage(event)
        hideLoading()
    }

    private fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        hideLoading()
        showToast("Error: $message")
    }

    private fun loadEventImage(event: Event) {
        Log.d(TAG, "🔍 CARGANDO IMAGEN DEL EVENTO EN DETALLE")
        Log.d(TAG, "📷 Main Image URL: '${event.main_image}'")

        if (!event.main_image.isNullOrBlank()) {
            Log.d(TAG, "✅ Intentando cargar imagen: ${event.main_image}")

            try {
                Glide.with(this)
                    .load(event.main_image)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(ivEventImage)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando imagen", e)
                ivEventImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            Log.d(TAG, "🖼️ Sin imagen, usando default")
            ivEventImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun editEvent() {
        Log.d(TAG, "✏️ Editando evento: $eventId")
        val intent = Intent(this, CreateEventActivity::class.java)
        intent.putExtra("edit_mode", true)
        intent.putExtra("event_id", eventId)
        startActivityForResult(intent, EDIT_EVENT_REQUEST)
    }

    private fun deleteEvent() {
        Log.d(TAG, "🗑️ Solicitando eliminar evento: $eventId")

        AlertDialog.Builder(this)
            .setTitle("Eliminar Evento")
            .setMessage("¿Estás seguro que deseas eliminar este evento?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                // TODO: Implementar eliminación
                showToast("Eliminación - Próximamente")
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun formatEventDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateObj = inputFormat.parse(date)
            outputFormat.format(dateObj)
        } catch (e: Exception) {
            date
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_EVENT_REQUEST && resultCode == RESULT_OK) {
            loadEventDetails()
        }
    }
}