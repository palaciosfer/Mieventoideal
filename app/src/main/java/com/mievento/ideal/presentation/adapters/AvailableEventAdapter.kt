package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

class AvailableEventAdapter(
    private var events: List<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<AvailableEventAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var ivEventImage: ImageView
        private lateinit var tvName: TextView
        private lateinit var tvType: TextView
        private lateinit var tvDate: TextView
        private lateinit var tvLocation: TextView
        private lateinit var tvDescription: TextView
        private lateinit var tvOrganizer: TextView
        private lateinit var participateButton: TextView

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
                setBackgroundColor(Color.parseColor("#4CAF50"))
            }

            headerLayout.addView(tvName)
            headerLayout.addView(tvType)

            tvDate = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#2196F3"))
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

            tvOrganizer = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.parseColor("#FF9800"))
                setPadding(0, 4, 0, 8)
            }

            participateButton = TextView(layout.context).apply {
                text = "🎯 ¡Solicitar Participación!"
                textSize = 14f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setPadding(32, 16, 32, 16)
                gravity = Gravity.CENTER
            }

            layout.addView(ivEventImage)
            layout.addView(headerLayout)
            layout.addView(tvDate)
            layout.addView(tvLocation)
            layout.addView(tvDescription)
            layout.addView(tvOrganizer)
            layout.addView(participateButton)
        }

        fun bind(event: Event) {
            tvName.text = event.name
            tvType.text = event.type.uppercase()

            val date = try {
                event.event_date.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: event.event_date
            } catch (e: Exception) {
                event.event_date
            }
            tvDate.text = "📅 $date a las ${event.event_time}"

            tvLocation.text = "📍 ${event.location}"

            if (!event.description.isNullOrBlank()) {
                tvDescription.text = event.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // Mostrar organizador (admin_name del backend)
            tvOrganizer.text = "👤 Organizado por: ${event.admin_name ?: "Administrador"}"

            // 🔥 CARGAR IMAGEN REAL CON GLIDE
            loadEventImage(event)

            layout.setOnClickListener { onEventClick(event) }
        }

        private fun loadEventImage(event: Event) {
            if (!event.main_image.isNullOrBlank()) {
                // Cargar imagen real del evento
                Glide.with(layout.context)
                    .load(event.main_image)
                    .apply(
                        RequestOptions()
                            .placeholder(getLoadingPlaceholder()) // Imagen mientras carga
                            .error(getErrorPlaceholder()) // Imagen si falla la carga
                            .transform(RoundedCorners(16)) // Esquinas redondeadas
                            .centerCrop() // Centrar y recortar
                    )
                    .into(ivEventImage)
            } else {
                // Imagen por defecto según el tipo de evento
                loadDefaultImage(event.type)
            }
        }

        private fun loadDefaultImage(eventType: String) {
            val defaultImageResource = getDefaultImageForEventType(eventType)

            Glide.with(layout.context)
                .load(defaultImageResource)
                .apply(
                    RequestOptions()
                        .transform(RoundedCorners(16))
                        .centerCrop()
                )
                .into(ivEventImage)
        }

        private fun getDefaultImageForEventType(type: String): Int {
            return when (type.lowercase()) {
                "boda" -> android.R.drawable.ic_menu_my_calendar
                "fiesta" -> android.R.drawable.ic_media_play
                "conferencia" -> android.R.drawable.ic_menu_info_details
                "taller" -> android.R.drawable.ic_menu_edit
                "otro" -> android.R.drawable.ic_menu_gallery
                else -> android.R.drawable.ic_menu_gallery
            }
        }

        private fun getLoadingPlaceholder(): Int {
            return android.R.drawable.ic_menu_rotate
        }

        private fun getErrorPlaceholder(): Int {
            return android.R.drawable.ic_menu_close_clear_cancel
        }

        private fun getEventTypeColor(type: String): Int {
            return when (type.lowercase()) {
                "boda" -> Color.parseColor("#E91E63")
                "fiesta" -> Color.parseColor("#FF9800")
                "conferencia" -> Color.parseColor("#2196F3")
                "taller" -> Color.parseColor("#4CAF50")
                "otro" -> Color.parseColor("#9C27B0")
                else -> Color.parseColor("#4CAF50")
            }
        }
    }
}