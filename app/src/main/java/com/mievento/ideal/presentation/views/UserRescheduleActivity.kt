package com.mievento.ideal.presentation.views

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mievento.ideal.data.models.RescheduleRequest
import com.mievento.ideal.data.repositories.RescheduleRepository
import com.mievento.ideal.presentation.adapters.UserRescheduleAdapter
import com.mievento.ideal.presentation.presenters.UserReschedulePresenter
import com.mievento.ideal.presentation.presenters.UserRescheduleView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class UserRescheduleActivity : AppCompatActivity(), UserRescheduleView {

    private lateinit var presenter: UserReschedulePresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: UserRescheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val rescheduleRepository = RescheduleRepository(tokenManager)
        presenter = UserReschedulePresenter(this, rescheduleRepository)

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
            text = "Mis Solicitudes de Reagendamiento"
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
            layoutManager = LinearLayoutManager(this@UserRescheduleActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No has realizado solicitudes de reagendamiento"
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
        adapter = UserRescheduleAdapter(
            requests = emptyList()
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
}