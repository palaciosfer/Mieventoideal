package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Task
import com.mievento.ideal.utils.toString
import com.mievento.ideal.utils.toDate

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit,
    private val onStatusClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.WHITE)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvTitle: TextView
        private lateinit var tvDescription: TextView
        private lateinit var tvDueDate: TextView
        private lateinit var tvPriority: TextView
        private lateinit var tvStatus: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvTitle = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvDescription = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvDueDate = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvPriority = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER
            }

            tvStatus = TextView(layout.context).apply {
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER
            }

            val buttonLayout = LinearLayout(layout.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            buttonLayout.addView(tvPriority)
            buttonLayout.addView(tvStatus, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 0, 0)
            })

            layout.addView(tvTitle)
            layout.addView(tvDescription)
            layout.addView(tvDueDate)
            layout.addView(buttonLayout)
        }

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDescription.text = task.description ?: "Sin descripción"

            if (!task.due_date.isNullOrBlank()) {
                val date = task.due_date.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: task.due_date
                tvDueDate.text = "Vence: $date"
            } else {
                tvDueDate.text = "Sin fecha límite"
            }

            tvPriority.text = task.priority.capitalize()
            tvPriority.setBackgroundColor(getPriorityColor(task.priority))

            tvStatus.text = task.status.replace("_", " ").capitalize()
            tvStatus.setBackgroundColor(getStatusColor(task.status))

            layout.setOnClickListener { onTaskClick(task) }
            layout.setOnLongClickListener {
                onTaskLongClick(task)
                true
            }
            tvStatus.setOnClickListener { onStatusClick(task) }
        }

        private fun getPriorityColor(priority: String): Int {
            return when (priority) {
                "alta" -> Color.parseColor("#F44336")
                "media" -> Color.parseColor("#FF9800")
                "baja" -> Color.parseColor("#4CAF50")
                else -> Color.parseColor("#757575")
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "pendiente" -> Color.parseColor("#FF9800")
                "en_proceso" -> Color.parseColor("#2196F3")
                "finalizada" -> Color.parseColor("#4CAF50")
                else -> Color.parseColor("#757575")
            }
        }
    }
}