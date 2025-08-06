package com.mievento.ideal.presentation.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.mievento.ideal.data.repositories.RescheduleRepository
import com.mievento.ideal.presentation.presenters.RescheduleRequestPresenter
import com.mievento.ideal.presentation.presenters.RescheduleRequestView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible
import java.text.SimpleDateFormat
import java.util.*

class RescheduleRequestActivity : AppCompatActivity(), RescheduleRequestView {

    private lateinit var presenter: RescheduleRequestPresenter
    private lateinit var etDescription: EditText
    private lateinit var etNotes: EditText
    private lateinit var etReason: EditText
    private lateinit var btnDate: Button
    private lateinit var btnTime: Button
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar

    private var eventId = 0
    private var eventName = ""
    private var selectedDate = ""
    private var selectedTime = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        eventName = intent.getStringExtra("event_name") ?: "Evento"

        val tokenManager = TokenManager(this)
        val rescheduleRepository = RescheduleRepository(tokenManager)
        presenter = RescheduleRequestPresenter(this, rescheduleRepository)

        setupUI()
    }

    private fun setupUI() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }

        // Toolbar
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 32)
            gravity = Gravity.CENTER_VERTICAL
        }

        val btnBack = Button(this).apply {
            text = "← Volver"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#2196F3"))
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = "Reagendar: $eventName"
            textSize = 18f
            setTextColor(Color.parseColor("#212121"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
        }

        toolbar.addView(btnBack)
        toolbar.addView(title)

        // Info text
        val infoText = TextView(this).apply {
            text = "Solicita reagendar el evento con nueva fecha, hora y detalles. El administrador revisará tu solicitud."
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 0, 0, 24)
        }

        // Form fields
        btnDate = Button(this).apply {
            text = "Seleccionar Nueva Fecha *"
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#212121"))
            setOnClickListener { showDatePicker() }
        }

        btnTime = Button(this).apply {
            text = "Seleccionar Nueva Hora *"
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#212121"))
            setOnClickListener { showTimePicker() }
        }

        etDescription = EditText(this).apply {
            hint = "Nueva descripción (opcional)"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        etNotes = EditText(this).apply {
            hint = "Nuevas notas (opcional)"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        etReason = EditText(this).apply {
            hint = "Razón del reagendamiento"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
            minLines = 3
        }

        btnSubmit = Button(this).apply {
            text = "Enviar Solicitud"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setPadding(32)
            setOnClickListener { submitRequest() }
        }

        progressBar = ProgressBar(this).apply {
            gone()
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16)
        }

        mainLayout.addView(toolbar)
        mainLayout.addView(infoText)
        mainLayout.addView(createLabel("Nueva Fecha *"))
        mainLayout.addView(btnDate, layoutParams)
        mainLayout.addView(createLabel("Nueva Hora *"))
        mainLayout.addView(btnTime, layoutParams)
        mainLayout.addView(createLabel("Nueva Descripción"))
        mainLayout.addView(etDescription, layoutParams)
        mainLayout.addView(createLabel("Nuevas Notas"))
        mainLayout.addView(etNotes, layoutParams)
        mainLayout.addView(createLabel("Razón del Reagendamiento"))
        mainLayout.addView(etReason, layoutParams)
        mainLayout.addView(btnSubmit, layoutParams)
        mainLayout.addView(progressBar, layoutParams)

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }

    private fun createLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 16, 0, 8)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = dateFormat.format(calendar.time)
                btnDate.text = "Fecha: " + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = timeFormat.format(calendar.time)
                btnTime.text = "Hora: $selectedTime"
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun submitRequest() {
        presenter.createRescheduleRequest(
            eventId = eventId,
            newDate = selectedDate,
            newTime = selectedTime,
            newDescription = etDescription.text.toString(),
            newNotes = etNotes.text.toString(),
            reason = etReason.text.toString()
        )
    }

    override fun showLoading() {
        progressBar.visible()
        btnSubmit.isEnabled = false
    }

    override fun hideLoading() {
        progressBar.gone()
        btnSubmit.isEnabled = true
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun onRequestCreated() {
        showToast("Solicitud enviada exitosamente. El administrador la revisará.")
        finish()
    }
}