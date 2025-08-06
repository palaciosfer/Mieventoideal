package com.mievento.ideal.presentation.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Expense
import com.mievento.ideal.utils.toString
import com.mievento.ideal.utils.toDate
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onExpenseClick: (Expense) -> Unit,
    private val onExpenseLongClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.WHITE)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size


    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout = itemView as LinearLayout
        private lateinit var tvDescription: TextView
        private lateinit var tvCategory: TextView
        private lateinit var tvAmount: TextView
        private lateinit var tvDate: TextView
        private lateinit var tvProvider: TextView

        init {
            setupViews()
        }

        private fun setupViews() {
            tvDescription = TextView(layout.context).apply {
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
            }

            tvCategory = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvAmount = TextView(layout.context).apply {
                textSize = 16f
                setTextColor(Color.parseColor("#F44336"))
            }

            tvDate = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            tvProvider = TextView(layout.context).apply {
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
            }

            layout.addView(tvDescription)
            layout.addView(tvCategory)
            layout.addView(tvAmount)
            layout.addView(tvDate)
            layout.addView(tvProvider)
        }

        fun bind(expense: Expense) {
            tvDescription.text = expense.description
            tvCategory.text = "Categoría: ${expense.category}"

            val amount = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(expense.amount)
            tvAmount.text = amount

            val date = expense.expense_date.toDate("yyyy-MM-dd")?.toString("dd/MM/yyyy") ?: expense.expense_date
            tvDate.text = "Fecha: $date"

            tvProvider.text = if (!expense.provider_name.isNullOrBlank()) {
                "Proveedor: ${expense.provider_name}"
            } else {
                "Sin proveedor asociado"
            }

            layout.setOnClickListener { onExpenseClick(expense) }
            layout.setOnLongClickListener {
                onExpenseLongClick(expense)
                true
            }
        }
    }
}