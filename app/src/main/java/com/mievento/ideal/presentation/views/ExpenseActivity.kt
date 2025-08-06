package com.mievento.ideal.presentation.views

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mievento.ideal.data.models.Expense
import com.mievento.ideal.data.repositories.ExpenseRepository
import com.mievento.ideal.presentation.adapters.ExpenseAdapter
import com.mievento.ideal.presentation.presenters.ExpenseListPresenter
import com.mievento.ideal.presentation.presenters.ExpenseListView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible
import java.text.SimpleDateFormat
import java.util.*

class ExpenseActivity : AppCompatActivity(), ExpenseListView {

    private lateinit var presenter: ExpenseListPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var expenseAdapter: ExpenseAdapter

    private var eventId = 0
    private var eventName = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        eventName = intent.getStringExtra("event_name") ?: "Evento"

        val tokenManager = TokenManager(this)
        val expenseRepository = ExpenseRepository(tokenManager)
        presenter = ExpenseListPresenter(this, expenseRepository)

        setupUI()
        presenter.loadExpenses(eventId)
    }

    private fun setupUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // Toolbar
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 48, 32, 24)
            setBackgroundColor(Color.parseColor("#2196F3"))
            gravity = Gravity.CENTER_VERTICAL
        }

        val btnBack = Button(this).apply {
            text = "←"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.WHITE)
            textSize = 20f
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = "Gastos - $eventName"
            textSize = 18f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(32, 0, 0, 0)
        }

        toolbar.addView(btnBack)
        toolbar.addView(title)

        // Content container
        val contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay gastos registrados.\n¡Registra el primer gasto!"
            textSize = 16f
            setTextColor(Color.parseColor("#757575"))
            gravity = Gravity.CENTER
            setPadding(32)
            gone()
        }

        // Progress bar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // FAB
        fab = FloatingActionButton(this).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setOnClickListener {
                showCreateExpenseDialog()
            }
        }

        // Add views to container with proper LayoutParams
        contentContainer.addView(recyclerView)
        contentContainer.addView(emptyView)

        // Progress bar with CENTER gravity
        val progressParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        contentContainer.addView(progressBar, progressParams)

        // FAB with BOTTOM | END gravity and margins
        val fabParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 48, 48)
        }
        contentContainer.addView(fab, fabParams)

        mainLayout.addView(toolbar)
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        expenseAdapter = ExpenseAdapter(
            emptyList(),
            onExpenseClick = { expense -> showEditExpenseDialog(expense) },
            onExpenseLongClick = { expense -> showExpenseOptionsDialog(expense) }
        )
        recyclerView.adapter = expenseAdapter
    }

    private fun showExpenseOptionsDialog(expense: Expense) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle(expense.description)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditExpenseDialog(expense)
                    1 -> showDeleteDialog(expense)
                }
            }
            .show()
    }

    private fun showDeleteDialog(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Gasto")
            .setMessage("¿Estás seguro de que quieres eliminar '${expense.description}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                presenter.deleteExpense(expense.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun showLoading() {
        progressBar.visible()
        recyclerView.gone()
        emptyView.gone()
    }

    override fun hideLoading() {
        progressBar.gone()
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun showExpenses(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            expenseAdapter.updateExpenses(expenses)
        }
    }

    override fun onExpenseCreated() {
        showToast("Gasto registrado")
    }

    override fun onExpenseUpdated() {
        showToast("Gasto actualizado")
        presenter.loadExpenses(eventId)
    }

    override fun onExpenseDeleted() {
        showToast("Gasto eliminado")
        presenter.loadExpenses(eventId)
    }

    override fun showCreateExpenseDialog() {
        showExpenseFormDialog(null)
    }

    override fun showEditExpenseDialog(expense: Expense) {
        showExpenseFormDialog(expense)
    }

    private fun showExpenseFormDialog(expense: Expense?) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val etCategory = EditText(this).apply {
            hint = "Categoría *"
            setText(expense?.category ?: "")
        }

        val etDescription = EditText(this).apply {
            hint = "Descripción *"
            setText(expense?.description ?: "")
        }

        val etAmount = EditText(this).apply {
            hint = "Monto *"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(expense?.amount?.toString() ?: "")
        }

        var selectedDate = expense?.expense_date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val btnDate = Button(this).apply {
            text = "Fecha: $selectedDate"
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#212121"))
            setOnClickListener {
                showDatePicker { date ->
                    selectedDate = date
                    text = "Fecha: $date"
                }
            }
        }

        val etNotes = EditText(this).apply {
            hint = "Notas"
            setText(expense?.notes ?: "")
        }

        dialogView.addView(etCategory)
        dialogView.addView(etDescription)
        dialogView.addView(etAmount)
        dialogView.addView(btnDate)
        dialogView.addView(etNotes)

        AlertDialog.Builder(this)
            .setTitle(if (expense == null) "Registrar Gasto" else "Editar Gasto")
            .setView(dialogView)
            .setPositiveButton(if (expense == null) "Registrar" else "Actualizar") { _, _ ->
                if (expense == null) {
                    presenter.createExpense(
                        eventId,
                        etCategory.text.toString(),
                        etDescription.text.toString(),
                        etAmount.text.toString(),
                        selectedDate,
                        etNotes.text.toString()
                    )
                } else {
                    presenter.updateExpense(
                        expense.id,
                        etCategory.text.toString(),
                        etDescription.text.toString(),
                        etAmount.text.toString(),
                        selectedDate,
                        etNotes.text.toString()
                    )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val date = dateFormat.format(calendar.time)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}