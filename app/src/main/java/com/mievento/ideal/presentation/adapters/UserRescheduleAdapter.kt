package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.RescheduleRequest
import com.mievento.ideal.utils.toString
import com.mievento.ideal.utils.toDate

class UserRescheduleAdapter(
    private var requests: List<RescheduleRequest>
) : RecyclerView.Adapter<UserRescheduleAdapter.ViewHolder>() {

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
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateRequests(newRequests: List<RescheduleRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvEventName: TextView
        private lateinit var tvNewDate: TextView
        private lateinit var tvReason: TextView
        private lateinit var tvStatus: TextView
        private lateinit var tvResponse: TextView
        private lateinit var tvRequestDate: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvEventName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvNewDate = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#2196F3"))
            }

            tvReason = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvStatus = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER
            }

            tvResponse = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                visibility = View.GONE
            }

            tvRequestDate = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.parseColor("#757575"))
            }

            layout.addView(tvEventName)
            layout.addView(tvNewDate)
            layout.addView(tvReason)
            layout.addView(tvStatus)
            layout.addView(tvResponse)
            layout.addView(tvRequestDate)
        }

        fun bind(request: RescheduleRequest) {
            tvEventName.text = request.event_name ?: "Evento"

            val newDate = request.new_date.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: request.new_date
            tvNewDate.text = "Nueva fecha solicitada: $newDate ${request.new_time}"

            tvReason.text = if (!request.reason.isNullOrBlank()) {
                "Razón: ${request.reason}"
            } else {
                "Sin razón especificada"
            }

            tvStatus.text = when (request.status) {
                "pendiente" -> "En Proceso"
                "aceptada" -> "Aceptada"
                "rechazada" -> "Rechazada"
                else -> request.status.capitalize()
            }
            tvStatus.setBackgroundColor(getStatusColor(request.status))

            // Mostrar respuesta del admin si existe
            if (!request.admin_response.isNullOrBlank() && request.status != "pendiente") {
                tvResponse.visibility = View.VISIBLE
                tvResponse.text = "Respuesta: ${request.admin_response}"
            } else {
                tvResponse.visibility = View.GONE
            }

            val requestDate = request.created_at.toDate("yyyy-MM-dd HH:mm:ss")?.toString("dd/MM/yyyy HH:mm") ?: request.created_at
            tvRequestDate.text = "Solicitado: $requestDate"
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "pendiente" -> Color.parseColor("#FF9800")
                "aceptada" -> Color.parseColor("#4CAF50")
                "rechazada" -> Color.parseColor("#F44336")
                else -> Color.parseColor("#757575")
            }
        }
    }
}