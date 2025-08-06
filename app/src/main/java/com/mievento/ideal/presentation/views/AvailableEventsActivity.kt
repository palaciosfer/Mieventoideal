package com.mievento.ideal.presentation.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.Event
import com.mievento.ideal.data.repositories.ParticipationRepository
import com.mievento.ideal.presentation.adapters.AvailableEventAdapter
import com.mievento.ideal.presentation.presenters.AvailableEventsPresenter
import com.mievento.ideal.presentation.presenters.AvailableEventsView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class AvailableEventsActivity : AppCompatActivity(), AvailableEventsView {

    private lateinit var presenter: AvailableEventsPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: AvailableEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val participationRepository = ParticipationRepository(tokenManager)
        presenter = AvailableEventsPresenter(this, participationRepository)

        setupUI()
        presenter.loadAvailableEvents()
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
            text = "Eventos Disponibles"
            textSize = 18f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(32, 0, 0, 0)
        }

        toolbar.addView(btnBack)
        toolbar.addView(title)

        // Info text
        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.parseColor("#E3F2FD"))
        }

        val infoText = TextView(this).apply {
            text = "Selecciona un evento para solicitar participar. Una vez aceptado, podrás gestionar tu participación."
            textSize = 14f
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
        }

        infoLayout.addView(infoText)

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
            layoutManager = LinearLayoutManager(this@AvailableEventsActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay eventos disponibles en este momento"
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

        contentContainer.addView(recyclerView)
        contentContainer.addView(emptyView)

        val progressParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        contentContainer.addView(progressBar, progressParams)

        mainLayout.addView(toolbar)
        mainLayout.addView(infoLayout)
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        adapter = AvailableEventAdapter(
            events = emptyList(),
            onEventClick = { event -> showParticipationDialog(event) }
        )
        recyclerView.adapter = adapter
    }

    private fun showParticipationDialog(event: Event) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val titleText = TextView(this).apply {
            text = "Solicitar Participación"
            textSize = 18f
            setTextColor(Color.parseColor("#212121"))
            setPadding(0, 0, 0, 16)
        }

        val eventInfo = TextView(this).apply {
            text = "Evento: ${event.name}\nFecha: ${event.event_date} ${event.event_time}\nUbicación: ${event.location}"
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 0, 0, 16)
        }

        val etNotes = EditText(this).apply {
            hint = "Notas adicionales (opcional)"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
            minLines = 2
        }

        dialogView.addView(titleText)
        dialogView.addView(eventInfo)
        dialogView.addView(etNotes)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Solicitar Participación") { _, _ ->
                presenter.requestParticipation(event.id, etNotes.text.toString())
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

    override fun showEvents(events: List<Event>) {
        if (events.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            adapter.updateEvents(events)
        }
    }

    override fun onParticipationRequested() {
        showToast("Solicitud enviada. Espera la respuesta del organizador.")
        finish()
    }
}