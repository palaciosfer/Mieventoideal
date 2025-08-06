package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.utils.toString
import com.mievento.ideal.utils.toDate
import java.text.NumberFormat
import java.util.*

class EventAdapter(
    private var events: List<Event>,
    private val onEventClick: (Event) -> Unit,
    private val onEventLongClick: (Event) -> Unit,
    private val onEventEdit: (Event) -> Unit, // 🔥 NUEVO
    private val onEventParticipants: (Event) -> Unit, // 🔥 NUEVO
    private val onEventStatusChange: (Event, String) -> Unit // 🔥 NUEVO
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    companion object {
        private const val TAG = "EventAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            setBackgroundColor(Color.WHITE)

            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(16, 8, 16, 8)
            layoutParams = params
        }

        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        Log.d(TAG, "📋 Actualizando adapter con ${newEvents.size} eventos")
        newEvents.forEachIndexed { index, event ->
            Log.d(TAG, "   Evento $index: ${event.name} - Estado: ${event.status} - Imagen: ${event.main_image}")
        }
        events = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var ivEventImage: ImageView
        private lateinit var tvName: TextView
        private lateinit var tvType: TextView
        private lateinit var tvDate: TextView
        private lateinit var tvLocation: TextView
        private lateinit var tvDescription: TextView
        private lateinit var tvBudget: TextView
        private lateinit var tvStatus: TextView
        private lateinit var buttonLayout: LinearLayout
        private lateinit var btnEdit: Button
        private lateinit var btnParticipants: Button
        private lateinit var btnStatus: Button

        init {
            setupViews()
        }

        private fun setupViews() {
            // Imagen del evento
            ivEventImage = ImageView(layout.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    200
                )
                setBackgroundColor(Color.parseColor("#E0E0E0"))
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Header con nombre y tipo
            val headerLayout = LinearLayout(layout.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 8)
            }

            tvName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            tvType = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(12, 6, 12, 6)
                setBackgroundColor(Color.parseColor("#2196F3"))
            }

            headerLayout.addView(tvName)
            headerLayout.addView(tvType)

            tvDate = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#4CAF50"))
                setPadding(0, 4, 0, 4)
            }

            tvLocation = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, 4, 0, 4)
            }

            tvDescription = TextView(layout.context).apply {
                textSize = 13f
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, 8, 0, 8)
                maxLines = 2
            }

            tvBudget = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#FF9800"))
                setPadding(0, 4, 0, 8)
            }

            tvStatus = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER
            }

            // 🔥 NUEVO: Layout de botones de administración
            buttonLayout = LinearLayout(layout.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 0)
                gravity = Gravity.CENTER
            }

            btnEdit = Button(layout.context).apply {
                text = "✏️ EDITAR"
                setBackgroundColor(Color.parseColor("#2196F3"))
                setTextColor(Color.WHITE)
                setPadding(12, 8, 12, 8)
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
            }

            btnParticipants = Button(layout.context).apply {
                text = "👥 PARTICIPANTES"
                setBackgroundColor(Color.parseColor("#FF9800"))
                setTextColor(Color.WHITE)
                setPadding(12, 8, 12, 8)
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
            }

            // 🔥 NUEVO: Botón dinámico para cambiar estado
            btnStatus = Button(layout.context).apply {
                setPadding(12, 8, 12, 8)
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
            }

            buttonLayout.addView(btnEdit)
            buttonLayout.addView(btnParticipants)
            buttonLayout.addView(btnStatus)

            // Agregar todas las vistas al layout principal
            layout.addView(ivEventImage)
            layout.addView(headerLayout)
            layout.addView(tvDate)
            layout.addView(tvLocation)
            layout.addView(tvDescription)
            layout.addView(tvBudget)
            layout.addView(tvStatus)
            layout.addView(buttonLayout) // 🔥 BOTONES DE ADMINISTRACIÓN
        }

        fun bind(event: Event) {
            Log.d(TAG, "🎯 Binding evento: ${event.id} - ${event.name} - Estado: ${event.status}")

            tvName.text = event.name
            tvType.text = event.type.uppercase()

            val date = event.event_date.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: event.event_date
            tvDate.text = "📅 $date a las ${event.event_time}"

            tvLocation.text = "📍 ${event.location}"

            if (!event.description.isNullOrBlank()) {
                tvDescription.text = event.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            val budget = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(event.budget)
            tvBudget.text = "💰 Presupuesto: $budget"

            tvStatus.text = when (event.status) {
                "planeacion" -> "📋 En Planeación"
                "activo" -> "🎯 Activo (Visible)"
                "finalizado" -> "✅ Finalizado"
                "cancelado" -> "❌ Cancelado"
                else -> event.status.capitalize()
            }
            tvStatus.setBackgroundColor(getStatusColor(event.status))

            // 🔥 CONFIGURAR BOTONES SEGÚN ESTADO
            setupEventButtons(event)

            // 🔥 CARGAR IMAGEN CON LOGS DETALLADOS
            loadEventImage(event)

            layout.setOnClickListener { onEventClick(event) }
            layout.setOnLongClickListener {
                onEventLongClick(event)
                true
            }
        }

        // 🔥 NUEVO: Configurar botones según el estado del evento
        private fun setupEventButtons(event: Event) {
            Log.d(TAG, "⚙️ Configurando botones para evento ${event.id} con estado: ${event.status}")

            // Botón EDITAR - Siempre disponible
            btnEdit.setOnClickListener {
                Log.d(TAG, "✏️ Click en EDITAR para evento: ${event.id}")
                onEventEdit(event)
            }

            // Botón PARTICIPANTES - Siempre disponible
            btnParticipants.setOnClickListener {
                Log.d(TAG, "👥 Click en PARTICIPANTES para evento: ${event.id}")
                onEventParticipants(event)
            }

            // 🔥 BOTÓN DE ESTADO - Cambia según el estado actual
            when (event.status) {
                "planeacion" -> {
                    btnStatus.text = "🚀 PUBLICAR"
                    btnStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                    btnStatus.setTextColor(Color.WHITE)
                    btnStatus.setOnClickListener {
                        Log.d(TAG, "🚀 Click en PUBLICAR para evento: ${event.id}")
                        onEventStatusChange(event, "activo")
                    }
                }
                "activo" -> {
                    btnStatus.text = "⏸️ PAUSAR"
                    btnStatus.setBackgroundColor(Color.parseColor("#FF5722"))
                    btnStatus.setTextColor(Color.WHITE)
                    btnStatus.setOnClickListener {
                        Log.d(TAG, "⏸️ Click en PAUSAR para evento: ${event.id}")
                        onEventStatusChange(event, "planeacion")
                    }
                }
                "finalizado" -> {
                    btnStatus.text = "🔄 REACTIVAR"
                    btnStatus.setBackgroundColor(Color.parseColor("#2196F3"))
                    btnStatus.setTextColor(Color.WHITE)
                    btnStatus.setOnClickListener {
                        Log.d(TAG, "🔄 Click en REACTIVAR para evento: ${event.id}")
                        onEventStatusChange(event, "activo")
                    }
                }
                "cancelado" -> {
                    btnStatus.text = "🔄 RESTAURAR"
                    btnStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                    btnStatus.setTextColor(Color.WHITE)
                    btnStatus.setOnClickListener {
                        Log.d(TAG, "🔄 Click en RESTAURAR para evento: ${event.id}")
                        onEventStatusChange(event, "planeacion")
                    }
                }
                else -> {
                    btnStatus.text = "❓ ESTADO"
                    btnStatus.setBackgroundColor(Color.parseColor("#757575"))
                    btnStatus.setTextColor(Color.WHITE)
                    btnStatus.setOnClickListener {
                        Log.d(TAG, "❓ Click en estado desconocido para evento: ${event.id}")
                        onEventStatusChange(event, "planeacion")
                    }
                }
            }
        }

        private fun loadEventImage(event: Event) {
            Log.d(TAG, "🔍 ===== INICIANDO CARGA DE IMAGEN =====")
            Log.d(TAG, "📷 Event ID: ${event.id}")
            Log.d(TAG, "📷 Event Name: ${event.name}")
            Log.d(TAG, "📷 Main Image URL: '${event.main_image}'")
            Log.d(TAG, "📷 URL is null: ${event.main_image == null}")
            Log.d(TAG, "📷 URL is blank: ${event.main_image?.isBlank()}")
            Log.d(TAG, "📷 URL is null or blank: ${event.main_image.isNullOrBlank()}")

            if (!event.main_image.isNullOrBlank()) {
                Log.d(TAG, "✅ URL no está vacía, procediendo a cargar")
                Log.d(TAG, "🔗 URL completa: ${event.main_image}")

                // Verificar que la URL sea válida
                if (!event.main_image.startsWith("http")) {
                    Log.e(TAG, "❌ URL INVÁLIDA (no empieza con http): ${event.main_image}")
                    ivEventImage.setImageResource(getDefaultImageForEventType(event.type))
                    return
                }

                Log.d(TAG, "🚀 Iniciando carga con Glide...")

                try {
                    Glide.with(layout.context)
                        .load(event.main_image)
                        .apply(
                            RequestOptions()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_close_clear_cancel)
                                .transform(RoundedCorners(16))
                                .timeout(15000) // 15 segundos timeout
                        )
                        .into(ivEventImage)

                    Log.d(TAG, "✅ Glide.into() ejecutado correctamente para: ${event.main_image}")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al configurar Glide: ${e.message}")
                    Log.e(TAG, "❌ Exception: ${e.javaClass.simpleName}")
                    ivEventImage.setImageResource(getDefaultImageForEventType(event.type))
                }

            } else {
                Log.d(TAG, "🖼️ Sin imagen disponible para evento ${event.id}")
                Log.d(TAG, "🖼️ Usando imagen por defecto para tipo: ${event.type}")
                ivEventImage.setImageResource(getDefaultImageForEventType(event.type))
            }

            Log.d(TAG, "🔍 ===== FIN CARGA DE IMAGEN =====")
        }

        private fun getDefaultImageForEventType(type: String): Int {
            return when (type.lowercase()) {
                "boda" -> android.R.drawable.ic_menu_my_calendar
                "fiesta" -> android.R.drawable.ic_media_play
                "conferencia" -> android.R.drawable.ic_menu_info_details
                "taller" -> android.R.drawable.ic_menu_edit
                else -> android.R.drawable.ic_menu_gallery
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "planeacion" -> Color.parseColor("#FF9800") // Naranja
                "activo" -> Color.parseColor("#4CAF50") // Verde
                "finalizado" -> Color.parseColor("#2196F3") // Azul
                "cancelado" -> Color.parseColor("#F44336") // Rojo
                else -> Color.parseColor("#757575") // Gris
            }
        }
    }
}