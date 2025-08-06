package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Guest

class GuestAdapter(
    private var guests: List<Guest>,
    private val onGuestClick: (Guest) -> Unit,
    private val onGuestLongClick: (Guest) -> Unit
) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.WHITE)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return GuestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        holder.bind(guests[position])
    }

    override fun getItemCount(): Int = guests.size

    fun updateGuests(newGuests: List<Guest>) {
        guests = newGuests
        notifyDataSetChanged()
    }

    inner class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvName: TextView
        private lateinit var tvContact: TextView
        private lateinit var tvRelationship: TextView
        private lateinit var tvStatus: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvName = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvContact = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvRelationship = TextView(layout.context).apply {
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
            layout.addView(tvContact)
            layout.addView(tvRelationship)
            layout.addView(tvStatus)
        }

        fun bind(guest: Guest) {
            tvName.text = guest.name

            val contact = when {
                !guest.email.isNullOrBlank() && !guest.phone.isNullOrBlank() ->
                    "${guest.email} | ${guest.phone}"
                !guest.email.isNullOrBlank() -> guest.email
                !guest.phone.isNullOrBlank() -> guest.phone
                else -> "Sin contacto"
            }
            tvContact.text = contact

            tvRelationship.text = if (!guest.relationship.isNullOrBlank()) {
                "Relación: ${guest.relationship}"
            } else {
                "Sin relación especificada"
            }

            tvStatus.text = guest.confirmation_status.capitalize()
            tvStatus.setBackgroundColor(getStatusColor(guest.confirmation_status))

            layout.setOnClickListener { onGuestClick(guest) }
            layout.setOnLongClickListener {
                onGuestLongClick(guest)
                true
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "confirmado" -> Color.parseColor("#4CAF50")
                "pendiente" -> Color.parseColor("#FF9800")
                "rechazado" -> Color.parseColor("#F44336")
                else -> Color.parseColor("#757575")
            }
        }
    }
}