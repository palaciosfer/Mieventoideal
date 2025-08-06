package com.mievento.ideal.presentation.views

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.EventParticipation
import com.mievento.ideal.data.repositories.ParticipationRepository
import com.mievento.ideal.presentation.adapters.EventParticipantAdapter
import com.mievento.ideal.presentation.presenters.EventParticipantsPresenter
import com.mievento.ideal.presentation.presenters.EventParticipantsView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class EventParticipantsActivity : AppCompatActivity(), EventParticipantsView {

    private lateinit var presenter: EventParticipantsPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: EventParticipantAdapter

    private var eventId = 0
    private var eventName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        eventName = intent.getStringExtra("event_name") ?: "Evento"

        val tokenManager = TokenManager(this)
        val participationRepository = ParticipationRepository(tokenManager)
        presenter = EventParticipantsPresenter(this, participationRepository)

        setupUI()
        presenter.loadEventParticipants(eventId)
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
            text = "Participantes: $eventName"
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(32, 0, 0, 0)
        }

        toolbar.addView(btnBack)
        toolbar.addView(title)

        // Actions info
        val actionsInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.parseColor("#E8F5E8"))
        }

        val infoTitle = TextView(this).apply {
            text = "Gestión de Participantes"
            textSize = 16f
            setTextColor(Color.parseColor("#2E7D32"))
            setPadding(0, 0, 0, 8)
        }

        val infoText = TextView(this).apply {
            text = "Para cada solicitud puedes: ✅ Aceptar - 🔄 Reagendar - ❌ Rechazar"
            textSize = 14f
            setTextColor(Color.parseColor("#388E3C"))
        }

        actionsInfo.addView(infoTitle)
        actionsInfo.addView(infoText)

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
            layoutManager = LinearLayoutManager(this@EventParticipantsActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay solicitudes de participación para este evento"
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
        mainLayout.addView(actionsInfo)
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        adapter = EventParticipantAdapter(
            participants = emptyList(),
            onAcceptClick = { participant: EventParticipation ->
                showResponseDialog(participant, "aceptado")
            },
            onRescheduleClick = { participant: EventParticipation ->
                showRescheduleDialog(participant)
            },
            onRejectClick = { participant: EventParticipation ->
                showResponseDialog(participant, "rechazado")
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showResponseDialog(participant: EventParticipation, action: String) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val actionText = when (action) {
            "aceptado" -> "aceptar"
            "rechazado" -> "rechazar"
            else -> action
        }

        val titleText = TextView(this).apply {
            text = "${actionText.capitalize()} participación"
            textSize = 18f
            setTextColor(Color.parseColor("#212121"))
            setPadding(0, 0, 0, 16)
        }

        val participantInfo = TextView(this).apply {
            val name = participant.full_name ?: participant.user_name ?: "Usuario"
            val notes = participant.notes ?: "Sin notas"
            text = "Participante: $name\nNotas: $notes"
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 0, 0, 16)
        }

        val etResponse = EditText(this).apply {
            hint = "Mensaje para el participante (opcional)"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
            minLines = 2
        }

        dialogView.addView(titleText)
        dialogView.addView(participantInfo)
        dialogView.addView(etResponse)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(actionText.capitalize()) { _, _ ->
                presenter.respondToParticipant(
                    eventId,
                    participant.user_id,
                    action,
                    etResponse.text.toString()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRescheduleDialog(participant: EventParticipation) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val titleText = TextView(this).apply {
            text = "Reagendar Participación"
            textSize = 18f
            setTextColor(Color.parseColor("#212121"))
            setPadding(0, 0, 0, 16)
        }

        val infoText = TextView(this).apply {
            text = "Esta acción notificará al participante que necesita coordinar una nueva fecha."
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 0, 0, 16)
        }

        val etMessage = EditText(this).apply {
            hint = "Mensaje sobre el reagendamiento"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
            minLines = 3
        }

        dialogView.addView(titleText)
        dialogView.addView(infoText)
        dialogView.addView(etMessage)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Enviar Reagendamiento") { _, _ ->
                presenter.respondToParticipant(
                    eventId,
                    participant.user_id,
                    "reagendar",
                    etMessage.text.toString()
                )
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

    override fun showParticipants(participants: List<EventParticipation>) {
        if (participants.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            adapter.updateParticipants(participants)
        }
    }

    override fun onParticipantResponseSent() {
        showToast("Respuesta enviada al participante")
    }
}