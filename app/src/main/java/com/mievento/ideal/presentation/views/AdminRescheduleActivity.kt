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
import com.mievento.ideal.data.models.RescheduleRequest
import com.mievento.ideal.data.repositories.RescheduleRepository
import com.mievento.ideal.presentation.adapters.AdminRescheduleAdapter
import com.mievento.ideal.presentation.presenters.AdminReschedulePresenter
import com.mievento.ideal.presentation.presenters.AdminRescheduleView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class AdminRescheduleActivity : AppCompatActivity(), AdminRescheduleView {

    private lateinit var presenter: AdminReschedulePresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: AdminRescheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val rescheduleRepository = RescheduleRepository(tokenManager)
        presenter = AdminReschedulePresenter(this, rescheduleRepository)

        setupUI()
        presenter.loadRequests()
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
            text = "Solicitudes de Reagendamiento"
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
            layoutManager = LinearLayoutManager(this@AdminRescheduleActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay solicitudes de reagendamiento"
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
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        adapter = AdminRescheduleAdapter(
            requests = emptyList(),
            onAcceptClick = { request -> showResponseDialog(request, "aceptada") },
            onRejectClick = { request -> showResponseDialog(request, "rechazada") }
        )
        recyclerView.adapter = adapter
    }

    private fun showResponseDialog(request: RescheduleRequest, status: String) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val messageText = TextView(this).apply {
            text = if (status == "aceptada") "Aceptar reagendamiento" else "Rechazar reagendamiento"
            textSize = 18f
            setTextColor(Color.parseColor("#212121"))
            setPadding(0, 0, 0, 16)
        }

        val etResponse = EditText(this).apply {
            hint = "Mensaje para el usuario (opcional)"
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
            minLines = 3
        }

        dialogView.addView(messageText)
        dialogView.addView(etResponse)

        AlertDialog.Builder(this)
            .setTitle("Responder Solicitud")
            .setView(dialogView)
            .setPositiveButton(if (status == "aceptada") "Aceptar" else "Rechazar") { _, _ ->
                presenter.respondToRequest(
                    request.id,
                    status,
                    etResponse.text.toString()
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

    override fun showRequests(requests: List<RescheduleRequest>) {
        if (requests.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            adapter.updateRequests(requests)
        }
    }

    override fun onRequestResponded() {
        showToast("Respuesta enviada")
        presenter.loadRequests()
    }
}