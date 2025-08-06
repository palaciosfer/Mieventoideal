package com.mievento.ideal.presentation.views

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.EventParticipation
import com.mievento.ideal.data.repositories.ParticipationRepository
import com.mievento.ideal.presentation.adapters.MyParticipationAdapter
import com.mievento.ideal.presentation.presenters.MyParticipationsPresenter
import com.mievento.ideal.presentation.presenters.MyParticipationsView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class MyParticipationsActivity : AppCompatActivity(), MyParticipationsView {

    private lateinit var presenter: MyParticipationsPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: MyParticipationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val participationRepository = ParticipationRepository(tokenManager)
        presenter = MyParticipationsPresenter(this, participationRepository)

        setupUI()
        presenter.loadMyParticipations()
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
            text = "Mis Participaciones"
            textSize = 18f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(32, 0, 0, 0)
        }

        toolbar.addView(btnBack)
        toolbar.addView(title)

        // Status info
        val statusInfo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.WHITE)
        }

        val statusColors = listOf(
            "🟡 Pendiente" to Color.parseColor("#FF9800"),
            "🟢 Aceptado" to Color.parseColor("#4CAF50"),
            "🔴 Rechazado" to Color.parseColor("#F44336")
        )

        statusColors.forEach { (text, color) ->
            val statusView = TextView(this).apply {
                this.text = text
                textSize = 12f
                setTextColor(color)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
            }
            statusInfo.addView(statusView)
        }

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
            layoutManager = LinearLayoutManager(this@MyParticipationsActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No has solicitado participar en ningún evento aún.\n¡Explora eventos disponibles!"
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
        mainLayout.addView(statusInfo)
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        adapter = MyParticipationAdapter(
            participations = emptyList()
        )
        recyclerView.adapter = adapter
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

    override fun showParticipations(participations: List<EventParticipation>) {
        if (participations.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            adapter.updateParticipations(participations)
        }
    }
}