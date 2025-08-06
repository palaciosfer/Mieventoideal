package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Notification
import com.mievento.ideal.utils.toString
import com.mievento.ideal.utils.toDate

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)

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
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvTitle: TextView
        private lateinit var tvMessage: TextView
        private lateinit var tvDate: TextView
        private lateinit var tvType: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvTitle = TextView(layout.context).apply {
                textSize = 16f
                setTextColor(Color.parseColor("#212121"))
            }

            tvMessage = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvDate = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.parseColor("#757575"))
            }

            tvType = TextView(layout.context).apply {
                textSize = 10f
                setTextColor(Color.WHITE)
                setPadding(12, 6, 12, 6)
                setBackgroundColor(Color.parseColor("#2196F3"))
            }

            val headerLayout = LinearLayout(layout.context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            headerLayout.addView(tvTitle, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            headerLayout.addView(tvType)

            layout.addView(headerLayout)
            layout.addView(tvMessage)
            layout.addView(tvDate)
        }

        fun bind(notification: Notification) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message

            val date = notification.created_at.toDate("yyyy-MM-dd HH:mm:ss")?.toString("dd/MM/yyyy HH:mm") ?: notification.created_at
            tvDate.text = date

            tvType.text = when (notification.type) {
                "reschedule" -> "REAGENDAMIENTO"
                "event" -> "EVENTO"
                "general" -> "GENERAL"
                else -> notification.type.uppercase()
            }

            // Cambiar apariencia si no está leída
            val backgroundColor = if (notification.is_read) Color.WHITE else Color.parseColor("#F0F8FF")
            layout.setBackgroundColor(backgroundColor)

            layout.setOnClickListener {
                onNotificationClick(notification)
            }
        }
    }
}