package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.RescheduleRequest
import com.mievento.ideal.utils.toString
import com.mievento.ideal.utils.toDate

class AdminRescheduleAdapter(
    private var requests: List<RescheduleRequest>,
    private val onAcceptClick: (RescheduleRequest) -> Unit,
    private val onRejectClick: (RescheduleRequest) -> Unit
) : RecyclerView.Adapter<AdminRescheduleAdapter.ViewHolder>() {

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
        private lateinit var tvUserName: TextView
        private lateinit var tvCurrentInfo: TextView
        private lateinit var tvNewInfo: TextView
        private lateinit var tvReason: TextView
        private lateinit var tvStatus: TextView
        private lateinit var buttonLayout: LinearLayout
        private lateinit var btnAccept: Button
        private lateinit var btnReject: Button

        init {
            setupViews()
        }

        private fun setupViews() {
            tvEventName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvUserName = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvCurrentInfo = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvNewInfo = TextView(layout.context).apply {
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

            buttonLayout = LinearLayout(layout.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            btnAccept = Button(layout.context).apply {
                text = "Aceptar"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
            }

            btnReject = Button(layout.context).apply {
                text = "Rechazar"
                setBackgroundColor(Color.parseColor("#F44336"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
            }

            buttonLayout.addView(btnAccept)
            buttonLayout.addView(btnReject, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 0, 0)
            })

            layout.addView(tvEventName)
            layout.addView(tvUserName)
            layout.addView(tvCurrentInfo)
            layout.addView(tvNewInfo)
            layout.addView(tvReason)
            layout.addView(tvStatus)
            layout.addView(buttonLayout)
        }

        fun bind(request: RescheduleRequest) {
            tvEventName.text = request.event_name ?: "Evento"
            tvUserName.text = "Solicitado por: ${request.user_name ?: "Usuario"}"

            val currentDate = "Fecha actual: Por definir"
            tvCurrentInfo.text = currentDate

            val newDate = request.new_date.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: request.new_date
            tvNewInfo.text = "Nueva fecha: $newDate ${request.new_time}"

            tvReason.text = if (!request.reason.isNullOrBlank()) {
                "Razón: ${request.reason}"
            } else {
                "Sin razón especificada"
            }

            tvStatus.text = request.status.capitalize()
            tvStatus.setBackgroundColor(getStatusColor(request.status))

            // Mostrar botones solo si está pendiente
            if (request.status == "pendiente") {
                buttonLayout.visibility = View.VISIBLE
                btnAccept.setOnClickListener { onAcceptClick(request) }
                btnReject.setOnClickListener { onRejectClick(request) }
            } else {
                buttonLayout.visibility = View.GONE
            }
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