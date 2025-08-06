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
import com.mievento.ideal.data.models.Task
import com.mievento.ideal.data.repositories.TaskRepository
import com.mievento.ideal.presentation.adapters.TaskAdapter
import com.mievento.ideal.presentation.presenters.TaskListPresenter
import com.mievento.ideal.presentation.presenters.TaskListView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : AppCompatActivity(), TaskListView {

    private lateinit var presenter: TaskListPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter

    private var eventId = 0
    private var eventName = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        eventName = intent.getStringExtra("event_name") ?: "Evento"

        val tokenManager = TokenManager(this)
        val taskRepository = TaskRepository(tokenManager)
        presenter = TaskListPresenter(this, taskRepository)

        setupUI()
        presenter.loadTasks(eventId)
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
            text = "Tareas - $eventName"
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
            layoutManager = LinearLayoutManager(this@TaskActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay tareas aún.\n¡Agrega la primera tarea!"
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
                showCreateTaskDialog()
            }
        }

        contentContainer.addView(recyclerView)
        contentContainer.addView(emptyView)
        contentContainer.addView(progressBar, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        contentContainer.addView(fab, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.END
        ).apply {
            setMargins(0, 0, 48, 48)
        })

        mainLayout.addView(toolbar)
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        // En el método setupUI(), reemplaza:
        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onTaskClick = { task -> showEditTaskDialog(task) },
            onTaskLongClick = { task -> showTaskOptionsDialog(task) },
            onStatusClick = { task -> showStatusDialog(task) }
        )
        recyclerView.adapter = taskAdapter
    }

    private fun showTaskOptionsDialog(task: Task) {
        val options = arrayOf("Editar", "Cambiar Estado", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle(task.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditTaskDialog(task)
                    1 -> showStatusDialog(task)
                    2 -> showDeleteDialog(task)
                }
            }
            .show()
    }

    private fun showStatusDialog(task: Task) {
        val options = arrayOf("Pendiente", "En Proceso", "Finalizada")
        val currentIndex = when (task.status) {
            "pendiente" -> 0
            "en_proceso" -> 1
            "finalizada" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("Estado de la Tarea")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val newStatus = when (which) {
                    0 -> "pendiente"
                    1 -> "en_proceso"
                    2 -> "finalizada"
                    else -> "pendiente"
                }
                presenter.updateTaskStatus(task.id, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que quieres eliminar '${task.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                presenter.deleteTask(task.id)
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

    override fun showTasks(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            taskAdapter.updateTasks(tasks)
        }
    }

    override fun onTaskCreated() {
        showToast("Tarea creada")
    }

    override fun onTaskUpdated() {
        showToast("Tarea actualizada")
        presenter.loadTasks(eventId)
    }

    override fun onTaskDeleted() {
        showToast("Tarea eliminada")
        presenter.loadTasks(eventId)
    }

    override fun showCreateTaskDialog() {
        showTaskFormDialog(null)
    }

    override fun showEditTaskDialog(task: Task) {
        showTaskFormDialog(task)
    }

    private fun showTaskFormDialog(task: Task?) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val etTitle = EditText(this).apply {
            hint = "Título *"
            setText(task?.title ?: "")
        }

        val etDescription = EditText(this).apply {
            hint = "Descripción"
            setText(task?.description ?: "")
        }

        var selectedDate = task?.due_date ?: ""
        val btnDate = Button(this).apply {
            text = if (selectedDate.isNotBlank()) {
                "Fecha: $selectedDate"
            } else {
                "Seleccionar Fecha (Opcional)"
            }
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#212121"))
            setOnClickListener {
                showDatePicker { date ->
                    selectedDate = date
                    text = "Fecha: $date"
                }
            }
        }

        val spinnerPriority = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@TaskActivity,
                android.R.layout.simple_spinner_item,
                arrayOf("baja", "media", "alta")
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // Set current priority
            task?.let { t ->
                val priorities = arrayOf("baja", "media", "alta")
                val index = priorities.indexOf(t.priority)
                if (index >= 0) setSelection(index)
            }
        }

        dialogView.addView(etTitle)
        dialogView.addView(etDescription)
        dialogView.addView(btnDate)
        dialogView.addView(TextView(this).apply {
            text = "Prioridad:"
            setPadding(0, 16, 0, 8)
        })
        dialogView.addView(spinnerPriority)

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "Agregar Tarea" else "Editar Tarea")
            .setView(dialogView)
            .setPositiveButton(if (task == null) "Agregar" else "Actualizar") { _, _ ->
                if (task == null) {
                    presenter.createTask(
                        eventId,
                        etTitle.text.toString(),
                        etDescription.text.toString(),
                        selectedDate,
                        spinnerPriority.selectedItem.toString()
                    )
                } else {
                    presenter.updateTask(
                        task.id,
                        etTitle.text.toString(),
                        etDescription.text.toString(),
                        selectedDate,
                        spinnerPriority.selectedItem.toString()
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