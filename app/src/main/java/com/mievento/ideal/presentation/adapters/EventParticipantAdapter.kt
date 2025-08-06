package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.EventParticipation

class EventParticipantAdapter(
    private var participants: List<EventParticipation>,
    private val onAcceptClick: (EventParticipation) -> Unit,
    private val onRescheduleClick: (EventParticipation) -> Unit,
    private val onRejectClick: (EventParticipation) -> Unit
) : RecyclerView.Adapter<EventParticipantAdapter.ViewHolder>() {

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
        holder.bind(participants[position])
    }

    override fun getItemCount(): Int = participants.size

    fun updateParticipants(newParticipants: List<EventParticipation>) {
        participants = newParticipants
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvUserName: TextView
        private lateinit var tvUserInfo: TextView
        private lateinit var tvNotes: TextView
        private lateinit var tvStatus: TextView
        private lateinit var tvParticipationDate: TextView
        private lateinit var buttonLayout: LinearLayout
        private lateinit var btnAccept: Button
        private lateinit var btnReschedule: Button
        private lateinit var btnReject: Button

        init {
            setupViews()
        }

        private fun setupViews() {
            tvUserName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
                setPadding(0, 0, 0, 8)
            }

            tvUserInfo = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, 0, 0, 8)
            }

            tvNotes = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, 0, 0, 8)
                visibility = View.GONE
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
                setPadding(0, 8, 0, 16)
            }

            // Contenedor de los 3 botones de acción
            buttonLayout = LinearLayout(layout.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }

            btnAccept = Button(layout.context).apply {
                text = "✅ Aceptar"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setPadding(12, 8, 12, 8)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
            }

            btnReschedule = Button(layout.context).apply {
                text = "🔄 Reagendar"
                setBackgroundColor(Color.parseColor("#2196F3"))
                setTextColor(Color.WHITE)
                setPadding(12, 8, 12, 8)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
            }

            btnReject = Button(layout.context).apply {
                text = "❌ Rechazar"
                setBackgroundColor(Color.parseColor("#F44336"))
                setTextColor(Color.WHITE)
                setPadding(12, 8, 12, 8)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
            }

            buttonLayout.addView(btnAccept)
            buttonLayout.addView(btnReschedule)
            buttonLayout.addView(btnReject)

            layout.addView(tvUserName)
            layout.addView(tvUserInfo)
            layout.addView(tvNotes)
            layout.addView(tvStatus)
            layout.addView(tvParticipationDate)
            layout.addView(buttonLayout)
        }

        fun bind(participant: EventParticipation) {
            // MANEJO SEGURO de propiedades que pueden ser nulas
            val displayName = when {
                !participant.full_name.isNullOrBlank() -> participant.full_name
                !participant.user_name.isNullOrBlank() -> participant.user_name
                else -> "Usuario #${participant.user_id}"
            }
            tvUserName.text = "👤 $displayName"

            // MANEJO SEGURO de email y phone
            val emailText = if (!participant.email.isNullOrBlank()) {
                participant.email
            } else {
                "No disponible"
            }

            val phoneText = if (!participant.phone.isNullOrBlank()) {
                participant.phone
            } else {
                "No disponible"
            }

            tvUserInfo.text = "📧 Email: $emailText\n📞 Teléfono: $phoneText"

            // Mostrar notas si existen
            if (!participant.notes.isNullOrBlank()) {
                tvNotes.text = "📝 Notas: ${participant.notes}"
                tvNotes.visibility = View.VISIBLE
            } else {
                tvNotes.visibility = View.GONE
            }

            // Estado de participación
            tvStatus.text = when (participant.status) {
                "pendiente" -> "⏳ Pendiente"
                "aceptado" -> "✅ Aceptado"
                "rechazado" -> "❌ Rechazado"
                "reagendar" -> "🔄 Por Reagendar"
                else -> participant.status.capitalize()
            }
            tvStatus.setBackgroundColor(getStatusColor(participant.status))

            // Fecha de participación (manejo seguro)
            val participationDate = try {
                // Intentar formatear la fecha, si falla usar como está
                participant.participation_date
            } catch (e: Exception) {
                participant.participation_date
            }
            tvParticipationDate.text = "📅 Solicitado: $participationDate"

            // Mostrar botones solo si está pendiente
            if (participant.status == "pendiente") {
                buttonLayout.visibility = View.VISIBLE
                btnAccept.setOnClickListener { onAcceptClick(participant) }
                btnReschedule.setOnClickListener { onRescheduleClick(participant) }
                btnReject.setOnClickListener { onRejectClick(participant) }
            } else {
                buttonLayout.visibility = View.GONE
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