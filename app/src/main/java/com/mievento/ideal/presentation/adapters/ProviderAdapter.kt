package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Provider
import java.text.NumberFormat
import java.util.*

class ProviderAdapter(
    private var providers: List<Provider>,
    private val onProviderClick: (Provider) -> Unit,
    private val onProviderLongClick: (Provider) -> Unit
) : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.WHITE)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return ProviderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        holder.bind(providers[position])
    }

    override fun getItemCount(): Int = providers.size

    fun updateProviders(newProviders: List<Provider>) {
        providers = newProviders
        notifyDataSetChanged()
    }

    inner class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvName: TextView
        private lateinit var tvService: TextView
        private lateinit var tvContact: TextView
        private lateinit var tvBudget: TextView
        private lateinit var tvStatus: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvService = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvContact = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvBudget = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvStatus = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER
            }

            layout.addView(tvName)
            layout.addView(tvService)
            layout.addView(tvContact)
            layout.addView(tvBudget)
            layout.addView(tvStatus)
        }

        fun bind(provider: Provider) {
            tvName.text = provider.name
            tvService.text = "Servicio: ${provider.service}"

            val contact = when {
                !provider.contact_name.isNullOrBlank() -> provider.contact_name
                !provider.email.isNullOrBlank() -> provider.email
                !provider.phone.isNullOrBlank() -> provider.phone
                else -> "Sin contacto"
            }
            tvContact.text = "Contacto: $contact"

            val budget = provider.estimated_budget?.let {
                NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(it)
            } ?: "Sin presupuesto"
            tvBudget.text = "Presupuesto: $budget"

            tvStatus.text = provider.status.capitalize()
            tvStatus.setBackgroundColor(getStatusColor(provider.status))

            layout.setOnClickListener { onProviderClick(provider) }
            layout.setOnLongClickListener {
                onProviderLongClick(provider)
                true
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "cotizacion" -> Color.parseColor("#FF9800")
                "contratado" -> Color.parseColor("#4CAF50")
                "pagado" -> Color.parseColor("#2196F3")
                "cancelado" -> Color.parseColor("#F44336")
                else -> Color.parseColor("#757575")
            }
        }
    }
}