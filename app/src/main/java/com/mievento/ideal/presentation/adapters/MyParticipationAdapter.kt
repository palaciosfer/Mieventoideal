package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.EventParticipation
import com.mievento.ideal.utils.toDate
import com.mievento.ideal.utils.toString

class MyParticipationAdapter(
    private var participations: List<EventParticipation>
) : RecyclerView.Adapter<MyParticipationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
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
        holder.bind(participations[position])
    }

    override fun getItemCount(): Int = participations.size

    fun updateParticipations(newParticipations: List<EventParticipation>) {
        participations = newParticipations
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvEventName: TextView
        private lateinit var tvEventDate: TextView
        private lateinit var tvLocation: TextView
        private lateinit var tvStatus: TextView
        private lateinit var tvParticipationDate: TextView
        private lateinit var tvNotes: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvEventName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvEventDate = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvLocation = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvStatus = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER
            }

            tvParticipationDate = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.parseColor("#757575"))
            }

            tvNotes = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                visibility = View.GONE
            }

            layout.addView(tvEventName)
            layout.addView(tvEventDate)
            layout.addView(tvLocation)
            layout.addView(tvStatus)
            layout.addView(tvParticipationDate)
            layout.addView(tvNotes)
        }

        fun bind(participation: EventParticipation) {
            tvEventName.text = participation.event_name ?: "Evento"

            val eventDate = participation.event_date?.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: participation.event_date
            tvEventDate.text = "📅 $eventDate ${participation.event_time ?: ""}"

            tvLocation.text = "📍 ${participation.location ?: "Ubicación no disponible"}"

            tvStatus.text = when (participation.status) {
                "pendiente" -> "⏳ Pendiente"
                "aceptado" -> "✅ Aceptado"
                "rechazado" -> "❌ Rechazado"
                "reagendar" -> "🔄 Por Reagendar"
                else -> participation.status.capitalize()
            }
            tvStatus.setBackgroundColor(getStatusColor(participation.status))

            val participationDate = participation.participation_date.toDate("yyyy-MM-dd HH:mm:ss")?.toString("dd/MM/yyyy HH:mm") ?: participation.participation_date
            tvParticipationDate.text = "Solicitado: $participationDate"

            if (!participation.notes.isNullOrBlank()) {
                tvNotes.text = "Notas: ${participation.notes}"
                tvNotes.visibility = View.VISIBLE
            } else {
                tvNotes.visibility = View.GONE
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "pendiente" -> Color.parseColor("#FF9800")
                "aceptado" -> Color.parseColor("#4CAF50")
                "rechazado" -> Color.parseColor("#F44336")
                "reagendar" -> Color.parseColor("#2196F3")
                else -> Color.parseColor("#757575")
            }
        }
    }
}